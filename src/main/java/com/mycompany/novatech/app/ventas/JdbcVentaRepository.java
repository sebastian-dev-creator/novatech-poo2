package com.mycompany.novatech.app.ventas;

import com.mycompany.novatech.config.ConexionBD;
import com.mycompany.novatech.app.comercial.Validacion;
import java.sql.*;
import java.math.*;
import java.util.*;

/** Repository: cabecera, líneas y existencias se escriben en una sola transacción. */
public final class JdbcVentaRepository implements VentaRepository {
    private static final BigDecimal MAX_TOTAL=new BigDecimal("999999999999.99");
    private Connection abrir()throws SQLException{return ConexionBD.getInstancia().obtenerConexion();}
    private Venta cabecera(ResultSet r)throws SQLException {
        Venta v=new Venta();v.id=r.getInt("id");v.clienteId=r.getInt("id_cliente");v.usuarioId=r.getInt("id_usuario");v.version=r.getInt("version");
        v.clave=r.getString("clave_creacion");v.tipo=r.getString("tipo_comprobante");v.estado=r.getString("estado");v.medioPago=r.getString("medio_pago");
        v.clienteNombre=r.getString("cliente_nombre");v.clienteDocumento=r.getString("cliente_documento");v.observacion=r.getString("observacion");
        v.subtotal=r.getBigDecimal("subtotal");v.porcentaje=r.getBigDecimal("descuento_porcentaje");v.descuento=r.getBigDecimal("descuento");v.total=r.getBigDecimal("total");
        v.creador=r.getString("usuario_registro");v.actualizador=r.getString("usuario_actualizacion");v.fecha=r.getString("fecha_creacion");v.fechaPago=r.getString("fecha_pago");v.fechaAnulacion=r.getString("fecha_anulacion");return v;
    }
    private Venta buscar(Connection c,int id,boolean bloquear)throws SQLException {
        Venta v;
        try(PreparedStatement p=c.prepareStatement("SELECT * FROM ventas WHERE id=?"+(bloquear?" FOR UPDATE":""))){p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())return null;v=cabecera(r);}}
        try(PreparedStatement p=c.prepareStatement("SELECT * FROM detalle_venta WHERE id_venta=? ORDER BY id_producto")){p.setInt(1,id);try(ResultSet r=p.executeQuery()){while(r.next()){
            DetalleVenta d=new DetalleVenta(r.getInt("id_producto"),r.getBigDecimal("cantidad"));d.codigo=r.getString("codigo");d.nombre=r.getString("nombre");d.tipo=r.getString("tipo");d.precio=r.getBigDecimal("precio_unitario");d.subtotal=r.getBigDecimal("subtotal");v.detalles.add(d);
        }}}return v;
    }
    @Override public Venta buscar(int id)throws SQLException {
        // Una lectura coherente evita mezclar cabecera anterior con líneas nuevas.
        try(Connection c=abrir()){c.setAutoCommit(false);try{Venta v=buscar(c,id,false);c.commit();return v;}catch(SQLException|RuntimeException e){c.rollback();throw e;}}
    }
    @Override public List<Venta> listar(String busqueda)throws SQLException {
        List<Venta> result=new ArrayList<>();
        try(Connection c=abrir();PreparedStatement p=c.prepareStatement("SELECT * FROM ventas WHERE cliente_nombre LIKE ? OR cliente_documento LIKE ? OR CAST(id AS CHAR)=? ORDER BY id DESC LIMIT 500")) {
            p.setString(1,"%"+busqueda+"%");p.setString(2,"%"+busqueda+"%");p.setString(3,busqueda);try(ResultSet r=p.executeQuery()){while(r.next())result.add(cabecera(r));}
        }return result;
    }
    private Venta vigente(Connection c,int id,int version)throws SQLException {
        Venta v=buscar(c,id,true);if(v==null)throw new IllegalArgumentException("La venta ya no existe.");
        if(v.version!=version)throw new IllegalArgumentException("La venta cambió en otra pestaña. Vuelve a abrirla antes de continuar.");return v;
    }
    private void cliente(Connection c,Venta v,boolean copiar)throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT c.activo,c.tipo_documento,c.ruc,c.razon_social,d.nombres,d.apellidos,d.dni FROM clientes c JOIN datos_personales d ON d.id_datos_personales=c.id_datos_personales WHERE c.id=? FOR SHARE")){
            p.setInt(1,v.clienteId);try(ResultSet r=p.executeQuery()){
                if(!r.next()||!r.getBoolean(1))throw new IllegalArgumentException("Selecciona un cliente activo.");
                if("FACTURA".equals(v.tipo)&&!"RUC".equals(r.getString(2)))throw new IllegalArgumentException("La factura requiere un cliente con RUC.");
                if(copiar){boolean empresa="RUC".equals(r.getString(2));v.clienteNombre=empresa?r.getString(4):r.getString(5)+" "+r.getString(6);v.clienteDocumento=empresa?r.getString(3):r.getString(7);}
            }
        }
    }
    @Override public int guardar(Venta v,int usuarioId,String actor)throws SQLException {
        if(v.id!=0)throw new IllegalArgumentException("Las ventas registradas no se editan. Anula la venta y registra una nueva.");
        if(v.clienteId<=0||!Arrays.asList("BOLETA","FACTURA").contains(v.tipo))throw new IllegalArgumentException("Selecciona cliente y comprobante válidos.");
        if(v.clave==null||!v.clave.matches("[0-9a-fA-F-]{36}"))throw new IllegalArgumentException("Formulario inválido. Abre una venta nueva.");
        CalculoVenta calculo=CalculoVenta.para(v.porcentaje);v.observacion=Validacion.texto(v.observacion,"Observación",500,false);
        if(v.detalles.isEmpty()||v.detalles.size()>50)throw new IllegalArgumentException("Incluye entre 1 y 50 artículos.");
        Set<Integer> ids=new HashSet<>();for(DetalleVenta d:v.detalles){
            if(d.productoId<=0||!ids.add(d.productoId))throw new IllegalArgumentException("Selecciona artículos distintos; agrupa sus cantidades.");
            if(d.cantidad==null||d.cantidad.signum()<=0||d.cantidad.scale()>3||d.cantidad.compareTo(new BigDecimal("999999999.99"))>0)throw new IllegalArgumentException("Cantidad: mayor que cero y hasta tres decimales.");
        }
        v.detalles.sort(Comparator.comparingInt(d->d.productoId));
        try(Connection c=abrir()) {c.setAutoCommit(false);try {
            try(PreparedStatement p=c.prepareStatement("SELECT id,id_usuario FROM ventas WHERE clave_creacion=?")){p.setString(1,v.clave);try(ResultSet r=p.executeQuery()){if(r.next()){if(r.getInt(2)!=usuarioId)throw new IllegalArgumentException("Formulario de otra sesión.");return r.getInt(1);}}}
            cliente(c,v,true);v.subtotal=BigDecimal.ZERO.setScale(2);
            // Orden de bloqueo por id: dos ventas nunca bloquean productos en orden inverso.
            for(DetalleVenta d:v.detalles)try(PreparedStatement p=c.prepareStatement("SELECT codigo,nombre,tipo,precio,activo FROM productos WHERE id=? FOR UPDATE")) {
                p.setInt(1,d.productoId);try(ResultSet r=p.executeQuery()){
                    if(!r.next()||!r.getBoolean(5))throw new IllegalArgumentException("Todos los artículos deben existir y estar activos.");
                    d.codigo=r.getString(1);d.nombre=r.getString(2);d.tipo=r.getString(3);d.precio=r.getBigDecimal(4);d.subtotal=d.precio.multiply(d.cantidad).setScale(2,RoundingMode.HALF_UP);v.subtotal=v.subtotal.add(d.subtotal);
                }
            }
            if(v.subtotal.compareTo(MAX_TOTAL)>0)throw new IllegalArgumentException("El importe supera el máximo permitido.");
            v.descuento=calculo.descuento(v.subtotal);v.total=v.subtotal.subtract(v.descuento);
            String cols="id_cliente=?,cliente_nombre=?,cliente_documento=?,tipo_comprobante=?,subtotal=?,descuento_porcentaje=?,descuento=?,total=?,observacion=?,usuario_actualizacion=?";
            try(PreparedStatement p=c.prepareStatement("INSERT INTO ventas SET "+cols+",clave_creacion=?,id_usuario=?,usuario_registro=?",Statement.RETURN_GENERATED_KEYS)) {
                p.setInt(1,v.clienteId);p.setString(2,v.clienteNombre);p.setString(3,v.clienteDocumento);p.setString(4,v.tipo);p.setBigDecimal(5,v.subtotal);p.setBigDecimal(6,v.porcentaje);p.setBigDecimal(7,v.descuento);p.setBigDecimal(8,v.total);p.setString(9,v.observacion);p.setString(10,actor);
                p.setString(11,v.clave);p.setInt(12,usuarioId);p.setString(13,actor);
                p.executeUpdate();try(ResultSet r=p.getGeneratedKeys()){r.next();v.id=r.getInt(1);}
            }
            try(PreparedStatement p=c.prepareStatement("INSERT INTO detalle_venta(id_venta,id_producto,codigo,nombre,tipo,cantidad,precio_unitario,subtotal) VALUES (?,?,?,?,?,?,?,?)")) {
                for(DetalleVenta d:v.detalles){p.setInt(1,v.id);p.setInt(2,d.productoId);p.setString(3,d.codigo);p.setString(4,d.nombre);p.setString(5,d.tipo);p.setBigDecimal(6,d.cantidad);p.setBigDecimal(7,d.precio);p.setBigDecimal(8,d.subtotal);p.addBatch();}p.executeBatch();
            }
            c.commit();return v.id;
        }catch(SQLException|RuntimeException e){c.rollback();throw e;}}
    }
    @Override public void pagar(int id,int version,String medio,String actor)throws SQLException {
        if(!Arrays.asList("EFECTIVO","TARJETA","TRANSFERENCIA").contains(medio))throw new IllegalArgumentException("Selecciona el medio de pago.");
        transicion(id,version,medio,actor,false);
    }
    @Override public void anular(int id,int version,String actor)throws SQLException{transicion(id,version,null,actor,true);}
    private void transicion(int id,int version,String medio,String actor,boolean anular)throws SQLException {
        try(Connection c=abrir()){c.setAutoCommit(false);try {
            Venta v=vigente(c,id,version);EstadoVenta estado=EstadoVenta.de(v.estado);String nuevo=anular?estado.anular():estado.pagar();
            if(!anular)cliente(c,v,false);
            // Una pendiente nunca descontó existencias: su anulación no toca productos.
            if(!anular || "PAGADA".equals(v.estado))
            for(DetalleVenta d:v.detalles)try(PreparedStatement p=c.prepareStatement("SELECT tipo,activo,stock FROM productos WHERE id=? FOR UPDATE")) {
                p.setInt(1,d.productoId);try(ResultSet r=p.executeQuery()) {
                    if(!r.next())throw new IllegalArgumentException("El artículo ya no existe.");
                    if(!r.getString(1).equals(d.tipo))throw new IllegalArgumentException("Cambió el tipo del artículo. Anula la venta y registra una nueva.");
                    if(!anular&&!r.getBoolean(2))throw new IllegalArgumentException("La venta contiene artículos inactivos.");
                    if(!"SERVICIO".equals(d.tipo)) {
                        BigDecimal stock=anular?r.getBigDecimal(3).add(d.cantidad):r.getBigDecimal(3).subtract(d.cantidad);
                        if(stock.signum()<0)throw new IllegalArgumentException("Stock insuficiente para "+d.codigo+". No se registró el pago.");
                        if(stock.compareTo(new BigDecimal("999999999.99"))>0)throw new IllegalArgumentException("La devolución supera la capacidad del stock.");
                        try(PreparedStatement u=c.prepareStatement("UPDATE productos SET stock=? WHERE id=?")){u.setBigDecimal(1,stock);u.setInt(2,d.productoId);u.executeUpdate();}
                    }
                }
            }
            String sql=anular?"UPDATE ventas SET estado=?,fecha_anulacion=CURRENT_TIMESTAMP,usuario_actualizacion=?,version=version+1 WHERE id=?":"UPDATE ventas SET estado=?,fecha_pago=CURRENT_TIMESTAMP,usuario_actualizacion=?,version=version+1,medio_pago=? WHERE id=?";
            try(PreparedStatement p=c.prepareStatement(sql)){p.setString(1,nuevo);p.setString(2,actor);if(anular)p.setInt(3,id);else {p.setString(3,medio);p.setInt(4,id);}p.executeUpdate();}
            c.commit();
        }catch(SQLException|RuntimeException e){c.rollback();throw e;}}
    }
    /** Defensa para consumidores antiguos: nunca elimina comprobantes registrados. */
    @Override public void eliminar(int id,int version)throws SQLException {
        throw new IllegalArgumentException("Las ventas no se eliminan. Utiliza la anulación.");
    }
}
