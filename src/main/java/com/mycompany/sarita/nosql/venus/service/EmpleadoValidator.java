package com.mycompany.sarita.nosql.venus.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class EmpleadoValidator {

    private static final java.util.regex.Pattern NAME_PATTERN =
            java.util.regex.Pattern.compile("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+(?: [A-Za-zÁÉÍÓÚÜÑáéíóúüñ]+)*$");
    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[^\\s@]+@[^\\s@]+\\.com$", java.util.regex.Pattern.CASE_INSENSITIVE);

    private EmpleadoValidator() {
    }

    public static ValidationResult validar(EmpleadoRequest datos) {
        List<String> errores = new ArrayList<>();

        if (datos == null) {
            errores.add("Los datos del empleado son obligatorios.");
            return new ValidationResult(false, errores);
        }

        validarNombre(datos.getNombre(), "nombre", errores);
        validarNombre(datos.getApellidoPaterno(), "apellido paterno", errores);
        if (datos.getApellidoMaterno() != null && !datos.getApellidoMaterno().isBlank()) {
            validarNombre(datos.getApellidoMaterno(), "apellido materno", errores);
        }

        LocalDate fechaNacimiento = datos.getFechaNacimiento();
        if (fechaNacimiento == null) {
            errores.add("La fecha de nacimiento es obligatoria.");
        } else {
            LocalDate edadMinima = LocalDate.now().minusYears(18);
            if (fechaNacimiento.isAfter(LocalDate.now()) || fechaNacimiento.isAfter(edadMinima)) {
                errores.add("La persona debe ser mayor o igual a 18 años.");
            }
        }

        LocalDate fechaIngreso = datos.getFechaIngreso();
        if (fechaIngreso == null) {
            errores.add("La fecha de ingreso es obligatoria.");
        } else {
            if (fechaNacimiento != null && fechaIngreso.isBefore(fechaNacimiento)) {
                errores.add("La fecha de ingreso no puede ser menor a la fecha de nacimiento.");
            }
            if (fechaIngreso.isAfter(LocalDate.now())) {
                errores.add("La fecha de ingreso no puede ser posterior a la fecha actual.");
            }
        }

        String correo = datos.getCorreoInstitucional();
        if (correo == null || correo.isBlank()) {
            errores.add("El correo institucional es obligatorio.");
        } else if (!EMAIL_PATTERN.matcher(correo.trim()).matches()) {
            errores.add("El correo institucional debe terminar en .com.");
        }

        String telefono = datos.getTelefonoCelular();
        if (telefono == null || telefono.isBlank()) {
            errores.add("El teléfono celular es obligatorio.");
        } else {
            String limpio = telefono.replaceAll("\\D+", "");
            if (!limpio.matches("\\d{10}")) {
                errores.add("El teléfono celular debe contener exactamente 10 dígitos.");
            }
        }

        return new ValidationResult(errores.isEmpty(), errores);
    }

    private static void validarNombre(String valor, String campo, List<String> errores) {
        if (valor == null || valor.isBlank()) {
            errores.add("El " + campo + " es obligatorio.");
        } else if (!NAME_PATTERN.matcher(valor.trim()).matches()) {
            errores.add("El " + campo + " solo puede contener letras y espacios.");
        }
    }

    public record ValidationResult(boolean isValido, List<String> errores) {
    }
}