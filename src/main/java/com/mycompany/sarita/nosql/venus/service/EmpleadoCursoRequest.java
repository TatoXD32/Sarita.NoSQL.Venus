package com.mycompany.sarita.nosql.venus.service;

import java.time.LocalDate;

public class EmpleadoCursoRequest {
    private Long idEmpleado;
    private String nombreCurso;
    private String descripcion;
    private String constancia;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    public Long getIdEmpleado() { return idEmpleado; }
    public void setIdEmpleado(Long idEmpleado) { this.idEmpleado = idEmpleado; }
    public String getNombreCurso() { return nombreCurso; }
    public void setNombreCurso(String nombreCurso) { this.nombreCurso = nombreCurso; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getConstancia() { return constancia; }
    public void setConstancia(String constancia) { this.constancia = constancia; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }
}