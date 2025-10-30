package com.jorge.inmobiliaria2025.ui.inquilinos;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.Retrofit.ApiService;
import com.jorge.inmobiliaria2025.Retrofit.RetrofitClient;
import com.jorge.inmobiliaria2025.localdata.SessionManager;
import com.jorge.inmobiliaria2025.model.InquilinoConInmueble;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InquilinosViewModel extends AndroidViewModel {

    private MutableLiveData<List<InquilinoConInmueble>> listaInquilinos = new MutableLiveData<>();
    private MutableLiveData<Integer> navegarDetalle = new MutableLiveData<>();

    public InquilinosViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<List<InquilinoConInmueble>> getListaInquilinos() { return listaInquilinos; }

    public LiveData<Integer> getNavegarDetalle() { return navegarDetalle; }

    public void seleccionarInquilino(int idInquilino) {
        navegarDetalle.setValue(idInquilino);
    }

    public void limpiarNavegacion() {
        navegarDetalle.setValue(null);
    }

    public void cargarInquilinosConInmueble() {
        ApiService api = RetrofitClient.getInstance(getApplication()).create(ApiService.class);
        String token = SessionManager.getInstance(getApplication()).obtenerToken();

        // ✅ Log del token
        Log.d("JWT_TOKEN_DEBUG", "Token enviado: " + token);

        api.getInquilinosConInmueble("Bearer " + token)
                .enqueue(new Callback<List<InquilinoConInmueble>>() {
                    @Override
                    public void onResponse(Call<List<InquilinoConInmueble>> call, Response<List<InquilinoConInmueble>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            listaInquilinos.setValue(response.body());
                            Log.d("InquilinosViewModel", "Cargados: " + response.body().size());
                        } else {
                            listaInquilinos.setValue(null);
                            Log.e("InquilinosViewModel", "Error HTTP: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<InquilinoConInmueble>> call, Throwable t) {
                        listaInquilinos.setValue(null);
                        Log.e("InquilinosViewModel", "Fallo: " + t.getMessage());
                    }
                });
    }
}
