package com.example.antracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antracker.AgregarMovimientoActivity;
import com.example.antracker.EditarMovimientoActivity;
import com.example.antracker.MovimientosAdapter;
import com.example.antracker.R;
import com.example.antracker.data.model.Movimiento;
import com.example.antracker.data.repository.MovimientoRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovimientosFragment extends Fragment {

    private Spinner              spinnerFiltroTipo;
    private Spinner              spinnerFiltroCategoria;
    private RecyclerView         recyclerView;
    private FloatingActionButton fabAdd;

    private MovimientosAdapter   adapter;
    private List<Movimiento>     listaMovimientos;  // fuente de datos completa
    private List<Movimiento>     listaFiltrada;     // datos mostrados
    private MovimientoRepository movimientoRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_movimientos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        movimientoRepository = new MovimientoRepository();

        inicializarVistas(view);
        configurarSpinners();
        configurarRecyclerView();
        configurarListeners();
        cargarMovimientos();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarMovimientos(); // Refresca al volver de Agregar / Editar
    }

    // ── Inicialización ────────────────────────────────────────────────────────────

    private void inicializarVistas(View view) {
        spinnerFiltroTipo      = view.findViewById(R.id.spinner_filtro_tipo);
        spinnerFiltroCategoria = view.findViewById(R.id.spinner_filtro_categoria);
        recyclerView           = view.findViewById(R.id.recycler_movimientos);
        fabAdd                 = view.findViewById(R.id.fab_add_movimiento);
    }

    private void configurarSpinners() {
        String[] tipos = {"Todos", "Ingreso", "Gasto"};
        spinnerFiltroTipo.setAdapter(crearAdapter(tipos));

        String[] categorias = {"Todas", "Fijo", "Variable", "Hormiga", "Salario", "Freelance", "Inversiones", "Otros"};
        spinnerFiltroCategoria.setAdapter(crearAdapter(categorias));
    }

    private ArrayAdapter<String> crearAdapter(String[] items) {
        ArrayAdapter<String> a = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, items);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return a;
    }

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        listaMovimientos = new ArrayList<>();
        listaFiltrada    = new ArrayList<>();

        adapter = new MovimientosAdapter(
                listaFiltrada,
                this::mostrarDialogoEliminar,   // delete
                this::abrirEditar               // edit  ← NUEVO
        );

        recyclerView.setAdapter(adapter);
    }

    private void configurarListeners() {
        AdapterView.OnItemSelectedListener filtroListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                aplicarFiltros();
            }
            @Override
            public void onNothingSelected(AdapterView<?> p) {}
        };

        spinnerFiltroTipo.setOnItemSelectedListener(filtroListener);
        spinnerFiltroCategoria.setOnItemSelectedListener(filtroListener);

        fabAdd.setOnClickListener(v ->
                startActivity(new Intent(requireContext(), AgregarMovimientoActivity.class)));
    }

    // ── Datos ─────────────────────────────────────────────────────────────────────

    private void cargarMovimientos() {
        movimientoRepository.obtenerMovimientos(task -> {
            if (task == null || !task.isSuccessful() || task.getResult() == null) return;

            listaMovimientos.clear();

            for (QueryDocumentSnapshot doc : task.getResult()) {
                Movimiento m = doc.toObject(Movimiento.class);
                m.setId(doc.getId());
                listaMovimientos.add(m);
            }

            // Ordenar por fecha descendente (más reciente primero)
            Collections.sort(listaMovimientos, (a, b) -> {
                if (a.getFecha() == null && b.getFecha() == null) return 0;
                if (a.getFecha() == null) return 1;
                if (b.getFecha() == null) return -1;
                return b.getFecha().compareTo(a.getFecha());
            });

            aplicarFiltros();
        });
    }

    private void aplicarFiltros() {
        String tipoSel      = spinnerFiltroTipo.getSelectedItem().toString();
        String categoriaSel = spinnerFiltroCategoria.getSelectedItem().toString();

        listaFiltrada.clear();

        for (Movimiento m : listaMovimientos) {
            boolean tipoOk = tipoSel.equals("Todos")
                    || m.getTipo().equalsIgnoreCase(tipoSel);

            boolean categoriaOk = categoriaSel.equals("Todas")
                    || m.getCategoria().equalsIgnoreCase(categoriaSel);

            if (tipoOk && categoriaOk) listaFiltrada.add(m);
        }

        adapter.notifyDataSetChanged();
    }

    // ── Acciones ──────────────────────────────────────────────────────────────────

    /** Abre EditarMovimientoActivity pasando todos los datos del movimiento */
    private void abrirEditar(Movimiento movimiento) {
        Intent intent = new Intent(requireContext(), EditarMovimientoActivity.class);
        intent.putExtra(EditarMovimientoActivity.EXTRA_MOVIMIENTO_ID,          movimiento.getId());
        intent.putExtra(EditarMovimientoActivity.EXTRA_MOVIMIENTO_TIPO,        movimiento.getTipo());
        intent.putExtra(EditarMovimientoActivity.EXTRA_MOVIMIENTO_CATEGORIA,   movimiento.getCategoria());
        intent.putExtra(EditarMovimientoActivity.EXTRA_MOVIMIENTO_MONTO,       movimiento.getMonto());
        intent.putExtra(EditarMovimientoActivity.EXTRA_MOVIMIENTO_DESCRIPCION, movimiento.getDescripcion());
        intent.putExtra(EditarMovimientoActivity.EXTRA_MOVIMIENTO_FECHA_MS,
                movimiento.getFecha() != null ? movimiento.getFecha().getTime() : System.currentTimeMillis());
        startActivity(intent);
    }

    private void mostrarDialogoEliminar(Movimiento movimiento) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar movimiento")
                .setMessage("¿Deseas eliminar \"" + movimiento.getDescripcion() + "\"?")
                .setPositiveButton("Eliminar", (d, w) -> eliminarMovimiento(movimiento))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarMovimiento(Movimiento movimiento) {
        if (movimiento.getId() == null) return;

        movimientoRepository.eliminarMovimiento(
                movimiento.getId(),
                unused -> {
                    listaMovimientos.remove(movimiento);
                    aplicarFiltros();
                },
                e -> e.printStackTrace()
        );
    }
}
