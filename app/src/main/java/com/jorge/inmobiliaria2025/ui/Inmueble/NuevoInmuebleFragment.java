package com.jorge.inmobiliaria2025.ui.Inmueble;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class NuevoInmuebleFragment extends Fragment {

    private FragmentNuevoInmuebleBinding binding;
    private InmuebleViewModel vm;

    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (vm != null) {
                            // ✅ Solo pasamos el resultado al ViewModel
                            vm.procesarSeleccionImagen(result);
                        }
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

        // Observadores LiveData
        vm.getMensajeToast().observe(getViewLifecycleOwner(),
                mensaje -> Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show());

        vm.getAccionLimpiarCampos().observe(getViewLifecycleOwner(),
                limpiar -> {
                    binding.etDireccion.setText("");
                    binding.etPrecio.setText("");
                    binding.etMetros.setText("");
                    binding.swDisponibleForm.setChecked(false);
                    binding.ivPreview.setImageResource(R.drawable.ic_image_placeholder);
                });

        vm.getAccionNavegarAtras().observe(getViewLifecycleOwner(),
                accion -> navController.popBackStack());

        vm.getImagenUriSeleccionada().observe(getViewLifecycleOwner(), uri ->
                binding.ivPreview.setImageURI(uri)
        );

        // Selector de imagen
        binding.btnSeleccionarImagen.setOnClickListener(vw -> abrirSelectorImagen());

        // Guardar inmueble (validaciones dentro del ViewModel)
        binding.btnGuardar.setOnClickListener(view -> vm.guardarInmueble(
                binding.etDireccion.getText().toString(),
                binding.etPrecio.getText().toString(),
                binding.etMetros.getText().toString(),
                binding.swDisponibleForm.isChecked(),
                vm.getImagenUriSeleccionada().getValue()
        ));

        return v;
    }

    private void abrirSelectorImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        seleccionarImagenLauncher.launch(intent);
    }

}
