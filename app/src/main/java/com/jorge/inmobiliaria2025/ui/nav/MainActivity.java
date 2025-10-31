package com.jorge.inmobiliaria2025.ui.nav;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jorge.inmobiliaria2025.R;
import com.jorge.inmobiliaria2025.databinding.ActivityMainBinding;
import com.jorge.inmobiliaria2025.model.Propietario;
import com.jorge.inmobiliaria2025.ui.login.LoginActivity;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_POST_NOTIFICATIONS = 101;

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

        // 🔔 Solicitar permiso de notificaciones (solo Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.w("PERMISOS", "🔔 Solicitando permiso POST_NOTIFICATIONS...");
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_POST_NOTIFICATIONS);
            }
        }

        DrawerLayout drawer = binding.drawerLayout;
        navController = NavHostFragment.findNavController(
                java.util.Objects.requireNonNull(
                        getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment)
                )
        );

        // ✅ Drawer configurado
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

        // 🆕 🔔 Suscripción automática a notificaciones de pagos
        FirebaseMessaging.getInstance().subscribeToTopic("pagos")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "✅ Suscrito al tema 'pagos'");
                    } else {
                        Log.e("FCM", "❌ Error al suscribirse al tema 'pagos'", task.getException());
                    }
                });

        // 🧭 Si la app fue abierta desde una notificación
        manejarNavegacionDesdeNotificacion();

        // ✅ Listener de navegación del Drawer
        binding.navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            Log.d("NAV", "🧭 Item seleccionado: " + getResources().getResourceEntryName(id));

            if (id == R.id.nav_contratos) {
                Log.d("NAV", "🧹 Reiniciando stack y navegando a Contratos...");

                navController.popBackStack(R.id.nav_graph, true);

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

            boolean handled = NavigationUI.onNavDestinationSelected(item, navController)
                    || super.onOptionsItemSelected(item);

            if (handled) {
                binding.drawerLayout.closeDrawer(binding.navView);
            }

            return handled;
        });

        // ✅ Observadores de LiveData
        observarEventos();

        // ✅ Manejo del botón “Atrás”
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                DrawerLayout drawerLayout = binding.drawerLayout;

                if (drawerLayout.isDrawerOpen(binding.navView)) {
                    drawerLayout.closeDrawer(binding.navView);
                    return;
                }

                if (!navController.navigateUp()) {
                    Log.w("NAV_BACK", "⚠️ Sin más destinos en back stack, cerrando actividad");

                    Snackbar.make(binding.getRoot(),
                                    "Finalizando navegación...",
                                    Snackbar.LENGTH_SHORT)
                            .setAnchorView(binding.toolbar)
                            .show();

                    finish();
                }
            }
        });
    }

    // 🆕 ✅ Manejar navegación al abrir desde notificación (con espera al NavController)
    private void manejarNavegacionDesdeNotificacion() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("screen")) {
            String destino = intent.getStringExtra("screen");

            if ("pagos".equals(destino)) {
                Log.d("FCM_NAV", "🧭 Preparando navegación a Pagos desde notificación...");

                // ⏳ Esperamos a que la UI esté 100% lista
                binding.getRoot().post(() -> {
                    try {
                        if (navController != null) {
                            Log.d("FCM_NAV", "➡️ Navegando al fragmento de contratos/pagos");
                            navController.navigate(R.id.nav_contratos);
                        } else {
                            Log.w("FCM_NAV", "⚠️ NavController no listo aún");
                        }
                    } catch (Exception e) {
                        Log.e("FCM_NAV", "💥 Error navegando desde notificación: " + e.getMessage());
                    }
                });
            }
        }
    }


    // 🔔 Resultado del permiso (solo logs informativos)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_POST_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("PERMISOS", "✅ Permiso POST_NOTIFICATIONS concedido");
            } else {
                Log.w("PERMISOS", "⚠️ Usuario denegó el permiso de notificaciones");
            }
        }
    }

    // ✅ método observarEventos()
    private void observarEventos() {
        mainVM.getNavegarLogin().observe(this, navegar -> {
            if (navegar) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
        });

        mainVM.getPropietarioHeader().observe(this, this::actualizarHeaderUsuario);

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

        navVM.getAccionNavegarDetalle().observe(this, args -> {
            Log.i("MAIN", "➡️ Navegando al detalle desde NavViewModel");
            navController.navigate(R.id.action_inmueblesFragment_to_detalleInmuebleFragment, args);
        });
    }

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
