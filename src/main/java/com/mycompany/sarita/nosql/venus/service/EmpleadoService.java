package com.mycompany.sarita.nosql.venus.service;

import com.mycompany.sarita.nosql.venus.model.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Locale;
import java.sql.Date;
import java.math.BigDecimal;

@ApplicationScoped
public class EmpleadoService {
    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager entityManager;

    private String normalizeText(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    public List<Empleado> listar() {
        return entityManager.createQuery("select e from Empleado e "
                + "join fetch e.sexo join fetch e.departamento join fetch e.puesto "
                + "join fetch e.estatus order by e.idEmpleado", Empleado.class).getResultList();
    }

    public Empleado buscar(Long id) { return entityManager.find(Empleado.class, id); }

    public boolean correoExiste(String correo, Long idExcluir) {
        String query = "select count(e) from Empleado e where upper(e.correoInstitucional) = upper(:correo)"
                + (idExcluir == null ? "" : " and e.idEmpleado <> :id");
        var typedQuery = entityManager.createQuery(query, Long.class)
                .setParameter("correo", normalizeText(correo));
        if (idExcluir != null) typedQuery.setParameter("id", idExcluir);
        return typedQuery.getSingleResult() > 0;
    }

    private void aplicarEmpleado(Empleado empleado, EmpleadoRequest datos) {
        empleado.setNombre(normalizeText(datos.getNombre()));
        empleado.setApellidoPaterno(normalizeText(datos.getApellidoPaterno()));
        empleado.setApellidoMaterno(normalizeText(datos.getApellidoMaterno()));
        empleado.setFechaNacimiento(datos.getFechaNacimiento());
        empleado.setFechaIngreso(datos.getFechaIngreso());
        empleado.setCorreoInstitucional(normalizeText(datos.getCorreoInstitucional()));
        empleado.setTelefonoCelular(normalizeText(datos.getTelefonoCelular()));
        empleado.setSexo(entityManager.getReference(Sexo.class, normalizeText(datos.getIdSexo())));
        empleado.setDepartamento(entityManager.getReference(Departamento.class, datos.getIdDepto()));
        empleado.setPuesto(entityManager.getReference(Puesto.class, datos.getIdPuesto()));
        String estatus = normalizeText(datos.getIdEstatus());
        if (estatus == null || estatus.isBlank()) estatus = "ACTIVO";
        empleado.setEstatus(entityManager.getReference(CatEstatus.class, estatus));
    }

    private void aplicarDomicilio(Domicilio domicilio, EmpleadoRequest datos) {
        domicilio.setCalle(normalizeText(datos.getCalle()));
        domicilio.setCodigoPostal(datos.getCodigoPostal());
        domicilio.setNumeroExterior(normalizeText(datos.getNumeroExterior()));
        domicilio.setNumeroInterior(normalizeText(datos.getNumeroInterior()));
        domicilio.setEstado(entityManager.getReference(Estado.class, normalizeText(datos.getIdEstado())));
    }

    @Transactional
public Empleado crear(EmpleadoRequest datos) {

    String estatus = normalizeText(datos.getIdEstatus());
    if (estatus == null || estatus.isBlank()) {
        estatus = "ACTIVO";
    }

    entityManager.createNativeQuery("""
        INSERT INTO SARITA.EMPLEADOS
        (NOMBRE, APELLIDO_PATERNO, APELLIDO_MATERNO,
         FECHA_NACIMIENTO, FECHA_INGRESO,
         CORREO_INSTITUCIONAL, TELEFONO_CELULAR,
         ID_SEXO, ID_DEPTO, ID_PUESTO, ID_ESTATUS)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """)
        .setParameter(1, normalizeText(datos.getNombre()))
        .setParameter(2, normalizeText(datos.getApellidoPaterno()))
        .setParameter(3, normalizeText(datos.getApellidoMaterno()))
        .setParameter(4, Date.valueOf(datos.getFechaNacimiento()))
        .setParameter(5, Date.valueOf(datos.getFechaIngreso()))
        .setParameter(6, normalizeText(datos.getCorreoInstitucional()))
        .setParameter(7, normalizeText(datos.getTelefonoCelular()))
        .setParameter(8, normalizeText(datos.getIdSexo()))
        .setParameter(9, datos.getIdDepto())
        .setParameter(10, datos.getIdPuesto())
        .setParameter(11, estatus)
        .executeUpdate();

    entityManager.flush();

    Number id = (Number) entityManager.createNativeQuery("""
        SELECT ID_EMPLEADO
        FROM SARITA.EMPLEADOS
        WHERE CORREO_INSTITUCIONAL = ?
        """)
        .setParameter(1, normalizeText(datos.getCorreoInstitucional()))
        .getSingleResult();

    Long idEmpleado = id.longValue();

    entityManager.createNativeQuery("""
        INSERT INTO SARITA.DOMICILIOS
        (ID_EMPLEADO, CALLE, CODIGO_POSTAL,
         NUMERO_EXTERIOR, NUMERO_INTERIOR, ID_ESTADO)
        VALUES (?, ?, ?, ?, ?, ?)
        """)
        .setParameter(1, idEmpleado)
        .setParameter(2, normalizeText(datos.getCalle()))
        .setParameter(3, datos.getCodigoPostal())
        .setParameter(4, normalizeText(datos.getNumeroExterior()))
        .setParameter(5, normalizeText(datos.getNumeroInterior()))
        .setParameter(6, normalizeText(datos.getIdEstado()))
        .executeUpdate();

    entityManager.flush();
    entityManager.clear();

    return entityManager.find(Empleado.class, idEmpleado);
}

    @Transactional
    public Empleado actualizar(Long id, EmpleadoRequest datos) {
        Empleado empleado = buscar(id);
        if (empleado == null) return null;
        aplicarEmpleado(empleado, datos);
        Domicilio domicilio = empleado.getDomicilio();
        if (domicilio == null) {
            domicilio = new Domicilio();
            aplicarDomicilio(domicilio, datos);
            empleado.setDomicilio(domicilio);
            entityManager.persist(domicilio);
        } else {
            aplicarDomicilio(domicilio, datos);
        }
        return empleado;
    }

    @Transactional
    public boolean darDeBaja(Long id) {
        Empleado empleado = buscar(id);
        if (empleado == null) return false;
        empleado.setEstatus(entityManager.getReference(CatEstatus.class, "BAJA"));
        return true;
    }

    public Catalogos catalogos() {
        return new Catalogos(
            entityManager.createQuery("select d from Departamento d order by d.descripcion", Departamento.class).getResultList(),
            entityManager.createQuery("select p from Puesto p order by p.descripcionPuesto", Puesto.class).getResultList(),
            entityManager.createQuery("select s from Sexo s order by s.descripcion", Sexo.class).getResultList(),
            entityManager.createQuery("select e from CatEstatus e order by e.descripcion", CatEstatus.class).getResultList(),
            entityManager.createQuery("select d from Domicilio d join fetch d.estado order by d.idEmpleado", Domicilio.class).getResultList(),
            entityManager.createQuery("select e from Estado e join fetch e.pais order by e.nombre", Estado.class).getResultList()
        );
    }

    public record Catalogos(List<Departamento> departamentos, List<Puesto> puestos,
            List<Sexo> sexos, List<CatEstatus> estatuses,
            List<Domicilio> domicilios, List<Estado> estados) { }
}