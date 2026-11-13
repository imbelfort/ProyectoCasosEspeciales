package DAO;

import Database.Conexion;
import Models.OficioRespuestaCpd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class OficioRespuestaCpdDAO {

    public record DestinatarioNotificacion(
            long idNotificacion,
            int idUsuario,
            String email
    ) {}

    public record ResultadoRespuesta(
            int idSolicitud,
            String codigoSolicitud,
            String estadoNuevo,
            String periodo,
            String numeroOficio,
            List<DestinatarioNotificacion> destinatarios
    ) {}

    public ResultadoRespuesta registrarRespuesta(
            OficioRespuestaCpd respuesta
    ) throws SQLException {
        try (Connection con = Conexion.getInstancia().EstablecerConexion()) {
            con.setAutoCommit(false);
            try {
                SolicitudOficio solicitud = obtenerSolicitudOficio(con, respuesta);
                if (!"REMITIDA_A_CPD".equalsIgnoreCase(solicitud.estado())) {
                    throw new IllegalArgumentException(
                            "La solicitud debe estar remitida a CPD y no tener respuesta previa. "
                            + "Estado actual: " + solicitud.estado() + ".");
                }

                actualizarSolicitud(con, respuesta);
                insertarRespuesta(con, respuesta);
                insertarHistorial(con, respuesta, solicitud.estado());

                List<DestinatarioNotificacion> destinatarios =
                        obtenerDestinatarios(con, solicitud, respuesta);
                con.commit();
                return new ResultadoRespuesta(
                        respuesta.getIdSolicitud(),
                        solicitud.codigoSolicitud(),
                        respuesta.getEstado(),
                        solicitud.periodo(),
                        solicitud.numeroOficio(),
                        destinatarios);
            } catch (SQLException | RuntimeException ex) {
                try {
                    con.rollback();
                } catch (SQLException rollbackException) {
                    ex.addSuppressed(rollbackException);
                }
                throw ex;
            }
        }
    }

    private SolicitudOficio obtenerSolicitudOficio(
            Connection con,
            OficioRespuestaCpd respuesta
    ) throws SQLException {
        String sql = "SELECT s.estado, s.codigo, s.id_estudiante, u.email, " +
                "e.carrera_id, p.gestion, o.numero_oficio " +
                "FROM solicitud s " +
                "JOIN estudiantes e ON e.id_estudiante = s.id_estudiante " +
                "JOIN usuario u ON u.id_usuario = s.id_estudiante " +
                "JOIN periodos p ON p.id = s.id_periodo " +
                "JOIN oficio_cpd o ON o.id = ? AND o.estado = 'EMITIDO' " +
                "WHERE s.id = ? AND (" +
                "EXISTS (SELECT 1 FROM oficio_cpd_solicitud os " +
                "WHERE os.id_oficio = o.id AND os.id_solicitud = s.id) " +
                "OR (NOT EXISTS (SELECT 1 FROM oficio_cpd_solicitud os " +
                "WHERE os.id_oficio = o.id) AND o.id_solicitud = s.id)) " +
                "FOR UPDATE OF s";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, respuesta.getIdOficio());
            ps.setInt(2, respuesta.getIdSolicitud());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "La solicitud no pertenece a un oficio remitido a CPD.");
                }
                return new SolicitudOficio(
                        rs.getString("estado"),
                        rs.getString("codigo"),
                        rs.getInt("id_estudiante"),
                        rs.getString("email"),
                        rs.getInt("carrera_id"),
                        rs.getString("gestion"),
                        rs.getString("numero_oficio"));
            }
        }
    }

    private void actualizarSolicitud(
            Connection con,
            OficioRespuestaCpd respuesta
    ) throws SQLException {
        String sql = "UPDATE solicitud SET estado = ?, observaciones = ?, " +
                "motivo_rechazo = ?, fecha_revision = CURRENT_TIMESTAMP " +
                "WHERE id = ? AND estado = 'REMITIDA_A_CPD'";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, respuesta.getEstado());
            ps.setString(2, respuesta.getObservaciones());
            if ("RECHAZADA_CPD".equals(respuesta.getEstado())) {
                ps.setString(3, respuesta.getObservaciones());
            } else {
                ps.setNull(3, Types.VARCHAR);
            }
            ps.setInt(4, respuesta.getIdSolicitud());
            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "No se pudo actualizar el estado de la solicitud.");
            }
        }
    }

    private void insertarRespuesta(
            Connection con,
            OficioRespuestaCpd respuesta
    ) throws SQLException {
        String sql = "INSERT INTO oficio_respuesta_cpd " +
                "(id_oficio, id_solicitud, codigo_respuesta, resultado, " +
                "observaciones, referencia_respaldo, fecha_respuesta, " +
                "fecha_habilitacion_efectiva, id_responsable_cpd, estado) " +
                "VALUES (?, ?, ?, ?, ?, NULL, CURRENT_TIMESTAMP, " +
                "CASE WHEN ? THEN CURRENT_TIMESTAMP ELSE NULL END, ?, 'COMPLETADO')";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, respuesta.getIdOficio());
            ps.setInt(2, respuesta.getIdSolicitud());
            ps.setString(3, respuesta.getCodigoRespuesta());
            ps.setString(4, respuesta.getResultado());
            ps.setString(5, respuesta.getObservaciones());
            ps.setBoolean(6, "HABILITADA".equals(respuesta.getEstado()));
            ps.setInt(7, respuesta.getIdResponsableCpd());
            ps.executeUpdate();
        }
    }

    private void insertarHistorial(
            Connection con,
            OficioRespuestaCpd respuesta,
            String estadoAnterior
    ) throws SQLException {
        String sql = "INSERT INTO historial_solicitud " +
                "(id_solicitud, id_usuario, estado_anterior, estado_nuevo, accion, observacion) " +
                "VALUES (?, ?, ?, ?, 'RESPUESTA_CPD', ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, respuesta.getIdSolicitud());
            ps.setInt(2, respuesta.getIdResponsableCpd());
            ps.setString(3, estadoAnterior);
            ps.setString(4, respuesta.getEstado());
            ps.setString(5, "Resultado CPD " + respuesta.getResultado()
                    + ". Código: " + respuesta.getCodigoRespuesta()
                    + ". Observación: " + respuesta.getObservaciones());
            ps.executeUpdate();
        }
    }

    private List<DestinatarioNotificacion> obtenerDestinatarios(
            Connection con,
            SolicitudOficio solicitud,
            OficioRespuestaCpd respuesta
    ) throws SQLException {
        List<DestinatarioNotificacion> destinatarios = new ArrayList<>();
        String asunto = "Respuesta CPD " + respuesta.getResultado()
                + " - Solicitud " + solicitud.codigoSolicitud();
        String mensaje = "CPD respondió " + respuesta.getResultado()
                + " para la solicitud " + solicitud.codigoSolicitud()
                + " del período " + solicitud.periodo()
                + ". Oficio: " + solicitud.numeroOficio()
                + ". Código de respuesta: " + respuesta.getCodigoRespuesta()
                + ". Observación: " + respuesta.getObservaciones();
        destinatarios.add(new DestinatarioNotificacion(
                0, solicitud.idEstudiante(), solicitud.correoEstudiante()));

        String academicSql = "SELECT DISTINCT u.id_usuario, u.email FROM usuario u " +
                "JOIN usuario_roles ur ON ur.id_usuario = u.id_usuario " +
                "AND ur.estado = 'ACTIVO' " +
                "JOIN roles r ON r.id = ur.id_rol AND r.estado = 'ACTIVO' " +
                "LEFT JOIN personal_carrera pc ON pc.id_personal = u.id_usuario " +
                "LEFT JOIN director_carrera dc ON dc.id_director = u.id_usuario " +
                "WHERE u.estado = 'ACTIVO' AND u.email IS NOT NULL AND (" +
                "(r.nombre = 'PERSONAL_ACADEMICO' AND pc.carrera_id = ?) OR " +
                "(r.nombre = 'DIRECTOR' AND dc.carrera_id = ?))";
        try (PreparedStatement ps = con.prepareStatement(academicSql)) {
            ps.setInt(1, solicitud.idCarrera());
            ps.setInt(2, solicitud.idCarrera());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idUsuario = rs.getInt("id_usuario");
                    String email = rs.getString("email");
                    if (email == null || email.isBlank()
                            || idUsuario == solicitud.idEstudiante()) {
                        continue;
                    }
                    destinatarios.add(new DestinatarioNotificacion(
                            0, idUsuario, email));
                }
            }
        }
        List<DestinatarioNotificacion> notificaciones = new ArrayList<>();
        String insertSql = "INSERT INTO notificaciones " +
                "(id_usuario, id_solicitud, destinatario, tipo, asunto, mensaje, " +
                "canal, estado, intentos, ultimo_error, fecha_creacion, fecha_envio) " +
                "VALUES (?, ?, ?, 'RESPUESTA_CPD', ?, ?, 'EMAIL', 'PENDIENTE', " +
                "0, NULL, CURRENT_TIMESTAMP, NULL) RETURNING id";
        for (DestinatarioNotificacion destinatario : destinatarios) {
            try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                ps.setInt(1, destinatario.idUsuario());
                ps.setInt(2, respuesta.getIdSolicitud());
                ps.setString(3, destinatario.email());
                ps.setString(4, asunto);
                ps.setString(5, mensaje);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException(
                                "No se pudo crear una notificación para "
                                + destinatario.email() + ".");
                    }
                    notificaciones.add(new DestinatarioNotificacion(
                            rs.getLong(1), destinatario.idUsuario(),
                            destinatario.email()));
                }
            }
        }
        return notificaciones;
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT r.id, o.numero_oficio, s.codigo AS solicitud, " +
                "r.codigo_respuesta, r.resultado, " +
                "COALESCE(u.nombre || ' ' || u.apellidos, 'No identificado') AS responsable, " +
                "r.fecha_respuesta, " +
                "COALESCE(r.fecha_habilitacion_efectiva::text, 'NO APLICA') AS fecha_habilitacion, " +
                "r.observaciones FROM oficio_respuesta_cpd r " +
                "JOIN oficio_cpd o ON r.id_oficio = o.id " +
                "JOIN solicitud s ON r.id_solicitud = s.id " +
                "LEFT JOIN usuario u ON u.id_usuario = r.id_responsable_cpd " +
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
                        rs.getString("responsable"),
                        rs.getTimestamp("fecha_respuesta") == null
                                ? "" : rs.getTimestamp("fecha_respuesta").toString(),
                        rs.getString("fecha_habilitacion"),
                        rs.getString("observaciones") == null
                                ? "" : rs.getString("observaciones")
                });
            }
        }
        return lista;
    }

    private record SolicitudOficio(
            String estado,
            String codigoSolicitud,
            int idEstudiante,
            String correoEstudiante,
            int idCarrera,
            String periodo,
            String numeroOficio
    ) {}
}
