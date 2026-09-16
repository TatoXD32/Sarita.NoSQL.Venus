package com.mycompany.sarita.nosql.venus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "empleado")
public class Empleado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empleado")
    private Long idEmpleado;
    private String nombre;
    @Column(name = "apellido_paterno")
    private String apellidoPaterno;
    @Column(name = "apellido_materno")
    private String apellidoMaterno;
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
    @Column(name = "correo_institucional")
    private String correoInstitucional;
    @Column(name = "telefono_celular")
    private String telefonoCelular;
    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;
    @ManyToOne
    @JoinColumn(name = "id_sexo", nullable = false)
    private Sexo sexo;
    @ManyToOne
    @JoinColumn(name = "id_depto", nullable = false)
    private Departamento departamento;
    @ManyToOne
    @JoinColumn(name = "id_domicilio", nullable = false, unique = true)
    private Domicilio domicilio;
    @ManyToOne
    @JoinColumn(name = "id_puesto", nullable = false)
    private Puesto puesto;
    @ManyToOne
    @JoinColumn(name = "id_estatus", nullable = false)
    private CatEstatus estatus;
    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }
    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getCorreoInstitucional() { return correoInstitucional; }
    public void setCorreoInstitucional(String correoInstitucional) { this.correoInstitucional = correoInstitucional; }
    public String getTelefonoCelular() { return telefonoCelular; }
    public void setTelefonoCelular(String telefonoCelular) { this.telefonoCelular = telefonoCelular; }
    public LocalDate getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDate fechaIngreso) { this.fechaIngreso = fechaIngreso; }
    public Sexo getSexo() { return sexo; }
    public void setSexo(Sexo sexo) { this.sexo = sexo; }
    public Departamento getDepartamento() { return departamento; }
    public void setDepartamento(Departamento departamento) { this.departamento = departamento; }
    public Domicilio getDomicilio() { return domicilio; }
    public void setDomicilio(Domicilio domicilio) { this.domicilio = domicilio; }
    public Puesto getPuesto() { return puesto; }
    public void setPuesto(Puesto puesto) { this.puesto = puesto; }
    public CatEstatus getEstatus() { return estatus; }
    public void setEstatus(CatEstatus estatus) { this.estatus = estatus; }
}