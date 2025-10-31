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

        // ✅ Obtener NavController una sola vez
        NavHostFragment navHostFragment = (NavHostFragment)
                requireActivity().getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);
        navController = navHostFragment.getNavController();

        // ✅ ViewModel scoped al nav_graph
        vm = new ViewModelProvider(
                navController.getViewModelStoreOwner(R.id.nav_graph)
        ).get(DetalleContratoViewModel.class);

        // ✅ Logs diagnósticos
        DebugNavTracker.logFragment(this, "Detalle_onCreateView");
        DebugNavTracker.logNavController(navController, "Detalle_onCreateView");
        DebugNavTracker.logViewModel(vm, "Detalle_onCreateView");

        // ✅ Observa contrato (UI reactiva)
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

        // ✅ Observa acción de navegación a Pagos
        vm.getNavegarAPagos().observe(getViewLifecycleOwner(), args -> {
            if (args != null) {
                DebugNavTracker.logFragment(this, "Detalle_beforeNavigateToPagos");
                DebugNavTracker.logNavController(navController, "Detalle_beforeNavigateToPagos");
                DebugNavTracker.logViewModel(vm, "Detalle_beforeNavigateToPagos");

                navController.navigate(R.id.action_detalleContratoFragment_to_pagosFragment, args);
                vm.limpiarAccionNavegar();
            }
        });


        // ✅ Observa acciones de UI desde el ViewModel
        vm.getUiAccion().observe(getViewLifecycleOwner(), accion -> {
            if (accion == null) return;

            switch (accion) {

                case MOSTRAR_DIALOGO_CONFIRMACION:
                    new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Confirmar acción")
                            .setMessage("¿Seguro que querés rescindir este contrato? Esta acción no se puede deshacer.")
                            .setPositiveButton("Rescindir", (dialog, which) -> vm.confirmarRescision())
                            .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                            .show();
                    break;


                case MOSTRAR_MENSAJE_EXITO:
                    // ✅ Mostrar multa si está en el último mensaje del backend
                    String ultimaRespuesta = vm.getUltimoMensaje(); // añadimos getter abajo
                    String multa = null;
                    try {
                        if (ultimaRespuesta != null && ultimaRespuesta.contains("multa")) {
                            org.json.JSONObject json = new org.json.JSONObject(ultimaRespuesta);
                            multa = json.optString("multa", null);
                        }
                    } catch (Exception ignored) {}

                    String mensaje = (multa != null)
                            ? "Contrato rescindido correctamente.\nMulta: $" + multa
                            : "Contrato rescindido correctamente.";

                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Éxito")
                            .setMessage(mensaje)
                            .setPositiveButton("OK", null)
                            .show();
                    break;

                case MOSTRAR_MENSAJE_ERROR:
                    new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("Error")
                            .setMessage("No se pudo rescindir el contrato.")
                            .setPositiveButton("OK", null)
                            .show();
                    break;

                case VOLVER_A_CONTRATOS:
                    navController.navigate(R.id.action_detalleContratoFragment_to_nav_contratos);
                    break;
            }
        });


        // ✅ Acciones de botones
        binding.btnVerPagos.setOnClickListener(v -> vm.onVerPagosClick());
        binding.btnVolverContratos.setOnClickListener(v ->
                navController.navigate(R.id.action_detalleContratoFragment_to_nav_contratos)
        );
        binding.btnRescindirContrato.setOnClickListener(v -> vm.onRescindirClick());


        // ✅ Inicializar argumentos
        vm.inicializarDesdeArgs(getArguments());

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        DebugNavTracker.logFragment(this, "Detalle_onDestroyView");
        super.onDestroyView();
        binding = null;
    }
}
