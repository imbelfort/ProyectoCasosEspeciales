package Models;

import java.sql.Timestamp;

public class BloqueoGrupo {
    private int id;
    private int idGrupo;
    private Integer idSolicitud; // nullable
    private int cantidad;
    private String motivo;
    private String estado; // RESERVADO, LIBERADO, CONFIRMADO
    private Integer autorizadoPor; // FK Usuario(id_usuario) nullable
    private Timestamp fechaBloqueo;
    private Timestamp fechaResolucion;
    private String observacion;

    public BloqueoGrupo() {
        this.cantidad = 1;
        this.estado = "RESERVADO";
    }

    public BloqueoGrupo(int id, int idGrupo, Integer idSolicitud, int cantidad, String motivo,
                        String estado, Integer autorizadoPor, Timestamp fechaBloqueo,
                        Timestamp fechaResolucion, String observacion) {
        this.id = id;
        this.idGrupo = idGrupo;
        this.idSolicitud = idSolicitud;
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.estado = estado != null ? estado : "RESERVADO";
        this.autorizadoPor = autorizadoPor;
        this.fechaBloqueo = fechaBloqueo;
        this.fechaResolucion = fechaResolucion;
        this.observacion = observacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getAutorizadoPor() {
        return autorizadoPor;
    }

    public void setAutorizadoPor(Integer autorizadoPor) {
        this.autorizadoPor = autorizadoPor;
    }

    public Timestamp getFechaBloqueo() {
        return fechaBloqueo;
    }

    public void setFechaBloqueo(Timestamp fechaBloqueo) {
        this.fechaBloqueo = fechaBloqueo;
    }

    public Timestamp getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(Timestamp fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
}
