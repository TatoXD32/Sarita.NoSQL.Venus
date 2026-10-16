package com.mycompany.sarita.nosql.venus.model;
import jakarta.persistence.*;
@Entity
@Table(name="ESTATUS_EMPLEADOS", schema="SARITA")
public class CatEstatus {
    @Id @Column(name="ID_ESTATUS") private String idEstatus;
    @Column(name="DESCRIPCION") private String descripcion;
    public String getIdEstatus(){return idEstatus;}
    public void setIdEstatus(String value){idEstatus=value;}
    public String getDescripcion(){return descripcion;}
    public void setDescripcion(String value){descripcion=value;}
}