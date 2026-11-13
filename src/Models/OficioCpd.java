package Models;

import java.sql.Timestamp;

public class OficioCpd {
    private int id;
    private String numeroOficio; // e.g. "OF-CPD-2026-001"
    private int idSolicitud;
    private String destinatario; // e.g. "CPD Central"
    private String referencia;
    private String detalle;
    private int idResponsableRemision; // FK Usuario(id_usuario)
    private Timestamp fechaRemision;
    private String estado; // EMITIDO, RECIBIDO_CPD, CONTESTADO
    private String rutaDocumento;

    public OficioCpd() {
        this.estado = "EMITIDO";
    }

    public OficioCpd(int id, String numeroOficio, int idSolicitud, String destinatario,
                     String referencia, String detalle, int idResponsableRemision,
                     Timestamp fechaRemision, String estado) {
        this.id = id;
        this.numeroOficio = numeroOficio;
        this.idSolicitud = idSolicitud;
        this.destinatario = destinatario;
        this.referencia = referencia;
        this.detalle = detalle;
        this.idResponsableRemision = idResponsableRemision;
        this.fechaRemision = fechaRemision;
        this.estado = estado != null ? estado : "EMITIDO";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroOficio() {
        return numeroOficio;
    }

    public void setNumeroOficio(String numeroOficio) {
        this.numeroOficio = numeroOficio;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public int getIdResponsableRemision() {
        return idResponsableRemision;
    }

    public void setIdResponsableRemision(int idResponsableRemision) {
        this.idResponsableRemision = idResponsableRemision;
    }

    public Timestamp getFechaRemision() {
        return fechaRemision;
    }

    public void setFechaRemision(Timestamp fechaRemision) {
        this.fechaRemision = fechaRemision;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRutaDocumento() {
        return rutaDocumento;
    }

    public void setRutaDocumento(String rutaDocumento) {
        this.rutaDocumento = rutaDocumento;
    }
}
