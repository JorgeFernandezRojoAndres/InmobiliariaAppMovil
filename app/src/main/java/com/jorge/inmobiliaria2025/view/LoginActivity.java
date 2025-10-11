package com.jorge.inmobiliaria2025.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jorge.inmobiliaria2025.InmobiliariaApp;
import com.jorge.inmobiliaria2025.MainActivity;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.data.SessionManager;
import com.jorge.inmobiliaria2025.data.network.ApiService;
import com.jorge.inmobiliaria2025.data.network.RetrofitClient;
import com.jorge.inmobiliaria2025.model.LoginRequest;
import com.jorge.inmobiliaria2025.model.TokenResponse;
import com.jorge.inmobiliaria2025.model.Propietario;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ✅ LoginActivity
 * Pantalla de inicio de sesión que autentica contra el backend .NET
 * y almacena el token JWT y datos del propietario de forma persistente.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // ✅ Inicializar Retrofit y SessionManager
        apiService = RetrofitClient.getInstance(this).create(ApiService.class);
        sessionManager = new SessionManager(this);

        // 🚀 Si ya hay token guardado, ir directo al MainActivity
        if (sessionManager.isLogged()) {
            Log.d("LOGIN", "🔁 Sesión existente detectada, saltando LoginActivity");
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
            return;
        }

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.contains("@")) {
                Toast.makeText(this, "Email inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            // 🔐 Enviar login al backend
            LoginRequest loginRequest = new LoginRequest(email, password);
            Log.d("LOGIN", "➡️ Enviando solicitud de login para: " + email);

            apiService.login(loginRequest).enqueue(new Callback<TokenResponse>() {
                @Override
                public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        TokenResponse tokenResponse = response.body();
                        String token = tokenResponse.getToken();
                        Propietario propietario = tokenResponse.getPropietario();

                        if (token != null && !token.isEmpty()) {

                            // ⚙️ Guardar token y propietario (si existe)
                            sessionManager.saveToken(token);
                            sessionManager.saveEmail(email);

                            if (propietario != null) {
                                sessionManager.guardarPropietario(propietario);
                                Log.d("LOGIN", "👤 Propietario guardado: " + propietario.getNombreCompleto());
                            } else {
                                Log.w("LOGIN", "⚠️ Propietario nulo en respuesta");
                                Toast.makeText(LoginActivity.this,
                                        "Advertencia: no se recibieron datos del propietario",
                                        Toast.LENGTH_LONG).show();
                            }

                            // 🔄 Sincronizar con InmobiliariaApp
                            InmobiliariaApp app = InmobiliariaApp.getInstance();
                            if (app != null) {
                                app.guardarEmail(email);
                            }

                            Log.d("LOGIN", "✅ Token guardado correctamente");
                            Toast.makeText(LoginActivity.this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show();

                            // 🚀 Ir al menú principal
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();

                        } else {
                            Log.e("LOGIN", "❌ Token vacío recibido del servidor");
                            Toast.makeText(LoginActivity.this, "Error: token vacío", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        int code = response.code();
                        String msg = "Credenciales inválidas";
                        if (code == 401) msg = "Usuario o contraseña incorrectos";
                        if (code == 500) msg = "Error interno del servidor";

                        Toast.makeText(LoginActivity.this, "❌ " + msg, Toast.LENGTH_SHORT).show();
                        Log.e("LOGIN", "Error HTTP " + code + " - " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<TokenResponse> call, Throwable t) {
                    Log.e("LOGIN", "❌ Error de conexión: " + t.getMessage());
                    Toast.makeText(LoginActivity.this,
                            "Error de red: " + t.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}