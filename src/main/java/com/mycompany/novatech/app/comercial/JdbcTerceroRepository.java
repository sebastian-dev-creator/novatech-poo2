package com.mycompany.novatech.app.comercial;

import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.util.*;

/** Implementación común: cada alta/edición/baja es una transacción completa. */
abstract class JdbcTerceroRepository<T extends Tercero> implements Repository<T> {
    private final boolean proveedor;
    private final String tabla;
    JdbcTerceroRepository(boolean proveedor) {
        this.proveedor=proveedor; this.tabla=proveedor?"proveedores":"clientes";
    }
    protected abstract T nuevo();
    private String select() {
        return "SELECT t.*,d.nombres,d.apellidos,d.dni FROM "+tabla+" t JOIN datos_personales d ON d.id_datos_personales=t.id_datos_personales";
    }
    private T leer(ResultSet r) throws SQLException {
        T x=nuevo();x.id=r.getInt("id");x.personaId=r.getInt("id_datos_personales");
        x.nombres=r.getString("nombres");x.apellidos=r.getString("apellidos");
        x.tipoDocumento=proveedor?"RUC":r.getString("tipo_documento");
        x.documento=r.getString("RUC".equals(x.tipoDocumento)?"ruc":"dni");x.razonSocial=r.getString("razon_social");
        x.correo=r.getString("correo");x.telefono=r.getString("telefono");x.direccion=r.getString("direccion");
        x.distritoId=r.getInt("id_distrito");x.activo=r.getBoolean("activo");return x;
    }
    @Override public List<T> listar(String busqueda) throws SQLException {
        String q=Validacion.texto(busqueda,"Búsqueda",100,false);
        String sql=select()+" WHERE LOCATE(?,CONCAT_WS(' ',d.nombres,d.apellidos,t.ruc,t.razon_social,d.dni))>0 ORDER BY t.id DESC LIMIT 500";
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,q);try(ResultSet r=p.executeQuery()){List<T> lista=new ArrayList<>();while(r.next())lista.add(leer(r));return lista;}
        }
    }
    @Override public T buscar(int id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement(select()+" WHERE t.id=?")) {
            p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?leer(r):null;}
        }
    }
    @Override public int guardar(T x,String actor) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            c.setAutoCommit(false);
            try {int id=guardar(c,x,actor);c.commit();return id;}
            catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    protected int guardar(Connection c,T x,String actor) throws SQLException {
        x.validar(proveedor);actor=Validacion.texto(actor,"Usuario de registro",50,true);
        boolean empresa=proveedor||"RUC".equals(x.tipoDocumento);
        int persona=0, distritoAnterior=0;
        if(x.id>0) {
            try(PreparedStatement p=c.prepareStatement("SELECT id_datos_personales,id_distrito FROM "+tabla+" WHERE id=? FOR UPDATE")) {
                p.setInt(1,x.id);try(ResultSet r=p.executeQuery()) {
                    if(!r.next())throw new IllegalArgumentException("El registro ya no existe.");
                    persona=r.getInt(1);distritoAnterior=r.getInt(2);
                }
            }
        }
        if(x.distritoId>0 && x.distritoId!=distritoAnterior) {
            try(PreparedStatement p=c.prepareStatement("SELECT id_distrito FROM distritos WHERE id_distrito=? AND activo=1 FOR SHARE")) {
                p.setInt(1,x.distritoId);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Selecciona un distrito activo.");}
            }
        }
        // Si el cliente ya es usuario, reutilizar su ficha sin duplicar ni cambiar su identidad.
        if(persona==0 && !empresa) {
            try(PreparedStatement p=c.prepareStatement("SELECT id_datos_personales,nombres,apellidos FROM datos_personales WHERE dni=? FOR UPDATE")) {
                p.setString(1,x.documento);try(ResultSet r=p.executeQuery()) {
                    if(r.next()) {
                        if(!x.nombres.equalsIgnoreCase(r.getString(2)) || !x.apellidos.equalsIgnoreCase(r.getString(3)))
                            throw new IllegalArgumentException("El DNI ya tiene otra identidad registrada. Revisa los nombres y apellidos existentes.");
                        persona=r.getInt(1);
                    }
                }
            }
        }
        if(persona==0) {
            try(PreparedStatement p=c.prepareStatement("INSERT INTO datos_personales(id_usuario,nombres,apellidos,dni,usuario_registro) VALUES(NULL,?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                p.setString(1,x.nombres);p.setString(2,x.apellidos);p.setString(3,empresa?null:x.documento);p.setString(4,actor);p.executeUpdate();
                try(ResultSet r=p.getGeneratedKeys()){r.next();persona=r.getInt(1);}
            }
        } else if(x.id>0) {
            boolean compartida;
            try(PreparedStatement p=c.prepareStatement("SELECT d.id_usuario,d.nombres,d.apellidos,d.dni,EXISTS(SELECT 1 FROM clientes t WHERE t.id_datos_personales=d.id_datos_personales"+(!proveedor?" AND t.id<>?":"")+"),EXISTS(SELECT 1 FROM proveedores t WHERE t.id_datos_personales=d.id_datos_personales"+(proveedor?" AND t.id<>?":"")+") FROM datos_personales d WHERE d.id_datos_personales=? FOR UPDATE")) {
                p.setInt(1,x.id);p.setInt(2,persona);
                try(ResultSet r=p.executeQuery()) {
                    if(!r.next())throw new IllegalArgumentException("La ficha personal ya no existe.");
                    compartida=r.getObject(1)!=null || r.getBoolean(5) || r.getBoolean(6);
                    if(compartida && (!x.nombres.equals(r.getString(2)) || !x.apellidos.equals(r.getString(3)) || (!proveedor&&!x.documento.equals(r.getString(4)))))
                        throw new IllegalArgumentException("Esta identidad está compartida. Actualízala desde su ficha de usuario; aquí puedes editar los datos comerciales.");
                }
            }
            if(!compartida)try(PreparedStatement p=c.prepareStatement("UPDATE datos_personales SET nombres=?,apellidos=?,dni=? WHERE id_datos_personales=?")) {
                p.setString(1,x.nombres);p.setString(2,x.apellidos);p.setString(3,empresa?null:x.documento);p.setInt(4,persona);p.executeUpdate();
            }
        }
        String columnas="correo=?,telefono=?,direccion=?,id_distrito=?,activo=?,ruc=?,razon_social=?"+(proveedor?"":",tipo_documento=?");
        String sql=x.id==0?"INSERT INTO "+tabla+" SET "+columnas+",id_datos_personales=?,usuario_registro=?":"UPDATE "+tabla+" SET "+columnas+" WHERE id=?";
        try(PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            int i=1;p.setString(i++,x.correo);p.setString(i++,x.telefono);p.setString(i++,x.direccion);
            if(x.distritoId==0)p.setNull(i++,Types.INTEGER);else p.setInt(i++,x.distritoId);
            p.setBoolean(i++,x.activo);
            p.setString(i++,empresa?x.documento:null);p.setString(i++,x.razonSocial);if(!proveedor)p.setString(i++,x.tipoDocumento);
            if(x.id==0){p.setInt(i++,persona);p.setString(i,actor);}else p.setInt(i,x.id);
            p.executeUpdate();if(x.id>0)return x.id;
            try(ResultSet r=p.getGeneratedKeys()){r.next();return r.getInt(1);}
        }
    }
    @Override public void eliminar(int id) throws SQLException {
        if(id<=0)throw new IllegalArgumentException("Selecciona un registro existente.");
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                int persona;
                try(PreparedStatement p=c.prepareStatement("SELECT id_datos_personales FROM "+tabla+" WHERE id=? FOR UPDATE")) {
                    p.setInt(1,id);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("El registro ya no existe.");persona=r.getInt(1);}
                }
                try(PreparedStatement p=c.prepareStatement("DELETE FROM "+tabla+" WHERE id=?")){p.setInt(1,id);p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("DELETE FROM datos_personales WHERE id_datos_personales=? AND id_usuario IS NULL AND NOT EXISTS(SELECT 1 FROM clientes WHERE id_datos_personales=?) AND NOT EXISTS(SELECT 1 FROM proveedores WHERE id_datos_personales=?)")) {
                    p.setInt(1,persona);p.setInt(2,persona);p.setInt(3,persona);p.executeUpdate();
                }
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
}
