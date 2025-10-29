package com.jorge.inmobiliaria2025.model;

import java.io.Serializable;

public class Contrato implements Serializable {
    private int id;
    private int idInquilino;
    private int idInmueble;
    private String fechaInicio;
    private String fechaFin;
    private double montoMensual;
    private String estado;
    private Inmueble inmueble;

    public Contrato() {}

    // === Getters y Setters ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdInquilino() { return idInquilino; }
    public void setIdInquilino(int idInquilino) { this.idInquilino = idInquilino; }

    public int getIdInmueble() { return idInmueble; }
    public void setIdInmueble(int idInmueble) { this.idInmueble = idInmueble; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }

    public double getMontoMensual() { return montoMensual; }
    public void setMontoMensual(double montoMensual) { this.montoMensual = montoMensual; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Inmueble getInmueble() { return inmueble; }
    public void setInmueble(Inmueble inmueble) { this.inmueble = inmueble; }

    // === Helper opcional para mostrar texto amigable ===
    public String getResumen() {
        String dir = (inmueble != null && inmueble.getDireccion() != null)
                ? inmueble.getDireccion() : "(sin dirección)";
        return "Inmueble: " + dir + " | Estado: " + (estado != null ? estado : "N/A");
    }
}
