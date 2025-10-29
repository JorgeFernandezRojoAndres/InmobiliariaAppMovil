package com.jorge.inmobiliaria2025.ui.contratos;

import android.app.Application;
import android.os.Bundle;

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

    // ✅ LiveData de contratos y navegación
    private final MutableLiveData<List<Contrato>> contratos = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Bundle> accionNavegarADetalle = new MutableLiveData<>();

    // ✅ Repositorio y sesión
    private final ContratoRepository repo;
    private final SessionManager sessionManager;

    public ContratosViewModel(@NonNull Application app) {
        super(app);
        sessionManager = new SessionManager(getApplication());
        repo = new ContratoRepository(getApplication());

        // 🔹 Sincronizar con el LiveData del repo
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

    // ================================
    // 🔹 Lógica de carga desde el repo
    // ================================
    public void cargarContratos() {
        String token = sessionManager.obtenerToken();
        if (token != null && !token.isEmpty()) {
            repo.cargarContratosVigentes(token);
        } else {
            contratos.postValue(Collections.emptyList());
        }
    }

    // ================================
    // 🔹 Manejo de selección de contrato
    // ================================
    public void onContratoSeleccionado(Contrato contrato) {
        if (contrato == null) return;

        Bundle bundle = new Bundle();
        bundle.putSerializable("contratoSeleccionado", contrato);
        accionNavegarADetalle.setValue(bundle);

        // 🧹 Limpieza automática del evento
        accionNavegarADetalle.postValue(null);
    }
}
