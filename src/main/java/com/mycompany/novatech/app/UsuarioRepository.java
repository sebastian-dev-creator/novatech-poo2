package com.mycompany.novatech.app;

import com.mycompany.novatech.config.ConexionBD;
import com.mycompany.novatech.seguridad.PasswordUtil;
import java.sql.*;
import java.util.*;

/** Repository: concentra SQL y transacciones fuera de la interfaz. */
public final class UsuarioRepository {
    private static final String SELECT = "SELECT u.*, r.nombre rol, r.activo rol_activo, d.nombres, d.apellidos, d.dni, d.id_sexo, d.id_estado_civil FROM usuarios u JOIN roles r ON r.id_rol=u.id_rol LEFT JOIN datos_personales d ON d.id_usuario=u.id_usuario ";

    private Usuario map(ResultSet r) throws SQLException {
        Usuario u = new Usuario();
        u.id=r.getInt("id_usuario"); u.rolId=r.getInt("id_rol");
        u.username=r.getString("nombre_usuario"); u.rol=r.getString("rol");
        u.nombres=r.getString("nombres"); u.apellidos=r.getString("apellidos");
        u.activo=r.getBoolean("activo") && r.getBoolean("rol_activo");
        u.bloqueado=r.getBoolean("bloqueado"); u.intentos=r.getInt("intentos_fallidos");
        u.dni=r.getString("dni");
        u.sexoId=r.getObject("id_sexo",Integer.class);
        u.estadoCivilId=r.getObject("id_estado_civil",Integer.class);
        return u;
    }

    public List<Usuario> listar() throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion(); PreparedStatement p=c.prepareStatement(SELECT+"ORDER BY u.id_usuario"); ResultSet r=p.executeQuery()) {
            List<Usuario> lista=new ArrayList<>(); while(r.next()) lista.add(map(r)); return lista;
        }
    }
    public Usuario buscar(int id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion(); PreparedStatement p=c.prepareStatement(SELECT+"WHERE u.id_usuario=?")) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()) { return r.next()?map(r):null; }
        }
    }
    public List<String[]> roles() throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion(); PreparedStatement p=c.prepareStatement("SELECT id_rol,nombre,descripcion,activo FROM roles ORDER BY id_rol"); ResultSet r=p.executeQuery()) {
            List<String[]> a=new ArrayList<>(); while(r.next()) a.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4)}); return a;
        }
    }

    public List<String[]> catalogoPersonal(boolean sexo) throws SQLException {
        String sql=sexo ? "SELECT id_sexo,descripcion FROM sexos WHERE activo=1 ORDER BY descripcion"
                        : "SELECT id_estado_civil,descripcion FROM estados_civiles WHERE activo=1 ORDER BY descripcion";
        try(Connection c=ConexionBD.getInstancia().obtenerConexion(); PreparedStatement p=c.prepareStatement(sql); ResultSet r=p.executeQuery()) {
            List<String[]> resultado=new ArrayList<>();
            while(r.next()) resultado.add(new String[]{r.getString(1),r.getString(2)});
            return resultado;
        }
    }
    private void validarCatalogo(Connection c, Integer id, boolean sexo) throws SQLException {
        if(id==null) return;
        String sql=sexo ? "SELECT id_sexo FROM sexos WHERE id_sexo=? AND activo=1"
                        : "SELECT id_estado_civil FROM estados_civiles WHERE id_estado_civil=? AND activo=1";
        try(PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,id);
            try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new IllegalArgumentException("Selecciona una opción activa del catálogo."); }
        }
    }

    /** Bloqueo persistente al tercer fallo; bloqueo de fila evita carreras. */
    public Usuario autenticar(String nombre, String password) throws SQLException {
        return autenticarConEstado(nombre,password).usuario;
    }
    public ResultadoAcceso autenticarConEstado(String nombre, String password) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            c.setAutoCommit(false);
            try(PreparedStatement p=c.prepareStatement(SELECT+"WHERE u.nombre_usuario=? FOR UPDATE")) {
                p.setString(1,nombre);
                Usuario u; String hash;
                try(ResultSet r=p.executeQuery()) {
                    if(!r.next()) { c.rollback(); return ResultadoAcceso.denegado(); }
                    u=map(r); hash=r.getString("password_hash");
                }
                if(u.bloqueado) { c.rollback(); return ResultadoAcceso.bloqueado(); }
                if(!u.activo) { c.rollback(); return ResultadoAcceso.denegado(); }
                boolean ok=PasswordUtil.verificar(password,hash);
                try(PreparedStatement q=c.prepareStatement(ok
                    ?"UPDATE usuarios SET intentos_fallidos=0, ultimo_acceso=CURRENT_TIMESTAMP WHERE id_usuario=?"
                    :"UPDATE usuarios SET intentos_fallidos=?, bloqueado=? WHERE id_usuario=?")) {
                    if(ok) q.setInt(1,u.id);
                    else { int n=Math.min(3,u.intentos+1); q.setInt(1,n); q.setBoolean(2,n>=3); q.setInt(3,u.id); }
                    q.executeUpdate();
                }
                c.commit();
                if(ok) { u.intentos=0; return ResultadoAcceso.correcto(u); }
                return u.intentos+1>=3 ? ResultadoAcceso.bloqueado() : ResultadoAcceso.denegado();
            } catch(SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }

    private void bloquearAdministradores(Connection c) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT id_rol FROM roles WHERE nombre='ADMINISTRADOR' FOR UPDATE");ResultSet r=p.executeQuery()) { while(r.next()) {} }
    }
    private void comprobarAdministrador(Connection c) throws SQLException {
        try(PreparedStatement p=c.prepareStatement("SELECT COUNT(*) FROM usuarios u JOIN roles r ON r.id_rol=u.id_rol WHERE r.nombre='ADMINISTRADOR' AND r.activo=1 AND u.activo=1 AND u.bloqueado=0"); ResultSet r=p.executeQuery()) {
            r.next(); if(r.getInt(1)==0) throw new IllegalArgumentException("Debe quedar un administrador activo y desbloqueado.");
        }
    }
    public void guardar(int id,String nombre,String nombres,String apellidos,int rol,String password,boolean activo) throws SQLException {
        guardar(id,nombre,nombres,apellidos,rol,password,activo,null,null);
    }
    public void guardar(int id,String nombre,String nombres,String apellidos,int rol,String password,boolean activo,DatosPersonales datos,String actor) throws SQLException {
        if(datos!=null) datos.validar();
        if(!nombre.matches("[A-Za-z0-9._-]{3,50}")) throw new IllegalArgumentException("Usuario: de 3 a 50 letras, números, puntos, guiones o guiones bajos.");
        if(nombres.isBlank() || apellidos.isBlank() || nombres.length()>80 || apellidos.length()>80) throw new IllegalArgumentException("Nombres y apellidos son obligatorios; máximo 80 caracteres.");
        if((id==0 || !password.isEmpty()) && (password.length()<12 || password.length()>128)) throw new IllegalArgumentException("La contraseña debe tener entre 12 y 128 caracteres.");
        String hash=password.isEmpty()?null:PasswordUtil.generarHash(password);
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                bloquearAdministradores(c);
                if(datos!=null) { validarCatalogo(c,datos.sexoId,true); validarCatalogo(c,datos.estadoCivilId,false); }
                boolean nuevo=id==0;
                try(PreparedStatement p=c.prepareStatement("SELECT id_rol FROM roles WHERE id_rol=? AND activo=1")) {
                    p.setInt(1,rol); try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new IllegalArgumentException("Selecciona un rol activo."); }
                }
                if(id==0) {
                    try(PreparedStatement p=c.prepareStatement("INSERT INTO usuarios(nombre_usuario,password_hash,id_rol,activo) VALUES(?,?,?,?)",Statement.RETURN_GENERATED_KEYS)) {
                        p.setString(1,nombre);p.setString(2,hash);p.setInt(3,rol);p.setBoolean(4,activo);p.executeUpdate();
                        try(ResultSet r=p.getGeneratedKeys()) { r.next(); id=r.getInt(1); }
                    }
                } else {
                    try(PreparedStatement p=c.prepareStatement("UPDATE usuarios SET nombre_usuario=?,id_rol=?,activo=?,password_hash=COALESCE(?,password_hash) WHERE id_usuario=?")) {
                        p.setString(1,nombre);p.setInt(2,rol);p.setBoolean(3,activo);p.setString(4,hash);p.setInt(5,id);
                        if(p.executeUpdate()==0) throw new IllegalArgumentException("Usuario inexistente.");
                    }
                }
                try(PreparedStatement p=c.prepareStatement("INSERT INTO datos_personales(id_usuario,nombres,apellidos) VALUES(?,?,?) ON DUPLICATE KEY UPDATE nombres=?,apellidos=?")) {
                    p.setInt(1,id);p.setString(2,nombres);p.setString(3,apellidos);p.setString(4,nombres);p.setString(5,apellidos);p.executeUpdate();
                }
                if(datos!=null) {
                    try(PreparedStatement p=c.prepareStatement("UPDATE datos_personales SET dni=?,id_sexo=?,id_estado_civil=? WHERE id_usuario=?")) {
                        p.setString(1,datos.dni); p.setObject(2,datos.sexoId,Types.INTEGER); p.setObject(3,datos.estadoCivilId,Types.INTEGER); p.setInt(4,id); p.executeUpdate();
                    }
                }
                if(nuevo && actor!=null) {
                    for(String tabla:new String[]{"usuarios","datos_personales"}) {
                        try(PreparedStatement p=c.prepareStatement("UPDATE "+tabla+" SET usuario_registro=? WHERE id_usuario=?")) {
                            p.setString(1,actor);p.setInt(2,id);p.executeUpdate();
                        }
                    }
                }
                comprobarAdministrador(c); c.commit();
            } catch(SQLException | RuntimeException e) { c.rollback(); throw e; }
        }
    }
    public void accion(int id,int actor,String accion) throws SQLException {
        if(id==actor && !"desbloquear".equals(accion)) throw new IllegalArgumentException("No puedes desactivar ni eliminar tu propia cuenta.");
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                bloquearAdministradores(c);
                if("eliminar".equals(accion)) {
                    ejecutar(c,"DELETE FROM datos_personales WHERE id_usuario=?",id);
                    ejecutar(c,"DELETE FROM usuarios WHERE id_usuario=?",id);
                } else if("desbloquear".equals(accion)) ejecutar(c,"UPDATE usuarios SET bloqueado=0,intentos_fallidos=0 WHERE id_usuario=?",id);
                else if("desactivar".equals(accion)) ejecutar(c,"UPDATE usuarios SET activo=0 WHERE id_usuario=?",id);
                else throw new IllegalArgumentException("Acción inválida.");
                comprobarAdministrador(c);c.commit();
            } catch(SQLException | RuntimeException e) { c.rollback();throw e; }
        }
    }
    private void ejecutar(Connection c,String sql,int id) throws SQLException { try(PreparedStatement p=c.prepareStatement(sql)) {p.setInt(1,id);p.executeUpdate();} }

    public void guardarRol(int id,String nombre,String descripcion) throws SQLException {
        nombre=nombre.toUpperCase(Locale.ROOT);
        if(!nombre.matches("[A-Z_]{3,30}") || descripcion.length()>150) throw new IllegalArgumentException("Rol: 3 a 30 letras sin espacios; descripción de hasta 150 caracteres.");
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            if(id!=0) {
                try(PreparedStatement p=c.prepareStatement("SELECT nombre FROM roles WHERE id_rol=?")) {
                    p.setInt(1,id);try(ResultSet r=p.executeQuery()) { if(!r.next()) throw new IllegalArgumentException("Rol inexistente."); if("ADMINISTRADOR".equals(r.getString(1)) && !"ADMINISTRADOR".equals(nombre)) throw new IllegalArgumentException("El nombre ADMINISTRADOR está reservado."); }
                }
            }
            try(PreparedStatement p=c.prepareStatement(id==0?"INSERT INTO roles(nombre,descripcion) VALUES(?,?)":"UPDATE roles SET nombre=?,descripcion=? WHERE id_rol=?")) {
                p.setString(1,nombre);p.setString(2,descripcion);if(id!=0)p.setInt(3,id);p.executeUpdate();
            }
        }
    }
    public void eliminarRol(int id) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement("DELETE FROM roles WHERE id_rol=? AND nombre NOT IN ('ADMINISTRADOR','OPERADOR')")) {
            p.setInt(1,id);if(p.executeUpdate()==0)throw new IllegalArgumentException("No se eliminan los roles base.");
        }
    }
}
