package Models;

import java.sql.Timestamp;

public class Notificacion {
    private int id;
    private Integer idUsuario;
    private Integer idSolicitud;
    private String destinatario;
    private String tipo;
    private String asunto;
    private String mensaje;
    private String canal; // EMAIL
    private String estado; // PENDIENTE, ENVIADO, FALLIDO
    private int intentos;
    private String ultimoError;
    private Timestamp fechaCreacion;
    private Timestamp fechaEnvio;
    private Timestamp fechaLectura;

    public Notificacion() {
        this.canal = "EMAIL";
        this.estado = "PENDIENTE";
        this.intentos = 0;
    }

    public Notificacion(int id, Integer idUsuario, Integer idSolicitud, String destinatario,
                        String tipo, String asunto, String mensaje, String canal, String estado,
                        int intentos, String ultimoError, Timestamp fechaCreacion,
                        Timestamp fechaEnvio, Timestamp fechaLectura) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.idSolicitud = idSolicitud;
        this.destinatario = destinatario;
        this.tipo = tipo;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.canal = canal != null ? canal : "EMAIL";
        this.estado = estado != null ? estado : "PENDIENTE";
        this.intentos = intentos;
        this.ultimoError = ultimoError;
        this.fechaCreacion = fechaCreacion;
        this.fechaEnvio = fechaEnvio;
        this.fechaLectura = fechaLectura;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Integer idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getCanal() {
        return canal;
    }

    public void setCanal(String canal) {
        this.canal = canal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getIntentos() {
        return intentos;
    }

    public void setIntentos(int intentos) {
        this.intentos = intentos;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public void setUltimoError(String ultimoError) {
        this.ultimoError = ultimoError;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Timestamp getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(Timestamp fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public Timestamp getFechaLectura() {
        return fechaLectura;
    }

    public void setFechaLectura(Timestamp fechaLectura) {
        this.fechaLectura = fechaLectura;
    }
}
