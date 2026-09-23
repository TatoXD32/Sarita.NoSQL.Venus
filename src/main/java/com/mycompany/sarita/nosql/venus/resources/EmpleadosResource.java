package com.mycompany.sarita.nosql.venus.resources;

import com.mycompany.sarita.nosql.venus.model.Empleado;
import com.mycompany.sarita.nosql.venus.service.EmpleadoRequest;
import com.mycompany.sarita.nosql.venus.service.EmpleadoService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("empleados")
@Produces("application/json; charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public class EmpleadosResource {

    @Inject
    private EmpleadoService service;

    @GET
    public List<Empleado> listar() {
        return service.listar();
    }

    @GET
    @Path("catalogos")
    public EmpleadoService.Catalogos catalogos() {
        return service.catalogos();
    }

    @POST
    public Response crear(EmpleadoRequest datos) {
        if (service.correoExiste(datos.getCorreoInstitucional(), null)) {
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("El correo institucional ya está registrado.").build();
        }
        return Response.status(Response.Status.CREATED).entity(service.crear(datos)).build();
    }

    @GET
    @Path("{id}")
    public Response buscar(@PathParam("id") Long id) {
        Empleado empleado = service.buscar(id);
        return empleado == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(empleado).build();
    }

    @PUT
    @Path("{id}")
    public Response actualizar(@PathParam("id") Long id, EmpleadoRequest datos) {
        if (service.correoExiste(datos.getCorreoInstitucional(), id)) {
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.TEXT_PLAIN)
                    .entity("El correo institucional ya está registrado por otro empleado.").build();
        }
        Empleado empleado = service.actualizar(id, datos);
        return empleado == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok(empleado).build();
    }

    @DELETE
    @Path("{id}")
    public Response darDeBaja(@PathParam("id") Long id) {
        return service.darDeBaja(id) ? Response.noContent().build() : Response.status(Response.Status.NOT_FOUND).build();
    }
}