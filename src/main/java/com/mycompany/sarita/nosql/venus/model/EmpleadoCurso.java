package com.mycompany.sarita.nosql.venus.model;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name="EMPLEADO_CURSOS",schema="SARITA")
public class EmpleadoCurso {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="ID_EMPLEADO_CURSO") private Long idEmpleadoCurso;
    @ManyToOne @JoinColumn(name="ID_EMPLEADO",nullable=false) private Empleado empleado;
    @Column(name="NOMBRE_CURSO",nullable=false) private String nombreCurso;
    @Column(name="DESCRIPCION") private String descripcion;
    @Column(name="CONSTANCIA") private String constancia;
    @Column(name="FECHA_INICIO") private LocalDate fechaInicio;
    @Column(name="FECHA_FIN") private LocalDate fechaFin;
    public Long getIdEmpleadoCurso(){return idEmpleadoCurso;}
    public void setIdEmpleadoCurso(Long v){idEmpleadoCurso=v;}
    public Empleado getEmpleado(){return empleado;}
    public void setEmpleado(Empleado v){empleado=v;}
    public String getNombreCurso(){return nombreCurso;}
    public void setNombreCurso(String v){nombreCurso=v;}
    public String getDescripcion(){return descripcion;}
    public void setDescripcion(String v){descripcion=v;}
    public String getConstancia(){return constancia;}
    public void setConstancia(String v){constancia=v;}
    public LocalDate getFechaInicio(){return fechaInicio;}
    public void setFechaInicio(LocalDate v){fechaInicio=v;}
    public LocalDate getFechaFin(){return fechaFin;}
    public void setFechaFin(LocalDate v){fechaFin=v;}
}