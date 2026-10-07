package com.mycompany.novatech.app;
import java.sql.SQLException;
import java.util.List;
public interface ContactoRepository {
    List<Contacto> listar(int usuario,String clase)throws SQLException;
    void guardar(Contacto contacto,String clase,String actor)throws SQLException;
    void eliminar(int usuario,int id,String clase)throws SQLException;
}
