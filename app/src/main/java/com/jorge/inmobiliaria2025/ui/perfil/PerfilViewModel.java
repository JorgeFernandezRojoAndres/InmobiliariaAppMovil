    package com.jorge.inmobiliaria2025.ui.perfil;
    import android.content.Context;
    
    import android.app.Application;
    import android.net.Uri;
    import android.util.Log;
    import com.jorge.inmobiliaria2025.Retrofit.RetrofitClient;
    
    import androidx.annotation.NonNull;
    import androidx.lifecycle.AndroidViewModel;
    import androidx.lifecycle.LiveData;
    import androidx.lifecycle.MutableLiveData;
    
    import com.jorge.inmobiliaria2025.InmobiliariaApp;
    import com.jorge.inmobiliaria2025.Retrofit.ApiService;
    
    import com.jorge.inmobiliaria2025.localdata.SessionManager;
    import com.jorge.inmobiliaria2025.model.CambioClaveDto;
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
    
        // ---------- LiveData ----------
        private final MutableLiveData<String> email = new MutableLiveData<>();
        private final MutableLiveData<Boolean> cerrarSesionEvento = new MutableLiveData<>();
        private final MutableLiveData<Propietario> propietario = new MutableLiveData<>(new Propietario());
        private final MutableLiveData<String> avatarUrl = new MutableLiveData<>("");
        private final MutableLiveData<Boolean> abrirGaleriaEvento = new MutableLiveData<>();
        private final MutableLiveData<String> mensajeEvento = new MutableLiveData<>();
        private final MutableLiveData<Void> mostrarDialogoClave = new MutableLiveData<>();
        private final MutableLiveData<Propietario> eventoActualizarHeader = new MutableLiveData<>();
        private final MutableLiveData<Void> activarEdicionEvento = new MutableLiveData<>();
        private final MutableLiveData<Void> guardarCambiosEvento = new MutableLiveData<>();
        private final MutableLiveData<Boolean> permitirCambioAvatar = new MutableLiveData<>(false);
        private final MutableLiveData<Boolean> modoEdicion = new MutableLiveData<>(false);
        private final SessionManager sessionManager;
    
        public PerfilViewModel(@NonNull Application app) {
            super(app);
            sessionManager = SessionManager.getInstance(app.getApplicationContext());
    
    
            cargarEmail();
            cargarPerfilDesdeApi();
        }
    
        // ---------- Getters ----------
        public LiveData<String> getEmail() { return email; }
        public LiveData<Void> getMostrarDialogoClave() { return mostrarDialogoClave; }
        public LiveData<Boolean> getModoEdicion() { return modoEdicion; }
        public LiveData<Boolean> getCerrarSesionEvento() { return cerrarSesionEvento; }
        public LiveData<Propietario> getPropietario() { return propietario; }
        public LiveData<String> getAvatarUrl() { return avatarUrl; }
        public LiveData<Boolean> getAbrirGaleriaEvento() { return abrirGaleriaEvento; }
        public LiveData<String> getMensajeEvento() { return mensajeEvento; }
        public LiveData<Propietario> getEventoActualizarHeader() { return eventoActualizarHeader; }
        public LiveData<Void> getActivarEdicionEvento() { return activarEdicionEvento; }
        public LiveData<Void> getGuardarCambiosEvento() { return guardarCambiosEvento; }
        public LiveData<Boolean> getPermitirCambioAvatar() { return permitirCambioAvatar; }
    
        // ---------- Inicialización ----------
        private void cargarEmail() {
            String guardado = InmobiliariaApp.getInstance().obtenerEmail();
            email.setValue(guardado != null ? guardado : "No hay sesión activa");
        }
    
        public void cargarPerfilDesdeApi() {
            ApiService api = RetrofitClient.getInstance(getApplication()).create(ApiService.class);
    
            api.obtenerPerfil().enqueue(new Callback<Propietario>() {
                @Override
                public void onResponse(@NonNull Call<Propietario> call, @NonNull Response<Propietario> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
    
                            Propietario p = response.body();
    
                            // ✅ Guardar y publicar propietario
                            propietario.postValue(p);
                            email.postValue(p.getEmail());
                            sessionManager.guardarPropietario(p);
    
                            // ✅ Actualizar avatar
                            avatarUrl.postValue(sessionManager.getAvatarFullUrl(p.getAvatarUrl()));
                            eventoActualizarHeader.postValue(p);
    
                            // ❌ NO guardar token aquí
                            // Este endpoint NO devuelve token, así que no lo buscamos más
    
                        } else if (response.code() == 401) {
                            emitirMensaje("⚠️ Sesión expirada.");
                            sessionManager.logout();
                            cerrarSesionEvento.postValue(true);
                        } else {
                            emitirMensaje("❌ No se pudo obtener el perfil");
                        }
    
                    } catch (Exception e) {
                        emitirMensaje("⚠️ Error procesando perfil");
                    }
                }
    
                @Override
                public void onFailure(@NonNull Call<Propietario> call, @NonNull Throwable t) {
                    emitirMensaje("⚠️ Error de red al obtener perfil");
                }
            });
        }
    
    
        // ---------- Acciones de usuario ----------
        // ---------- Acciones de usuario ----------
        public void onEditarPerfilClick(String nombre, String apellido, String email, String telefono) {
            // ✅ Agregamos el control del modo edición usando LiveData
            if (modoEdicion.getValue() == null) {
                modoEdicion.setValue(false);
            }
    
            boolean enEdicion = Boolean.TRUE.equals(modoEdicion.getValue());
    
            if (!enEdicion) {
                // 🔓 Activar modo edición
                modoEdicion.setValue(true);
                permitirCambioAvatar.setValue(true);
                activarEdicionEvento.setValue(null);
                emitirMensaje("✏️ Modo edición activado");
            } else {
                // 💾 Guardar cambios del propietario
                Propietario p = propietario.getValue();
                if (p != null) {
                    p.setNombre(nombre);
                    p.setApellido(apellido);
                    p.setEmail(email);
                    p.setTelefono(telefono);
                    guardarPerfil();
                }
                modoEdicion.setValue(false);
                permitirCambioAvatar.setValue(false);
                guardarCambiosEvento.setValue(null);
            }
        }
    
    
    
        public void onCerrarSesionClick() {
            sessionManager.logout();
            InmobiliariaApp.getInstance().cerrarSesion();
            cerrarSesionEvento.setValue(true);
        }
    
    
        public void onCambiarClaveClick() {
            mostrarDialogoClave.setValue(null);
        }
    
        public void onAvatarClick() {
            if (Boolean.TRUE.equals(permitirCambioAvatar.getValue())) {
                abrirGaleriaEvento.setValue(true);
            } else {
                emitirMensaje("Toca 'Editar' para poder cambiar la foto");
            }
        }
    
        // ---------- Guardar cambios ----------
        public void guardarCambiosPerfil(Propietario p) {
            ApiService api = RetrofitClient.getInstance(getApplication()).create(ApiService.class);
            String token = sessionManager.obtenerToken();
            if (token == null || token.isEmpty()) {
                emitirMensaje("⚠️ Sesión expirada.");
                cerrarSesionEvento.postValue(true);
                return;
            }
    
            api.actualizarPerfil(p).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            emitirMensaje(json.optString("mensaje", "✅ Perfil actualizado correctamente"));
    
                            // 🔄 Si el backend devuelve datos nuevos del propietario
                            JSONObject propJson = json.optJSONObject("propietario");
                            if (propJson != null) {
                                p.setNombre(propJson.optString("nombre", p.getNombre()));
                                p.setApellido(propJson.optString("apellido", p.getApellido()));
                                p.setEmail(propJson.optString("email", p.getEmail()));
                                p.setTelefono(propJson.optString("telefono", p.getTelefono()));
                                p.setAvatarUrl(propJson.optString("avatarUrl", p.getAvatarUrl()));
                            }
    
                            // 🧠 Sincronizar datos locales y LiveData
                            propietario.postValue(p);
                            sessionManager.guardarPropietario(p);
                            eventoActualizarHeader.postValue(p);
                            guardarCambiosEvento.setValue(null);
    
                        } catch (Exception e) {
                            Log.e("PerfilViewModel", "Error procesando respuesta", e);
                            emitirMensaje("⚠️ Error procesando respuesta del servidor");
                        }
                    } else {
                        emitirMensaje("❌ No se pudo actualizar (" + response.code() + ")");
                    }
                }
    
                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    emitirMensaje("⚠️ Error de red: " + t.getMessage());
                }
            });
        }
    
        public void guardarPerfil() {
            Context context = getApplication();
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
    
            api.actualizarPerfil(p).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
    
                            String responseStr = response.body().string();
                            JSONObject json = new JSONObject(responseStr);
    
                            String nuevoToken = json.optString("token", "");
    
                        // ✅ Solo guardar si es realmente un JWT válido
                            if (nuevoToken != null && nuevoToken.length() > 20) {
                                sessionManager.saveToken(nuevoToken);
                                RetrofitClient.reset(); // reconstruir cliente con nuevo token
                                Log.d("PerfilViewModel", "🔑 Nuevo token guardado tras cambio de email");
                            } else {
                                Log.d("PerfilViewModel", "ℹ️ No se recibió token nuevo (no hubo cambio de email)");
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
    
    
        // ---------- Subir avatar ----------
        public void procesarResultadoImagen(Uri uri) {
            if (uri == null) {
                emitirMensaje("⚠️ No se seleccionó imagen");
                return;
            }
    
            try (InputStream inputStream = getApplication().getContentResolver().openInputStream(uri);
                 ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
    
                byte[] data = new byte[1024];
                int nRead;
                while ((nRead = inputStream.read(data)) != -1) buffer.write(data, 0, nRead);
    
                byte[] bytes = buffer.toByteArray();
                subirAvatar(bytes);
    
            } catch (Exception e) {
                emitirMensaje("⚠️ Error leyendo imagen");
            }
        }
    
        private void subirAvatar(byte[] bytes) {
            ApiService api = RetrofitClient.getInstance(getApplication()).create(ApiService.class);
            String token = sessionManager.obtenerToken();
            if (token == null || token.isEmpty()) {
                emitirMensaje("⚠️ Sesión expirada.");
                cerrarSesionEvento.postValue(true);
                return;
            }
    
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("archivo", "avatar.jpg", requestFile);
    
            api.subirAvatar(body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            JSONObject json = new JSONObject(response.body().string());
                            String newUrl = json.optString("avatarUrl", "");
    
                            // ✅ Guardar avatar en SessionManager
                            sessionManager.guardarAvatar(newUrl);
    
                            // ✅ Reforzar token para no perder sesión
                            String currentToken = sessionManager.obtenerToken();
                            if (currentToken != null && !currentToken.isEmpty()) {
                                sessionManager.saveToken(currentToken);
                                RetrofitClient.reset(); // reconstruye Retrofit con token nuevo
                            }
    
    
                            // ✅ Actualizar LiveData para UI
                            avatarUrl.postValue(sessionManager.getAvatarFullUrl(newUrl));
                            eventoActualizarHeader.postValue(sessionManager.getPropietario());
    
                            // ✅ Recargar perfil para refrescar todos los datos
                            cargarPerfilDesdeApi();
    
                            emitirMensaje("✅ Avatar actualizado correctamente");
    
                        } catch (Exception e) {
                            emitirMensaje("⚠️ Error procesando respuesta del servidor");
                        }
                    } else {
                        emitirMensaje("❌ Error al subir imagen");
                    }
                }
    
                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    emitirMensaje("⚠️ Error de red al subir avatar");
                }
            });
        }
    
    
        // ---------- Cambio de contraseña ----------
        public void cambiarClave(String actual, String nueva) {
            if (actual.isEmpty() || nueva.isEmpty()) {
                emitirMensaje("⚠️ Complete ambos campos");
                return;
            }
    
            ApiService api = RetrofitClient.getInstance(getApplication()).create(ApiService.class);
            String token = sessionManager.obtenerToken();
            if (token == null || token.isEmpty()) {
                emitirMensaje("⚠️ Sesión expirada.");
                cerrarSesionEvento.postValue(true);
                return;
            }
    
            CambioClaveDto dto = new CambioClaveDto(actual, nueva);
            api.cambiarClave(dto).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        emitirMensaje("✅ Contraseña actualizada correctamente");
                    } else if (response.code() == 400) {
                        emitirMensaje("⚠️ Contraseña actual incorrecta");
                    } else {
                        emitirMensaje("❌ Error al cambiar contraseña");
                    }
                }
    
                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    emitirMensaje("⚠️ Error de red: " + t.getMessage());
                }
            });
        }
    
        // ---------- Utilidad ----------
        private void emitirMensaje(String msg) {
            mensajeEvento.postValue(msg);
        }
    }
