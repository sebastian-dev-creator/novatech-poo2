package com.mycompany.novatech.app.ventas;

import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.util.*;

/** Solicitudes abiertas se pueden ajustar; una solicitud convertida conserva su detalle. */
public final class Solicitudes {
    public static final class Pedido {
        public final Venta datos = new Venta();
        public int id, version, ventaId;
        public String fecha;
    }

    private static Connection abrir() throws SQLException {
        return ConexionBD.getInstancia().obtenerConexion();
    }

    static List<Pedido> listar() throws SQLException {
        try (Connection c = abrir(); PreparedStatement p = c.prepareStatement(
                "SELECT * FROM solped ORDER BY id DESC LIMIT 500"); ResultSet r = p.executeQuery()) {
            List<Pedido> pedidos = new ArrayList<>();
            while (r.next()) pedidos.add(cabecera(r));
            return pedidos;
        }
    }

    private static Pedido cabecera(ResultSet r) throws SQLException {
        Pedido p = new Pedido();
        p.id = r.getInt("id");
        p.ventaId = r.getInt("id_venta");
        p.version = r.getInt("version");
        p.fecha = r.getString("fecha");
        p.datos.clave = r.getString("clave");
        p.datos.clienteId = r.getInt("id_cliente");
        p.datos.usuarioId = r.getInt("id_usuario");
        p.datos.tipo = r.getString("tipo");
        p.datos.porcentaje = r.getBigDecimal("porcentaje");
        p.datos.observacion = r.getString("observacion");
        return p;
    }

    static Pedido cargar(Connection c, int id, boolean bloquear) throws SQLException {
        Pedido pedido;
        try (PreparedStatement p = c.prepareStatement("SELECT * FROM solped WHERE id=?" + (bloquear ? " FOR UPDATE" : ""))) {
            p.setInt(1,id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("Solicitud inexistente.");
                pedido = cabecera(r);
            }
        }
        try (PreparedStatement p = c.prepareStatement("SELECT d.*,p.nombre,p.codigo FROM solped_detalle d JOIN productos p ON p.id=d.id_producto WHERE id_solped=? ORDER BY id_producto")) {
            p.setInt(1,id);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    DetalleVenta d = new DetalleVenta(r.getInt("id_producto"),r.getBigDecimal("cantidad"));
                    d.nombre = r.getString("nombre");
                    d.codigo = r.getString("codigo");
                    pedido.datos.detalles.add(d);
                }
            }
        }
        return pedido;
    }

    interface Operacion<T> { T ejecutar(Connection c) throws SQLException; }
    static <T> T transaccion(Operacion<T> operacion) throws SQLException {
        try (Connection c = abrir()) {
            c.setAutoCommit(false);
            try {
                T resultado = operacion.ejecutar(c);
                c.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    static void editable(Pedido pedido, int version) {
        if (pedido.ventaId != 0) throw new IllegalArgumentException("La solicitud ya tiene una venta.");
        if (pedido.version != version) throw new IllegalArgumentException("La solicitud cambió. Ábrela nuevamente.");
    }

    static int guardar(Connection c, Venta v, int usuario, int id, int version) throws SQLException {
        if (id == 0) {
            try (PreparedStatement p = c.prepareStatement(
                    "INSERT INTO solped(clave,id_cliente,id_usuario,tipo,porcentaje,observacion) VALUES(?,?,?,?,?,?) ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id)",Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1,v.clave); p.setInt(2,v.clienteId); p.setInt(3,usuario);
                p.setString(4,v.tipo); p.setBigDecimal(5,v.porcentaje); p.setString(6,v.observacion);
                p.executeUpdate();
            }
            try (PreparedStatement p = c.prepareStatement("SELECT id,id_usuario FROM solped WHERE clave=? FOR UPDATE")) {
                p.setString(1,v.clave);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next() || r.getInt(2)!=usuario) throw new IllegalArgumentException("Formulario de otra sesión.");
                    id = r.getInt(1);
                }
            }
            if (!cargar(c,id,true).datos.detalles.isEmpty()) return id;
        } else {
            editable(cargar(c,id,true),version);
            try (PreparedStatement p = c.prepareStatement("UPDATE solped SET id_cliente=?,tipo=?,porcentaje=?,observacion=?,version=version+1 WHERE id=?")) {
                p.setInt(1,v.clienteId); p.setString(2,v.tipo); p.setBigDecimal(3,v.porcentaje); p.setString(4,v.observacion); p.setInt(5,id); p.executeUpdate();
            }
            try (PreparedStatement p = c.prepareStatement("DELETE FROM solped_detalle WHERE id_solped=?")) {
                p.setInt(1,id); p.executeUpdate();
            }
        }
        try (PreparedStatement p = c.prepareStatement("INSERT INTO solped_detalle(id_solped,id_producto,cantidad) VALUES(?,?,?)")) {
            for (DetalleVenta d : v.detalles) {
                p.setInt(1,id); p.setInt(2,d.productoId); p.setBigDecimal(3,d.cantidad); p.addBatch();
            }
            p.executeBatch();
        }
        return id;
    }

    static void eliminar(int id,int version) throws SQLException {
        transaccion(c -> {
            editable(cargar(c,id,true),version);
            try (PreparedStatement p = c.prepareStatement("DELETE FROM solped WHERE id=?")) {
                p.setInt(1,id); p.executeUpdate();
            }
            return null;
        });
    }
}
