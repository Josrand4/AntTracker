package com.example.antracker;

/**
 * Clase legacy — se mantiene para compatibilidad pero el modelo principal
 * es {@link com.example.antracker.data.model.Movimiento}.
 *
 * BUG CORREGIDO: tenía una clase anidada con el mismo nombre (Gasto dentro de Gasto),
 * lo cual causaba error de compilación.
 */
public class Gasto {

    private String descripcion;
    private double monto;
    private String fecha;

    public Gasto(String descripcion, double monto, String fecha) {
        this.descripcion = descripcion;
        this.monto       = monto;
        this.fecha       = fecha;
    }

    public String getDescripcion() { return descripcion; }
    public double getMonto()       { return monto; }
    public String getFecha()       { return fecha; }
}
