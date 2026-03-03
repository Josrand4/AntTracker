package com.example.antracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // 1. Referenciar los componentes
        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvSignIn = findViewById(R.id.tvSignIn);
        EditText etName = findViewById(R.id.etName);
        EditText etEmail = findViewById(R.id.etEmail);

        // Acción del botón principal
        btnSignUp.setOnClickListener(v -> {
            String nombre = etName.getText().toString();
            if (!nombre.isEmpty()) {
                Toast.makeText(this, "¡Registro exitoso para " + nombre + "!", Toast.LENGTH_SHORT).show();
                // Aquí podrías guardar en base de datos más adelante
            }
        });

        // 2. Ir a Login si ya tiene cuenta
        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
