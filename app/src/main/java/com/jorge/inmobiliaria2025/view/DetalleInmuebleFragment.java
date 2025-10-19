package com.jorge.inmobiliaria2025.view;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.model.Inmueble;
import com.jorge.inmobiliaria2025.model.TipoInmueble;
import com.jorge.inmobiliaria2025.viewmodel.InmuebleViewModel;

import java.util.ArrayList;
import java.util.List;

public class DetalleInmuebleFragment extends Fragment {

    private EditText etDireccion, etMetros, etPrecio;
    private Switch swActivo;
    private ImageView imgInmueble;
    private Spinner spTipo;
    private TextView tvPropietario;
    private Button btnEditar, btnGuardar, btnCambiarImg;
    private InmuebleViewModel vm;
    private Uri imagenSeleccionadaUri = null;
    private ArrayAdapter<String> tipoAdapter;
    private final List<TipoInmueble> listaTipos = new ArrayList<>();

    // 🎯 Selección de imagen desde galería
    private final ActivityResultLauncher<Intent> seleccionarImagenLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imagenSeleccionadaUri = result.getData().getData();
                    imgInmueble.setImageURI(imagenSeleccionadaUri);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_detalle_inmueble, container, false);

        // 🧱 Referencias UI
        etDireccion = v.findViewById(R.id.etDireccionDetalle);
        etMetros = v.findViewById(R.id.etMetrosDetalle);
        etPrecio = v.findViewById(R.id.etPrecioDetalle);
        swActivo = v.findViewById(R.id.swActivoDetalle);
        imgInmueble = v.findViewById(R.id.imgInmueble);
        spTipo = v.findViewById(R.id.spTipoInmuebleDetalle);
        tvPropietario = v.findViewById(R.id.tvPropietarioDetalle);
        btnEditar = v.findViewById(R.id.btnEditar);
        btnGuardar = v.findViewById(R.id.btnGuardar);
        btnCambiarImg = v.findViewById(R.id.btnCambiarImg);

        vm = new ViewModelProvider(requireActivity()).get(InmuebleViewModel.class);

        // 🔹 Carga inicial del inmueble recibido
        vm.cargarDesdeBundle(getArguments());
        vm.getInmuebleSeleccionado().observe(getViewLifecycleOwner(), this::mostrarInmueble);

        // 🔹 Configura el spinner de tipos
        tipoAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipo.setAdapter(tipoAdapter);

        // 🔹 Observa los tipos de inmueble
        vm.getTiposInmueble().observe(getViewLifecycleOwner(), tipos -> {
            listaTipos.clear();
            tipoAdapter.clear();

            if (tipos != null && !tipos.isEmpty()) {
                listaTipos.addAll(tipos);
                for (TipoInmueble tipo : tipos) {
                    tipoAdapter.add(tipo.getNombre());
                }
            } else {
                tipoAdapter.add("Sin tipos disponibles");
            }
            tipoAdapter.notifyDataSetChanged();

            Inmueble inmueble = vm.getInmuebleSeleccionado().getValue();
            if (inmueble != null && inmueble.getTipoNombre() != null) {
                int pos = tipoAdapter.getPosition(inmueble.getTipoNombre());
                if (pos >= 0) spTipo.setSelection(pos);
            }
        });

        // 🎛️ Botones
        btnEditar.setOnClickListener(view -> habilitarEdicion(true));
        btnGuardar.setOnClickListener(view -> guardarCambios());
        btnCambiarImg.setOnClickListener(view -> abrirGaleria());

        // 🆕 Mantener el texto “metros cuadrados” visible sin romper el valor numérico
        etMetros.addTextChangedListener(new TextWatcher() {
            private boolean editing = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (editing) return;
                editing = true;
                String text = s.toString().replace(" metros cuadrados", "").trim();
                if (!text.isEmpty()) {
                    etMetros.setText(text + " metros cuadrados");
                    etMetros.setSelection(text.length()); // el cursor queda antes del texto fijo
                }
                editing = false;
            }
        });

        return v;
    }

    private void mostrarInmueble(Inmueble inmueble) {
        if (inmueble == null) return;

        etDireccion.setText(inmueble.getDireccion());
        etMetros.setText(inmueble.getMetrosCuadrados() + " metros cuadrados");
        etPrecio.setText(String.valueOf(inmueble.getPrecio()));
        swActivo.setChecked(inmueble.isActivo());
        tvPropietario.setText(
                inmueble.getNombrePropietario() != null
                        ? inmueble.getNombrePropietario()
                        : "Propietario no disponible"
        );

        if (inmueble.getTipoNombre() != null && tipoAdapter.getCount() > 0) {
            int position = tipoAdapter.getPosition(inmueble.getTipoNombre());
            if (position >= 0) spTipo.setSelection(position);
        }

        Glide.with(requireContext())
                .load(inmueble.getImagenUrl() != null ? inmueble.getImagenUrl() : R.drawable.image_background)
                .centerCrop()
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(imgInmueble);

        habilitarEdicion(false);
    }

    private void habilitarEdicion(boolean habilitar) {
        etDireccion.setEnabled(habilitar);
        etMetros.setEnabled(habilitar);
        etPrecio.setEnabled(habilitar);
        swActivo.setEnabled(habilitar);
        spTipo.setEnabled(habilitar);
        btnGuardar.setVisibility(habilitar ? View.VISIBLE : View.GONE);
        btnCambiarImg.setVisibility(habilitar ? View.VISIBLE : View.GONE);
        btnEditar.setVisibility(habilitar ? View.GONE : View.VISIBLE);
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        seleccionarImagenLauncher.launch(intent);
    }

    private void guardarCambios() {
        Inmueble inmueble = vm.getInmuebleSeleccionado().getValue();
        if (inmueble == null) return;

        try {
            inmueble.setDireccion(etDireccion.getText().toString().trim());

            // 🔹 Limpia el texto antes de convertir a número
            String metrosTexto = etMetros.getText().toString().replace(" metros cuadrados", "").trim();
            inmueble.setMetrosCuadrados(Integer.parseInt(metrosTexto));

            inmueble.setPrecio(Double.parseDouble(etPrecio.getText().toString()));
            inmueble.setActivo(swActivo.isChecked());

            // ✅ Asignar TipoId correctamente antes de enviar
            int pos = spTipo.getSelectedItemPosition();
            if (pos >= 0 && pos < listaTipos.size()) {
                TipoInmueble tipoSeleccionado = listaTipos.get(pos);
                inmueble.setTipoId(tipoSeleccionado.getId());
                inmueble.setTipoNombre(tipoSeleccionado.getNombre());
            } else {
                Toast.makeText(requireContext(), "⚠️ Tipo de inmueble no válido", Toast.LENGTH_SHORT).show();
                return; // corta el guardado si no hay tipo seleccionado
            }


            vm.actualizarInmueble(inmueble, imagenSeleccionadaUri);
            vm.setInmuebleSeleccionado(inmueble);

            habilitarEdicion(false);
            Toast.makeText(requireContext(), "✅ Inmueble actualizado correctamente", Toast.LENGTH_SHORT).show();
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "❌ Campos numéricos inválidos", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "❌ Error al guardar cambios: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
