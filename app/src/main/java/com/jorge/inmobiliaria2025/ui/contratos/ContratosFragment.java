package com.jorge.inmobiliaria2025.ui.contratos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.FragmentContratosBinding;

public class ContratosFragment extends Fragment {

    private ContratosViewModel vm;
    private ContratoAdapter adapter;
    private RecyclerView rv; // ✅ declarada correctamente

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 🔹 Usamos el binding correcto para ContratosFragment
        FragmentContratosBinding binding = FragmentContratosBinding.inflate(inflater, container, false);

        vm = new ViewModelProvider(this).get(ContratosViewModel.class);

        // 🔹 LayoutManager para RecyclerView
        binding.rvContratos.setLayoutManager(new LinearLayoutManager(requireContext()));

        // 🔹 Adapter y LiveData
        adapter = new ContratoAdapter(null, vm::onContratoSeleccionado);
        binding.rvContratos.setAdapter(adapter);

        vm.getContratos().observe(getViewLifecycleOwner(), adapter::updateData);
        vm.getAccionNavegarADetalle().observe(getViewLifecycleOwner(), bundle ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.detalleContratoFragment, bundle)
        );

        vm.cargarContratos();

        return binding.getRoot();
    }


}
