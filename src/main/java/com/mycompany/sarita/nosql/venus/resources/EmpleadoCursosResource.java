package com.mycompany.sarita.nosql.venus.resources;

import com.mycompany.sarita.nosql.venus.model.Empleado;
import com.mycompany.sarita.nosql.venus.model.EmpleadoCurso;
import com.mycompany.sarita.nosql.venus.service.EmpleadoCursoRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.sql.Date;

@Path("empleado-cursos")
@Produces("application/json; charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public class EmpleadoCursosResource {
    @PersistenceContext(unitName = "my_persistence_unit")
    private EntityManager entityManager;

    @GET
    public List<Map<String, Object>> listar() {
        return entityManager.createQuery(
                "select ec from EmpleadoCurso ec join fetch ec.empleado order by ec.fechaInicio desc",
                EmpleadoCurso.class).getResultList().stream().map(this::respuesta).toList();
    }

    @GET
    @Path("empleado/{id}")
    public List<Map<String, Object>> porEmpleado(@PathParam("id") Long id) {
        return entityManager.createQuery(
                "select ec from EmpleadoCurso ec join fetch ec.empleado where ec.empleado.idEmpleado = :id order by ec.fechaInicio desc",
                EmpleadoCurso.class).setParameter("id", id).getResultList().stream().map(this::respuesta).toList();
    }

    // Respuesta plana: evita la referencia circular EmpleadoCurso -> Empleado -> ...
    private Map<String, Object> respuesta(EmpleadoCurso ec) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("idEmpleadoCurso", ec.getIdEmpleadoCurso());
        datos.put("nombreCurso", ec.getNombreCurso());
        datos.put("descripcion", ec.getDescripcion());
        datos.put("constancia", ec.getConstancia());
        datos.put("fechaInicio", ec.getFechaInicio());
        datos.put("fechaFin", ec.getFechaFin());
        Empleado empleado = ec.getEmpleado();
        if (empleado != null) {
            Map<String, Object> empleadoDatos = new LinkedHashMap<>();
            empleadoDatos.put("idEmpleado", empleado.getIdEmpleado());
            empleadoDatos.put("nombre", empleado.getNombre());
            empleadoDatos.put("apellidoPaterno", empleado.getApellidoPaterno());
            empleadoDatos.put("apellidoMaterno", empleado.getApellidoMaterno());
            datos.put("empleado", empleadoDatos);
        }
        return datos;
    }

    private String limpiar(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private Response validar(EmpleadoCursoRequest request) {
        if (request == null || request.getIdEmpleado() == null
                || request.getNombreCurso() == null || request.getNombreCurso().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("idEmpleado y nombreCurso son obligatorios").build();
        }
        if (request.getFechaInicio() != null && request.getFechaFin() != null
                && request.getFechaFin().isBefore(request.getFechaInicio())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("fechaFin no puede ser anterior a fechaInicio").build();
        }
        if (entityManager.find(Empleado.class, request.getIdEmpleado()) == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("El empleado no existe").build();
        }
        return null;
    }

    private void aplicar(EmpleadoCurso ec, EmpleadoCursoRequest request) {
        ec.setEmpleado(entityManager.getReference(Empleado.class, request.getIdEmpleado()));
        ec.setNombreCurso(limpiar(request.getNombreCurso()));
        ec.setDescripcion(limpiar(request.getDescripcion()));
        ec.setConstancia(request.getConstancia());
        ec.setFechaInicio(request.getFechaInicio() == null ? java.time.LocalDate.now() : request.getFechaInicio());
        ec.setFechaFin(request.getFechaFin());
    }

   @POST
@Transactional
public Response asignar(EmpleadoCursoRequest request) {
    Response error = validar(request);
    if (error != null) return error;

    java.time.LocalDate inicio = request.getFechaInicio() == null
            ? java.time.LocalDate.now()
            : request.getFechaInicio();

    Object resultado = entityManager.createNativeQuery("""
        INSERT INTO SARITA.EMPLEADO_CURSOS
        (ID_EMPLEADO, NOMBRE_CURSO, DESCRIPCION,
         CONSTANCIA, FECHA_INICIO, FECHA_FIN)
        VALUES (?, ?, ?, ?, ?, ?)
        """)
        .setParameter(1, request.getIdEmpleado())
        .setParameter(2, limpiar(request.getNombreCurso()))
        .setParameter(3, limpiar(request.getDescripcion()))
        .setParameter(4, request.getConstancia())
        .setParameter(5, Date.valueOf(inicio))
        .setParameter(6, request.getFechaFin() == null
                ? null : Date.valueOf(request.getFechaFin()))
        .executeUpdate();

    return Response.status(Response.Status.CREATED)
            .entity(Map.of(
                    "mensaje", "Curso registrado correctamente",
                    "registrosInsertados", resultado
            ))
            .build();
}
    @PUT
    @Path("{id}")
    @Transactional
    public Response actualizar(@PathParam("id") Long id, EmpleadoCursoRequest request) {
        EmpleadoCurso ec = entityManager.find(EmpleadoCurso.class, id);
        if (ec == null) return Response.status(Response.Status.NOT_FOUND).build();
        Response error = validar(request);
        if (error != null) return error;
        aplicar(ec, request);
        return Response.ok(respuesta(ec)).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response eliminar(@PathParam("id") Long id) {
        EmpleadoCurso ec = entityManager.find(EmpleadoCurso.class, id);
        if (ec == null) return Response.status(Response.Status.NOT_FOUND).build();
        entityManager.remove(ec);
        return Response.noContent().build();
    }
}