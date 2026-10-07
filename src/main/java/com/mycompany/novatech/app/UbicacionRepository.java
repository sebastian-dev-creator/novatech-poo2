package com.mycompany.novatech.app;
import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.util.*;
/** Catálogos territoriales con padres obligatorios y claves foráneas. */
public final class UbicacionRepository {
    private String[] campos(String nivel){
        switch(nivel){
            case "pais":return new String[]{"paises","id_pais",""};
            case "departamento":return new String[]{"departamentos","id_departamento","id_pais"};
            case "provincia":return new String[]{"provincias","id_provincia","id_departamento"};
            case "distrito":return new String[]{"distritos","id_distrito","id_provincia"};
            default:throw new IllegalArgumentException("Nivel de ubicación inválido.");
        }
    }
    public List<String[]> listar(String nivel)throws SQLException{
        String[] f=campos(nivel);String sql="SELECT "+f[1]+",nombre,"+(f[2].isEmpty()?"0":f[2])+",activo FROM "+f[0]+" ORDER BY nombre";
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            List<String[]> lista=new ArrayList<>();while(r.next())lista.add(new String[]{r.getString(1),r.getString(2),r.getString(3),r.getString(4)});return lista;
        }
    }
    public List<String[]> distritosCompletos()throws SQLException{
        String sql="SELECT d.id_distrito,CONCAT(pa.nombre,' / ',de.nombre,' / ',p.nombre,' / ',d.nombre) FROM distritos d JOIN provincias p ON p.id_provincia=d.id_provincia JOIN departamentos de ON de.id_departamento=p.id_departamento JOIN paises pa ON pa.id_pais=de.id_pais WHERE d.activo=1 AND p.activo=1 AND de.activo=1 AND pa.activo=1 ORDER BY pa.nombre,de.nombre,p.nombre,d.nombre";
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()){
            List<String[]> lista=new ArrayList<>();while(r.next())lista.add(new String[]{r.getString(1),r.getString(2)});return lista;
        }
    }
    public List<String[]> superiores(String nivel)throws SQLException{
        List<String[]> lista=listar(nivel);
        if("pais".equals(nivel))return lista;
        String padre="departamento".equals(nivel)?"pais":"departamento";
        Map<String,String> nombres=new HashMap<>();
        for(String[] p:superiores(padre))nombres.put(p[0],p[1]);
        for(String[] r:lista)r[1]=nombres.getOrDefault(r[2],"")+" / "+r[1];
        return lista;
    }
    public void guardar(String nivel,int id,String nombre,int padre,boolean activo,String actor)throws SQLException{
        String[] f=campos(nivel);boolean tienePadre=!f[2].isEmpty();
        if(id<0||nombre.isBlank()||nombre.length()>100||(tienePadre&&padre<=0))throw new IllegalArgumentException("Completa el nombre (máximo 100 caracteres) y la ubicación superior.");
        String sql=id==0?"INSERT INTO "+f[0]+" (nombre,activo"+(tienePadre?","+f[2]:"")+",usuario_registro) VALUES (?,?"+(tienePadre?",?":"")+",?)":"UPDATE "+f[0]+" SET nombre=?,activo=?"+(tienePadre?","+f[2]+"=?":"")+" WHERE "+f[1]+"=?";
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement(sql)){
            int i=1;p.setString(i++,nombre);p.setBoolean(i++,activo);if(tienePadre)p.setInt(i++,padre);if(id==0)p.setString(i,actor);else p.setInt(i,id);
            if(p.executeUpdate()==0)throw new IllegalArgumentException("La ubicación no existe.");
        }
    }
    public void eliminar(String nivel,int id)throws SQLException{
        String[] f=campos(nivel);
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement("DELETE FROM "+f[0]+" WHERE "+f[1]+"=?")){
            p.setInt(1,id);if(p.executeUpdate()==0)throw new IllegalArgumentException("La ubicación no existe.");
        }
    }
}
