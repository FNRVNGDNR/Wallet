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

public class RegistroActivity extends AppCompatActivity {

    //Attr
    private AuthViewModel viewModel;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etConfirmarPassword;
    private Button btnRegistrar;
    private TextView tvIrLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro);

        //El viewModel se obtiene del sistema, así se conserva al girar la pantalla
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        //Métodos para crear
        conexionVistas();
        observarViewModel();
        configurarEventos();
    }

    //Conexión del XML a Java
    private void conexionVistas() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmarPassword = findViewById(R.id.etConfirmarPassword);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        tvIrLogin = findViewById(R.id.tvIrLogin);
    }

    //Observadores (La magia de LiveData)
    private void observarViewModel() {

        //Al registrarse, Firebase deja la sesión iniciada: vamos a la pantalla principal
        viewModel.getSesionIniciada().observe(this, sesionIniciada -> {
            if (sesionIniciada != null && sesionIniciada) {
                //Intent Explicito
                Intent principal = new Intent(RegistroActivity.this, MainActivity.class);
                principal.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(principal);
                finish();
            }
        });

        //Mensajes Toast
        viewModel.getToastMessage().observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(RegistroActivity.this, mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Creamos los Eventos
    private void configurarEventos() {

        //REGISTRAR
        btnRegistrar.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString();
            String confirmar = etConfirmarPassword.getText().toString();
            viewModel.registrarUsuario(email, password, confirmar);
        });

        //VOLVER AL LOGIN
        tvIrLogin.setOnClickListener(v -> finish());
    }
}
