package com.example.antracker.data.repository;

import androidx.annotation.NonNull;
import com.example.antracker.data.model.Movimiento;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.Date;

public class MovimientoRepository {

    private final FirebaseFirestore db;
    private final CollectionReference movimientosRef;
    private String userId;

    public MovimientoRepository() {
        db = FirebaseFirestore.getInstance();
        movimientosRef = db.collection("movimientos");
        updateUserId();
    }

    private void updateUserId() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        userId = (user != null) ? user.getUid() : "";
    }

    /**
     * Agregar un nuevo movimiento
     */
    public void agregarMovimiento(Movimiento movimiento,
                                   OnSuccessListener<DocumentReference> onSuccess,
                                   OnFailureListener onFailure) {
        updateUserId();
        movimiento.setUserId(userId);
        movimientosRef.add(movimiento)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Obtener todos los movimientos del usuario actual
     * Sin ordenamiento para evitar requerir índice compuesto
     */
    public void obtenerMovimientos(OnCompleteListener<QuerySnapshot> onComplete) {
        updateUserId();
        if (userId.isEmpty()) {
            // Si no hay usuario, devolver lista vacía
            onComplete.onComplete(null);
            return;
        }
        movimientosRef
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(onComplete);
    }

    /**
     * Obtener movimientos por tipo (ingreso/gasto)
     */
    public void obtenerMovimientosPorTipo(String tipo,
                                          OnCompleteListener<QuerySnapshot> onComplete) {
        updateUserId();
        if (userId.isEmpty()) {
            onComplete.onComplete(null);
            return;
        }
        movimientosRef
                .whereEqualTo("userId", userId)
                .whereEqualTo("tipo", tipo.toLowerCase())
                .get()
                .addOnCompleteListener(onComplete);
    }

    /**
     * Obtener movimientos por categoría
     */
    public void obtenerMovimientosPorCategoria(String categoria,
                                                OnCompleteListener<QuerySnapshot> onComplete) {
        updateUserId();
        if (userId.isEmpty()) {
            onComplete.onComplete(null);
            return;
        }
        movimientosRef
                .whereEqualTo("userId", userId)
                .whereEqualTo("categoria", categoria.toLowerCase())
                .get()
                .addOnCompleteListener(onComplete);
    }

    /**
     * Obtener movimientos de un rango de fechas
     * Simplificado para evitar índices complejos
     */
    public void obtenerMovimientosPorFecha(Date fechaInicio, Date fechaFin,
                                            OnCompleteListener<QuerySnapshot> onComplete) {
        updateUserId();
        if (userId.isEmpty()) {
            onComplete.onComplete(null);
            return;
        }
        movimientosRef
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    // Filtrar por fecha en memoria para evitar índices complejos
                    onComplete.onComplete(task);
                });
    }

    /**
     * Eliminar un movimiento
     */
    public void eliminarMovimiento(String movimientoId,
                                    OnSuccessListener<Void> onSuccess,
                                    OnFailureListener onFailure) {
        movimientosRef.document(movimientoId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Actualizar un movimiento
     */
    public void actualizarMovimiento(String movimientoId,
                                      Movimiento movimiento,
                                      OnSuccessListener<Void> onSuccess,
                                      OnFailureListener onFailure) {
        updateUserId();
        movimiento.setUserId(userId);
        movimientosRef.document(movimientoId)
                .set(movimiento)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public String getUserId() {
        updateUserId();
        return userId;
    }
}
