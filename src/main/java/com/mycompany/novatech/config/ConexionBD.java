/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author gamep
 */

package com.mycompany.novatech.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConexionBD {

    // Bloque 1: configuración privada e inmutable del gestor.
    private final String url;
    private final String usuario;
    private final String password;

    // Bloque 2: impide crear gestores desde otras clases mediante new.
    private ConexionBD() {
        url = System.getenv("NOVATECH_DB_URL");
        usuario = System.getenv("NOVATECH_DB_USER");
        password = System.getenv("NOVATECH_DB_PASSWORD");
    }

    // Bloque 3: Java inicializa esta clase una vez, con seguridad entre hilos.
    private static class InstanciaHolder {
        private static final ConexionBD INSTANCIA = new ConexionBD();
    }

    // Bloque 4: acceso global al mismo gestor por cargador de clases.
    public static ConexionBD getInstancia() {
        return InstanciaHolder.INSTANCIA;
    }

    // Bloque 5: cada operación recibe su propia Connection y debe cerrarla.
    // Singleton se aplica al gestor, no a una conexión física compartida.
    public Connection obtenerConexion() throws SQLException {

        if (url == null || url.isBlank() || usuario == null || usuario.isBlank()
                || password == null || password.isEmpty()) {
            throw new SQLException(
                "Faltan las variables de entorno de la base de datos."
            );
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró MySQL Connector/J.", e);
        }

        return DriverManager.getConnection(url, usuario, password);
    }
}
