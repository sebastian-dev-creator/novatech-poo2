package com.mycompany.novatech.app;

import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.util.*;

/** Permisos conocidos por la aplicación; los nombres arbitrarios no crean funcionalidades. */
public final class PermisoRepository {
    public Set<String> delUsuario(int usuario) throws SQLException {
        String sql="SELECT DISTINCT p.nombre FROM usuarios u JOIN permisos p ON p.id_permiso=u.id_permiso OR EXISTS (SELECT 1 FROM roles_permisos rp WHERE rp.id_rol=u.id_rol AND rp.id_permiso=p.id_permiso AND rp.activo=1) WHERE u.id_usuario=? AND p.activo=1";
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,usuario);try(ResultSet r=p.executeQuery()) {
                Set<String> permisos=new HashSet<>();while(r.next())permisos.add(r.getString(1));return permisos;
            }
        }
    }
    public List<String[]> delRol(int rol) throws SQLException {
        String sql="SELECT p.id_permiso,p.nombre,p.descripcion,EXISTS(SELECT 1 FROM roles_permisos rp WHERE rp.id_rol=? AND rp.id_permiso=p.id_permiso AND rp.activo=1) FROM permisos p WHERE p.activo=1 ORDER BY p.id_permiso";
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,rol);try(ResultSet r=p.executeQuery()) {
                List<String[]> lista=new ArrayList<>();while(r.next())lista.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4)});return lista;
            }
        }
    }
    public void guardar(int rol,Set<Integer> elegidos,String actor) throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                try(PreparedStatement p=c.prepareStatement("SELECT nombre FROM roles WHERE id_rol=? FOR UPDATE")) {
                    p.setInt(1,rol);try(ResultSet r=p.executeQuery()) {
                        if(!r.next())throw new IllegalArgumentException("El rol no existe.");
                        if("ADMINISTRADOR".equals(r.getString(1)))throw new IllegalArgumentException("El administrador conserva todos sus permisos para gestionar el sistema.");
                    }
                }
                Set<Integer> disponibles=new HashSet<>();int menu=0;
                try(PreparedStatement p=c.prepareStatement("SELECT id_permiso,nombre FROM permisos WHERE activo=1");ResultSet r=p.executeQuery()) {
                    while(r.next()){disponibles.add(r.getInt(1));if("VER_MENU".equals(r.getString(2)))menu=r.getInt(1);}
                }
                if(!disponibles.containsAll(elegidos))throw new IllegalArgumentException("Permiso inválido.");
                if(menu>0)elegidos.add(menu);
                try(PreparedStatement p=c.prepareStatement("DELETE FROM roles_permisos WHERE id_rol=?")){p.setInt(1,rol);p.executeUpdate();}
                try(PreparedStatement p=c.prepareStatement("INSERT INTO roles_permisos(id_rol,id_permiso,usuario_registro) VALUES(?,?,?)")) {
                    for(int id:elegidos){p.setInt(1,rol);p.setInt(2,id);p.setString(3,actor);p.executeUpdate();}
                }
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
}
