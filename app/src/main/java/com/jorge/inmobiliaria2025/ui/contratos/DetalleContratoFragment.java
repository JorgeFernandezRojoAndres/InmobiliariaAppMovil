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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.FragmentDetalleContratoBinding;
import com.jorge.inmobiliaria2025.databinding.DialogRenovarContratoBinding;
import com.jorge.inmobiliaria2025.utils.DebugNavTracker;

public class DetalleContratoFragment extends Fragment {

    private static final String TAG = "DETALLE_CONTRATO";
    private DetalleContratoViewModel vm;
    private FragmentDetalleContratoBinding binding;
    private NavController navController;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentDetalleContratoBinding.inflate(inflater, container, false);

        NavHostFragment navHostFragment = (NavHostFragment)
                requireActivity().getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        vm = new ViewModelProvider(
                navController.getViewModelStoreOwner(R.id.nav_graph)
        ).get(DetalleContratoViewModel.class);

        DebugNavTracker.logFragment(this, "Detalle_onCreateView");

        // === Observers principales ===
        vm.getContrato().observe(getViewLifecycleOwner(), contrato -> {
            if (contrato == null) return;
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

        vm.getNavegarAPagos().observe(getViewLifecycleOwner(), args -> {
            if (args != null) {
                navController.navigate(R.id.action_detalleContratoFragment_to_pagosFragment, args);
                vm.limpiarAccionNavegar();
            }
        });

        // ✅ Unificado: el ViewModel envía los eventos de UI listos para mostrar
        vm.getUiEvento().observe(getViewLifecycleOwner(), evento -> {
            if (evento == null) return;

            Log.d(TAG, "📬 uiEvento recibido -> tipo=" + evento.getTipo() +
                    ", titulo=" + evento.getTitulo() +
                    ", accion=" + evento.getAccionAsociada());

            switch (evento.getTipo()) {

                case CONFIRMACION:
                    Log.d(TAG, "🟢 Mostrando diálogo de CONFIRMACION");
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(evento.getTitulo())
                            .setMessage(evento.getMensaje())
                            .setPositiveButton(evento.getTextoPositivo(),
                                    (d, w) -> vm.confirmarRescision())
                            .setNegativeButton(evento.getTextoNegativo(),
                                    (d, w) -> d.dismiss())
                            .show();
                    break;

                case INFORMACION:
                    // 🧠 Evitar mostrar diálogos vacíos (solo navegación)
                    if ((evento.getTitulo() == null || evento.getTitulo().trim().isEmpty()) &&
                            (evento.getMensaje() == null || evento.getMensaje().trim().isEmpty())) {
                        Log.d(TAG, "⚠️ Evento de navegación sin diálogo: se omite mostrar cartel");
                        break;
                    }

                    Log.d(TAG, "🟢 Mostrando diálogo de INFORMACION");
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(evento.getTitulo() != null ? evento.getTitulo() : "Información")
                            .setMessage(evento.getMensaje() != null ? evento.getMensaje() : "")
                            .setPositiveButton("OK", null)
                            .show();
                    break;

                case ERROR:
                    Log.d(TAG, "🟠 Mostrando diálogo de ERROR");
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle(evento.getTitulo() != null ? evento.getTitulo() : "Error")
                            .setMessage(evento.getMensaje() != null ? evento.getMensaje() : "Ocurrió un error.")
                            .setPositiveButton("OK", null)
                            .show();
                    break;
            }

            // 🔁 Acción asociada opcional (por ejemplo, volver a contratos)
            if ("NAVEGAR_CONTRATOS".equals(evento.getAccionAsociada())) {
                Log.d(TAG, "🔁 Ejecutando navegación a Contratos...");
                navController.navigate(R.id.action_detalleContratoFragment_to_nav_contratos);

                // 🧹 Limpia el evento después de navegar para evitar repeticiones o bucles
                vm.limpiarUiEvento();
                Log.d(TAG, "🧹 uiEvento limpiado después de navegar");
            }

        });


        // === Diálogo de renovación ===
        vm.getMostrarDialogoRenovar().observe(getViewLifecycleOwner(), show -> {
            if (Boolean.TRUE.equals(show)) {
                DialogRenovarContratoBinding dialogBinding =
                        DialogRenovarContratoBinding.inflate(getLayoutInflater());

                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Renovar Contrato")
                        .setView(dialogBinding.getRoot())
                        .setPositiveButton("Renovar", (dialog, which) -> {
                            vm.onConfirmarRenovacion(
                                    dialogBinding.etNuevaFechaInicio.getText().toString(),
                                    dialogBinding.etNuevaFechaFin.getText().toString(),
                                    dialogBinding.etNuevoMonto.getText().toString()
                            );
                        })
                        .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                        .show();

                vm.limpiarDialogoRenovar();
            }
        });

        // === Botones ===
        binding.btnVerPagos.setOnClickListener(v -> vm.onVerPagosClick());
        binding.btnVolverContratos.setOnClickListener(v -> vm.onVolverClick());
        binding.btnRescindirContrato.setOnClickListener(v -> vm.onRescindirClick());
        binding.btnRenovarContrato.setOnClickListener(v -> vm.onRenovarClick());

        vm.inicializarDesdeArgs(getArguments());

        return binding.getRoot();
    }


    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
