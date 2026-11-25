package com.devst.misgastos.repositorio;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

// REPOSITORIO: único lugar que habla con Firebase Authentication
public class AuthRepositorio {

    private static final String TAG = "AuthRepositorio";
    // Firebase Authentication guarda y protege las contraseñas (NO se guardan en Firestore)
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    // Usuario conectado (null si no hay sesión). Firebase recuerda la sesión al cerrar la app
    public FirebaseUser getUsuarioActual() {
        return auth.getCurrentUser();
    }

    // REGISTRO con correo y contraseña
    public void registrarUsuario(String email, String password,
                                 MutableLiveData<Boolean> sesionIniciada, MutableLiveData<String> toastMessage) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "Usuario registrado: " + authResult.getUser().getUid());
                    toastMessage.postValue("Cuenta creada exitosamente");
                    sesionIniciada.postValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al registrar", e);
                    if (e instanceof FirebaseAuthUserCollisionException) {
                        toastMessage.postValue("Ya existe una cuenta con ese correo");
                    } else {
                        toastMessage.postValue("Error al registrar: " + e.getMessage());
                    }
                });
    }

    // INICIO DE SESIÓN con correo y contraseña
    public void iniciarSesion(String email, String password,
                              MutableLiveData<Boolean> sesionIniciada, MutableLiveData<String> toastMessage) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "Sesión iniciada: " + authResult.getUser().getUid());
                    toastMessage.postValue("Bienvenido");
                    sesionIniciada.postValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al iniciar sesión", e);
                    if (e instanceof FirebaseAuthInvalidUserException
                            || e instanceof FirebaseAuthInvalidCredentialsException) {
                        toastMessage.postValue("Correo o contraseña incorrectos");
                    } else {
                        toastMessage.postValue("Error al iniciar sesión: " + e.getMessage());
                    }
                });
    }

    // CERRAR SESIÓN
    public void cerrarSesion() {
        auth.signOut();
    }
}
