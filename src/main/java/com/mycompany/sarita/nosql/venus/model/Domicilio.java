package com.mycompany.sarita.nosql.venus.model;
import jakarta.persistence.*;
import jakarta.json.bind.annotation.JsonbTransient;
@Entity
@Table(name="DOMICILIOS", schema="SARITA")
public class Domicilio {
    @Id @Column(name="ID_EMPLEADO") private Long idEmpleado;
    @OneToOne @MapsId @JoinColumn(name="ID_EMPLEADO") private Empleado empleado;
    @Column(name="CALLE") private String calle;
    @Column(name="CODIGO_POSTAL") private String codigoPostal;
    @Column(name="NUMERO_EXTERIOR") private String numeroExterior;
    @Column(name="NUMERO_INTERIOR") private String numeroInterior;
    @ManyToOne @JoinColumn(name="ID_ESTADO", nullable=false) private Estado estado;
    public Long getIdEmpleado(){return idEmpleado;}
    public void setIdEmpleado(Long v){idEmpleado=v;}
@JsonbTransient
public Empleado getEmpleado() {
    return empleado;
}
    public void setEmpleado(Empleado v){empleado=v;}
    public String getCalle(){return calle;}
    public void setCalle(String v){calle=v;}
    public String getCodigoPostal(){return codigoPostal;}
    public void setCodigoPostal(String v){codigoPostal=v;}
    public String getNumeroExterior(){return numeroExterior;}
    public void setNumeroExterior(String v){numeroExterior=v;}
    public String getNumeroInterior(){return numeroInterior;}
    public void setNumeroInterior(String v){numeroInterior=v;}
    public Estado getEstado(){return estado;}
    public void setEstado(Estado v){estado=v;}
}
