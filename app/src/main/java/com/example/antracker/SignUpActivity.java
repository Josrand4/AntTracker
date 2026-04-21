package com.example.antracker;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.antracker.data.model.Usuario;
import com.example.antracker.data.repository.UsuarioRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private UsuarioRepository usuarioRepository;

    private EditText etName, etEmail, etPassword;
    private Button btnSignUp;
    private TextView tvSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        usuarioRepository = new UsuarioRepository();

        // inicializar vistas
        inicializarVistas();
        configurarListeners();
    }

    private void inicializarVistas() {
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignIn = findViewById(R.id.tvSignIn);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        

        etPassword = findViewById(R.id.etPassword);
    }

    private void configurarListeners() {
        //  registro
        btnSignUp.setOnClickListener(v -> registrarUsuario());

        // Ir a Login si ya tiene cuenta
        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registrarUsuario() {
        String nombre = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = ((EditText)findViewById(R.id.etConfirmPassword)).getText().toString().trim();

        // Validaciones
        if (nombre.isEmpty()) {
            etName.setError("Ingresa tu nombre");
            return;
        }

        if (email.isEmpty()) {
            etEmail.setError("Ingresa tu email");
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Ingresa una contraseña");
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres");
            return;
        }

        if (!password.equals(confirmPassword)) {
            ((EditText)findViewById(R.id.etConfirmPassword)).setError("Las contraseñas no coinciden");
            return;
        }

        //  usuario con Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        
                        //  información adicional en Firestore
                        Usuario nuevoUsuario = new Usuario(
                                user.getUid(),
                                nombre,
                                email,
                                null  // Sin foto de perfil para registro manual
                        );

                        usuarioRepository.guardarUsuario(nuevoUsuario,
                                aVoid -> {
                                    Toast.makeText(SignUpActivity.this, 
                                            "¡Registro exitoso! Bienvenido " + nombre, 
                                            Toast.LENGTH_SHORT).show();
                                    navigateToMainActivity();
                                },
                                e -> {
                                    // Aún así navegamos si el auth funcionó
                                    Toast.makeText(SignUpActivity.this, 
                                            "Registro exitoso", 
                                            Toast.LENGTH_SHORT).show();
                                    navigateToMainActivity();
                                });
                    } else {
                        String errorMessage = task.getException() != null ? 
                                task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(SignUpActivity.this, 
                                "Error en el registro: " + errorMessage, 
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}