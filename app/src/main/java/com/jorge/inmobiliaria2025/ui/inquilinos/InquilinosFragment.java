package com.jorge.inmobiliaria2025.ui.inquilinos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.FragmentInquilinosBinding;

import java.util.ArrayList;

public class InquilinosFragment extends Fragment {

    private FragmentInquilinosBinding binding;
    private InquilinosViewModel vm;
    private InquilinoAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentInquilinosBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Inicializando el ViewModel
        vm = new ViewModelProvider(this).get(InquilinosViewModel.class);

        // Inicializando el adapter
        adapter = new InquilinoAdapter(new ArrayList<>(), new InquilinoAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int idInquilino) {
                // Acción al hacer clic en un inquilino
                Bundle bundle = new Bundle();
                bundle.putInt("idInquilino", idInquilino); // Pasamos el id del inquilino
                Navigation.findNavController(root)
                        .navigate(R.id.action_nav_inquilinos_to_detalleInquilinoFragment, bundle);
            }
        });

        binding.rvInquilinos.setAdapter(adapter);

        // Observar cambios en la lista de inquilinos
        vm.getListaInquilinos().observe(getViewLifecycleOwner(), inquilinos -> {
            if (inquilinos != null && !inquilinos.isEmpty()) {
                adapter.setLista(inquilinos);  // Asegúrate de que 'setLista' esté bien implementado
            } else {
                Log.w("InquilinosFragment", "No se han recibido inquilinos o la lista está vacía.");
            }
        });


        // Cargar inquilinos con inmueble
        vm.cargarInquilinosConInmueble();

        return root;
    }
}
