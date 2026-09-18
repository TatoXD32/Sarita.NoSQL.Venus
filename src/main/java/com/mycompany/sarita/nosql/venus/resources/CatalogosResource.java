package com.mycompany.sarita.nosql.venus.resources;

import com.mycompany.sarita.nosql.venus.model.CatEstatus;
import com.mycompany.sarita.nosql.venus.model.Curso;
import com.mycompany.sarita.nosql.venus.model.Estado;
import com.mycompany.sarita.nosql.venus.model.Departamento;
import com.mycompany.sarita.nosql.venus.model.Pais;
import com.mycompany.sarita.nosql.venus.model.Puesto;
import com.mycompany.sarita.nosql.venus.service.CursoRequest;
import com.mycompany.sarita.nosql.venus.service.DepartamentoRequest;
import com.mycompany.sarita.nosql.venus.service.EstadoRequest;
import com.mycompany.sarita.nosql.venus.service.EstatusRequest;
import com.mycompany.sarita.nosql.venus.service.PaisRequest;
import com.mycompany.sarita.nosql.venus.service.PuestoRequest;
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

@Path("catalogos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CatalogosResource {
    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager entityManager;

    @GET
    @Path("estatus")
    public List<CatEstatus> estatus(@QueryParam("categoria") String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return entityManager.createQuery("select e from CatEstatus e order by e.categoria, e.descripcion", CatEstatus.class).getResultList();
        }
        return entityManager.createQuery("select e from CatEstatus e where e.categoria = :categoria order by e.descripcion", CatEstatus.class)
                .setParameter("categoria", categoria.toUpperCase()).getResultList();
    }

    @POST
    @Path("estatus")
    @Transactional
    public Response crearEstatus(EstatusRequest request) {
        entityManager.createNativeQuery("insert into cat_estatus (id_estatus, descripcion, categoria) values (?, ?, ?)")
                .setParameter(1, request.getIdEstatus())
                .setParameter(2, request.getDescripcion())
                .setParameter(3, request.getCategoria())
                .executeUpdate();
        CatEstatus estatus = entityManager.find(CatEstatus.class, request.getIdEstatus());
        return Response.status(Response.Status.CREATED).entity(estatus).build();
    }

    @PUT
    @Path("estatus/{id}")
    @Transactional
    public Response actualizarEstatus(@PathParam("id") String id, EstatusRequest request) {
        CatEstatus estatus = entityManager.find(CatEstatus.class, id);
        if (estatus == null) return Response.status(Response.Status.NOT_FOUND).build();
        estatus.setDescripcion(request.getDescripcion());
        estatus.setCategoria(request.getCategoria());
        return Response.ok(estatus).build();
    }

    @GET
    @Path("cursos")
    public List<Curso> cursos() {
        return entityManager.createQuery("select c from Curso c order by c.nombre", Curso.class).getResultList();
    }

    @POST
    @Path("cursos")
    @Transactional
    public Response crearCurso(CursoRequest request) {
        entityManager.createNativeQuery("insert into curso (nombre, descripcion, id_estatus) values (?, ?, ?)")
            .setParameter(1, request.getNombre())
            .setParameter(2, request.getDescripcion())
            .setParameter(3, request.getIdEstatus())
            .executeUpdate();
        Curso curso = entityManager.createQuery("select c from Curso c where c.nombre = :nombre", Curso.class)
            .setParameter("nombre", request.getNombre()).getSingleResult();
        return Response.status(Response.Status.CREATED).entity(curso).build();
    }

    @PUT
    @Path("cursos/{id}")
    @Transactional
    public Response actualizarCurso(@PathParam("id") Long id, CursoRequest request) {
        Curso curso = entityManager.find(Curso.class, id);
        if (curso == null) return Response.status(Response.Status.NOT_FOUND).build();
        curso.setNombre(request.getNombre());
        curso.setDescripcion(request.getDescripcion());
        curso.setIdEstatus(request.getIdEstatus());
        return Response.ok(curso).build();
    }

    @DELETE
    @Path("cursos/{id}")
    @Transactional
    public Response eliminarCurso(@PathParam("id") Long id) {
        Curso curso = entityManager.find(Curso.class, id);
        if (curso == null) return Response.status(Response.Status.NOT_FOUND).build();
        curso.setIdEstatus("INACTIVO");
        return Response.noContent().build();
    }

    @GET
    @Path("departamentos")
    public List<Departamento> departamentos() {
        return entityManager.createQuery("select d from Departamento d order by d.descripcion", Departamento.class).getResultList();
    }

    @POST
    @Path("departamentos")
    @Transactional
    public Response crearDepartamento(DepartamentoRequest request) {
        entityManager.createNativeQuery("insert into departamento (descripcion, id_estatus) values (?, ?)")
            .setParameter(1, request.getDescripcion())
            .setParameter(2, request.getIdEstatus())
            .executeUpdate();
        Departamento departamento = entityManager.createQuery("select d from Departamento d where d.descripcion = :descripcion", Departamento.class)
            .setParameter("descripcion", request.getDescripcion()).getSingleResult();
        return Response.status(Response.Status.CREATED).entity(departamento).build();
    }

    @PUT
    @Path("departamentos/{id}")
    @Transactional
    public Response actualizarDepartamento(@PathParam("id") Long id, DepartamentoRequest request) {
        Departamento departamento = entityManager.find(Departamento.class, id);
        if (departamento == null) return Response.status(Response.Status.NOT_FOUND).build();
        departamento.setDescripcion(request.getDescripcion());
        departamento.setIdEstatus(request.getIdEstatus());
        return Response.ok(departamento).build();
    }

    @DELETE
    @Path("departamentos/{id}")
    @Transactional
    public Response eliminarDepartamento(@PathParam("id") Long id) {
        Departamento departamento = entityManager.find(Departamento.class, id);
        if (departamento == null) return Response.status(Response.Status.NOT_FOUND).build();
        departamento.setIdEstatus("INACTIVO");
        return Response.noContent().build();
    }

    @GET
    @Path("puestos")
    public List<Puesto> puestos() {
        return entityManager.createQuery("select p from Puesto p order by p.descripcionPuesto", Puesto.class).getResultList();
    }

    @POST
    @Path("puestos")
    @Transactional
    public Response crearPuesto(PuestoRequest request) {
        entityManager.createNativeQuery("insert into puesto (descripcion_puesto, id_estatus) values (?, ?)")
            .setParameter(1, request.getDescripcionPuesto())
            .setParameter(2, request.getIdEstatus())
            .executeUpdate();
        Puesto puesto = entityManager.createQuery("select p from Puesto p where p.descripcionPuesto = :descripcion", Puesto.class)
            .setParameter("descripcion", request.getDescripcionPuesto()).getSingleResult();
        return Response.status(Response.Status.CREATED).entity(puesto).build();
    }

    @PUT
    @Path("puestos/{id}")
    @Transactional
    public Response actualizarPuesto(@PathParam("id") Long id, PuestoRequest request) {
        Puesto puesto = entityManager.find(Puesto.class, id);
        if (puesto == null) return Response.status(Response.Status.NOT_FOUND).build();
        puesto.setDescripcionPuesto(request.getDescripcionPuesto());
        puesto.setIdEstatus(request.getIdEstatus());
        return Response.ok(puesto).build();
    }

    @DELETE
    @Path("puestos/{id}")
    @Transactional
    public Response eliminarPuesto(@PathParam("id") Long id) {
        Puesto puesto = entityManager.find(Puesto.class, id);
        if (puesto == null) return Response.status(Response.Status.NOT_FOUND).build();
        puesto.setIdEstatus("INACTIVO");
        return Response.noContent().build();
    }

    @GET
    @Path("paises")
    public List<Pais> paises() {
        return entityManager.createQuery("select p from Pais p order by p.nombre", Pais.class).getResultList();
    }

    @POST
    @Path("paises")
    @Transactional
    public Response crearPais(PaisRequest request) {
        Pais pais = new Pais();
        pais.setIdPais(request.getIdPais());
        pais.setNombre(request.getNombre());
        pais.setIdEstatus(request.getIdEstatus());
        entityManager.persist(pais);
        return Response.status(Response.Status.CREATED).entity(pais).build();
    }

    @PUT
    @Path("paises/{id}")
    @Transactional
    public Response actualizarPais(@PathParam("id") String id, PaisRequest request) {
        Pais pais = entityManager.find(Pais.class, id);
        if (pais == null) return Response.status(Response.Status.NOT_FOUND).build();
        pais.setNombre(request.getNombre());
        pais.setIdEstatus(request.getIdEstatus());
        return Response.ok(pais).build();
    }

    @DELETE
    @Path("paises/{id}")
    @Transactional
    public Response eliminarPais(@PathParam("id") String id) {
        Pais pais = entityManager.find(Pais.class, id);
        if (pais == null) return Response.status(Response.Status.NOT_FOUND).build();
        pais.setIdEstatus("INACTIVO");
        return Response.noContent().build();
    }

    @GET
    @Path("estados")
    public List<Estado> estados() {
        return entityManager.createQuery("select e from Estado e join fetch e.pais order by e.pais.nombre, e.nombre", Estado.class).getResultList();
    }

    @POST
    @Path("estados")
    @Transactional
    public Response crearEstado(EstadoRequest request) {
        Estado estado = new Estado();
        estado.setIdEstado(request.getIdEstado());
        estado.setNombre(request.getNombre());
        estado.setPais(entityManager.getReference(Pais.class, request.getIdPais()));
        estado.setIdEstatus(request.getIdEstatus());
        entityManager.persist(estado);
        return Response.status(Response.Status.CREATED).entity(estado).build();
    }

    @PUT
    @Path("estados/{id}")
    @Transactional
    public Response actualizarEstado(@PathParam("id") String id, EstadoRequest request) {
        Estado estado = entityManager.find(Estado.class, id);
        if (estado == null) return Response.status(Response.Status.NOT_FOUND).build();
        estado.setNombre(request.getNombre());
        estado.setPais(entityManager.getReference(Pais.class, request.getIdPais()));
        estado.setIdEstatus(request.getIdEstatus());
        return Response.ok(estado).build();
    }

    @DELETE
    @Path("estados/{id}")
    @Transactional
    public Response eliminarEstado(@PathParam("id") String id) {
        Estado estado = entityManager.find(Estado.class, id);
        if (estado == null) return Response.status(Response.Status.NOT_FOUND).build();
        estado.setIdEstatus("INACTIVO");
        return Response.noContent().build();
    }
}
