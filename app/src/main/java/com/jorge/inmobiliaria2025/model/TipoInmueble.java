package com.jorge.inmobiliaria2025.model;

import java.io.Serializable;

public class TipoInmueble implements Serializable {
    private int id;
    private String nombre;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    @Override
    public String toString() {
        return nombre; // el Spinner mostrará esto
    }
}
