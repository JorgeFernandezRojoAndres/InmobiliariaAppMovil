package com.jorge.inmobiliaria2025.model;

public class CambioClaveDto {
    private String ClaveActual;
    private String NuevaClave;

    public CambioClaveDto(String claveActual, String nuevaClave) {
        this.ClaveActual = claveActual;
        this.NuevaClave = nuevaClave;
    }

    public String getClaveActual() {
        return ClaveActual;
    }

    public String getNuevaClave() {
        return NuevaClave;
    }
}
