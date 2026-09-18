package com.mycompany.sarita.nosql.venus.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

@Entity
@Table(name = "puesto")
public class Puesto {
    @Id
    @Column(name = "id_puesto")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPuesto;
    @Column(name = "descripcion_puesto")
    private String descripcionPuesto;
    @Column(name = "id_estatus")
    private String idEstatus;
    public Long getIdPuesto() { return idPuesto; }
    public void setIdPuesto(Long idPuesto) { this.idPuesto = idPuesto; }
    public String getDescripcionPuesto() { return descripcionPuesto; }
    public void setDescripcionPuesto(String descripcionPuesto) { this.descripcionPuesto = descripcionPuesto; }
    public String getIdEstatus() { return idEstatus; }
    public void setIdEstatus(String idEstatus) { this.idEstatus = idEstatus; }
}