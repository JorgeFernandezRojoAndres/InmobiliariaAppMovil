package com.jorge.inmobiliaria2025.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.viewmodel.SettingsViewModel;

public class SettingsFragment extends Fragment {

    private SettingsViewModel vm;
    private EditText etBaseUrl;
    private Button btnGuardar, btnRestablecer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        vm = new ViewModelProvider(this).get(SettingsViewModel.class);

        etBaseUrl = view.findViewById(R.id.etBaseUrl);
        btnGuardar = view.findViewById(R.id.btnGuardarBaseUrl);
        btnRestablecer = view.findViewById(R.id.btnRestablecerBaseUrl);

        // 🔹 Observa los LiveData
        vm.getBaseUrlActual().observe(getViewLifecycleOwner(), url -> etBaseUrl.setText(url));
        vm.getMensaje().observe(getViewLifecycleOwner(), msg -> Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show());

        // 🔹 Eventos de UI
        btnGuardar.setOnClickListener(v -> vm.guardarUrl(etBaseUrl.getText().toString().trim()));
        btnRestablecer.setOnClickListener(v -> vm.restablecerUrlPorDefecto());

        return view;
    }
}
