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

        // 🧩 Configurar vista en cuadrícula (2 columnas)
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // 🧩 Inicializar ViewModel
        vm = new ViewModelProvider(requireActivity()).get(InmuebleViewModel.class);

        // 🧩 Crear adapter con listeners: click + cambio de disponibilidad
        adapter = new InmueblesAdapter(
                new ArrayList<>(),
                inmueble -> { // 👉 Click en item
                    vm.setInmuebleSeleccionado(inmueble);
                    NavHostFragment.findNavController(this)
                            .navigate(R.id.action_inmueblesFragment_to_detalleInmuebleFragment);
                },
                inmueble -> { // 👉 Cambio de switch
                    vm.actualizarDisponibilidad(inmueble);
                }
        );

        rv.setAdapter(adapter);

        // 🧠 Observa lista de inmuebles del ViewModel
        vm.getListaFiltrada().observe(getViewLifecycleOwner(), lista -> {
            if (lista != null && !lista.isEmpty()) {
                adapter.actualizarLista(lista);
            } else {
                adapter.actualizarLista(new ArrayList<>()); // Evita NPE si viene vacía
            }
        });

        // 🚀 Carga inicial: si el ViewModel aún no tiene datos, los trae del backend
        if (vm.getListaFiltrada().getValue() == null || vm.getListaFiltrada().getValue().isEmpty()) {
            vm.cargarInmuebles();
        }

        // ➕ Botón para agregar nuevo inmueble
        fabAgregar.setOnClickListener(view ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_inmueblesFragment_to_nuevoInmuebleFragment)
        );

        return v;
    }
}
