package com.jorge.inmobiliaria2025.ui.login;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.InmobiliariaApp;
import com.jorge.inmobiliaria2025.Retrofit.ApiService;
import com.jorge.inmobiliaria2025.Retrofit.RetrofitClient;
import com.jorge.inmobiliaria2025.localdata.SessionManager;
import com.jorge.inmobiliaria2025.model.LoginRequest;
import com.jorge.inmobiliaria2025.model.Propietario;
import com.jorge.inmobiliaria2025.model.TokenResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends AndroidViewModel {

    // =========================================================
    // 🔹 DEPENDENCIAS
    // =========================================================
    private final ApiService apiService;
    private final SessionManager sessionManager;

    // =========================================================
    // 🔹 LIVE DATA
    // =========================================================
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navegarMain = new MutableLiveData<>();
    private final MutableLiveData<Boolean> camposListos = new MutableLiveData<>();
    private final MutableLiveData<String> deepLinkToken = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application app) {
        super(app);
        apiService = RetrofitClient.getInstance(app).create(ApiService.class);
        sessionManager = SessionManager.getInstance(app);

        // Si el usuario ya tiene sesión guardada, ir directo al Main
        if (sessionManager.isLogged()) {
            Log.d("LOGIN", "🔁 Sesión existente detectada, saltando LoginActivity");
            navegarMain.setValue(true);
        }
    }

    // =========================================================
    // 🔹 LOGIN
    // =========================================================
    public void iniciarSesion(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            camposListos.setValue(true);
            return;
        }

        if (!email.contains("@")) {
            mensaje.setValue("Email inválido");
            return;
        }

        LoginRequest req = new LoginRequest(email, password);
        apiService.login(req).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    procesarLoginExitoso(email, response.body());
                } else {
                    manejarErrorLogin(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Log.e("LOGIN", "❌ Error de conexión: " + t.getMessage());
                mensaje.postValue("Error de red: " + t.getLocalizedMessage());
            }
        });
    }

    // =========================================================
    // 🔹 PROCESAMIENTO LOGIN
    // =========================================================
    private void procesarLoginExitoso(String email, TokenResponse tokenResponse) {
        String token = tokenResponse.getToken();
        Propietario propietario = tokenResponse.getPropietario();

        if (token == null || token.isEmpty()) {
            mensaje.postValue("Error: token vacío");
            return;
        }

        sessionManager.saveToken(token);
        RetrofitClient.reset();
        sessionManager.saveEmail(email);

        if (propietario != null) {
            sessionManager.guardarPropietario(propietario);
            Log.d("LOGIN", "👤 Propietario guardado: " + propietario.getNombreCompleto());
        } else {
            Log.w("LOGIN", "⚠️ Propietario nulo en respuesta");
            mensaje.postValue("Advertencia: no se recibieron datos del propietario");
        }

        InmobiliariaApp app = InmobiliariaApp.getInstance();
        if (app != null) app.guardarEmail(email);

        mensaje.postValue("Inicio de sesión exitoso");
        navegarMain.postValue(true);
    }

    private void manejarErrorLogin(int code, String msg) {
        String mensajeError = switch (code) {
            case 401 -> "Usuario o contraseña incorrectos";
            case 500 -> "Error interno del servidor";
            default -> "Credenciales inválidas";
        };
        mensaje.postValue("❌ " + mensajeError);
        Log.e("LOGIN", "Error HTTP " + code + " - " + msg);
    }

    // =========================================================
    // 🔹 OLVIDÉ MI CONTRASEÑA
    // =========================================================
    public void enviarRecuperacion(String email) {
        if (email == null || email.trim().isEmpty()) {
            mensaje.postValue("Debes ingresar un correo electrónico");
            return;
        }

        if (!email.contains("@")) {
            mensaje.postValue("Email inválido");
            return;
        }

        Log.d("RECUPERACION", "📧 Solicitando reset para: " + email);

        apiService.solicitarReset(email).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    mensaje.postValue("📬 Se envió un enlace a tu correo para restablecer la contraseña");
                    Log.d("RECUPERACION", "✅ Correo enviado correctamente");
                } else {
                    mensaje.postValue("⚠️ No se encontró una cuenta con ese correo");
                    Log.w("RECUPERACION", "Error HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                mensaje.postValue("❌ Error al enviar correo: " + t.getMessage());
                Log.e("RECUPERACION", "Fallo de red: " + t.getMessage());
            }
        });
    }

    // =========================================================
    // 🔹 DEEP LINK TOKEN (RESET PASSWORD)
    // =========================================================
    public void setDeepLinkToken(String token) {
        if (token != null && !token.trim().isEmpty()) {
            Log.d("DEEPLINK", "📩 Token recibido desde deep link: " + token);
            deepLinkToken.postValue(token);
        }
    }

    public LiveData<String> getDeepLinkToken() {
        return deepLinkToken;
    }

    // =========================================================
    // 🔹 OBSERVABLES
    // =========================================================
    public LiveData<String> getMensaje() { return mensaje; }
    public LiveData<Boolean> getNavegarMain() { return navegarMain; }
    public LiveData<Boolean> getCamposListos() { return camposListos; }
}
