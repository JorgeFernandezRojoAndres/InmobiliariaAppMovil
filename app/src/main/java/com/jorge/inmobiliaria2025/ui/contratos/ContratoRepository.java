package com.jorge.inmobiliaria2025.ui.contratos;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.Retrofit.ApiService;
import com.jorge.inmobiliaria2025.Retrofit.RetrofitClient;
import com.jorge.inmobiliaria2025.model.Contrato;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContratoRepository {

    private static final String TAG = "ContratoRepo";
    private final ApiService api;
    private final MutableLiveData<List<Contrato>> contratosLiveData = new MutableLiveData<>();

    public ContratoRepository(Context context) {
        api = RetrofitClient.getInstance(context).create(ApiService.class);
    }

    public LiveData<List<Contrato>> getContratosLiveData() {
        return contratosLiveData;
    }

    // 🔹 Carga los contratos vigentes del propietario autenticado
    public void cargarContratosVigentes(String token) {
        Log.i(TAG, "📡 Solicitando contratos vigentes al backend...");

        api.getContratosVigentes("Bearer " + token).enqueue(new Callback<List<Contrato>>() {
            @Override
            public void onResponse(Call<List<Contrato>> call, Response<List<Contrato>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Contrato> contratos = response.body();
                    contratosLiveData.postValue(contratos);

                    Log.i(TAG, "✅ Contratos recibidos: " + contratos.size());
                    for (Contrato c : contratos) {
                        Log.v(TAG, "📝 Contrato ID=" + c.getId() +
                                " | Direccion=" + (c.getInmueble() != null ? c.getInmueble().getDireccion() : "null") +
                                " | Estado=" + c.getEstado());
                    }

                } else {
                    int code = response.code();
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            // 🔍 Intentamos parsear el JSON devuelto por el backend
                            try {
                                JSONObject json = new JSONObject(errorBody);
                                String mensaje = json.optString("mensaje", "");
                                String detalle = json.optString("detalle", "");
                                Log.e(TAG, "💥 Error backend (" + code + "): " + mensaje + " | Detalle: " + detalle);
                            } catch (JSONException je) {
                                Log.e(TAG, "⚠️ Error backend (" + code + "): cuerpo no JSON -> " + errorBody);
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "⚠️ No se pudo leer el cuerpo del error: " + e.getMessage());
                    }

                    contratosLiveData.postValue(Collections.emptyList());
                }
            }

            @Override
            public void onFailure(Call<List<Contrato>> call, Throwable t) {
                Log.e(TAG, "❌ Error al conectar con el servidor: " + t.getMessage(), t);
                contratosLiveData.postValue(Collections.emptyList());
            }
        });
    }
}
