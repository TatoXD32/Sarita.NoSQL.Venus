package com.mycompany.sarita.nosql.venus.model;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name="EMPLEADOS", schema="SARITA")
public class Empleado {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="ID_EMPLEADO") private Long idEmpleado;
    @Column(name="NOMBRE") private String nombre;
    @Column(name="APELLIDO_PATERNO") private String apellidoPaterno;
    @Column(name="APELLIDO_MATERNO") private String apellidoMaterno;
    @Column(name="FECHA_NACIMIENTO") private LocalDate fechaNacimiento;
    @Column(name="CORREO_INSTITUCIONAL") private String correoInstitucional;
    @Column(name="TELEFONO_CELULAR") private String telefonoCelular;
    @Column(name="FECHA_INGRESO") private LocalDate fechaIngreso;
    @ManyToOne @JoinColumn(name="ID_SEXO",nullable=false) private Sexo sexo;
    @ManyToOne @JoinColumn(name="ID_DEPTO",nullable=false) private Departamento departamento;
    @OneToOne(mappedBy="empleado") private Domicilio domicilio;
    @ManyToOne @JoinColumn(name="ID_PUESTO",nullable=false) private Puesto puesto;
    @ManyToOne @JoinColumn(name="ID_ESTATUS",nullable=false) private CatEstatus estatus;
    public Long getIdEmpleado(){return idEmpleado;}
    public void setIdEmpleado(Long v){idEmpleado=v;}
    public String getNombre(){return nombre;}
    public void setNombre(String v){nombre=v;}
    public String getApellidoPaterno(){return apellidoPaterno;}
    public void setApellidoPaterno(String v){apellidoPaterno=v;}
    public String getApellidoMaterno(){return apellidoMaterno;}
    public void setApellidoMaterno(String v){apellidoMaterno=v;}
    public LocalDate getFechaNacimiento(){return fechaNacimiento;}
    public void setFechaNacimiento(LocalDate v){fechaNacimiento=v;}
    public String getCorreoInstitucional(){return correoInstitucional;}
    public void setCorreoInstitucional(String v){correoInstitucional=v;}
    public String getTelefonoCelular(){return telefonoCelular;}
    public void setTelefonoCelular(String v){telefonoCelular=v;}
    public LocalDate getFechaIngreso(){return fechaIngreso;}
    public void setFechaIngreso(LocalDate v){fechaIngreso=v;}
    public Sexo getSexo(){return sexo;}
    public void setSexo(Sexo v){sexo=v;}
    public Departamento getDepartamento(){return departamento;}
    public void setDepartamento(Departamento v){departamento=v;}
    public Domicilio getDomicilio(){return domicilio;}
    public void setDomicilio(Domicilio v){domicilio=v;if(v!=null)v.setEmpleado(this);}
    public Puesto getPuesto(){return puesto;}
    public void setPuesto(Puesto v){puesto=v;}
    public CatEstatus getEstatus(){return estatus;}
    public void setEstatus(CatEstatus v){estatus=v;}
}