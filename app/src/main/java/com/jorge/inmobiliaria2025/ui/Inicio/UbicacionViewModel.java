package com.jorge.inmobiliaria2025.ui.Inicio;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

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

    // === EVENTOS UI ===
    public abstract static class EventoUI {
        public static class PedirPermiso extends EventoUI {}
        public static class DibujarMarkers extends EventoUI {
            public final List<MarkerOptions> markers;
            public DibujarMarkers(List<MarkerOptions> markers) { this.markers = markers; }
        }
        public static class MoverCamara extends EventoUI {
            public final LatLngBounds bounds;
            public MoverCamara(LatLngBounds b) { this.bounds = b; }
        }
    }

    private final MutableLiveData<EventoUI> _eventosUI = new MutableLiveData<>();
    public LiveData<EventoUI> getEventosUI() { return _eventosUI; }

    // === Ubicación y mapa ===
    private final MutableLiveData<Location> mLocation = new MutableLiveData<>();
    private final MutableLiveData<LatLng> mInmobiliariaLatLng =
            new MutableLiveData<>(new LatLng(-33.301726, -66.337752));

    private final MediatorLiveData<LatLngBounds> mBounds = new MediatorLiveData<>();
    public LiveData<LatLngBounds> getBounds() { return mBounds; }

    private final MutableLiveData<List<MarkerOptions>> mMarkers =
            new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<MarkerOptions>> getMarkers() { return mMarkers; }

    private final FusedLocationProviderClient fusedClient;

    public UbicacionViewModel(@NonNull Application application) {
        super(application);
        fusedClient = LocationServices.getFusedLocationProviderClient(application);

        // recalcular bounds cada vez que cambia ubicación u oficina
        mBounds.addSource(mLocation, loc -> recomputarBounds(loc, mInmobiliariaLatLng.getValue()));
        mBounds.addSource(mInmobiliariaLatLng, inmo -> recomputarBounds(mLocation.getValue(), inmo));
    }

    /** ================================
     *  FLUJO PRINCIPAL DE UBICACIÓN
     *  ================================ */
    public void iniciarObtencionUbicacion() {
        Log.d("UBICACION_VM", "🚀 Iniciando obtención de ubicación...");

        boolean permisoOk = ContextCompat.checkSelfPermission(
                getApplication(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;

        if (!permisoOk) {
            Log.w("UBICACION_VM", "🚫 Permiso no concedido, enviando evento a la vista");
            _eventosUI.setValue(new EventoUI.PedirPermiso());
            return;
        }

        Log.d("UBICACION_VM", "✅ Permiso concedido, solicitando última ubicación...");

        fusedClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Log.d("UBICACION_VM", "📍 Ubicación obtenida: "
                                + location.getLatitude() + ", " + location.getLongitude());
                        procesarUbicacion(location);
                    } else {
                        Log.w("UBICACION_VM", "⚠️ getLastLocation devolvió null");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("UBICACION_VM", "❌ Error obteniendo ubicación: " + e.getMessage(), e);
                });
    }

    /** ================================
     *  PROCESAR PERMISOS (vista solo reenvía)
     *  ================================ */
    public void procesarResultadoPermisos(int requestCode,
                                          int[] grantResults,
                                          int expectedCode) {

        boolean concedido = requestCode == expectedCode &&
                grantResults != null &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (concedido) {
            Log.d("UBICACION_VM", "🔓 Permiso concedido. Reintentando...");
            iniciarObtencionUbicacion();
        } else {
            Log.w("UBICACION_VM", "🚷 Permiso denegado.");
        }
    }

    /** ================================
     *  PROCESAR UBICACIÓN
     *  ================================ */
    private void procesarUbicacion(Location location) {
        Optional.ofNullable(location).ifPresent(loc -> {
            mLocation.setValue(loc);

            LatLng inmobiliaria = mInmobiliariaLatLng.getValue();
            if (inmobiliaria == null) {
                Log.w("UBICACION_VM", "⚠️ Coordenada de inmobiliaria nula");
                return;
            }

            // Crear marcadores
            List<MarkerOptions> lista = new ArrayList<>();
            lista.add(new MarkerOptions()
                    .position(inmobiliaria)
                    .title("Inmobiliaria")
                    .snippet("Nuestra oficina central"));

            lista.add(new MarkerOptions()
                    .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                    .title("Mi ubicación actual")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            mMarkers.setValue(lista);

            // Enviar evento a la vista para dibujarlos
            _eventosUI.setValue(new EventoUI.DibujarMarkers(lista));

            // Recalcular bounds
            recomputarBounds(loc, inmobiliaria);
        });
    }

    /** ================================
     *  CALCULAR BOUNDS
     *  ================================ */
    private void recomputarBounds(Location loc, LatLng inmo) {
        if (loc == null || inmo == null) return;

        LatLng mi = new LatLng(loc.getLatitude(), loc.getLongitude());
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(mi)
                .include(inmo)
                .build();

        mBounds.setValue(bounds);

        // Enviar acción para mover cámara
        _eventosUI.setValue(new EventoUI.MoverCamara(bounds));

        Log.d("UBICACION_VM", "📏 Bounds recalculados.");
    }
}
