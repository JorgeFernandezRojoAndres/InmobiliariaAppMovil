package com.jorge.inmobiliaria2025.ui.login;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.Retrofit.ApiService;
import com.jorge.inmobiliaria2025.Retrofit.RetrofitClient;
import com.jorge.inmobiliaria2025.model.ResetPasswordDto;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecuperarPasswordViewModel extends AndroidViewModel {

    private final ApiService api;
    private final MutableLiveData<String> mensaje = new MutableLiveData<>();
    private final MutableLiveData<Boolean> resetExitoso = new MutableLiveData<>();

    public RecuperarPasswordViewModel(@NonNull Application application) {
        super(application);
        api = RetrofitClient.getInstance(application).create(ApiService.class);
    }

    // =========================================================
    // 🔹 MÉTODO PRINCIPAL LLAMADO DESDE EL FRAGMENT
    // =========================================================
    public void onClickGuardar(String token, String clave1, String clave2) {
        if (!validarCampos(token, clave1, clave2)) return;

        ResetPasswordDto dto = new ResetPasswordDto("", token, clave1);
        enviarPeticionReset(dto);
    }

    // =========================================================
    // 🔹 VALIDACIONES INTERNAS
    // =========================================================
    private boolean validarCampos(String token, String clave1, String clave2) {
        if (token == null || token.trim().isEmpty()) {
            mensaje.postValue("⚠️ Token inválido o ausente. Intentá desde el enlace correcto.");
            return false;
        }
        if (clave1.trim().isEmpty() || clave2.trim().isEmpty()) {
            mensaje.postValue("⚠️ Completá todos los campos.");
            return false;
        }
        if (!clave1.equals(clave2)) {
            mensaje.postValue("⚠️ Las contraseñas no coinciden.");
            return false;
        }
        if (clave1.length() < 6) {
            mensaje.postValue("⚠️ La contraseña debe tener al menos 6 caracteres.");
            return false;
        }
        return true;
    }

    // =========================================================
    // 🔹 PETICIÓN RETROFIT
    // =========================================================
    private void enviarPeticionReset(ResetPasswordDto dto) {
        Log.d("RESET", "📩 Enviando solicitud de restablecimiento para token: " + dto.getToken());

        api.resetPassword(dto).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    mensaje.postValue("✅ Contraseña restablecida correctamente.");
                    resetExitoso.postValue(true);
                } else {
                    Log.w("RESET", "⚠️ Error HTTP " + response.code());
                    mensaje.postValue("❌ No se pudo restablecer la contraseña.");
                    resetExitoso.postValue(false);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("RESET", "❌ Error de conexión: " + t.getMessage());
                mensaje.postValue("⚠️ Error de red: " + t.getLocalizedMessage());
                resetExitoso.postValue(false);
            }
        });
    }

    // =========================================================
    // 🔹 OBSERVABLES
    // =========================================================
    public LiveData<String> getMensaje() {
        return mensaje;
    }

    public LiveData<Boolean> getResetExitoso() {
        return resetExitoso;
    }
}
