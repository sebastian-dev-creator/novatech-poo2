package com.mycompany.novatech.app.ventas;

/** State: cada estado decide qué operaciones permite y a cuál transita. */
public abstract class EstadoVenta {
    public void editar(){throw new IllegalArgumentException("Las ventas registradas no se editan ni se eliminan. Anula la venta y registra una nueva.");}
    public String pagar(){throw new IllegalArgumentException("La venta ya no está pendiente.");}
    public String anular(){throw new IllegalArgumentException("La venta ya está anulada.");}
    public static EstadoVenta de(String estado) {
        switch(estado) {
            case "PENDIENTE": return new Pendiente();
            case "PAGADA": return new Pagada();
            case "ANULADA": return new Anulada();
            default: throw new IllegalArgumentException("Estado de venta inválido.");
        }
    }
    private static final class Pendiente extends EstadoVenta {
        @Override public String anular(){return "ANULADA";}
        @Override public String pagar(){return "PAGADA";}
    }
    private static final class Pagada extends EstadoVenta {
        @Override public String anular(){return "ANULADA";}
    }
    private static final class Anulada extends EstadoVenta {}
}
