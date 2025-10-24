package com.jorge.inmobiliaria2025.ui.nav;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * 🧭 NavViewModel
 * ViewModel global que centraliza las órdenes de navegación.
 * Permite que cualquier otro ViewModel emita un destino
 * sin agregar lógica de navegación en los fragments.
 */
public class NavViewModel extends AndroidViewModel {

    // 🔹 Evento de navegación (detalle de inmueble, u otros)
    private final MutableLiveData<Bundle> _accionNavegarDetalle = new MutableLiveData<>();

    public NavViewModel(@NonNull Application app) {
        super(app);
    }

    /** 👁️ LiveData observado por MainActivity */
    public LiveData<Bundle> getAccionNavegarDetalle() {
        return _accionNavegarDetalle;
    }

    /** 🚀 Emite un evento de navegación hacia el detalle de inmueble */
    public void navegarADetalle(Bundle args) {
        _accionNavegarDetalle.postValue(args);
    }

    /** 🧹 Limpia el evento luego de usarlo (para evitar repetición en rotaciones) */
    public void limpiarAccion() {
        _accionNavegarDetalle.setValue(null);
    }
}
