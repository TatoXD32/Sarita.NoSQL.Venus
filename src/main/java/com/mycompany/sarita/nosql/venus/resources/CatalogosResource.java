
package com.mycompany.sarita.nosql.venus.resources;

import com.mycompany.sarita.nosql.venus.model.CatEstatus;
import com.mycompany.sarita.nosql.venus.model.Curso;
import com.mycompany.sarita.nosql.venus.model.Estado;
import com.mycompany.sarita.nosql.venus.model.Departamento;
import com.mycompany.sarita.nosql.venus.model.Pais;
import com.mycompany.sarita.nosql.venus.model.Puesto;
import com.mycompany.sarita.nosql.venus.model.Sexo;
import com.mycompany.sarita.nosql.venus.service.CursoRequest;
import com.mycompany.sarita.nosql.venus.service.DepartamentoRequest;
import com.mycompany.sarita.nosql.venus.service.EstadoRequest;
import com.mycompany.sarita.nosql.venus.service.EstatusRequest;
import com.mycompany.sarita.nosql.venus.service.PaisRequest;
import com.mycompany.sarita.nosql.venus.service.PuestoRequest;
import com.mycompany.sarita.nosql.venus.service.SexoRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Locale;

@Path("catalogos")
@Produces("application/json; charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public class CatalogosResource {

    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager entityManager;

    private String normalizeText(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    @GET
    @Path("estatus")
    public List<CatEstatus> estatus(@QueryParam("categoria") String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return entityManager
                    .createQuery(
                            "select e from CatEstatus e order by e.categoria, e.descripcion",
                            CatEstatus.class)
                    .getResultList();
        }

        return entityManager
                .createQuery(
                        "select e from CatEstatus e where e.categoria = :categoria order by e.descripcion",
                        CatEstatus.class)
                .setParameter("categoria", categoria.toUpperCase())
                .getResultList();
    }

    @POST
    @Path("estatus")
    @Transactional
    public Response crearEstatus(EstatusRequest request) {
        entityManager
                .createNativeQuery(
                        "insert into cat_estatus (id_estatus, descripcion, categoria) values (?, ?, ?)")
                .setParameter(1, request.getIdEstatus())
                .setParameter(2, request.getDescripcion())
                .setParameter(3, request.getCategoria())
                .executeUpdate();

        CatEstatus estatus = entityManager.find(
                CatEstatus.class,
                request.getIdEstatus());

        return Response.status(Response.Status.CREATED)
                .entity(estatus)
                .build();
    }

    @PUT
    @Path("estatus/{id}")
    @Transactional
    public Response actualizarEstatus(
            @PathParam("id") String id,
            EstatusRequest request) {

        CatEstatus estatus = entityManager.find(CatEstatus.class, id);

        if (estatus == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        estatus.setDescripcion(request.getDescripcion());
        estatus.setCategoria(request.getCategoria());

        return Response.ok(estatus).build();
    }

    @GET
    @Path("cursos")
    public List<Curso> cursos() {
        return entityManager
                .createQuery(
                        "select c from Curso c order by c.nombre",
                        Curso.class)
                .getResultList();
    }

    @POST
    @Path("cursos")
    @Transactional
    public Response crearCurso(CursoRequest request) {

        String nombre = normalizeText(request.getNombre());
        String descripcion = normalizeText(request.getDescripcion());
        String idEstatus = normalizeText(request.getIdEstatus());

        if (idEstatus == null || idEstatus.isBlank()) {
            idEstatus = "ACTIVO";
        }

        entityManager
                .createNativeQuery(
                        "insert into curso (nombre, descripcion, id_estatus) values (?, ?, ?)")
                .setParameter(1, nombre)
                .setParameter(2, descripcion)
                .setParameter(3, idEstatus)
                .executeUpdate();

        Curso curso = entityManager
                .createQuery(
                        "select c from Curso c where upper(c.nombre) = :nombre",
                        Curso.class)
                .setParameter("nombre", nombre)
                .getSingleResult();

        return Response.status(Response.Status.CREATED)
                .entity(curso)
                .build();
    }

    @PUT
    @Path("cursos/{id}")
    @Transactional
    public Response actualizarCurso(
            @PathParam("id") Long id,
            CursoRequest request) {

        Curso curso = entityManager.find(Curso.class, id);

        if (curso == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        curso.setNombre(normalizeText(request.getNombre()));
        curso.setDescripcion(normalizeText(request.getDescripcion()));
        curso.setIdEstatus(normalizeText(request.getIdEstatus()));

        return Response.ok(curso).build();
    }

    @DELETE
    @Path("cursos/{id}")
    @Transactional
    public Response eliminarCurso(@PathParam("id") Long id) {

        Curso curso = entityManager.find(Curso.class, id);

        if (curso == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        curso.setIdEstatus("INACTIVO");

        return Response.noContent().build();
    }

    @GET
    @Path("departamentos")
    public List<Departamento> departamentos() {
        return entityManager
                .createQuery(
                        "select d from Departamento d order by d.descripcion",
                        Departamento.class)
                .getResultList();
    }

    @POST
    @Path("departamentos")
    @Transactional
    public Response crearDepartamento(DepartamentoRequest request) {

        String descripcion = normalizeText(request.getDescripcion());
        String idEstatus = normalizeText(request.getIdEstatus());

        if (idEstatus == null || idEstatus.isBlank()) {
            idEstatus = "ACTIVO";
        }

        entityManager
                .createNativeQuery(
                        "insert into departamento (descripcion, id_estatus) values (?, ?)")
                .setParameter(1, descripcion)
                .setParameter(2, idEstatus)
                .executeUpdate();

        Departamento departamento = entityManager
                .createQuery(
                        "select d from Departamento d where upper(d.descripcion) = :descripcion",
                        Departamento.class)
                .setParameter("descripcion", descripcion)
                .getSingleResult();

        return Response.status(Response.Status.CREATED)
                .entity(departamento)
                .build();
    }

    @PUT
    @Path("departamentos/{id}")
    @Transactional
    public Response actualizarDepartamento(
            @PathParam("id") Long id,
            DepartamentoRequest request) {

        Departamento departamento = entityManager.find(
                Departamento.class,
                id);

        if (departamento == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        departamento.setDescripcion(
                normalizeText(request.getDescripcion()));

        departamento.setIdEstatus(
                normalizeText(request.getIdEstatus()));

        return Response.ok(departamento).build();
    }

    @DELETE
    @Path("departamentos/{id}")
    @Transactional
    public Response eliminarDepartamento(@PathParam("id") Long id) {

        Departamento departamento = entityManager.find(
                Departamento.class,
                id);

        if (departamento == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        departamento.setIdEstatus("INACTIVO");

        return Response.noContent().build();
    }

    @GET
    @Path("puestos")
    public List<Puesto> puestos() {
        return entityManager
                .createQuery(
                        "select p from Puesto p order by p.descripcionPuesto",
                        Puesto.class)
                .getResultList();
    }

    @POST
    @Path("puestos")
    @Transactional
    public Response crearPuesto(PuestoRequest request) {

        String descripcion = normalizeText(
                request.getDescripcionPuesto());

        String idEstatus = normalizeText(
                request.getIdEstatus());

        if (idEstatus == null || idEstatus.isBlank()) {
            idEstatus = "ACTIVO";
        }

        entityManager
                .createNativeQuery(
                        "insert into puesto (descripcion_puesto, id_estatus) values (?, ?)")
                .setParameter(1, descripcion)
                .setParameter(2, idEstatus)
                .executeUpdate();

        Puesto puesto = entityManager
                .createQuery(
                        "select p from Puesto p where upper(p.descripcionPuesto) = :descripcion",
                        Puesto.class)
                .setParameter("descripcion", descripcion)
                .getSingleResult();

        return Response.status(Response.Status.CREATED)
                .entity(puesto)
                .build();
    }

    @PUT
    @Path("puestos/{id}")
    @Transactional
    public Response actualizarPuesto(
            @PathParam("id") Long id,
            PuestoRequest request) {

        Puesto puesto = entityManager.find(
                Puesto.class,
                id);

        if (puesto == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        puesto.setDescripcionPuesto(
                normalizeText(request.getDescripcionPuesto()));

        puesto.setIdEstatus(
                normalizeText(request.getIdEstatus()));

        return Response.ok(puesto).build();
    }

    @DELETE
    @Path("puestos/{id}")
    @Transactional
    public Response eliminarPuesto(@PathParam("id") Long id) {

        Puesto puesto = entityManager.find(
                Puesto.class,
                id);

        if (puesto == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        puesto.setIdEstatus("INACTIVO");

        return Response.noContent().build();
    }

    @GET
    @Path("sexos")
    public List<Sexo> sexos() {
        return entityManager
                .createQuery(
                        "select s from Sexo s order by s.descripcion",
                        Sexo.class)
                .getResultList();
    }

    @POST
    @Path("sexos")
    @Transactional
    public Response crearSexo(SexoRequest request) {

        Sexo sexo = new Sexo();

        sexo.setIdSexo(
                normalizeText(request.getIdSexo()));

        sexo.setDescripcion(
                normalizeText(request.getDescripcion()));

        entityManager.persist(sexo);

        return Response.status(Response.Status.CREATED)
                .entity(sexo)
                .build();
    }

    @PUT
    @Path("sexos/{id}")
    @Transactional
    public Response actualizarSexo(
            @PathParam("id") String id,
            SexoRequest request) {

        Sexo sexo = entityManager.find(
                Sexo.class,
                id);

        if (sexo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        sexo.setDescripcion(
                normalizeText(request.getDescripcion()));

        return Response.ok(sexo).build();
    }

    @DELETE
    @Path("sexos/{id}")
    @Transactional
    public Response eliminarSexo(@PathParam("id") String id) {

        Sexo sexo = entityManager.find(
                Sexo.class,
                id);

        if (sexo == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        entityManager.remove(sexo);

        return Response.noContent().build();
    }

    @GET
    @Path("paises")
    public List<Pais> paises() {
        return entityManager
                .createQuery(
                        "select p from Pais p order by p.nombre",
                        Pais.class)
                .getResultList();
    }

    @POST
    @Path("paises")
    @Transactional
    public Response crearPais(PaisRequest request) {

        String idEstatus = normalizeText(
                request.getIdEstatus());

        if (idEstatus == null || idEstatus.isBlank()) {
            idEstatus = "ACTIVO";
        }

        Pais pais = new Pais();

        pais.setIdPais(
                normalizeText(request.getIdPais()));

        pais.setNombre(
                normalizeText(request.getNombre()));

        pais.setIdEstatus(idEstatus);

        entityManager.persist(pais);

        return Response.status(Response.Status.CREATED)
                .entity(pais)
                .build();
    }

    @PUT
    @Path("paises/{id}")
    @Transactional
    public Response actualizarPais(
            @PathParam("id") String id,
            PaisRequest request) {

        Pais pais = entityManager.find(
                Pais.class,
                id);

        if (pais == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        pais.setNombre(
                normalizeText(request.getNombre()));

        pais.setIdEstatus(
                normalizeText(request.getIdEstatus()));

        return Response.ok(pais).build();
    }

    @DELETE
    @Path("paises/{id}")
    @Transactional
    public Response eliminarPais(@PathParam("id") String id) {

        Pais pais = entityManager.find(
                Pais.class,
                id);

        if (pais == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        pais.setIdEstatus("INACTIVO");

        return Response.noContent().build();
    }

    @GET
    @Path("estados")
    public List<Estado> estados() {
        return entityManager
                .createQuery(
                        "select e from Estado e join fetch e.pais order by e.pais.nombre, e.nombre",
                        Estado.class)
                .getResultList();
    }

    @POST
    @Path("estados")
    @Transactional
    public Response crearEstado(EstadoRequest request) {

        String idEstatus = normalizeText(
                request.getIdEstatus());

        if (idEstatus == null || idEstatus.isBlank()) {
            idEstatus = "ACTIVO";
        }

        Estado estado = new Estado();

        estado.setIdEstado(
                normalizeText(request.getIdEstado()));

        estado.setNombre(
                normalizeText(request.getNombre()));

        estado.setPais(
                entityManager.getReference(
                        Pais.class,
                        normalizeText(request.getIdPais())));

        estado.setIdEstatus(idEstatus);

        entityManager.persist(estado);

        return Response.status(Response.Status.CREATED)
                .entity(estado)
                .build();
    }

    @PUT
    @Path("estados/{id}")
    @Transactional
    public Response actualizarEstado(
            @PathParam("id") String id,
            EstadoRequest request) {

        Estado estado = entityManager.find(
                Estado.class,
                id);

        if (estado == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        estado.setNombre(
                normalizeText(request.getNombre()));

        estado.setPais(
                entityManager.getReference(
                        Pais.class,
                        normalizeText(request.getIdPais())));

        estado.setIdEstatus(
                normalizeText(request.getIdEstatus()));

        return Response.ok(estado).build();
    }

    @DELETE
    @Path("estados/{id}")
    @Transactional
    public Response eliminarEstado(@PathParam("id") String id) {

        Estado estado = entityManager.find(
                Estado.class,
                id);

        if (estado == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        estado.setIdEstatus("INACTIVO");

        return Response.noContent().build();
    }
}
