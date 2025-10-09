package com.jorge.inmobiliaria2025.model;

public class Pago {
    private int id;
    private int idContrato;
    private String fechaPago;
    private double importe;
    private int numeroPago;

    public Pago() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdContrato() { return idContrato; }
    public void setIdContrato(int idContrato) { this.idContrato = idContrato; }

    public String getFechaPago() { return fechaPago; }
    public void setFechaPago(String fechaPago) { this.fechaPago = fechaPago; }

    public double getImporte() { return importe; }
    public void setImporte(double importe) { this.importe = importe; }

    public int getNumeroPago() { return numeroPago; }
    public void setNumeroPago(int numeroPago) { this.numeroPago = numeroPago; }
}
