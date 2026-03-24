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
import java.util.List;

public class MovimientoRepository {

    private final FirebaseFirestore db;
    private final CollectionReference movimientosRef;
    private final String userId;

    public MovimientoRepository() {
        db = FirebaseFirestore.getInstance();
        movimientosRef = db.collection("movimientos");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        } else {
            userId = "";
        }
    }

    /**
     * Agregar un nuevo movimiento
     */
    public void agregarMovimiento(Movimiento movimiento,
                                   OnSuccessListener<DocumentReference> onSuccess,
                                   OnFailureListener onFailure) {
        movimiento.setUserId(userId);
        movimientosRef.add(movimiento)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Obtener todos los movimientos del usuario actual
     */
    public void obtenerMovimientos(OnCompleteListener<QuerySnapshot> onComplete) {
        movimientosRef
                .whereEqualTo("userId", userId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(onComplete);
    }

    /**
     * Obtener movimientos por tipo (ingreso/gasto)
     */
    public void obtenerMovimientosPorTipo(String tipo,
                                          OnCompleteListener<QuerySnapshot> onComplete) {
        movimientosRef
                .whereEqualTo("userId", userId)
                .whereEqualTo("tipo", tipo)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(onComplete);
    }

    /**
     * Obtener movimientos por categoría
     */
    public void obtenerMovimientosPorCategoria(String categoria,
                                                OnCompleteListener<QuerySnapshot> onComplete) {
        movimientosRef
                .whereEqualTo("userId", userId)
                .whereEqualTo("categoria", categoria)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(onComplete);
    }

    /**
     * Obtener movimientos de un rango de fechas
     */
    public void obtenerMovimientosPorFecha(Date fechaInicio, Date fechaFin,
                                            OnCompleteListener<QuerySnapshot> onComplete) {
        movimientosRef
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                .whereLessThanOrEqualTo("fecha", fechaFin)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(onComplete);
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
        movimiento.setUserId(userId);
        movimientosRef.document(movimientoId)
                .set(movimiento)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    /**
     * Calcular totales por tipo en un período
     */
    public void calcularTotales(Date fechaInicio, Date fechaFin,
                                 OnCompleteListener<QuerySnapshot> onComplete) {
        movimientosRef
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                .whereLessThanOrEqualTo("fecha", fechaFin)
                .get()
                .addOnCompleteListener(onComplete);
    }

    public String getUserId() {
        return userId;
    }
}
