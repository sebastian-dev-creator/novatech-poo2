package com.mycompany.novatech.app.comercial;
import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.util.*;

/** Los nombres de tabla proceden exclusivamente de esta lista cerrada. */
public final class CatalogoComercialRepository {
    public String tabla(String clase) {
        switch(clase){case "categoria":return "categorias";case "marca":return "marcas";case "unidad":return "unidades_medida";default:throw new IllegalArgumentException("Catálogo inválido.");}
    }
    public List<String[]> listar(String clase)throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement("SELECT id,nombre,activo FROM "+tabla(clase)+" ORDER BY nombre");ResultSet r=p.executeQuery()) {
            List<String[]> lista=new ArrayList<>();while(r.next())lista.add(new String[]{r.getString(1),r.getString(2),r.getString(3)});return lista;
        }
    }
    public void guardar(String clase,int id,String nombre,boolean activo,String actor)throws SQLException {
        nombre=Validacion.texto(nombre,"Nombre",100,true);actor=Validacion.texto(actor,"Usuario de registro",50,true);
        if(id<0)throw new IllegalArgumentException("Identificador inválido.");
        String sql=id==0?"INSERT INTO "+tabla(clase)+"(nombre,activo,usuario_registro) VALUES(?,?,?)":"UPDATE "+tabla(clase)+" SET nombre=?,activo=? WHERE id=?";
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,nombre);p.setBoolean(2,activo);if(id==0)p.setString(3,actor);else p.setInt(3,id);
            if(p.executeUpdate()==0)throw new IllegalArgumentException("El catálogo ya no existe.");
        }
    }
    public void eliminar(String clase,int id)throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement("DELETE FROM "+tabla(clase)+" WHERE id=?")) {
            p.setInt(1,id);if(p.executeUpdate()==0)throw new IllegalArgumentException("El catálogo ya no existe.");
        }
    }
    public List<String[]> distritos()throws SQLException {
        String sql="SELECT d.id_distrito,CONCAT_WS(' / ',p.nombre,dep.nombre,pr.nombre,d.nombre),d.activo FROM distritos d JOIN provincias pr ON pr.id_provincia=d.id_provincia JOIN departamentos dep ON dep.id_departamento=pr.id_departamento JOIN paises p ON p.id_pais=dep.id_pais ORDER BY p.nombre,dep.nombre,pr.nombre,d.nombre";
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()) {
            List<String[]> lista=new ArrayList<>();while(r.next())lista.add(new String[]{r.getString(1),r.getString(2),r.getString(3)});return lista;
        }
    }
}
