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

    // -------------------------------------------------------------------
    // ✅ Procesar respuesta lista
    // -------------------------------------------------------------------
    private void procesarRespuestaLista(Response<List<Contrato>> response, String tipo) {
        if (response.isSuccessful() && response.body() != null) {
            List<Contrato> contratos = response.body();
            contratosLiveData.postValue(contratos);

            Log.i(TAG, "✅ Contratos (" + tipo + "): " + contratos.size());
            for (Contrato c : contratos) {
                Log.v(TAG, "📝 ID=" + c.getId() +
                        " | Dir=" + (c.getInmueble() != null ? c.getInmueble().getDireccion() : "null") +
                        " | Estado=" + c.getEstado());
            }

        } else {
            int code = response.code();
            String errorBody = "";
            try {
                if (response.errorBody() != null) {
                    errorBody = response.errorBody().string();
                    try {
                        JSONObject json = new JSONObject(errorBody);
                        String mensaje = json.optString("mensaje", "");
                        String detalle = json.optString("detalle", "");
                        Log.e(TAG, "💥 Error backend (" + code + "): " + mensaje + " | " + detalle);
                    } catch (JSONException je) {
                        Log.e(TAG, "⚠️ Backend (" + code + "): cuerpo no JSON -> " + errorBody);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "⚠️ No se pudo leer error: " + e.getMessage());
            }

            contratosLiveData.postValue(Collections.emptyList());
        }
    }

    private void procesarErrorConexion(Throwable t) {
        Log.e(TAG, "❌ Error servidor: " + t.getMessage(), t);
        contratosLiveData.postValue(Collections.emptyList());
    }

    // -------------------------------------------------------------------
    // ✅ Cargar listas desde API
    // -------------------------------------------------------------------
    public void cargarContratosVigentes() {
        Log.i(TAG, "📡 Cargar vigentes...");
        api.getContratosVigentes().enqueue(new Callback<List<Contrato>>() {
            @Override public void onResponse(Call<List<Contrato>> call, Response<List<Contrato>> r) {
                procesarRespuestaLista(r, "vigentes");
            }
            @Override public void onFailure(Call<List<Contrato>> call, Throwable t) {
                procesarErrorConexion(t);
            }
        });
    }

    public void cargarContratosFinalizados() {
        Log.i(TAG, "📡 Cargar finalizados...");
        api.getContratosFinalizados().enqueue(new Callback<List<Contrato>>() {
            @Override public void onResponse(Call<List<Contrato>> call, Response<List<Contrato>> r) {
                procesarRespuestaLista(r, "finalizados");
            }
            @Override public void onFailure(Call<List<Contrato>> call, Throwable t) {
                procesarErrorConexion(t);
            }
        });
    }

    public void cargarContratosTodos() {
        Log.i(TAG, "📡 Cargar todos...");
        api.getContratosTodos().enqueue(new Callback<List<Contrato>>() {
            @Override public void onResponse(Call<List<Contrato>> call, Response<List<Contrato>> r) {
                procesarRespuestaLista(r, "todos");
            }
            @Override public void onFailure(Call<List<Contrato>> call, Throwable t) {
                procesarErrorConexion(t);
            }
        });
    }

    // -------------------------------------------------------------------
    // ✅ Rescindir contrato
    // -------------------------------------------------------------------
    public void rescindirContrato(int idContrato, MutableLiveData<String> resultado) {
        Log.i(TAG, "📡 Rescindir contrato ID=" + idContrato);

        api.rescindirContrato(idContrato).enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> r) {
                if (r.isSuccessful()) {
                    try {
                        String msg = r.body() != null ? r.body().string() : "✅ Rescindido";
                        Log.i(TAG, msg);
                        resultado.postValue(msg);
                    } catch (IOException e) {
                        Log.e(TAG, "⚠️ Error leyendo respuesta", e);
                        resultado.postValue("Error leyendo respuesta");
                    }
                } else {
                    try {
                        String errorMsg = r.errorBody() != null ? r.errorBody().string() : "Error desconocido";
                        Log.e(TAG, "❌ Rescindir: " + errorMsg);
                        resultado.postValue(errorMsg);
                    } catch (IOException e) {
                        resultado.postValue("Error HTTP sin cuerpo legible");
                    }
                }
            }

            @Override public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                Log.e(TAG, "❌ Conexión rescindir", t);
                resultado.postValue("Error de conexión: " + t.getMessage());
            }
        });
    }

    // -------------------------------------------------------------------
    // ✅ Callback para renovación
    // -------------------------------------------------------------------
    public interface CallbackRenovar {
        void onSuccess(String mensaje);
        void onError(String mensaje);
    }

    // -------------------------------------------------------------------
    // ✅ Renovar contrato
    // -------------------------------------------------------------------
    public void renovarContrato(int idContrato, String inicio, String fin, String monto,
                                CallbackRenovar callback) {

        Log.i(TAG, "📡 Renovar contrato ID=" + idContrato);

        api.renovarContrato(idContrato, inicio, fin, monto)
                .enqueue(new Callback<okhttp3.ResponseBody>() {
                    @Override public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> r) {
                        if (r.isSuccessful()) {
                            try {
                                String msg = r.body() != null ? r.body().string() : "OK";
                                Log.i(TAG, "✅ Renovado: " + msg);
                                callback.onSuccess(msg);
                            } catch (IOException e) {
                                callback.onError("Error leyendo respuesta");
                            }
                        } else {
                            try {
                                String errorMsg = r.errorBody() != null ? r.errorBody().string() : "Error desconocido";
                                Log.e(TAG, "❌ Renovar: " + errorMsg);
                                callback.onError(errorMsg);
                            } catch (IOException e) {
                                callback.onError("Error procesando error");
                            }
                        }
                    }

                    @Override public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                        Log.e(TAG, "❌ Conexión renovando", t);
                        callback.onError("Error de conexión: " + t.getMessage());
                    }
                });
    }
}
