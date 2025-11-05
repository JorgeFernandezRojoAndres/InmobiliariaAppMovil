package com.jorge.inmobiliaria2025.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.jorge.inmobiliaria2025.ui.nav.MainActivity;
import com.jorge.inmobiliaria2025.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel vm;
    private SensorManager sensorManager;
    private SensorEventListener shakeListener;

    private long lastShakeTime = 0;
    private float shakeThreshold = 18f;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setNavigationBarColor(android.graphics.Color.parseColor("#6AD4EF"));
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ✅ Instanciamos el ViewModel usando ViewModelProvider (forma correcta en Java)
        vm = new ViewModelProvider(
                this,
                new ViewModelProvider.AndroidViewModelFactory(getApplication())
        ).get(LoginViewModel.class);

        // ✅ Observadores de LiveData
        vm.getMensaje().observe(this, mensaje ->
                Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
        );

        vm.getNavegarMain().observe(this, navegar -> {
            if (navegar) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        vm.getCamposListos().observe(this, vacio -> {
            if (vacio)
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
        });

        // ✅ Botón de login
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            vm.iniciarSesion(email, password);
        });
        // ✅ Activar detector de movimiento para llamar
        initShakeDetector();
    }private void initShakeDetector() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        shakeListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float acceleration = (float) Math.sqrt(x*x + y*y + z*z);
                long now = System.currentTimeMillis();

                if (acceleration > shakeThreshold && (now - lastShakeTime > 1200)) {
                    lastShakeTime = now;
                    llamarInmobiliaria();
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {}
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
            sensorManager.registerListener(shakeListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    private void llamarInmobiliaria() {
        String phone = "tel:2664261172"; // teléfono real

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(phone));

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
            return;
        }

        Toast.makeText(this, "Llamando a la inmobiliaria...", Toast.LENGTH_SHORT).show();
        startActivity(intent);
    }

}
