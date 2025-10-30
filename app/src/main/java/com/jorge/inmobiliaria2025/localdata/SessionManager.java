package com.jorge.inmobiliaria2025.localdata;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.jorge.inmobiliaria2025.InmobiliariaApp;
import com.jorge.inmobiliaria2025.model.Propietario;

/**
 * ✅ SessionManager
 * Maneja el almacenamiento persistente del token, email y datos del propietario logueado.
 * 100% estable — versión funcional previa.
 */
public class SessionManager {
    private static final String PREF_NAME = "inmobiliaria_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private static final String KEY_EMAIL = "usuario_email";
    private static final String KEY_PROPIETARIO = "propietario_data";
    private static final String KEY_AVATAR = "avatar_url";
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    // 🌐 IP base (modificable según red actual)
    private static final String BASE_URL = "http://192.168.1.37:5027/";

    // Instancia estática
    private static SessionManager instance;

    // Constructor privado
    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Método estático para obtener la instancia
    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
        Log.d("SessionManager", "✅ Token guardado correctamente: " + token);
    }

    public String getToken() {
        String token = prefs.getString(KEY_TOKEN, null);
        Log.d("SessionManager", "🔑 Token recuperado: " + token);
        return token;
    }

    public String obtenerToken() {
        return getToken();
    }

    // 📧 Email
    public void saveEmail(String email) {
        prefs.edit().putString(KEY_EMAIL, email).apply();
        InmobiliariaApp app = InmobiliariaApp.getInstance();
        if (app != null) app.guardarEmail(email);
        Log.d("SessionManager", "📩 Email guardado: " + email);
    }

    public String getEmail() {
        String email = prefs.getString(KEY_EMAIL, null);
        if (email == null) {
            InmobiliariaApp app = InmobiliariaApp.getInstance();
            if (app != null) email = app.obtenerEmail();
        }
        return email;
    }

    // 🧠 Verificar sesión activa
    public boolean isLogged() {
        String token = getToken();
        boolean isLoggedIn = token != null && !token.trim().isEmpty();
        Log.d("SessionManager", "¿Está logueado? " + isLoggedIn);
        return isLoggedIn;
    }

    // 🚪 Cerrar sesión
    public void logout() {
        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_EMAIL)
                .remove(KEY_PROPIETARIO)
                .remove(KEY_AVATAR)
                .apply();
        InmobiliariaApp app = InmobiliariaApp.getInstance();
        if (app != null) app.cerrarSesion();
        Log.d("SessionManager", "🚪 Sesión cerrada correctamente");
    }

    // 👤 Propietario
    public void guardarPropietario(Propietario propietario) {
        if (propietario == null) return;
        try {
            String json = gson.toJson(propietario);
            prefs.edit().putString(KEY_PROPIETARIO, json).apply();
            if (propietario.getAvatarUrl() != null) {
                prefs.edit().putString(KEY_AVATAR, propietario.getAvatarUrl()).apply();
            }
            Log.d("SessionManager", "👤 Propietario guardado: " + propietario.getNombreCompleto());
        } catch (Exception e) {
            Log.e("SessionManager", "⚠️ Error al guardar propietario: " + e.getMessage());
        }
    }

    public Propietario obtenerPropietarioActual() {
        String json = prefs.getString(KEY_PROPIETARIO, null);
        try {
            if (json != null && !json.trim().isEmpty()) {
                Propietario propietario = gson.fromJson(json, Propietario.class);
                if (propietario != null) {
                    String avatarExtra = prefs.getString(KEY_AVATAR, null);
                    if (avatarExtra != null && (propietario.getAvatarUrl() == null || propietario.getAvatarUrl().isEmpty())) {
                        propietario.setAvatarUrl(avatarExtra);
                    }
                    return propietario;
                }
            }
        } catch (Exception e) {
            Log.e("SessionManager", "⚠️ Error al leer propietario: " + e.getMessage());
        }

        Propietario empty = new Propietario();
        empty.setNombre("Sin nombre");
        empty.setApellido("");
        empty.setEmail(getEmail());
        return empty;
    }

    // 🖼️ Avatar
    public void guardarAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) return;
        Propietario propietario = obtenerPropietarioActual();
        propietario.setAvatarUrl(avatarUrl);
        guardarPropietario(propietario);
        prefs.edit().putString(KEY_AVATAR, avatarUrl).apply();
        Log.d("SessionManager", "🖼️ Avatar actualizado: " + avatarUrl);
    }

    public String obtenerAvatar() {
        String avatar = prefs.getString(KEY_AVATAR, null);
        if (avatar == null || avatar.isEmpty()) {
            Propietario propietario = obtenerPropietarioActual();
            if (propietario != null) avatar = propietario.getAvatarUrl();
        }
        return avatar;
    }

    // 💾 Guardar sesión completa
    public void guardarSesionCompleta(String token, Propietario propietario) {
        if (token != null && !token.trim().isEmpty()) saveToken(token);
        if (propietario != null) {
            guardarPropietario(propietario);
            saveEmail(propietario.getEmail());
        }
        Log.i("SessionManager", "✅ Sesión completa guardada correctamente");
    }

    // 🌐 URL completa del avatar
    public String getAvatarFullUrl() {
        String avatar = obtenerAvatar();
        if (avatar == null || avatar.isEmpty()) return null;
        if (avatar.startsWith("http")) return avatar;
        return BASE_URL + (avatar.startsWith("/") ? avatar : "/" + avatar);
    }

    public String getAvatarFullUrl(String relativeUrl) {
        if (relativeUrl == null || relativeUrl.isEmpty()) return "";
        if (relativeUrl.startsWith("http")) return relativeUrl;
        return BASE_URL + (relativeUrl.startsWith("/") ? relativeUrl : "/" + relativeUrl);
    }

    public Propietario getPropietario() {
        return obtenerPropietarioActual();
    }

    public void savePropietario(Propietario propietario) {
        guardarPropietario(propietario);
    }
}
