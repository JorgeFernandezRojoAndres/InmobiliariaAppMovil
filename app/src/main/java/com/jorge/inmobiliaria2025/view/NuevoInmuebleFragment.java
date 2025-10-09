package com.jorge.inmobiliaria2025.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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

        // 🔹 Observa el estado de guardado
        vm.getEstadoGuardado().observe(getViewLifecycleOwner(), estado -> {
            if (getContext() == null) return; // evita crash si el fragmento no está activo

            switch (estado) {
                case EXITO:
                    Toast.makeText(getContext(), "✅ Inmueble guardado correctamente", Toast.LENGTH_SHORT).show();

                    // Limpia los campos antes de salir
                    etDireccion.setText("");
                    etPrecio.setText("");
                    swDisponible.setChecked(false);

                    // Navega de vuelta al listado de inmuebles
                    navController.popBackStack();
                    break;

                case CAMPOS_VACIOS:
                    Toast.makeText(getContext(), "⚠️ Complete todos los campos", Toast.LENGTH_SHORT).show();
                    break;

                case PRECIO_INVALIDO:
                    Toast.makeText(getContext(), "❌ Precio inválido", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        // 🔹 Acción del botón Guardar
        btnGuardar.setOnClickListener(view -> {
            String direccion = etDireccion.getText().toString().trim();
            String precio = etPrecio.getText().toString().trim();
            boolean disponible = swDisponible.isChecked();

            if (direccion.isEmpty() || precio.isEmpty()) {
                Toast.makeText(getContext(), "Complete todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            vm.guardarInmueble(direccion, precio, disponible);
        });

        return v;
    }
}
