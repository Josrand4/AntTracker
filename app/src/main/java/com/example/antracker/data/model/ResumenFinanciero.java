package com.example.antracker.data.model;

public class ResumenFinanciero {
    private double ingresosTotales;
    private double gastosFijos;
    private double gastosVariables;
    private double gastosHormiga;
    private double gastoDiarioPromedio;
    private int diasEnPeriodo;

    public ResumenFinanciero() {
        this.ingresosTotales = 0;
        this.gastosFijos = 0;
        this.gastosVariables = 0;
        this.gastosHormiga = 0;
        this.gastoDiarioPromedio = 0;
    }

    // Métodos de cálculo
    public double getGastosTotales() {
        return gastosFijos + gastosVariables + gastosHormiga;
    }

    public double getSaldoFinal() {
        return ingresosTotales - getGastosTotales();
    }

    public void calcularGastoDiarioPromedio() {
        if (diasEnPeriodo > 0) {
            this.gastoDiarioPromedio = getGastosTotales() / diasEnPeriodo;
        }
    }

    // Getters y Setters
    public double getIngresosTotales() { return ingresosTotales; }
    public void setIngresosTotales(double ingresosTotales) {
        this.ingresosTotales = ingresosTotales;
    }

    public double getGastosFijos() { return gastosFijos; }
    public void setGastosFijos(double gastosFijos) { this.gastosFijos = gastosFijos; }

    public double getGastosVariables() { return gastosVariables; }
    public void setGastosVariables(double gastosVariables) {
        this.gastosVariables = gastosVariables;
    }

    public double getGastosHormiga() { return gastosHormiga; }
    public void setGastosHormiga(double gastosHormiga) { this.gastosHormiga = gastosHormiga; }

    public double getGastoDiarioPromedio() { return gastoDiarioPromedio; }
    public void setDiasEnPeriodo(int dias) {
        this.diasEnPeriodo = dias;
        calcularGastoDiarioPromedio();
    }
}