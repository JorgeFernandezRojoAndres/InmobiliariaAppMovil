package com.jorge.inmobiliaria2025.ui.Inicio;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.util.Optional;

public class UbicacionViewModel extends AndroidViewModel {

    private final MutableLiveData<Location> mLocation = new MutableLiveData<>();
    private final MutableLiveData<LatLng> mInmobiliariaLatLng = new MutableLiveData<>(new LatLng(-33.301726, -66.337752));  // Coordenadas de la inmobiliaria
    private final FusedLocationProviderClient fusedClient;

    public UbicacionViewModel(@NonNull Application application) {
        super(application);
        fusedClient = LocationServices.getFusedLocationProviderClient(application);
    }

    public MutableLiveData<Location> getLocation() {
        return mLocation;
    }

    public MutableLiveData<LatLng> getInmobiliariaLatLng() {
        return mInmobiliariaLatLng;
    }

    /** Método para obtener la ubicación y actualizar el LiveData */
    public void obtenerUbicacionSegura(Context ctx, Runnable solicitarPermiso, Runnable alFinalizar) {
        // Verificar si el permiso ya está concedido
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            // Si ya se tiene el permiso, obtener la ubicación
            fusedClient.getLastLocation()
                    .addOnSuccessListener(this::procesarUbicacion)
                    .addOnFailureListener(e -> {
                        // Manejo de error, si no se puede obtener la ubicación
                        e.printStackTrace();
                    })
                    .addOnCompleteListener(task -> alFinalizar.run());
        } else {
            // Si no se tiene el permiso, solicitarlo
            solicitarPermiso.run();
        }
    }

    /** Método para procesar la ubicación y actualizar el LiveData */
    private void procesarUbicacion(Location location) {
        Optional.ofNullable(location)
                .ifPresent(mLocation::setValue); // Actualiza el LiveData con la ubicación
    }

    /** Helper para manejar permisos */
    public void verificarPermiso(Context ctx, String permiso, Runnable siNoTiene, Runnable siTiene) {
        boolean concedido = ActivityCompat.checkSelfPermission(ctx, permiso) == PackageManager.PERMISSION_GRANTED;
        (concedido ? siTiene : siNoTiene).run();  // Ejecuta el Runnable según si tiene o no permisos
    }
}
