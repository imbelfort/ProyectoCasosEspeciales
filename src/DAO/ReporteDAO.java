package DAO;

import Database.Conexion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

    public List<String[]> reporteGeneral() throws SQLException {
        String sql = "SELECT " +
                     "COUNT(*) AS total_solicitudes, " +
                     "COUNT(CASE WHEN estado IN ('ENVIADA', 'EN_REVISION') THEN 1 END) AS pendientes_evaluacion, " +
                     "COUNT(CASE WHEN estado = 'APROBADA_DIRECCION' THEN 1 END) AS aprobadas_direccion, " +
                     "COUNT(CASE WHEN estado = 'RECHAZADA_DIRECCION' THEN 1 END) AS rechazadas_direccion, " +
                     "COUNT(CASE WHEN estado = 'REMITIDA_A_CPD' THEN 1 END) AS remitidas_cpd, " +
                     "COUNT(CASE WHEN estado = 'HABILITADA' THEN 1 END) AS habilitadas_cpd, " +
                     "COUNT(CASE WHEN estado LIKE '%OBSERVADA%' THEN 1 END) AS observadas " +
                     "FROM solicitud";

        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("total_solicitudes")),
                        String.valueOf(rs.getInt("pendientes_evaluacion")),
                        String.valueOf(rs.getInt("aprobadas_direccion")),
                        String.valueOf(rs.getInt("rechazadas_direccion")),
                        String.valueOf(rs.getInt("remitidas_cpd")),
                        String.valueOf(rs.getInt("habilitadas_cpd")),
                        String.valueOf(rs.getInt("observadas"))
                });
            }
        }
        return lista;
    }

    public List<String[]> reportePorEstado() throws SQLException {
        String sql = "SELECT estado, COUNT(*) AS cantidad, " +
                     "ROUND(COUNT(*) * 100.0 / (SELECT GREATEST(COUNT(*), 1) FROM solicitud), 2) AS porcentaje " +
                     "FROM solicitud " +
                     "GROUP BY estado " +
                     "ORDER BY cantidad DESC";

        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        rs.getString("estado"),
                        String.valueOf(rs.getInt("cantidad")),
                        rs.getString("porcentaje") + "%"
                });
            }
        }
        return lista;
    }

    public List<String[]> reportePorPeriodo() throws SQLException {
        String sql = "SELECT p.gestion, c.nombre AS carrera, COUNT(s.id) AS total_solicitudes, " +
                     "COUNT(CASE WHEN s.estado = 'HABILITADA' THEN 1 END) AS habilitadas " +
                     "FROM periodos p " +
                     "LEFT JOIN solicitud s ON s.id_periodo = p.id " +
                     "LEFT JOIN estudiantes e ON s.id_estudiante = e.id_estudiante " +
                     "LEFT JOIN carrera c ON e.carrera_id = c.id " +
                     "GROUP BY p.gestion, c.nombre " +
                     "ORDER BY p.gestion DESC, total_solicitudes DESC";

        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        rs.getString("gestion") != null ? rs.getString("gestion") : "N/A",
                        rs.getString("carrera") != null ? rs.getString("carrera") : "GENERAL",
                        String.valueOf(rs.getInt("total_solicitudes")),
                        String.valueOf(rs.getInt("habilitadas"))
                });
            }
        }
        return lista;
    }

    public List<String[]> reporteCupos() throws SQLException {
        String sql = "SELECT m.sigla, m.nombre AS materia, g.sigla_grupo, " +
                     "g.cupo_maximo, g.cupos_disponibles, " +
                     "COALESCE(SUM(CASE WHEN b.estado = 'RESERVADO' THEN b.cantidad ELSE 0 END), 0) AS cupos_reservados, " +
                     "g.estado " +
                     "FROM grupo g " +
                     "JOIN materia m ON g.id_materia = m.id " +
                     "LEFT JOIN bloqueo_grupo b ON b.id_grupo = g.id " +
                     "GROUP BY m.sigla, m.nombre, g.sigla_grupo, g.cupo_maximo, g.cupos_disponibles, g.estado " +
                     "ORDER BY m.sigla, g.sigla_grupo";

        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        rs.getString("sigla"),
                        rs.getString("materia"),
                        rs.getString("sigla_grupo"),
                        String.valueOf(rs.getInt("cupo_maximo")),
                        String.valueOf(rs.getInt("cupos_disponibles")),
                        String.valueOf(rs.getInt("cupos_reservados")),
                        rs.getString("estado")
                });
            }
        }
        return lista;
    }

    public void registrarReporte(int idUsuario, String tipo, String parametros) throws SQLException {
        String sql = "INSERT INTO reporte (id_usuario, tipo_reporte, parametros, fecha_generacion) " +
                     "VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, tipo);
            ps.setString(3, parametros);
            ps.executeUpdate();
        }
    }
}