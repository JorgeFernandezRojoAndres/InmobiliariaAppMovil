package com.jorge.inmobiliaria2025.view;

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

        // ✅ Cerrar sesión del SessionManager
        new SessionManager(requireContext()).logout();

        // ✅ Cerrar sesión global de la app
        InmobiliariaApp.getInstance().cerrarSesion();

        // ✅ Redirigir al login y cerrar la actividad actual
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        requireActivity().finish();

        // ✅ Retornar una vista vacía para cumplir el ciclo de vida
        return new View(requireContext());
    }
}
