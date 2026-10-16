package com.mycompany.sarita.nosql.venus.resources;

import com.mycompany.sarita.nosql.venus.model.*;
import com.mycompany.sarita.nosql.venus.service.*;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.*;

@Path("catalogos")
@Produces("application/json; charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public class CatalogosResource {
    @PersistenceContext(unitName = "my_persistence_unit") private EntityManager em;
    private String trim(String s) { return s == null ? null : s.trim(); }
    private String key(String s) { return s == null ? null : s.trim().toUpperCase(Locale.ROOT); }
    private Response missing() { return Response.status(Response.Status.NOT_FOUND).build(); }
    private Response created(Object item) { return Response.status(Response.Status.CREATED).entity(item).build(); }

    @GET @Path("estatus")
    public List<CatEstatus> estatus() { return em.createQuery("select e from CatEstatus e order by e.descripcion", CatEstatus.class).getResultList(); }
    @POST @Path("estatus") @Transactional
    public Response crearEstatus(EstatusRequest req) {
        if(req == null || req.getIdEstatus() == null || req.getDescripcion() == null) return Response.status(400).build();
        CatEstatus item = new CatEstatus(); item.setIdEstatus(key(req.getIdEstatus())); item.setDescripcion(trim(req.getDescripcion())); em.persist(item); em.flush(); return created(item);
    }
    @PUT @Path("estatus/{id}") @Transactional
    public Response actualizarEstatus(@PathParam("id") String id, EstatusRequest req) {
        CatEstatus item = em.find(CatEstatus.class, id); if(item == null) return missing();
        item.setDescripcion(trim(req.getDescripcion())); return Response.ok(item).build();
    }
    @GET @Path("departamentos")
    public List<Departamento> departamentos() { return em.createQuery("select d from Departamento d order by d.descripcion", Departamento.class).getResultList(); }
    @POST @Path("departamentos") @Transactional
    public Response crearDepartamento(DepartamentoRequest req) {
        Departamento item = new Departamento(); item.setDescripcion(trim(req.getDescripcion())); em.persist(item); em.flush(); return created(item);
    }
    @PUT @Path("departamentos/{id}") @Transactional
    public Response actualizarDepartamento(@PathParam("id") Long id, DepartamentoRequest req) {
        Departamento item = em.find(Departamento.class,id); if(item == null) return missing(); item.setDescripcion(trim(req.getDescripcion())); return Response.ok(item).build();
    }
    @DELETE @Path("departamentos/{id}") @Transactional
    public Response eliminarDepartamento(@PathParam("id") Long id) {
        Departamento item = em.find(Departamento.class,id); if(item == null) return missing(); em.remove(item); return Response.noContent().build();
    }
    @GET @Path("puestos")
    public List<Puesto> puestos() { return em.createQuery("select p from Puesto p order by p.descripcionPuesto", Puesto.class).getResultList(); }
    @POST @Path("puestos") @Transactional
    public Response crearPuesto(PuestoRequest req) {
        Puesto item = new Puesto(); item.setDescripcionPuesto(trim(req.getDescripcionPuesto())); em.persist(item); em.flush(); return created(item);
    }
    @PUT @Path("puestos/{id}") @Transactional
    public Response actualizarPuesto(@PathParam("id") Long id, PuestoRequest req) {
        Puesto item = em.find(Puesto.class,id); if(item == null) return missing(); item.setDescripcionPuesto(trim(req.getDescripcionPuesto())); return Response.ok(item).build();
    }
    @DELETE @Path("puestos/{id}") @Transactional
    public Response eliminarPuesto(@PathParam("id") Long id) {
        Puesto item = em.find(Puesto.class,id); if(item == null) return missing(); em.remove(item); return Response.noContent().build();
    }
    @GET @Path("sexos")
    public List<Sexo> sexos() { return em.createQuery("select s from Sexo s order by s.descripcion", Sexo.class).getResultList(); }
    @POST @Path("sexos") @Transactional
    public Response crearSexo(SexoRequest req) {
        Sexo item = new Sexo(); item.setIdSexo(key(req.getIdSexo())); item.setDescripcion(trim(req.getDescripcion())); em.persist(item); em.flush(); return created(item);
    }
    @PUT @Path("sexos/{id}") @Transactional
    public Response actualizarSexo(@PathParam("id") String id, SexoRequest req) {
        Sexo item = em.find(Sexo.class,id); if(item == null) return missing(); item.setDescripcion(trim(req.getDescripcion())); return Response.ok(item).build();
    }
    @DELETE @Path("sexos/{id}") @Transactional
    public Response eliminarSexo(@PathParam("id") String id) {
        Sexo item = em.find(Sexo.class,id); if(item == null) return missing(); em.remove(item); return Response.noContent().build();
    }
    @GET @Path("paises")
    public List<Pais> paises() { return em.createQuery("select p from Pais p order by p.nombre", Pais.class).getResultList(); }
    @POST @Path("paises") @Transactional
    public Response crearPais(PaisRequest req) {
        Pais item = new Pais(); item.setIdPais(key(req.getIdPais())); item.setNombre(trim(req.getNombre())); em.persist(item); em.flush(); return created(item);
    }
    @PUT @Path("paises/{id}") @Transactional
    public Response actualizarPais(@PathParam("id") String id, PaisRequest req) {
        Pais item = em.find(Pais.class,id); if(item == null) return missing(); item.setNombre(trim(req.getNombre())); return Response.ok(item).build();
    }
    @DELETE @Path("paises/{id}") @Transactional
    public Response eliminarPais(@PathParam("id") String id) {
        Pais item = em.find(Pais.class,id); if(item == null) return missing(); em.remove(item); return Response.noContent().build();
    }
    @GET @Path("estados")
    public List<Estado> estados() { return em.createQuery("select e from Estado e join fetch e.pais order by e.pais.nombre, e.nombre", Estado.class).getResultList(); }
    @POST @Path("estados") @Transactional
    public Response crearEstado(EstadoRequest req) {
        Estado item = new Estado(); item.setIdEstado(key(req.getIdEstado())); item.setNombre(trim(req.getNombre())); item.setPais(em.getReference(Pais.class,key(req.getIdPais()))); em.persist(item); em.flush(); return created(item);
    }
    @PUT @Path("estados/{id}") @Transactional
    public Response actualizarEstado(@PathParam("id") String id, EstadoRequest req) {
        Estado item = em.find(Estado.class,id); if(item == null) return missing(); item.setNombre(trim(req.getNombre())); item.setPais(em.getReference(Pais.class,key(req.getIdPais()))); return Response.ok(item).build();
    }
    @DELETE @Path("estados/{id}") @Transactional
    public Response eliminarEstado(@PathParam("id") String id) {
        Estado item = em.find(Estado.class,id); if(item == null) return missing(); em.remove(item); return Response.noContent().build();
    }
}