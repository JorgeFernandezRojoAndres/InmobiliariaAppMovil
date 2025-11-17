package com.jorge.inmobiliaria2025.ui.Inicio;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.FragmentUbicacionBinding;

import java.util.Objects;

public class UbicacionFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UbicacionViewModel ubicacionVM;
    private FragmentUbicacionBinding binding;

    private static final int REQUEST_LOCATION_CODE = 1001;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentUbicacionBinding.inflate(inflater, container, false);
        ubicacionVM = new ViewModelProvider(this).get(UbicacionViewModel.class);

        // Inicializa el mapa
        SupportMapFragment mapFragment =
                (SupportMapFragment) Objects.requireNonNull(getChildFragmentManager()
                        .findFragmentById(R.id.map));
        mapFragment.getMapAsync(this);

        // 🔹 Observa eventos desde el ViewModel (sin if de validación)
        ubicacionVM.getEventosUI().observe(getViewLifecycleOwner(), evento -> {

            if (evento instanceof UbicacionViewModel.EventoUI.PedirPermiso) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_CODE
                );
            }

            if (evento instanceof UbicacionViewModel.EventoUI.DibujarMarkers) {
                UbicacionViewModel.EventoUI.DibujarMarkers ev =
                        (UbicacionViewModel.EventoUI.DibujarMarkers) evento;

                mMap.clear();
                for (MarkerOptions m : ev.markers) {
                    mMap.addMarker(m);
                }
            }

            if (evento instanceof UbicacionViewModel.EventoUI.MoverCamara) {
                UbicacionViewModel.EventoUI.MoverCamara ev =
                        (UbicacionViewModel.EventoUI.MoverCamara) evento;

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(ev.bounds, 200));
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // 🛰️ Iniciar el flujo de ubicación
        ubicacionVM.iniciarObtencionUbicacion();
    }

    // 🔐 Reenvía el resultado al ViewModel
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ubicacionVM.procesarResultadoPermisos(requestCode, grantResults, REQUEST_LOCATION_CODE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
