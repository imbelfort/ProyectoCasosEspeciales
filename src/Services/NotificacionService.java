package Services;

import DAO.NotificacionDAO;

import java.sql.SQLException;
import java.util.List;

public class NotificacionService {

    private NotificacionDAO dao;

    public NotificacionService() {
        this.dao = new NotificacionDAO();
    }

    public List<String[]> listarNotificaciones() throws SQLException {
        return dao.listAll();
    }

    public String getComandosNotificacion() {
        return "Comandos Notificaciones:\n" +
                "- notificacion listar";
    }
}
