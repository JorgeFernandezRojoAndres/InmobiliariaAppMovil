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

    @Nullable
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

        // ======== Observers ya existentes ========
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

        vm.getNavegarAPagos().observe(getViewLifecycleOwner(), args -> {
            if (args != null) {
                navController.navigate(R.id.action_detalleContratoFragment_to_pagosFragment, args);
                vm.limpiarAccionNavegar();
            }
        });

        vm.getUiAccion().observe(getViewLifecycleOwner(), accion -> {
            if (accion == null) return;

            switch (accion) {

                case MOSTRAR_DIALOGO_CONFIRMACION:
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Confirmar acción")
                            .setMessage("¿Seguro que querés rescindir este contrato?")
                            .setPositiveButton("Rescindir", (d, w) -> vm.confirmarRescision())
                            .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                            .show();
                    break;

                case MOSTRAR_MENSAJE_EXITO:
                    String ultimaRespuesta = vm.getUltimoMensaje();
                    String multa = null;
                    try {
                        if (ultimaRespuesta != null && ultimaRespuesta.contains("multa")) {
                            org.json.JSONObject json = new org.json.JSONObject(ultimaRespuesta);
                            multa = json.optString("multa", null);
                        }
                    } catch (Exception ignored) {}

                    String mensaje = (multa != null)
                            ? "Contrato rescindido.\nMulta: $" + multa
                            : "Contrato rescindido.";
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Éxito")
                            .setMessage(mensaje)
                            .setPositiveButton("OK", null)
                            .show();
                    break;

                case MOSTRAR_MENSAJE_ERROR:
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Error")
                            .setMessage("No se pudo rescindir el contrato.")
                            .setPositiveButton("OK", null)
                            .show();
                    break;

                case VOLVER_A_CONTRATOS:
                    navController.navigate(R.id.action_detalleContratoFragment_to_nav_contratos);
                    break;

                case MOSTRAR_MENSAJE_EXITO_RENOVAR:
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Contrato renovado")
                            .setMessage("La renovación se realizó correctamente.")
                            .setPositiveButton("OK", null)
                            .show();
                    break;

                case MOSTRAR_MENSAJE_ERROR_RENOVAR:
                    String detalle = vm.getMensajeRenovacion().getValue();

                    String msgError;
                    if (detalle != null && detalle.toLowerCase().contains("ya tiene un contrato vigente")) {
                        msgError = "Este inmueble ya tiene un contrato activo.\nNo se puede renovar el contrato anterior.";
                    } else {
                        msgError = "No se pudo renovar el contrato.";
                    }

                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Error al renovar")
                            .setMessage(msgError)
                            .setPositiveButton("OK", null)
                            .show();
                    break;

            }
        });


        // ===== ✅ NUEVO: Observer para abrir diálogo de renovación =====
        vm.getMostrarDialogoRenovar().observe(getViewLifecycleOwner(), show -> {
            if (show == null || !show) return;

            DialogRenovarContratoBinding dialogBinding =
                    DialogRenovarContratoBinding.inflate(getLayoutInflater());

            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Renovar Contrato")
                    .setView(dialogBinding.getRoot())
                    .setPositiveButton("Renovar", (dialog, which) -> {

                        String nuevaFechaInicio = dialogBinding.etNuevaFechaInicio.getText().toString();
                        String nuevaFechaFin = dialogBinding.etNuevaFechaFin.getText().toString();
                        String nuevoMonto = dialogBinding.etNuevoMonto.getText().toString();

                        vm.onConfirmarRenovacion(nuevaFechaInicio, nuevaFechaFin, nuevoMonto);
                    })
                    .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                    .show();

            vm.limpiarDialogoRenovar();
        });

        // ===== Botones =====
        binding.btnVerPagos.setOnClickListener(v -> vm.onVerPagosClick());
        binding.btnVolverContratos.setOnClickListener(v ->
                navController.navigate(R.id.action_detalleContratoFragment_to_nav_contratos)
        );
        binding.btnRescindirContrato.setOnClickListener(v -> vm.onRescindirClick());

        // ✅ NUEVO: botón renovar
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
