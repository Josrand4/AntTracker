package com.example.antracker;

import android.content.Intent;
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

    private List<Movimiento> movimientos;
    private NumberFormat formatoMoneda;
    private SimpleDateFormat formatoFecha;
    private OnItemClickListener listener;
    private OnEliminarClickListener eliminarListener;

    public interface OnItemClickListener {
        void onItemClick(Movimiento movimiento);
    }

    public interface OnEliminarClickListener {
        void onEliminarClick(Movimiento movimiento, int position);
    }

    public MovimientosAdapter(List<Movimiento> movimientos) {
        this.movimientos = movimientos;
        this.formatoMoneda = NumberFormat.getCurrencyInstance(new Locale("es", "MX"));
        this.formatoFecha = new SimpleDateFormat("dd/MM/yyyy", new Locale("es", "MX"));
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnEliminarClickListener(OnEliminarClickListener listener) {
        this.eliminarListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movimiento, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Movimiento movimiento = movimientos.get(position);

        holder.tvDescripcion.setText(movimiento.getDescripcion());
        holder.tvCategoria.setText(capitalizar(movimiento.getCategoria()));

        if (movimiento.getFecha() != null) {
            holder.tvFecha.setText(formatoFecha.format(movimiento.getFecha()));
        } else {
            holder.tvFecha.setText("-");
        }

        holder.tvMonto.setText(formatoMoneda.format(movimiento.getMonto()));

        // Color según tipo
        if (movimiento.getTipo().equalsIgnoreCase("ingreso")) {
            holder.tvMonto.setTextColor(holder.itemView.getContext().getResources()
                    .getColor(R.color.verde_ingreso, null));
        } else {
            holder.tvMonto.setTextColor(holder.itemView.getContext().getResources()
                    .getColor(R.color.rojo_gasto, null));
        }

        // Click en el item para editar
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(movimiento);
            }
        });

        // Click en botón eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            if (eliminarListener != null) {
                eliminarListener.onEliminarClick(movimiento, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return movimientos.size();
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) return texto;
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDescripcion, tvCategoria, tvFecha, tvMonto;
        ImageButton btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDescripcion = itemView.findViewById(R.id.tv_descripcion);
            tvCategoria = itemView.findViewById(R.id.tv_categoria);
            tvFecha = itemView.findViewById(R.id.tv_fecha);
            tvMonto = itemView.findViewById(R.id.tv_monto);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar);
        }
    }
}