package com.jorge.inmobiliaria2025;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.jorge.inmobiliaria2025.data.SessionManager;
import com.jorge.inmobiliaria2025.model.Propietario;
import com.jorge.inmobiliaria2025.view.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private SessionManager session;
    private NavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🔹 Inicializar app y verificar sesión persistida
        InmobiliariaApp app = InmobiliariaApp.getInstance();
        if (app == null) {
            app = (InmobiliariaApp) getApplicationContext();
        }

        String email = app.obtenerEmail();
        Log.d("MAIN", "Email recuperado desde prefs: " + email);

        if (email == null || email.isEmpty()) {
            Log.d("MAIN", "No hay sesión activa, redirigiendo a Login...");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);
        session = new SessionManager(this);

        // ✅ Toolbar y Drawer
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);

        // ✅ Configuración del Drawer con NavController
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_ubicacion,     // 🗺️ Fragmento Ubicación
                R.id.nav_perfil,
                R.id.nav_inmuebles,
                R.id.nav_contratos,
                R.id.nav_pagos,
                R.id.nav_logout
        )
                .setOpenableLayout(drawer)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        // 🧠 Mostrar datos del propietario logueado en el header del menú lateral
        Propietario propietario = session.obtenerPropietarioActual();
        actualizarHeaderUsuario(propietario, email);

        Log.d("MAIN", "👤 Header actualizado con los datos del propietario.");
    }

    // ✅ Método reutilizable para actualizar header después de login o cambio de avatar
    public void actualizarHeaderUsuario(Propietario propietario, String fallbackEmail) {
        if (navView == null) return;

        View headerView = navView.getHeaderView(0);
        TextView tvNombre = headerView.findViewById(R.id.header_title);
        TextView tvEmail = headerView.findViewById(R.id.header_subtitle);
        ImageView imgPerfil = headerView.findViewById(R.id.imageViewProfile);

        if (propietario != null) {
            String nombreCompleto = (propietario.getNombre() != null ? propietario.getNombre() : "")
                    + " " + (propietario.getApellido() != null ? propietario.getApellido() : "");
            tvNombre.setText(nombreCompleto.trim().isEmpty() ? "Mi Perfil" : nombreCompleto);
            tvEmail.setText(propietario.getEmail() != null ? propietario.getEmail() : fallbackEmail);

            // 🖼️ Cargar imagen con URL completa
            try {
                if (propietario.getAvatarUrl() != null && !propietario.getAvatarUrl().isEmpty()) {
                    String urlCompleta = "http://192.168.1.33:5027" + propietario.getAvatarUrl();
                    Glide.with(this)
                            .load(urlCompleta)
                            .placeholder(R.drawable.ic_person)
                            .circleCrop()
                            .into(imgPerfil);
                } else {
                    imgPerfil.setImageResource(R.drawable.ic_person);
                }
            } catch (Exception e) {
                Log.e("MAIN", "⚠️ Error cargando avatar: " + e.getMessage());
                imgPerfil.setImageResource(R.drawable.ic_person);
            }
        } else {
            // Fallback: solo email disponible
            tvNombre.setText("Propietario");
            tvEmail.setText(fallbackEmail);
            imgPerfil.setImageResource(R.drawable.ic_person);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}