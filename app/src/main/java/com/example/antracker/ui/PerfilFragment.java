package com.example.antracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.antracker.LoginActivity;
import com.example.antracker.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PerfilFragment extends Fragment {

    private ImageView ivFotoPerfil;
    private TextView tvNombre, tvEmail;
    private Button btnCerrarSesion;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_perfil, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Google Sign-In Client para poder cerrar sesión
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        inicializarVistas(view);
        cargarDatosUsuario();
        configurarListeners();
    }

    private void inicializarVistas(View view) {
        ivFotoPerfil = view.findViewById(R.id.iv_foto_perfil);
        tvNombre = view.findViewById(R.id.tv_nombre);
        tvEmail = view.findViewById(R.id.tv_email);
        btnCerrarSesion = view.findViewById(R.id.btn_cerrar_sesion);
    }

    private void cargarDatosUsuario() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            tvNombre.setText(user.getDisplayName());
            tvEmail.setText(user.getEmail());

            // Cargar foto con Glide
            if (user.getPhotoUrl() != null) {
                Glide.with(this)
                        .load(user.getPhotoUrl())
                        .placeholder(R.drawable.bg_circle)
                        .circleCrop()
                        .into(ivFotoPerfil);
            } else {
                ivFotoPerfil.setImageResource(R.drawable.bg_circle);
            }
        }
    }

    private void configurarListeners() {
        btnCerrarSesion.setOnClickListener(v -> cerrarSesion());
    }

    private void cerrarSesion() {
        // Cerrar sesión de Firebase
        mAuth.signOut();

        // Cerrar sesión de Google
        mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
            Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(requireContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });
    }
}
