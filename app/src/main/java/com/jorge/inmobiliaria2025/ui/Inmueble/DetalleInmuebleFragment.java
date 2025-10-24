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

    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> vm.procesarSeleccionImagen(result, binding.imgInmueble));

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

        // 🔹 Observadores (se eliminó vm.getInmueble() para evitar repoblado global)
        vm.getImagenSeleccionada().observe(getViewLifecycleOwner(), uri -> imagenSeleccionadaUri = uri);
        vm.getMensajeToast().observe(getViewLifecycleOwner(),
                msg -> Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show());
        vm.getAccionNavegarAtras().observe(getViewLifecycleOwner(),
                a -> NavHostFragment.findNavController(this).popBackStack());
        vm.getTiposInmueble().observe(getViewLifecycleOwner(), tipos -> {
            listaTipos.clear();
            tipoAdapter.clear();
            tipos.forEach(t -> {
                listaTipos.add(t);
                tipoAdapter.add(t.getNombre());
            });
            tipoAdapter.notifyDataSetChanged();
        });

        // 🆕 Observadores de campos individuales (sin ifs)
        vm.getDireccion().observe(getViewLifecycleOwner(), binding.etDireccionDetalle::setText);
        vm.getMetros().observe(getViewLifecycleOwner(), binding.etMetrosDetalle::setText);
        vm.getPrecio().observe(getViewLifecycleOwner(), binding.etPrecioDetalle::setText);
        vm.getActivo().observe(getViewLifecycleOwner(), binding.swActivoDetalle::setChecked);
        vm.getTipoSeleccionado().observe(getViewLifecycleOwner(),
                tipo -> binding.spTipoInmuebleDetalle.setSelection(listaTipos.indexOf(tipo)));

        // 🖼️ Mostrar imagen sin if: Glide maneja nulos automáticamente
        vm.getImagenUrl().observe(getViewLifecycleOwner(), url ->
                Glide.with(requireContext())
                        .load(Objects.requireNonNullElse(url, R.drawable.ic_image_placeholder))
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_delete)
                        .into(binding.imgInmueble)
        );

        // 🔹 Configurar spinner
        tipoAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spTipoInmuebleDetalle.setAdapter(tipoAdapter);

        // 🎛️ Botones
        binding.btnEditar.setOnClickListener(v -> {
            vm.setEnEdicion(true);
            habilitarEdicion(true);
        });

        binding.btnGuardar.setOnClickListener(v -> {
            vm.setEnEdicion(false);
            vm.guardarCambios(
                    binding.etDireccionDetalle.getText().toString(),
                    binding.etMetrosDetalle.getText().toString(),
                    binding.etPrecioDetalle.getText().toString(),
                    binding.swActivoDetalle.isChecked(),
                    binding.spTipoInmuebleDetalle.getSelectedItemPosition(),
                    listaTipos,
                    imagenSeleccionadaUri
            );
        });

        binding.btnCambiarImg.setOnClickListener(v -> abrirGaleria());

        // 🆕 Formateo delegado sin condicionales
        binding.etMetrosDetalle.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                vm.formatearMetros(s.toString());
            }
        });
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
