package com.example.antracker;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class TwoFactorAuthActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "email";
    public static final String EXTRA_PASSWORD = "password";

    private TextView tvEmail;
    private EditText etCodigo;
    private Button btnVerificar, btnReenviar;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private String email;
    private String password;
    private Handler handler;
    private Runnable verificarRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verificacion);

        mAuth = FirebaseAuth.getInstance();
        handler = new Handler(Looper.getMainLooper());

        email = getIntent().getStringExtra(EXTRA_EMAIL);
        password = getIntent().getStringExtra(EXTRA_PASSWORD);

        if (email == null || password == null) {
            Toast.makeText(this, "Error: Datos de sesión no válidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        inicializarVistas();
        tvEmail.setText(email);
        configurarListeners();
        enviarEmailVerificacion();
        iniciarVerificacionAutomatica();
    }

    private void inicializarVistas() {
        tvEmail = findViewById(R.id.tvEmail);
        etCodigo = findViewById(R.id.et_codigo);
        btnVerificar = findViewById(R.id.btn_verificar);
        btnReenviar = findViewById(R.id.btn_reenviar);
        progressBar = findViewById(R.id.progressBar);
    }

    private void configurarListeners() {
        btnVerificar.setOnClickListener(v -> verificarManualmente());
        btnReenviar.setOnClickListener(v -> enviarEmailVerificacion());
    }

    private void enviarEmailVerificacion() {
        showLoading(true);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        showLoading(false);
                        if (task.isSuccessful()) {
                            Toast.makeText(this,
                                    "Se ha enviado un enlace de verificación a tu correo",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this,
                                    "Error al enviar el correo: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void iniciarVerificacionAutomatica() {
        verificarRunnable = new Runnable() {
            @Override
            public void run() {
                verificarEmailAutomaticamente();
                handler.postDelayed(this, 3000); // Verificar cada 3 segundos
            }
        };
        handler.post(verificarRunnable);
    }

    private void verificarEmailAutomaticamente() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful() && user.isEmailVerified()) {
                    handler.removeCallbacks(verificarRunnable);
                    Toast.makeText(this, "¡Email verificado correctamente!", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                }
            });
        }
    }

    private void verificarManualmente() {
        String codigo = etCodigo.getText().toString().trim();
        if (codigo.isEmpty()) {
            etCodigo.setError("Ingresa el código");
            return;
        }

        showLoading(true);

        // Recargar usuario y verificar
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (task.isSuccessful() && user.isEmailVerified()) {
                    showLoading(false);
                    Toast.makeText(this, "¡Verificación exitosa!", Toast.LENGTH_SHORT).show();
                    handler.removeCallbacks(verificarRunnable);
                    navigateToMainActivity();
                } else {
                    showLoading(false);
                    Toast.makeText(this,
                            "El correo aún no ha sido verificado. Revisa tu bandeja de entrada y haz clic en el enlace.",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(TwoFactorAuthActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnVerificar.setEnabled(!show);
        btnReenviar.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && verificarRunnable != null) {
            handler.removeCallbacks(verificarRunnable);
        }
    }
}