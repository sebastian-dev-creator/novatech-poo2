package com.mycompany.novatech.app.comercial;
import com.mycompany.novatech.config.ConexionBD;
import java.sql.*;
import java.util.*;
public final class JdbcProveedorRepository extends JdbcTerceroRepository<Proveedor> implements ProveedorRepository {
    public JdbcProveedorRepository(){super(true);}
    @Override protected Proveedor nuevo(){return new Proveedor();}
    @Override public void importar(List<Proveedor> proveedores,String actor) throws SQLException {
        if(proveedores==null || proveedores.isEmpty() || proveedores.size()>50)
            throw new IllegalArgumentException("Importa entre 1 y 50 proveedores.");
        try(Connection c=ConexionBD.getInstancia().obtenerConexion()) {
            c.setAutoCommit(false);
            try {
                for(Proveedor p:proveedores){if(p.id!=0)throw new IllegalArgumentException("La importación solo crea proveedores nuevos.");guardar(c,p,actor);}
                c.commit();
            }catch(SQLException|RuntimeException e){c.rollback();throw e;}
        }
    }
}
