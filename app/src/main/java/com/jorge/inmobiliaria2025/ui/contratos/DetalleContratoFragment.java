package com.jorge.inmobiliaria2025.ui.contratos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.FragmentDetalleContratoBinding;

public class DetalleContratoFragment extends Fragment {

    private static final String TAG = "DETALLE_CONTRATO";
    private DetalleContratoViewModel vm;
    private FragmentDetalleContratoBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDetalleContratoBinding.inflate(inflater, container, false);
        vm = new ViewModelProvider(this).get(DetalleContratoViewModel.class);

        // Observa los datos del contrato
        vm.getContrato().observe(getViewLifecycleOwner(), contrato -> {
            binding.tvIdContrato.setText(String.valueOf(contrato.getId()));
            binding.tvFechasDetalle.setText(
                    getString(R.string.rango_fechas, contrato.getFechaInicio(), contrato.getFechaFin())
            );
            binding.tvMontoDetalle.setText(
                    getString(R.string.detalle_monto, contrato.getMontoMensual())
            );
            binding.tvEstadoDetalle.setText(
                    getString(R.string.detalle_estado, contrato.getEstado())
            );
        });

        // Botón para ver pagos
        binding.btnVerPagos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vm.onVerPagosClick();
            }
        });

        // Observa evento de navegación (único if permitido)
        vm.getNavegarAPagos().observe(getViewLifecycleOwner(), args -> {
            if (args != null) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_detalleContratoFragment_to_pagosFragment, args);
                vm.limpiarAccionNavegar();
            }
        });

        // Inicializa el ViewModel (la validación de argumentos se hace adentro)
        vm.inicializarDesdeArgs(getArguments());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
