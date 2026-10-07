package com.mycompany.novatech.app;

import java.sql.SQLException;
import java.util.List;

/** Contrato de persistencia del módulo de usuarios: no contiene SQL ni objetos Connection. */
public interface UsuarioRepository {
    List<Usuario> listar() throws SQLException;
    Usuario buscar(int id) throws SQLException;
    List<String[]> roles() throws SQLException;
    List<String[]> catalogoPersonal(boolean sexo) throws SQLException;
    Usuario autenticar(String nombre,String password) throws SQLException;
    ResultadoAcceso autenticarConEstado(String nombre,String password) throws SQLException;
    void guardar(int id,String nombre,String nombres,String apellidos,int rol,String password,boolean activo) throws SQLException;
    void guardar(int id,String nombre,String nombres,String apellidos,int rol,String password,boolean activo,DatosPersonales datos,String actor) throws SQLException;
    void accion(int id,int actor,String accion) throws SQLException;
    void guardarRol(int id,String nombre,String descripcion,String actor) throws SQLException;
    void eliminarRol(int id) throws SQLException;
}
