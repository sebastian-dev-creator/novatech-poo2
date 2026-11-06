package com.mycompany.novatech.app.reportes;

import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.math.*;
import java.time.LocalDate;
import java.util.*;

/** Cabecera, detalle, recepción y costo promedio se confirman en una transacción. */
public final class CompraRepository {
    public static final class Linea {
        public int producto;
        public String nombre,tipo;
        public BigDecimal cantidad,costo,subtotal;
    }
    public static final class Compra {
        public int id,proveedor,version,evaluacion;
        public String clave,nombre,estado,fecha,recepcion,pago,observacion;
        public LocalDate prevista;
        public BigDecimal total;
        public List<Linea> lineas=new ArrayList<>();
    }
    private Compra leer(ResultSet r)throws SQLException {
        Compra x=new Compra();x.id=r.getInt("id");x.proveedor=r.getInt("id_proveedor");
        x.version=r.getInt("version");x.evaluacion=r.getInt("evaluacion");x.clave=r.getString("clave");
        x.nombre=r.getString("proveedor_nombre");x.estado=r.getString("estado");x.fecha=r.getString("fecha");
        x.recepcion=r.getString("fecha_recepcion");x.pago=r.getString("fecha_pago");
        x.observacion=r.getString("observacion");x.prevista=r.getDate("fecha_prevista").toLocalDate();x.total=r.getBigDecimal("total");
        return x;
    }
    public List<Compra> listar()throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT * FROM compras ORDER BY id DESC LIMIT 500")) {
            List<Compra> compras=new ArrayList<>();while(r.next())compras.add(leer(r));return compras;
        }
    }
    private Compra buscar(Connection c,int id,boolean bloqueo)throws SQLException {
        Compra x;
        try(PreparedStatement p=c.prepareStatement("SELECT * FROM compras WHERE id=?"+(bloqueo?" FOR UPDATE":""))) {
            p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Compra inexistente.");x=leer(r);}
        }
        try(PreparedStatement p=c.prepareStatement("SELECT * FROM detalle_compra WHERE id_compra=? ORDER BY id_producto")) {
            p.setInt(1,id);try(ResultSet r=p.executeQuery()){while(r.next()){
                Linea l=new Linea();l.producto=r.getInt("id_producto");l.nombre=r.getString("nombre");l.tipo=r.getString("tipo");
                l.cantidad=r.getBigDecimal("cantidad");l.costo=r.getBigDecimal("costo");l.subtotal=r.getBigDecimal("subtotal");x.lineas.add(l);
            }}
        }
        return x;
    }
    public Compra buscar(int id)throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()){return buscar(c,id,false);}
    }
    private Integer porClave(Connection c,String clave)throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT id FROM compras WHERE clave=?")) {
            p.setString(1,clave);try(ResultSet r=p.executeQuery()){return r.next()?r.getInt(1):null;}
        }
    }
    public int registrar(Compra x,String actor)throws SQLException {
        if(x.proveedor<=0 || x.prevista==null || x.prevista.getYear()<1000 || x.prevista.getYear()>9998)
            throw new IllegalArgumentException("Selecciona proveedor y fecha prevista válidos.");
        try{UUID.fromString(x.clave);}catch(RuntimeException e){throw new IllegalArgumentException("Abre una nueva compra.");}
        if(x.observacion==null || x.observacion.length()>500)throw new IllegalArgumentException("La observación admite hasta 500 caracteres.");
        if(x.lineas.isEmpty() || x.lineas.size()>20)throw new IllegalArgumentException("Incluye entre 1 y 20 artículos.");
        Set<Integer> ids=new HashSet<>();x.total=BigDecimal.ZERO;
        for(Linea l:x.lineas) {
            if(l.producto<=0 || !ids.add(l.producto))throw new IllegalArgumentException("Selecciona artículos distintos.");
            if(l.cantidad==null || l.costo==null || l.cantidad.signum()<=0 || l.costo.signum()<0 || l.cantidad.scale()>3 || l.costo.scale()>2
                    || l.cantidad.compareTo(new BigDecimal("999999999.999"))>0 || l.costo.compareTo(new BigDecimal("999999999.99"))>0)
                throw new IllegalArgumentException("Revisa cantidades y costos. Cantidad mayor que cero, costo desde cero.");
            l.subtotal=l.cantidad.multiply(l.costo).setScale(2,RoundingMode.HALF_UP);x.total=x.total.add(l.subtotal);
        }
        if(x.total.compareTo(new BigDecimal("999999999999.99"))>0)throw new IllegalArgumentException("Importe demasiado grande.");
        x.lineas.sort(Comparator.comparingInt(l->l.producto));
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                Integer existente=porClave(c,x.clave);if(existente!=null){c.commit();return existente;}
                try(PreparedStatement p=c.prepareStatement("SELECT razon_social FROM proveedores WHERE id=? AND activo=1 FOR SHARE")) {
                    p.setInt(1,x.proveedor);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Selecciona un proveedor activo.");x.nombre=r.getString(1);}
                }
                for(Linea l:x.lineas)try(PreparedStatement p=c.prepareStatement("SELECT nombre,tipo FROM productos WHERE id=? AND activo=1 FOR SHARE")) {
                    p.setInt(1,l.producto);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Selecciona artículos activos.");l.nombre=r.getString(1);l.tipo=r.getString(2);}
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO compras(clave,id_proveedor,proveedor_nombre,fecha_prevista,total,observacion,usuario) VALUES (?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1,x.clave);p.setInt(2,x.proveedor);p.setString(3,x.nombre);p.setDate(4,java.sql.Date.valueOf(x.prevista));
                    p.setBigDecimal(5,x.total);p.setString(6,x.observacion);p.setString(7,actor);p.executeUpdate();
                    try(ResultSet r=p.getGeneratedKeys()){r.next();x.id=r.getInt(1);}
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO detalle_compra(id_compra,id_producto,nombre,tipo,cantidad,costo,subtotal) VALUES (?,?,?,?,?,?,?)")) {
                    for(Linea l:x.lineas){p.setInt(1,x.id);p.setInt(2,l.producto);p.setString(3,l.nombre);p.setString(4,l.tipo);p.setBigDecimal(5,l.cantidad);p.setBigDecimal(6,l.costo);p.setBigDecimal(7,l.subtotal);p.addBatch();}p.executeBatch();
                }
                c.commit();return x.id;
            }catch(SQLException|RuntimeException e){
                c.rollback();
                if(e instanceof SQLIntegrityConstraintViolationException){Integer existente=porClave(c,x.clave);if(existente!=null)return existente;}
                throw e;
            }
        }
    }
    public void cambiar(int id,int version,String accion,int evaluacion)throws SQLException {
        if(!Arrays.asList("recibir","pagar","anular").contains(accion))throw new IllegalArgumentException("Acción inválida.");
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                Compra x=buscar(c,id,true);
                if(x.version!=version)throw new IllegalArgumentException("La compra cambió. Recarga antes de continuar.");
                String sql;
                if("recibir".equals(accion)) {
                    if(!"PENDIENTE".equals(x.estado) || evaluacion<1 || evaluacion>5)throw new IllegalArgumentException("Solo puedes recibir una compra pendiente y calificarla de 1 a 5.");
                    recibir(c,x);
                    sql="UPDATE compras SET estado='RECIBIDA',fecha_recepcion=CURRENT_TIMESTAMP,evaluacion=?,version=version+1 WHERE id=?";
                } else if("pagar".equals(accion)) {
                    if(!"RECIBIDA".equals(x.estado)||x.pago!=null)throw new IllegalArgumentException("Solo puedes pagar una compra recibida y pendiente de pago.");
                    sql="UPDATE compras SET fecha_pago=CURRENT_TIMESTAMP,version=version+1 WHERE id=?";
                } else {
                    if(!"PENDIENTE".equals(x.estado)||x.pago!=null)throw new IllegalArgumentException("Solo puedes anular compras pendientes de recepción y pago.");
                    sql="UPDATE compras SET estado='ANULADA',fecha_anulacion=CURRENT_TIMESTAMP,version=version+1 WHERE id=?";
                }
                try(PreparedStatement p=c.prepareStatement(sql)) {
                    int indice=1;if("recibir".equals(accion))p.setInt(indice++,evaluacion);p.setInt(indice,id);p.executeUpdate();
                }
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    private void recibir(Connection c,Compra x)throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SET @nt_motivo=?")){p.setString(1,"Recepción compra "+x.id);p.execute();}
        for(Linea l:x.lineas)try(PreparedStatement p=c.prepareStatement("SELECT stock,costo,tipo,activo FROM productos WHERE id=? FOR UPDATE")) {
            p.setInt(1,l.producto);
            try(ResultSet r=p.executeQuery()) {
                if(!r.next()||!r.getBoolean(4)||!l.tipo.equals(r.getString(3)))throw new IllegalArgumentException("Revisa el estado y tipo de los artículos antes de recibir.");
                if("SERVICIO".equals(l.tipo))continue;
                BigDecimal anterior=r.getBigDecimal(1),costo=r.getBigDecimal(2),nuevo=anterior.add(l.cantidad);
                if(nuevo.compareTo(new BigDecimal("999999999.999"))>0)throw new IllegalArgumentException("El stock supera el máximo admitido.");
                BigDecimal promedio=anterior.signum()==0?l.costo:costo==null?null:
                        anterior.multiply(costo).add(l.cantidad.multiply(l.costo)).divide(nuevo,2,RoundingMode.HALF_UP);
                try(PreparedStatement u=c.prepareStatement("UPDATE productos SET stock=?,costo=? WHERE id=?")) {
                    u.setBigDecimal(1,nuevo);u.setBigDecimal(2,promedio);u.setInt(3,l.producto);u.executeUpdate();
                }
            }
        }
    }
}
