package com.mycompany.novatech.app.comercial;
import java.sql.SQLException;
import java.util.List;
public interface ProveedorRepository extends Repository<Proveedor> {
    void importar(List<Proveedor> proveedores, String actor) throws SQLException;
}
