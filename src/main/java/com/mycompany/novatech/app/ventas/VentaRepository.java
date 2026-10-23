package com.mycompany.novatech.app.ventas;
import java.sql.SQLException;
import java.util.List;
public interface VentaRepository {
    List<Venta> listar(String busqueda) throws SQLException;
    Venta buscar(int id) throws SQLException;
    int guardar(Venta venta,int usuarioId,String actor) throws SQLException;
    void pagar(int id,int version,String medio,String actor) throws SQLException;
    void anular(int id,int version,String actor) throws SQLException;
    void eliminar(int id,int version) throws SQLException;
}
