package com.example.antracker;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.antracker.R;
import com.example.antracker.ui.DashboardFragment;


public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FloatingActionButton fabAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inicializarVistas();
        configurarNavegacion();

        // Cargar fragmento inicial
        if (savedInstanceState == null) {
            cargarFragmento(new DashboardFragment());
        }
    }

    private void inicializarVistas() {
        bottomNav = findViewById(R.id.bottom_nav);
        fabAdd = findViewById(R.id.fab_add);
    }

    private void configurarNavegacion() {
//        bottomNav.setOnItemSelectedListener(item -> {
//            Fragment fragment = null;
//            int itemId = item.getItemId();
//
//            if (itemId == R.id.nav_dashboard) {
//                fragment = new DashboardFragment();
//            } else if (itemId == R.id.nav_gastos) {
//                // fragment = new GastosFragment(); // Lo crearemos después
//            } else if (itemId == R.id.nav_ingresos) {
//                // fragment = new IngresosFragment();
//            } else if (itemId == R.id.nav_reportes) {
//                // fragment = new ReportesFragment();
//            }
//
//            if (fragment != null) {
//                cargarFragmento(fragment);
//                return true;
//            }
//            return false;
//        });
//
//        fabAdd.setOnClickListener(v -> {
//            // Abrir diálogo para agregar gasto/ingreso rápido
//            mostrarDialogoAgregar();
//        });
    }

    private void cargarFragmento(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }

    private void mostrarDialogoAgregar() {
        // Implementaremos esto después
    }
}