package com.mycompany.novatech.app;
/** Producto concreto del Factory Method: perfil administrador. */
public final class UsuarioAdministrador extends Usuario {
    @Override public boolean esAdministrador() { return true; }
}
