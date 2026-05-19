package com.example.antracker.ui;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.example.antracker.R;
import com.example.antracker.data.model.Movimiento;
import com.example.antracker.data.repository.MovimientoRepository;
import com.example.antracker.ui.dialog.MonthYearPickerDialog;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportesFragment extends Fragment {

    private PieChart pieChartGastos;
    private BarChart barChartEvolucion;
    private TextView tvTotalGastos, tvMayorCategoria, tvPeriodo;
    private View btnDescargarPdf;

    private MovimientoRepository movimientoRepository;
    private NumberFormat formatoMoneda;
    private SimpleDateFormat formatoFechaMes;
    private SimpleDateFormat formatoFechaCorta;

    private Calendar calendarActual;
    private List<Movimiento> movimientosMesActual;

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
        formatoFechaMes = new SimpleDateFormat("MMMM yyyy", new Locale("es", "MX"));
        formatoFechaCorta = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "MX"));
        calendarActual = Calendar.getInstance();
        movimientosMesActual = new ArrayList<>();

        inicializarVistas(view);
        configurarPieChart();
        configurarBarChart();
        configurarListeners();
        cargarDatosReales();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarDatosReales();
    }

    private void inicializarVistas(View view) {
        pieChartGastos = view.findViewById(R.id.pie_chart_gastos);
        barChartEvolucion = view.findViewById(R.id.bar_chart_evolucion);
        tvTotalGastos = view.findViewById(R.id.tv_total_gastos);
        tvMayorCategoria = view.findViewById(R.id.tv_mayor_categoria);
        tvPeriodo = view.findViewById(R.id.tv_periodo);
        btnDescargarPdf = view.findViewById(R.id.btn_descargar_pdf);
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

    private void configurarListeners() {
        // Selector de periodo
        tvPeriodo.setOnClickListener(v -> mostrarSelectorMesAnio());
        
        // Botón descargar PDF
        btnDescargarPdf.setOnClickListener(v -> generarYCompartirPDF());
    }

    private void mostrarSelectorMesAnio() {
        int year = calendarActual.get(Calendar.YEAR);
        int month = calendarActual.get(Calendar.MONTH);
        
        MonthYearPickerDialog dialog = MonthYearPickerDialog.newInstance(year, month);
        dialog.setListener((selectedYear, selectedMonth) -> {
            calendarActual.set(Calendar.YEAR, selectedYear);
            calendarActual.set(Calendar.MONTH, selectedMonth);
            cargarDatosReales();
        });
        dialog.show(getParentFragmentManager(), "MonthYearPicker");
    }

    private void cargarDatosReales() {
        // Actualizar título del periodo
        tvPeriodo.setText(capitalizar(formatoFechaMes.format(calendarActual.getTime())));
        
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
                Map<String, Double> gastosPorCategoria = new HashMap<>();
                double totalGastos = 0;
                movimientosMesActual.clear();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    Movimiento movimiento = document.toObject(Movimiento.class);
                    movimiento.setId(document.getId());
                    
                    // Filtrar por fecha en memoria
                    if (movimiento.getFecha() != null) {
                        Date fechaMov = movimiento.getFecha();
                        if (!fechaMov.before(inicioMes.getTime()) && !fechaMov.after(finMes.getTime())) {
                            movimientosMesActual.add(movimiento);
                            
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

                // Ordenar movimientos por fecha
                Collections.sort(movimientosMesActual, (m1, m2) -> {
                    if (m1.getFecha() == null || m2.getFecha() == null) return 0;
                    return m2.getFecha().compareTo(m1.getFecha());
                });

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
                        int index = 5 - mesesAtras;
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

    // ============================================
    // GENERACIÓN DE PDF
    // ============================================

    private void generarYCompartirPDF() {
        if (movimientosMesActual.isEmpty()) {
            Toast.makeText(requireContext(), "No hay datos para generar el reporte", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File pdfFile = crearPDF();
            if (pdfFile != null && pdfFile.exists()) {
                Toast.makeText(requireContext(), "Reporte generado exitosamente", Toast.LENGTH_SHORT).show();
                compartirPDF(pdfFile);
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error al generar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private File crearPDF() throws IOException {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        
        Paint paint = new Paint();
        int y = 50;
        int margin = 40;
        int pageWidth = 595;

        // Título
        paint.setColor(Color.parseColor("#0F3D2E"));
        paint.setTextSize(24);
        paint.setFakeBoldText(true);
        String titulo = "AnTracker - Reporte de Gastos";
        canvas.drawText(titulo, margin, y, paint);
        
        y += 30;
        paint.setTextSize(14);
        paint.setFakeBoldText(false);
        paint.setColor(Color.GRAY);
        String periodo = "Periodo: " + capitalizar(formatoFechaMes.format(calendarActual.getTime()));
        canvas.drawText(periodo, margin, y, paint);
        
        y += 15;
        String fechaGeneracion = "Generado: " + formatoFechaCorta.format(new Date());
        canvas.drawText(fechaGeneracion, margin, y, paint);

        // Línea separadora
        y += 20;
        paint.setColor(Color.LTGRAY);
        canvas.drawLine(margin, y, pageWidth - margin, y, paint);

        // Filtrar solo gastos y ordenar por categoría
        List<Movimiento> gastos = new ArrayList<>();
        for (Movimiento m : movimientosMesActual) {
            if (m.getTipo().equalsIgnoreCase("gasto")) {
                gastos.add(m);
            }
        }
        
        Collections.sort(gastos, Comparator.comparing(Movimiento::getFecha, Comparator.nullsLast(Comparator.reverseOrder())));

        // Tabla de gastos
        y += 30;
        paint.setColor(Color.parseColor("#0F3D2E"));
        paint.setTextSize(16);
        paint.setFakeBoldText(true);
        canvas.drawText("Detalle de Gastos", margin, y, paint);
        
        y += 25;
        paint.setTextSize(10);
        paint.setFakeBoldText(true);
        paint.setColor(Color.WHITE);
        
        // Encabezados
        int[] cols = {margin, margin + 70, margin + 200, margin + 320, margin + 420};
        int rowHeight = 20;
        
        paint.setColor(Color.parseColor("#0F3D2E"));
        canvas.drawRect(margin, y - 15, pageWidth - margin, y + 5, paint);
        paint.setColor(Color.WHITE);
        canvas.drawText("Fecha", cols[0] + 5, y, paint);
        canvas.drawText("Descripción", cols[1] + 5, y, paint);
        canvas.drawText("Categoría", cols[2] + 5, y, paint);
        canvas.drawText("Monto", cols[3] + 5, y, paint);

        y += 20;
        paint.setFakeBoldText(false);
        paint.setColor(Color.BLACK);
        
        double totalGastos = 0;
        Map<String, Double> resumenCategoria = new HashMap<>();
        
        for (Movimiento mov : gastos) {
            // Alternar color de fondo
            if ((y / rowHeight) % 2 == 0) {
                paint.setColor(Color.parseColor("#F5F5F5"));
                canvas.drawRect(margin, y - 15, pageWidth - margin, y + 5, paint);
            }
            
            paint.setColor(Color.BLACK);
            String fecha = mov.getFecha() != null ? formatoFechaCorta.format(mov.getFecha()) : "-";
            canvas.drawText(fecha, cols[0] + 5, y, paint);
            
            String desc = mov.getDescripcion();
            if (desc.length() > 20) desc = desc.substring(0, 20) + "...";
            canvas.drawText(desc, cols[1] + 5, y, paint);
            
            canvas.drawText(capitalizar(mov.getCategoria()), cols[2] + 5, y, paint);
            canvas.drawText(formatoMoneda.format(mov.getMonto()), cols[3] + 5, y, paint);
            
            totalGastos += mov.getMonto();
            resumenCategoria.merge(mov.getCategoria().toLowerCase(), mov.getMonto(), Double::sum);
            
            y += rowHeight;
            
            // Nueva página si es necesario
            if (y > 750 && gastos.indexOf(mov) < gastos.size() - 1) {
                document.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(595, 842, document.getPages().size() + 1).create();
                page = document.startPage(pageInfo);
                canvas = page.getCanvas();
                y = 50;
            }
        }

        // Línea separadora
        y += 10;
        paint.setColor(Color.LTGRAY);
        canvas.drawLine(margin, y, pageWidth - margin, y, paint);
        
        // Total
        y += 25;
        paint.setColor(Color.BLACK);
        paint.setTextSize(14);
        paint.setFakeBoldText(true);
        canvas.drawText("TOTAL DE GASTOS: " + formatoMoneda.format(totalGastos), margin, y, paint);

        // Resumen por categoría
        y += 40;
        paint.setColor(Color.parseColor("#0F3D2E"));
        paint.setTextSize(14);
        canvas.drawText("Resumen por Categoría:", margin, y, paint);
        
        y += 20;
        paint.setTextSize(11);
        paint.setFakeBoldText(false);
        paint.setColor(Color.BLACK);
        
        for (Map.Entry<String, Double> entry : resumenCategoria.entrySet()) {
            double porcentaje = (entry.getValue() / totalGastos) * 100;
            String linea = String.format("  • %s: %s (%.1f%%)", 
                capitalizar(entry.getKey()),
                formatoMoneda.format(entry.getValue()),
                porcentaje);
            canvas.drawText(linea, margin, y, paint);
            y += 18;
        }

        document.finishPage(page);

        // Guardar archivo
        String fileName = "Reporte_AnTracker_" +
            calendarActual.get(Calendar.YEAR) + "_" + 
            (calendarActual.get(Calendar.MONTH) + 1) + ".pdf";
        
        File dir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        if (dir == null) {
            dir = requireContext().getFilesDir();
        }
        
        File pdfFile = new File(dir, fileName);
        FileOutputStream fos = new FileOutputStream(pdfFile);
        document.writeTo(fos);
        document.close();
        fos.close();
        
        return pdfFile;
    }

    private void compartirPDF(File pdfFile) {
        Uri uri = FileProvider.getUriForFile(requireContext(), 
            requireContext().getPackageName() + ".provider", pdfFile);
        
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/pdf");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Reporte AnTracker - " +
            capitalizar(formatoFechaMes.format(calendarActual.getTime())));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        startActivity(Intent.createChooser(intent, "Compartir reporte"));
    }
}