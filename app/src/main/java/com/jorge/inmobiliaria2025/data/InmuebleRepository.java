package com.jorge.inmobiliaria2025.data;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.jorge.inmobiliaria2025.data.network.ApiService;
import com.jorge.inmobiliaria2025.data.network.RetrofitClient;
import com.jorge.inmobiliaria2025.model.Inmueble;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InmuebleRepository {
    private final ApiService apiService;

    public InmuebleRepository(Context context) {
        // ✅ Se corrige el método para usar getInstance() (el real en RetrofitClient)
        apiService = RetrofitClient.getInstance(context).create(ApiService.class);
    }

    public LiveData<List<Inmueble>> obtenerInmueblesAlquilados() {
        MutableLiveData<List<Inmueble>> data = new MutableLiveData<>();

        // ✅ Retrofit requiere el tipo explícito en Callback<>
        apiService.getInmueblesAlquilados().enqueue(new Callback<List<Inmueble>>() {
            @Override
            public void onResponse(Call<List<Inmueble>> call, Response<List<Inmueble>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    data.setValue(null);
                }
            }

            @Override
            public void onFailure(Call<List<Inmueble>> call, Throwable t) {
                data.setValue(null);
            }
        });

        return data;
    }
}
