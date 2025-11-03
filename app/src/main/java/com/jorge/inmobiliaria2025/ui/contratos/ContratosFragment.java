package com.jorge.inmobiliaria2025.ui.contratos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.FragmentContratosBinding;

import java.util.ArrayList;

public class ContratosFragment extends Fragment {

    private static final String TAG = "CONTRATOS";
    private ContratosViewModel vm;
    private ContratoAdapter adapter;
    private RecyclerView rv;
    private FragmentContratosBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "🧩 onCreateView() iniciado");
        binding = FragmentContratosBinding.inflate(inflater, container, false);
        vm = new ViewModelProvider(this).get(ContratosViewModel.class);
        Log.d(TAG, "✅ ViewModel de Contratos creado correctamente");

        // RecyclerView
        rv = binding.rvContratos;
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ContratoAdapter(new ArrayList<>(), vm::onContratoSeleccionado);
        rv.setAdapter(adapter);

        // Lista de contratos
        vm.getContratos().observe(getViewLifecycleOwner(), contratos -> {
            adapter.updateData(contratos);
        });

        // Navegación a detalle
        vm.getAccionNavegarADetalle().observe(getViewLifecycleOwner(), args -> {
            if (args == null) return;
            NavController navController = NavHostFragment.findNavController(this);
            NavOptions options = new NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left)
                    .setPopEnterAnim(R.anim.slide_in_left)
                    .setPopExitAnim(R.anim.slide_out_right)
                    .build();
            navController.navigate(R.id.action_contratosFragment_to_detalleContratoFragment, args, options);
        });

        // ✅ Siempre cargar todos
        vm.cargarContratos();

        // 🔙 Botón atrás controlado por el ViewModel
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Log.d(TAG, "🔙 Botón Atrás presionado -> evento enviado al ViewModel");
                        vm.onVolverAlMapa(); // 👉 Notifica al ViewModel
                    }
                }
        );

        // 🧭 Observa el evento de navegación al mapa
        vm.getVolverAlMapaEvent().observe(getViewLifecycleOwner(), volver -> {
            if (Boolean.TRUE.equals(volver)) {
                Log.d(TAG, "🗺️ Navegando al mapa desde el observer");
                NavController navController = NavHostFragment.findNavController(ContratosFragment.this);
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true)
                        .setEnterAnim(R.anim.slide_in_left)
                        .setExitAnim(R.anim.slide_out_right)
                        .build();
                navController.navigate(R.id.nav_ubicacion, null, options);
            }
        });

        return binding.getRoot();
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 onResume() -> ContratosFragment activo");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "🧹 onDestroyView() ejecutado (ContratosFragment)");
    }
}
