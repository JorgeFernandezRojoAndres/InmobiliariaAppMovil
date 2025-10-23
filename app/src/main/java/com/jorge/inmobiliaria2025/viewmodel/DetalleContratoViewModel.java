package com.jorge.inmobiliaria2025.viewmodel;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.model.Contrato;

public class DetalleContratoViewModel extends AndroidViewModel {

    private final MutableLiveData<Contrato> contrato = new MutableLiveData<>();
    private final MutableLiveData<Bundle> accionNavegarAPagos = new MutableLiveData<>();

    public DetalleContratoViewModel(@NonNull Application app) {
        super(app);
    }

    // =============================
    // 🔹 Observables públicos
    // =============================
    public LiveData<Contrato> getContrato() {
        return contrato;
    }

    public LiveData<Bundle> getAccionNavegarAPagos() {
        return accionNavegarAPagos;
    }

    // =============================
    // 🔹 Inicialización desde argumentos
    // =============================
    public void inicializarDesdeArgs(Bundle args) {
        if (args == null) return;
        Contrato recibido = (Contrato) args.getSerializable("contratoSeleccionado");
        contrato.postValue(recibido);
    }

    // =============================
    // 🔹 Acción: navegar a pagos
    // =============================
    public void onVerPagosClick() {
        Contrato actual = contrato.getValue();
        if (actual == null) return;

        Bundle bundle = new Bundle();
        bundle.putSerializable("contratoSeleccionado", actual);
        accionNavegarAPagos.postValue(bundle);
    }
}
