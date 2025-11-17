package com.jorge.inmobiliaria2025.ui.logout;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.jorge.inmobiliaria2025.InmobiliariaApp;
import com.jorge.inmobiliaria2025.localdata.SessionManager;
import com.jorge.inmobiliaria2025.ui.login.LoginActivity;

public class LogoutFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = new View(requireContext());

        // 🔸 Mostrar diálogo de confirmación
        new AlertDialog.Builder(requireContext())
                .setTitle("Cerrar sesión")
                .setMessage("¿Deseás cerrar tu sesión actual?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // ✅ Cerrar sesión del SessionManager
                    SessionManager.getInstance(requireContext()).logout();

                    // ✅ Cerrar sesión global de la app
                    InmobiliariaApp.getInstance().cerrarSesion();

                    // ✅ Redirigir al login y cerrar la actividad actual
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                })
                .setNegativeButton("Cancelar", (dialog, which) -> {
                    // 🔹 Si cancela, volver al fragment anterior
                    requireActivity().onBackPressed();
                })
                .show();

        return view;
    }
}
