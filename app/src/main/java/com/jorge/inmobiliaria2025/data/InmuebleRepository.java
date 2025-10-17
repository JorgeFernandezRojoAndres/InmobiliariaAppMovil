package com.jorge.inmobiliaria2025.data;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.data.network.ApiService;
import com.jorge.inmobiliaria2025.data.network.RetrofitClient;
import com.jorge.inmobiliaria2025.data.SessionManager;
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
                    data.setValue(response.body());
                    Log.i("RepoInmueble", "✅ Inmuebles recibidos: " + response.body().size());
                } else {
                    Log.w("RepoInmueble", "⚠️ Error HTTP " + response.code() + " al obtener inmuebles");
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<Inmueble>> call, Throwable t) {
                Log.e("RepoInmueble", "❌ Error al obtener inmuebles: " + t.getMessage());
                data.setValue(null);
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
    // 🔹 ACTUALIZAR INMUEBLE COMPLETO
    // ================================
    public LiveData<Boolean> actualizarInmueble(Inmueble inmueble, Uri imagenUri) {
        MutableLiveData<Boolean> resultado = new MutableLiveData<>();
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            Log.w("RepoInmueble", "⚠️ Token nulo al actualizar inmueble");
            resultado.setValue(false);
            return resultado;
        }

        // 🧱 Construcción multipart
        RequestBody direccion = RequestBody.create(MediaType.parse("text/plain"), inmueble.getDireccion());
        RequestBody tipoId = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.getTipoId()));
        RequestBody metros = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.getMetrosCuadrados()));
        RequestBody precio = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.getPrecio()));
        RequestBody activo = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(inmueble.isActivo()));

        MultipartBody.Part imagenPart = null;
        try {
            if (imagenUri != null) {
                File file = new File(FileUtils.getPathFromUri(context, imagenUri));
                if (file.exists()) {
                    RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
                    imagenPart = MultipartBody.Part.createFormData("imagen", file.getName(), requestFile);
                } else {
                    Log.w("RepoInmueble", "⚠️ Archivo de imagen no encontrado: " + file.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            Log.e("RepoInmueble", "⚠️ Error al procesar imagen: " + e.getMessage());
        }

        apiService.actualizarInmueble("Bearer " + token,
                        inmueble.getId(), direccion, tipoId, metros, precio, activo, imagenPart)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.i("RepoInmueble", "✅ Inmueble actualizado correctamente ID=" + inmueble.getId());
                            resultado.setValue(true);
                        } else {
                            Log.w("RepoInmueble", "⚠️ Falló actualización inmueble (HTTP " + response.code() + ")");
                            resultado.setValue(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e("RepoInmueble", "❌ Error al actualizar inmueble: " + t.getMessage());
                        resultado.setValue(false);
                    }
                });

        return resultado;
    }
}
