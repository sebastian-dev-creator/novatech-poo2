package com.mycompany.novatech.app.comercial;

import java.math.BigDecimal;

/** Reglas compartidas del dominio; también se aplican fuera del navegador. */
public final class Validacion {
    private Validacion() {}
    public static String texto(String valor, String nombre, int max, boolean obligatorio) {
        String v = valor == null ? "" : valor.trim();
        if ((obligatorio && v.isEmpty()) || v.length() > max)
            throw new IllegalArgumentException(nombre + ": completa el campo (máximo " + max + " caracteres).");
        return v;
    }
    public static int id(String valor) {
        if (valor == null || valor.isBlank()) return 0;
        try { int n = Integer.parseInt(valor); if (n >= 0) return n; }
        catch (NumberFormatException ignored) {}
        throw new IllegalArgumentException("Identificador inválido.");
    }
    public static BigDecimal decimal(String valor, String campo, int escala) {
        try {
            BigDecimal n = new BigDecimal(valor);
            if (n.signum() >= 0 && n.compareTo(new BigDecimal("999999999.99")) <= 0)
                return n.setScale(escala, java.math.RoundingMode.UNNECESSARY);
        } catch (NumberFormatException | ArithmeticException | NullPointerException ignored) {}
        throw new IllegalArgumentException(campo + ": introduce un número no negativo, con hasta " + escala + " decimales.");
    }
    public static void contacto(String correo, String telefono) {
        texto(correo,"Correo",254,false); texto(telefono,"Teléfono",20,false);
        if (!correo.isEmpty() && !correo.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            throw new IllegalArgumentException("Introduce un correo válido.");
        if (!telefono.isEmpty() && (!telefono.matches("[+0-9() .-]{5,20}") || !telefono.matches(".*[0-9].*")))
            throw new IllegalArgumentException("Revisa el teléfono.");
    }
}
