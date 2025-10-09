package com.jorge.inmobiliaria2025.model;

import com.google.gson.annotations.SerializedName;

/**
 * ✅ LoginRequest
 * Estructura enviada al backend .NET 8/9 para autenticación JWT.
 * Coincide exactamente con el modelo LoginView del backend:
 *  {
 *      "Email": "usuario@dominio.com",
 *      "Clave": "1234"
 *  }
 */
public class LoginRequest {

    // ⚙️ Mantiene mayúsculas exactas que el backend espera
    @SerializedName("Email")
    private String email;

    @SerializedName("Clave")
    private String clave;

    public LoginRequest(String email, String clave) {
        this.email = email;
        this.clave = clave;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }
}


