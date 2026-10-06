/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author gamep
 */

package com.mycompany.novatech.seguridad;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {

    private static final int ITERACIONES = 600_000;
    private static final int LONGITUD_BITS = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String generarHash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                    "La contraseña no puede estar vacía.");
        }

        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        byte[] hash = calcularHash(password, salt, ITERACIONES);

        return "pbkdf2_sha256$" + ITERACIONES + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verificar(String password, String hashGuardado) {
        if (password == null || hashGuardado == null
                || hashGuardado.length() > 255) {
            return false;
        }

        try {
            String[] partes = hashGuardado.split("\\$", -1);

            if (partes.length != 4
                    || !"pbkdf2_sha256".equals(partes[0])) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[1]);

            if (iteraciones < ITERACIONES || iteraciones > 2_000_000) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(partes[2]);
            byte[] hashEsperado = Base64.getDecoder().decode(partes[3]);

            if (salt.length != 16 || hashEsperado.length != 32) {
                return false;
            }

            byte[] hashCalculado =
                    calcularHash(password, salt, iteraciones);

            return MessageDigest.isEqual(hashEsperado, hashCalculado);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] calcularHash(
            String password, byte[] salt, int iteraciones) {

        PBEKeySpec especificacion = new PBEKeySpec(
                password.toCharArray(), salt,
                iteraciones, LONGITUD_BITS);

        try {
            SecretKeyFactory fabrica =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            return fabrica.generateSecret(especificacion).getEncoded();

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                    "No se pudo procesar la contraseña.", e);
        } finally {
            especificacion.clearPassword();
        }
    }
}