package com.mycompany.novatech.app;
public final class AdministradorFactory extends UsuarioFactory {
    @Override protected Usuario crearUsuario() { return new UsuarioAdministrador(); }
}
