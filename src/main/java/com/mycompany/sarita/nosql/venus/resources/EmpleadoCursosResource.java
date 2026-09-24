package com.mycompany.sarita.nosql.venus.resources;

import com.mycompany.sarita.nosql.venus.model.Curso;
import com.mycompany.sarita.nosql.venus.model.Empleado;
import com.mycompany.sarita.nosql.venus.model.EmpleadoCurso;
import com.mycompany.sarita.nosql.venus.service.EmpleadoCursoRequest;
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
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("empleado-cursos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmpleadoCursosResource {
    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager entityManager;

    @GET
    public List<EmpleadoCurso> listar() {
        return entityManager.createQuery(
                "select ec from EmpleadoCurso ec join fetch ec.empleado join fetch ec.curso order by ec.fechaInicio desc",
                EmpleadoCurso.class).getResultList();
    }

    @GET
    @Path("empleado/{id}")
    public List<EmpleadoCurso> porEmpleado(@PathParam("id") Long id) {
        return entityManager.createQuery(
                "select ec from EmpleadoCurso ec join fetch ec.empleado join fetch ec.curso where ec.empleado.idEmpleado = :id order by ec.fechaInicio desc",
                EmpleadoCurso.class)
                .setParameter("id", id).getResultList();
    }

    @POST
    @Transactional
    public Response asignar(EmpleadoCursoRequest request) {
        entityManager.createNativeQuery(
                "insert into empleado_curso (id_empleado, id_curso, fecha_inicio, fecha_fin) values (?, ?, ?, ?)")
                .setParameter(1, request.getIdEmpleado())
                .setParameter(2, request.getIdCurso())
                .setParameter(3, request.getFechaInicio())
                .setParameter(4, request.getFechaFin())
                .executeUpdate();
        EmpleadoCurso asignacion = entityManager.createQuery(
                "select ec from EmpleadoCurso ec where ec.empleado.idEmpleado = :empleado and ec.curso.idCurso = :curso order by ec.idEmpleadoCurso desc",
                EmpleadoCurso.class)
                .setParameter("empleado", request.getIdEmpleado())
                .setParameter("curso", request.getIdCurso())
                .setMaxResults(1)
                .getSingleResult();
        return Response.status(Response.Status.CREATED).entity(asignacion).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    public Response actualizar(@PathParam("id") Long id, EmpleadoCursoRequest request) {
        EmpleadoCurso asignacion = entityManager.find(EmpleadoCurso.class, id);
        if (asignacion == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        asignacion.setEmpleado(entityManager.getReference(Empleado.class, request.getIdEmpleado()));
        asignacion.setCurso(entityManager.getReference(Curso.class, request.getIdCurso()));
        asignacion.setFechaInicio(request.getFechaInicio());
        asignacion.setFechaFin(request.getFechaFin());
        return Response.ok(asignacion).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response eliminar(@PathParam("id") Long id) {
        EmpleadoCurso asignacion = entityManager.find(EmpleadoCurso.class, id);
        if (asignacion == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        entityManager.remove(asignacion);
        return Response.noContent().build();
    }
}
