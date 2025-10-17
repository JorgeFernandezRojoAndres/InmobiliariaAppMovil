package com.jorge.inmobiliaria2025.view;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.adapter.InmueblesAdapter;
import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.viewmodel.InmuebleViewModel;

import java.util.ArrayList;
import java.util.List;

public class InmueblesFragment extends Fragment {

    private InmuebleViewModel vm;
    private InmueblesAdapter adapter;
    private RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_inmuebles, container, false);

        rv = v.findViewById(R.id.rvInmuebles);
        FloatingActionButton fabAgregar = v.findViewById(R.id.fabAgregar);

        // 🧩 Vista en cuadrícula (2 columnas)
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // 🧩 ViewModel compartido con la Activity
        vm = new ViewModelProvider(requireActivity()).get(InmuebleViewModel.class);

        // 🧩 Adapter con listeners: click + cambio de disponibilidad
        adapter = new InmueblesAdapter(
                new ArrayList<>(),
                inmueble -> { // 👉 Click en item
                    if (inmueble != null) {
                        vm.setInmuebleSeleccionado(inmueble); // 🔹 Sincroniza en ViewModel
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("inmueble", inmueble);
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_inmueblesFragment_to_detalleInmuebleFragment, bundle);
                    }
                },
                inmueble -> { // 👉 Cambio de switch
                    if (inmueble != null) {
                        vm.actualizarDisponibilidad(inmueble);
                    }
                }
        );

        rv.setAdapter(adapter);

        // 🧠 Observa la lista filtrada del ViewModel
        vm.getListaFiltrada().observe(getViewLifecycleOwner(), lista -> {
            List<Inmueble> inmuebles = (lista != null) ? lista : new ArrayList<>();
            adapter.actualizarLista(inmuebles);
        });

        // 🚀 Carga inicial
        if (vm.getListaFiltrada().getValue() == null || vm.getListaFiltrada().getValue().isEmpty()) {
            vm.cargarInmuebles();
        }

        // ➕ Botón agregar inmueble
        fabAgregar.setOnClickListener(view ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_inmueblesFragment_to_nuevoInmuebleFragment)
        );

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 🔁 Refresca al volver del detalle o edición
        vm.cargarInmuebles();
    }
}
