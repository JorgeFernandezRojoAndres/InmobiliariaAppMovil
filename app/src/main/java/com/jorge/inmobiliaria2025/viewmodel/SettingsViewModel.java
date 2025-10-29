package com.jorge.inmobiliaria2025.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class SettingsViewModel extends AndroidViewModel {

    private final MutableLiveData<String> mensaje = new MutableLiveData<>();
    private final MutableLiveData<String> baseUrlActual = new MutableLiveData<>();

    // 🌍 URL base fija (según tu red actual)
    private static final String BASE_URL_FIJA = "http://192.168.1.37:5027/";

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        baseUrlActual.setValue(BASE_URL_FIJA);
    }

    public LiveData<String> getMensaje() {
        return mensaje;
    }

    public LiveData<String> getBaseUrlActual() {
        return baseUrlActual;
    }

    // 🔹 Validación y simulación de guardado (sin modificar App)
    public void guardarUrl(String nuevaUrl) {
        if (TextUtils.isEmpty(nuevaUrl)) {
            mensaje.setValue("⚠️ Ingrese una URL válida.");
            return;
        }

        if (!nuevaUrl.startsWith("http")) {
            mensaje.setValue("⚠️ La URL debe comenzar con http o https.");
            return;
        }

        if (!nuevaUrl.endsWith("/")) {
            nuevaUrl += "/";
        }

        // Solo actualiza LiveData, no modifica RetrofitClient ni App
        baseUrlActual.setValue(nuevaUrl);
        mensaje.setValue("🌍 URL actualizada correctamente:\n" + nuevaUrl);
    }

    public void restablecerUrlPorDefecto() {
        String defecto = BASE_URL_FIJA;
        baseUrlActual.setValue(defecto);
        mensaje.setValue("🔄 URL restablecida a la predeterminada:\n" + defecto);
    }
}
