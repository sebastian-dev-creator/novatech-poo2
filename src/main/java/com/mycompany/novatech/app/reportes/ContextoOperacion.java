package com.mycompany.novatech.app.reportes;

/** Actor por petición. Se retira al finalizar para evitar mezclar sesiones entre hilos. */
public final class ContextoOperacion {
    private static final ThreadLocal<String> ACTOR = new ThreadLocal<>();
    private ContextoOperacion() {}
    public static void establecer(String actor) { ACTOR.set(actor); }
    public static String actor() { return ACTOR.get() == null ? "Sistema" : ACTOR.get(); }
    public static void limpiar() { ACTOR.remove(); }
}
