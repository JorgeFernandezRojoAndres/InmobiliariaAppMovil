package com.jorge.inmobiliaria2025.ui.nav;

import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar; // ✅ IMPORTANTE
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
                java.util.Objects.requireNonNull(
                        getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)
                )
        );

        // ✅ Drawer configurado (sin pagosFragment, que no es destino raíz)
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_ubicacion,
                R.id.nav_perfil,
                R.id.nav_inmuebles,
                R.id.nav_contratos,
                R.id.nav_logout
        ).setOpenableLayout(drawer).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        // 🧠 ViewModels
        mainVM = new ViewModelProvider(this).get(MainViewModel.class);
        navVM = new ViewModelProvider(this).get(NavViewModel.class);

        // ✅ Listener de navegación del Drawer (con fix de stack limpio)
        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Log.d("NAV", "🧭 Item seleccionado: " + getResources().getResourceEntryName(id));

            if (id == R.id.nav_contratos) {
                Log.d("NAV", "🧹 Reiniciando stack y navegando a Contratos...");

                // 🔥 Limpia todo el back stack (detalle previo incluido)
                navController.popBackStack(R.id.nav_graph, true);

                // 🔥 Navega a Contratos solo si no está activo
                NavDestination current = navController.getCurrentDestination();
                if (current == null || current.getId() != R.id.nav_contratos) {
                    NavOptions options = new NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .setPopUpTo(R.id.nav_graph, false)
                            .build();
                    navController.navigate(R.id.nav_contratos, null, options);
                }

                binding.drawerLayout.closeDrawer(binding.navView);
                return true;
            }

            // 🔹 Fallback estándar para otros destinos del Drawer
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController)
                    || super.onOptionsItemSelected(item);

            if (handled) {
                binding.drawerLayout.closeDrawer(binding.navView);
            }

            return handled;
        });

        // ✅ Observadores de LiveData
        observarEventos();

        // ✅ Manejo del botón “Atrás” universal con Snackbar
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DrawerLayout drawerLayout = binding.drawerLayout;

                if (drawerLayout.isDrawerOpen(binding.navView)) {
                    drawerLayout.closeDrawer(binding.navView);
                    return;
                }

                // Si Navigation puede retroceder → hacerlo
                if (!navController.popBackStack()) {
                    // 💬 Stack vacío → mostrar Snackbar y volver a contratos
                    Log.w("NAV_BACK", "⚠️ Stack vacío, redirigiendo a lista de contratos");

                    Snackbar.make(binding.getRoot(),
                                    "Volviendo a la lista de contratos...",
                                    Snackbar.LENGTH_SHORT)
                            .setAnchorView(binding.toolbar) // 🧩 opcional: aparece sobre toolbar
                            .show();

                    // 🔁 Redirigir manualmente
                    try {
                        navController.navigate(R.id.nav_contratos);
                    } catch (Exception e) {
                        Log.e("NAV_BACK", "💥 Error al navegar a nav_contratos: " + e.getMessage(), e);
                        finish(); // fallback seguro
                    }
                }
            }
        });
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

        // 🔹 Navegación global desde ViewModel
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
