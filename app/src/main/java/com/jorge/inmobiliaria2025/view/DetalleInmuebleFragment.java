package com.jorge.inmobiliaria2025.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.viewmodel.InmuebleViewModel;

public class DetalleInmuebleFragment extends Fragment {

    private TextView tvDireccion, tvPrecio, tvDisponible;
    private ImageView imgInmueble;
    private InmuebleViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_detalle_inmueble, container, false);

        tvDireccion = v.findViewById(R.id.tvDireccionDetalle);
        tvPrecio = v.findViewById(R.id.tvPrecioDetalle);
        tvDisponible = v.findViewById(R.id.tvDisponibleDetalle);
        imgInmueble = v.findViewById(R.id.imgInmueble);

        // 🔹 ViewModel compartido
        vm = new ViewModelProvider(requireActivity()).get(InmuebleViewModel.class);

        // 🔹 Observa el inmueble seleccionado
        vm.getInmuebleSeleccionado().observe(getViewLifecycleOwner(), this::mostrarInmueble);

        // 🔹 Si se pasa por argumentos (al navegar desde la lista)
        vm.cargarDesdeBundle(getArguments());

        return v;
    }

    private void mostrarInmueble(Inmueble inmueble) {
        if (inmueble == null) return;

        tvDireccion.setText(inmueble.getDireccion());
        tvPrecio.setText(getString(R.string.precio_formato, inmueble.getPrecio()));

        // 🔹 Colorea según estado
        if (inmueble.isDisponible()) {
            tvDisponible.setText("Disponible");
            tvDisponible.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
        } else {
            tvDisponible.setText("No disponible");
            tvDisponible.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
        }

        // 🔹 Carga de imagen con Glide
        Glide.with(requireContext())
                .load(R.drawable.image_background) // Cambiar a inmueble.getImagenUrl() si luego lo agregás
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(imgInmueble);
    }
}
