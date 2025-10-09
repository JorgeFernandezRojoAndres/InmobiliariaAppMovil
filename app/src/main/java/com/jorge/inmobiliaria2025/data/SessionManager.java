package com.jorge.inmobiliaria2025.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.jorge.inmobiliaria2025.InmobiliariaApp;
import com.jorge.inmobiliaria2025.model.Propietario;

/**
 * ✅ SessionManager
 * Maneja el almacenamiento persistente del token, email y datos del propietario logueado.
 * Compatible con Gson y el modelo Propietario (incluyendo avatarUrl).
 */
public class SessionManager {
    private static final String PREF_NAME = "inmobiliaria_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_EMAIL = "usuario_email";
    private static final String KEY_PROPIETARIO = "propietario_data";
    private static final String KEY_AVATAR = "avatar_url"; // 🆕 clave explícita
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    // 🌐 IP base (modificable según red)
    private static final String BASE_URL = "http://192.168.1.33:5027";

    public SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // 🔐 Guardar token JWT
    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
        Log.d("SessionManager", "✅ Token guardado correctamente.");
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // ✅ Alias de compatibilidad
    public void saveAuthToken(String token) {
        saveToken(token);
    }

    public String obtenerToken() {
        return getToken();
    }

    // 📧 Guardar email y sincronizar con InmobiliariaApp
    public void saveEmail(String email) {
        prefs.edit().putString(KEY_EMAIL, email).apply();
        InmobiliariaApp app = InmobiliariaApp.getInstance();
        if (app != null) {
            app.guardarEmail(email);
        }
        Log.d("SessionManager", "📩 Email guardado: " + email);
    }

    public String getEmail() {
        String email = prefs.getString(KEY_EMAIL, null);
        if (email == null) {
            InmobiliariaApp app = InmobiliariaApp.getInstance();
            if (app != null) {
                email = app.obtenerEmail();
            }
        }
        return email;
    }

    // 🧠 Verificar sesión activa
    public boolean isLogged() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    // 🚪 Cerrar sesión completamente
    public void logout() {
        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_EMAIL)
                .remove(KEY_PROPIETARIO)
                .remove(KEY_AVATAR) // 🆕 limpiar URL del avatar también
                .apply();

        InmobiliariaApp app = InmobiliariaApp.getInstance();
        if (app != null) {
            app.cerrarSesion();
        }
        Log.d("SessionManager", "🚪 Sesión cerrada correctamente.");
    }

    // 🧩 Guardar objeto Propietario completo
    public void guardarPropietario(Propietario propietario) {
        if (propietario == null) return;
        try {
            String json = gson.toJson(propietario);
            prefs.edit().putString(KEY_PROPIETARIO, json).apply();
            if (propietario.getAvatarUrl() != null) {
                prefs.edit().putString(KEY_AVATAR, propietario.getAvatarUrl()).apply(); // 🆕 sincronizar campo separado
            }
            Log.d("SessionManager", "👤 Propietario guardado: " + propietario.getNombreCompleto());
        } catch (Exception e) {
            Log.e("SessionManager", "⚠️ Error al guardar propietario: " + e.getMessage());
        }
    }

    // 🧩 Obtener el propietario actual (seguro y tolerante a null)
    public Propietario obtenerPropietarioActual() {
        String json = prefs.getString(KEY_PROPIETARIO, null);
        try {
            if (json != null && !json.trim().isEmpty()) {
                Propietario propietario = gson.fromJson(json, Propietario.class);
                if (propietario != null) {
                    // 🆕 Asegura que el avatar se lea también desde la clave directa
                    String avatarExtra = prefs.getString(KEY_AVATAR, null);
                    if (avatarExtra != null && (propietario.getAvatarUrl() == null || propietario.getAvatarUrl().isEmpty())) {
                        propietario.setAvatarUrl(avatarExtra);
                    }
                    Log.d("SessionManager", "👤 Propietario cargado: " + propietario.getNombreCompleto());
                    return propietario;
                } else {
                    Log.w("SessionManager", "⚠️ JSON válido pero propietario es null");
                }
            } else {
                Log.w("SessionManager", "⚠️ No se encontró JSON guardado de propietario");
            }
        } catch (Exception e) {
            Log.e("SessionManager", "⚠️ Error al leer propietario: " + e.getMessage());
        }

        // 🔒 Retorna objeto vacío (nunca null)
        Propietario empty = new Propietario();
        empty.setNombre("Sin nombre");
        empty.setApellido("");
        empty.setEmail(getEmail());
        Log.w("SessionManager", "⚙️ Retornando propietario vacío para evitar crash");
        return empty;
    }

    // 🖼️ Guardar solo avatar del propietario (y sincronizar clave directa)
    public void guardarAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) return;

        Propietario propietario = obtenerPropietarioActual();
        if (propietario != null) {
            propietario.setAvatarUrl(avatarUrl);
            guardarPropietario(propietario);
        }

        prefs.edit().putString(KEY_AVATAR, avatarUrl).apply(); // ✅ persistente inmediato
        Log.d("SessionManager", "🖼️ Avatar actualizado: " + avatarUrl);
    }

    // 🖼️ Obtener solo la URL relativa del avatar guardado
    public String obtenerAvatar() {
        String avatar = prefs.getString(KEY_AVATAR, null);
        if (avatar == null || avatar.isEmpty()) {
            Propietario propietario = obtenerPropietarioActual();
            if (propietario != null) avatar = propietario.getAvatarUrl();
        }
        return avatar;
    }

    // 💾 Guardar login completo
    public void guardarSesionCompleta(String token, Propietario propietario) {
        if (token != null && !token.trim().isEmpty()) {
            saveToken(token);
        }
        if (propietario != null) {
            guardarPropietario(propietario);
            saveEmail(propietario.getEmail());
        }
        Log.i("SessionManager", "✅ Sesión completa guardada correctamente.");
    }

    // 🆕 Sincronizar avatar desde servidor tras actualización
    public void actualizarAvatarDesdeServidor(String nuevaUrl) {
        if (nuevaUrl == null || nuevaUrl.isEmpty()) {
            Log.w("SessionManager", "⚠️ No se recibió URL válida para avatar");
            return;
        }
        guardarAvatar(nuevaUrl);
        Log.i("SessionManager", "🌐 Avatar sincronizado con backend: " + nuevaUrl);
    }

    // 🆕 Obtener URL completa del avatar (útil para Glide)
    public String getAvatarFullUrl() {
        String avatar = obtenerAvatar();
        if (avatar == null || avatar.isEmpty()) {
            return null;
        }
        if (avatar.startsWith("http")) return avatar; // ya es absoluta
        return BASE_URL + avatar;
    }

    // 🧩 Alias de compatibilidad para fragmentos antiguos
    public Propietario getPropietario() {
        return obtenerPropietarioActual();
    }

    public void savePropietario(Propietario propietario) {
        guardarPropietario(propietario);
    }
}
