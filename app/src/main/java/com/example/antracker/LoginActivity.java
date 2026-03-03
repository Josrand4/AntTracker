package com.example.antracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnSignIn = findViewById(R.id.btnSignIn);
        TextView tvSignUp = findViewById(R.id.tvSignUp);

        // Acción al presionar Iniciar Sesión para ir al Home
        btnSignIn.setOnClickListener(v -> {
            // 1. Mensaje de éxito (Opcional)
            Toast.makeText(this, "Login Exitoso", Toast.LENGTH_SHORT).show();

            // 2. Crear el Intent para saltar a MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);

            // 3. Finalizar esta actividad para que no se regrese al Login al dar "atrás"
            finish();
        });

        // Volver a Registro si no tiene cuenta
        tvSignUp.setOnClickListener(v -> {
            // Esto cierra la pantalla de Login y vuelve a la anterior (SignUpActivity)
            finish();
        });
    }
}
