package com.example.antracker.ui;

import android.graphics.Color;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportesFragment extends Fragment {

    private PieChart pieChartGastos;
    private BarChart barChartEvolucion;
    private TextView tvTotalGastos, tvMayorCategoria;

    private MovimientoRepository movimientoRepository;
    private NumberFormat formatoMoneda;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reportes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        movimientoRepository = new MovimientoRepository();
        formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));

        inicializarVistas(view);
        configurarPieChart();
        configurarBarChart();
        cargarDatosReales();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recargar datos cuando se vuelve a este fragment
        cargarDatosReales();
    }

    private void inicializarVistas(View view) {
        pieChartGastos = view.findViewById(R.id.pie_chart_gastos);
        barChartEvolucion = view.findViewById(R.id.bar_chart_evolucion);
        tvTotalGastos = view.findViewById(R.id.tv_total_gastos);
        tvMayorCategoria = view.findViewById(R.id.tv_mayor_categoria);
    }

    private void configurarPieChart() {
        pieChartGastos.setUsePercentValues(true);
        pieChartGastos.getDescription().setEnabled(false);
        pieChartGastos.setExtraOffsets(5, 10, 5, 5);
        pieChartGastos.setDragDecelerationFrictionCoef(0.95f);
        pieChartGastos.setDrawHoleEnabled(true);
        pieChartGastos.setHoleColor(Color.WHITE);
        pieChartGastos.setTransparentCircleRadius(61f);
        pieChartGastos.setEntryLabelTextSize(12f);
        pieChartGastos.getLegend().setEnabled(true);
    }

    private void configurarBarChart() {
        barChartEvolucion.getDescription().setEnabled(false);
        barChartEvolucion.setDrawGridBackground(false);
        barChartEvolucion.setDrawBarShadow(false);
        barChartEvolucion.setDrawValueAboveBar(true);
        barChartEvolucion.getAxisLeft().setEnabled(true);
        barChartEvolucion.getAxisRight().setEnabled(false);
        barChartEvolucion.getXAxis().setEnabled(true);
        barChartEvolucion.getLegend().setEnabled(true);
    }

    private void cargarDatosReales() {
        // Calcular fechas de inicio y fin del mes actual
        Calendar inicioMes = Calendar.getInstance();
        inicioMes.set(Calendar.DAY_OF_MONTH, 1);
        inicioMes.set(Calendar.HOUR_OF_DAY, 0);
        inicioMes.set(Calendar.MINUTE, 0);
        inicioMes.set(Calendar.SECOND, 0);

        Calendar finMes = Calendar.getInstance();
        finMes.set(Calendar.DAY_OF_MONTH, finMes.getActualMaximum(Calendar.DAY_OF_MONTH));
        finMes.set(Calendar.HOUR_OF_DAY, 23);
        finMes.set(Calendar.MINUTE, 59);
        finMes.set(Calendar.SECOND, 59);

        movimientoRepository.obtenerMovimientosPorFecha(inicioMes.getTime(), finMes.getTime(), task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Map<String, Double> gastosPorCategoria = new HashMap<>();
                double totalGastos = 0;

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Movimiento movimiento = document.toObject(Movimiento.class);
                    movimiento.setId(document.getId());
                    
                    // Filtrar por fecha en memoria
                    if (movimiento.getFecha() != null) {
                        Date fechaMov = movimiento.getFecha();
                        if (!fechaMov.before(inicioMes.getTime()) && !fechaMov.after(finMes.getTime())) {
                            // Solo procesar gastos para el pie chart
                            if (movimiento.getTipo() != null && movimiento.getTipo().equalsIgnoreCase("gasto")) {
                                String categoria = movimiento.getCategoria();
                                double monto = movimiento.getMonto();
                                
                                if (categoria != null) {
                                    gastosPorCategoria.merge(categoria.toLowerCase(), monto, Double::sum);
                                    totalGastos += monto;
                                }
                            }
                        }
                    }
                }

                actualizarPieChart(gastosPorCategoria);
                actualizarResumen(totalGastos, gastosPorCategoria);
                actualizarBarChart(task);
            }
        });
    }

    private void actualizarPieChart(Map<String, Double> gastosPorCategoria) {
        List<PieEntry> entriesPie = new ArrayList<>();
        
        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            entriesPie.add(new PieEntry(entry.getValue().floatValue(), 
                capitalizar(entry.getKey())));
        }

        if (entriesPie.isEmpty()) {
            // Si no hay datos, mostrar un mensaje vacío
            entriesPie.add(new PieEntry(1f, "Sin gastos"));
        }

        PieDataSet dataSetPie = new PieDataSet(entriesPie, "Distribución de Gastos");
        dataSetPie.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSetPie.setValueTextSize(12f);
        dataSetPie.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(dataSetPie);
        pieData.setValueFormatter(new PercentFormatter(pieChartGastos));
        pieChartGastos.setData(pieData);
        pieChartGastos.invalidate();
    }

    private void actualizarResumen(double totalGastos, Map<String, Double> gastosPorCategoria) {
        tvTotalGastos.setText(formatoMoneda.format(totalGastos));
        
        // Encontrar la categoría con mayor gasto
        String mayorCategoria = "Ninguna";
        double mayorMonto = 0;
        
        for (Map.Entry<String, Double> entry : gastosPorCategoria.entrySet()) {
            if (entry.getValue() > mayorMonto) {
                mayorMonto = entry.getValue();
                mayorCategoria = entry.getKey();
            }
        }

        if (totalGastos > 0) {
            double porcentaje = (mayorMonto / totalGastos) * 100;
            tvMayorCategoria.setText(String.format("%s (%.0f%%)", 
                capitalizar(mayorCategoria), porcentaje));
        } else {
            tvMayorCategoria.setText("-");
        }
    }

    private void actualizarBarChart(com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> task) {
        // Agrupar gastos por mes (últimos 6 meses)
        Map<Integer, Double> gastosPorMes = new HashMap<>();
        Calendar ahora = Calendar.getInstance();
        
        for (int i = 0; i < 6; i++) {
            gastosPorMes.put(i, 0.0);
        }

        if (task.isSuccessful()) {
            for (QueryDocumentSnapshot document : task.getResult()) {
                Movimiento movimiento = document.toObject(Movimiento.class);
                
                if (movimiento.getTipo().equalsIgnoreCase("gasto") && movimiento.getFecha() != null) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(movimiento.getFecha());
                    
                    int mesesAtras = (ahora.get(Calendar.YEAR) - cal.get(Calendar.YEAR)) * 12 +
                                    (ahora.get(Calendar.MONTH) - cal.get(Calendar.MONTH));
                    
                    if (mesesAtras >= 0 && mesesAtras < 6) {
                        int index = 5 - mesesAtras; // Invertir para que el mes actual esté a la derecha
                        gastosPorMes.merge(index, movimiento.getMonto(), Double::sum);
                    }
                }
            }
        }

        List<BarEntry> entriesBar = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            entriesBar.add(new BarEntry(i, gastosPorMes.getOrDefault(i, 0.0).floatValue()));
        }

        BarDataSet dataSetBar = new BarDataSet(entriesBar, "Gastos Mensuales");
        dataSetBar.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSetBar.setValueTextSize(10f);

        BarData barData = new BarData(dataSetBar);
        barChartEvolucion.setData(barData);
        barChartEvolucion.invalidate();
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }
}