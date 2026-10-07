package com.mycompany.novatech.app;

/** Creador abstracto. Las subclases deciden el producto mediante crearUsuario(). */
public abstract class UsuarioFactory {
    protected abstract Usuario crearUsuario();
    public final Usuario crear() {
        Usuario u=crearUsuario();
        u.username="";u.nombres="";u.apellidos="";u.activo=true;
        return u;
    }
    public static UsuarioFactory paraRol(String rol) {
        return "ADMINISTRADOR".equals(rol)?new AdministradorFactory():new OperativoFactory();
    }
    public static Usuario nuevo() { return new OperativoFactory().crear(); }
}
