package com.jorge.inmobiliaria2025.model;

import com.google.gson.annotations.SerializedName;

/**
 * ✅ TokenResponse
 * Coincide exactamente con la respuesta JSON del backend .NET
 */
public class TokenResponse {
    @SerializedName("token")
    private String token;

    @SerializedName(value = "Propietario", alternate = {"propietario"})
    private Propietario propietario;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Propietario getPropietario() {
        return propietario;
    }

    public void setPropietario(Propietario propietario) {
        this.propietario = propietario;
    }
}
