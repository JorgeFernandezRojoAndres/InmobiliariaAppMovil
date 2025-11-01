package com.jorge.inmobiliaria2025.ui.contratos;


import android.util.Log;
import android.app.Application;
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

    public ContratoRepository(Application app) {
        api = RetrofitClient.getInstance(app).create(ApiService.class);
    }

    public LiveData<List<Contrato>> getContratosLiveData() {
        return contratosLiveData;
    }

    // 🔹 Carga los contratos vigentes del propietario autenticado
    public void cargarContratosVigentes() {
        Log.i(TAG, "📡 Solicitando contratos vigentes al backend...");

        api.getContratosVigentes().enqueue(new Callback<List<Contrato>>() {
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

    // -------------------- ⚖️ RESCISIÓN DE CONTRATO --------------------
    public void rescindirContrato(int idContrato, MutableLiveData<String> resultado) {
        Log.i(TAG, "📡 Enviando solicitud de rescisión para contrato ID=" + idContrato);

        api.rescindirContrato(idContrato).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                int code = response.code();
                Log.i(TAG, "📨 Respuesta HTTP: " + code);

                if (response.isSuccessful()) {
                    try {
                        String msg = response.body() != null ? response.body().string() : "✅ Contrato rescindido correctamente.";
                        Log.i(TAG, "✅ Contrato rescindido correctamente (ID=" + idContrato + ") → " + msg);
                        resultado.postValue(msg);
                    } catch (IOException e) {
                        Log.e(TAG, "⚠️ Error al leer cuerpo de respuesta: " + e.getMessage());
                        resultado.postValue("Error leyendo respuesta del servidor.");
                    }
                } else {
                    try {
                        String errorMsg = response.errorBody() != null
                                ? response.errorBody().string()
                                : "(sin cuerpo)";
                        Log.e(TAG, "🔴 Error HTTP " + code + ": " + errorMsg);
                        resultado.postValue("Error " + code + ": " + errorMsg);
                    } catch (IOException e) {
                        Log.e(TAG, "⚠️ Error leyendo errorBody: " + e.getMessage());
                        resultado.postValue("Error HTTP " + code + " (sin cuerpo legible)");
                    }
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Log.e(TAG, "❌ Falló la conexión al intentar rescindir contrato: " + t.getMessage(), t);
                resultado.postValue("Error de conexión: " + t.getMessage());
            }
        });
    }


}
