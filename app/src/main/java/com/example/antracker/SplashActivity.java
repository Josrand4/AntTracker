package com.example.antracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // 2 segundos
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // inicializa Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // verifica sesión después del delay
        new Handler(Looper.getMainLooper()).postDelayed(this::verificarYRedirigir, SPLASH_DELAY);
    }

    private void verificarYRedirigir() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        Intent intent;

        if (currentUser != null) {
            // Usuario autenticado lleva al Dashboard
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            // no hay sesión ir al Login
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
