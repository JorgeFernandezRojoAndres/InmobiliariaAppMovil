package com.jorge.inmobiliaria2025.model;

public class Contrato {
    private int id;
    private int idInquilino;
    private int idInmueble;
    private String fechaInicio;
    private String fechaFin;
    private double montoMensual;

    public Contrato() {}

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
}
