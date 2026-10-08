package com.mycompany.novatech.app.comercial;
import java.sql.SQLException;
import java.util.List;

/** Contrato de persistencia compartido por los tres módulos de la etapa 5. */
public interface Repository<T> {
    List<T> listar(String busqueda) throws SQLException;
    T buscar(int id) throws SQLException;
    int guardar(T entidad, String actor) throws SQLException;
    void eliminar(int id) throws SQLException;
}
