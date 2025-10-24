package com.jorge.inmobiliaria2025.ui.nav;

import android.app.Application;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.jorge.inmobiliaria2025.InmobiliariaApp;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.localdata.SessionManager;
import com.jorge.inmobiliaria2025.model.Propietario;

/**
 * 🧠 MainViewModel
 * ViewModel global de sesión y encabezado del Drawer.
 * Controla el estado de login, los datos del propietario
 * y actualiza el avatar sin lógica en la Activity ni Fragments.
 */
public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<Propietario> _propietarioHeader = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _navegarLogin = new MutableLiveData<>();

    public MainViewModel(@NonNull Application app) {
        super(app);
        verificarSesion();
    }

    /** 🔹 Comprueba si hay sesión activa y actualiza los LiveData */
    private void verificarSesion() {
        SessionManager sm = new SessionManager(getApplication());
        Propietario p = sm.obtenerPropietarioActual();

        if (p == null) {
            p = new Propietario();
            p.setEmail(InmobiliariaApp.getInstance().obtenerEmail());
            _navegarLogin.postValue(true);
        } else {
            _navegarLogin.postValue(false);
        }

        _propietarioHeader.postValue(p);
        actualizarAvatar(p);
    }

    /** 🔹 Método público por si se necesita refrescar sesión luego del login */
    public void refrescarSesion() {
        verificarSesion();
    }

    /** 🔹 Permite actualizar el header desde cualquier Fragment */
    public void actualizarHeader(Propietario propietario) {
        if (propietario != null) {
            _propietarioHeader.postValue(propietario);
        }
    }

    /** 🔹 Publicación inmutable para la vista */
    public LiveData<Propietario> getPropietarioHeader() {
        return _propietarioHeader;
    }

    public LiveData<Boolean> getNavegarLogin() {
        return _navegarLogin;
    }
    public Object getAvatarUrl(Propietario propietario) {
        String url = propietario.getAvatarUrl();
        if (url == null || url.isEmpty()) {
            return R.drawable.ic_person; // recurso local
        }
        return url; // URL remota
    }

    /** 🔹 Carga del avatar (sin lógica en la Activity) */
    public void cargarAvatarEn(ImageView imageView, Propietario propietario) {
        try {
            SessionManager sm = new SessionManager(getApplication());
            String url = sm.getAvatarFullUrl(propietario.getAvatarUrl());

            Glide.with(getApplication())
                    .load((url != null && !url.isEmpty()) ? url : R.drawable.ic_person)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(imageView);
        } catch (Exception e) {
            imageView.setImageResource(R.drawable.ic_person);
        }
    }
    // ✅ Nuevo método reemplazando getAvatarUrl y cargarAvatarEn
    private final MutableLiveData<Object> _avatarUrl = new MutableLiveData<>();
    public LiveData<Object> getAvatarUrl() {
        return _avatarUrl;
    }

    /** 🔹 Actualiza el LiveData del avatar según el propietario actual */
    private void actualizarAvatar(Propietario propietario) {
        Object recurso = (propietario == null || propietario.getAvatarUrl() == null || propietario.getAvatarUrl().isEmpty())
                ? R.drawable.ic_person
                : propietario.getAvatarUrl();
        _avatarUrl.postValue(recurso);
    }

}
