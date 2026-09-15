package com.mycompany.sarita.nosql.venus.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "departamento")
public class Departamento {
    @Id
    @Column(name = "id_depto")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDepto;
    private String descripcion;
    public Long getIdDepto() { return idDepto; }
    public void setIdDepto(Long idDepto) { this.idDepto = idDepto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}