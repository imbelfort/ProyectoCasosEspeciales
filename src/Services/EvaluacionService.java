package Services;

import DAO.EvaluacionSolicitudDAO;
import DAO.NotificacionDAO;
import Models.EvaluacionSolicitud;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

public class EvaluacionService {

    private final EvaluacionSolicitudDAO evaluacionDAO;
    private final NotificacionDAO notificacionDAO;

    public record ResultadoEvaluacion(
            int idSolicitud,
            String codigoSolicitud,
            String nuevoEstado,
            String decision,
            String motivo,
            String correoEstudiante
    ) {}

    public EvaluacionService() {
        this.evaluacionDAO = new EvaluacionSolicitudDAO();
        this.notificacionDAO = new NotificacionDAO();
    }

    public ResultadoEvaluacion evaluarSolicitud(
            List<String> params,
            int idEvaluador
    )
            throws SQLException {
        if (params == null || params.size() < 3) {
            throw new IllegalArgumentException(
                    "Formato: evaluacion registrar id_solicitud,decision(APROBADA|RECHAZADA),motivo[,observaciones].");
        }

        int idSolicitud;
        try {
            idSolicitud = Integer.parseInt(params.get(0).trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "El ID de la solicitud debe ser un número entero.", ex);
        }

        int indiceDecision = 1;
        if (params.size() >= 4
                && esNumero(params.get(1))
                && esDecision(params.get(2))) {
            indiceDecision = 2;
        }

        String decision = normalizarDecision(params.get(indiceDecision));
        if (!"APROBADA".equals(decision) && !"RECHAZADA".equals(decision)
                && !"OBSERVADA".equals(decision)) {
            throw new IllegalArgumentException(
                    "Decisión inválida. Use APROBADA, RECHAZADA u OBSERVADA.");
        }

        int indiceMotivo = indiceDecision + 1;
        if (params.size() <= indiceMotivo
                || params.get(indiceMotivo).trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El motivo de la decisión es obligatorio.");
        }

        String motivo;
        String observaciones = "";
        if (params.size() == indiceMotivo + 2) {
            motivo = params.get(indiceMotivo).trim();
            observaciones = params.get(indiceMotivo + 1).trim();
        } else {
            motivo = String.join(", ",
                    params.subList(indiceMotivo, params.size())).trim();
        }

        String nuevoEstado = "APROBADA".equals(decision)
                ? "APROBADA_DIRECCION"
                : "RECHAZADA".equals(decision) ? "RECHAZADA_DIRECCION" : "OBSERVADA";
        EvaluacionSolicitud evaluacion = new EvaluacionSolicitud(
                0, idSolicitud, idEvaluador, decision, motivo, observaciones, null);
        EvaluacionSolicitudDAO.ResultadoResolucion resultado =
                evaluacionDAO.registrarResolucion(evaluacion, nuevoEstado);
        return new ResultadoEvaluacion(
                idSolicitud,
                resultado.codigoSolicitud(),
                nuevoEstado,
                decision,
                motivo,
                resultado.correoEstudiante());
    }

    public void registrarResultadoNotificacion(
            int idSolicitud,
            boolean enviado,
            String error
    ) throws SQLException {
        notificacionDAO.registrarResultadoEnvio(
                idSolicitud, "RESULTADO_EVALUACION", enviado, error);
    }

    public List<String[]> listarEvaluaciones() throws SQLException {
        return evaluacionDAO.listAll();
    }

    public List<String[]> listarEvaluacionesSolicitud(int idSolicitud)
            throws SQLException {
        return evaluacionDAO.listBySolicitud(idSolicitud);
    }

    public String getComandosEvaluacion() {
        return "Comandos CU3 - Evaluación por Dirección de Carrera:\n" +
                "- evaluacion listar\n" +
                "- solicitud periodos id_periodo_o_gestion\n" +
                "- solicitud consultar id_solicitud\n" +
                "- evaluacion registrar id_solicitud,APROBADA|RECHAZADA|OBSERVADA,motivo[,observaciones]";
    }

    private boolean esNumero(String valor) {
        try {
            Integer.parseInt(valor.trim());
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean esDecision(String valor) {
        String decision = normalizarDecision(valor);
        return "APROBADA".equals(decision) || "RECHAZADA".equals(decision)
                || "OBSERVADA".equals(decision);
    }

    private String normalizarDecision(String valor) {
        String decision = valor.trim().toUpperCase(Locale.ROOT);
        return switch (decision) {
            case "ACEPTADO", "ACEPTADA" -> "APROBADA";
            case "RECHAZADO" -> "RECHAZADA";
            default -> decision;
        };
    }
}
