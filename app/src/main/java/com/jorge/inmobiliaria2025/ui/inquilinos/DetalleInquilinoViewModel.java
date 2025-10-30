package com.jorge.inmobiliaria2025.ui.inquilinos;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.LiveData;

import com.jorge.inmobiliaria2025.Retrofit.ApiService;
import com.jorge.inmobiliaria2025.Retrofit.RetrofitClient;
import com.jorge.inmobiliaria2025.localdata.SessionManager;
import com.jorge.inmobiliaria2025.model.InquilinoConInmueble;
import com.jorge.inmobiliaria2025.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetalleInquilinoViewModel extends AndroidViewModel {

    private final MutableLiveData<String> nombreCompleto = new MutableLiveData<>();
    private final MutableLiveData<String> dni = new MutableLiveData<>();
    private final MutableLiveData<String> telefono = new MutableLiveData<>();
    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<String> urlImagen = new MutableLiveData<>();

    public DetalleInquilinoViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<String> getNombreCompleto() { return nombreCompleto; }
    public LiveData<String> getDni() { return dni; }
    public LiveData<String> getTelefono() { return telefono; }
    public LiveData<String> getEmail() { return email; }
    public LiveData<String> getUrlImagen() { return urlImagen; }

    public void recibirId(Bundle args) {
        if (args == null) return;
        int id = args.getInt("idInquilino", -1);
        if (id != -1) cargarInquilino(id);
    }

    private void cargarInquilino(int id) {
        ApiService api = RetrofitClient.getInstance(getApplication()).create(ApiService.class);
        String token = SessionManager.getInstance(getApplication()).obtenerToken();

        api.getInquilinoById("Bearer " + token, id)
                .enqueue(new Callback<InquilinoConInmueble>() {
                    @Override
                    public void onResponse(Call<InquilinoConInmueble> call, Response<InquilinoConInmueble> response) {
                        InquilinoConInmueble i = response.body();

                        if (i == null) {
                            mostrarVacio();
                            return;
                        }

                        nombreCompleto.setValue(i.getNombre() + " " + i.getApellido());
                        dni.setValue(i.getDni());
                        telefono.setValue(i.getTelefono());
                        email.setValue(i.getEmail());

                        String url = i.getImagenUrlInmueble();
                        if (url == null || url.isEmpty()) {
                            urlImagen.setValue(String.valueOf(R.drawable.ic_image_placeholder));
                        } else if (!url.startsWith("http")) {
                            urlImagen.setValue(RetrofitClient.BASE_URL + url.replaceFirst("^/", ""));
                        } else {
                            urlImagen.setValue(url);
                        }
                    }

                    @Override
                    public void onFailure(Call<InquilinoConInmueble> call, Throwable t) {
                        mostrarVacio();
                    }
                });
    }

    private void mostrarVacio() {
        nombreCompleto.setValue("Sin datos");
        dni.setValue("-");
        telefono.setValue("-");
        email.setValue("-");
        urlImagen.setValue(String.valueOf(R.drawable.ic_image_placeholder));
    }
}
