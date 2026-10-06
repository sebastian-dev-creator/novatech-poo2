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

    private ConexionBD() {
    }

    public static Connection obtenerConexion() throws SQLException {
        String url = System.getenv("NOVATECH_DB_URL");
        String usuario = System.getenv("NOVATECH_DB_USER");
        String password = System.getenv("NOVATECH_DB_PASSWORD");

        if (url == null || usuario == null || password == null) {
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
