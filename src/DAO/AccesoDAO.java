
package DAO;

import Database.Conexion;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class AccesoDAO {

    private Connection conexion() throws SQLException {
        Connection con = Conexion.getInstancia()
                .EstablecerConexion();

        if (con == null) {
            throw new SQLException(
                "No se pudo conectar con PostgreSQL.");
        }
        return con;
    }

    public record Cuenta(
        int id,
        String email,
        String estado
    ) {}

    public record Sesion(
        int idUsuario,
        String email,
        Set<String> roles
    ) {}

    public Cuenta buscarCuenta(String email)
            throws SQLException {

        String sql =
            "SELECT id_usuario,email,estado FROM usuario " +
            "WHERE LOWER(email)=LOWER(?)";

        try (Connection con = conexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                return new Cuenta(
                    rs.getInt("id_usuario"),
                    rs.getString("email"),
                    rs.getString("estado")
                );
            }
        }
    }

    public Set<String> obtenerRoles(int idUsuario)
            throws SQLException {

        Set<String> roles = new HashSet<>();

        String sql =
            "SELECT r.nombre FROM usuario_roles ur " +
            "JOIN roles r ON r.id=ur.id_rol " +
            "WHERE ur.id_usuario=? " +
            "AND ur.estado='ACTIVO' " +
            "AND r.estado='ACTIVO'";

        try (Connection con = conexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    roles.add(rs.getString("nombre"));
                }
            }
        }

        return roles;
    }

    public void crearSesion(
            int idUsuario,
            String tokenHash,
            LocalDateTime expiracion,
            String remitenteEmail
    ) throws SQLException {

        String cerrarSesiones =
            "UPDATE sesion_usuario SET estado='CERRADA' " +
            "WHERE LOWER(remitente_email)=LOWER(?) AND estado='ACTIVA'";
        String crearSesion =
            "INSERT INTO sesion_usuario " +
            "(id_usuario,token_hash,fecha_expiracion,remitente_email,estado) " +
            "VALUES (?,?,?,?,'ACTIVA')";

        try (Connection con = conexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement(cerrarSesiones)) {
                ps.setString(1, remitenteEmail);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(crearSesion)) {
                ps.setInt(1, idUsuario);
                ps.setString(2, tokenHash);
                ps.setTimestamp(3, Timestamp.valueOf(expiracion));
                ps.setString(4, remitenteEmail);
                ps.executeUpdate();
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            }
        } catch (SQLException ex) {
            throw ex;
        }
    }

    public Sesion validarSesionPorRemitente(String remitenteEmail)
            throws SQLException {
        String sql =
            "SELECT u.id_usuario,u.email,r.nombre AS rol " +
            "FROM sesion_usuario s " +
            "JOIN usuario u ON u.id_usuario=s.id_usuario " +
            "JOIN usuario_roles ur ON ur.id_usuario=u.id_usuario " +
            "JOIN roles r ON r.id=ur.id_rol " +
            "WHERE LOWER(s.remitente_email)=LOWER(?) " +
            "AND u.estado='ACTIVO' " +
            "AND s.estado='ACTIVA' " +
            "AND s.fecha_expiracion>CURRENT_TIMESTAMP " +
            "AND ur.estado='ACTIVO' " +
            "AND r.estado='ACTIVO'";

        try (Connection con = conexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, remitenteEmail);
            try (ResultSet rs = ps.executeQuery()) {
                return mapearSesion(rs);
            }
        }
    }

    public Sesion validarSesion(String tokenHash)
            throws SQLException {

        String sql =
            "SELECT u.id_usuario,u.email,r.nombre AS rol " +
            "FROM sesion_usuario s " +
            "JOIN usuario u ON u.id_usuario=s.id_usuario " +
            "JOIN usuario_roles ur ON ur.id_usuario=u.id_usuario " +
            "JOIN roles r ON r.id=ur.id_rol " +
            "WHERE s.token_hash=? " +
            "AND u.estado='ACTIVO' " +
            "AND s.estado='ACTIVA' " +
            "AND s.fecha_expiracion>CURRENT_TIMESTAMP " +
            "AND ur.estado='ACTIVO' " +
            "AND r.estado='ACTIVO'";

        try (Connection con = conexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tokenHash);

            try (ResultSet rs = ps.executeQuery()) {
                return mapearSesion(rs);
            }
        }
    }

    private Sesion mapearSesion(ResultSet rs) throws SQLException {
        int id = 0;
        String correo = null;
        Set<String> roles = new HashSet<>();

        while (rs.next()) {
            id = rs.getInt("id_usuario");
            correo = rs.getString("email");
            roles.add(rs.getString("rol"));
        }

        return id == 0 || roles.isEmpty()
                ? null
                : new Sesion(id, correo, roles);
    }

    public void actualizarUltimoUso(String hash)
            throws SQLException {

        String sql =
            "UPDATE sesion_usuario " +
            "SET fecha_ultimo_uso=CURRENT_TIMESTAMP " +
            "WHERE token_hash=? AND estado='ACTIVA'";

        try (Connection con = conexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.executeUpdate();
        }
    }

    public void actualizarUltimoUsoPorRemitente(String remitenteEmail)
            throws SQLException {
        String sql =
            "UPDATE sesion_usuario " +
            "SET fecha_ultimo_uso=CURRENT_TIMESTAMP " +
            "WHERE LOWER(remitente_email)=LOWER(?) AND estado='ACTIVA'";

        try (Connection con = conexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, remitenteEmail);
            ps.executeUpdate();
        }
    }

    public void cerrarSesion(String hash, int idUsuario)
            throws SQLException {

        String sql =
            "UPDATE sesion_usuario SET estado='CERRADA' " +
            "WHERE token_hash=? AND id_usuario=?";

        try (Connection con = conexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, hash);
            ps.setInt(2, idUsuario);
            ps.executeUpdate();
        }
    }

    public void cerrarSesionPorRemitente(String remitenteEmail)
            throws SQLException {
        String sql =
            "UPDATE sesion_usuario SET estado='CERRADA' " +
            "WHERE LOWER(remitente_email)=LOWER(?) AND estado='ACTIVA'";

        try (Connection con = conexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, remitenteEmail);
            ps.executeUpdate();
        }
    }

    public void cerrarSesionesUsuario(int idUsuario)
            throws SQLException {

        String sql =
            "UPDATE sesion_usuario SET estado='CERRADA' " +
            "WHERE id_usuario=? AND estado='ACTIVA'";

        try (Connection con = conexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.executeUpdate();
        }
    }

    public void limpiarSesionesExpiradas()
            throws SQLException {

        String sql =
            "UPDATE sesion_usuario SET estado='EXPIRADA' " +
            "WHERE estado='ACTIVA' " +
            "AND fecha_expiracion<=CURRENT_TIMESTAMP";

        try (Connection con = conexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}
