package com.jorge.inmobiliaria2025.ui.login;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jorge.inmobiliaria2025.databinding.FragmentRecuperarPasswordBinding;

public class RecuperarPasswordFragment extends Fragment {

    private FragmentRecuperarPasswordBinding binding;
    private RecuperarPasswordViewModel vm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecuperarPasswordBinding.inflate(inflater, container, false);

        vm = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication())
        ).get(RecuperarPasswordViewModel.class);

        // ==============================
        // 🔹 OBSERVERS
        // ==============================
        vm.getMensaje().observe(getViewLifecycleOwner(), mensaje ->
                Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
        );

        vm.getResetExitoso().observe(getViewLifecycleOwner(), exito -> {
            if (Boolean.TRUE.equals(exito)) {
                requireActivity().onBackPressed();
            }
        });

        // ==============================
        // 🔹 EVENTO: BOTÓN GUARDAR
        // ==============================
        binding.btnGuardar.setOnClickListener(v -> {
            vm.onClickGuardar(
                    obtenerToken(),
                    binding.etNuevaClave.getText().toString(),
                    binding.etConfirmarClave.getText().toString()
            );
        });

        return binding.getRoot();
    }

    private String obtenerToken() {
        Bundle args = getArguments();
        return args != null ? args.getString("token", "") : "";
    }
}
