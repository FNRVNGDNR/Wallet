package com.devst.misgastos.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.devst.misgastos.R; // Importa tu R
import com.devst.misgastos.viewmodel.AuthViewModel; // Importa tu ViewModel

public class RegistroActivity extends AppCompatActivity {

    // Vistas
    EditText etEmail, etPassword, etConfirmarPassword;
    Button btnRegistrar;
    TextView tvIrLogin;

    // ViewModel
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        // --- 1. Obtener el ViewModel ---
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // --- 2. Enlazar Vistas ---
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmarPassword = findViewById(R.id.etConfirmarPassword);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        tvIrLogin = findViewById(R.id.tvIrLogin);

        // --- 3. Configurar Observadores ---
        setupObservers();

        // --- 4. Configurar Listeners de Botones ---
        setupButtonListeners();
    }

    private void setupObservers() {

        // Al registrarse, Firebase deja la sesión iniciada: vamos a la pantalla principal
        final Observer<Boolean> sesionObserver = sesionIniciada -> {
            if (sesionIniciada != null && sesionIniciada) {
                Intent intent = new Intent(RegistroActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        };

        // Observador para los mensajes Toast
        final Observer<String> toastObserver = message -> {
            Toast.makeText(RegistroActivity.this, message, Toast.LENGTH_SHORT).show();
        };

        viewModel.getSesionIniciada().observe(this, sesionObserver);
        viewModel.getToastMessage().observe(this, toastObserver);
    }

    private void setupButtonListeners() {

        // REGISTRAR
        btnRegistrar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            String confirmar = etConfirmarPassword.getText().toString();
            viewModel.registrarUsuario(email, password, confirmar);
        });

        // VOLVER AL LOGIN
        tvIrLogin.setOnClickListener(v -> finish());
    }
}
