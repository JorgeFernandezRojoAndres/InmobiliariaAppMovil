package com.jorge.inmobiliaria2025.ui.inquilinos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.jorge.inmobiliaria2025.databinding.FragmentDetalleInquilinoBinding;

public class DetalleInquilinoFragment extends Fragment {

    private FragmentDetalleInquilinoBinding binding;
    private DetalleInquilinoViewModel vm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDetalleInquilinoBinding.inflate(inflater, container, false);
        vm = new ViewModelProvider(this).get(DetalleInquilinoViewModel.class);

        // ViewModel recibirá el ID y decidirá qué hacer
        vm.recibirId(getArguments());

        // Observa texto ya preparado por el ViewModel
        vm.getNombreCompleto().observe(getViewLifecycleOwner(), binding.tvNombreInquilino::setText);
        vm.getDni().observe(getViewLifecycleOwner(), binding.tvDniInquilino::setText);
        vm.getTelefono().observe(getViewLifecycleOwner(), binding.tvTelefonoInquilino::setText);
        vm.getEmail().observe(getViewLifecycleOwner(), binding.tvEmailInquilino::setText);

        // Imagen ya viene lista
        vm.getUrlImagen().observe(getViewLifecycleOwner(), url ->
                Glide.with(requireContext())
                        .load(url)
                        .placeholder(com.jorge.inmobiliaria2025.R.drawable.ic_image_placeholder)
                        .into(binding.imgInmueble)
        );


        return binding.getRoot();
    }
}
