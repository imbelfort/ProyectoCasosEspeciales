package Models;

import java.sql.Timestamp;

public class ArchivoSolicitud {
    private int id;
    private int idSolicitud;
    private String nombreArchivo;
    private String tipoArchivo;
    private String rutaArchivo;
    private Timestamp fechaSubida;

    public ArchivoSolicitud() {
    }

    public ArchivoSolicitud(int id, int idSolicitud, String nombreArchivo, String tipoArchivo, String rutaArchivo, Timestamp fechaSubida) {
        this.id = id;
        this.idSolicitud = idSolicitud;
        this.nombreArchivo = nombreArchivo;
        this.tipoArchivo = tipoArchivo;
        this.rutaArchivo = rutaArchivo;
        this.fechaSubida = fechaSubida;
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

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getTipoArchivo() {
        return tipoArchivo;
    }

    public void setTipoArchivo(String tipoArchivo) {
        this.tipoArchivo = tipoArchivo;
    }

    public String getRutaArchivo() {
        return rutaArchivo;
    }

    public void setRutaArchivo(String rutaArchivo) {
        this.rutaArchivo = rutaArchivo;
    }

    public Timestamp getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(Timestamp fechaSubida) {
        this.fechaSubida = fechaSubida;
    }
}
