package com.devst.misgastos.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.devst.misgastos.model.Gasto; // Importa tu modelo
import com.devst.misgastos.repositorio.GastoRepositorio; // Importa tu repositorio

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MainViewModel extends ViewModel {

    private GastoRepositorio repository;

    // LiveData para notificar a la Vista (Activity)
    private MutableLiveData<Gasto> gastoEncontrado;
    private MutableLiveData<List<Gasto>> listaGastos;
    private MutableLiveData<String> toastMessage;

    public MainViewModel() {
        repository = new GastoRepositorio();
        gastoEncontrado = new MutableLiveData<>();
        listaGastos = new MutableLiveData<>();
        toastMessage = new MutableLiveData<>();
    }

    // --- Getters para que la Vista "observe" ---

    public LiveData<Gasto> getGastoEncontrado() {
        return gastoEncontrado;
    }

    public LiveData<List<Gasto>> getListaGastos() {
        return listaGastos;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    // --- Métodos que la Vista llamará (Lógica de UI) ---

    public void cargarGastos() {
        repository.listenGastos(listaGastos, toastMessage);
    }

    public void guardarGasto(String id, String descripcion, String montoTexto,
                             String categoria, String fecha, String metodoPago) {
        if (id.isEmpty() || descripcion.isEmpty() || montoTexto.isEmpty() || fecha.isEmpty()) {
            toastMessage.setValue("Error: ID, Descripción, Monto y Fecha no pueden estar vacíos");
            return;
        }
        long monto;
        try {
            monto = Long.parseLong(montoTexto);
        } catch (NumberFormatException e) {
            toastMessage.setValue("Error: El monto debe ser un número");
            return;
        }
        if (monto <= 0) {
            toastMessage.setValue("Error: El monto debe ser mayor a 0");
            return;
        }
        if (!esFechaValida(fecha)) {
            toastMessage.setValue("Error: La fecha debe tener el formato dd/mm/aaaa");
            return;
        }
        Gasto gasto = new Gasto(id, descripcion, monto, categoria, fecha, metodoPago);
        repository.createOrUpdateGasto(id, gasto, toastMessage);
    }

    public void buscarGasto(String id) {
        if (id.isEmpty()) {
            toastMessage.setValue("Error: Ingrese un ID para buscar");
            return;
        }
        repository.getGasto(id, gastoEncontrado, toastMessage);
    }

    public void eliminarGasto(String id) {
        if (id.isEmpty()) {
            toastMessage.setValue("Error: Ingrese un ID para eliminar");
            return;
        }
        // Limpiamos el LiveData del gasto para que los campos en la UI se limpien
        gastoEncontrado.setValue(null);
        repository.deleteGasto(id, toastMessage);
    }

    // Selecciona un gasto de la lista para mostrarlo en el formulario
    public void seleccionarGasto(Gasto gasto) {
        gastoEncontrado.setValue(gasto);
    }

    // Suma de todos los montos del usuario
    public long calcularTotal(List<Gasto> gastos) {
        long total = 0;
        for (Gasto gasto : gastos) {
            total += gasto.getMonto();
        }
        return total;
    }

    // Comprueba que la fecha exista y tenga el formato dd/MM/yyyy
    private boolean esFechaValida(String fecha) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        formato.setLenient(false);
        try {
            formato.parse(fecha);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    // Al cerrar sesión dejamos de escuchar los gastos del usuario
    public void detenerGastos() {
        repository.stopListening();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopListening();
    }
}
