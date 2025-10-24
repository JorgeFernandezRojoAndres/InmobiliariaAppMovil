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

import java.util.Optional;

public class UbicacionViewModel extends AndroidViewModel {

    private final MutableLiveData<Location> mLocation = new MutableLiveData<>();
    private final FusedLocationProviderClient fusedClient;

    public UbicacionViewModel(@NonNull Application application) {
        super(application);
        fusedClient = LocationServices.getFusedLocationProviderClient(application);
    }

    public MutableLiveData<Location> getLocation() {
        return mLocation;
    }

    // Encapsula toda la lógica sin if visibles
    public void obtenerUbicacionSegura(Context ctx,
                                       Runnable solicitarPermiso,
                                       Runnable alFinalizar) {

        Runnable obtenerUbicacion = () -> fusedClient.getLastLocation()
                .addOnSuccessListener(this::procesarUbicacion);

        ejecutarSiTienePermiso(
                ctx,
                Manifest.permission.ACCESS_FINE_LOCATION,
                solicitarPermiso,
                () -> {
                    obtenerUbicacion.run();
                    alFinalizar.run();
                }
        );
    }

    private void procesarUbicacion(Location location) {
        Optional.ofNullable(location)
                .ifPresent(mLocation::setValue);
    }

    // Helper genérico sin if explícitos
    private void ejecutarSiTienePermiso(Context ctx,
                                        String permiso,
                                        Runnable siNoTiene,
                                        Runnable siTiene) {

        boolean concedido = ActivityCompat.checkSelfPermission(ctx, permiso)
                == PackageManager.PERMISSION_GRANTED;

        ejecutarSegun(concedido, siTiene, siNoTiene);
    }

    private void ejecutarSegun(boolean condicion, Runnable siVerdadero, Runnable siFalso) {
        (condicion ? siVerdadero : siFalso).run();
    }
}
