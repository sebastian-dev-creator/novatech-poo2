package com.mycompany.novatech.app.ventas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Observadores locales que participan en la misma transacción que el cambio de estado. */
final class EventosVenta {
    interface Observador {
        void alCambiar(Connection conexion, Venta venta, String estado) throws SQLException;
    }

    private final List<Observador> suscriptores = new ArrayList<>();

    void suscribir(Observador observador) {
        suscriptores.add(observador);
    }

    void publicar(Connection conexion, Venta venta, String estado) throws SQLException {
        for (Observador observador : suscriptores) {
            observador.alCambiar(conexion, venta, estado);
        }
    }

    static void actualizarGuia(Connection conexion, Venta venta, String estado) throws SQLException {
        if ("PAGADA".equals(estado) && venta.detalles.stream().allMatch(d -> "SERVICIO".equals(d.tipo))) {
            return;
        }
        String sql = "PAGADA".equals(estado)
                ? "INSERT INTO guias(id_venta) VALUES (?)"
                : "UPDATE guias SET estado='ANULADA' WHERE id_venta=?";
        try (PreparedStatement p = conexion.prepareStatement(sql)) {
            p.setInt(1, venta.id);
            p.executeUpdate();
        }
    }
}
