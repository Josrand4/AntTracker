package com.example.antracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.antracker.data.model.Movimiento;
import com.example.antracker.data.repository.MovimientoRepository;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AgregarMovimientoActivity extends AppCompatActivity {

    private RadioGroup rgTipo;
    private Spinner spinnerCategoria;
    private EditText etMonto, etDescripcion, etFecha;
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
    }

    private void inicializarVistas() {
        rgTipo = findViewById(R.id.rg_tipo);
        spinnerCategoria = findViewById(R.id.spinner_categoria);
        etMonto = findViewById(R.id.et_monto);
        etDescripcion = findViewById(R.id.et_descripcion);
        etFecha = findViewById(R.id.et_fecha);
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
        });
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
        String tipo = (tipoId == R.id.rb_ingreso) ? "Ingreso" : "Gasto";

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

        // Guardar en Firestore
        movimientoRepository.agregarMovimiento(movimiento,
                documentReference -> {
                    Toast.makeText(this, "Movimiento guardado exitosamente", Toast.LENGTH_SHORT).show();
                    finish();
                },
                e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}