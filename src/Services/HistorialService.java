package Services;

import DAO.HistorialSolicitudDAO;

import java.sql.SQLException;
import java.util.List;

public class HistorialService {

    private HistorialSolicitudDAO dao;

    public HistorialService() {
        this.dao = new HistorialSolicitudDAO();
    }

    public List<String[]> consultarHistorialSolicitud(int idSolicitud) throws SQLException {
        return dao.listBySolicitud(idSolicitud);
    }

    public List<String[]> listarTodoElHistorial() throws SQLException {
        return dao.listAll();
    }

    public String getComandosHistorial() {
        return "Comandos CU7 - Historial y Trazabilidad:\n" +
                "- historial listar\n" +
                "- historial consultar id_solicitud";
    }
}
