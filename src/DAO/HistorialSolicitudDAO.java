package DAO;

import Database.Conexion;
import Models.HistorialSolicitud;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistorialSolicitudDAO {

    public void create(HistorialSolicitud hist) throws SQLException {
        String sql = "INSERT INTO historial_solicitud (id_solicitud, id_usuario, estado_anterior, estado_nuevo, accion, observacion, fecha_evento) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, hist.getIdSolicitud());
            if (hist.getIdUsuario() != null) {
                ps.setInt(2, hist.getIdUsuario());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, hist.getEstadoAnterior());
            ps.setString(4, hist.getEstadoNuevo());
            ps.setString(5, hist.getAccion());
            ps.setString(6, hist.getObservacion());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    hist.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<String[]> listBySolicitud(int idSolicitud) throws SQLException {
        String sql = "SELECT h.id, s.codigo, COALESCE(u.nombre || ' ' || u.apellidos, 'SISTEMA') AS usuario, " +
                     "h.estado_anterior, h.estado_nuevo, h.accion, h.observacion, h.fecha_evento " +
                     "FROM historial_solicitud h " +
                     "JOIN solicitud s ON h.id_solicitud = s.id " +
                     "LEFT JOIN usuario u ON h.id_usuario = u.id_usuario " +
                     "WHERE h.id_solicitud = ? " +
                     "ORDER BY h.id ASC";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                            String.valueOf(rs.getInt("id")),
                            rs.getString("codigo"),
                            rs.getString("usuario"),
                            rs.getString("estado_anterior") != null ? rs.getString("estado_anterior") : "INICIO",
                            rs.getString("estado_nuevo") != null ? rs.getString("estado_nuevo") : "",
                            rs.getString("accion") != null ? rs.getString("accion") : "",
                            rs.getString("observacion") != null ? rs.getString("observacion") : "",
                            rs.getTimestamp("fecha_evento") != null ? rs.getTimestamp("fecha_evento").toString() : ""
                    });
                }
            }
        }
        return lista;
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT h.id, s.codigo, COALESCE(u.nombre || ' ' || u.apellidos, 'SISTEMA') AS usuario, " +
                     "h.estado_anterior, h.estado_nuevo, h.accion, h.observacion, h.fecha_evento " +
                     "FROM historial_solicitud h " +
                     "JOIN solicitud s ON h.id_solicitud = s.id " +
                     "LEFT JOIN usuario u ON h.id_usuario = u.id_usuario " +
                     "ORDER BY h.id DESC";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("codigo"),
                        rs.getString("usuario"),
                        rs.getString("estado_anterior") != null ? rs.getString("estado_anterior") : "INICIO",
                        rs.getString("estado_nuevo") != null ? rs.getString("estado_nuevo") : "",
                        rs.getString("accion") != null ? rs.getString("accion") : "",
                        rs.getString("observacion") != null ? rs.getString("observacion") : "",
                        rs.getTimestamp("fecha_evento") != null ? rs.getTimestamp("fecha_evento").toString() : ""
                });
            }
        }
        return lista;
    }
}
