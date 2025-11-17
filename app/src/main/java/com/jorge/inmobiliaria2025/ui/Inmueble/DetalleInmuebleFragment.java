package com.jorge.inmobiliaria2025.ui.Inmueble;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.FragmentDetalleInmuebleBinding;
import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.model.TipoInmueble;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DetalleInmuebleFragment extends Fragment {

    private FragmentDetalleInmuebleBinding binding;
    private DetalleInmuebleViewModel vm;
    private Uri imagenSeleccionadaUri;
    private ArrayAdapter<String> tipoAdapter;
    private final List<TipoInmueble> listaTipos = new ArrayList<>();

    // ✅ Limpio: sin pasar ImageView al ViewModel
    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> vm.procesarSeleccionImagen(result));

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDetalleInmuebleBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(this).get(DetalleInmuebleViewModel.class);
        vm.cargarInmueble((Inmueble) requireArguments().getSerializable("inmueble"));

        //  observador para imagen seleccionada (reemplaza setImageURI)
        vm.getImagenSeleccionada().observe(getViewLifecycleOwner(), uri -> {
            imagenSeleccionadaUri = uri;
            if (uri != null) {
                Glide.with(this)
                        .load(uri)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_delete)
                        .into(binding.imgInmueble);
            }
        });

        // 🔹 Observadores
        vm.getMensajeToast().observe(getViewLifecycleOwner(),
                msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());

        vm.getAccionNavegarAtras().observe(getViewLifecycleOwner(),
                a -> NavHostFragment.findNavController(this).popBackStack());

        // 🔹 Inicializar el ArrayAdapter y asociarlo al Spinner
        tipoAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTipoInmuebleDetalle.setAdapter(tipoAdapter);

        // Observa los tipos de inmueble de forma reactiva
        vm.getTiposInmueble().observe(getViewLifecycleOwner(), tipos -> {
            listaTipos.clear();
            tipoAdapter.clear();
            if (tipos != null) {
                tipos.forEach(t -> {
                    listaTipos.add(t);
                    tipoAdapter.add(t.getNombre());
                });
            }
            tipoAdapter.notifyDataSetChanged();
        });

        vm.getDireccion().observe(getViewLifecycleOwner(), binding.etDireccionDetalle::setText);
        vm.getMetros().observe(getViewLifecycleOwner(), binding.etMetrosDetalle::setText);
        vm.getPrecio().observe(getViewLifecycleOwner(), binding.etPrecioDetalle::setText);
        vm.getActivo().observe(getViewLifecycleOwner(), binding.swActivoDetalle::setChecked);

        // Observa el tipo seleccionado de forma reactiva
        vm.getTipoSeleccionado().observe(getViewLifecycleOwner(), tipo -> {
            if (tipo != null) {
                int position = listaTipos.indexOf(tipo);
                if (position != -1) {
                    binding.spTipoInmuebleDetalle.setSelection(position);
                }
            }
        });

        // 🖼️ Imagen desde la URL del inmueble (servidor)
        vm.getImagenUrl().observe(getViewLifecycleOwner(), url ->
                Glide.with(requireContext())
                        .load(Objects.requireNonNullElse(url, R.drawable.ic_image_placeholder))
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_delete)
                        .into(binding.imgInmueble)
        );



        // 🎛️ Observador del modo edición
        vm.getModoEdicion().observe(getViewLifecycleOwner(), this::habilitarEdicion);

        // 🎛️ Botón Editar
        binding.btnEditar.setOnClickListener(v -> vm.habilitarEdicion());

        binding.btnGuardar.setOnClickListener(v -> {
            // Actualiza los LiveData antes de guardar
            vm.setDireccion(binding.etDireccionDetalle.getText().toString());
            vm.setMetros(binding.etMetrosDetalle.getText().toString());
            vm.setPrecio(binding.etPrecioDetalle.getText().toString());
            vm.setActivo(binding.swActivoDetalle.isChecked());
            vm.setImagenSeleccionada(imagenSeleccionadaUri);

            // Obtener item del spinner sin crashear por cast
            Object item = binding.spTipoInmuebleDetalle.getSelectedItem();

            TipoInmueble tipo = null;
            if (item != null && item instanceof String) {
                List<TipoInmueble> lista = vm.getTiposInmueble().getValue();
                if (lista != null) {
                    for (TipoInmueble t : lista) {
                        if (t.getNombre().equals(item.toString())) {
                            tipo = t;
                            break;
                        }
                    }
                }
            }

            if (tipo != null) {
                vm.setTipoSeleccionado(tipo);
            }

            // Guardar cambios
            vm.guardarCambios(new ViewModelProvider(requireActivity()).get(InmuebleViewModel.class));
        });

        // 🎛️ Botón Cambiar Imagen
        binding.btnCambiarImg.setOnClickListener(v -> abrirGaleria());

        // 🆕 TextWatcher simple (solo notifica texto)
        binding.etMetrosDetalle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                vm.actualizarMetrosTexto(s.toString());
            }
        });

        // 🧮 Observa metros formateados desde el VM
        vm.getMetrosFormateados().observe(getViewLifecycleOwner(),
                m -> binding.etMetrosDetalle.setText(m));

        vm.getVisibilidadGuardar().observe(getViewLifecycleOwner(), binding.btnGuardar::setVisibility);
        vm.getVisibilidadEditar().observe(getViewLifecycleOwner(), binding.btnEditar::setVisibility);
        vm.getVisibilidadCambiarImg().observe(getViewLifecycleOwner(), binding.btnCambiarImg::setVisibility);
    }


    private void habilitarEdicion(boolean habilitar) {
        binding.etDireccionDetalle.setEnabled(habilitar);
        binding.etPrecioDetalle.setEnabled(habilitar);
        binding.swActivoDetalle.setEnabled(habilitar);
        binding.spTipoInmuebleDetalle.setEnabled(habilitar);
        binding.etMetrosDetalle.setEnabled(false);

        binding.btnGuardar.setVisibility(habilitar ? View.VISIBLE : View.GONE);
        binding.btnCambiarImg.setVisibility(habilitar ? View.VISIBLE : View.GONE);
        binding.btnEditar.setVisibility(habilitar ? View.GONE : View.VISIBLE);
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        seleccionarImagenLauncher.launch(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
