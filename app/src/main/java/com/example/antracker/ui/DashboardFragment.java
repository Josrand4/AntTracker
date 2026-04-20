package com.example.antracker.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.antracker.R;
import com.example.antracker.data.model.Movimiento;
import com.example.antracker.data.repository.MovimientoRepository;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView tvSaldoFinal, tvIngresosTotales, tvGastosTotales;
    private TextView tvGastosFijos, tvGastosVariables, tvGastosHormiga;
    private TextView tvGastoDiarioPromedio, tvPeriodo;

    private Calendar calendarActual;
    private NumberFormat formatoMoneda;
    private SimpleDateFormat formatoFecha;
    private MovimientoRepository movimientoRepository;

    private double ingresosTotales = 0;
    private double gastosFijos = 0;
    private double gastosVariables = 0;
    private double gastosHormiga = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        movimientoRepository = new MovimientoRepository();

        inicializarVistas(view);
        inicializarFormatos();
        configurarListeners(view);
        cargarDatosMesActual();
    }

    private void inicializarVistas(View view) {
        tvSaldoFinal = view.findViewById(R.id.tv_saldo_final);
        tvIngresosTotales = view.findViewById(R.id.tv_ingresos_totales);
        tvGastosTotales = view.findViewById(R.id.tv_gastos_totales);
        tvGastosFijos = view.findViewById(R.id.tv_gastos_fijos);
        tvGastosVariables = view.findViewById(R.id.tv_gastos_variables);
        tvGastosHormiga = view.findViewById(R.id.tv_gastos_hormiga);
        tvGastoDiarioPromedio = view.findViewById(R.id.tv_gasto_diario_promedio);
        tvPeriodo = view.findViewById(R.id.tv_periodo);
    }

    private void inicializarFormatos() {
        calendarActual = Calendar.getInstance();
        formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        formatoFecha = new SimpleDateFormat("MMMM yyyy", new Locale("es", "MX"));
    }

    private void configurarListeners(View view) {
        view.findViewById(R.id.btn_prev_month).setOnClickListener(v -> cambiarMes(-1));
        view.findViewById(R.id.btn_next_month).setOnClickListener(v -> cambiarMes(1));
    }

    private void cargarDatosMesActual() {
        actualizarTituloPeriodo();
        cargarDatosReales();
    }

    private void cambiarMes(int delta) {
        calendarActual.add(Calendar.MONTH, delta);
        actualizarTituloPeriodo();
        cargarDatosReales();
    }

    private void actualizarTituloPeriodo() {
        String titulo = formatoFecha.format(calendarActual.getTime());
        tvPeriodo.setText(titulo.toUpperCase());
    }

    private void cargarDatosReales() {
        // Resetear totales
        ingresosTotales = 0;
        gastosFijos = 0;
        gastosVariables = 0;
        gastosHormiga = 0;

        // Calcular fechas de inicio y fin del mes
        Calendar inicioMes = (Calendar) calendarActual.clone();
        inicioMes.set(Calendar.DAY_OF_MONTH, 1);
        inicioMes.set(Calendar.HOUR_OF_DAY, 0);
        inicioMes.set(Calendar.MINUTE, 0);
        inicioMes.set(Calendar.SECOND, 0);

        Calendar finMes = (Calendar) calendarActual.clone();
        finMes.set(Calendar.DAY_OF_MONTH, finMes.getActualMaximum(Calendar.DAY_OF_MONTH));
        finMes.set(Calendar.HOUR_OF_DAY, 23);
        finMes.set(Calendar.MINUTE, 59);
        finMes.set(Calendar.SECOND, 59);

        movimientoRepository.obtenerMovimientosPorFecha(inicioMes.getTime(), finMes.getTime(), task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Movimiento movimiento = document.toObject(Movimiento.class);
                    movimiento.setId(document.getId());
                    
                    // Filtrar por fecha en memoria
                    if (movimiento.getFecha() != null) {
                        Date fechaMov = movimiento.getFecha();
                        if (!fechaMov.before(inicioMes.getTime()) && !fechaMov.after(finMes.getTime())) {
                            procesarMovimiento(movimiento);
                        }
                    }
                }
                actualizarUI();
            }
        });
    }

    private void procesarMovimiento(Movimiento movimiento) {
        if (movimiento.getTipo().equalsIgnoreCase("ingreso")) {
            ingresosTotales += movimiento.getMonto();
        } else if (movimiento.getTipo().equalsIgnoreCase("gasto")) {
            switch (movimiento.getCategoria().toLowerCase()) {
                case "fijo":
                    gastosFijos += movimiento.getMonto();
                    break;
                case "variable":
                    gastosVariables += movimiento.getMonto();
                    break;
                case "hormiga":
                    gastosHormiga += movimiento.getMonto();
                    break;
            }
        }
    }

    private void actualizarUI() {
        double gastosTotales = gastosFijos + gastosVariables + gastosHormiga;
        double saldoFinal = ingresosTotales - gastosTotales;

        // gasto diario promedio
        int diasEnMes = calendarActual.getActualMaximum(Calendar.DAY_OF_MONTH);
        double gastoDiarioPromedio = diasEnMes > 0 ? gastosTotales / diasEnMes : 0;

        tvIngresosTotales.setText(formatoMoneda.format(ingresosTotales));
        tvGastosTotales.setText(formatoMoneda.format(gastosTotales));
        tvGastosFijos.setText(formatoMoneda.format(gastosFijos));
        tvGastosVariables.setText(formatoMoneda.format(gastosVariables));
        tvGastosHormiga.setText(formatoMoneda.format(gastosHormiga));
        tvGastoDiarioPromedio.setText(formatoMoneda.format(gastoDiarioPromedio));
        tvSaldoFinal.setText(formatoMoneda.format(saldoFinal));

        // Color del saldo
        if (saldoFinal >= 0) {
            tvSaldoFinal.setTextColor(getResources().getColor(R.color.verde_ingreso, null));
        } else {
            tvSaldoFinal.setTextColor(getResources().getColor(R.color.rojo_gasto, null));
        }
    }
}