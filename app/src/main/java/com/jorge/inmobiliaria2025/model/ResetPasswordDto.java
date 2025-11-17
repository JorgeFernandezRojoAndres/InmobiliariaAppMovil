package com.jorge.inmobiliaria2025.model;

public class ResetPasswordDto {
    private String email;
    private String token;
    private String nuevaClave;

    public ResetPasswordDto(String email, String token, String nuevaClave) {
        this.email = email;
        this.token = token;
        this.nuevaClave = nuevaClave;
    }

    public String getEmail() { return email; }
    public String getToken() { return token; }
    public String getNuevaClave() { return nuevaClave; }
}
