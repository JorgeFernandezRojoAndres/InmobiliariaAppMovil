package com.jorge.inmobiliaria2025.view;

import android.content.Intent;
import android.net.Uri;
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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jorge.inmobiliaria2025.MainActivity;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.data.SessionManager;
import com.jorge.inmobiliaria2025.model.Propietario;
import com.jorge.inmobiliaria2025.viewmodel.PerfilViewModel;

public class PerfilFragment extends Fragment {

    private PerfilViewModel vm;
    private EditText etCodigo, etDni, etNombre, etApellido, etEmail, etPassword, etTelefono;
    private Button btnEditar, btnCerrar, btnCambiarClave;
    private boolean modoEdicion = false;
    private ImageView ivAvatar;
    private SessionManager sm;
    private TextView tvEmail;
    private Uri imageUri;

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

        // 📧 Email visible
        vm.getEmail().observe(getViewLifecycleOwner(), tvEmail::setText);

        // 👤 Datos del propietario
        vm.getPropietario().observe(getViewLifecycleOwner(), propietario -> {
            etCodigo.setText(String.valueOf(propietario.getId()));
            etDni.setText(propietario.getDni());
            etNombre.setText(propietario.getNombre());
            etApellido.setText(propietario.getApellido());
            etEmail.setText(propietario.getEmail());
            etPassword.setText(propietario.getClave());
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

        // 🚪 Cerrar sesión
        vm.getCerrarSesionEvento().observe(getViewLifecycleOwner(), cerrar -> manejarCierreSesion());

        // 📸 Evento abrir galería
        vm.getAbrirGaleriaEvento().observe(getViewLifecycleOwner(), abrir ->
                pickImageLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build())
        );

        // 💬 Mensajes del ViewModel
        vm.getMensajeEvento().observe(getViewLifecycleOwner(),
                msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());

        vm.getActivarEdicionEvento().observe(getViewLifecycleOwner(), u -> activarModoEdicion());
        vm.getGuardarCambiosEvento().observe(getViewLifecycleOwner(), u -> guardarCambios());


        // 🧭 Evento para actualizar header (sustituye el if final)
        vm.getEventoActualizarHeader().observe(getViewLifecycleOwner(), propietario ->
                ((MainActivity) requireActivity()).actualizarHeaderUsuario(propietario, propietario.getEmail())
        );

        btnCerrar.setOnClickListener(v1 -> vm.cerrarSesion(requireContext()));
        btnCambiarClave.setOnClickListener(v1 -> tvEmail.setText("🔒 Cambiar clave aún no implementado"));
        btnEditar.setOnClickListener(v1 -> vm.alternarModoEdicion());
        ivAvatar.setOnClickListener(v2 -> vm.onAvatarClick(modoEdicion));

        vm.cargarPropietario();
        return v;
    }

    // ------------------ MÉTODOS PRIVADOS ------------------

    private void inicializarVistas(View v) {
        etCodigo = v.findViewById(R.id.etCodigo);
        etDni = v.findViewById(R.id.etDni);
        etNombre = v.findViewById(R.id.etNombre);
        etApellido = v.findViewById(R.id.etApellido);
        etEmail = v.findViewById(R.id.etEmail);
        etPassword = v.findViewById(R.id.etPassword);
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
    }

    private void guardarCambios() {
        Propietario p = new Propietario();
        p.setId(Integer.parseInt(etCodigo.getText().toString()));
        p.setDni(etDni.getText().toString());
        p.setNombre(etNombre.getText().toString());
        p.setApellido(etApellido.getText().toString());
        p.setEmail(etEmail.getText().toString());
        p.setClave(etPassword.getText().toString());
        p.setTelefono(etTelefono.getText().toString());

        vm.actualizarPropietario(p);
        setCamposEditable(false);
        btnEditar.setText("EDITAR");
        modoEdicion = false;
        tvEmail.setText("✅ Datos actualizados correctamente");
    }

    private void setCamposEditable(boolean editable) {
        etDni.setEnabled(editable);
        etNombre.setEnabled(editable);
        etApellido.setEnabled(editable);
        etEmail.setEnabled(editable);
        etPassword.setEnabled(editable);
        etTelefono.setEnabled(editable);
    }
}
