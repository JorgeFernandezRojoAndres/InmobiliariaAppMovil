package com.jorge.inmobiliaria2025.viewmodel;

import android.app.Application;
import android.annotation.SuppressLint;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

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

    @SuppressLint("MissingPermission")
    public void obtenerUbicacion() {
        fusedClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    mLocation.setValue(location);
                }
            }
        });
    }
}
