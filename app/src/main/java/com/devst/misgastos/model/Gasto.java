package com.devst.misgastos.model;

public class Gasto {
    private String id;
    private String descripcion;
    private long monto;
    private String categoria;
    private String fecha;
    private String metodoPago;

    // IMPORTANTE: Constructor vacío requerido por Firestore para deserializar
    public Gasto() {}

    public Gasto(String id, String descripcion, long monto, String categoria, String fecha, String metodoPago) {
        this.id = id;
        this.descripcion = descripcion;
        this.monto = monto;
        this.categoria = categoria;
        this.fecha = fecha;
        this.metodoPago = metodoPago;
    }

    // --- Getters y Setters (Requeridos por Firestore) ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public long getMonto() {
        return monto;
    }

    public void setMonto(long monto) {
        this.monto = monto;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
}
