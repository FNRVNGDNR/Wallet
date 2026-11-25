package com.devst.misgastos.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.devst.misgastos.R; // Importa tu R
import com.devst.misgastos.model.Gasto; // Importa tu modelo
import com.devst.misgastos.viewmodel.AuthViewModel; // Importa tu ViewModel de sesión
import com.devst.misgastos.viewmodel.MainViewModel; // Importa tu ViewModel

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // Vistas
    EditText etGastoId, etDescripcion, etMonto, etFecha;
    Spinner spCategoria, spMetodoPago;
    Button btnCreate, btnRead, btnUpdate, btnDelete, btnLogout;
    TextView tvUsuario, tvTotal;
    ListView lvGastos;

    // ViewModels
    private MainViewModel viewModel;
    private AuthViewModel authViewModel;

    // Lista que se muestra en pantalla
    private List<Gasto> gastos = new ArrayList<>();
    private ArrayAdapter<String> adapterLista;

    // Formato de pesos chilenos: 5990 -> $5.990
    private final NumberFormat formatoPesos = NumberFormat.getIntegerInstance(new Locale("es", "CL"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Asegúrate que tu layout se llame así

        // --- 1. Obtener los ViewModel ---
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Si no hay sesión iniciada no se puede ver la información: volvemos al login
        if (!authViewModel.haySesionActiva()) {
            irAlLogin();
            return;
        }

        // --- 2. Enlazar Vistas ---
        etGastoId = findViewById(R.id.etGastoId);
        etDescripcion = findViewById(R.id.etDescripcion);
        etMonto = findViewById(R.id.etMonto);
        etFecha = findViewById(R.id.etFecha);
        spCategoria = findViewById(R.id.spCategoria);
        spMetodoPago = findViewById(R.id.spMetodoPago);
        btnCreate = findViewById(R.id.btnCreate);
        btnRead = findViewById(R.id.btnRead);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnDelete = findViewById(R.id.btnDelete);
        btnLogout = findViewById(R.id.btnLogout);
        tvUsuario = findViewById(R.id.tvUsuario);
        tvTotal = findViewById(R.id.tvTotal);
        lvGastos = findViewById(R.id.lvGastos);

        tvUsuario.setText("Conectado como: " + authViewModel.getCorreoUsuario());
        etFecha.setText(fechaDeHoy());

        // --- 3. Configurar Spinners y Lista ---
        setupSpinners();
        setupLista();

        // --- 4. Configurar Observadores (La magia de LiveData) ---
        setupObservers();

        // --- 5. Configurar Listeners de Botones ---
        setupButtonListeners();

        // --- 6. Cargar los gastos del usuario desde Firestore ---
        viewModel.cargarGastos();
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> adapterCategorias = ArrayAdapter.createFromResource(
                this, R.array.categorias, android.R.layout.simple_spinner_item);
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adapterCategorias);

        ArrayAdapter<CharSequence> adapterMetodos = ArrayAdapter.createFromResource(
                this, R.array.metodos_pago, android.R.layout.simple_spinner_item);
        adapterMetodos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMetodoPago.setAdapter(adapterMetodos);
    }

    private void setupLista() {
        adapterLista = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        lvGastos.setAdapter(adapterLista);

        // Al tocar un gasto de la lista, se muestran sus datos en el formulario
        lvGastos.setOnItemClickListener((parent, view, position, id) ->
                viewModel.seleccionarGasto(gastos.get(position)));
    }

    private void setupObservers() {

        // Observador para el Gasto encontrado
        final Observer<Gasto> gastoObserver = gasto -> {
            if (gasto != null) {
                // Si el gasto existe, llena los campos
                etGastoId.setText(gasto.getId());
                etDescripcion.setText(gasto.getDescripcion());
                etMonto.setText(String.valueOf(gasto.getMonto()));
                etFecha.setText(gasto.getFecha());
                seleccionarEnSpinner(spCategoria, R.array.categorias, gasto.getCategoria());
                seleccionarEnSpinner(spMetodoPago, R.array.metodos_pago, gasto.getMetodoPago());
            } else {
                // Si es nulo (después de eliminar o buscar y no encontrar), vacía los campos
                etDescripcion.setText("");
                etMonto.setText("");
                etFecha.setText(fechaDeHoy());
                spCategoria.setSelection(0);
                spMetodoPago.setSelection(0);
            }
        };

        // Observador para la lista de gastos (se actualiza sola cuando cambia Firestore)
        final Observer<List<Gasto>> listaObserver = lista -> {
            gastos = lista;
            List<String> textos = new ArrayList<>();
            for (Gasto gasto : lista) {
                textos.add(gasto.getId() + " | " + gasto.getDescripcion() + " | $"
                        + formatoPesos.format(gasto.getMonto()) + "\n"
                        + gasto.getCategoria() + " · " + gasto.getMetodoPago() + " · " + gasto.getFecha());
            }
            adapterLista.clear();
            adapterLista.addAll(textos);
            tvTotal.setText("Total gastado: $" + formatoPesos.format(viewModel.calcularTotal(lista))
                    + " (" + lista.size() + " registros)");
        };

        // Observador para los mensajes Toast
        final Observer<String> toastObserver = message -> {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        };

        // Conectar los observadores a los LiveData del ViewModel
        viewModel.getGastoEncontrado().observe(this, gastoObserver);
        viewModel.getListaGastos().observe(this, listaObserver);
        viewModel.getToastMessage().observe(this, toastObserver);
    }

    private void setupButtonListeners() {

        // CREATE
        btnCreate.setOnClickListener(v -> guardarDatos());

        // READ
        btnRead.setOnClickListener(v -> {
            String id = etGastoId.getText().toString().trim();
            viewModel.buscarGasto(id);
        });

        // UPDATE
        btnUpdate.setOnClickListener(v -> guardarDatos()); // Reutiliza la misma lógica

        // DELETE
        btnDelete.setOnClickListener(v -> {
            String id = etGastoId.getText().toString().trim();
            viewModel.eliminarGasto(id);
            // Los campos se limpiarán automáticamente gracias al observador
        });

        // FECHA: abre un calendario para elegirla
        etFecha.setOnClickListener(v -> mostrarCalendario());

        // CERRAR SESIÓN
        btnLogout.setOnClickListener(v -> {
            viewModel.detenerGastos();
            authViewModel.cerrarSesion();
            Toast.makeText(MainActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            irAlLogin();
        });
    }

    // Método helper para los botones de Guardar/Actualizar
    private void guardarDatos() {
        String id = etGastoId.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String monto = etMonto.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String categoria = spCategoria.getSelectedItem().toString();
        String metodoPago = spMetodoPago.getSelectedItem().toString();

        viewModel.guardarGasto(id, descripcion, monto, categoria, fecha, metodoPago);
    }

    private void mostrarCalendario() {
        Calendar calendario = Calendar.getInstance();
        new DatePickerDialog(this, (view, anio, mes, dia) -> {
            etFecha.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dia, mes + 1, anio));
        }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String fechaDeHoy() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }

    // Marca en el Spinner la opción guardada en Firestore
    private void seleccionarEnSpinner(Spinner spinner, int arrayId, String valor) {
        List<String> opciones = Arrays.asList(getResources().getStringArray(arrayId));
        int posicion = opciones.indexOf(valor);
        spinner.setSelection(Math.max(posicion, 0));
    }

    private void irAlLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
