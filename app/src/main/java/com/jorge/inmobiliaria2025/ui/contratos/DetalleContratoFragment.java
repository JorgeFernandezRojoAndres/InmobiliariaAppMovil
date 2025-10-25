package com.jorge.inmobiliaria2025.ui.contratos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.FragmentDetalleContratoBinding;

public class DetalleContratoFragment extends Fragment {

    private TextView tvIdContrato, tvFechas, tvMonto, tvEstado;
    private MaterialButton btnPagos;
    private DetalleContratoViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // 🔹 Usamos ViewBinding
        FragmentDetalleContratoBinding binding = FragmentDetalleContratoBinding.inflate(inflater, container, false);

        vm = new ViewModelProvider(this).get(DetalleContratoViewModel.class);

        // 🟢 Observa los datos del contrato (sin condicionales)
        vm.getContrato().observe(getViewLifecycleOwner(), contrato -> {
            binding.tvIdContrato.setText(String.valueOf(contrato.getId()));
            binding.tvFechasDetalle.setText(contrato.getFechaInicio() + " → " + contrato.getFechaFin());
            binding.tvMontoDetalle.setText("Monto: $" + contrato.getMontoMensual());
            binding.tvEstadoDetalle.setText("Estado: " + contrato.getEstado());
        });

        // 🟢 Observa la acción de navegación a pagos (sin if)
        vm.getAccionNavegarAPagos().observe(getViewLifecycleOwner(), bundle ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_detalleContratoFragment_to_pagosFragment, bundle)
        );

        // 🟢 Botón solo notifica al ViewModel
        binding.btnVerPagos.setOnClickListener(v -> vm.onVerPagosClick());

        // 🟢 El ViewModel maneja los argumentos y validaciones
        vm.inicializarDesdeArgs(getArguments());

        return binding.getRoot();
    }

}
