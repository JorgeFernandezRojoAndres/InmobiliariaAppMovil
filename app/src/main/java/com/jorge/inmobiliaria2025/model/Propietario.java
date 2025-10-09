package com.jorge.inmobiliaria2025.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * ✅ Modelo Propietario
 * Representa los datos del propietario autenticado.
 * Compatible con la API .NET 8 (PropietariosController) y serializable con Gson.
 */
public class Propietario implements Serializable {

    @SerializedName("Id")
    private int id;

    @SerializedName(value = "Documento", alternate = {"dni"})
    private String dni;

    @SerializedName("Nombre")
    private String nombre;

    @SerializedName("Apellido")
    private String apellido;

    @SerializedName("Email")
    private String email;

    @SerializedName(value = "Clave", alternate = {"password"})
    private String clave;

    // ⚙️ Corregido: única definición de teléfono, evita duplicación
    @SerializedName(value = "Telefono", alternate = {"telefono"})
    private String telefono;

    @SerializedName(value = "AvatarUrl", alternate = {"avatarUrl"})
    private String avatarUrl;

    // 🔹 Constructores
    public Propietario() {}

    public Propietario(int id, String dni, String nombre, String apellido,
                       String email, String clave, String telefono) {
        this.id = id;
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.clave = clave;
        this.telefono = telefono;
    }

    // 🧩 Constructor extendido con avatar
    public Propietario(int id, String dni, String nombre, String apellido,
                       String email, String clave, String telefono, String avatarUrl) {
        this(id, dni, nombre, apellido, email, clave, telefono);
        this.avatarUrl = avatarUrl;
    }

    // 🔹 Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDni() { return dni; }
    public void setDni(String dni) { this.dni = dni; }

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
