package com.jorge.inmobiliaria2025.viewmodel;

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

    private final MutableLiveData<Bundle> navToDetalle = new MutableLiveData<>();

    public NavViewModel(@NonNull Application app) {
        super(app);
    }

    // 🔹 LiveData observado por la MainActivity
    public LiveData<Bundle> getNavToDetalle() {
        return navToDetalle;
    }

    // 🔹 Emite un evento de navegación hacia el detalle de inmueble
    public void navegarADetalle(Bundle args) {
        navToDetalle.postValue(args);
    }
}
