package DAO;

import Database.Conexion;
import Models.Solicitud;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SolicitudDAO {

    public void create(Solicitud sol) throws SQLException {
        String sql = "INSERT INTO solicitud (codigo, id_estudiante, id_periodo, tipo_caso, motivo, estado, " +
                     "fecha_solicitud, fecha_revision, id_revisor, motivo_rechazo, observaciones) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, NULL, NULL, NULL, ?) " +
                     "RETURNING id";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, sol.getCodigo());
            ps.setInt(2, sol.getIdEstudiante());
            ps.setInt(3, sol.getIdPeriodo());
            ps.setString(4, sol.getTipoCaso());
            ps.setString(5, sol.getMotivo());
            ps.setString(6, sol.getEstado() != null ? sol.getEstado() : "ENVIADA");
            ps.setString(7, sol.getObservaciones());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    sol.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT s.id, s.codigo, u.nombre || ' ' || u.apellidos AS estudiante, " +
                     "e.registro_universitario, p.gestion, s.tipo_caso, s.estado, " +
                     "s.fecha_solicitud, s.motivo " +
                     "FROM solicitud s " +
                     "JOIN estudiantes e ON s.id_estudiante = e.id_estudiante " +
                     "JOIN usuario u ON e.id_estudiante = u.id_usuario " +
                     "JOIN periodos p ON s.id_periodo = p.id " +
                     "ORDER BY s.id DESC";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("codigo") != null ? rs.getString("codigo") : "",
                        rs.getString("estudiante") != null ? rs.getString("estudiante") : "",
                        rs.getString("registro_universitario") != null ? rs.getString("registro_universitario") : "",
                        rs.getString("gestion") != null ? rs.getString("gestion") : "",
                        rs.getString("tipo_caso") != null ? rs.getString("tipo_caso") : "",
                        rs.getString("estado") != null ? rs.getString("estado") : "",
                        rs.getTimestamp("fecha_solicitud") != null ? rs.getTimestamp("fecha_solicitud").toString() : "",
                        rs.getString("motivo") != null ? rs.getString("motivo") : ""
                });
            }
        }
        return lista;
    }

    public String[] readById(int id) throws SQLException {
        String sql = "SELECT s.id, s.codigo, u.nombre || ' ' || u.apellidos AS estudiante, " +
                     "e.registro_universitario, p.gestion, s.tipo_caso, s.estado, " +
                     "s.fecha_solicitud, s.motivo, s.observaciones, s.motivo_rechazo " +
                     "FROM solicitud s " +
                     "JOIN estudiantes e ON s.id_estudiante = e.id_estudiante " +
                     "JOIN usuario u ON e.id_estudiante = u.id_usuario " +
                     "JOIN periodos p ON s.id_periodo = p.id " +
                     "WHERE s.id = ?";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                            String.valueOf(rs.getInt("id")),
                            rs.getString("codigo"),
                            rs.getString("estudiante"),
                            rs.getString("registro_universitario"),
                            rs.getString("gestion"),
                            rs.getString("tipo_caso"),
                            rs.getString("estado"),
                            rs.getTimestamp("fecha_solicitud") != null ? rs.getTimestamp("fecha_solicitud").toString() : "",
                            rs.getString("motivo") != null ? rs.getString("motivo") : "",
                            rs.getString("observaciones") != null ? rs.getString("observaciones") : "",
                            rs.getString("motivo_rechazo") != null ? rs.getString("motivo_rechazo") : ""
                    };
                }
            }
        }
        return null;
    }

    public void updateEstado(int id, String nuevoEstado, Integer idRevisor, String observaciones, String motivoRechazo) throws SQLException {
        String sql = "UPDATE solicitud SET estado = ?, id_revisor = ?, observaciones = ?, motivo_rechazo = ?, fecha_revision = CURRENT_TIMESTAMP " +
                     "WHERE id = ?";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            if (idRevisor != null) {
                ps.setInt(2, idRevisor);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, observaciones);
            ps.setString(4, motivoRechazo);
            ps.setInt(5, id);
            ps.executeUpdate();
        }
    }

    public String getEstado(int id) throws SQLException {
        String sql = "SELECT estado FROM solicitud WHERE id = ?";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("estado");
                }
            }
        }
        return null;
    }

    public List<String[]> listByEstudianteEmail(String email) throws SQLException {
        String sql = "SELECT s.id, s.codigo, u.nombre || ' ' || u.apellidos AS estudiante, " +
                     "e.registro_universitario, p.gestion, s.tipo_caso, s.estado, " +
                     "s.fecha_solicitud, s.motivo " +
                     "FROM solicitud s " +
                     "JOIN estudiantes e ON s.id_estudiante = e.id_estudiante " +
                     "JOIN usuario u ON e.id_estudiante = u.id_usuario " +
                     "JOIN periodos p ON s.id_periodo = p.id " +
                     "WHERE LOWER(u.email) = LOWER(?) " +
                     "ORDER BY s.id DESC";
        return ejecutarConsultaLista(sql, email);
    }

    public List<String[]> listPendientesByEstudianteEmail(String email) throws SQLException {
        String sql = "SELECT s.id, s.codigo, u.nombre || ' ' || u.apellidos AS estudiante, " +
                     "e.registro_universitario, p.gestion, s.tipo_caso, s.estado, " +
                     "s.fecha_solicitud, s.motivo " +
                     "FROM solicitud s " +
                     "JOIN estudiantes e ON s.id_estudiante = e.id_estudiante " +
                     "JOIN usuario u ON e.id_estudiante = u.id_usuario " +
                     "JOIN periodos p ON s.id_periodo = p.id " +
                     "WHERE LOWER(u.email) = LOWER(?) AND s.estado NOT IN ('HABILITADA', 'RECHAZADA') " +
                     "ORDER BY s.id DESC";
        return ejecutarConsultaLista(sql, email);
    }

    public List<String[]> listByRegistro(String registro) throws SQLException {
        String sql = "SELECT s.id, s.codigo, u.nombre || ' ' || u.apellidos AS estudiante, " +
                     "e.registro_universitario, p.gestion, s.tipo_caso, s.estado, " +
                     "s.fecha_solicitud, s.motivo " +
                     "FROM solicitud s " +
                     "JOIN estudiantes e ON s.id_estudiante = e.id_estudiante " +
                     "JOIN usuario u ON e.id_estudiante = u.id_usuario " +
                     "JOIN periodos p ON s.id_periodo = p.id " +
                     "WHERE LOWER(e.registro_universitario) = LOWER(?) " +
                     "ORDER BY s.id DESC";
        return ejecutarConsultaLista(sql, registro);
    }

    private List<String[]> ejecutarConsultaLista(String sql, String parametro) throws SQLException {
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, parametro.trim());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                            String.valueOf(rs.getInt("id")),
                            rs.getString("codigo") != null ? rs.getString("codigo") : "",
                            rs.getString("estudiante") != null ? rs.getString("estudiante") : "",
                            rs.getString("registro_universitario") != null ? rs.getString("registro_universitario") : "",
                            rs.getString("gestion") != null ? rs.getString("gestion") : "",
                            rs.getString("tipo_caso") != null ? rs.getString("tipo_caso") : "",
                            rs.getString("estado") != null ? rs.getString("estado") : "",
                            rs.getTimestamp("fecha_solicitud") != null ? rs.getTimestamp("fecha_solicitud").toString() : "",
                            rs.getString("motivo") != null ? rs.getString("motivo") : ""
                    });
                }
            }
        }
        return lista;
    }
}
