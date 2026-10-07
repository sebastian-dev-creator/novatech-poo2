package com.mycompany.novatech.app;

/** Entidad de dominio. Los permisos se resuelven en el servidor. */
public abstract class Usuario {
    public int id;
    public int rolId;
    public String username, nombres, apellidos, rol;
    public boolean activo, bloqueado;
    public int intentos;
    public String dni;
    public Integer sexoId, estadoCivilId;
    public java.util.Set<String> permisos = new java.util.HashSet<>();
    public boolean puede(String permiso) { return permisos.contains(permiso); }
    public abstract boolean esAdministrador();
}
