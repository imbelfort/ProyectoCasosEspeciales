package Models;

public class SolicitudGrupo {
    private int id;
    private int idSolicitud;
    private int idGrupo;
    private String motivo;
    private String estado; // PENDIENTE, APROBADO, RECHAZADO

    public SolicitudGrupo() {
        this.estado = "PENDIENTE";
    }

    public SolicitudGrupo(int id, int idSolicitud, int idGrupo, String motivo, String estado) {
        this.id = id;
        this.idSolicitud = idSolicitud;
        this.idGrupo = idGrupo;
        this.motivo = motivo;
        this.estado = estado != null ? estado : "PENDIENTE";
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

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
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
}
