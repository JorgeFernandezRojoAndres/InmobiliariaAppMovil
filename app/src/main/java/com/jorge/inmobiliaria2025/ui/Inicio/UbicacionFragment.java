package com.jorge.inmobiliaria2025.ui.Inicio;

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
import androidx.lifecycle.ViewModelProvider;

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
    private UbicacionViewModel ubicacionVM;
    private static final int REQUEST_LOCATION_PERMISSION = 1; // Constante para solicitud de permisos

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_ubicacion, container, false);

        // Inicializar ViewModel
        ubicacionVM = new ViewModelProvider(this).get(UbicacionViewModel.class);

        // Inicializamos el mapa
        Objects.requireNonNull(
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)
        ).getMapAsync(this);

        // Observar la ubicación en el ViewModel
        ubicacionVM.getLocation().observe(getViewLifecycleOwner(), location -> {
            if (location != null && mMap != null) {
                // Marcador de la inmobiliaria
                LatLng inmobiliaria = new LatLng(-33.301726, -66.337752);
                mMap.addMarker(new MarkerOptions()
                        .position(inmobiliaria)
                        .title("Inmobiliaria Alone")
                        .snippet("Nuestra oficina central"));

                // Ubicación actual del usuario
                LatLng miUbicacion = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(miUbicacion)
                        .title("Mi ubicación actual")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                // Centrar el mapa para mostrar ambas ubicaciones
                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(miUbicacion)
                        .include(inmobiliaria)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));
            }
        });

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Centrar el mapa en la inmobiliaria
        LatLng inmobiliaria = ubicacionVM.getInmobiliariaLatLng().getValue();
        mMap.addMarker(new MarkerOptions()
                .position(inmobiliaria)
                .title("Inmobiliaria Alone")
                .snippet("Nuestra oficina central"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(inmobiliaria, 14));

        // Solicitar la ubicación actual desde el ViewModel
        ubicacionVM.obtenerUbicacionSegura(requireContext(), this::solicitarPermiso, this::obtenerUbicacion);
    }

    private void solicitarPermiso() {
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION_PERMISSION
        );
    }

    private void obtenerUbicacion() {
        ubicacionVM.obtenerUbicacionSegura(requireContext(), this::solicitarPermiso, this::obtenerUbicacion);
    }

    // Manejo del resultado de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ubicacionVM.obtenerUbicacionSegura(requireContext(), this::solicitarPermiso, this::obtenerUbicacion);
        }
    }
}
