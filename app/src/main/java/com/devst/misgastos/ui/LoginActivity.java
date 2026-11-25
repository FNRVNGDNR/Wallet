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

public class LoginActivity extends AppCompatActivity {

    // Vistas
    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvIrRegistro;

    // ViewModel
    private AuthViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // --- 1. Obtener el ViewModel ---
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Si ya hay una sesión iniciada, vamos directo a la pantalla principal
        if (viewModel.haySesionActiva()) {
            irAPantallaPrincipal();
            return;
        }

        // --- 2. Enlazar Vistas ---
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvIrRegistro = findViewById(R.id.tvIrRegistro);

        // --- 3. Configurar Observadores ---
        setupObservers();

        // --- 4. Configurar Listeners de Botones ---
        setupButtonListeners();
    }

    private void setupObservers() {

        // Observador para saber si el login fue exitoso
        final Observer<Boolean> sesionObserver = sesionIniciada -> {
            if (sesionIniciada != null && sesionIniciada) {
                irAPantallaPrincipal();
            }
        };

        // Observador para los mensajes Toast
        final Observer<String> toastObserver = message -> {
            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
        };

        viewModel.getSesionIniciada().observe(this, sesionObserver);
        viewModel.getToastMessage().observe(this, toastObserver);
    }

    private void setupButtonListeners() {

        // INICIAR SESIÓN
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            viewModel.iniciarSesion(email, password);
        });

        // IR A REGISTRO
        tvIrRegistro.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegistroActivity.class)));
    }

    // Abre la pantalla principal y cierra el login para que "atrás" no vuelva aquí
    private void irAPantallaPrincipal() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
