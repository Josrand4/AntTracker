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

    private EditText etName, etEmail, etPassword, etConfirmPassword;
    private Button btnSignUp;
    private TextView tvSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        usuarioRepository = new UsuarioRepository();

        // Inicializar vistas
        inicializarVistas();
        configurarListeners();
    }

    private void inicializarVistas() {
        btnSignUp = findViewById(R.id.btnSignUp);
        tvSignIn = findViewById(R.id.tvSignIn);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
    }

    private void configurarListeners() {
        // Botón de registro
        btnSignUp.setOnClickListener(v -> registrarUsuario());

        // Ir a Login si ya tiene cuenta
        tvSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void registrarUsuario() {
        String nombre = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

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
            etConfirmPassword.setError("Las contraseñas no coinciden");
            return;
        }

        btnSignUp.setEnabled(false);

        // Crear usuario con Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    btnSignUp.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null) {
                            // Actualizar display name
                            user.updateProfile(new com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                    .setDisplayName(nombre)
                                    .build());

                            // Guardar información adicional en Firestore
                            Usuario nuevoUsuario = new Usuario(
                                    user.getUid(),
                                    nombre,
                                    email,
                                    null  // Sin foto de perfil para registro manual
                            );

                            usuarioRepository.guardarUsuario(nuevoUsuario,
                                    aVoid -> {
                                        // Enviar email de verificación (2FA)
                                        enviarEmailVerificacion(user, email, password);
                                    },
                                    e -> {
                                        // Aún así enviar verificación
                                        enviarEmailVerificacion(user, email, password);
                                    });
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Toast.makeText(SignUpActivity.this,
                                "Error en el registro: " + errorMessage,
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void enviarEmailVerificacion(FirebaseUser user, String email, String password) {
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpActivity.this,
                                "Registro exitoso. Se ha enviado un enlace de verificación a tu correo.",
                                Toast.LENGTH_LONG).show();

                        // Ir a verificación de 2 pasos
                        Intent intent = new Intent(SignUpActivity.this, TwoFactorAuthActivity.class);
                        intent.putExtra(TwoFactorAuthActivity.EXTRA_EMAIL, email);
                        intent.putExtra(TwoFactorAuthActivity.EXTRA_PASSWORD, password);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SignUpActivity.this,
                                "Registro exitoso, pero hubo un error al enviar el correo de verificación.",
                                Toast.LENGTH_LONG).show();
                        navigateToLogin();
                    }
                });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}