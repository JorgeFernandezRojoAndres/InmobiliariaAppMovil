package com.jorge.inmobiliaria2025.model;

import com.google.gson.annotations.SerializedName;

public class InquilinoConInmueble {

    @SerializedName("inquilinoId") // asegura asignación correcta
    private int id;

    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String email;

    // Soporte API nueva
    @SerializedName("inmueble")
    private InmuebleData inmueble;

    // Campos previos (fallback si la API antigua los mandara)
    private String direccionInmueble;
    private String imagenUrlInmueble;

    // Clase interna para mapear el objeto inmueble
    public static class InmuebleData {
        private int id;
        private String direccion;

        @SerializedName("imagenUrl")
        private String imagenUrl;

        public int getId() { return id; }
        public String getDireccion() { return direccion; }
        public String getImagenUrl() { return imagenUrl; }
    }

    // Getters estándar
    public int getId() { return id; }

    public String getNombre() { return nombre; }

    public String getApellido() { return apellido; }

    public String getNombreCompleto() { return nombre + " " + apellido; }

    public String getDni() { return dni; }

    public String getTelefono() { return telefono; }

    public String getEmail() { return email; }

    // Dirección: primero intenta la nueva estructura, si no existe usa tu campo viejo
    public String getDireccionInmueble() {
        if (inmueble != null && inmueble.getDireccion() != null) {
            return inmueble.getDireccion();
        }
        return direccionInmueble;
    }

    // Imagen: igual estrategia
    public String getImagenUrlInmueble() {
        if (inmueble != null && inmueble.getImagenUrl() != null) {
            return inmueble.getImagenUrl();
        }
        return imagenUrlInmueble;
    }

    // Tu método viejo sigue andando
    public String getImagenUrl() {
        return getImagenUrlInmueble();
    }
}
