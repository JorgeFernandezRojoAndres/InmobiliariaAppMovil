package com.jorge.inmobiliaria2025.model;

import java.io.Serializable;
import java.util.Objects; // Importar Objects

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

    // 🔴 Importante: Sobrescribir equals() para comparar por ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true; // Si es la misma instancia, son iguales
        if (o == null || getClass() != o.getClass()) return false; // Si nulo o de otra clase, no son iguales
        TipoInmueble that = (TipoInmueble) o; // Convertir a TipoInmueble
        return id == that.id; // La igualdad se basa SOLAMENTE en el ID
    }

    // 🔴 Importante: Sobrescribir hashCode() para ser consistente con equals()
    @Override
    public int hashCode() {
        return Objects.hash(id); // Generar hash basado SOLAMENTE en el ID
    }
}
