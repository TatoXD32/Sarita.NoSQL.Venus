package com.mycompany.sarita.nosql.venus.service;

import java.time.LocalDate;

public class EmpleadoRequest {
    private String nombre;
    private String apellidoPaterno;
    private String apellidoMaterno;
    private LocalDate fechaNacimiento;
    private String correoInstitucional;
    private String telefonoCelular;
    private String idSexo;
    private Long idDepto;
    private Long idPuesto;

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
    public String getIdSexo() { return idSexo; }
    public void setIdSexo(String idSexo) { this.idSexo = idSexo; }
    public Long getIdDepto() { return idDepto; }
    public void setIdDepto(Long idDepto) { this.idDepto = idDepto; }
    public Long getIdPuesto() { return idPuesto; }
    public void setIdPuesto(Long idPuesto) { this.idPuesto = idPuesto; }
}