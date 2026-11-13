package DAO;

import Database.Conexion;
import Models.Notificacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificacionDAO {

    public void create(Notificacion n) throws SQLException {
        String sql = "INSERT INTO notificaciones (id_usuario, id_solicitud, destinatario, tipo, asunto, mensaje, " +
                     "canal, estado, intentos, ultimo_error, fecha_creacion, fecha_envio) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?) RETURNING id";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (n.getIdUsuario() != null) ps.setInt(1, n.getIdUsuario()); else ps.setNull(1, Types.INTEGER);
            if (n.getIdSolicitud() != null) ps.setInt(2, n.getIdSolicitud()); else ps.setNull(2, Types.INTEGER);
            ps.setString(3, n.getDestinatario());
            ps.setString(4, n.getTipo());
            ps.setString(5, n.getAsunto());
            ps.setString(6, n.getMensaje());
            ps.setString(7, n.getCanal() != null ? n.getCanal() : "EMAIL");
            ps.setString(8, n.getEstado() != null ? n.getEstado() : "ENVIADO");
            ps.setInt(9, n.getIntentos());
            ps.setString(10, n.getUltimoError());
            ps.setTimestamp(11, n.getFechaEnvio() != null ? n.getFechaEnvio() : new Timestamp(System.currentTimeMillis()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    n.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT id, COALESCE(id_solicitud::text, 'N/A') AS solicitud, destinatario, tipo, asunto, " +
                     "estado, intentos, fecha_creacion, COALESCE(fecha_envio::text, 'PENDIENTE') AS fecha_envio " +
                     "FROM notificaciones ORDER BY id DESC";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("solicitud"),
                        rs.getString("destinatario"),
                        rs.getString("tipo") != null ? rs.getString("tipo") : "",
                        rs.getString("asunto") != null ? rs.getString("asunto") : "",
                        rs.getString("estado") != null ? rs.getString("estado") : "",
                        String.valueOf(rs.getInt("intentos")),
                        rs.getTimestamp("fecha_creacion") != null ? rs.getTimestamp("fecha_creacion").toString() : "",
                        rs.getString("fecha_envio")
                });
            }
        }
        return lista;
    }

    public void registrarResultadoEnvio(
            int idSolicitud,
            String tipo,
            boolean enviado,
            String error
    ) throws SQLException {
        String sql = "UPDATE notificaciones SET estado = ?, " +
                "intentos = intentos + 1, ultimo_error = ?, " +
                "fecha_envio = CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE fecha_envio END " +
                "WHERE id_solicitud = ? AND tipo = ? " +
                "AND id = (SELECT MAX(id) FROM notificaciones " +
                "WHERE id_solicitud = ? AND tipo = ?)";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, enviado ? "ENVIADO" : "PENDIENTE");
            ps.setString(2, enviado ? null : error);
            ps.setBoolean(3, enviado);
            ps.setInt(4, idSolicitud);
            ps.setString(5, tipo);
            ps.setInt(6, idSolicitud);
            ps.setString(7, tipo);
            ps.executeUpdate();
        }
    }

    public void registrarResultadoEnvio(
            long idNotificacion,
            boolean enviado,
            String error
    ) throws SQLException {
        String sql = "UPDATE notificaciones SET estado = ?, intentos = intentos + 1, " +
                "ultimo_error = ?, fecha_envio = CASE WHEN ? " +
                "THEN CURRENT_TIMESTAMP ELSE fecha_envio END WHERE id = ?";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, enviado ? "ENVIADO" : "PENDIENTE");
            ps.setString(2, enviado ? null : error);
            ps.setBoolean(3, enviado);
            ps.setLong(4, idNotificacion);
            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "No se encontró la notificación " + idNotificacion
                        + " para guardar el resultado del envío.");
            }
        }
    }
}
