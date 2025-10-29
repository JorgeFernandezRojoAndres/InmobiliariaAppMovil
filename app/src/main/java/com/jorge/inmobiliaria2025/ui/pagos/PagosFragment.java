package com.jorge.inmobiliaria2025.ui.pagos;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jorge.inmobiliaria2025.R;

/**
 * 💰 PagosFragment
 * Muestra la lista de pagos asociados a un contrato.
 * Totalmente gestionado por PagosViewModel (MVVM limpio).
 */
public class PagosFragment extends Fragment {

    private PagosViewModel vm;
    private RecyclerView recyclerView;
    private TextView tvMensaje;
    private int contratoId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_pagos, container, false);

        recyclerView = v.findViewById(R.id.recyclerPagos);
        tvMensaje = v.findViewById(R.id.tvMensajePagos);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        Log.d("PAGOS", "🧩 PagosFragment creado");

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        vm = new ViewModelProvider(this).get(PagosViewModel.class);

        // ✅ Lee los argumentos enviados desde DetalleContratoFragment
        if (getArguments() != null) {
            contratoId = getArguments().getInt("contratoId", -1);
            Log.d("PAGOS", "📦 contratoId recibido: " + contratoId);
        } else {
            Log.w("PAGOS", "⚠️ No se recibieron argumentos en PagosFragment");
        }

        // 🧠 Observa el estado de la UI
        vm.getUiState().observe(getViewLifecycleOwner(), state -> {
            tvMensaje.setText(state.getMensaje());

            @SuppressWarnings("WrongConstant")
            int visMsg = state.getVisibilidadMensaje();
            tvMensaje.setVisibility(View.VISIBLE);

            @SuppressWarnings("WrongConstant")
            int visLista = state.getVisibilidadLista();
            recyclerView.setVisibility(View.VISIBLE);

            recyclerView.setAdapter(state.getAdapter());
        });

        // 🟢 Inicializa el ViewModel con los argumentos recibidos
        Bundle args = new Bundle();
        args.putInt("contratoId", contratoId);
        vm.inicializar(requireContext(), args);

    }
}
