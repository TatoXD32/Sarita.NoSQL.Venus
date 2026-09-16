package com.mycompany.sarita.nosql.venus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "estado")
public class Estado {
    @Id
    @Column(name = "id_estado")
    private String idEstado;
    private String nombre;
    @ManyToOne
    @JoinColumn(name = "id_pais", nullable = false)
    private Pais pais;
    @Column(name = "id_estatus")
    private String idEstatus;
    public String getIdEstado() { return idEstado; }
    public void setIdEstado(String idEstado) { this.idEstado = idEstado; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Pais getPais() { return pais; }
    public void setPais(Pais pais) { this.pais = pais; }
    public String getIdEstatus() { return idEstatus; }
    public void setIdEstatus(String idEstatus) { this.idEstatus = idEstatus; }
}
