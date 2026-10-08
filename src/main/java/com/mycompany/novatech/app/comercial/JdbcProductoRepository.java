package com.mycompany.novatech.app.comercial;
import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.util.*;

public final class JdbcProductoRepository implements ProductoRepository {
    private Producto leer(ResultSet r)throws SQLException {
        return new Producto.Builder().identidad(r.getInt("id"),r.getString("codigo"),r.getString("nombre"))
            .clasificacion(r.getString("tipo"),r.getInt("id_categoria"),r.getInt("id_marca"),r.getInt("id_unidad"),r.getInt("id_proveedor"))
            .descripcion(r.getString("descripcion")).valores(r.getString("precio"),r.getString("stock"),r.getString("stock_minimo"))
            .activo(r.getBoolean("activo")).build();
    }
    @Override public List<Producto> listar(String busqueda)throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement("SELECT * FROM productos WHERE LOCATE(?,CONCAT_WS(' ',codigo,nombre,tipo))>0 ORDER BY id DESC LIMIT 500")) {
            p.setString(1,Validacion.texto(busqueda,"Búsqueda",100,false));
            try(ResultSet r=p.executeQuery()){List<Producto> lista=new ArrayList<>();while(r.next())lista.add(leer(r));return lista;}
        }
    }
    @Override public Producto buscar(int id)throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement("SELECT * FROM productos WHERE id=?")) {
            p.setInt(1,id);try(ResultSet r=p.executeQuery()){return r.next()?leer(r):null;}
        }
    }
    @Override public int guardar(Producto x,String actor)throws SQLException {
        actor=Validacion.texto(actor,"Usuario de registro",50,true);
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                int[] anteriores=new int[4];
                if(x.id>0)try(PreparedStatement p=c.prepareStatement("SELECT id_categoria,id_marca,id_unidad,id_proveedor FROM productos WHERE id=? FOR UPDATE")) {
                    p.setInt(1,x.id);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("El artículo ya no existe.");for(int i=0;i<4;i++)anteriores[i]=r.getInt(i+1);}
                }
                String[] tablas={"categorias","marcas","unidades_medida","proveedores"};
                int[] ids={x.categoriaId,x.marcaId,x.unidadId,x.proveedorId};
                for(int i=0;i<4;i++)if(ids[i]>0 && ids[i]!=anteriores[i]) {
                    try(PreparedStatement p=c.prepareStatement("SELECT id FROM "+tablas[i]+" WHERE id=? AND activo=1 FOR SHARE")) {
                        p.setInt(1,ids[i]);try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalArgumentException("Selecciona catálogos y proveedor activos.");}
                    }
                }
                String cols="codigo=?,nombre=?,descripcion=?,tipo=?,precio=?,stock=?,stock_minimo=?,activo=?,id_categoria=?,id_marca=?,id_unidad=?,id_proveedor=?";
                try(PreparedStatement p=c.prepareStatement(x.id==0?"INSERT INTO productos SET "+cols+",usuario_registro=?":"UPDATE productos SET "+cols+" WHERE id=?",Statement.RETURN_GENERATED_KEYS)) {
                    p.setString(1,x.codigo);p.setString(2,x.nombre);p.setString(3,x.descripcion);p.setString(4,x.tipo);p.setBigDecimal(5,x.precio);
                    p.setBigDecimal(6,x.stock);p.setBigDecimal(7,x.stockMinimo);p.setBoolean(8,x.activo);p.setInt(9,x.categoriaId);
                    if(x.marcaId==0)p.setNull(10,Types.INTEGER);else p.setInt(10,x.marcaId);p.setInt(11,x.unidadId);
                    if(x.proveedorId==0)p.setNull(12,Types.INTEGER);else p.setInt(12,x.proveedorId);
                    if(x.id==0)p.setString(13,actor);else p.setInt(13,x.id);
                    p.executeUpdate();int id=x.id;
                    if(id==0)try(ResultSet r=p.getGeneratedKeys()){r.next();id=r.getInt(1);}
                    c.commit();return id;
                }
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
    @Override public void eliminar(int id)throws SQLException {
        try(Connection c=ConexionBD.getInstancia().obtenerConexion();PreparedStatement p=c.prepareStatement("DELETE FROM productos WHERE id=? AND stock=0")) {
            p.setInt(1,id);if(p.executeUpdate()==0)throw new IllegalArgumentException("El artículo no existe o tiene stock. Si tiene existencias, márcalo como inactivo.");
        }
    }
}
