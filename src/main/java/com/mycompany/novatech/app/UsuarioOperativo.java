package com.mycompany.novatech.app;
/** Producto concreto: operador u otro rol sin administración. */
public final class UsuarioOperativo extends Usuario {
    @Override public boolean esAdministrador() { return false; }
}
