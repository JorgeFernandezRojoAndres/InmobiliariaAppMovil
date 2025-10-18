package com.jorge.inmobiliaria2025.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;



import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.data.network.ApiService;
import com.jorge.inmobiliaria2025.data.network.RetrofitClient;
import com.jorge.inmobiliaria2025.data.SessionManager;
import com.jorge.inmobiliaria2025.data.InmobiliariaDatabase;
import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.model.TipoInmueble;
import com.jorge.inmobiliaria2025.utils.FileUtils;

import java.io.File;
import java.util.Collections;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InmuebleRepository {

    private final ApiService apiService;
    private final SessionManager sessionManager;
    private final Context context;

    public InmuebleRepository(Context context) {
        this.context = context;
        this.apiService = RetrofitClient.getInstance(context).create(ApiService.class);
        this.sessionManager = new SessionManager(context);
    }

    // ================================
// 🔹 OBTENER INMUEBLES
// ================================
    public LiveData<List<Inmueble>> obtenerMisInmuebles() {
        MutableLiveData<List<Inmueble>> data = new MutableLiveData<>();
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Log.w("RepoInmueble", "⚠️ Token nulo al intentar obtener inmuebles");
            data.setValue(null);
            return data;
        }

        apiService.getMisInmuebles("Bearer " + token).enqueue(new Callback<List<Inmueble>>() {
            @Override
            public void onResponse(Call<List<Inmueble>> call, Response<List<Inmueble>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Inmueble> lista = response.body();

                    for (Inmueble i : lista) {
                        String img = i.getImagenUrl();
                        Log.i("RepoInmueble", "📦 Imagen cruda desde API: " + img);

                        if (img != null && !img.isEmpty()) {
                            // ⚙️ Si ya empieza con http → la dejamos igual
                            if (!img.startsWith("http")) {
                                // ✅ Evita duplicar /uploads/
                                String base = RetrofitClient.BASE_URL.replaceAll("/$", "");
                                String clean = img.replaceFirst("^/+", ""); // quita barras iniciales
                                i.setImagenUrl(base + "/" + clean);
                            }
                        } else {
                            Log.w("RepoInmueble", "⚠️ Inmueble sin imagen: " + i.getDireccion());
                        }

                        Log.i("RepoInmueble", "🖼️ URL final: " + i.getImagenUrl());
                    }


                    // 🧩 Actualizar base local (sincronización Room)
                    InmobiliariaDatabase db = InmobiliariaDatabase.getInstance(context);
                    InmuebleDao dao = db.inmuebleDao();

                    new Thread(() -> {
                        try {
                            dao.deleteAll();
                            dao.insertAll(lista);
                            Log.i("RepoInmueble", "🧱 DB local sincronizada con " + lista.size() + " inmuebles");
                        } catch (Exception e) {
                            Log.e("RepoInmueble", "❌ Error al sincronizar DB local: " + e.getMessage());
                        }
                    }).start();

                    data.setValue(lista);
                    Log.i("RepoInmueble", "✅ Inmuebles recibidos: " + lista.size());
                } else {
                    Log.w("RepoInmueble", "⚠️ Error HTTP " + response.code() + " al obtener inmuebles");
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<Inmueble>> call, Throwable t) {
                Log.e("RepoInmueble", "❌ Error al obtener inmuebles: " + t.getMessage());
                InmobiliariaDatabase db = InmobiliariaDatabase.getInstance(context);
                InmuebleDao dao = db.inmuebleDao();

                new Thread(() -> {
                    try {
                        List<Inmueble> listaLocal = dao.getAll();
                        data.postValue(listaLocal);
                        Log.w("RepoInmueble", "📦 Modo offline: cargados " + listaLocal.size() + " inmuebles desde Room");
                    } catch (Exception e) {
                        Log.e("RepoInmueble", "❌ Error al leer DB local: " + e.getMessage());
                    }
                }).start();
            }
        });

        return data;
    }

    // ================================
    // 🔹 ACTUALIZAR DISPONIBILIDAD
    // ================================
    public LiveData<Boolean> actualizarDisponibilidad(Inmueble inmueble) {
        MutableLiveData<Boolean> resultado = new MutableLiveData<>();
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Log.w("RepoInmueble", "⚠️ Token nulo al actualizar disponibilidad");
            resultado.setValue(false);
            return resultado;
        }

        apiService.actualizarDisponibilidad("Bearer " + token, inmueble.getId(), inmueble)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.i("RepoInmueble", "✅ Disponibilidad actualizada (ID=" + inmueble.getId() + ")");
                            resultado.setValue(true);
                        } else {
                            Log.w("RepoInmueble", "⚠️ Falló actualización (HTTP " + response.code() + ")");
                            resultado.setValue(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("RepoInmueble", "❌ Error al actualizar disponibilidad: " + t.getMessage());
                        resultado.setValue(false);
                    }
                });

        return resultado;
    }

    // ================================
    // 🔹 OBTENER TIPOS DE INMUEBLE
    // ================================
    public LiveData<List<TipoInmueble>> obtenerTiposInmueble() {
        MutableLiveData<List<TipoInmueble>> data = new MutableLiveData<>();
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Log.w("RepoInmueble", "⚠️ Token nulo al obtener tipos de inmueble");
            data.setValue(Collections.emptyList());
            return data;
        }

        apiService.getTiposInmueble("Bearer " + token).enqueue(new Callback<List<TipoInmueble>>() {
            @Override
            public void onResponse(Call<List<TipoInmueble>> call, Response<List<TipoInmueble>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TipoInmueble> lista = response.body();
                    data.setValue(lista);
                    Log.i("RepoInmueble", "✅ Tipos de inmueble recibidos: " + lista.size());
                } else {
                    Log.w("RepoInmueble", "⚠️ No se pudieron obtener tipos (HTTP " + response.code() + ")");
                    data.setValue(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<List<TipoInmueble>> call, Throwable t) {
                Log.e("RepoInmueble", "❌ Error al obtener tipos: " + t.getMessage());
                data.setValue(Collections.emptyList());
            }
        });

        return data;
    }

    // ================================
    // 🔹 ACTUALIZAR INMUEBLE (auto JSON o form-data)
    // ================================
    public LiveData<Boolean> actualizarInmueble(Inmueble inmueble, Uri imagenUri) {
        MutableLiveData<Boolean> resultado = new MutableLiveData<>();
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Log.w("RepoInmueble", "⚠️ Token nulo al actualizar inmueble");
            resultado.setValue(false);
            return resultado;
        }

        // Si NO hay imagen → JSON normal
        if (imagenUri == null) {
            apiService.actualizarInmueble("Bearer " + token, inmueble.getId(), inmueble)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Log.i("RepoInmueble", "✅ Inmueble actualizado correctamente (JSON)");
                                resultado.setValue(true);
                            } else {
                                Log.w("RepoInmueble", "⚠️ Error HTTP " + response.code() + " al actualizar inmueble (JSON)");
                                resultado.setValue(false);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e("RepoInmueble", "❌ Error al actualizar inmueble (JSON): " + t.getMessage());
                            resultado.setValue(false);
                        }
                    });
            return resultado;
        }

        // Si hay imagen → usar form-data
        try {
            File file = new File(FileUtils.getPathFromUri(context, imagenUri));
            if (!file.exists()) {
                Log.e("RepoInmueble", "❌ Archivo de imagen no encontrado");
                resultado.setValue(false);
                return resultado;
            }

            RequestBody direccion = RequestBody.create(MediaType.parse("text/plain"), inmueble.getDireccion());
            RequestBody tipoId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.getTipoId()));
            RequestBody metros = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.getMetrosCuadrados()));
            RequestBody precio = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.getPrecio()));
            RequestBody activo = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.isActivo()));
            RequestBody id = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.getId()));

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part imagenPart = MultipartBody.Part.createFormData("imagen", file.getName(), requestFile);

            apiService.actualizarInmuebleConImagen(
                    "Bearer " + token,
                    inmueble.getId(),
                    id, direccion, tipoId, metros, precio, activo, imagenPart
            ).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        Log.i("RepoInmueble", "✅ Inmueble actualizado correctamente con imagen");
                        resultado.setValue(true);
                    } else {
                        Log.w("RepoInmueble", "⚠️ Falló actualización inmueble (HTTP " + response.code() + ")");
                        resultado.setValue(false);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("RepoInmueble", "❌ Error al actualizar inmueble (form-data): " + t.getMessage());
                    resultado.setValue(false);
                }
            });
        } catch (Exception e) {
            Log.e("RepoInmueble", "❌ Excepción al preparar imagen: " + e.getMessage());
            resultado.setValue(false);
        }

        return resultado;
    }

    // ================================
// 🆕 SUBIR IMAGEN DE INMUEBLE
// ================================
    public LiveData<Boolean> subirImagenInmueble(int idInmueble, Uri imagenUri) {
        MutableLiveData<Boolean> resultado = new MutableLiveData<>();
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Log.w("RepoInmueble", "⚠️ Token nulo al subir imagen");
            resultado.setValue(false);
            return resultado;
        }

        if (imagenUri == null) {
            Log.w("RepoInmueble", "⚠️ URI de imagen nula");
            resultado.setValue(false);
            return resultado;
        }

        try {
            File file = new File(FileUtils.getPathFromUri(context, imagenUri));
            if (!file.exists()) {
                Log.e("RepoInmueble", "❌ Archivo no encontrado: " + file.getAbsolutePath());
                resultado.setValue(false);
                return resultado;
            }

            // 🔹 Crea el cuerpo del archivo e ID
            RequestBody idBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(idInmueble));
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part imagenPart = MultipartBody.Part.createFormData("imagen", file.getName(), requestFile);

            // 🔹 Llamada a la API
            apiService.subirImagenInmueble("Bearer " + token, idBody, imagenPart)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Log.i("RepoInmueble", "✅ Imagen subida correctamente (ID=" + idInmueble + ")");
                                resultado.setValue(true);
                            } else {
                                Log.w("RepoInmueble", "⚠️ Falló subida de imagen (HTTP " + response.code() + ")");
                                resultado.setValue(false);
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e("RepoInmueble", "❌ Error al subir imagen: " + t.getMessage());
                            resultado.setValue(false);
                        }
                    });

        } catch (Exception e) {
            Log.e("RepoInmueble", "❌ Excepción al subir imagen: " + e.getMessage());
            resultado.setValue(false);
        }

        return resultado;
    }

    // ================================
// 🧩 ACTUALIZAR INMUEBLE CON IMAGEN (form-data)
// ================================
    public LiveData<Boolean> actualizarInmuebleConImagenForm(Inmueble inmueble, Uri imagenUri) {
        MutableLiveData<Boolean> resultado = new MutableLiveData<>();
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Log.w("RepoInmueble", "⚠️ Token nulo al actualizar inmueble (form-data)");
            resultado.setValue(false);
            return resultado;
        }

        MultipartBody.Part imagenPart = null;
        try {
            if (imagenUri != null) {
                File file = new File(FileUtils.getPathFromUri(context, imagenUri));
                if (file.exists()) {
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                    imagenPart = MultipartBody.Part.createFormData("imagen", file.getName(), requestFile);
                }
            }
        } catch (Exception e) {
            Log.e("RepoInmueble", "⚠️ Error al procesar imagen: " + e.getMessage());
        }

        // 🔹 Campos del formulario
        RequestBody idBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.getId()));
        RequestBody direccion = RequestBody.create(MediaType.parse("text/plain"), inmueble.getDireccion());
        RequestBody tipoId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.getTipoId()));
        RequestBody metros = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.getMetrosCuadrados()));
        RequestBody precio = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.getPrecio()));
        RequestBody activo = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.isActivo()));

        // 🔹 Llamada al endpoint multipart
        apiService.actualizarInmuebleConImagen(
                "Bearer " + token,
                inmueble.getId(),
                idBody, direccion, tipoId, metros, precio, activo, imagenPart
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.i("RepoInmueble", "✅ Inmueble actualizado correctamente con imagen (ID=" + inmueble.getId() + ")");
                    resultado.setValue(true);
                } else {
                    Log.w("RepoInmueble", "⚠️ Falló actualización inmueble (HTTP " + response.code() + ")");
                    resultado.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("RepoInmueble", "❌ Error al actualizar inmueble (form-data): " + t.getMessage());
                resultado.setValue(false);
            }
        });

        return resultado;
    }

}

