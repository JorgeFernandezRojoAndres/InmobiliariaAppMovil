package com.jorge.inmobiliaria2025.view;

import android.app.Activity;
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
import com.jorge.inmobiliaria2025.viewmodel.InmuebleViewModel;

public class NuevoInmuebleFragment extends Fragment {

    private EditText etDireccion, etPrecio;
    private SwitchCompat swDisponible;
    private Button btnGuardar, btnSeleccionarImagen;
    private ImageView ivPreview;
    private Uri imagenUriSeleccionada;
    private InmuebleViewModel vm;

    // 📷 Launcher para seleccionar imagen
    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imagenUriSeleccionada = result.getData().getData();
                    ivPreview.setImageURI(imagenUriSeleccionada);
                }
            });

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
        btnSeleccionarImagen = v.findViewById(R.id.btnSeleccionarImagen);
        ivPreview = v.findViewById(R.id.ivPreview);

        vm = new ViewModelProvider(requireActivity()).get(InmuebleViewModel.class);
        NavController navController = NavHostFragment.findNavController(this);

        // 🧩 Observa mensajes
        vm.getMensajeToast().observe(getViewLifecycleOwner(), mensaje -> {
            if (mensaje == null || getContext() == null) return;
            vm.mostrarToast(getContext(), mensaje);
        });

        // 🧩 Navegación atrás controlada por ViewModel
        vm.getNavegarAtras().observe(getViewLifecycleOwner(), navegar -> {
            if (Boolean.TRUE.equals(navegar)) {
                limpiarCampos();
                navController.popBackStack();
            }
        });

        // 📷 Botón seleccionar imagen
        btnSeleccionarImagen.setOnClickListener(vw -> abrirSelectorImagen());

        // 💾 Botón guardar inmueble
        btnGuardar.setOnClickListener(view -> {
            String direccion = etDireccion.getText().toString().trim();
            String precioTexto = etPrecio.getText().toString().trim();
            boolean disponible = swDisponible.isChecked();

            // ✅ Llamada corregida al ViewModel
            vm.procesarGuardado(direccion, precioTexto, disponible);

            // 🔹 Si hay imagen seleccionada, subirla luego de guardar
            if (imagenUriSeleccionada != null) {
                vm.subirImagenInmueble(1, imagenUriSeleccionada); // 👈 reemplazar por ID real del backend
            }
        });

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
        swDisponible.setChecked(false);
        ivPreview.setImageResource(R.drawable.ic_image_placeholder);
        imagenUriSeleccionada = null;
    }
}
