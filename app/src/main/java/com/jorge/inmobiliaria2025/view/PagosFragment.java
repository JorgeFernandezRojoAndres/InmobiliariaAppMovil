package com.jorge.inmobiliaria2025.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.viewmodel.PagosViewModel;

public class PagosFragment extends Fragment {

    private PagosViewModel vm;
    private RecyclerView recyclerView;
    private TextView tvMensaje;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_pagos, container, false);
        recyclerView = v.findViewById(R.id.recyclerPagos);
        tvMensaje = v.findViewById(R.id.tvMensajePagos);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        return v;
    }


    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(this).get(PagosViewModel.class);

        vm.getUiState().observe(getViewLifecycleOwner(), state -> {
            tvMensaje.setText(state.getMensaje());
            tvMensaje.setVisibility(state.getVisibilidadMensaje());
            recyclerView.setVisibility(state.getVisibilidadLista());
            recyclerView.setAdapter(state.getAdapter());
        });


        // 🟢 ViewModel maneja todo (argumentos, carga, validaciones)
        vm.inicializar(requireContext(), getArguments());
    }
}
