package com.jorge.inmobiliaria2025.view;

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
import com.jorge.inmobiliaria2025.viewmodel.DetalleContratoViewModel;

public class DetalleContratoFragment extends Fragment {

    private TextView tvIdContrato, tvFechas, tvMonto, tvEstado;
    private MaterialButton btnPagos;
    private DetalleContratoViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_detalle_contrato, container, false);

        tvIdContrato = v.findViewById(R.id.tvIdContrato);
        tvFechas = v.findViewById(R.id.tvFechasDetalle);
        tvMonto = v.findViewById(R.id.tvMontoDetalle);
        tvEstado = v.findViewById(R.id.tvEstadoDetalle);
        btnPagos = v.findViewById(R.id.btnVerPagos);

        vm = new ViewModelProvider(this).get(DetalleContratoViewModel.class);

        // 🟢 Observa los datos del contrato (sin condicionales)
        vm.getContrato().observe(getViewLifecycleOwner(), contrato -> {
            tvIdContrato.setText(String.valueOf(contrato.getId()));
            tvFechas.setText(contrato.getFechaInicio() + " → " + contrato.getFechaFin());
            tvMonto.setText("Monto: $" + contrato.getMontoMensual());
            tvEstado.setText("Estado: " + contrato.getEstado());
        });

        // 🟢 Observa la acción de navegación a pagos (sin if)
        vm.getAccionNavegarAPagos().observe(getViewLifecycleOwner(), bundle ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_detalleContratoFragment_to_pagosFragment, bundle)
        );

        // 🟢 Botón solo notifica al ViewModel
        btnPagos.setOnClickListener(v1 -> vm.onVerPagosClick());

        // 🟢 El ViewModel maneja los argumentos y validaciones
        vm.inicializarDesdeArgs(getArguments());

        return v;
    }
}
