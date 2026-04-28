package com.example.antracker;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.antracker.data.model.Movimiento;
import com.example.antracker.data.repository.MovimientoRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditarMovimientoActivity extends AppCompatActivity {

    public static final String EXTRA_MOVIMIENTO_ID          = "movimiento_id";
    public static final String EXTRA_MOVIMIENTO_TIPO        = "movimiento_tipo";
    public static final String EXTRA_MOVIMIENTO_CATEGORIA   = "movimiento_categoria";
    public static final String EXTRA_MOVIMIENTO_MONTO       = "movimiento_monto";
    public static final String EXTRA_MOVIMIENTO_DESCRIPCION = "movimiento_descripcion";
    public static final String EXTRA_MOVIMIENTO_FECHA_MS    = "movimiento_fecha_ms";

    private RadioGroup  rgTipo;
    private RadioButton rbIngreso, rbGasto;
    private Spinner     spinnerCategoria;
    private EditText    etMonto, etDescripcion, etFecha;
    private Button      btnGuardar, btnCancelar;

    private Calendar         calendar;
    private SimpleDateFormat dateFormat;
    private MovimientoRepository movimientoRepository;

    private String movimientoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agregar_movimiento); // Reutiliza el mismo layout

        // Cambiar título
        TextView tvTitle = findViewById(R.id.tvTitle);
        if (tvTitle != null) tvTitle.setText("Editar Movimiento");

        movimientoRepository = new MovimientoRepository();
        calendar   = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "MX"));

        inicializarVistas();
        configurarListeners();
        cargarDatosDesdeIntent();
    }

    private void inicializarVistas() {
        rgTipo           = findViewById(R.id.rg_tipo);
        rbIngreso        = findViewById(R.id.rb_ingreso);
        rbGasto          = findViewById(R.id.rb_gasto);
        spinnerCategoria = findViewById(R.id.spinner_categoria);
        etMonto          = findViewById(R.id.et_monto);
        etDescripcion    = findViewById(R.id.et_descripcion);
        etFecha          = findViewById(R.id.et_fecha);
        btnGuardar       = findViewById(R.id.btn_guardar);
        btnCancelar      = findViewById(R.id.btn_cancelar);
    }

    private void cargarDatosDesdeIntent() {
        movimientoId = getIntent().getStringExtra(EXTRA_MOVIMIENTO_ID);

        String tipo        = getIntent().getStringExtra(EXTRA_MOVIMIENTO_TIPO);
        String categoria   = getIntent().getStringExtra(EXTRA_MOVIMIENTO_CATEGORIA);
        double monto       = getIntent().getDoubleExtra(EXTRA_MOVIMIENTO_MONTO, 0);
        String descripcion = getIntent().getStringExtra(EXTRA_MOVIMIENTO_DESCRIPCION);
        long   fechaMs     = getIntent().getLongExtra(EXTRA_MOVIMIENTO_FECHA_MS, System.currentTimeMillis());

        // Seleccionar tipo radio
        boolean esIngreso = "ingreso".equalsIgnoreCase(tipo);
        if (esIngreso) {
            rbIngreso.setChecked(true);
        } else {
            rbGasto.setChecked(true);
        }

        // Poblar spinner antes de seleccionar
        actualizarSpinnerCategoria(esIngreso);
        seleccionarCategoria(categoria);

        // Actualizar spinner al cambiar tipo
        rgTipo.setOnCheckedChangeListener((group, checkedId) ->
                actualizarSpinnerCategoria(checkedId == R.id.rb_ingreso));

        etMonto.setText(String.valueOf(monto));
        etDescripcion.setText(descripcion);

        calendar.setTimeInMillis(fechaMs);
        etFecha.setText(dateFormat.format(calendar.getTime()));
    }

    private void actualizarSpinnerCategoria(boolean esIngreso) {
        String[] opciones = esIngreso
                ? new String[]{"Salario", "Freelance", "Inversiones", "Otros"}
                : new String[]{"Fijo", "Variable", "Hormiga"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, opciones);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);
    }

    private void seleccionarCategoria(String categoria) {
        if (categoria == null) return;
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerCategoria.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).equalsIgnoreCase(categoria)) {
                spinnerCategoria.setSelection(i);
                return;
            }
        }
    }

    private void configurarListeners() {
        etFecha.setOnClickListener(v -> mostrarDatePicker());
        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnCancelar.setOnClickListener(v -> finish());
    }

    private void mostrarDatePicker() {
        new DatePickerDialog(this,
                (view, year, month, day) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    etFecha.setText(dateFormat.format(calendar.getTime()));
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void guardarCambios() {
        String montoStr    = etMonto.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String categoria   = spinnerCategoria.getSelectedItem().toString();
        String tipo        = (rgTipo.getCheckedRadioButtonId() == R.id.rb_ingreso) ? "ingreso" : "gasto";

        if (montoStr.isEmpty()) {
            etMonto.setError("Ingrese el monto");
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
            if (monto <= 0) {
                etMonto.setError("El monto debe ser mayor a 0");
                return;
            }
        } catch (NumberFormatException e) {
            etMonto.setError("Monto inválido");
            return;
        }

        if (descripcion.isEmpty()) {
            etDescripcion.setError("Ingrese una descripción");
            return;
        }

        if (movimientoId == null || movimientoId.isEmpty()) {
            Toast.makeText(this, "Error: ID de movimiento no encontrado", Toast.LENGTH_SHORT).show();
            return;
        }

        Movimiento movimiento = new Movimiento();
        movimiento.setId(movimientoId);
        movimiento.setTipo(tipo);
        movimiento.setCategoria(categoria.toLowerCase());
        movimiento.setMonto(monto);
        movimiento.setDescripcion(descripcion);
        movimiento.setFecha(calendar.getTime());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) movimiento.setUserId(user.getUid());

        btnGuardar.setEnabled(false); // Prevenir doble-tap

        movimientoRepository.actualizarMovimiento(movimientoId, movimiento,
                aVoid -> {
                    Toast.makeText(this, "Movimiento actualizado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                },
                e -> {
                    btnGuardar.setEnabled(true);
                    Toast.makeText(this,
                            "Error al actualizar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
