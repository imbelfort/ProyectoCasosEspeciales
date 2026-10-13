package DAO;

import Database.Conexion;
import Models.BloqueoGrupo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BloqueoGrupoDAO {

    public void create(BloqueoGrupo bg) throws SQLException {
        String sql = "INSERT INTO bloqueo_grupo (id_grupo, id_solicitud, cantidad, motivo, estado, autorizado_por, fecha_bloqueo, observacion) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?) RETURNING id";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bg.getIdGrupo());
            if (bg.getIdSolicitud() != null) {
                ps.setInt(2, bg.getIdSolicitud());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setInt(3, bg.getCantidad());
            ps.setString(4, bg.getMotivo());
            ps.setString(5, bg.getEstado() != null ? bg.getEstado() : "RESERVADO");
            if (bg.getAutorizadoPor() != null) {
                ps.setInt(6, bg.getAutorizadoPor());
            } else {
                ps.setNull(6, Types.INTEGER);
            }
            ps.setString(7, bg.getObservacion());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    bg.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT b.id, m.sigla AS materia, g.sigla_grupo, " +
                     "COALESCE(s.codigo, 'N/A') AS solicitud, b.cantidad, b.motivo, b.estado, " +
                     "COALESCE(u.nombre || ' ' || u.apellidos, 'SISTEMA') AS autorizado_por, " +
                     "b.fecha_bloqueo, b.fecha_resolucion " +
                     "FROM bloqueo_grupo b " +
                     "JOIN grupo g ON b.id_grupo = g.id " +
                     "JOIN materia m ON g.id_materia = m.id " +
                     "LEFT JOIN solicitud s ON b.id_solicitud = s.id " +
                     "LEFT JOIN usuario u ON b.autorizado_por = u.id_usuario " +
                     "ORDER BY b.id DESC";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("materia"),
                        rs.getString("sigla_grupo"),
                        rs.getString("solicitud"),
                        String.valueOf(rs.getInt("cantidad")),
                        rs.getString("motivo") != null ? rs.getString("motivo") : "",
                        rs.getString("estado") != null ? rs.getString("estado") : "",
                        rs.getString("autorizado_por"),
                        rs.getTimestamp("fecha_bloqueo") != null ? rs.getTimestamp("fecha_bloqueo").toString() : "",
                        rs.getTimestamp("fecha_resolucion") != null ? rs.getTimestamp("fecha_resolucion").toString() : "PENDIENTE"
                });
            }
        }
        return lista;
    }

    public void liberarBloqueo(int idBloqueo, String observacion) throws SQLException {
        String sql = "UPDATE bloqueo_grupo SET estado = 'LIBERADO', fecha_resolucion = CURRENT_TIMESTAMP, observacion = ? " +
                     "WHERE id = ?";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, observacion);
            ps.setInt(2, idBloqueo);
            ps.executeUpdate();
        }
    }

    public BloqueoGrupo readById(int idBloqueo) throws SQLException {
        String sql = "SELECT id, id_grupo, id_solicitud, cantidad, motivo, estado, autorizado_por, fecha_bloqueo, fecha_resolucion, observacion " +
                     "FROM bloqueo_grupo WHERE id = ?";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idBloqueo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BloqueoGrupo bg = new BloqueoGrupo();
                    bg.setId(rs.getInt("id"));
                    bg.setIdGrupo(rs.getInt("id_grupo"));
                    int solId = rs.getInt("id_solicitud");
                    if (!rs.wasNull()) bg.setIdSolicitud(solId);
                    bg.setCantidad(rs.getInt("cantidad"));
                    bg.setMotivo(rs.getString("motivo"));
                    bg.setEstado(rs.getString("estado"));
                    int autId = rs.getInt("autorizado_por");
                    if (!rs.wasNull()) bg.setAutorizadoPor(autId);
                    bg.setFechaBloqueo(rs.getTimestamp("fecha_bloqueo"));
                    bg.setFechaResolucion(rs.getTimestamp("fecha_resolucion"));
                    bg.setObservacion(rs.getString("observacion"));
                    return bg;
                }
            }
        }
        return null;
    }
}
