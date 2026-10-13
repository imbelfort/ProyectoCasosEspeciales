package Models;

import java.sql.Date;

public class Periodo {
    private int id;
    private String gestion; // e.g. "1-2026", "2-2026"
    private Date fechaInicio;
    private Date fechaFin;
    private String estado; // ACTIVO, CERRADO

    public Periodo() {
        this.estado = "ACTIVO";
    }

    public Periodo(int id, String gestion, Date fechaInicio, Date fechaFin, String estado) {
        this.id = id;
        this.gestion = gestion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.estado = estado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGestion() {
        return gestion;
    }

    public void setGestion(String gestion) {
        this.gestion = gestion;
    }

    public Date getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(Date fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public Date getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(Date fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
