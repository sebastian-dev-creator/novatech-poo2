package com.mycompany.novatech.app;

/** Entidad de dominio. Los permisos se resuelven en el servidor. */
public final class Usuario {
    public int id;
    public int rolId;
    public String username, nombres, apellidos, rol;
    public boolean activo, bloqueado;
    public int intentos;
    public String dni;
    public Integer sexoId, estadoCivilId;
    public boolean esAdministrador() { return "ADMINISTRADOR".equals(rol); }
}
