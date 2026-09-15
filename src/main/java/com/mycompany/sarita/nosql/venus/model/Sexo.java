package com.mycompany.sarita.nosql.venus.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "sexo")
public class Sexo {
    @Id
    @Column(name = "id_sexo")
    private String idSexo;
    private String descripcion;
    public String getIdSexo() { return idSexo; }
    public void setIdSexo(String idSexo) { this.idSexo = idSexo; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}