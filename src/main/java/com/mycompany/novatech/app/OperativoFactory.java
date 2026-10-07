package com.mycompany.novatech.app;
public final class OperativoFactory extends UsuarioFactory {
    @Override protected Usuario crearUsuario() { return new UsuarioOperativo(); }
}
