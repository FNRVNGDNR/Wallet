package com.devst.misgastos.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.devst.misgastos.R; // Importa tu R
import com.devst.misgastos.viewmodel.AuthViewModel; // Importa tu ViewModel

public class LoginActivity extends AppCompatActivity {

    //Attr
    private AuthViewModel viewModel;
    private EditText etEmail;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvIrRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        //El viewModel se obtiene del sistema, así se conserva al girar la pantalla
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        //Si ya hay una sesión iniciada, vamos directo a la pantalla principal
        if (viewModel.haySesionActiva()) {
            irAPantallaPrincipal();
            return;
        }

        //Métodos para crear
        conexionVistas();
        observarViewModel();
        configurarEventos();
    }

    //Conexión del XML a Java
    private void conexionVistas() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvIrRegistro = findViewById(R.id.tvIrRegistro);
    }

    //Observadores (La magia de LiveData)
    private void observarViewModel() {

        //Si el login fue exitoso, pasamos a la pantalla principal
        viewModel.getSesionIniciada().observe(this, sesionIniciada -> {
            if (sesionIniciada != null && sesionIniciada) {
                irAPantallaPrincipal();
            }
        });

        //Mensajes Toast
        viewModel.getToastMessage().observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(LoginActivity.this, mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Creamos los Eventos
    private void configurarEventos() {

        //INICIAR SESIÓN
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            viewModel.iniciarSesion(email, password);
        });

        //IR A REGISTRO
        tvIrRegistro.setOnClickListener(v -> {
            //Intent Explicito
            Intent registro = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(registro);
        });
    }

    //Abre la pantalla principal y cierra el login para que "atrás" no vuelva aquí
    private void irAPantallaPrincipal() {
        //Intent Explicito
        Intent principal = new Intent(LoginActivity.this, MainActivity.class);
        principal.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(principal);
        finish();
    }
}
