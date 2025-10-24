package com.jorge.inmobiliaria2025.ui.Inicio;
import java.util.Optional;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jorge.inmobiliaria2025.R;

public class UbicacionFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Nullable

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ubicacion, container, false);

        Objects.requireNonNull(
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)
        ).getMapAsync(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        return root;
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Coordenadas fijas de la inmobiliaria (ajustá según tu ubicación real)
        LatLng inmobiliaria = new LatLng(-33.301726, -66.337752);

        // Marcador de la inmobiliaria
        mMap.addMarker(new MarkerOptions()
                .position(inmobiliaria)
                .title("Inmobiliaria Alone")
                .snippet("Nuestra oficina central"));

        // Centrar el mapa inicialmente en la inmobiliaria
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(inmobiliaria, 14));

        // Mostrar ubicación actual del usuario
        mostrarUbicacionActual();
    }

    private void mostrarUbicacionActual() {
        UbicacionViewModel ubicacionVM = new ViewModelProvider(this).get(UbicacionViewModel.class);

        Runnable solicitarPermiso = () -> requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION
        );

        Runnable alFinalizar = () -> { /* nada, pero lo dejamos por si querés algo después */ };

        ubicacionVM.obtenerUbicacionSegura(requireContext(), solicitarPermiso, alFinalizar);

        ubicacionVM.getLocation().observe(getViewLifecycleOwner(), location -> {
            Optional.ofNullable(location)
                    .filter(loc -> mMap != null)
                    .ifPresent(loc -> {
                        LatLng miUbicacion = new LatLng(loc.getLatitude(), loc.getLongitude());
                        LatLng inmobiliaria = new LatLng(-33.301726, -66.337752);

                        mMap.addMarker(new MarkerOptions()
                                .position(miUbicacion)
                                .title("Mi ubicación actual")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                        LatLngBounds bounds = new LatLngBounds.Builder()
                                .include(miUbicacion)
                                .include(inmobiliaria)
                                .build();

                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
                    });
        });
    }



    // Manejo del resultado de la solicitud de permisos
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mostrarUbicacionActual();
        }
    }
}