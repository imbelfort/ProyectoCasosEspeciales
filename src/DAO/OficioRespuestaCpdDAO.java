package DAO;

import Database.Conexion;
import Models.OficioRespuestaCpd;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OficioRespuestaCpdDAO {

    public void create(OficioRespuestaCpd resp) throws SQLException {
        String sql = "INSERT INTO oficio_respuesta_cpd (id_oficio, id_solicitud, codigo_respuesta, resultado, " +
                     "observaciones, referencia_respaldo, fecha_respuesta, fecha_habilitacion_efectiva, id_responsable_cpd, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?) RETURNING id";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, resp.getIdOficio());
            ps.setInt(2, resp.getIdSolicitud());
            ps.setString(3, resp.getCodigoRespuesta());
            ps.setString(4, resp.getResultado());
            ps.setString(5, resp.getObservaciones());
            ps.setString(6, resp.getReferenciaRespaldo());
            ps.setTimestamp(7, resp.getFechaHabilitacionEfectiva());
            if (resp.getIdResponsableCpd() != null) {
                ps.setInt(8, resp.getIdResponsableCpd());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setString(9, resp.getEstado() != null ? resp.getEstado() : "REGISTRADO");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resp.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT r.id, o.numero_oficio, s.codigo AS solicitud, r.codigo_respuesta, " +
                     "r.resultado, r.referencia_respaldo, r.fecha_respuesta, " +
                     "COALESCE(r.fecha_habilitacion_efectiva::text, 'NO APLICA') AS fecha_habilitacion, " +
                     "r.observaciones " +
                     "FROM oficio_respuesta_cpd r " +
                     "JOIN oficio_cpd o ON r.id_oficio = o.id " +
                     "JOIN solicitud s ON r.id_solicitud = s.id " +
                     "ORDER BY r.id DESC";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("numero_oficio"),
                        rs.getString("solicitud"),
                        rs.getString("codigo_respuesta"),
                        rs.getString("resultado"),
                        rs.getString("referencia_respaldo") != null ? rs.getString("referencia_respaldo") : "",
                        rs.getTimestamp("fecha_respuesta") != null ? rs.getTimestamp("fecha_respuesta").toString() : "",
                        rs.getString("fecha_habilitacion"),
                        rs.getString("observaciones") != null ? rs.getString("observaciones") : ""
                });
            }
        }
        return lista;
    }
}
