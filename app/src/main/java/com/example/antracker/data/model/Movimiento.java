package com.example.antracker.data.model;

import java.util.Date;

public class Movimiento {
    private String id;
    private String userId;
    private String tipo;
    private String categoria;
    private double monto;
    private String descripcion;
    private Date fecha;
    private Date fechaCreacion;
    private boolean esRecurrente;

    // Constructor vacío requerido por Firestore
    public Movimiento() {
        this.fechaCreacion = new Date();
        this.esRecurrente = false;
    }

    // Constructor completo
    public Movimiento(String userId, String tipo, String categoria,
                      double monto, String descripcion, Date fecha) {
        this.userId = userId;
        this.tipo = tipo;
        this.categoria = categoria;
        this.monto = monto;
        this.descripcion = descripcion;
        this.fecha = fecha;
        this.fechaCreacion = new Date();
        this.esRecurrente = false;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public boolean isEsRecurrente() { return esRecurrente; }
    public void setEsRecurrente(boolean esRecurrente) { this.esRecurrente = esRecurrente; }

    @Override
    public String toString() {
        return "Movimiento{" +
                "id='" + id + '\'' +
                ", tipo='" + tipo + '\'' +
                ", categoria='" + categoria + '\'' +
                ", monto=" + monto +
                ", descripcion='" + descripcion + '\'' +
                ", fecha=" + fecha +
                '}';
    }
}
