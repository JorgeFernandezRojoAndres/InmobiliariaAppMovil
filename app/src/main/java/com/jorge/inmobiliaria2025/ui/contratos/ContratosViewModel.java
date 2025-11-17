package com.jorge.inmobiliaria2025.ui.contratos;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.jorge.inmobiliaria2025.localdata.SessionManager;
import com.jorge.inmobiliaria2025.model.Contrato;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ContratosViewModel extends AndroidViewModel {

    private static final String TAG = "CONTRATOS_VM";

    private final MutableLiveData<List<Contrato>> contratos = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Bundle> accionNavegarADetalle = new MutableLiveData<>();
    private final MutableLiveData<String> mensajeRenovacion = new MutableLiveData<>();
    private final MutableLiveData<Boolean> volverAlMapaEvent = new MutableLiveData<>();

    private final ContratoRepository repo;
    private final SessionManager sessionManager;

    // 🔒 Observer almacenado para poder removerlo en onCleared()
    private final Observer<List<Contrato>> observerRepoContratos = lista -> {
        if (lista == null) {
            contratos.postValue(Collections.emptyList());
        } else {
            List<Contrato> copia = new ArrayList<>(lista);

            // Formatear las fechas antes de agregar a la lista
            for (Contrato contrato : copia) {
                SimpleDateFormat sdfEntrada = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date fechaInicio = null;
                Date fechaFin = null;

                try {
                    fechaInicio = sdfEntrada.parse(contrato.getFechaInicio());
                    fechaFin = sdfEntrada.parse(contrato.getFechaFin());
                } catch (ParseException e) {
                    Log.e(TAG, "Error al parsear fechas", e);
                }

                contrato.setFechaInicio(formatearFechaParaMostrar(fechaInicio));
                contrato.setFechaFin(formatearFechaParaMostrar(fechaFin));
            }

            // Ordenar: primero por estado, luego por fecha descendente
            copia.sort((c1, c2) -> {
                int estadoCompare = c1.getEstado().compareToIgnoreCase(c2.getEstado());
                if (estadoCompare != 0) return estadoCompare;
                return c2.getFechaInicio().compareToIgnoreCase(c1.getFechaInicio());
            });

            contratos.postValue(copia);
        }
    };

    public ContratosViewModel(@NonNull Application app) {
        super(app);
        sessionManager = SessionManager.getInstance(getApplication());
        repo = new ContratoRepository(getApplication());

        // 🧩 Observa el LiveData del repositorio con observer removible
        repo.getContratosLiveData().observeForever(observerRepoContratos);
    }

    // 👉 llamado por el Fragment en onDestroyView()
    public void limpiarObservers() {
        try {
            repo.getContratosLiveData().removeObserver(observerRepoContratos);
            Log.d(TAG, "🧹 Observers del repo limpiados correctamente");
        } catch (Exception e) {
            Log.w(TAG, "No se pudieron limpiar observers: " + e.getMessage());
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        limpiarObservers(); // por seguridad adicional
    }

    public LiveData<List<Contrato>> getContratos() {
        return contratos;
    }

    public LiveData<Bundle> getAccionNavegarADetalle() {
        return accionNavegarADetalle;
    }

    public LiveData<Boolean> getVolverAlMapaEvent() {
        return volverAlMapaEvent;
    }

    public void onVolverAlMapa() {
        volverAlMapaEvent.setValue(true);
    }

    public void cargarContratos() {
        String token = sessionManager.obtenerToken();
        if (token == null || token.isEmpty()) {
            contratos.postValue(Collections.emptyList());
            return;
        }
        repo.cargarContratosTodos();
    }

    public void onContratoSeleccionado(Contrato contrato) {
        if (contrato == null) return;
        Bundle bundle = new Bundle();
        bundle.putSerializable("contratoSeleccionado", contrato);
        navegarADetalle(bundle);
    }

    public void navegarADetalle(Bundle args) {
        if (args == null) {
            Log.w(TAG, "⚠️ Navegación ignorada: args == null");
            return;
        }
        accionNavegarADetalle.postValue(args);
    }

    // ✅ Renovar contrato (con conversión de Date + BigDecimal)
    public void renovarContrato(int idContrato, Date inicio, Date fin, BigDecimal monto) {
        String token = sessionManager.obtenerToken();
        if (token == null || token.isEmpty()) {
            mensajeRenovacion.postValue("Sesión expirada");
            return;
        }

        String inicioFormateado = formatearFechaParaMostrar(inicio);
        String finFormateado = formatearFechaParaMostrar(fin);

        SimpleDateFormat sdfRepo = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String inicioStr = sdfRepo.format(inicio);
        String finStr = sdfRepo.format(fin);
        String montoStr = monto.toPlainString();

        repo.renovarContrato(idContrato, inicioStr, finStr, montoStr, new ContratoRepository.CallbackRenovar() {
            @Override
            public void onSuccess(String mensaje) {
                mensajeRenovacion.postValue(mensaje != null ? mensaje : "Contrato renovado correctamente");
                cargarContratos();
            }

            @Override
            public void onError(String mensaje) {
                mensajeRenovacion.postValue(mensaje);
            }
        });

        Log.d(TAG, "Contrato renovado: Fecha inicio: " + inicioFormateado + ", Fecha fin: " + finFormateado);
    }

    // Método para convertir la fecha en formato DD/MM/YYYY
    public String formatearFechaParaMostrar(Date fecha) {
        if (fecha == null) {
            return "N/A";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return sdf.format(fecha);
    }
}
