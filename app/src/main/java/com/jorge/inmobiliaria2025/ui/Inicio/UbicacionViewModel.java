package com.jorge.inmobiliaria2025.ui.Inicio;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.MediatorLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UbicacionViewModel extends AndroidViewModel {

    private final MutableLiveData<Location> mLocation = new MutableLiveData<>();
    private final MutableLiveData<LatLng> mInmobiliariaLatLng =
            new MutableLiveData<>(new LatLng(-33.301726, -66.337752));

    private final MutableLiveData<Boolean> _solicitarPermiso = new MutableLiveData<>();
    public LiveData<Boolean> solicitarPermisoEvent() { return _solicitarPermiso; }

    private final FusedLocationProviderClient fusedClient;

    // 🆕 LiveData para los límites del mapa
    private final MediatorLiveData<LatLngBounds> mBounds = new MediatorLiveData<>();
    public LiveData<LatLngBounds> getBounds() { return mBounds; }

    // 🆕 LiveData para los marcadores del mapa
    private final MutableLiveData<List<MarkerOptions>> mMarkers = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<MarkerOptions>> getMarkers() { return mMarkers; }

    public UbicacionViewModel(@NonNull Application application) {
        super(application);
        fusedClient = LocationServices.getFusedLocationProviderClient(application);

        // 🔁 Cuando cambia la ubicación o la coordenada fija, recalculamos los bounds
        mBounds.addSource(mLocation, loc -> recomputarBounds(loc, mInmobiliariaLatLng.getValue()));
        mBounds.addSource(mInmobiliariaLatLng, inmo -> recomputarBounds(mLocation.getValue(), inmo));
    }

    public MutableLiveData<Location> getLocation() { return mLocation; }
    public MutableLiveData<LatLng> getInmobiliariaLatLng() { return mInmobiliariaLatLng; }

    /** ✅ Flujo MVVM sin lógica en la vista */
    public void iniciarObtencionUbicacion() {
        if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedClient.getLastLocation()
                    .addOnSuccessListener(this::procesarUbicacion)
                    .addOnFailureListener(Throwable::printStackTrace);
        } else {
            _solicitarPermiso.setValue(true); // la vista solo observa este evento
        }
    }

    /** ✅ Resultado de permisos (la vista solo reenvía el resultado) */
    public void procesarResultadoPermisos(int requestCode, int[] grantResults, int expectedCode) {
        if (requestCode == expectedCode &&
                grantResults != null && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            iniciarObtencionUbicacion();
        }
    }

    /** 🔁 Método legado para compatibilidad */
    @Deprecated
    public void obtenerUbicacionSegura(Context ctx, Runnable solicitarPermiso, Runnable alFinalizar) {
        iniciarObtencionUbicacion();
        if (alFinalizar != null) alFinalizar.run();
    }

    /** 🔁 Helper de permisos */
    public void verificarPermiso(Context ctx, String permiso, Runnable siNoTiene, Runnable siTiene) {
        boolean concedido = ContextCompat.checkSelfPermission(ctx, permiso) == PackageManager.PERMISSION_GRANTED;
        (concedido ? siTiene : siNoTiene).run();
    }

    /** 🧭 Procesa la ubicación y genera marcadores y bounds */
    private void procesarUbicacion(Location location) {
        Optional.ofNullable(location).ifPresent(loc -> {
            mLocation.setValue(loc);

            LatLng inmobiliaria = mInmobiliariaLatLng.getValue();
            if (inmobiliaria == null) return;

            // 🟢 Crear lista de marcadores
            List<MarkerOptions> lista = new ArrayList<>();
            lista.add(new MarkerOptions()
                    .position(inmobiliaria)
                    .title("Inmobiliaria Alone")
                    .snippet("Nuestra oficina central"));
            lista.add(new MarkerOptions()
                    .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .title("Mi ubicación actual")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            // Publicar los marcadores
            mMarkers.postValue(lista);

            // Recalcular bounds
            recomputarBounds(loc, inmobiliaria);
        });
    }

    /** 🧭 Cálculo automático de bounds */
    private void recomputarBounds(Location loc, LatLng inmo) {
        if (loc == null || inmo == null) return;
        LatLng mi = new LatLng(loc.getLatitude(), loc.getLongitude());
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(mi)
                .include(inmo)
                .build();
        mBounds.setValue(bounds);
    }
}
