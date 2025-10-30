package com.jorge.inmobiliaria2025.ui.pagos;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.Retrofit.ApiService;
import com.jorge.inmobiliaria2025.Retrofit.RetrofitClient;
import com.jorge.inmobiliaria2025.localdata.SessionManager;
import com.jorge.inmobiliaria2025.model.Contrato;
import com.jorge.inmobiliaria2025.model.Pago;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PagosViewModel extends AndroidViewModel {

    private static final String TAG = "PagosVM";

    // ===============================
    // 🔹 Estado UI inmutable (mensaje + visibilidades + adapter)
    // ===============================
    public static class UiState {
        private final String mensaje;
        private final int visibilidadMensaje;
        private final int visibilidadLista;
        private final PagosAdapter adapter;

        public UiState(String mensaje, int visibilidadMensaje, int visibilidadLista, PagosAdapter adapter) {
            this.mensaje = mensaje;
            this.visibilidadMensaje = visibilidadMensaje;
            this.visibilidadLista = visibilidadLista;
            this.adapter = adapter;
        }

        public String getMensaje() { return mensaje; }
        @SuppressWarnings("WrongConstant")
        public int getVisibilidadMensaje() { return visibilidadMensaje; }
        @SuppressWarnings("WrongConstant")
        public int getVisibilidadLista() { return visibilidadLista; }
        public PagosAdapter getAdapter() { return adapter; }
    }

    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();

    public PagosViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<UiState> getUiState() {
        return uiState;
    }

    // ============================================================
    // 🔹 Inicialización flexible: contratoSeleccionado o contratoId
    // ============================================================
    public void inicializar(Context ctx, Bundle args) {
        if (args == null) {
            Log.w(TAG, "⚠️ inicializar(): args == null");
            mostrarMensaje("No se recibió ningún contrato o ID válido.");
            return;
        }

        Log.d(TAG, "🧩 Args recibidos en inicializar(): " + args.keySet());

        Integer contratoId = null;

        try {
            // 1️⃣ Caso: viene el objeto completo "contratoSeleccionado"
            if (args.containsKey("contratoSeleccionado")) {
                Contrato contrato = (Contrato) args.getSerializable("contratoSeleccionado");
                if (contrato != null && contrato.getId() > 0) {
                    contratoId = contrato.getId();
                    Log.d(TAG, "✅ Contrato recibido vía Serializable con ID=" + contratoId);
                } else {
                    Log.w(TAG, "⚠️ contratoSeleccionado es null o inválido");
                }
            }

            // 2️⃣ Caso alternativo: solo viene el ID
            if (contratoId == null && args.containsKey("contratoId")) {
                int idBundle = args.getInt("contratoId", -1);
                if (idBundle > 0) {
                    contratoId = idBundle;
                    Log.d(TAG, "✅ contratoId recibido directamente = " + contratoId);
                } else {
                    Log.w(TAG, "⚠️ contratoId inválido: " + idBundle);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Error al leer argumentos: " + e.getMessage(), e);
        }

        // Validar ID final
        if (contratoId == null || contratoId <= 0) {
            Log.w(TAG, "⚠️ No se encontró un contrato o ID válido.");
            mostrarMensaje("No se recibió ningún contrato o ID válido.");
            return;
        }

        Log.i(TAG, "🚀 Iniciando carga de pagos para contrato ID=" + contratoId);
        cargarPagos(ctx, contratoId);
    }

    // ===============================
    // 🔹 Llamada a la API
    // ===============================
    private void cargarPagos(Context context, int idContrato) {
        SessionManager session = SessionManager.getInstance(context);

        String token = session.obtenerToken();

        if (token == null || token.isEmpty()) {
            Log.w(TAG, "⚠️ Token no disponible. No se puede cargar pagos.");
            mostrarMensaje("⚠️ Sesión expirada. Inicie sesión nuevamente.");
            return;
        }

        ApiService api = RetrofitClient.getInstance(context).create(ApiService.class);
        Log.d(TAG, "🌐 Enviando request a getPagosPorContrato con ID=" + idContrato);

        api.getPagosPorContrato("Bearer " + token, idContrato)
                .enqueue(new Callback<List<Pago>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Pago>> call, @NonNull Response<List<Pago>> response) {
                        Log.d(TAG, "📡 Respuesta HTTP: " + response.code());

                        if (response.isSuccessful() && response.body() != null) {
                            List<Pago> pagos = response.body();
                            Log.d(TAG, "✅ Pagos recibidos: " + pagos.size());

                            if (!pagos.isEmpty()) {
                                PagosAdapter adapter = new PagosAdapter(context, pagos);
                                uiState.postValue(new UiState(
                                        "",
                                        View.GONE,
                                        View.VISIBLE,
                                        adapter
                                ));
                            } else {
                                Log.w(TAG, "⚠️ El contrato no tiene pagos registrados.");
                                mostrarMensaje("No se encontraron pagos registrados.");
                            }
                        } else {
                            Log.e(TAG, "❌ Error en respuesta: " + response.code());
                            mostrarMensaje("Error al obtener pagos del servidor (" + response.code() + ").");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Pago>> call, @NonNull Throwable t) {
                        Log.e(TAG, "💥 Error de red al cargar pagos: " + t.getMessage());
                        mostrarMensaje("Error de conexión al obtener pagos.");
                    }
                });
    }

    // ===============================
    // 🔹 Mostrar mensaje de estado
    // ===============================
    private void mostrarMensaje(String msg) {
        uiState.postValue(new UiState(
                msg,
                View.VISIBLE,
                View.GONE,
                null
        ));
    }
}
