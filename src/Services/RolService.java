package Services;

import DAO.RolDAO;

import java.sql.Connection;
import java.sql.SQLException;

public class RolService {

    private final RolDAO rolDAO = new RolDAO();

    public void asignarRolAutomaticamente(
            Connection con,
            int idUsuario,
            String nombreRol
    ) throws SQLException {
        rolDAO.asignarRol(con, idUsuario, nombreRol);
    }

    public boolean tieneRol(
            Connection con,
            int idUsuario,
            String nombreRol
    ) throws SQLException {
        return rolDAO.tieneRol(con, idUsuario, nombreRol);
    }
}
