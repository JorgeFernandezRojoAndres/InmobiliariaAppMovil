package com.jorge.inmobiliaria2025.viewmodel;
import com.jorge.inmobiliaria2025.model.CambioClaveDto;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.view.View;
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
    private final MutableLiveData<Propietario> eventoActualizarHeader = new MutableLiveData<>();
    private final MutableLiveData<Void> activarEdicionEvento = new MutableLiveData<>();
    private final MutableLiveData<Void> guardarCambiosEvento = new MutableLiveData<>();
    private final MutableLiveData<Boolean> permitirCambioAvatar = new MutableLiveData<>(false);

    private final SessionManager sessionManager;

    public PerfilViewModel(@NonNull Application app) {
        super(app);
        sessionManager = new SessionManager(app);
        cargarEmail();
        cargarPerfilDesdeApi(app);
    }

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
    public LiveData<Boolean> getPermitirCambioAvatar() { return permitirCambioAvatar; }

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

    public void cargarPerfilDesdeApi(Context context) {
        ApiService api = RetrofitClient.getInstance(context).create(ApiService.class);
        String token = sessionManager.obtenerToken();

        // 🔹 Validación de sesión centralizada
        if (token == null || token.isBlank())
        {
            Log.w("PerfilViewModel", "⚠️ Token no disponible. Cargando perfil local.");
            emitirMensaje("⚠️ Sesión expirada. Inicie sesión nuevamente.");
            cargarPropietario();
            cerrarSesionEvento.postValue(Boolean.TRUE);
            return;
        }

        api.obtenerPerfil("Bearer " + token).enqueue(new Callback<Propietario>() {
            @Override
            public void onResponse(@NonNull Call<Propietario> call, @NonNull Response<Propietario> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Propietario p = response.body();
                        propietario.postValue(p);
                        avatarUrl.postValue(sessionManager.getAvatarFullUrl(p.getAvatarUrl()));
                        sessionManager.guardarPropietario(p);
                        email.postValue(p.getEmail());
                        eventoActualizarHeader.postValue(p);
                        Log.d("PerfilViewModel", "✅ Perfil sincronizado correctamente");
                    }
                    else {
                        String mensaje = (response.code() == 401)
                                ? "⚠️ Sesión expirada. Inicie sesión nuevamente."
                                : "❌ No se pudo obtener el perfil (" + response.code() + ")";
                        emitirMensaje(mensaje);

                        if (response.code() == 401) {
                            sessionManager.logout();
                            cerrarSesionEvento.postValue(Boolean.TRUE);
                        } else {
                            cargarPropietario();
                        }
                    }
                } catch (Exception e) {
                    Log.e("PerfilViewModel", "❌ Error procesando respuesta de perfil", e);
                    emitirMensaje("⚠️ Error procesando perfil: " + e.getMessage());
                    cargarPropietario();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Propietario> call, @NonNull Throwable t) {
                Log.e("PerfilViewModel", "🌐 Error de red al obtener perfil: " + t.getMessage());
                emitirMensaje("⚠️ No se pudo conectar con el servidor");
                cargarPropietario();
            }
        });
    }

    public void cargarPropietario() {
        Propietario p = sessionManager.obtenerPropietarioActual();
        if (p == null) {
            propietario.setValue(new Propietario());
            avatarUrl.setValue("");
        } else {
            propietario.setValue(p);
            avatarUrl.setValue(sessionManager.getAvatarFullUrl(p.getAvatarUrl()));
        }
    }

    public void actualizarPropietario(Propietario p) {
        try {
            sessionManager.guardarPropietario(p);
            propietario.setValue(p);
            email.setValue(p.getEmail());
            avatarUrl.setValue(sessionManager.getAvatarFullUrl(p.getAvatarUrl()));
            eventoActualizarHeader.setValue(p);
            emitirMensaje("✅ Datos actualizados correctamente");
        } catch (Exception e) {
            emitirMensaje("⚠️ No se pudieron guardar los cambios");
            Log.e("PerfilViewModel", "Error al actualizar propietario: " + e.getMessage());
        }
    }

    public void alternarModoEdicion() {
        boolean nuevoEstado = !Boolean.TRUE.equals(modoEdicion.getValue());
        modoEdicion.setValue(nuevoEstado);
        permitirCambioAvatar.setValue(nuevoEstado);
        if (nuevoEstado) activarEdicionEvento.setValue(null);
        else guardarCambiosEvento.setValue(null);
    }

    public void setModoEdicion(boolean activo) {
        modoEdicion.setValue(activo);
        permitirCambioAvatar.setValue(activo);
    }

    public void onAvatarClick(boolean estaEnModoEdicion) {
        if (estaEnModoEdicion) abrirGaleriaEvento.setValue(true);
        else emitirMensaje("Toca 'Editar' para poder cambiar la foto de perfil");
    }

    public void setPermitirCambioAvatar(boolean permitir) {
        permitirCambioAvatar.setValue(permitir);
    }

    public void emitirMensaje(String msg) {
        mensajeEvento.setValue(msg);
    }

    // ✅ Nuevo método: decide cómo mostrar la clave en la UI
    public String obtenerTextoSeguroClave(String clave) {
        if (clave == null || clave.trim().isEmpty()) {
            return "••••••••"; // placeholder visible cuando no hay clave
        }
        return clave;
    }

    // -------------------- ✅ Validación + acceso a datos --------------------
    public void guardarCambiosPerfil(String id, String documento, String nombre, String apellido,
                                     String email, String clave, String telefono, Context context) {

        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty()) {
            emitirMensaje("⚠️ Todos los campos obligatorios deben estar completos");
            return;
        }
        if (!email.contains("@")) {
            emitirMensaje("⚠️ Ingrese un email válido");
            return;
        }

        try {
            Propietario p = new Propietario();
            p.setId(Integer.parseInt(id)); // mantener ID interno (no editable, pero necesario)
            p.setDocumento(documento);
            p.setNombre(nombre);
            p.setApellido(apellido);
            p.setEmail(email);
            p.setClave(clave);
            p.setTelefono(telefono);

            actualizarPropietario(p);
            guardarPerfil(context);
            emitirMensaje("✅ Datos actualizados correctamente");

        } catch (Exception e) {
            emitirMensaje("❌ Error al guardar los cambios: " + e.getMessage());
            Log.e("PerfilViewModel", "Error guardarCambiosPerfil", e);
        }
    }

    public void guardarPerfil(Context context) {
        ApiService api = RetrofitClient.getInstance(context).create(ApiService.class);
        String token = sessionManager.obtenerToken();
        if (token == null || token.isEmpty()) {
            emitirMensaje("⚠️ Sesión expirada. Inicie sesión nuevamente.");
            return;
        }

        Propietario p = propietario.getValue();
        if (p == null) {
            emitirMensaje("❌ No hay datos de perfil para guardar.");
            return;
        }

        if (p.getTelefono() == null) p.setTelefono("");

        // 🔹 Usamos ResponseBody para manejar JSON genérico { token, propietario }
        api.actualizarPerfil("Bearer " + token, p).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {

                        // 🔍 Parseamos manualmente la respuesta JSON
                        String responseStr = response.body().string();
                        JSONObject json = new JSONObject(responseStr);

                        // 🔑 Si vino un nuevo token (por cambio de email), lo guardamos
                        String nuevoToken = json.optString("token", "");
                        if (!nuevoToken.isEmpty()) {
                            sessionManager.saveToken(nuevoToken);
                            Log.d("PerfilViewModel", "🔑 Nuevo token guardado tras cambio de email");
                        }

                        // 🧩 Extraemos el propietario actualizado
                        JSONObject propJson = json.optJSONObject("propietario");
                        if (propJson != null) {
                            Propietario actualizado = new Propietario();
                            actualizado.setId(propJson.optInt("id", 0));
                            actualizado.setNombre(propJson.optString("nombre", ""));
                            actualizado.setApellido(propJson.optString("apellido", ""));
                            actualizado.setEmail(propJson.optString("email", ""));
                            actualizado.setTelefono(propJson.optString("telefono", ""));
                            actualizado.setAvatarUrl(propJson.optString("avatarUrl", ""));

                            // 🔄 Actualizamos todo localmente
                            propietario.postValue(actualizado);
                            avatarUrl.postValue(sessionManager.getAvatarFullUrl(actualizado.getAvatarUrl()));
                            sessionManager.guardarPropietario(actualizado);
                            eventoActualizarHeader.postValue(actualizado);
                            sessionManager.saveEmail(actualizado.getEmail());
                        }

                        emitirMensaje(json.optString("mensaje", "✅ Perfil actualizado correctamente"));

                    } else {
                        emitirMensaje("❌ No se pudo actualizar (" + response.code() + ")");
                    }

                } catch (Exception e) {
                    emitirMensaje("⚠️ Error procesando respuesta: " + e.getMessage());
                    Log.e("PerfilViewModel", "Error en guardarPerfil", e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                emitirMensaje("⚠️ Error de red: " + t.getMessage());
            }
        });
    }


    // -------------------- Procesar imagen y subir avatar --------------------
    public void procesarResultadoImagen(Uri uri, Context context) {
        if (uri == null) {
            emitirMensaje("⚠️ No se seleccionó ninguna imagen");
            return;
        }
        subirAvatar(uri, context);
    }

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
        String token = sessionManager.obtenerToken();
        if (token == null || token.isEmpty()) {
            emitirMensaje("⚠️ Sesión expirada. Inicie sesión nuevamente.");
            return;
        }

        Call<ResponseBody> call = api.subirAvatar("Bearer " + token, body);

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

                    // ✅ Actualiza directamente el avatar en la sesión
                    sessionManager.guardarAvatar(newUrl);
                    avatarUrl.postValue(sessionManager.getAvatarFullUrl(newUrl));

                    Propietario actualizado = sessionManager.obtenerPropietarioActual();
                    if (actualizado != null) eventoActualizarHeader.postValue(actualizado);

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

    // -------------------- 🔐 Cambio de contraseña --------------------
    public void cambiarClave(String claveActual, String nuevaClave, Context context) {
        if (claveActual.isEmpty() || nuevaClave.isEmpty()) {
            emitirMensaje("⚠️ Complete ambos campos");
            return;
        }

        String token = sessionManager.obtenerToken();
        if (token == null || token.isEmpty()) {
            emitirMensaje("⚠️ Sesión expirada. Inicie sesión nuevamente.");
            cerrarSesionEvento.setValue(Boolean.TRUE);
            return;
        }

        ApiService api = RetrofitClient.getInstance(context).create(ApiService.class);

        // ✅ Enviamos el DTO con las claves correctas (mayúscula inicial)
        CambioClaveDto dto = new CambioClaveDto(claveActual, nuevaClave);

        Call<ResponseBody> call = api.cambiarClave("Bearer " + token, dto);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseStr = response.body().string();
                        JSONObject json = new JSONObject(responseStr);
                        String mensaje = json.optString("mensaje", "✅ Contraseña actualizada correctamente");
                        emitirMensaje(mensaje);
                        Log.d("PerfilViewModel", "🔑 Cambio de clave exitoso");
                    } else if (response.code() == 400) {
                        emitirMensaje("⚠️ Contraseña actual incorrecta");
                    } else if (response.code() == 401) {
                        emitirMensaje("⚠️ Sesión expirada. Inicie sesión nuevamente.");
                        cerrarSesionEvento.setValue(Boolean.TRUE);
                    } else {
                        emitirMensaje("❌ Error al cambiar la contraseña (" + response.code() + ")");
                    }
                } catch (Exception e) {
                    emitirMensaje("⚠️ Error procesando respuesta: " + e.getMessage());
                    Log.e("PerfilViewModel", "Error en cambiarClave", e);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                emitirMensaje("⚠️ Error de red: " + t.getMessage());
                Log.e("PerfilViewModel", "Error de red cambiarClave", t);
            }
        });
    }


}
