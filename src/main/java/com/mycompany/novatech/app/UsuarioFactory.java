package com.mycompany.novatech.app;
/** Factory: centraliza la creación del modelo de un formulario nuevo. */
public final class UsuarioFactory {
    private UsuarioFactory() {}
    public static Usuario nuevo() {
        Usuario u=new Usuario();u.username="";u.nombres="";u.apellidos="";u.activo=true;return u;
    }
}
