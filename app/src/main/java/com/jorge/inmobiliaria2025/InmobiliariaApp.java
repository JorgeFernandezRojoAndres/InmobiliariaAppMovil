package com.jorge.inmobiliaria2025;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * ✅ InmobiliariaApp
 * Punto de entrada global estable.
 * Proporciona:
 *  - Contexto global de aplicación.
 *  - Preferencias persistentes.
 *  - Métodos para guardar/cerrar sesión.
 */
public class InmobiliariaApp extends Application {

    private static InmobiliariaApp instance;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = getSharedPreferences("inmobiliaria_prefs", Context.MODE_PRIVATE);
    }

    // 🔹 Devuelve la instancia global
    public static InmobiliariaApp getInstance() {
        return instance;
    }

    // 🔹 Devuelve el contexto global
    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    // 🔹 Preferencias compartidas
    public SharedPreferences getPrefs() {
        return prefs;
    }

    // ✅ Guarda el email del usuario logueado
    public void guardarEmail(String email) {
        prefs.edit().putString("usuario_email", email).apply();
    }

    // ✅ Devuelve el email guardado, o null si no hay sesión
    public String obtenerEmail() {
        return prefs.getString("usuario_email", null);
    }

    // ✅ Cierra sesión borrando solo el usuario (sin eliminar otras preferencias)
    public void cerrarSesion() {
        prefs.edit().remove("usuario_email").apply();
    }
}
