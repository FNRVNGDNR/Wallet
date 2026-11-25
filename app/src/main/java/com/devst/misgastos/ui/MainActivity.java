package com.devst.misgastos.ui;

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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

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

    //Attr
    private MainViewModel viewModel;
    private AuthViewModel authViewModel;
    private EditText etGastoId, etDescripcion, etMonto, etFecha;
    private Spinner spCategoria, spMetodoPago;
    private Button btnCreate, btnRead, btnUpdate, btnDelete, btnLogout;
    private TextView tvUsuario, tvTotal;
    private ListView lvGastos;

    //Adaptador de la lista (usa el toString() de Gasto)
    private ArrayAdapter<Gasto> adapter;

    //Formato de pesos chilenos: 5990 -> 5.990
    private final NumberFormat formatoPesos = NumberFormat.getIntegerInstance(new Locale("es", "CL"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //El viewModel se obtiene del sistema, así se conserva al girar la pantalla
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        //Si no hay sesión iniciada no se puede ver la información: volvemos al login
        if (!authViewModel.haySesionActiva()) {
            irAlLogin();
            return;
        }

        //Métodos para crear
        conexionVistas();
        configurarSpinners();
        configurarLista();
        observarViewModel();
        configurarEventos();

        //Cargar los gastos del usuario desde Firestore
        viewModel.cargarGastos();
    }

    //Conexión del XML a Java
    private void conexionVistas() {
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

        tvUsuario.setText(getString(R.string.usuario_conectado, authViewModel.getCorreoUsuario()));
        etFecha.setText(fechaDeHoy());
    }

    //Opciones de los Spinners (vienen de strings.xml)
    private void configurarSpinners() {
        ArrayAdapter<CharSequence> adapterCategorias = ArrayAdapter.createFromResource(
                this, R.array.categorias, android.R.layout.simple_spinner_item);
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategoria.setAdapter(adapterCategorias);

        ArrayAdapter<CharSequence> adapterMetodos = ArrayAdapter.createFromResource(
                this, R.array.metodos_pago, android.R.layout.simple_spinner_item);
        adapterMetodos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMetodoPago.setAdapter(adapterMetodos);
    }

    //Lista de gastos
    private void configurarLista() {
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        lvGastos.setAdapter(adapter);
    }

    //Observadores (La magia de LiveData)
    private void observarViewModel() {

        //Gasto encontrado o seleccionado
        viewModel.getGastoEncontrado().observe(this, gasto -> {
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
        });

        //Lista de gastos (se actualiza sola cuando cambia Firestore)
        viewModel.getListaGastos().observe(this, gastos -> {
            adapter.clear();
            adapter.addAll(gastos);
            String total = formatoPesos.format(viewModel.calcularTotal(gastos));
            tvTotal.setText(getString(R.string.total_gastado, total, gastos.size()));
        });

        //Mensajes Toast
        viewModel.getToastMessage().observe(this, mensaje -> {
            if (mensaje != null) {
                Toast.makeText(MainActivity.this, mensaje, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //Creamos los Eventos
    private void configurarEventos() {

        //CREATE
        btnCreate.setOnClickListener(v -> guardarDatos());

        //READ
        btnRead.setOnClickListener(v -> {
            String id = etGastoId.getText().toString().trim();
            viewModel.buscarGasto(id);
        });

        //UPDATE
        btnUpdate.setOnClickListener(v -> guardarDatos()); // Reutiliza la misma lógica

        //DELETE
        btnDelete.setOnClickListener(v -> {
            String id = etGastoId.getText().toString().trim();
            confirmarEliminacion(id);
        });

        //Tocar un gasto de la lista: se muestra en el formulario para editarlo
        lvGastos.setOnItemClickListener((parent, view, position, id) ->
                viewModel.seleccionarGasto(adapter.getItem(position)));

        //Mantener presionado un gasto de la lista: pide confirmación y se elimina
        lvGastos.setOnItemLongClickListener((parent, view, position, id) -> {
            confirmarEliminacion(adapter.getItem(position).getId());
            return true;
        });

        //FECHA: abre un calendario para elegirla
        etFecha.setOnClickListener(v -> mostrarCalendario());

        //CERRAR SESIÓN
        btnLogout.setOnClickListener(v -> {
            viewModel.detenerGastos();
            authViewModel.cerrarSesion();
            Toast.makeText(MainActivity.this, R.string.sesion_cerrada, Toast.LENGTH_SHORT).show();
            irAlLogin();
        });
    }

    //Pide confirmación antes de eliminar (la acción no se puede deshacer)
    private void confirmarEliminacion(String id) {
        if (id.isEmpty()) {
            viewModel.eliminarGasto(id); // El ViewModel muestra el error de validación
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.titulo_eliminar)
                .setMessage(getString(R.string.mensaje_eliminar, id))
                .setPositiveButton(R.string.btn_confirmar_eliminar, (dialog, which) -> viewModel.eliminarGasto(id))
                .setNegativeButton(R.string.btn_cancelar, null)
                .show();
    }

    //Método helper para los botones de Guardar/Actualizar
    private void guardarDatos() {
        String id = etGastoId.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String monto = etMonto.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String categoria = spCategoria.getSelectedItem().toString();
        String metodoPago = spMetodoPago.getSelectedItem().toString();

        viewModel.guardarGasto(id, descripcion, monto, categoria, fecha, metodoPago);
    }

    //Calendario para elegir la fecha
    private void mostrarCalendario() {
        Calendar calendario = Calendar.getInstance();
        new DatePickerDialog(this, (view, anio, mes, dia) ->
                etFecha.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d", dia, mes + 1, anio)),
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)).show();
    }

    private String fechaDeHoy() {
        return new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
    }

    //Marca en el Spinner la opción guardada en Firestore
    private void seleccionarEnSpinner(Spinner spinner, int arrayId, String valor) {
        List<String> opciones = Arrays.asList(getResources().getStringArray(arrayId));
        int posicion = opciones.indexOf(valor);
        spinner.setSelection(Math.max(posicion, 0));
    }

    //Volver al login
    private void irAlLogin() {
        //Intent Explicito
        Intent login = new Intent(MainActivity.this, LoginActivity.class);
        login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(login);
        finish();
    }
}
