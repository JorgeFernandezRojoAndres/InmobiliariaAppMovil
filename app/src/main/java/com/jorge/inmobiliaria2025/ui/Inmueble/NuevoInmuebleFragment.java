package com.jorge.inmobiliaria2025.ui.Inmueble;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.jorge.inmobiliaria2025.R;

public class NuevoInmuebleFragment extends Fragment {

    private EditText etDireccion, etPrecio, etMetros;
    private SwitchCompat swDisponible;
    private Button btnGuardar, btnSeleccionarImagen;
    private ImageView ivPreview;
    private Uri imagenUriSeleccionada;
    private InmuebleViewModel vm;

    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->
                    vm.procesarSeleccionImagen(result, ivPreview)
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_nuevo_inmueble, container, false);

        etDireccion = v.findViewById(R.id.etDireccion);
        etPrecio = v.findViewById(R.id.etPrecio);
        etMetros = v.findViewById(R.id.etMetros); // 🆕 nuevo campo
        swDisponible = v.findViewById(R.id.swDisponibleForm);
        btnGuardar = v.findViewById(R.id.btnGuardar);
        btnSeleccionarImagen = v.findViewById(R.id.btnSeleccionarImagen);
        ivPreview = v.findViewById(R.id.ivPreview);

        vm = new ViewModelProvider(requireActivity()).get(InmuebleViewModel.class);
        NavController navController = NavHostFragment.findNavController(this);

        // 🔹 Observadores (sin if)
        vm.getMensajeToast().observe(getViewLifecycleOwner(),
                mensaje -> vm.mostrarToast(requireContext(), mensaje));

        vm.getAccionLimpiarCampos().observe(getViewLifecycleOwner(),
                limpiar -> limpiarCampos());

        vm.getAccionNavegarAtras().observe(getViewLifecycleOwner(),
                accion -> navController.popBackStack());

        btnSeleccionarImagen.setOnClickListener(vw -> abrirSelectorImagen());

        btnGuardar.setOnClickListener(view -> vm.guardarInmueble(
                etDireccion.getText().toString(),
                etPrecio.getText().toString(),
                etMetros.getText().toString(), // 🆕 se pasa como string
                swDisponible.isChecked(),
                vm.getImagenUriSeleccionada().getValue()
        ));

        return v;
    }

    private void abrirSelectorImagen() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        seleccionarImagenLauncher.launch(intent);
    }

    private void limpiarCampos() {
        etDireccion.setText("");
        etPrecio.setText("");
        etMetros.setText(""); // 🆕 limpiar metros
        swDisponible.setChecked(false);
        ivPreview.setImageResource(R.drawable.ic_image_placeholder);
        imagenUriSeleccionada = null;
    }
}
