package com.jorge.inmobiliaria2025.ui.Inmueble;

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
import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.ui.nav.NavViewModel; // ✅ importa este

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

        // ✅ Usamos ViewBinding
        com.jorge.inmobiliaria2025.databinding.FragmentInmueblesBinding binding =
                com.jorge.inmobiliaria2025.databinding.FragmentInmueblesBinding.inflate(inflater, container, false);
        View v = binding.getRoot();


        rv = binding.rvInmuebles;
        FloatingActionButton fabAgregar = binding.fabAgregar;

        rv.setLayoutManager(new GridLayoutManager(requireContext(), 2));

        // 🧠 ViewModels
        vm = new ViewModelProvider(requireActivity()).get(InmuebleViewModel.class);

        // ✅ Conectamos el NavViewModel compartido
        NavViewModel navVM = new ViewModelProvider(requireActivity()).get(NavViewModel.class);
        vm.setNavViewModel(navVM);

        // 🔹 Adapter sin lógica en el fragment
        adapter = new InmueblesAdapter(
                new ArrayList<>(),
                vm::onInmuebleClick,
                vm::onCambiarDisponibilidad
        );
        rv.setAdapter(adapter);

        // 🧠 Observa la lista filtrada del ViewModel
        vm.getListaFiltrada().observe(getViewLifecycleOwner(), lista -> {
            List<Inmueble> inmuebles = (lista != null) ? lista : new ArrayList<>();
            adapter.actualizarLista(inmuebles);
        });

        // 📦 Carga inicial movida acá
        vm.cargarInmuebles();

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
