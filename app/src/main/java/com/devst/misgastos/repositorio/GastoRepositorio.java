package com.devst.misgastos.repositorio;

import android.util.Log;
import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.devst.misgastos.model.Gasto; // Importa tu modelo

import java.util.ArrayList;
import java.util.List;

public class GastoRepositorio {

    private static final String TAG = "GastoRepositorio";
    private static final String USERS_COLLECTION = "usuarios";
    private static final String COLLECTION_NAME = "gastos";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private ListenerRegistration listenerGastos;

    // Cada usuario tiene sus propios gastos: usuarios/{uid}/gastos/{id}
    // Así el UID de Firebase Authentication relaciona al usuario con sus datos
    private CollectionReference getColeccionGastos() {
        String uid = auth.getCurrentUser().getUid();
        return db.collection(USERS_COLLECTION).document(uid).collection(COLLECTION_NAME);
    }

    // CREATE / UPDATE (Firestore usa "set" para crear o sobreescribir)
    public void createOrUpdateGasto(String id, Gasto gasto, MutableLiveData<String> toastMessage) {
        getColeccionGastos().document(id)
                .set(gasto)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Gasto guardado con ID: " + id);
                    toastMessage.postValue("Gasto guardado exitosamente");
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al guardar gasto", e);
                    toastMessage.postValue("Error al guardar: " + e.getMessage());
                });
    }

    // READ (un gasto por su ID)
    public void getGasto(String id, MutableLiveData<Gasto> gastoResult, MutableLiveData<String> toastMessage) {
        getColeccionGastos().document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Convierte el documento de Firebase a nuestro objeto Gasto
                        Gasto gasto = documentSnapshot.toObject(Gasto.class);
                        gastoResult.postValue(gasto); // ¡Aquí enviamos el resultado!
                        toastMessage.postValue("Gasto encontrado");
                    } else {
                        gastoResult.postValue(null); // No encontrado
                        toastMessage.postValue("Gasto no encontrado");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al buscar gasto", e);
                    toastMessage.postValue("Error al buscar: " + e.getMessage());
                });
    }

    // READ (todos los gastos del usuario, se actualiza en tiempo real)
    public void listenGastos(MutableLiveData<List<Gasto>> listaResult, MutableLiveData<String> toastMessage) {
        stopListening();
        listenerGastos = getColeccionGastos()
                .orderBy("id")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.w(TAG, "Error al cargar gastos", e);
                        toastMessage.postValue("Error al cargar: " + e.getMessage());
                        return;
                    }
                    List<Gasto> lista = new ArrayList<>();
                    for (QueryDocumentSnapshot document : snapshots) {
                        lista.add(document.toObject(Gasto.class));
                    }
                    listaResult.postValue(lista);
                });
    }

    // Deja de escuchar los cambios (al cerrar sesión o cerrar la pantalla)
    public void stopListening() {
        if (listenerGastos != null) {
            listenerGastos.remove();
            listenerGastos = null;
        }
    }

    // DELETE
    public void deleteGasto(String id, MutableLiveData<String> toastMessage) {
        getColeccionGastos().document(id)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Gasto eliminado: " + id);
                    toastMessage.postValue("Gasto eliminado");
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error al eliminar", e);
                    toastMessage.postValue("Error al eliminar: " + e.getMessage());
                });
    }
}
