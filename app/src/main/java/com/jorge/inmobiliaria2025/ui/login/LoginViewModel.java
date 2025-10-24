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

    private final ApiService apiService;
    private final SessionManager sessionManager;

    private final MutableLiveData<String> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navegarMain = new MutableLiveData<>();
    private final MutableLiveData<Boolean> camposListos = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application app) {
        super(app);
        apiService = RetrofitClient.getInstance(app).create(ApiService.class);
        sessionManager = new SessionManager(app);

        if (sessionManager.isLogged()) {
            Log.d("LOGIN", "🔁 Sesión existente detectada, saltando LoginActivity");
            navegarMain.setValue(true);
        }
    }

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
                    TokenResponse tokenResponse = response.body();
                    String token = tokenResponse.getToken();
                    Propietario propietario = tokenResponse.getPropietario();

                    if (token != null && !token.isEmpty()) {
                        sessionManager.saveToken(token);
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
                    } else {
                        mensaje.postValue("Error: token vacío");
                    }
                } else {
                    int code = response.code();
                    String msg = switch (code) {
                        case 401 -> "Usuario o contraseña incorrectos";
                        case 500 -> "Error interno del servidor";
                        default -> "Credenciales inválidas";
                    };
                    mensaje.postValue("❌ " + msg);
                    Log.e("LOGIN", "Error HTTP " + code + " - " + response.message());
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                Log.e("LOGIN", "❌ Error de conexión: " + t.getMessage());
                mensaje.postValue("Error de red: " + t.getLocalizedMessage());
            }
        });
    }

    public LiveData<String> getMensaje() { return mensaje; }
    public LiveData<Boolean> getNavegarMain() { return navegarMain; }
    public LiveData<Boolean> getCamposListos() { return camposListos; }
}
