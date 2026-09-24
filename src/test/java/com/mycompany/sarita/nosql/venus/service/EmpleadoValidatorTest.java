package com.mycompany.sarita.nosql.venus.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EmpleadoValidatorTest {

    @Test
    void rechazaMenoresDe18() {
        EmpleadoRequest request = new EmpleadoRequest();
        request.setFechaNacimiento(LocalDate.now());
        request.setTelefonoCelular("5512345678");

        assertFalse(EmpleadoValidator.validar(request).isValido());
    }

    @Test
    void aceptaPersonaMayorDeEdad() {
        EmpleadoRequest request = new EmpleadoRequest();
        request.setFechaNacimiento(LocalDate.now().minusYears(25));
        request.setTelefonoCelular("5512345678");

        assertTrue(EmpleadoValidator.validar(request).isValido());
    }

    @Test
    void rechazaTelefonoConLongitudInvalida() {
        EmpleadoRequest request = new EmpleadoRequest();
        request.setFechaNacimiento(LocalDate.now().minusYears(25));
        request.setTelefonoCelular("12345");

        assertFalse(EmpleadoValidator.validar(request).isValido());
    }

    @Test
    void aceptaTelefonoDe10Digitos() {
        EmpleadoRequest request = new EmpleadoRequest();
        request.setFechaNacimiento(LocalDate.now().minusYears(25));
        request.setTelefonoCelular("5512345678");

        assertTrue(EmpleadoValidator.validar(request).isValido());
    }
}
