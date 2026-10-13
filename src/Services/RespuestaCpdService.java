package Services;

import DAO.HistorialSolicitudDAO;
import DAO.NotificacionDAO;
import DAO.OficioRespuestaCpdDAO;
import DAO.SolicitudDAO;
import Models.HistorialSolicitud;
import Models.Notificacion;
import Models.OficioRespuestaCpd;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class RespuestaCpdService {

    private OficioRespuestaCpdDAO respuestaDAO;
    private SolicitudDAO solicitudDAO;
    private HistorialSolicitudDAO historialDAO;
    private NotificacionDAO notificacionDAO;

    public RespuestaCpdService() {
        this.respuestaDAO = new OficioRespuestaCpdDAO();
        this.solicitudDAO = new SolicitudDAO();
        this.historialDAO = new HistorialSolicitudDAO();
        this.notificacionDAO = new NotificacionDAO();
    }

    public void registrarRespuestaCpd(List<String> params, String remitenteEmail) throws SQLException {
        if (params.size() < 5) {
            throw new IllegalArgumentException(
                    "Formato: respuesta_cpd registrar id_oficio,id_solicitud,resultado(CONFIRMADA_CPD|HABILITADA|OBSERVADA_CPD|RECHAZADA_CPD),referencia_respaldo,observaciones,id_responsable_cpd?"
            );
        }
        int idOficio = Integer.parseInt(params.get(0).trim());
        int idSol = Integer.parseInt(params.get(1).trim());
        String resultado = params.get(2).trim().toUpperCase();
        String respaldo = params.get(3).trim();
        String obs = params.get(4).trim();
        Integer idRespCpd = params.size() > 5 ? Integer.parseInt(params.get(5).trim()) : null;

        String codResp = "R-CPD-2026-" + String.format("%04d", (int) (System.currentTimeMillis() % 10000));
        Timestamp fechaHab = "HABILITADA".equalsIgnoreCase(resultado) ? new Timestamp(System.currentTimeMillis()) : null;

        OficioRespuestaCpd resp = new OficioRespuestaCpd();
        resp.setIdOficio(idOficio);
        resp.setIdSolicitud(idSol);
        resp.setCodigoRespuesta(codResp);
        resp.setResultado(resultado);
        resp.setObservaciones(obs);
        resp.setReferenciaRespaldo(respaldo);
        resp.setFechaHabilitacionEfectiva(fechaHab);
        resp.setIdResponsableCpd(idRespCpd);
        resp.setEstado("COMPLETADO");

        respuestaDAO.create(resp);

        // Actualizar solicitud
        String nuevoEstado;
        if ("HABILITADA".equalsIgnoreCase(resultado)) {
            nuevoEstado = "HABILITADA";
        } else if ("CONFIRMADA_CPD".equalsIgnoreCase(resultado)) {
            nuevoEstado = "CONFIRMADA_CPD";
        } else if ("OBSERVADA_CPD".equalsIgnoreCase(resultado)) {
            nuevoEstado = "OBSERVADA_CPD";
        } else {
            nuevoEstado = "RECHAZADA_CPD";
        }

        solicitudDAO.updateEstado(idSol, nuevoEstado, idRespCpd, obs, "RECHAZADA_CPD".equals(resultado) ? obs : null);

        // Historial (CU7)
        HistorialSolicitud hist = new HistorialSolicitud();
        hist.setIdSolicitud(idSol);
        hist.setIdUsuario(idRespCpd);
        hist.setEstadoAnterior("REMITIDA_A_CPD");
        hist.setEstadoNuevo(nuevoEstado);
        hist.setAccion("RESOLUCION_CPD");
        hist.setObservacion("CPD registro resolución: " + resultado + ". Respaldo: " + respaldo);
        historialDAO.create(hist);

        // Notificación al estudiante y a Dirección (CU7)
        Notificacion notif = new Notificacion();
        notif.setIdSolicitud(idSol);
        notif.setIdUsuario(idRespCpd);
        notif.setDestinatario(remitenteEmail != null ? remitenteEmail : "estudiante@uagrm.edu.bo");
        notif.setTipo("HABILITACION_CPD");
        notif.setAsunto("Resolucion CPD - Solicitud ID " + idSol + ": " + nuevoEstado);
        notif.setMensaje("El Centro de Procesamiento de Datos (CPD) ha registrado la siguiente resolución para su caso: " + nuevoEstado + ". Respaldo: " + respaldo);
        notificacionDAO.create(notif);
    }

    public List<String[]> listarRespuestas() throws SQLException {
        return respuestaDAO.listAll();
    }

    public String getComandosRespuesta() {
        return "Comandos CU6 - Confirmación y Habilitación de CPD:\n" +
                "- respuesta_cpd listar\n" +
                "- respuesta_cpd registrar id_oficio,id_solicitud,resultado,referencia_respaldo,observaciones,id_responsable_cpd";
    }
}
