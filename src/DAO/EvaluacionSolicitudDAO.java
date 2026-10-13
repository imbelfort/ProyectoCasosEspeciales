package DAO;

import Database.Conexion;
import Models.EvaluacionSolicitud;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluacionSolicitudDAO {

    public void create(EvaluacionSolicitud ev) throws SQLException {
        String sql = "INSERT INTO evaluacion_solicitud (id_solicitud, id_evaluador, decision, justificacion, observaciones, fecha_evaluacion) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP) RETURNING id";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ev.getIdSolicitud());
            ps.setInt(2, ev.getIdEvaluador());
            ps.setString(3, ev.getDecision());
            ps.setString(4, ev.getJustificacion());
            ps.setString(5, ev.getObservaciones());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ev.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT e.id, s.codigo, u.nombre || ' ' || u.apellidos AS evaluador, " +
                     "e.decision, e.justificacion, e.observaciones, e.fecha_evaluacion " +
                     "FROM evaluacion_solicitud e " +
                     "JOIN solicitud s ON e.id_solicitud = s.id " +
                     "JOIN usuario u ON e.id_evaluador = u.id_usuario " +
                     "ORDER BY e.id DESC";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("codigo"),
                        rs.getString("evaluador"),
                        rs.getString("decision"),
                        rs.getString("justificacion") != null ? rs.getString("justificacion") : "",
                        rs.getString("observaciones") != null ? rs.getString("observaciones") : "",
                        rs.getTimestamp("fecha_evaluacion") != null ? rs.getTimestamp("fecha_evaluacion").toString() : ""
                });
            }
        }
        return lista;
    }
}
