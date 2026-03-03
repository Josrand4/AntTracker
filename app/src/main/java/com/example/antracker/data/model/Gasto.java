package com.example.antracker.data.model;

public class Gasto {
    public enum TipoGasto {
        FIJO,
        VARIABLE,
        HORMIGA
    }

    private String id;
    private double monto;
    private String descripcion;
    private TipoGasto tipo;
    private String categoria;
    private long fecha; // timestamp
    private boolean esRecurrente;

    public Gasto(double monto, String descripcion, TipoGasto tipo,
                 String categoria, long fecha) {
        this.monto = monto;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.categoria = categoria;
        this.fecha = fecha;
    }

    // Getters y Setters
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoGasto getTipo() { return tipo; }
    public void setTipo(TipoGasto tipo) { this.tipo = tipo; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }
}