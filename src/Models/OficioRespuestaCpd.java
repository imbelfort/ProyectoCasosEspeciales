package Models;

import java.sql.Timestamp;

public class OficioRespuestaCpd {
    private int id;
    private int idOficio; // FK OficioCpd
    private int idSolicitud; // FK Solicitud
    private String codigoRespuesta;
    private String resultado; // CONFIRMADA_CPD, HABILITADA, OBSERVADA_CPD, RECHAZADA_CPD
    private String observaciones;
    private String referenciaRespaldo;
    private Timestamp fechaRespuesta;
    private Timestamp fechaHabilitacionEfectiva;
    private Integer idResponsableCpd; // FK Usuario nullable
    private String estado;

    public OficioRespuestaCpd() {
        this.estado = "REGISTRADO";
    }

    public OficioRespuestaCpd(int id, int idOficio, int idSolicitud, String codigoRespuesta,
                              String resultado, String observaciones, String referenciaRespaldo,
                              Timestamp fechaRespuesta, Timestamp fechaHabilitacionEfectiva,
                              Integer idResponsableCpd, String estado) {
        this.id = id;
        this.idOficio = idOficio;
        this.idSolicitud = idSolicitud;
        this.codigoRespuesta = codigoRespuesta;
        this.resultado = resultado;
        this.observaciones = observaciones;
        this.referenciaRespaldo = referenciaRespaldo;
        this.fechaRespuesta = fechaRespuesta;
        this.fechaHabilitacionEfectiva = fechaHabilitacionEfectiva;
        this.idResponsableCpd = idResponsableCpd;
        this.estado = estado != null ? estado : "REGISTRADO";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdOficio() {
        return idOficio;
    }

    public void setIdOficio(int idOficio) {
        this.idOficio = idOficio;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getCodigoRespuesta() {
        return codigoRespuesta;
    }

    public void setCodigoRespuesta(String codigoRespuesta) {
        this.codigoRespuesta = codigoRespuesta;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getReferenciaRespaldo() {
        return referenciaRespaldo;
    }

    public void setReferenciaRespaldo(String referenciaRespaldo) {
        this.referenciaRespaldo = referenciaRespaldo;
    }

    public Timestamp getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(Timestamp fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public Timestamp getFechaHabilitacionEfectiva() {
        return fechaHabilitacionEfectiva;
    }

    public void setFechaHabilitacionEfectiva(Timestamp fechaHabilitacionEfectiva) {
        this.fechaHabilitacionEfectiva = fechaHabilitacionEfectiva;
    }

    public Integer getIdResponsableCpd() {
        return idResponsableCpd;
    }

    public void setIdResponsableCpd(Integer idResponsableCpd) {
        this.idResponsableCpd = idResponsableCpd;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
