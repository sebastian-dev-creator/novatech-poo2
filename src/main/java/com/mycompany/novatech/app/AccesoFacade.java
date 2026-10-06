package com.mycompany.novatech.app;
import java.sql.SQLException;
/** Facade del módulo de acceso: valida entradas y coordina autenticación. */
public final class AccesoFacade {
    private final UsuarioRepository repository=new UsuarioRepository();
    public Usuario ingresar(String nombre,String password) throws SQLException {
        return intentar(nombre,password).usuario;
    }
    public ResultadoAcceso intentar(String nombre,String password) throws SQLException {
        if(nombre==null || password==null || nombre.length()>50 || password.length()>128 || password.isEmpty())return ResultadoAcceso.denegado();
        return repository.autenticarConEstado(nombre.trim(),password);
    }
}
