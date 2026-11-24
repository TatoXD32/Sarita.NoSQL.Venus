package com.mycompany.sarita.nosql.venus.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EmpleadoValidatorTest {

    @Test
    void rechazaMenoresDe18() {
        EmpleadoRequest request = baseRequest();
        request.setFechaNacimiento(LocalDate.now());

        assertFalse(EmpleadoValidator.validar(request).isValido());
    }

    @Test
    void aceptaPersonaMayorDeEdad() {
        EmpleadoRequest request = baseRequest();
        request.setFechaNacimiento(LocalDate.now().minusYears(25));

        assertTrue(EmpleadoValidator.validar(request).isValido());
    }

    @Test
    void rechazaTelefonoConLongitudInvalida() {
        EmpleadoRequest request = baseRequest();
        request.setFechaNacimiento(LocalDate.now().minusYears(25));
        request.setTelefonoCelular("12345");

        assertFalse(EmpleadoValidator.validar(request).isValido());
    }

    @Test
    void aceptaTelefonoDe10Digitos() {
        EmpleadoRequest request = baseRequest();
        request.setFechaNacimiento(LocalDate.now().minusYears(25));
        request.setTelefonoCelular("5512345678");

        assertTrue(EmpleadoValidator.validar(request).isValido());
    }

    @Test
    void rechazaNombreConNumeros() {
        EmpleadoRequest request = baseRequest();
        request.setNombre("Juan123");

        assertFalse(EmpleadoValidator.validar(request).isValido());
    }

    @Test
    void rechazaIngresoAnteriorAlNacimiento() {
        EmpleadoRequest request = baseRequest();
        request.setFechaNacimiento(LocalDate.now().minusYears(25));
        request.setFechaIngreso(LocalDate.now().minusYears(26));

        assertFalse(EmpleadoValidator.validar(request).isValido());
    }

    @Test
    void rechazaIngresoPosteriorAHoy() {
        EmpleadoRequest request = baseRequest();
        request.setFechaIngreso(LocalDate.now().plusDays(1));

        assertFalse(EmpleadoValidator.validar(request).isValido());
    }

    @Test
    void rechazaCorreoQueNoTerminaEnCom() {
        EmpleadoRequest request = baseRequest();
        request.setCorreoInstitucional("empleado@empresa.mx");

        assertFalse(EmpleadoValidator.validar(request).isValido());
    }

    private static EmpleadoRequest baseRequest() {
        EmpleadoRequest request = new EmpleadoRequest();
        request.setNombre("Juan");
        request.setApellidoPaterno("Perez");
        request.setApellidoMaterno("Lopez");
        request.setFechaNacimiento(LocalDate.now().minusYears(25));
        request.setFechaIngreso(LocalDate.now().minusYears(1));
        request.setCorreoInstitucional("empleado@empresa.com");
        request.setTelefonoCelular("5512345678");
        return request;
    }
}
