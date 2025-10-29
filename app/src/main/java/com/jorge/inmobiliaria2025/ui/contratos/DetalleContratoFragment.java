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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.FragmentDetalleContratoBinding;
import com.jorge.inmobiliaria2025.model.Contrato;

public class DetalleContratoFragment extends Fragment {

    private static final String TAG = "DETALLE_CONTRATO";

    private DetalleContratoViewModel vm;
    private FragmentDetalleContratoBinding binding;
    private boolean navegandoAPagos = false;
    private int contratoId = -1;
    private Contrato contratoActual;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "🧩 onCreateView() iniciado");

        binding = FragmentDetalleContratoBinding.inflate(inflater, container, false);
        vm = new ViewModelProvider(this).get(DetalleContratoViewModel.class);
        Log.d(TAG, "✅ ViewModel creado correctamente");

        // 🧩 Observer de contrato
        vm.getContrato().observe(getViewLifecycleOwner(), contrato -> {
            Log.d(TAG, "📡 Observer disparado - contrato=" + (contrato != null ? contrato.getId() : "null"));
            if (contrato != null) {
                contratoActual = contrato;
                binding.tvIdContrato.setText(String.valueOf(contrato.getId()));
                binding.tvFechasDetalle.setText(contrato.getFechaInicio() + " → " + contrato.getFechaFin());
                binding.tvMontoDetalle.setText("Monto: $" + contrato.getMontoMensual());
                binding.tvEstadoDetalle.setText("Estado: " + contrato.getEstado());
                Log.d(TAG, "✅ Datos mostrados en la UI");
            } else {
                Log.w(TAG, "⚠️ Contrato recibido es null, no se actualiza UI");
            }
        });

        // ✅ Inicialización de argumentos DESPUÉS del observer
        if (getArguments() != null) {
            Log.d(TAG, "📦 Args recibidos: " + getArguments().keySet());
            contratoId = getArguments().getInt("contratoId", -1);
            Log.d(TAG, "📦 contratoId=" + contratoId);
            vm.inicializarDesdeArgs(getArguments());
        } else {
            Log.w(TAG, "⚠️ getArguments() == null, no se inicializa ViewModel");
        }

        // 💰 Botón para ver pagos
        binding.btnVerPagos.setOnClickListener(v -> {
            Log.d(TAG, "🟢 Click en btnVerPagos");

            if (navegandoAPagos) {
                Log.w(TAG, "⚠️ Navegación a Pagos bloqueada (navegandoAPagos=true)");
                return;
            }

            navegandoAPagos = true;

            if (contratoId == -1 && contratoActual == null) {
                Log.e(TAG, "❌ contratoId y contratoActual nulos. No se puede navegar a pagos.");
                navegandoAPagos = false;
                return;
            }

            Bundle args = new Bundle();
            if (contratoActual != null) {
                args.putSerializable("contratoSeleccionado", contratoActual);
                args.putInt("contratoId", contratoActual.getId());
                Log.d(TAG, "📦 Enviando contrato completo a Pagos (ID=" + contratoActual.getId() + ")");
            } else {
                args.putInt("contratoId", contratoId);
                Log.d(TAG, "📦 Enviando solo contratoId=" + contratoId + " a Pagos");
            }

            NavController navController = NavHostFragment.findNavController(this);
            try {
                Log.d(TAG, "➡️ Navegando a PagosFragment...");
                navController.navigate(R.id.action_detalleContratoFragment_to_pagosFragment, args);
                Log.d(TAG, "✅ Navegación a Pagos exitosa");
            } catch (Exception e) {
                Log.e(TAG, "💥 Error al navegar a Pagos: " + e.getMessage(), e);
            }

            binding.getRoot().postDelayed(() -> {
                navegandoAPagos = false;
                Log.d(TAG, "🔁 Reset navegandoAPagos=false");
            }, 400);
        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 onResume() ejecutado");
        if (contratoId != -1 && vm != null) {
            Log.d(TAG, "📡 Solicitando carga de contrato por ID=" + contratoId);
            vm.cargarContratoPorId(contratoId);
        } else {
            Log.w(TAG, "⚠️ No se carga contrato: contratoId=" + contratoId + ", vm=" + (vm != null));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "🧹 onDestroyView() ejecutado");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "💣 onDestroy() ejecutado");
    }
}
