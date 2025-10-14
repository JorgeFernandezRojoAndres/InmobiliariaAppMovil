package com.jorge.inmobiliaria2025.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * ✅ Modelo Propietario
 * Representa los datos del propietario autenticado.
 * Compatible con la API .NET 8 (PropietariosController) y serializable con Gson.
 */
public class Propietario implements Serializable {

    @SerializedName(value = "id", alternate = {"Id"})
    private int id;

    @SerializedName(value = "documento", alternate = {"Documento", "dni"})
    private String documento;

    @SerializedName(value = "nombre", alternate = {"Nombre"})
    private String nombre;

    @SerializedName(value = "apellido", alternate = {"Apellido"})
    private String apellido;

    @SerializedName(value = "email", alternate = {"Email"})
    private String email;

    @SerializedName(value = "clave", alternate = {"Clave", "password"})
    private String clave;

    @SerializedName(value = "telefono", alternate = {"Telefono"})
    private String telefono;

    @SerializedName(value = "avatarUrl", alternate = {"AvatarUrl"})
    private String avatarUrl;

    // 🔹 Constructores
    public Propietario() {}

    public Propietario(int id, String documento, String nombre, String apellido,
                       String email, String clave, String telefono) {
        this.id = id;
        this.documento = documento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.clave = clave;
        this.telefono = telefono;
    }

    // 🧩 Constructor extendido con avatar
    public Propietario(int id, String documento, String nombre, String apellido,
                       String email, String clave, String telefono, String avatarUrl) {
        this(id, documento, nombre, apellido, email, clave, telefono);
        this.avatarUrl = avatarUrl;
    }

    // 🔹 Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getClave() { return clave; }
    public void setClave(String clave) { this.clave = clave; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    // 🧠 Método de utilidad para mostrar nombre completo
    public String getNombreCompleto() {
        return (nombre != null ? nombre : "") + " " + (apellido != null ? apellido : "");
    }
}
