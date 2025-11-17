package com.jorge.inmobiliaria2025.ui.login;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.jorge.inmobiliaria2025.databinding.ActivityLoginBinding;
import com.jorge.inmobiliaria2025.databinding.DialogRecuperarPasswordBinding;
import com.jorge.inmobiliaria2025.ui.nav.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel vm;

    private SensorManager sensorManager;
    private SensorEventListener shakeListener;
    private long lastShakeTime = 0L;
    private static final float SHAKE_THRESHOLD = 18f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(android.graphics.Color.parseColor("#6AD4EF"));

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vm = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        ).get(LoginViewModel.class);

        // ===================== OBSERVERS =====================

        vm.getMensaje().observe(this,
                mensaje -> Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        );

        vm.getNavegarMain().observe(this, navegar -> {
            if (Boolean.TRUE.equals(navegar)) {
                irAMain();
            }
        });

        vm.getCamposListos().observe(this, mostrar -> {
            if (Boolean.TRUE.equals(mostrar)) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            }
        });

        // ===================== EVENTOS UI =====================

        // Login
        binding.btnLogin.setOnClickListener(v ->
                vm.iniciarSesion(
                        binding.etEmail.getText().toString().trim(),
                        binding.etPassword.getText().toString().trim()
                )
        );

        // Olvidé mi contraseña → diálogo que dispara vm.enviarRecuperacion()
        binding.tvOlvide.setOnClickListener(v -> mostrarDialogoRecuperacion());

        // Shake-to-call
        initShakeDetector();

        // Deep link desde email (token en la URL)
        handleDeepLinkIfPresent();
    }

    // ===================== NAVEGACIÓN =====================

    private void irAMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    // Muestra diálogo para pedir email y delega en el ViewModel
    private void mostrarDialogoRecuperacion() {
        DialogRecuperarPasswordBinding dialogBinding =
                DialogRecuperarPasswordBinding.inflate(getLayoutInflater());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Restablecer contraseña")
                .setView(dialogBinding.getRoot())
                .setPositiveButton("Enviar", (d, w) ->
                        vm.enviarRecuperacion(
                                dialogBinding.etEmailRecuperacion
                                        .getText()
                                        .toString()
                                        .trim()
                        )
                )
                .setNegativeButton("Cancelar", null)
                .create();

        dialog.show();
    }

    // Deep link: si viene con token, abre el fragmento de restablecer clave
    private void handleDeepLinkIfPresent() {
        Uri data = getIntent().getData();
        if (data == null) {
            return;
        }

        String token = data.getQueryParameter("token");
        if (token == null || token.isEmpty()) {
            return;
        }

        abrirRecuperarPasswordFragment(token);
    }

    private void abrirRecuperarPasswordFragment(String token) {
        RecuperarPasswordFragment fragment = new RecuperarPasswordFragment();
        Bundle args = new Bundle();
        args.putString("token", token);
        fragment.setArguments(args);

        FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
        tx.replace(android.R.id.content, fragment);
        tx.addToBackStack(null);
        tx.commit();
    }

    // ===================== SHAKE TO CALL =====================

    private void initShakeDetector() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager != null
                ? sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
                : null;

        if (sensorManager == null || accelerometer == null) {
            return;
        }

        shakeListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
                long now = System.currentTimeMillis();

                if (acceleration > SHAKE_THRESHOLD && (now - lastShakeTime > 1200)) {
                    lastShakeTime = now;
                    llamarInmobiliaria();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) { }
        };

        sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null && shakeListener != null) {
            sensorManager.unregisterListener(shakeListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null && shakeListener != null) {
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    private void llamarInmobiliaria() {
        String phone = "tel:2664261172";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, 1);
            return;
        }

        Toast.makeText(this, "Llamando a la inmobiliaria...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(phone)));
    }
}
