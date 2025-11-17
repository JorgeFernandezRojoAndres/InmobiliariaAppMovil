package com.jorge.inmobiliaria2025.ui.Inmueble;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.FragmentNuevoInmuebleBinding;
import com.jorge.inmobiliaria2025.model.TipoInmueble;

import java.util.ArrayList;
import java.util.List;

public class NuevoInmuebleFragment extends Fragment {

    private FragmentNuevoInmuebleBinding binding;
    private InmuebleViewModel vm;

    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (vm != null) vm.procesarSeleccionImagen(result);
                    }
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentNuevoInmuebleBinding.inflate(inflater, container, false);
        View v = binding.getRoot();

        vm = new ViewModelProvider(requireActivity()).get(InmuebleViewModel.class);
        NavController navController = NavHostFragment.findNavController(this);

        // 🔹 Switch inactivo por defecto
        binding.swDisponibleForm.setChecked(false);
        binding.swDisponibleForm.setEnabled(false);

        // ==========================
        // 🔹 Observadores LiveData
        // ==========================

        // Mensajes
        vm.getMensajeToast().observe(getViewLifecycleOwner(),
                msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());

        // Limpiar campos
        vm.getAccionLimpiarCampos().observe(getViewLifecycleOwner(), limpiar -> {
            binding.etDireccion.setText("");
            binding.etPrecio.setText("");
            binding.etMetros.setText("");
            binding.ivPreview.setImageResource(R.drawable.ic_image_placeholder);
            binding.swDisponibleForm.setChecked(false);
            binding.spTipo.setSelection(0);
            binding.spUso.setSelection(0);
        });

        // Navegación atrás
        vm.getAccionNavegarAtras().observe(getViewLifecycleOwner(),
                accion -> navController.popBackStack());

        // Imagen seleccionada
        vm.getImagenUriSeleccionada().observe(getViewLifecycleOwner(), uri -> {
            if (uri != null) binding.ivPreview.setImageURI(uri);
        });

        // ==========================
        // 🔹 Spinner: Tipos desde API
        // ==========================
        vm.getTiposInmueble().observe(getViewLifecycleOwner(), tipos -> {
            List<String> nombresTipos = new ArrayList<>();
            for (TipoInmueble t : tipos) nombresTipos.add(t.getNombre());

            ArrayAdapter<String> adapterTipo = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_spinner_item,
                    nombresTipos
            );
            adapterTipo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spTipo.setAdapter(adapterTipo);
        });

        // ==========================
        // 🔹 Spinner de Uso (local)
        // ==========================
        ArrayAdapter<CharSequence> adapterUso = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.usos_inmueble,
                android.R.layout.simple_spinner_item
        );
        adapterUso.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spUso.setAdapter(adapterUso);

        // ==========================
        // 🔹 Selector de imagen
        // ==========================
        binding.btnSeleccionarImagen.setOnClickListener(vw -> abrirSelectorImagen());

        // ==========================
// 🔹 Guardar inmueble
// ==========================
        binding.btnGuardar.setOnClickListener(view -> {
            int posicionTipo = binding.spTipo.getSelectedItemPosition();
            String usoSeleccionado = binding.spUso.getSelectedItem().toString();

            vm.onGuardarInmuebleClick(
                    binding.etDireccion.getText().toString(),
                    binding.etPrecio.getText().toString(),
                    binding.etMetros.getText().toString(),
                    posicionTipo,
                    usoSeleccionado,
                    vm.getImagenUriSeleccionada().getValue()
            );
        });


        return v;
    }

    private void abrirSelectorImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        seleccionarImagenLauncher.launch(intent);
    }
}
