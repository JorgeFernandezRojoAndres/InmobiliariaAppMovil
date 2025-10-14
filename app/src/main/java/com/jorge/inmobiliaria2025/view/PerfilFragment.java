package com.jorge.inmobiliaria2025.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jorge.inmobiliaria2025.MainActivity;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.data.SessionManager;
import com.jorge.inmobiliaria2025.viewmodel.PerfilViewModel;

public class PerfilFragment extends Fragment {

    private PerfilViewModel vm;
    private EditText etCodigo, etDocumento, etNombre, etApellido, etEmail, etTelefono;
    private Button btnEditar, btnCerrar, btnCambiarClave;
    private boolean modoEdicion = false;
    private ImageView ivAvatar;
    private SessionManager sm;
    private TextView tvEmail;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.PickVisualMedia(),
                    uri -> vm.procesarResultadoImagen(uri, requireContext())
            );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_perfil, container, false);
        inicializarVistas(v);

        vm = new ViewModelProvider(this).get(PerfilViewModel.class);
        sm = new SessionManager(requireContext());

        // 📧 Email
        vm.getEmail().observe(getViewLifecycleOwner(), tvEmail::setText);

        // 👤 Datos del propietario
        vm.getPropietario().observe(getViewLifecycleOwner(), propietario -> {
            etCodigo.setText(String.valueOf(propietario.getId()));
            etDocumento.setText(propietario.getDocumento());
            etNombre.setText(propietario.getNombre());
            etApellido.setText(propietario.getApellido());
            etEmail.setText(propietario.getEmail());
            etTelefono.setText(propietario.getTelefono());
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
                    .into(ivAvatar);
        });

        // 🚪 Eventos controlados desde ViewModel
        vm.getCerrarSesionEvento().observe(getViewLifecycleOwner(), cerrar -> manejarCierreSesion());
        vm.getAbrirGaleriaEvento().observe(getViewLifecycleOwner(), abrir ->
                pickImageLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );

        vm.getMensajeEvento().observe(getViewLifecycleOwner(),
                msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());

        vm.getActivarEdicionEvento().observe(getViewLifecycleOwner(), u -> activarModoEdicion());
        vm.getGuardarCambiosEvento().observe(getViewLifecycleOwner(), u -> guardarCambios());

        vm.getEventoActualizarHeader().observe(getViewLifecycleOwner(),
                propietario -> ((MainActivity) requireActivity())
                        .actualizarHeaderUsuario(propietario, propietario.getEmail()));

        vm.getPermitirCambioAvatar().observe(getViewLifecycleOwner(), permitir -> {
            ivAvatar.setEnabled(permitir);
            ivAvatar.setAlpha(permitir ? 1f : 0.5f);
        });

        btnCerrar.setOnClickListener(v1 -> vm.cerrarSesion(requireContext()));

        // 🔐 NUEVO: Diálogo de cambio de contraseña
        btnCambiarClave.setOnClickListener(v1 -> {
            View dialogView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.dialog_cambiar_clave, null);
            EditText etActual = dialogView.findViewById(R.id.etClaveActual);
            EditText etNueva = dialogView.findViewById(R.id.etNuevaClave);

            new AlertDialog.Builder(requireContext())
                    .setTitle("Cambiar contraseña")
                    .setView(dialogView)
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        String actual = etActual.getText().toString().trim();
                        String nueva = etNueva.getText().toString().trim();
                        vm.cambiarClave(actual, nueva, requireContext());
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        btnEditar.setOnClickListener(v1 -> vm.alternarModoEdicion());
        ivAvatar.setOnClickListener(v2 -> vm.onAvatarClick(modoEdicion));

        vm.cargarPerfilDesdeApi(requireContext());

        // 🔒 Ocultar ID (presente internamente, pero no visible ni editable)
        etCodigo.setVisibility(View.GONE);

        return v;
    }

    private void inicializarVistas(View v) {
        etCodigo = v.findViewById(R.id.etCodigo);
        etDocumento = v.findViewById(R.id.etDni);
        etNombre = v.findViewById(R.id.etNombre);
        etApellido = v.findViewById(R.id.etApellido);
        etEmail = v.findViewById(R.id.etEmail);
        etTelefono = v.findViewById(R.id.etTelefono);
        ivAvatar = v.findViewById(R.id.ivAvatar);
        tvEmail = v.findViewById(R.id.tvEmail);
        btnCerrar = v.findViewById(R.id.btnCerrarSesion);
        btnEditar = v.findViewById(R.id.btnEditarPerfil);
        btnCambiarClave = v.findViewById(R.id.btnCambiarClave);
    }

    private void manejarCierreSesion() {
        Intent i = new Intent(requireContext(), LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        requireActivity().finish();
    }

    private void activarModoEdicion() {
        setCamposEditable(true);
        btnEditar.setText("GUARDAR");
        modoEdicion = true;
        vm.emitirMensaje("✏️ Modo edición activado");
        vm.setPermitirCambioAvatar(true);
    }

    private void guardarCambios() {
        vm.guardarCambiosPerfil(
                etCodigo.getText().toString(),
                etDocumento.getText().toString(),
                etNombre.getText().toString(),
                etApellido.getText().toString(),
                etEmail.getText().toString(),
                "", // 🔹 contraseña eliminada del flujo
                etTelefono.getText().toString(),
                requireContext()
        );

        setCamposEditable(false);
        btnEditar.setText("EDITAR");
        modoEdicion = false;
        vm.setPermitirCambioAvatar(false);
    }

    private void setCamposEditable(boolean editable) {
        etCodigo.setEnabled(false); // nunca editable
        etDocumento.setEnabled(editable);
        etNombre.setEnabled(editable);
        etApellido.setEnabled(editable);
        etEmail.setEnabled(editable);
        etTelefono.setEnabled(editable);
    }
}
