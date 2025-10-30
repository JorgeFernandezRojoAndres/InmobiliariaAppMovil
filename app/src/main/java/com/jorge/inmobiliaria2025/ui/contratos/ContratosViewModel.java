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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContratosViewModel extends AndroidViewModel {

    private static final String TAG = "CONTRATOS_VM";
    private final MutableLiveData<List<Contrato>> contratos = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Bundle> accionNavegarADetalle = new MutableLiveData<>();
    private final ContratoRepository repo;
    private final SessionManager sessionManager;

    public ContratosViewModel(@NonNull Application app) {
        super(app);
        sessionManager = SessionManager.getInstance(getApplication());

        repo = new ContratoRepository(getApplication());

        repo.getContratosLiveData().observeForever(lista -> {
            if (lista == null)
                contratos.postValue(Collections.emptyList());
            else
                contratos.postValue(lista);
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
        if (token != null && !token.isEmpty()) {
            repo.cargarContratosVigentes(token);
        } else {
            contratos.postValue(Collections.emptyList());
        }
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
}
