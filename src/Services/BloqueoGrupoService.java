package Services;

import DAO.BloqueoGrupoDAO;
import DAO.GrupoDAO;
import DAO.HistorialSolicitudDAO;
import Models.BloqueoGrupo;
import Models.HistorialSolicitud;

import java.sql.SQLException;
import java.util.List;

public class BloqueoGrupoService {

    private BloqueoGrupoDAO bloqueoDAO;
    private GrupoDAO grupoDAO;
    private HistorialSolicitudDAO historialDAO;

    public BloqueoGrupoService() {
        this.bloqueoDAO = new BloqueoGrupoDAO();
        this.grupoDAO = new GrupoDAO();
        this.historialDAO = new HistorialSolicitudDAO();
    }

    public void bloquearCupo(List<String> params) throws SQLException {
        if (params.size() < 4) {
            throw new IllegalArgumentException(
                    "Formato: bloqueo_grupo bloquear id_grupo,id_solicitud,cantidad,motivo,id_autorizador?"
            );
        }
        int idGrupo = Integer.parseInt(params.get(0).trim());
        int idSol = Integer.parseInt(params.get(1).trim());
        int cantidad = Integer.parseInt(params.get(2).trim());
        String motivo = params.get(3).trim();
        Integer idAutorizador = params.size() > 4 ? Integer.parseInt(params.get(4).trim()) : null;

        // Descontar de los cupos disponibles del grupo
        boolean exito = grupoDAO.descontarCupo(idGrupo, cantidad);
        if (!exito) {
            throw new SQLException("No hay cupos suficientes disponibles en el grupo " + idGrupo + " para realizar el bloqueo.");
        }

        BloqueoGrupo bg = new BloqueoGrupo();
        bg.setIdGrupo(idGrupo);
        bg.setIdSolicitud(idSol);
        bg.setCantidad(cantidad);
        bg.setMotivo(motivo);
        bg.setEstado("RESERVADO");
        bg.setAutorizadoPor(idAutorizador);
        bg.setObservacion("Cupo reservado por caso especial.");

        bloqueoDAO.create(bg);

        // Registrar en historial si hay solicitud
        HistorialSolicitud hist = new HistorialSolicitud();
        hist.setIdSolicitud(idSol);
        hist.setIdUsuario(idAutorizador);
        hist.setEstadoAnterior("APROBADA_DIRECCION");
        hist.setEstadoNuevo("CUPO_RESERVADO");
        hist.setAccion("BLOQUEO_CUPO");
        hist.setObservacion("Se reservaron " + cantidad + " cupos en el grupo " + idGrupo + " por: " + motivo);
        historialDAO.create(hist);
    }

    public void liberarCupo(List<String> params) throws SQLException {
        if (params.size() < 2) {
            throw new IllegalArgumentException("Formato: bloqueo_grupo liberar id_bloqueo,observacion");
        }
        int idBloqueo = Integer.parseInt(params.get(0).trim());
        String obs = params.get(1).trim();

        BloqueoGrupo bg = bloqueoDAO.readById(idBloqueo);
        if (bg == null) {
            throw new SQLException("El bloqueo con id " + idBloqueo + " no existe.");
        }

        if (!"RESERVADO".equalsIgnoreCase(bg.getEstado())) {
            throw new SQLException("El bloqueo ya no esta en estado RESERVADO (estado actual: " + bg.getEstado() + ").");
        }

        // Reintegrar cupos al grupo
        grupoDAO.liberarCupo(bg.getIdGrupo(), bg.getCantidad());
        bloqueoDAO.liberarBloqueo(idBloqueo, obs);

        if (bg.getIdSolicitud() != null) {
            HistorialSolicitud hist = new HistorialSolicitud();
            hist.setIdSolicitud(bg.getIdSolicitud());
            hist.setEstadoAnterior("CUPO_RESERVADO");
            hist.setEstadoNuevo("CUPO_LIBERADO");
            hist.setAccion("LIBERACION_CUPO");
            hist.setObservacion("Se liberaron los cupos bloqueados: " + obs);
            historialDAO.create(hist);
        }
    }

    public List<String[]> listarBloqueos() throws SQLException {
        return bloqueoDAO.listAll();
    }

    public String getComandosBloqueo() {
        return "Comandos CU4 - Bloqueo y Control de Cupos:\n" +
                "- bloqueo_grupo listar\n" +
                "- bloqueo_grupo bloquear id_grupo,id_solicitud,cantidad,motivo,id_autorizador\n" +
                "- bloqueo_grupo liberar id_bloqueo,observacion";
    }
}
