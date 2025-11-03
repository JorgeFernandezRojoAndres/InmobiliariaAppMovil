package com.jorge.inmobiliaria2025.ui.contratos;

public class DialogoUI {

    public enum Tipo { CONFIRMACION, INFORMACION, ERROR }

    private final Tipo tipo;
    private final String titulo;
    private final String mensaje;
    private final String textoPositivo;
    private final String textoNegativo;
    private final String accionAsociada;

    public DialogoUI(Tipo tipo, String titulo, String mensaje,
                     String textoPositivo, String textoNegativo, String accionAsociada) {
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensaje = mensaje;
        this.textoPositivo = textoPositivo;
        this.textoNegativo = textoNegativo;
        this.accionAsociada = accionAsociada;
    }

    public Tipo getTipo() { return tipo; }
    public String getTitulo() { return titulo; }
    public String getMensaje() { return mensaje; }
    public String getTextoPositivo() { return textoPositivo; }
    public String getTextoNegativo() { return textoNegativo; }
    public String getAccionAsociada() { return accionAsociada; }
}
