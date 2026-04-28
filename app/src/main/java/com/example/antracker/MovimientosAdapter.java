package com.example.antracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.antracker.data.model.Movimiento;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MovimientosAdapter extends RecyclerView.Adapter<MovimientosAdapter.ViewHolder> {

    private final List<Movimiento>     movimientos;
    private final NumberFormat         formatoMoneda;
    private final SimpleDateFormat     formatoFecha;

    // ── Listeners ──────────────────────────────────────────────────────────────

    public interface OnDeleteClickListener {
        void onDeleteClick(Movimiento movimiento);
    }

    public interface OnEditClickListener {
        void onEditClick(Movimiento movimiento);
    }

    private final OnDeleteClickListener deleteListener;
    private final OnEditClickListener   editListener;

    // ── Constructor ─────────────────────────────────────────────────────────────

    public MovimientosAdapter(List<Movimiento> movimientos,
                               OnDeleteClickListener deleteListener,
                               OnEditClickListener editListener) {
        this.movimientos    = movimientos;
        this.deleteListener = deleteListener;
        this.editListener   = editListener;
        this.formatoMoneda  = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        this.formatoFecha   = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "MX"));
    }

    // ── Adapter overrides ────────────────────────────────────────────────────────

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movimiento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movimiento mov = movimientos.get(position);

        holder.tvDescripcion.setText(mov.getDescripcion());
        holder.tvCategoria.setText(capitalizar(mov.getCategoria()));
        holder.tvFecha.setText(mov.getFecha() != null
                ? formatoFecha.format(mov.getFecha()) : "-");
        holder.tvMonto.setText(formatoMoneda.format(mov.getMonto()));

        // Color del monto según tipo
        int colorRes = "ingreso".equalsIgnoreCase(mov.getTipo())
                ? R.color.verde_ingreso
                : R.color.rojo_gasto;
        holder.tvMonto.setTextColor(
                holder.itemView.getContext().getResources().getColor(colorRes, null));

        // Botón eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(mov);
        });

        // Botón editar
        holder.btnEditar.setOnClickListener(v -> {
            if (editListener != null) editListener.onEditClick(mov);
        });
    }

    @Override
    public int getItemCount() {
        return movimientos.size();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }

    // ── ViewHolder ───────────────────────────────────────────────────────────────

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final TextView    tvDescripcion;
        final TextView    tvCategoria;
        final TextView    tvFecha;
        final TextView    tvMonto;
        final ImageButton btnEliminar;
        final ImageButton btnEditar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion);
            tvCategoria   = itemView.findViewById(R.id.tv_categoria);
            tvFecha       = itemView.findViewById(R.id.tv_fecha);
            tvMonto       = itemView.findViewById(R.id.tv_monto);
            btnEliminar   = itemView.findViewById(R.id.btn_eliminar);
            btnEditar     = itemView.findViewById(R.id.btn_editar);
        }
    }
}
