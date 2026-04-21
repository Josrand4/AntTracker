package com.example.antracker.data.model;

import java.util.Date;

public class Usuario {
    private String uid;
    private String nombre;
    private String email;
    private String fotoUrl;
    private Date fechaRegistro;
    private String monedaPreferida;

    // Constructor vacío requerido por Firestore
    public Usuario() {
        this.fechaRegistro = new Date();
        this.monedaPreferida = "MXN";
    }

    // Constructor completo
    public Usuario(String uid, String nombre, String email, String fotoUrl) {
        this.uid = uid;
        this.nombre = nombre;
        this.email = email;
        this.fotoUrl = fotoUrl;
        this.fechaRegistro = new Date();
        this.monedaPreferida = "MXN";
    }

    // Getters y Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Date getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Date fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public String getMonedaPreferida() { return monedaPreferida; }
    public void setMonedaPreferida(String monedaPreferida) { this.monedaPreferida = monedaPreferida; }
}
