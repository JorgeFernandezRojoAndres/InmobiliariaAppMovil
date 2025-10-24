package com.jorge.inmobiliaria2025.ui.nav;
import android.view.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.ActivityMainBinding;
import com.jorge.inmobiliaria2025.model.Propietario;
import com.jorge.inmobiliaria2025.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private MainViewModel mainVM;
    private NavViewModel navVM;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 🧩 ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        DrawerLayout drawer = binding.drawerLayout;
        navController = NavHostFragment.findNavController(
                getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)
        );

        // ✅ Drawer configurado
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_ubicacion,
                R.id.nav_perfil,
                R.id.nav_inmuebles,
                R.id.nav_contratos,
                R.id.nav_pagos,
                R.id.nav_logout
        ).setOpenableLayout(drawer).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // 🧠 ViewModels
        mainVM = new ViewModelProvider(this).get(MainViewModel.class);
        navVM = new ViewModelProvider(this).get(NavViewModel.class);

        // ✅ Observadores de LiveData (sin condicionales ni lógica)
        observarEventos();
    }

    // ✅ método observarEventos()
    private void observarEventos() {
        // 🔹 Redirección a login
        mainVM.getNavegarLogin().observe(this, navegar -> {
            if (navegar) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

        // 🔹 Actualización del header
        mainVM.getPropietarioHeader().observe(this, this::actualizarHeaderUsuario);

        // 🔹 Observa también el LiveData del avatar
        mainVM.getAvatarUrl().observe(this, avatar -> {
            View headerView = binding.navView.getHeaderView(0);
            com.jorge.inmobiliaria2025.databinding.NavHeaderBinding headerBinding =
                    com.jorge.inmobiliaria2025.databinding.NavHeaderBinding.bind(headerView);

            Glide.with(this)
                    .load(avatar)
                    .placeholder(R.drawable.ic_person)
                    .error(R.drawable.ic_person)
                    .circleCrop()
                    .into(headerBinding.imageViewProfile);
        });

        // 🔹 Navegación global (ya validada desde NavViewModel)
        navVM.getAccionNavegarDetalle().observe(this, args -> {
            Log.i("MAIN", "➡️ Navegando al detalle desde NavViewModel");
            navController.navigate(R.id.action_inmueblesFragment_to_detalleInmuebleFragment, args);
        });
    }


    // 🔹 Actualiza el header del menú lateral

    private void actualizarHeaderUsuario(Propietario propietario) {
        View headerView = binding.navView.getHeaderView(0);
        com.jorge.inmobiliaria2025.databinding.NavHeaderBinding headerBinding =
                com.jorge.inmobiliaria2025.databinding.NavHeaderBinding.bind(headerView);

        headerBinding.headerTitle.setText(propietario.getNombre() + " " + propietario.getApellido());
        headerBinding.headerSubtitle.setText(propietario.getEmail());
    }


    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
