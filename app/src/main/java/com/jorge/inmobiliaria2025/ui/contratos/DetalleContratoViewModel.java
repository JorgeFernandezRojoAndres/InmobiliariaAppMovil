package com.jorge.inmobiliaria2025.ui.contratos;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.model.Contrato;

import org.json.JSONException;
import org.json.JSONObject;

public class DetalleContratoViewModel extends AndroidViewModel {

    private static final String TAG = "DetalleContratoVM";

    // -------------------- 🔹 LiveData principales --------------------
    private final MutableLiveData<Contrato> contrato = new MutableLiveData<>();
    private final MutableLiveData<Integer> contratoId = new MutableLiveData<>();
    private final MutableLiveData<Bundle> accionNavegarAPagos = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navegandoAPagos = new MutableLiveData<>(false);
    private String ultimoMensaje; // guarda el último JSON del backend


    public String getUltimoMensaje() {
        return ultimoMensaje;
    }
    // -------------------- 🔹 Constructor --------------------
    public DetalleContratoViewModel(@NonNull Application app) {
        super(app);
        Log.d(TAG, "🧩 ViewModel creado");
    }

    public LiveData<Contrato> getContrato() { return contrato; }
    public LiveData<Bundle> getNavegarAPagos() { return accionNavegarAPagos; }
    public LiveData<Integer> getContratoId() { return contratoId; }

    // ================================
    // 🔹 Inicialización desde argumentos
    // ================================
    public void inicializarDesdeArgs(Bundle args) {
        if (args == null || args.isEmpty()) {
            Log.w(TAG, "⚠️ inicializarDesdeArgs(): args nulos o vacíos");
            return;
        }

        if (args.containsKey("contratoSeleccionado")) {
            Object obj = args.getSerializable("contratoSeleccionado");
            if (obj instanceof Contrato) {
                Contrato recibido = (Contrato) obj;
                contrato.postValue(recibido);
                contratoId.postValue(recibido.getId());
                Log.d(TAG, "✅ Contrato inicializado con ID=" + recibido.getId());
                return;
            }
        }

        if (args.containsKey("contratoId")) {
            int id = args.getInt("contratoId", -1);
            if (id != -1) {
                contratoId.postValue(id);
                Log.d(TAG, "📦 contratoId recibido directamente: " + id);
                cargarContratoPorId(id);
            }
        }
    }

    // ================================
    // 🔹 Navegación a Pagos
    // ================================
    public void onVerPagosClick() {
        if (Boolean.TRUE.equals(navegandoAPagos.getValue())) return;

        navegandoAPagos.postValue(true);
        Bundle bundle = new Bundle();

        Contrato actual = contrato.getValue();
        Integer id = contratoId.getValue();

        if (actual != null) {
            bundle.putSerializable("contratoSeleccionado", actual);
            bundle.putInt("contratoId", actual.getId());
        } else if (id != null) {
            bundle.putInt("contratoId", id);
        }

        accionNavegarAPagos.postValue(bundle);
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> navegandoAPagos.postValue(false),
                400
        );
    }

    public void limpiarAccionNavegar() {
        accionNavegarAPagos.setValue(null);
    }

    private void cargarContratoPorId(int id) {
        Log.d(TAG, "📡 cargarContratoPorId() llamado con ID=" + id);
        // TODO: implementar Retrofit
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "🧩 ViewModel destruido (onCleared)");
    }

    // ================================
    // 🔹 RESCISIÓN DE CONTRATO
    // ================================
    private final MutableLiveData<UiAccion> uiAccion = new MutableLiveData<>();
    private final ContratoRepository repo = new ContratoRepository(getApplication());

    public LiveData<UiAccion> getUiAccion() {
        return uiAccion;
    }

    public enum UiAccion {
        MOSTRAR_DIALOGO_CONFIRMACION,
        MOSTRAR_MENSAJE_EXITO,
        MOSTRAR_MENSAJE_ERROR,
        VOLVER_A_CONTRATOS
    }

    // ✅ Paso 1: el usuario toca el botón
    public void onRescindirClick() {
        uiAccion.postValue(UiAccion.MOSTRAR_DIALOGO_CONFIRMACION);
    }

    // ✅ Paso 2: se confirma desde el diálogo
    public void confirmarRescision() {
        Contrato actual = contrato.getValue();
        if (actual == null) {
            uiAccion.postValue(UiAccion.MOSTRAR_MENSAJE_ERROR);
            return;
        }

        Log.i(TAG, "📡 Solicitando rescisión de contrato ID=" + actual.getId());

        // ✅ Ya no pasamos token, solo el ID y LiveData de respuesta
        repo.rescindirContrato(actual.getId(), new MutableLiveData<String>() {

            @Override
            public void postValue(String mensaje) {
                super.postValue(mensaje);

                ultimoMensaje = mensaje;
                Log.e(TAG, "🧩 Mensaje backend: " + mensaje);

                if (mensaje != null && mensaje.contains("multa")) {
                    try {
                        JSONObject json = new JSONObject(mensaje);
                        String texto = json.optString("mensaje", "Contrato rescindido correctamente.");
                        String multa = json.optString("multa", null);

                        if (multa != null && !multa.isEmpty()) {
                            uiAccion.postValue(UiAccion.MOSTRAR_MENSAJE_EXITO);
                            Log.i(TAG, "💰 Multa: $" + multa);
                        } else {
                            uiAccion.postValue(UiAccion.MOSTRAR_MENSAJE_EXITO);
                        }

                        new Handler(Looper.getMainLooper()).postDelayed(() ->
                                uiAccion.postValue(UiAccion.VOLVER_A_CONTRATOS), 1500);

                    } catch (JSONException e) {
                        Log.e(TAG, "⚠️ Error JSON: " + e.getMessage());
                        uiAccion.postValue(UiAccion.MOSTRAR_MENSAJE_EXITO);
                    }
                } else if (mensaje != null && mensaje.toLowerCase().contains("correctamente")) {
                    uiAccion.postValue(UiAccion.MOSTRAR_MENSAJE_EXITO);
                    new Handler(Looper.getMainLooper()).postDelayed(() ->
                            uiAccion.postValue(UiAccion.VOLVER_A_CONTRATOS), 1500);
                } else {
                    uiAccion.postValue(UiAccion.MOSTRAR_MENSAJE_ERROR);
                }
            }
        });
    }

}
