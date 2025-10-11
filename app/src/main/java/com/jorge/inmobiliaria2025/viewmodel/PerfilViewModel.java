package com.jorge.inmobiliaria2025.viewmodel;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.InmobiliariaApp;
import com.jorge.inmobiliaria2025.data.SessionManager;
import com.jorge.inmobiliaria2025.data.network.ApiService;
import com.jorge.inmobiliaria2025.data.network.RetrofitClient;
import com.jorge.inmobiliaria2025.model.Propietario;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilViewModel extends AndroidViewModel {

    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cerrarSesionEvento = new MutableLiveData<>();
    private final MutableLiveData<Propietario> propietario = new MutableLiveData<>(new Propietario());
    private final MutableLiveData<String> avatarUrl = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> abrirGaleriaEvento = new MutableLiveData<>();
    private final MutableLiveData<Boolean> modoEdicion = new MutableLiveData<>(false);
    private final MutableLiveData<String> mensajeEvento = new MutableLiveData<>();

    // 🆕 Evento para notificar a la Activity que actualice el header
    private final MutableLiveData<Propietario> eventoActualizarHeader = new MutableLiveData<>();

    // 🆕 Nuevos eventos sin if
    private final MutableLiveData<Void> activarEdicionEvento = new MutableLiveData<>();
    private final MutableLiveData<Void> guardarCambiosEvento = new MutableLiveData<>();

    private final SessionManager sessionManager;

    public PerfilViewModel(@NonNull Application app) {
        super(app);
        sessionManager = new SessionManager(app);
        cargarEmail();
        cargarPropietario();
    }

    // -------------------- LiveData públicos --------------------
    public LiveData<String> getEmail() { return email; }
    public LiveData<Boolean> getCerrarSesionEvento() { return cerrarSesionEvento; }
    public LiveData<Propietario> getPropietario() { return propietario; }
    public LiveData<String> getAvatarUrl() { return avatarUrl; }
    public LiveData<Boolean> getAbrirGaleriaEvento() { return abrirGaleriaEvento; }
    public LiveData<Boolean> getModoEdicion() { return modoEdicion; }
    public LiveData<String> getMensajeEvento() { return mensajeEvento; }
    public LiveData<Propietario> getEventoActualizarHeader() { return eventoActualizarHeader; }
    public LiveData<Void> getActivarEdicionEvento() { return activarEdicionEvento; }
    public LiveData<Void> getGuardarCambiosEvento() { return guardarCambiosEvento; }

    // -------------------- Lógica principal --------------------

    public void cargarEmail() {
        String guardado = InmobiliariaApp.getInstance().obtenerEmail();
        email.setValue(guardado != null ? guardado : "No hay sesión activa");
    }

    public void cerrarSesion(Context context) {
        sessionManager.logout();
        InmobiliariaApp.getInstance().cerrarSesion();
        email.setValue("Sesión cerrada");
        cerrarSesionEvento.setValue(Boolean.TRUE);
    }

    public void cargarPropietario() {
        Propietario p = sessionManager.obtenerPropietarioActual();
        if (p == null) {
            propietario.setValue(new Propietario());
            avatarUrl.setValue("");
        } else {
            propietario.setValue(p);
            avatarUrl.setValue(sessionManager.getAvatarFullUrl());
        }
    }

    // 🧩 Centraliza la actualización del propietario y emite evento para el header
    public void actualizarPropietario(Propietario p) {
        try {
            sessionManager.guardarPropietario(p);
            propietario.setValue(p);
            email.setValue(p.getEmail());
            avatarUrl.setValue(sessionManager.getAvatarFullUrl());

            // 🔔 Notificar actualización del header en la Activity
            eventoActualizarHeader.setValue(p);

            emitirMensaje("✅ Datos actualizados correctamente");
        } catch (Exception e) {
            emitirMensaje("⚠️ No se pudieron guardar los cambios");
            Log.e("PerfilViewModel", "Error al actualizar propietario: " + e.getMessage());
        }
    }

    // -------------------- Modo edición --------------------

    public void alternarModoEdicion() {
        boolean nuevoEstado = !Boolean.TRUE.equals(modoEdicion.getValue());
        modoEdicion.setValue(nuevoEstado);

        // 🔁 En lugar de if/else en el Fragment, emitimos eventos separados
        if (nuevoEstado) {
            activarEdicionEvento.setValue(null);
        } else {
            guardarCambiosEvento.setValue(null);
        }
    }

    public void setModoEdicion(boolean activo) {
        modoEdicion.setValue(activo);
    }

    // -------------------- Avatar --------------------

    public void onAvatarClick(boolean estaEnModoEdicion) {
        abrirGaleriaEvento.setValue(estaEnModoEdicion);
    }

    public void emitirMensaje(String msg) {
        mensajeEvento.setValue(msg);
    }

    // -------------------- Procesar resultado del selector --------------------

    public void procesarResultadoImagen(Uri uri, Context context) {
        if (uri == null) {
            emitirMensaje("⚠️ No se seleccionó ninguna imagen");
            return;
        }
        subirAvatar(uri, context);
    }

    // -------------------- Subir Avatar --------------------

    public void subirAvatar(@NonNull Uri imageUri, @NonNull Context context) {
        try (InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = inputStream.read(data)) != -1) {
                buffer.write(data, 0, nRead);
            }

            byte[] bytes = buffer.toByteArray();
            enviarAvatarAlServidor(bytes, context);

        } catch (Exception e) {
            Log.e("PerfilViewModel", "Error leyendo imagen: " + e.getMessage());
            emitirMensaje("⚠️ No se pudo leer la imagen seleccionada");
        }
    }

    private void enviarAvatarAlServidor(byte[] bytes, Context context) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), bytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("archivo", "avatar.jpg", requestFile);

        ApiService api = RetrofitClient.getInstance(context).create(ApiService.class);
        Call<ResponseBody> call = api.subirAvatar(body);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (!response.isSuccessful() || response.body() == null) {
                        emitirMensaje("❌ Error al subir imagen (" + response.code() + ")");
                        return;
                    }

                    String responseStr = response.body().string();
                    JSONObject json = new JSONObject(responseStr);
                    String newUrl = json.optString("avatarUrl", "");

                    if (newUrl.isEmpty()) {
                        emitirMensaje("⚠️ Respuesta inesperada del servidor");
                        return;
                    }

                    sessionManager.actualizarAvatarDesdeServidor(newUrl);
                    avatarUrl.setValue(newUrl);
                    cargarPropietario();

                    // 🔔 También avisar actualización de header tras cambio de avatar
                    Propietario actualizado = sessionManager.obtenerPropietarioActual();
                    if (actualizado != null) {
                        eventoActualizarHeader.setValue(actualizado);
                    }

                    emitirMensaje("✅ Avatar actualizado correctamente");

                } catch (Exception e) {
                    Log.e("PerfilViewModel", "Error procesando respuesta: " + e.getMessage());
                    emitirMensaje("⚠️ Error al procesar respuesta del servidor");
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                emitirMensaje("⚠️ Error de red: " + t.getMessage());
            }
        });
    }
}
