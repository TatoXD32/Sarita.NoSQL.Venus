package com.mycompany.sarita.nosql.venus.service;

import com.mycompany.sarita.nosql.venus.model.CatEstatus;
import com.mycompany.sarita.nosql.venus.model.Departamento;
import com.mycompany.sarita.nosql.venus.model.Empleado;
import com.mycompany.sarita.nosql.venus.model.Puesto;
import com.mycompany.sarita.nosql.venus.model.Sexo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.List;

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

    @Transactional
    public Empleado actualizar(Long id, EmpleadoRequest datos) {
        Empleado empleado = buscar(id);
        if (empleado == null) {
            return null;
        }
        empleado.setNombre(datos.getNombre());
        empleado.setApellidoPaterno(datos.getApellidoPaterno());
        empleado.setApellidoMaterno(datos.getApellidoMaterno());
        empleado.setFechaNacimiento(datos.getFechaNacimiento());
        empleado.setCorreoInstitucional(datos.getCorreoInstitucional());
        empleado.setTelefonoCelular(datos.getTelefonoCelular());
        empleado.setSexo(entityManager.getReference(Sexo.class, datos.getIdSexo()));
        empleado.setDepartamento(entityManager.getReference(Departamento.class, datos.getIdDepto()));
        empleado.setPuesto(entityManager.getReference(Puesto.class, datos.getIdPuesto()));
        return empleado;
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
                entityManager.createQuery("select d from Departamento d order by d.descripcion", Departamento.class).getResultList(),
                entityManager.createQuery("select p from Puesto p order by p.descripcionPuesto", Puesto.class).getResultList(),
                entityManager.createQuery("select s from Sexo s order by s.descripcion", Sexo.class).getResultList());
    }

    public record Catalogos(List<Departamento> departamentos, List<Puesto> puestos, List<Sexo> sexos) { }
}