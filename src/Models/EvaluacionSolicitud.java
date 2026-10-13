package Models;

import java.sql.Timestamp;

public class EvaluacionSolicitud {
    private int id;
    private int idSolicitud;
    private int idEvaluador; // FK Usuario(id_usuario)
    private String decision; // APROBADA, RECHAZADA, OBSERVADA
    private String justificacion;
    private String observaciones;
    private Timestamp fechaEvaluacion;

    public EvaluacionSolicitud() {
    }

    public EvaluacionSolicitud(int id, int idSolicitud, int idEvaluador, String decision,
                               String justificacion, String observaciones, Timestamp fechaEvaluacion) {
        this.id = id;
        this.idSolicitud = idSolicitud;
        this.idEvaluador = idEvaluador;
        this.decision = decision;
        this.justificacion = justificacion;
        this.observaciones = observaciones;
        this.fechaEvaluacion = fechaEvaluacion;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(int idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public int getIdEvaluador() {
        return idEvaluador;
    }

    public void setIdEvaluador(int idEvaluador) {
        this.idEvaluador = idEvaluador;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Timestamp getFechaEvaluacion() {
        return fechaEvaluacion;
    }

    public void setFechaEvaluacion(Timestamp fechaEvaluacion) {
        this.fechaEvaluacion = fechaEvaluacion;
    }
}
