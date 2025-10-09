package com.jorge.inmobiliaria2025.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jorge.inmobiliaria2025.InmobiliariaApp;
import com.jorge.inmobiliaria2025.data.SessionManager;
import com.jorge.inmobiliaria2025.model.Propietario;

public class PerfilViewModel extends AndroidViewModel {

    private final MutableLiveData<String> email = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cerrarSesionEvento = new MutableLiveData<>();
    private final MutableLiveData<Propietario> propietario = new MutableLiveData<>();

    private final SessionManager sessionManager;

    public PerfilViewModel(@NonNull Application app) {
        super(app);
        sessionManager = new SessionManager(app);
        cargarEmail();
        cargarPropietario();
    }

    // 🔹 Exponer el email como LiveData
    public LiveData<String> getEmail() {
        return email;
    }

    // 🔹 Exponer el evento de cierre de sesión
    public LiveData<Boolean> getCerrarSesionEvento() {
        return cerrarSesionEvento;
    }

    // 🔹 Nuevo: Exponer el propietario completo
    public LiveData<Propietario> getPropietario() {
        return propietario;
    }

    // 🔹 Cargar el email
    public void cargarEmail() {
        String guardado = InmobiliariaApp.getInstance().obtenerEmail();
        email.setValue(guardado != null ? guardado : "No hay sesión activa");
    }

    // 🔹 Cierra la sesión y emite el evento
    public void cerrarSesion(Context context) {
        sessionManager.logout();
        InmobiliariaApp.getInstance().cerrarSesion();
        email.setValue("Sesión cerrada");
        cerrarSesionEvento.postValue(true);
    }

    // 🔹 Cargar propietario desde SessionManager o App (según lo guardado al loguear)
    public void cargarPropietario() {
        Propietario p = sessionManager.obtenerPropietarioActual();
        if (p == null) {
            p = new Propietario(); // vacío por si no hay sesión aún
        }
        propietario.setValue(p);
    }

    // 🔹 Actualizar datos del propietario localmente
    public void actualizarPropietario(Propietario p) {
        // Guardar localmente (puede sincronizarse con backend después)
        sessionManager.guardarPropietario(p);
        propietario.setValue(p);
        email.setValue(p.getEmail());
    }
}
