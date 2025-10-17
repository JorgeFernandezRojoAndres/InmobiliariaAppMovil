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

    // 🆕 Nuevos campos compatibles con la API .NET
    @ColumnInfo(name = "tipo_id")
    private int tipoId;

    @ColumnInfo(name = "tipo_nombre")
    private String tipoNombre;

    @ColumnInfo(name = "metros_cuadrados")
    private int metrosCuadrados;

    @ColumnInfo(name = "propietario_id")
    private int propietarioId;

    @ColumnInfo(name = "activo")
    private boolean activo;

    @ColumnInfo(name = "nombre_propietario")
    private String nombrePropietario;

    @ColumnInfo(name = "imagen_url")
    private String imagenUrl;

    // 🧩 Constructor vacío requerido por Room
    public Inmueble() {}

    // ✅ Constructor básico (se mantiene igual)
    @Ignore
    public Inmueble(String direccion, double precio, boolean disponible) {
        this.direccion = direccion;
        this.precio = precio;
        this.disponible = disponible;
        this.activo = disponible; // sincroniza ambos flags
    }

    // ✅ Constructor completo (opcional)
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

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
        this.activo = disponible; // sincroniza con backend
    }

    public int getTipoId() { return tipoId; }
    public void setTipoId(int tipoId) { this.tipoId = tipoId; }

    public String getTipoNombre() { return tipoNombre; }
    public void setTipoNombre(String tipoNombre) { this.tipoNombre = tipoNombre; }

    public int getMetrosCuadrados() { return metrosCuadrados; }
    public void setMetrosCuadrados(int metrosCuadrados) { this.metrosCuadrados = metrosCuadrados; }

    public int getPropietarioId() { return propietarioId; }
    public void setPropietarioId(int propietarioId) { this.propietarioId = propietarioId; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public String getNombrePropietario() { return nombrePropietario; }
    public void setNombrePropietario(String nombrePropietario) { this.nombrePropietario = nombrePropietario; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    // 🧠 Método útil para depuración y logs
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
