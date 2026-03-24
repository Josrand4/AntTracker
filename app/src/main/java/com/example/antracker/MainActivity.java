package com.example.antracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.example.antracker.ui.DashboardFragment;
import com.example.antracker.ui.MovimientosFragment;
import com.example.antracker.ui.ReportesFragment;
import com.example.antracker.ui.PerfilFragment;


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
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment fragment = null;

            if (itemId == R.id.nav_dashboard) {
                fragment = new DashboardFragment();
            } else if (itemId == R.id.nav_movimientos) {
                fragment = new MovimientosFragment();
            } else if (itemId == R.id.nav_reportes) {
                fragment = new ReportesFragment();
            } else if (itemId == R.id.nav_perfil) {
                fragment = new PerfilFragment();
            }

            if (fragment != null) {
                cargarFragmento(fragment);
                return true;
            }
            return false;
        });

        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AgregarMovimientoActivity.class);
            startActivity(intent);
        });
    }

    private void cargarFragmento(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit();
    }
}
