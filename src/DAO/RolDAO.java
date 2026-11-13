package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RolDAO {

    public void asignarRol(Connection con, int idUsuario, String nombreRol)
            throws SQLException {
        String sql = """
                INSERT INTO usuario_roles (id_usuario, id_rol, estado)
                SELECT ?, id, 'ACTIVO'
                FROM roles
                WHERE UPPER(nombre) = UPPER(?)
                  AND estado = 'ACTIVO'
                ON CONFLICT (id_usuario, id_rol)
                DO UPDATE SET estado = 'ACTIVO'
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, nombreRol);
            if (ps.executeUpdate() == 0) {
                throw new IllegalStateException(
                        "El rol " + nombreRol + " no esta configurado o activo.");
            }
        }
    }

    public boolean tieneRol(Connection con, int idUsuario, String nombreRol)
            throws SQLException {
        String sql = """
                SELECT 1
                FROM usuario_roles ur
                JOIN roles r ON r.id = ur.id_rol
                WHERE ur.id_usuario = ?
                  AND UPPER(r.nombre) = UPPER(?)
                  AND ur.estado = 'ACTIVO'
                  AND r.estado = 'ACTIVO'
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, nombreRol);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
