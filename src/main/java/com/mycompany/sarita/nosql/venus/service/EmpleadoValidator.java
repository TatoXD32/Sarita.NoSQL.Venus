package com.mycompany.sarita.nosql.venus.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class EmpleadoValidator {

    private EmpleadoValidator() {
    }

    public static ValidationResult validar(EmpleadoRequest datos) {
        List<String> errores = new ArrayList<>();

        if (datos == null) {
            errores.add("Los datos del empleado son obligatorios.");
            return new ValidationResult(false, errores);
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

    public record ValidationResult(boolean isValido, List<String> errores) {
    }
}