package com.jorge.inmobiliaria2025.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

import java.io.Serializable;

@Entity(tableName = "inmueble")
public class Inmueble implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "direccion")
    private String direccion;

    @ColumnInfo(name = "precio")
    private double precio;

    @ColumnInfo(name = "disponible")
    private boolean disponible;

    // 🧩 Constructor vacío requerido por Room
    public Inmueble() {}

    // ✅ Constructor sin ID (para crear nuevos registros)
    @Ignore
    public Inmueble(String direccion, double precio, boolean disponible) {
        this.direccion = direccion;
        this.precio = precio;
        this.disponible = disponible;
    }

    // ✅ Constructor con ID (Room usará este)
    @Ignore
    public Inmueble(int id, String direccion, double precio, boolean disponible) {
        this.id = id;
        this.direccion = direccion;
        this.precio = precio;
        this.disponible = disponible;
    }

    // Getters y setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    // 🧠 Método útil para depuración y logs
    @Override
    public String toString() {
        return "Inmueble{" +
                "id=" + id +
                ", direccion='" + direccion + '\'' +
                ", precio=" + precio +
                ", disponible=" + disponible +
                '}';
    }
}
