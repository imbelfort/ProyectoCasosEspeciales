package DAO;

import Database.Conexion;
import Models.EvaluacionSolicitud;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EvaluacionSolicitudDAO {

    public record ResultadoResolucion(
            String estadoAnterior,
            int idEstudiante,
            String correoEstudiante,
            String codigoSolicitud
    ) {}

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

    public int obtenerEvaluadorId(String correo) throws SQLException {
        String sql = "SELECT u.id_usuario FROM usuario u " +
                     "JOIN usuario_roles ur ON ur.id_usuario = u.id_usuario " +
                     "JOIN roles r ON r.id = ur.id_rol " +
                     "WHERE LOWER(u.email) = LOWER(?) AND u.estado = 'ACTIVO' " +
                     "AND ur.estado = 'ACTIVO' AND r.estado = 'ACTIVO' " +
                     "AND r.nombre IN ('DIRECTOR', 'PERSONAL_ACADEMICO') " +
                     "ORDER BY CASE r.nombre WHEN 'DIRECTOR' THEN 0 ELSE 1 END LIMIT 1";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_usuario");
                }
            }
        }
        throw new IllegalArgumentException(
                "La cuenta remitente no tiene rol activo de Director o Personal Académico.");
    }

    public ResultadoResolucion registrarResolucion(
            EvaluacionSolicitud evaluacion,
            String nuevoEstado
    )
            throws SQLException {
        ResultadoResolucion resultado;
        try (Connection con = Conexion.getInstancia().EstablecerConexion()) {
            con.setAutoCommit(false);
            try {
                String estadoAnterior;
                int idEstudiante;
                String correoEstudiante;
                String codigoSolicitud;
                String selectSql = "SELECT s.estado, s.id_estudiante, u.email, s.codigo " +
                                   "FROM solicitud s " +
                                   "JOIN usuario u ON u.id_usuario = s.id_estudiante " +
                                   "WHERE s.id = ? FOR UPDATE OF s";
                try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                    ps.setInt(1, evaluacion.getIdSolicitud());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "No existe la solicitud con ID "
                                    + evaluacion.getIdSolicitud() + ".");
                        }
                        estadoAnterior = rs.getString("estado");
                        idEstudiante = rs.getInt("id_estudiante");
                        correoEstudiante = rs.getString("email");
                        codigoSolicitud = rs.getString("codigo");
                    }
                }
                resultado = new ResultadoResolucion(
                        estadoAnterior, idEstudiante, correoEstudiante, codigoSolicitud);

                if (!"ENVIADA".equalsIgnoreCase(estadoAnterior)
                        && !"EN_REVISION".equalsIgnoreCase(estadoAnterior)) {
                    throw new IllegalArgumentException(
                            "La solicitud ya fue procesada o no está pendiente de evaluación. "
                            + "Estado actual: " + estadoAnterior + ".");
                }

                String insertSql = "INSERT INTO evaluacion_solicitud " +
                        "(id_solicitud, id_evaluador, decision, justificacion, observaciones, fecha_evaluacion) " +
                        "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, evaluacion.getIdSolicitud());
                    ps.setInt(2, evaluacion.getIdEvaluador());
                    ps.setString(3, evaluacion.getDecision());
                    ps.setString(4, evaluacion.getJustificacion());
                    ps.setString(5, evaluacion.getObservaciones());
                    ps.executeUpdate();
                }

                String rechazo = "RECHAZADA".equals(evaluacion.getDecision())
                        ? evaluacion.getJustificacion() : null;
                String updateSql = "UPDATE solicitud SET estado = ?, id_revisor = ?, " +
                        "observaciones = ?, motivo_rechazo = ?, fecha_revision = CURRENT_TIMESTAMP " +
                        "WHERE id = ?";
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setString(1, nuevoEstado);
                    ps.setInt(2, evaluacion.getIdEvaluador());
                    ps.setString(3, evaluacion.getObservaciones());
                    ps.setString(4, rechazo);
                    ps.setInt(5, evaluacion.getIdSolicitud());
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo actualizar el estado de la solicitud.");
                    }
                }

                String observacionHistorial = "Evaluación " + evaluacion.getDecision()
                        + ". Motivo: " + evaluacion.getJustificacion();
                if (evaluacion.getObservaciones() != null
                        && !evaluacion.getObservaciones().isBlank()) {
                    observacionHistorial += ". Observaciones: "
                            + evaluacion.getObservaciones();
                }
                String historialSql = "INSERT INTO historial_solicitud " +
                        "(id_solicitud, id_usuario, estado_anterior, estado_nuevo, accion, observacion, fecha_evento) " +
                        "VALUES (?, ?, ?, ?, 'EVALUACION_DIRECCION', ?, CURRENT_TIMESTAMP)";
                try (PreparedStatement ps = con.prepareStatement(historialSql)) {
                    ps.setInt(1, evaluacion.getIdSolicitud());
                    ps.setInt(2, evaluacion.getIdEvaluador());
                    ps.setString(3, estadoAnterior);
                    ps.setString(4, nuevoEstado);
                    ps.setString(5, observacionHistorial);
                    ps.executeUpdate();
                }

                String notificacionSql = "INSERT INTO notificaciones " +
                        "(id_usuario, id_solicitud, destinatario, tipo, asunto, mensaje, canal, estado, intentos, ultimo_error, fecha_creacion) " +
                        "VALUES (?, ?, ?, 'RESULTADO_EVALUACION', ?, ?, 'EMAIL', 'PENDIENTE', 0, NULL, CURRENT_TIMESTAMP)";
                try (PreparedStatement ps = con.prepareStatement(notificacionSql)) {
                    ps.setInt(1, idEstudiante);
                    ps.setInt(2, evaluacion.getIdSolicitud());
                    ps.setString(3, correoEstudiante);
                    ps.setString(4, "Resultado de evaluación - Solicitud " + codigoSolicitud);
                    ps.setString(5, "Su solicitud " + codigoSolicitud + " fue "
                            + evaluacion.getDecision() + ". Estado: " + nuevoEstado
                            + ". Motivo: " + evaluacion.getJustificacion());
                    ps.executeUpdate();
                }

                con.commit();
            } catch (SQLException | RuntimeException ex) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    ex.addSuppressed(rollbackException);
                }
                throw ex;
            }
        }
        return resultado;
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

    public List<String[]> listBySolicitud(int idSolicitud) throws SQLException {
        String sql = "SELECT e.id, s.codigo, u.nombre || ' ' || u.apellidos AS evaluador, " +
                     "e.decision, e.justificacion, e.observaciones, e.fecha_evaluacion " +
                     "FROM evaluacion_solicitud e " +
                     "JOIN solicitud s ON e.id_solicitud = s.id " +
                     "JOIN usuario u ON e.id_evaluador = u.id_usuario " +
                     "WHERE e.id_solicitud = ? ORDER BY e.id DESC";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                            String.valueOf(rs.getInt("id")),
                            rs.getString("codigo"),
                            rs.getString("evaluador"),
                            rs.getString("decision"),
                            rs.getString("justificacion") != null
                                    ? rs.getString("justificacion") : "",
                            rs.getString("observaciones") != null
                                    ? rs.getString("observaciones") : "",
                            rs.getTimestamp("fecha_evaluacion") != null
                                    ? rs.getTimestamp("fecha_evaluacion").toString() : ""
                    });
                }
            }
        }
        return lista;
    }
}
