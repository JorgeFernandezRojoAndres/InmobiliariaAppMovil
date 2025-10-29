package com.jorge.inmobiliaria2025.ui.contratos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

public class ContratosFragment extends Fragment {

    private static final String TAG = "CONTRATOS";
    private ContratosViewModel vm;
    private ContratoAdapter adapter;
    private RecyclerView rv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Log.d(TAG, "🧩 onCreateView() iniciado");
        FragmentContratosBinding binding = FragmentContratosBinding.inflate(inflater, container, false);
        vm = new ViewModelProvider(this).get(ContratosViewModel.class);
        Log.d(TAG, "✅ ViewModel de Contratos creado correctamente");

        // 🟢 Configurar RecyclerView
        rv = binding.rvContratos;
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new ContratoAdapter(null, vm::onContratoSeleccionado);
        rv.setAdapter(adapter);

        // 🧠 Observa lista de contratos
        vm.getContratos().observe(getViewLifecycleOwner(), contratos -> {
            Log.d(TAG, "📡 Lista de contratos recibida desde ViewModel (" + (contratos != null ? contratos.size() : 0) + ")");
            adapter.updateData(contratos);
        });


        // ✅ Observa evento de navegación al detalle
        vm.getAccionNavegarADetalle().observe(getViewLifecycleOwner(), args -> {
            if (args == null) {
                Log.w(TAG, "⚠️ Navegación ignorada: args == null");
                return;
            }

            Log.d(TAG, "➡️ Navegando hacia DetalleContratoFragment con args=" + args);
            NavController navController = NavHostFragment.findNavController(this);

            try {
                // ⚙️ Opciones para mantener el backstack limpio
                NavOptions options = new NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setEnterAnim(R.anim.slide_in_right)
                        .setExitAnim(R.anim.slide_out_left)
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build();

                navController.navigate(R.id.action_contratosFragment_to_detalleContratoFragment, args, options);
                Log.d(TAG, "✅ Navegación ejecutada correctamente hacia DetalleContratoFragment");
            } catch (Exception e) {
                Log.e(TAG, "💥 Error al navegar a detalle: " + e.getMessage(), e);
            }
        });

        // 🔄 Cargar contratos al iniciar
        Log.d(TAG, "🚀 Solicitando carga de contratos...");
        vm.cargarContratos();

        // 🔙 Manejo del botón Atrás → volver al mapa (Inicio)
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        Log.d(TAG, "🔙 Botón Atrás presionado en ContratosFragment → Volviendo al mapa");
                        NavController navController = NavHostFragment.findNavController(ContratosFragment.this);

                        try {
                            NavOptions options = new NavOptions.Builder()
                                    .setPopUpTo(R.id.nav_graph, true)
                                    .setEnterAnim(R.anim.slide_in_left)
                                    .setExitAnim(R.anim.slide_out_right)
                                    .build();

                            navController.navigate(R.id.nav_ubicacion, null, options);
                            Log.d(TAG, "✅ Navegación hacia nav_ubicacion ejecutada correctamente");
                        } catch (Exception e) {
                            Log.e(TAG, "💥 Error al volver al mapa: " + e.getMessage(), e);
                        }
                    }
                }
        );

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
