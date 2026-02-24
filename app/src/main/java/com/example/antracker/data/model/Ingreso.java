package com.example.antracker.data.model;

public class Ingreso {
    public enum TipoIngreso {
        SALARIO, FREELANCE, INVERSION, OTRO
    }

    private String id;
    private double monto;
    private String descripcion;
    private TipoIngreso tipo;
    private long fecha;
    private boolean esRecurrente;

    public Ingreso(double monto, String descripcion, TipoIngreso tipo, long fecha) {
        this.monto = monto;
        this.descripcion = descripcion;
        this.tipo = tipo;
        this.fecha = fecha;
    }

    // Getters y Setters
    public double getMonto() { return monto; }
    public void setMonto(double monto) { this.monto = monto; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public TipoIngreso getTipo() { return tipo; }
    public void setTipo(TipoIngreso tipo) { this.tipo = tipo; }

    public long getFecha() { return fecha; }
    public void setFecha(long fecha) { this.fecha = fecha; }
}