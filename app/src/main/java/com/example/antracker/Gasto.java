package com.example.antracker;

public class Gasto {
    public class Gasto {
        private String descripcion;
        private double monto;
        private String fecha;

        public Gasto(String descripcion, double monto, String fecha) {
            this.descripcion = descripcion;
            this.monto = monto;
            this.fecha = fecha;
        }

        public String getDescripcion() { return descripcion; }
        public double getMonto() { return monto; }
        public String getFecha() { return fecha; }
    }
}
