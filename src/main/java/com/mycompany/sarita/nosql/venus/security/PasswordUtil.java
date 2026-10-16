
package com.mycompany.sarita.nosql.venus.security;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {

    private static final int ITERATIONS = 600000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtil() {
    }

    public static String hash(String password) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);

        byte[] derived = derive(password, salt, ITERATIONS);

        return "pbkdf2_sha256$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(derived);
    }

    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }

        try {
            String[] parts = stored.split("\\$", -1);

            if (parts.length != 4
                    || !parts[0].equals("pbkdf2_sha256")) {
                return false;
            }

            int iterations = Integer.parseInt(parts[1]);

            if (iterations != ITERATIONS) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);

            if (salt.length != SALT_LENGTH || expected.length != 32) {
                return false;
            }

            byte[] actual = derive(password, salt, iterations);

            return MessageDigest.isEqual(actual, expected);

        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private static byte[] derive(
            String password, byte[] salt, int iterations) {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(), salt, iterations, KEY_LENGTH);

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            return factory.generateSecret(spec).getEncoded();

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "No se pudo procesar la contraseña", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
