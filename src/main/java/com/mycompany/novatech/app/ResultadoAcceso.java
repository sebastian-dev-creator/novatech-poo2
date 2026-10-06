package com.mycompany.novatech.app;

/** Resultado explícito: el controlador distingue bloqueo persistente de credenciales inválidas. */
public final class ResultadoAcceso {
    public enum Estado { CORRECTO, DENEGADO, BLOQUEADO }
    public final Estado estado;
    public final Usuario usuario;
    private ResultadoAcceso(Estado estado, Usuario usuario) { this.estado=estado; this.usuario=usuario; }
    public static ResultadoAcceso correcto(Usuario usuario) { return new ResultadoAcceso(Estado.CORRECTO,usuario); }
    public static ResultadoAcceso denegado() { return new ResultadoAcceso(Estado.DENEGADO,null); }
    public static ResultadoAcceso bloqueado() { return new ResultadoAcceso(Estado.BLOQUEADO,null); }
}
