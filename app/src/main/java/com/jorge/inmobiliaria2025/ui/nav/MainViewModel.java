package com.jorge.inmobiliaria2025.ui.nav;

import android.app.Application;
import android.widget.ImageView;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.bumptech.glide.Glide;
import com.jorge.inmobiliaria2025.InmobiliariaApp;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.localdata.SessionManager;
import com.jorge.inmobiliaria2025.model.Propietario;

public class MainViewModel extends AndroidViewModel {

    private final MutableLiveData<Propietario> _propietarioHeader = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _navegarLogin = new MutableLiveData<>();

    // ✅ LiveData para avatar
    private final MutableLiveData<Object> _avatarUrl = new MutableLiveData<>();
    public LiveData<Object> getAvatarUrl() { return _avatarUrl; }

    public MainViewModel(@NonNull Application app) {
        super(app);
        verificarSesion();
    }

    private void verificarSesion() {
        SessionManager sm = SessionManager.getInstance(getApplication());

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

    public void refrescarSesion() {
        verificarSesion();
    }

    // ❗ método original (lo dejamos)
    public void actualizarHeader(Propietario propietario) {
        if (propietario != null) {
            _propietarioHeader.postValue(propietario);
            actualizarAvatar(propietario);
        }
    }

    // ✅ método nuevo: actualiza header luego de delay para evitar "sin JWT"
    public void actualizarHeaderConDelay(Propietario propietario) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (propietario != null) {
                _propietarioHeader.postValue(propietario);
                actualizarAvatar(propietario);
            }
        }, 350); // espera breve para que Retrofit recargue token
    }

    public LiveData<Propietario> getPropietarioHeader() {
        return _propietarioHeader;
    }

    public LiveData<Boolean> getNavegarLogin() {
        return _navegarLogin;
    }

    public Object getAvatarUrl(Propietario propietario) {
        String url = propietario.getAvatarUrl();
        if (url == null || url.isEmpty()) {
            return R.drawable.ic_person;
        }
        return url;
    }

    public void cargarAvatarEn(ImageView imageView, Propietario propietario) {
        try {
            SessionManager sm = SessionManager.getInstance(getApplication());
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

    private void actualizarAvatar(Propietario propietario) {
        Object recurso = (propietario == null || propietario.getAvatarUrl() == null || propietario.getAvatarUrl().isEmpty())
                ? R.drawable.ic_person
                : propietario.getAvatarUrl();
        _avatarUrl.postValue(recurso);
    }
}
