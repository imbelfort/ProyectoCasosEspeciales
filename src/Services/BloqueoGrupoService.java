package Services;

import DAO.BloqueoGrupoDAO;
import Models.BloqueoGrupo;

import java.sql.SQLException;
import java.util.List;

public class BloqueoGrupoService {

    private final BloqueoGrupoDAO bloqueoDAO;

    public BloqueoGrupoService() {
        this.bloqueoDAO = new BloqueoGrupoDAO();
    }

    public BloqueoGrupo bloquearCupo(List<String> params, int idAutorizador)
            throws SQLException {
        if (params == null || params.size() < 4) {
            throw new IllegalArgumentException(
                    "Formato: bloqueo_grupo bloquear id_grupo,id_solicitud,cantidad,motivo. "
                    + "El autorizador se identifica con la sesión activa.");
        }

        int idGrupo = parsePositiveInt(params.get(0), "ID del grupo");
        int idSolicitud = parsePositiveInt(params.get(1), "ID de la solicitud");
        int cantidad = parsePositiveInt(params.get(2), "cantidad de cupos");

        int endMotivo = params.size();
        if (params.size() >= 5 && esNumero(params.get(params.size() - 1))) {
            endMotivo--;
        }
        String motivo = String.join(", ", params.subList(3, endMotivo)).trim();
        if (motivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe indicar el motivo del bloqueo.");
        }

        BloqueoGrupo bloqueo = new BloqueoGrupo();
        bloqueo.setIdGrupo(idGrupo);
        bloqueo.setIdSolicitud(idSolicitud);
        bloqueo.setCantidad(cantidad);
        bloqueo.setMotivo(motivo);
        bloqueo.setEstado("RESERVADO");
        bloqueo.setAutorizadoPor(idAutorizador);
        bloqueo.setObservacion("Cupo reservado por caso especial.");
        bloqueoDAO.bloquearCupo(bloqueo);
        return bloqueo;
    }

    public BloqueoGrupo liberarCupo(List<String> params, int idAutorizador)
            throws SQLException {
        if (params == null || params.size() < 2) {
            throw new IllegalArgumentException(
                    "Formato: bloqueo_grupo liberar id_bloqueo,observacion.");
        }

        int idBloqueo = parsePositiveInt(params.get(0), "ID del bloqueo");
        String observacion = String.join(", ", params.subList(1, params.size())).trim();
        if (observacion.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe indicar el motivo u observación de la liberación.");
        }

        return bloqueoDAO.liberarCupo(idBloqueo, observacion, idAutorizador);
    }

    public List<String[]> listarBloqueos() throws SQLException {
        return bloqueoDAO.listAll();
    }

    public String getComandosBloqueo() {
        return "Comandos CU4 - Bloqueo y Control de Cupos:\n" +
                "- bloqueo_grupo listar\n" +
                "- bloqueo_grupo bloquear id_grupo,id_solicitud,cantidad,motivo\n" +
                "- bloqueo_grupo liberar id_bloqueo,observacion\n" +
                "El director o académico se identifica mediante la sesión activa.";
    }

    private int parsePositiveInt(String valor, String nombre) {
        int resultado;
        try {
            resultado = Integer.parseInt(valor.trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(nombre + " debe ser un número entero.", ex);
        }
        if (resultado <= 0) {
            throw new IllegalArgumentException(nombre + " debe ser mayor que cero.");
        }
        return resultado;
    }

    private boolean esNumero(String valor) {
        try {
            Integer.parseInt(valor.trim());
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}
