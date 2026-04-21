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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.antracker.AgregarMovimientoActivity;
import com.example.antracker.MovimientosAdapter;
import com.example.antracker.R;
import com.example.antracker.data.model.Movimiento;
import com.example.antracker.data.repository.MovimientoRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class MovimientosFragment extends Fragment {

    private Spinner spinnerFiltroTipo;
    private Spinner spinnerFiltroCategoria;
    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;

    private MovimientosAdapter adapter;
    private List<Movimiento> listaMovimientos;
    private List<Movimiento> listaFiltrada;
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
        // Recargar datos cuando se vuelve a este fragment
        cargarMovimientos();
    }

    private void inicializarVistas(View view) {
        spinnerFiltroTipo = view.findViewById(R.id.spinner_filtro_tipo);
        spinnerFiltroCategoria = view.findViewById(R.id.spinner_filtro_categoria);
        recyclerView = view.findViewById(R.id.recycler_movimientos);
        fabAdd = view.findViewById(R.id.fab_add_movimiento);
    }

    private void configurarSpinners() {
        String[] tipos = {"Todos", "Ingreso", "Gasto"};
        ArrayAdapter<String> tipoAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, tipos);
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltroTipo.setAdapter(tipoAdapter);

        String[] categorias = {"Todas", "Fijo", "Variable", "Hormiga", "Salario", "Otros"};
        ArrayAdapter<String> categoriaAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, categorias);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFiltroCategoria.setAdapter(categoriaAdapter);
    }

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        listaMovimientos = new ArrayList<>();
        listaFiltrada = new ArrayList<>();
        adapter = new MovimientosAdapter(listaFiltrada);
        recyclerView.setAdapter(adapter);
    }

    private void configurarListeners() {
        spinnerFiltroTipo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        spinnerFiltroCategoria.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                aplicarFiltros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AgregarMovimientoActivity.class);
            startActivity(intent);
        });
    }

    private void cargarMovimientos() {
        movimientoRepository.obtenerMovimientos(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                listaMovimientos.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Movimiento movimiento = document.toObject(Movimiento.class);
                    movimiento.setId(document.getId());
                    listaMovimientos.add(movimiento);
                }
                aplicarFiltros();
            }
        });
    }

    private void aplicarFiltros() {
        String tipoSeleccionado = spinnerFiltroTipo.getSelectedItem().toString();
        String categoriaSeleccionada = spinnerFiltroCategoria.getSelectedItem().toString();

        listaFiltrada.clear();

        for (Movimiento mov : listaMovimientos) {
            boolean tipoMatch = tipoSeleccionado.equals("Todos") ||
                    mov.getTipo().equalsIgnoreCase(tipoSeleccionado.toLowerCase());
            boolean categoriaMatch = categoriaSeleccionada.equals("Todas") ||
                    mov.getCategoria().equalsIgnoreCase(categoriaSeleccionada.toLowerCase());

            if (tipoMatch && categoriaMatch) {
                listaFiltrada.add(mov);
            }
        }

        adapter.notifyDataSetChanged();
    }
}