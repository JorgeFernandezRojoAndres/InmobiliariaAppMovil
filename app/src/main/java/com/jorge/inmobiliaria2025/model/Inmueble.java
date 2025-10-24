package com.jorge.inmobiliaria2025.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;
import androidx.annotation.NonNull;
import androidx.room.Ignore;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(tableName = "inmueble")
public class Inmueble implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "direccion")
    private String direccion;

    @ColumnInfo(name = "precio")
    private double precio;

    // 🔹 Permite null si viene así de la API
    @ColumnInfo(name = "disponible")
    private Boolean disponible;

    @SerializedName("tipoId")
    @ColumnInfo(name = "tipo_id")
    private int tipoId;

    @ColumnInfo(name = "tipo_nombre")
    private String tipoNombre;

    @ColumnInfo(name = "metros_cuadrados")
    private int metrosCuadrados;

    @ColumnInfo(name = "propietario_id")
    private int propietarioId;

    @SerializedName("activo")
    @ColumnInfo(name = "activo")
    private Boolean activo;

    @ColumnInfo(name = "nombre_propietario")
    private String nombrePropietario;

    @ColumnInfo(name = "imagen_url")
    private String imagenUrl;

    // 🧩 Constructor vacío requerido por Room
    public Inmueble() {}

    // ✅ Constructor básico
    @Ignore
    public Inmueble(String direccion, double precio, boolean disponible) {
        this.direccion = direccion;
        this.precio = precio;
        this.disponible = disponible;
        this.activo = disponible;
    }

    // ✅ Constructor completo
    @Ignore
    public Inmueble(int id, String direccion, double precio, boolean disponible) {
        this.id = id;
        this.direccion = direccion;
        this.precio = precio;
        this.disponible = disponible;
        this.activo = disponible;
    }

    // ========================
    // 🔹 Getters y Setters
    // ========================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    // ✅ Ahora devuelve Boolean (coherente con el tipo del campo)
    public Boolean isDisponible() {
        if (disponible != null) return disponible;
        return activo != null ? activo : false;
    }
    @Ignore
    public boolean isActivo() {
        return getActivo();
    }


    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
        this.activo = disponible;
    }

    public int getTipoId() { return tipoId; }
    public void setTipoId(int tipoId) { this.tipoId = tipoId; }

    public String getTipoNombre() { return tipoNombre; }
    public void setTipoNombre(String tipoNombre) { this.tipoNombre = tipoNombre; }

    public int getMetrosCuadrados() { return metrosCuadrados; }
    public void setMetrosCuadrados(int metrosCuadrados) { this.metrosCuadrados = metrosCuadrados; }

    public int getPropietarioId() { return propietarioId; }
    public void setPropietarioId(int propietarioId) { this.propietarioId = propietarioId; }

    // ✅ También devuelve Boolean para eliminar warning
    public Boolean getActivo() {
        if (activo != null) return activo;
        return disponible != null ? disponible : false;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
        this.disponible = activo;
    }

    public String getNombrePropietario() { return nombrePropietario; }
    public void setNombrePropietario(String nombrePropietario) { this.nombrePropietario = nombrePropietario; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    // 🧠 Log útil
    @NonNull
    @Override
    public String toString() {
        return "Inmueble{" +
                "id=" + id +
                ", direccion='" + direccion + '\'' +
                ", precio=" + precio +
                ", disponible=" + disponible +
                ", tipoId=" + tipoId +
                ", tipoNombre='" + tipoNombre + '\'' +
                ", metrosCuadrados=" + metrosCuadrados +
                ", propietarioId=" + propietarioId +
                ", activo=" + activo +
                ", nombrePropietario='" + nombrePropietario + '\'' +
                ", imagenUrl='" + imagenUrl + '\'' +
                '}';
    }
}
