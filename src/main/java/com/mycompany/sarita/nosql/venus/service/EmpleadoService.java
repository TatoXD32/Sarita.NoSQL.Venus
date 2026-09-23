package com.mycompany.sarita.nosql.venus.service;

import com.mycompany.sarita.nosql.venus.model.CatEstatus;
import com.mycompany.sarita.nosql.venus.model.Departamento;
import com.mycompany.sarita.nosql.venus.model.Domicilio;
import com.mycompany.sarita.nosql.venus.model.Estado;
import com.mycompany.sarita.nosql.venus.model.Empleado;
import com.mycompany.sarita.nosql.venus.model.Puesto;
import com.mycompany.sarita.nosql.venus.model.Sexo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Locale;

@ApplicationScoped
public class EmpleadoService {

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager entityManager;

    public List<Empleado> listar() {
        return entityManager.createQuery("select e from Empleado e "
                + "join fetch e.sexo join fetch e.departamento join fetch e.puesto "
                + "join fetch e.estatus order by e.idEmpleado", Empleado.class)
                .getResultList();
    }

    public Empleado buscar(Long id) {
        return entityManager.find(Empleado.class, id);
    }

    private String normalizeText(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    public boolean correoExiste(String correo, Long idExcluir) {
        String query = "select count(e) from Empleado e where upper(e.correoInstitucional) = upper(:correo)"
                + (idExcluir == null ? "" : " and e.idEmpleado <> :id");
        var typedQuery = entityManager.createQuery(query, Long.class)
                .setParameter("correo", normalizeText(correo));
        if (idExcluir != null) {
            typedQuery.setParameter("id", idExcluir);
        }
        return typedQuery.getSingleResult() > 0;
    }

    @Transactional
    public Empleado actualizar(Long id, EmpleadoRequest datos) {
        Empleado empleado = buscar(id);
        if (empleado == null) {
            return null;
        }
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
        empleado.setEstatus(entityManager.getReference(CatEstatus.class, normalizeText(datos.getIdEstatus())));
        actualizarDomicilio(empleado.getDomicilio(), datos);
        return empleado;
    }

    @Transactional
    public Empleado crear(EmpleadoRequest datos) {
        String nombre = normalizeText(datos.getNombre());
        String apellidoPaterno = normalizeText(datos.getApellidoPaterno());
        String apellidoMaterno = normalizeText(datos.getApellidoMaterno());
        String calle = normalizeText(datos.getCalle());
        String correo = normalizeText(datos.getCorreoInstitucional());
        String telefonoCelular = normalizeText(datos.getTelefonoCelular());
        String idEstatus = normalizeText(datos.getIdEstatus());
        if (idEstatus == null || idEstatus.isBlank()) {
            idEstatus = "ACTIVO";
        }

        entityManager.createNativeQuery("insert into domicilio (calle, codigo_postal, numero_exterior, numero_interior, id_estado) values (?, ?, ?, ?, ?)")
            .setParameter(1, calle)
            .setParameter(2, datos.getCodigoPostal())
            .setParameter(3, normalizeText(datos.getNumeroExterior()))
            .setParameter(4, normalizeText(datos.getNumeroInterior()))
            .setParameter(5, normalizeText(datos.getIdEstado()))
            .executeUpdate();

        Long idDomicilio = entityManager.createQuery("select d.idDomicilio from Domicilio d where upper(d.calle) = :calle and d.codigoPostal = :cp and upper(d.numeroExterior) = :exterior and upper(d.estado.idEstado) = :estado order by d.idDomicilio desc", Long.class)
            .setParameter("calle", calle)
            .setParameter("cp", datos.getCodigoPostal())
            .setParameter("exterior", normalizeText(datos.getNumeroExterior()))
            .setParameter("estado", normalizeText(datos.getIdEstado()))
            .setMaxResults(1)
            .getSingleResult();

        entityManager.createNativeQuery("insert into empleado (nombre, apellido_paterno, apellido_materno, fecha_nacimiento, correo_institucional, telefono_celular, fecha_ingreso, id_sexo, id_domicilio, id_depto, id_puesto, id_estatus) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
                .setParameter(1, nombre)
                .setParameter(2, apellidoPaterno)
                .setParameter(3, apellidoMaterno)
                .setParameter(4, datos.getFechaNacimiento())
                .setParameter(5, correo)
                .setParameter(6, telefonoCelular)
                .setParameter(7, datos.getFechaIngreso())
                .setParameter(8, normalizeText(datos.getIdSexo()))
                .setParameter(9, idDomicilio)
                .setParameter(10, datos.getIdDepto())
                .setParameter(11, datos.getIdPuesto())
                .setParameter(12, idEstatus)
                .executeUpdate();
        return entityManager.createQuery("select e from Empleado e where upper(e.correoInstitucional) = upper(:correo)", Empleado.class)
                .setParameter("correo", correo).getSingleResult();
    }

    private void actualizarDomicilio(Domicilio domicilio, EmpleadoRequest datos) {
        entityManager.createNativeQuery("update domicilio set calle = ?, codigo_postal = ?, numero_exterior = ?, numero_interior = ?, id_estado = ? where id_domicilio = ?")
                .setParameter(1, normalizeText(datos.getCalle()))
                .setParameter(2, datos.getCodigoPostal())
                .setParameter(3, normalizeText(datos.getNumeroExterior()))
                .setParameter(4, normalizeText(datos.getNumeroInterior()))
                .setParameter(5, normalizeText(datos.getIdEstado()))
                .setParameter(6, domicilio.getIdDomicilio())
                .executeUpdate();
    }

    @Transactional
    public boolean darDeBaja(Long id) {
        Empleado empleado = buscar(id);
        if (empleado == null) {
            return false;
        }
        empleado.setEstatus(entityManager.getReference(CatEstatus.class, "BAJA"));
        return true;
    }

    public Catalogos catalogos() {
        return new Catalogos(
                entityManager.createQuery("select d from Departamento d where d.idEstatus = 'ACTIVO' order by d.descripcion", Departamento.class).getResultList(),
                entityManager.createQuery("select p from Puesto p where p.idEstatus = 'ACTIVO' order by p.descripcionPuesto", Puesto.class).getResultList(),
                entityManager.createQuery("select s from Sexo s order by s.descripcion", Sexo.class).getResultList(),
                entityManager.createQuery("select e from CatEstatus e order by e.descripcion", CatEstatus.class).getResultList(),
                entityManager.createQuery("select d from Domicilio d join fetch d.estado order by d.idDomicilio", Domicilio.class).getResultList(),
                entityManager.createQuery("select e from Estado e join fetch e.pais where e.idEstatus = 'ACTIVO' and e.pais.idEstatus = 'ACTIVO' order by e.nombre", Estado.class).getResultList());
    }

    public record Catalogos(List<Departamento> departamentos, List<Puesto> puestos, List<Sexo> sexos, List<CatEstatus> estatuses, List<Domicilio> domicilios, List<Estado> estados) { }
}