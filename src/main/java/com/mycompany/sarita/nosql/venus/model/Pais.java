package com.mycompany.sarita.nosql.venus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pais")
public class Pais {
    @Id
    @Column(name = "id_pais")
    private String idPais;
    private String nombre;
    @Column(name = "id_estatus")
    private String idEstatus;
    public String getIdPais() { return idPais; }
    public void setIdPais(String idPais) { this.idPais = idPais; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getIdEstatus() { return idEstatus; }
    public void setIdEstatus(String idEstatus) { this.idEstatus = idEstatus; }
}
