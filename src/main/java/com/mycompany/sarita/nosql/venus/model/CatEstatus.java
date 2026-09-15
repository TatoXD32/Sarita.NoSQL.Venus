package com.mycompany.sarita.nosql.venus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cat_estatus")
public class CatEstatus {
    @Id
    @Column(name = "id_estatus")
    private String idEstatus;
    private String descripcion;
    private String categoria;
    public String getIdEstatus() { return idEstatus; }
    public void setIdEstatus(String idEstatus) { this.idEstatus = idEstatus; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}