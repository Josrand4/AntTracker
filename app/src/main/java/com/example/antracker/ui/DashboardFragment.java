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
import com.example.antracker.data.model.ResumenFinanciero;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView tvSaldoFinal, tvIngresosTotales, tvGastosTotales;
    private TextView tvGastosFijos, tvGastosVariables, tvGastosHormiga;
    private TextView tvGastoDiarioPromedio, tvPeriodo;

    private Calendar calendarActual;
    private NumberFormat formatoMoneda;
    private SimpleDateFormat formatoFecha;

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

        inicializarVistas(view);
        inicializarFormatos();
        configurarListeners();
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

        view.findViewById(R.id.btn_prev_month).setOnClickListener(v -> cambiarMes(-1));
        view.findViewById(R.id.btn_next_month).setOnClickListener(v -> cambiarMes(1));
    }

    private void inicializarFormatos() {
        calendarActual = Calendar.getInstance();
        formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        formatoFecha = new SimpleDateFormat("MMMM yyyy", new Locale("es", "MX"));
    }

    private void configurarListeners() {
        // Aquí configuraremos observers cuando tengamos ViewModel
    }

    private void cargarDatosMesActual() {
        actualizarTituloPeriodo();
        // Por ahora usamos datos de ejemplo
        cargarDatosEjemplo();
    }

    private void cambiarMes(int delta) {
        calendarActual.add(Calendar.MONTH, delta);
        actualizarTituloPeriodo();
        // Aquí cargaremos datos reales del mes seleccionado
        cargarDatosEjemplo();
    }

    private void actualizarTituloPeriodo() {
        String titulo = formatoFecha.format(calendarActual.getTime());
        tvPeriodo.setText(titulo.toUpperCase());
    }

    private void cargarDatosEjemplo() {
        // Datos de ejemplo para visualizar el layout
        ResumenFinanciero resumen = new ResumenFinanciero();
        resumen.setIngresosTotales(25000.00);
        resumen.setGastosFijos(8000.00);
        resumen.setGastosVariables(5000.00);
        resumen.setGastosHormiga(1200.00);
        resumen.setDiasEnPeriodo(30);

        actualizarUI(resumen);
    }

    private void actualizarUI(ResumenFinanciero resumen) {
        tvSaldoFinal.setText(formatoMoneda.format(resumen.getSaldoFinal()));
        tvIngresosTotales.setText(formatoMoneda.format(resumen.getIngresosTotales()));
        tvGastosTotales.setText(formatoMoneda.format(resumen.getGastosTotales()));
        tvGastosFijos.setText(formatoMoneda.format(resumen.getGastosFijos()));
        tvGastosVariables.setText(formatoMoneda.format(resumen.getGastosVariables()));
        tvGastosHormiga.setText(formatoMoneda.format(resumen.getGastosHormiga()));
        tvGastoDiarioPromedio.setText(formatoMoneda.format(resumen.getGastoDiarioPromedio()));

        // Cambiar color del saldo según sea positivo o negativo
        if (resumen.getSaldoFinal() >= 0) {
            tvSaldoFinal.setTextColor(getResources().getColor(R.color.verde_ingreso, null));
        } else {
            tvSaldoFinal.setTextColor(getResources().getColor(R.color.rojo_gasto, null));
        }
    }
}
