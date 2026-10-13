package Models;

import java.sql.Timestamp;

public class HistorialSolicitud {
    private int id;
    private int idSolicitud;
    private Integer idUsuario; // FK Usuario nullable
    private String estadoAnterior;
    private String estadoNuevo;
    private String accion;
    private String observacion;
    private Timestamp fechaEvento;

    public HistorialSolicitud() {
    }

    public HistorialSolicitud(int id, int idSolicitud, Integer idUsuario, String estadoAnterior,
                              String estadoNuevo, String accion, String observacion, Timestamp fechaEvento) {
        this.id = id;
        this.idSolicitud = idSolicitud;
        this.idUsuario = idUsuario;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.accion = accion;
        this.observacion = observacion;
        this.fechaEvento = fechaEvento;
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

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEstadoAnterior() {
        return estadoAnterior;
    }

    public void setEstadoAnterior(String estadoAnterior) {
        this.estadoAnterior = estadoAnterior;
    }

    public String getEstadoNuevo() {
        return estadoNuevo;
    }

    public void setEstadoNuevo(String estadoNuevo) {
        this.estadoNuevo = estadoNuevo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public Timestamp getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(Timestamp fechaEvento) {
        this.fechaEvento = fechaEvento;
    }
}
