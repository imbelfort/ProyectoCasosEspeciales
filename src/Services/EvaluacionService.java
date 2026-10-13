package Services;

import DAO.EvaluacionSolicitudDAO;
import DAO.HistorialSolicitudDAO;
import DAO.NotificacionDAO;
import DAO.SolicitudDAO;
import Models.EvaluacionSolicitud;
import Models.HistorialSolicitud;
import Models.Notificacion;

import java.sql.SQLException;
import java.util.List;

public class EvaluacionService {

    private EvaluacionSolicitudDAO evaluacionDAO;
    private SolicitudDAO solicitudDAO;
    private HistorialSolicitudDAO historialDAO;
    private NotificacionDAO notificacionDAO;

    public EvaluacionService() {
        this.evaluacionDAO = new EvaluacionSolicitudDAO();
        this.solicitudDAO = new SolicitudDAO();
        this.historialDAO = new HistorialSolicitudDAO();
        this.notificacionDAO = new NotificacionDAO();
    }

    public void evaluarSolicitud(List<String> params, String remitenteEmail) throws SQLException {
        if (params.size() < 4) {
            throw new IllegalArgumentException(
                    "Formato: evaluacion registrar id_solicitud,id_evaluador,decision(APROBADA|RECHAZADA|OBSERVADA),justificacion,observaciones?"
            );
        }
        int idSol = Integer.parseInt(params.get(0).trim());
        int idEvaluador = Integer.parseInt(params.get(1).trim());
        String decision = params.get(2).trim().toUpperCase();
        String justificacion = params.get(3).trim();
        String observaciones = params.size() > 4 ? params.get(4).trim() : "";

        String estadoAnterior = solicitudDAO.getEstado(idSol);
        if (estadoAnterior == null) {
            throw new SQLException("La solicitud con id " + idSol + " no existe.");
        }

        String nuevoEstado;
        if ("APROBADA".equalsIgnoreCase(decision)) {
            nuevoEstado = "APROBADA_DIRECCION";
        } else if ("RECHAZADA".equalsIgnoreCase(decision)) {
            nuevoEstado = "RECHAZADA_DIRECCION";
        } else if ("OBSERVADA".equalsIgnoreCase(decision)) {
            nuevoEstado = "OBSERVADA";
        } else {
            throw new IllegalArgumentException("Decision invalida. Use APROBADA, RECHAZADA u OBSERVADA.");
        }

        // Registrar evaluación
        EvaluacionSolicitud ev = new EvaluacionSolicitud(0, idSol, idEvaluador, decision, justificacion, observaciones, null);
        evaluacionDAO.create(ev);

        // Actualizar solicitud
        solicitudDAO.updateEstado(idSol, nuevoEstado, idEvaluador, observaciones, "RECHAZADA".equals(decision) ? justificacion : null);

        // Registrar historial (CU7)
        HistorialSolicitud hist = new HistorialSolicitud();
        hist.setIdSolicitud(idSol);
        hist.setIdUsuario(idEvaluador);
        hist.setEstadoAnterior(estadoAnterior);
        hist.setEstadoNuevo(nuevoEstado);
        hist.setAccion("EVALUACION_DIRECCION");
        hist.setObservacion("Dirección de Carrera resolvió: " + decision + ". Justificación: " + justificacion);
        historialDAO.create(hist);

        // Notificación al estudiante (CU7)
        Notificacion notif = new Notificacion();
        notif.setIdSolicitud(idSol);
        notif.setIdUsuario(idEvaluador);
        notif.setDestinatario(remitenteEmail != null ? remitenteEmail : "estudiante@uagrm.edu.bo");
        notif.setTipo("RESULTADO_EVALUACION");
        notif.setAsunto("Evaluacion Direccion de Carrera - Solicitud ID " + idSol);
        notif.setMensaje("Su solicitud fue evaluada con estado " + nuevoEstado + ". Justificacion: " + justificacion);
        notificacionDAO.create(notif);
    }

    public List<String[]> listarEvaluaciones() throws SQLException {
        return evaluacionDAO.listAll();
    }

    public String getComandosEvaluacion() {
        return "Comandos CU3 - Evaluación por Dirección de Carrera:\n" +
                "- evaluacion listar\n" +
                "- evaluacion registrar id_solicitud,id_evaluador,decision,justificacion,observaciones";
    }
}
