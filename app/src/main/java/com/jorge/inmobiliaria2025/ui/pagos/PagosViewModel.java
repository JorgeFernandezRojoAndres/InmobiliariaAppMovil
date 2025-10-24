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

import com.jorge.inmobiliaria2025.localdata.SessionManager;
import com.jorge.inmobiliaria2025.Retrofit.ApiService;
import com.jorge.inmobiliaria2025.Retrofit.RetrofitClient;
import com.jorge.inmobiliaria2025.model.Pago;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class PagosViewModel extends AndroidViewModel {
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
        public int getVisibilidadMensaje() { return visibilidadMensaje; }
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


    // ===============================
    // 🔹 Punto de entrada desde Fragment
    // ===============================
    public void inicializar(Context ctx, Bundle args) {
        Integer idContrato = (args != null) ? args.getInt("idContrato", -1) : -1;

        if (idContrato == -1) {
            mostrarMensaje("No se recibió ningún contrato.");
            return;
        }

        cargarPagos(ctx, idContrato);
    }

    private void cargarPagos(Context context, int idContrato) {
        SessionManager session = new SessionManager(context);
        String token = session.obtenerToken();

        if (token == null || token.isEmpty()) {
            Log.w("PagosVM", "⚠️ Token no disponible. No se puede cargar pagos.");
            mostrarMensaje("⚠️ Sesión expirada. Inicie sesión nuevamente.");
            return;
        }

        Log.d("PagosVM", "🔹 Solicitando pagos para contrato ID=" + idContrato);
        ApiService api = RetrofitClient.getInstance(context).create(ApiService.class);

        api.getPagosPorContrato("Bearer " + token, idContrato).enqueue(new Callback<List<Pago>>() {
            @Override
            public void onResponse(@NonNull Call<List<Pago>> call, @NonNull Response<List<Pago>> response) {
                Log.d("PagosVM", "📡 Respuesta HTTP: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    List<Pago> pagos = response.body();
                    Log.d("PagosVM", "✅ Pagos recibidos: " + pagos.size());

                    if (!pagos.isEmpty()) {
                        PagosAdapter adapter = new PagosAdapter(context, pagos);
                        uiState.postValue(new UiState("", View.GONE, View.VISIBLE, adapter));
                    } else {
                        Log.w("PagosVM", "⚠️ El contrato no tiene pagos.");
                        mostrarMensaje("No se encontraron pagos registrados.");
                    }
                } else {
                    Log.e("PagosVM", "❌ Error en respuesta: " + response.code());
                    mostrarMensaje("Error al obtener pagos del servidor (" + response.code() + ").");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Pago>> call, @NonNull Throwable t) {
                Log.e("PagosVM", "💥 Error de red al cargar pagos: " + t.getMessage());
                mostrarMensaje("Error de conexión al obtener pagos.");
            }
        });
    }


    // ===============================
    // 🔹 Método utilitario interno
    // ===============================
    private void mostrarMensaje(String msg) {
        uiState.postValue(new UiState(msg, View.VISIBLE, View.GONE, null));
    }
}
