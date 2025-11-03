package com.jorge.inmobiliaria2025.ui.contratos;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.localdata.SessionManager;
import com.jorge.inmobiliaria2025.model.Contrato;

import java.math.BigDecimal;
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

    // ✅ LiveData para mostrar mensaje de renovación
    private final MutableLiveData<String> mensajeRenovacion = new MutableLiveData<>();
    public LiveData<String> getMensajeRenovacion() { return mensajeRenovacion; }

    private final ContratoRepository repo;
    private final SessionManager sessionManager;
    private final MutableLiveData<Boolean> volverAlMapaEvent = new MutableLiveData<>();
    public LiveData<Boolean> getVolverAlMapaEvent() { return volverAlMapaEvent; }
    public void onVolverAlMapa() {
        volverAlMapaEvent.setValue(true);
    }

    public ContratosViewModel(@NonNull Application app) {
        super(app);
        sessionManager = SessionManager.getInstance(getApplication());
        repo = new ContratoRepository(getApplication());

        repo.getContratosLiveData().observeForever(lista -> {
            if (lista == null) {
                contratos.postValue(Collections.emptyList());
            } else {
                List<Contrato> copia = new ArrayList<>(lista);

                copia.sort((c1, c2) -> {
                    int estadoCompare = c1.getEstado().compareToIgnoreCase(c2.getEstado());
                    if (estadoCompare != 0) return estadoCompare;
                    return c2.getFechaInicio().compareToIgnoreCase(c1.getFechaInicio());
                });

                contratos.postValue(copia);
            }
        });
    }

    public LiveData<List<Contrato>> getContratos() {
        return contratos;
    }

    public LiveData<Bundle> getAccionNavegarADetalle() {
        return accionNavegarADetalle;
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

        // ✅ Convertir fechas y monto antes de llamar al repo
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String inicioStr = sdf.format(inicio);
        String finStr = sdf.format(fin);
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

    }
}
