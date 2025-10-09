package com.jorge.inmobiliaria2025.view;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.jorge.inmobiliaria2025.MainActivity;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.data.SessionManager;
import com.jorge.inmobiliaria2025.data.network.ApiService;
import com.jorge.inmobiliaria2025.data.network.RetrofitClient;
import com.jorge.inmobiliaria2025.model.Propietario;
import com.jorge.inmobiliaria2025.viewmodel.PerfilViewModel;

import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PerfilFragment extends Fragment {

    private PerfilViewModel vm;
    private EditText etCodigo, etDni, etNombre, etApellido, etEmail, etPassword, etTelefono;
    private Button btnEditar, btnCerrar, btnCambiarClave;
    private boolean modoEdicion = false;
    private ImageView ivAvatar;
    private static final int PICK_IMAGE_REQUEST = 100;
    private Uri imageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_perfil, container, false);

        etCodigo = v.findViewById(R.id.etCodigo);
        etDni = v.findViewById(R.id.etDni);
        etNombre = v.findViewById(R.id.etNombre);
        etApellido = v.findViewById(R.id.etApellido);
        etEmail = v.findViewById(R.id.etEmail);
        etPassword = v.findViewById(R.id.etPassword);
        etTelefono = v.findViewById(R.id.etTelefono);
        ivAvatar = v.findViewById(R.id.ivAvatar);

        TextView tvEmail = v.findViewById(R.id.tvEmail);
        btnCerrar = v.findViewById(R.id.btnCerrarSesion);
        btnEditar = v.findViewById(R.id.btnEditarPerfil);
        btnCambiarClave = v.findViewById(R.id.btnCambiarClave);

        vm = new ViewModelProvider(this).get(PerfilViewModel.class);
        SessionManager sm = new SessionManager(requireContext());

        vm.getEmail().observe(getViewLifecycleOwner(), tvEmail::setText);

        vm.getPropietario().observe(getViewLifecycleOwner(), propietario -> {
            if (propietario != null) {
                etCodigo.setText(String.valueOf(propietario.getId()));
                etDni.setText(propietario.getDni());
                etNombre.setText(propietario.getNombre());
                etApellido.setText(propietario.getApellido());
                etEmail.setText(propietario.getEmail());
                etPassword.setText(propietario.getClave());
                etTelefono.setText(propietario.getTelefono());

                // ✅ Mostrar avatar sin cache
                String avatarFullUrl = sm.getAvatarFullUrl();
                if (avatarFullUrl != null && !avatarFullUrl.isEmpty()) {
                    String urlSinCache = avatarFullUrl + "?t=" + System.currentTimeMillis();
                    Glide.with(this)
                            .load(urlSinCache)
                            .skipMemoryCache(true)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.ic_person)
                            .circleCrop()
                            .into(ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_person);
                }
            }
        });

        vm.getCerrarSesionEvento().observe(getViewLifecycleOwner(), cerrar -> {
            if (cerrar) {
                Intent i = new Intent(requireContext(), LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                requireActivity().finish();
            }
        });

        btnCerrar.setOnClickListener(v1 -> vm.cerrarSesion(requireContext()));
        btnCambiarClave.setOnClickListener(v1 -> tvEmail.setText("🔒 Cambiar clave aún no implementado"));

        btnEditar.setOnClickListener(v1 -> {
            if (!modoEdicion) {
                setCamposEditable(true);
                btnEditar.setText("GUARDAR");
                modoEdicion = true;
                Toast.makeText(requireContext(), "✏️ Modo edición activado", Toast.LENGTH_SHORT).show();
            } else {
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

                // 🔄 Forzar actualización visual del avatar y header
                SessionManager smLocal = new SessionManager(requireContext());
                String urlNueva = smLocal.getAvatarFullUrl() + "?t=" + System.currentTimeMillis();

                Glide.get(requireContext()).clearMemory();
                new Thread(() -> Glide.get(requireContext()).clearDiskCache()).start();

                Glide.with(requireContext())
                        .load(urlNueva)
                        .skipMemoryCache(true)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.ic_person)
                        .circleCrop()
                        .into(ivAvatar);

                Propietario pActual = smLocal.obtenerPropietarioActual();
                if (getActivity() instanceof MainActivity && pActual != null) {
                    ((MainActivity) getActivity()).actualizarHeaderUsuario(pActual, pActual.getEmail());
                }
            }
        });

        // 📸 Solo permite cambiar avatar en modo edición
        ivAvatar.setOnClickListener(v2 -> {
            if (modoEdicion) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
            } else {
                Toast.makeText(requireContext(), "📝 Presiona 'Editar' para cambiar la foto de perfil", Toast.LENGTH_SHORT).show();
            }
        });

        vm.cargarPropietario();
        return v;
    }

    // 🔹 Subir avatar al servidor y actualizar SessionManager + header del Drawer
    private void subirAvatar() {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            byte[] bytes = buffer.toByteArray();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), bytes);
            MultipartBody.Part body = MultipartBody.Part.createFormData("archivo", "avatar.jpg", requestFile);

            ApiService api = RetrofitClient.getInstance(requireContext()).create(ApiService.class);
            Call<ResponseBody> call = api.subirAvatar(body);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseStr = response.body().string();
                            JSONObject json = new JSONObject(responseStr);
                            String newUrl = json.optString("avatarUrl", null);

                            if (newUrl != null && !newUrl.isEmpty()) {
                                SessionManager sm = new SessionManager(requireContext());
                                sm.actualizarAvatarDesdeServidor(newUrl);

                                String urlNueva = sm.getAvatarFullUrl() + "?t=" + System.currentTimeMillis();

                                Glide.with(requireContext())
                                        .load(urlNueva)
                                        .skipMemoryCache(true)
                                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                                        .placeholder(R.drawable.ic_person)
                                        .circleCrop()
                                        .into(ivAvatar);

                                Toast.makeText(getContext(), "✅ Avatar actualizado", Toast.LENGTH_SHORT).show();

                                // 🔄 Refrescar cabecera del Drawer inmediatamente
                                Propietario p = sm.obtenerPropietarioActual();
                                if (getActivity() instanceof MainActivity && p != null) {
                                    ((MainActivity) getActivity()).actualizarHeaderUsuario(p, p.getEmail());
                                }

                            } else {
                                Toast.makeText(getContext(), "⚠️ Respuesta inesperada del servidor", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("PerfilFragment", "Error al procesar respuesta: " + e.getMessage());
                        }
                    } else {
                        Toast.makeText(getContext(), "❌ Error al subir imagen (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Toast.makeText(getContext(), "⚠️ Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e("PerfilFragment", "Error al subir avatar: " + e.getMessage());
            Toast.makeText(getContext(), "⚠️ No se pudo leer la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            subirAvatar();
        }
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
