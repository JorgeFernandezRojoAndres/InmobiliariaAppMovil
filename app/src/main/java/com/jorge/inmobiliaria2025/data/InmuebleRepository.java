package com.jorge.inmobiliaria2025.data;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.data.network.ApiService;
import com.jorge.inmobiliaria2025.data.network.RetrofitClient;
import com.jorge.inmobiliaria2025.data.SessionManager;
import com.jorge.inmobiliaria2025.model.Inmueble;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InmuebleRepository {

    private final ApiService apiService;
    private final SessionManager sessionManager;

    public InmuebleRepository(Context context) {
        apiService = RetrofitClient.getInstance(context).create(ApiService.class);
        sessionManager = new SessionManager(context);
    }

    // 🔹 Obtiene TODOS los inmuebles del propietario autenticado
    public LiveData<List<Inmueble>> obtenerMisInmuebles() {
        MutableLiveData<List<Inmueble>> data = new MutableLiveData<>();

        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
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
                    Log.w("RepoInmueble", "⚠️ Respuesta vacía o inválida del servidor");
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

    // 🔹 Nuevo método: actualiza disponibilidad (Activo) de un inmueble
    public LiveData<Boolean> actualizarDisponibilidad(Inmueble inmueble) {
        MutableLiveData<Boolean> resultado = new MutableLiveData<>();
        String token = sessionManager.getToken();

        if (token == null || token.isEmpty()) {
            resultado.setValue(false);
            return resultado;
        }

        apiService.actualizarDisponibilidad("Bearer " + token, inmueble.getId(), inmueble)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.i("RepoInmueble", "✅ Disponibilidad actualizada para ID " + inmueble.getId());
                            resultado.setValue(true);
                        } else {
                            Log.w("RepoInmueble", "⚠️ Falló actualización (código " + response.code() + ")");
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
}
