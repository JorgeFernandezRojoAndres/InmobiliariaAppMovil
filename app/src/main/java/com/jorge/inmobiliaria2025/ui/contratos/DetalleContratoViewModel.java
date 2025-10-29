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

public class DetalleContratoViewModel extends AndroidViewModel {

    private static final String TAG = "DetalleContratoVM";

    // 🔹 LiveData principales
    private final MutableLiveData<Contrato> contrato = new MutableLiveData<>();
    private final MutableLiveData<Integer> contratoId = new MutableLiveData<>();
    private final MutableLiveData<Bundle> accionNavegarAPagos = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navegandoAPagos = new MutableLiveData<>(false);

    // 🔹 Constructor
    public DetalleContratoViewModel(@NonNull Application app) {
        super(app);
        Log.d(TAG, "🧩 ViewModel creado");
    }

    // =============================
    // 🔹 Getters públicos observables
    // =============================
    public LiveData<Contrato> getContrato() { return contrato; }
    public LiveData<Bundle> getNavegarAPagos() { return accionNavegarAPagos; }
    public LiveData<Integer> getContratoId() { return contratoId; }

    // =============================
    // 🔹 Inicialización desde argumentos
    // =============================
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
            Log.w(TAG, "⚠️ 'contratoSeleccionado' no es instancia de Contrato");
        }

        if (args.containsKey("contratoId")) {
            int id = args.getInt("contratoId", -1);
            if (id != -1) {
                contratoId.postValue(id);
                Log.d(TAG, "📦 contratoId recibido directamente: " + id);
                cargarContratoPorId(id);
                return;
            }
            Log.w(TAG, "⚠️ contratoId inválido (-1)");
        }

        Log.w(TAG, "⚠️ No se encontró ni contratoSeleccionado ni contratoId en args");
    }

    // =============================
    // 🔹 Acción: navegar a pagos
    // =============================
    public void onVerPagosClick() {
        // único if permitido: evita doble click rápido
        if (Boolean.TRUE.equals(navegandoAPagos.getValue())) return;

        navegandoAPagos.postValue(true);
        Bundle bundle = new Bundle();

        Contrato actual = contrato.getValue();
        Integer id = contratoId.getValue();

        if (actual != null) {
            bundle.putSerializable("contratoSeleccionado", actual);
            bundle.putInt("contratoId", actual.getId());
            Log.d(TAG, "📦 Navegando con contrato completo (ID=" + actual.getId() + ")");
        } else if (id != null) {
            bundle.putInt("contratoId", id);
            Log.d(TAG, "📦 Navegando solo con contratoId=" + id);
        } else {
            Log.w(TAG, "⚠️ No hay datos de contrato para navegar a pagos");
        }

        accionNavegarAPagos.postValue(bundle);

        // Reset del flag después de 400 ms
        new Handler(Looper.getMainLooper()).postDelayed(
                () -> navegandoAPagos.postValue(false),
                400
        );
    }

    // =============================
    // 🔹 Limpieza del evento de navegación
    // =============================
    public void limpiarAccionNavegar() {
        accionNavegarAPagos.setValue(null);
    }

    // =============================
    // 🔹 Carga de contrato (privado - Retrofit)
    // =============================
    private void cargarContratoPorId(int id) {
        Log.d(TAG, "📡 cargarContratoPorId() llamado con ID=" + id);
        // TODO: implementar llamada Retrofit y luego:
        // contrato.postValue(contratoObtenido);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "🧩 ViewModel destruido (onCleared)");
    }
}
