package com.example.antracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.antracker.data.model.Movimiento;
import com.example.antracker.data.repository.MovimientoRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AgregarMovimientoActivity extends AppCompatActivity {

    private RadioGroup rgTipo;
    private Spinner spinnerCategoria;
    private EditText etMonto, etDescripcion, etFecha;
    private CheckBox cbRecurrente;
    private Button btnGuardar, btnCancelar;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private MovimientoRepository movimientoRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_movimiento);

        movimientoRepository = new MovimientoRepository();

        inicializarVistas();
        configurarSpinners();
        configurarListeners();

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "MX"));
        etFecha.setText(dateFormat.format(calendar.getTime()));

        // Verificar recurrente por defecto al inicio
        verificarRecurrentePorDefecto();
    }

    private void inicializarVistas() {
        rgTipo = findViewById(R.id.rg_tipo);
        spinnerCategoria = findViewById(R.id.spinner_categoria);
        etMonto = findViewById(R.id.et_monto);
        etDescripcion = findViewById(R.id.et_descripcion);
        etFecha = findViewById(R.id.et_fecha);
        cbRecurrente = findViewById(R.id.cb_recurrente);
        btnGuardar = findViewById(R.id.btn_guardar);
        btnCancelar = findViewById(R.id.btn_cancelar);
    }

    private void configurarSpinners() {
        String[] categoriasGasto = {"Fijo", "Variable", "Hormiga"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoriasGasto);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);

        rgTipo.setOnCheckedChangeListener((group, checkedId) -> {
            ArrayAdapter<String> newAdapter;
            if (checkedId == R.id.rb_ingreso) {
                String[] categoriasIngreso = {"Salario", "Freelance", "Inversiones", "Otros"};
                newAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, categoriasIngreso);
            } else {
                String[] categoriasGastoArr = {"Fijo", "Variable", "Hormiga"};
                newAdapter = new ArrayAdapter<>(this,
                        android.R.layout.simple_spinner_item, categoriasGastoArr);
            }
            newAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerCategoria.setAdapter(newAdapter);

            // Verificar si hay que marcar recurrente por defecto
            verificarRecurrentePorDefecto();
        });

        // Listener para detectar cambio en categoría seleccionada
        spinnerCategoria.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                verificarRecurrentePorDefecto();
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }

    private void verificarRecurrentePorDefecto() {
        String categoria = spinnerCategoria.getSelectedItem().toString().toLowerCase();
        int tipoId = rgTipo.getCheckedRadioButtonId();
        boolean esGasto = tipoId == R.id.rb_gasto;
        boolean esIngreso = tipoId == R.id.rb_ingreso;

        // Marcar recurrente por defecto si es gasto fijo o ingreso salario
        boolean debeSerRecurrente = (esGasto && categoria.equals("fijo")) ||
                (esIngreso && categoria.equals("salario"));

        cbRecurrente.setChecked(debeSerRecurrente);
    }

    private void configurarListeners() {
        etFecha.setOnClickListener(v -> mostrarDatePicker());
        btnGuardar.setOnClickListener(v -> guardarMovimiento());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void mostrarDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    etFecha.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void guardarMovimiento() {
        String montoStr = etMonto.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria = spinnerCategoria.getSelectedItem().toString();

        int tipoId = rgTipo.getCheckedRadioButtonId();
        String tipo = (tipoId == R.id.rb_ingreso) ? "ingreso" : "gasto";

        if (montoStr.isEmpty()) {
            etMonto.setError("Ingrese el monto");
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            etMonto.setError("Monto inválido");
            return;
        }

        if (descripcion.isEmpty()) {
            etDescripcion.setError("Ingrese una descripción");
            return;
        }

        Movimiento movimiento = new Movimiento();
        movimiento.setTipo(tipo.toLowerCase());
        movimiento.setCategoria(categoria.toLowerCase());
        movimiento.setMonto(monto);
        movimiento.setDescripcion(descripcion);
        movimiento.setFecha(calendar.getTime());
        movimiento.setEsRecurrente(cbRecurrente.isChecked());

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            movimiento.setUserId(currentUser.getUid());
        }

        // Guardar en Firestore
        movimientoRepository.agregarMovimiento(movimiento,
                documentReference -> {
                    // Asignar el ID generado por Firestore
                    movimiento.setId(documentReference.getId());
                    Toast.makeText(this, "Movimiento guardado exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                },
                e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}