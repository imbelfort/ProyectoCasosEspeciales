package Models;

import java.sql.Timestamp;

public class Solicitud {
    private int id;
    private String codigo; // e.g. "SOL-2026-001"
    private int idEstudiante;
    private int idPeriodo;
    private String tipoCaso; // LEVANTAMIENTO_PREREQUISITO, EXCESO_MATERIAS, PARALELO_LLENO, NIVELACION, CASO_ESPECIAL
    private String motivo;
    private String estado; // BORRADOR, ENVIADA, EN_REVISION, OBSERVADA, PENDIENTE_CORRECCION, APROBADA_DIRECCION, RECHAZADA_DIRECCION, CUPO_RESERVADO, PENDIENTE_REMISION_CPD, REMITIDA_A_CPD, RECIBIDA_CPD, OBSERVADA_CPD, CONFIRMADA_CPD, HABILITADA, RECHAZADA_CPD, CANCELADA
    private Timestamp fechaSolicitud;
    private Timestamp fechaRevision;
    private Integer idRevisor; // FK Usuario(id_usuario) nullable
    private String motivoRechazo;
    private String observaciones;

    public Solicitud() {
        this.estado = "ENVIADA";
    }

    public Solicitud(int id, String codigo, int idEstudiante, int idPeriodo, String tipoCaso,
                     String motivo, String estado, Timestamp fechaSolicitud, Timestamp fechaRevision,
                     Integer idRevisor, String motivoRechazo, String observaciones) {
        this.id = id;
        this.codigo = codigo;
        this.idEstudiante = idEstudiante;
        this.idPeriodo = idPeriodo;
        this.tipoCaso = tipoCaso;
        this.motivo = motivo;
        this.estado = estado != null ? estado : "ENVIADA";
        this.fechaSolicitud = fechaSolicitud;
        this.fechaRevision = fechaRevision;
        this.idRevisor = idRevisor;
        this.motivoRechazo = motivoRechazo;
        this.observaciones = observaciones;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public int getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(int idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public String getTipoCaso() {
        return tipoCaso;
    }

    public void setTipoCaso(String tipoCaso) {
        this.tipoCaso = tipoCaso;
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

    public Timestamp getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(Timestamp fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public Timestamp getFechaRevision() {
        return fechaRevision;
    }

    public void setFechaRevision(Timestamp fechaRevision) {
        this.fechaRevision = fechaRevision;
    }

    public Integer getIdRevisor() {
        return idRevisor;
    }

    public void setIdRevisor(Integer idRevisor) {
        this.idRevisor = idRevisor;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
