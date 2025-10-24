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

public class ContratosFragment extends Fragment {

    private ContratosViewModel vm;
    private ContratoAdapter adapter;
    private RecyclerView rv; // ✅ declarada correctamente

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_contratos, container, false);

        rv = v.findViewById(R.id.rvContratos);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        vm = new ViewModelProvider(this).get(ContratosViewModel.class);

        adapter = new ContratoAdapter(null, vm::onContratoSeleccionado);
        rv.setAdapter(adapter);

        vm.getContratos().observe(getViewLifecycleOwner(), adapter::updateData);

        vm.getAccionNavegarADetalle().observe(getViewLifecycleOwner(), bundle ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.detalleContratoFragment, bundle)
        );

        vm.cargarContratos();
        return v;
    }
}
