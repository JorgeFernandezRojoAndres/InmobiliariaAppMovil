package com.jorge.inmobiliaria2025.ui.contratos;

import android.os.Bundle;
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
import com.jorge.inmobiliaria2025.utils.DebugNavTracker; // ✅ agregado

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

        // ✅ Obtener navController 1 sola vez
        NavHostFragment navHostFragment = (NavHostFragment)
                requireActivity().getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

        navController = navHostFragment.getNavController();

        // ✅ ViewModel scoped al nav_graph
        vm = new ViewModelProvider(
                navController.getViewModelStoreOwner(R.id.nav_graph)
        ).get(DetalleContratoViewModel.class);

        // ✅ Logs diagnóstico
        DebugNavTracker.logFragment(this, "Detalle_onCreateView");
        DebugNavTracker.logNavController(navController, "Detalle_onCreateView");
        DebugNavTracker.logViewModel(vm, "Detalle_onCreateView");

        // ✅ Observa contrato
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

        // ✅ Botón ver pagos
        binding.btnVerPagos.setOnClickListener(v -> vm.onVerPagosClick());

        // ✅ Botón volver directo a contratos (atajo correcto)
        binding.btnVolverContratos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navController.navigate(R.id.action_detalleContratoFragment_to_nav_contratos);
            }
        });

        // ✅ Navegación a Pagos
        vm.getNavegarAPagos().observe(getViewLifecycleOwner(), args -> {
            if (args != null) {

                // 📌 Logs antes de navegar
                DebugNavTracker.logFragment(this, "Detalle_beforeNavigateToPagos");
                DebugNavTracker.logNavController(navController, "Detalle_beforeNavigateToPagos");
                DebugNavTracker.logViewModel(vm, "Detalle_beforeNavigateToPagos");

                navController.navigate(R.id.action_detalleContratoFragment_to_pagosFragment, args);
                vm.limpiarAccionNavegar();
            }
        });

        // ✅ Inicializa args
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
