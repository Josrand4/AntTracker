package com.example.antracker.data.repository;

import com.example.antracker.data.model.Usuario;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class UsuarioRepository {

    private final FirebaseFirestore db;
    private final CollectionReference usuariosRef;

    public UsuarioRepository() {
        db = FirebaseFirestore.getInstance();
        usuariosRef = db.collection("usuarios");
    }

    /**
     * Crear o actualizar usuario en Firestore
     */
    public void guardarUsuario(Usuario usuario,
                                OnSuccessListener<Void> onSuccess,
                                OnFailureListener onFailure) {
        usuariosRef.document(usuario.getUid())
                .set(usuario, SetOptions.merge())
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Crear usuario desde FirebaseUser (después de Google Sign-In)
     */
    public void crearUsuarioDesdeFirebase(FirebaseUser firebaseUser,
                                           OnSuccessListener<Void> onSuccess,
                                           OnFailureListener onFailure) {
        Usuario usuario = new Usuario(
                firebaseUser.getUid(),
                firebaseUser.getDisplayName(),
                firebaseUser.getEmail(),
                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null
        );

        guardarUsuario(usuario, onSuccess, onFailure);
    }
}
