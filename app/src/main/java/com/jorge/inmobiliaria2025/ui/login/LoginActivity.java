package com.jorge.inmobiliaria2025.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.jorge.inmobiliaria2025.MainActivity;
import com.jorge.inmobiliaria2025.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel vm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }
}
