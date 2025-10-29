package com.jorge.inmobiliaria2025.ui.perfil;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.jorge.inmobiliaria2025.ui.nav.MainViewModel;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jorge.inmobiliaria2025.ui.nav.MainActivity;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.DialogCambiarClaveBinding;
import com.jorge.inmobiliaria2025.databinding.FragmentPerfilBinding;
import com.jorge.inmobiliaria2025.ui.login.LoginActivity;

public class PerfilFragment extends Fragment {

    private PerfilViewModel vm;
    private FragmentPerfilBinding binding;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.PickVisualMedia(),
                    uri -> vm.procesarResultadoImagen(uri)
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentPerfilBinding.inflate(inflater, container, false);
        vm = new ViewModelProvider(this).get(PerfilViewModel.class);

        // 📧 Email
        vm.getEmail().observe(getViewLifecycleOwner(), binding.tvEmail::setText);

        // 👤 Datos del propietario
        vm.getPropietario().observe(getViewLifecycleOwner(), propietario -> {
            binding.etCodigo.setText(String.valueOf(propietario.getId()));
            binding.etDocumento.setText(propietario.getDocumento());
            binding.etNombre.setText(propietario.getNombre());
            binding.etApellido.setText(propietario.getApellido());
            binding.etEmail.setText(propietario.getEmail());
            binding.etTelefono.setText(propietario.getTelefono());
        });

        // 🖼️ Avatar reactivo
        vm.getAvatarUrl().observe(getViewLifecycleOwner(), url -> {
            String urlSinCache = (url == null || url.isEmpty()) ? null : url + "?t=" + System.currentTimeMillis();
            Glide.with(this)
                    .load(urlSinCache != null ? urlSinCache : R.drawable.ic_person)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .placeholder(R.drawable.ic_person)
                    .circleCrop()
                    .into(binding.ivAvatar);
        });

        // 🚪 Eventos controlados desde ViewModel
        vm.getCerrarSesionEvento().observe(getViewLifecycleOwner(), cerrar -> manejarCierreSesion());
        vm.getAbrirGaleriaEvento().observe(getViewLifecycleOwner(), abrir ->
                pickImageLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );
        vm.getMostrarDialogoClave().observe(getViewLifecycleOwner(), u -> mostrarDialogoCambioClave());
        // 💬 Mensajes del ViewModel
        vm.getMensajeEvento().observe(getViewLifecycleOwner(),
                msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());

        vm.getActivarEdicionEvento().observe(getViewLifecycleOwner(), u -> setCamposEditable(true));
        vm.getGuardarCambiosEvento().observe(getViewLifecycleOwner(), u -> setCamposEditable(false));

        vm.getEventoActualizarHeader().observe(getViewLifecycleOwner(), propietario -> {
            MainViewModel mainVM = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
            mainVM.actualizarHeader(propietario);
        });





        vm.getPermitirCambioAvatar().observe(getViewLifecycleOwner(), permitir -> {
            binding.ivAvatar.setEnabled(permitir);
            binding.ivAvatar.setAlpha(permitir ? 1f : 0.5f);
        });
//
        // ✅ Botones delegan la acción al ViewModel
        binding.btnCerrarSesion.setOnClickListener(v -> vm.onCerrarSesionClick());
        binding.btnCambiarClave.setOnClickListener(v -> vm.onCambiarClaveClick());
        binding.btnEditarPerfil.setOnClickListener(v ->
                vm.onEditarPerfilClick(
                        binding.etNombre.getText().toString().trim(),
                        binding.etApellido.getText().toString().trim(),
                        binding.etEmail.getText().toString().trim(),
                        binding.etTelefono.getText().toString().trim()
                ));
        binding.ivAvatar.setOnClickListener(v -> vm.onAvatarClick());

        vm.cargarPerfilDesdeApi();
        binding.etCodigo.setVisibility(View.GONE);


        return binding.getRoot();
    }


    // 🔐 Diálogo de cambio de contraseña con ViewBinding
    private void mostrarDialogoCambioClave() {
        DialogCambiarClaveBinding dialogBinding = DialogCambiarClaveBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Cambiar contraseña")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Aceptar", null)
                .setNegativeButton("Cancelar", (d, which) -> d.dismiss())
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String actual = dialogBinding.etClaveActual.getText().toString().trim();
            String nueva = dialogBinding.etNuevaClave.getText().toString().trim();
            vm.cambiarClave(actual, nueva);
            dialog.dismiss();
        }));

        dialog.show();
    }
    private void setCamposEditable(boolean editable) {
        binding.etCodigo.setEnabled(false);
        binding.etDocumento.setEnabled(false); // DNI nunca editable
        binding.etNombre.setEnabled(editable);
        binding.etApellido.setEnabled(editable);
        binding.etEmail.setEnabled(editable);
        binding.etTelefono.setEnabled(editable);
        binding.btnEditarPerfil.setText(editable ? "GUARDAR" : "EDITAR");
    }

    private void manejarCierreSesion() {
        Intent i = new Intent(requireContext(), LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        requireActivity().finish();
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
