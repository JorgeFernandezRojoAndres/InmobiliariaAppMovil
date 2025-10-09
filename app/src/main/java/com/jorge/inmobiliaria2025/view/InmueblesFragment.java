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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_inmuebles, container, false);

        RecyclerView rv = v.findViewById(R.id.rvInmuebles);
        FloatingActionButton fabAgregar = v.findViewById(R.id.fabAgregar);

        // 🔹 Configurar vista en cuadrícula de 2 columnas
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // 🔹 Obtener ViewModel compartido
        vm = new ViewModelProvider(requireActivity()).get(InmuebleViewModel.class);

        // 🔹 Inicializar adaptador con lista vacía y listener de clic
        adapter = new InmueblesAdapter(new ArrayList<>(), inmueble -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("inmueble", inmueble);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_inmueblesFragment_to_detalleInmuebleFragment, bundle);
        });

        rv.setAdapter(adapter);

        // 🔹 Observar lista de inmuebles y actualizar adapter con DiffUtil
        vm.getListaLiveData().observe(getViewLifecycleOwner(), inmuebles -> {
            if (inmuebles != null && !inmuebles.isEmpty()) {
                adapter.actualizarLista(inmuebles);
            }
        });

        // 🔹 Cargar inmuebles desde base de datos local o mock
        vm.cargarInmuebles();

        // 🔹 FAB: navegar al formulario de nuevo inmueble
        fabAgregar.setOnClickListener(view ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_inmueblesFragment_to_nuevoInmuebleFragment)
        );

        return v;
    }
}
