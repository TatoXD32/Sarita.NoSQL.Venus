package com.mycompany.sarita.nosql.venus.security;

import java.io.Console;
import java.util.Arrays;

public class GenerarAdmin {

    public static void main(String[] args) {
        Console console = System.console();

        if (console == null) {
            System.err.println("Ejecuta el programa desde la terminal.");
            return;
        }

        char[] password = console.readPassword(
                "Contraseña para admin (mínimo 12 caracteres): ");

        if (password == null) {
            return;
        }

        try {
            if (password.length < 12) {
                System.err.println("La contraseña es demasiado corta.");
                return;
            }

            String hash = PasswordUtil.hash(new String(password));

            System.out.println("Usuario: admin");
            System.out.println("Hash para el campo credenciales.contrasena_hash:");
            System.out.println(hash);

        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
