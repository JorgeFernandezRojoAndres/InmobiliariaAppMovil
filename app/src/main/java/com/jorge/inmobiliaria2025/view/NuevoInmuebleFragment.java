package com.jorge.inmobiliaria2025.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.viewmodel.InmuebleViewModel;

public class NuevoInmuebleFragment extends Fragment {

    private EditText etDireccion, etPrecio;
    private SwitchCompat swDisponible;
    private Button btnGuardar;
    private InmuebleViewModel vm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_nuevo_inmueble, container, false);

        etDireccion = v.findViewById(R.id.etDireccion);
        etPrecio = v.findViewById(R.id.etPrecio);
        swDisponible = v.findViewById(R.id.swDisponibleForm);
        btnGuardar = v.findViewById(R.id.btnGuardar);

        vm = new ViewModelProvider(requireActivity()).get(InmuebleViewModel.class);
        NavController navController = NavHostFragment.findNavController(this);

        // 🔹 Observa mensajes ya preparados por el ViewModel
        vm.getMensajeToast().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje == null || getContext() == null) return;
            vm.mostrarToast(getContext(), mensaje);
        });

        // 🔹 Observa navegación controlada desde el ViewModel
        vm.getNavegarAtras().observe(getViewLifecycleOwner(), navegar -> {
            if (Boolean.TRUE.equals(navegar)) {
                limpiarCampos();
                navController.popBackStack();
            }
        });

        // 🔹 Acción del botón Guardar
        btnGuardar.setOnClickListener(view -> {
            vm.procesarGuardado(
                    etDireccion.getText().toString().trim(),
                    etPrecio.getText().toString().trim(),
                    swDisponible.isChecked()
            );
        });

        return v;
    }

    private void limpiarCampos() {
        etDireccion.setText("");
        etPrecio.setText("");
        swDisponible.setChecked(false);
    }
}
