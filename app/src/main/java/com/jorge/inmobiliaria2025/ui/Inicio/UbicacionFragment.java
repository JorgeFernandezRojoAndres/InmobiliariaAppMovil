package com.jorge.inmobiliaria2025.ui.Inicio;

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
import com.jorge.inmobiliaria2025.databinding.FragmentUbicacionBinding;

import java.util.Objects;

public class UbicacionFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private UbicacionViewModel ubicacionVM;
    private FragmentUbicacionBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentUbicacionBinding.inflate(inflater, container, false);
        ubicacionVM = new ViewModelProvider(this).get(UbicacionViewModel.class);

        // ✅ Carga del mapa sin if ni lógica condicional
        SupportMapFragment mapFragment = (SupportMapFragment)
                Objects.requireNonNull(getChildFragmentManager().findFragmentById(com.jorge.inmobiliaria2025.R.id.map));
        mapFragment.getMapAsync(this);

        return binding.getRoot();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // ✅ Solo observar cambios, sin condicionales ni validaciones
        ubicacionVM.getMarkers().observe(getViewLifecycleOwner(), markers -> {
            for (MarkerOptions marker : markers) {
                mMap.addMarker(marker);
            }
        });

        ubicacionVM.getBounds().observe(getViewLifecycleOwner(),
                bounds -> mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150)));

        // ✅ Llamada única al ViewModel (sin lógica en vista)
        ubicacionVM.iniciarObtencionUbicacion();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
