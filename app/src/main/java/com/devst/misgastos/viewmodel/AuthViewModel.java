package com.devst.misgastos.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.devst.misgastos.repositorio.AuthRepositorio; // Importa tu repositorio

public class AuthViewModel extends ViewModel {

    //Attr y estados privados y modificables (solo el ViewModel los cambia)
    private final AuthRepositorio repository = new AuthRepositorio();
    private final MutableLiveData<Boolean> sesionIniciada = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    // --- Getters para que la Vista "observe" ---

    public LiveData<Boolean> getSesionIniciada() {
        return sesionIniciada;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public boolean haySesionActiva() {
        return repository.getUsuarioActual() != null;
    }

    public String getCorreoUsuario() {
        if (!haySesionActiva()) {
            return "";
        }
        return repository.getUsuarioActual().getEmail();
    }

    // --- Métodos (Eventos) que vendrán como solicitud de la Vista ---

    public void iniciarSesion(String email, String password) {
        //Consultas de negocio (validaciones)
        if (email.isEmpty() || password.isEmpty()) {
            toastMessage.setValue("Error: Correo y contraseña no pueden estar vacíos");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toastMessage.setValue("Error: El correo no es válido");
            return;
        }
        repository.iniciarSesion(email, password, sesionIniciada, toastMessage);
    }

    public void registrarUsuario(String email, String password, String confirmarPassword) {
        //Consultas de negocio (validaciones)
        if (email.isEmpty() || password.isEmpty() || confirmarPassword.isEmpty()) {
            toastMessage.setValue("Error: Todos los campos son obligatorios");
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            toastMessage.setValue("Error: El correo no es válido");
            return;
        }
        if (password.length() < 6) {
            toastMessage.setValue("Error: La contraseña debe tener al menos 6 caracteres");
            return;
        }
        if (!password.equals(confirmarPassword)) {
            toastMessage.setValue("Error: Las contraseñas no coinciden");
            return;
        }
        repository.registrarUsuario(email, password, sesionIniciada, toastMessage);
    }

    public void cerrarSesion() {
        repository.cerrarSesion();
    }
}
