package DAO;

import Database.Conexion;
import Models.BloqueoGrupo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BloqueoGrupoDAO {

    public void bloquearCupo(BloqueoGrupo bloqueo) throws SQLException {
        try (Connection con = Conexion.getInstancia().EstablecerConexion()) {
            con.setAutoCommit(false);
            try {
                String estadoSolicitud = validarSolicitudParaBloqueo(
                        con, bloqueo.getIdSolicitud(), bloqueo.getIdGrupo());

                int cuposDisponibles;
                String grupoSql = "SELECT cupos_disponibles FROM grupo WHERE id = ? FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(grupoSql)) {
                    ps.setInt(1, bloqueo.getIdGrupo());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "No existe el grupo con ID " + bloqueo.getIdGrupo() + ".");
                        }
                        cuposDisponibles = rs.getInt("cupos_disponibles");
                    }
                }
                if (cuposDisponibles < bloqueo.getCantidad()) {
                    throw new IllegalArgumentException(
                            "El grupo " + bloqueo.getIdGrupo() + " solo tiene "
                            + cuposDisponibles + " cupo(s) disponible(s); no se puede bloquear "
                            + bloqueo.getCantidad() + ".");
                }

                String descontarSql = "UPDATE grupo SET cupos_disponibles = cupos_disponibles - ? " +
                                      "WHERE id = ? AND cupos_disponibles >= ?";
                try (PreparedStatement ps = con.prepareStatement(descontarSql)) {
                    ps.setInt(1, bloqueo.getCantidad());
                    ps.setInt(2, bloqueo.getIdGrupo());
                    ps.setInt(3, bloqueo.getCantidad());
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo descontar el cupo. Actualice la lista de grupos e intente nuevamente.");
                    }
                }

                String insertSql = "INSERT INTO bloqueo_grupo " +
                        "(id_grupo, id_solicitud, cantidad, motivo, estado, autorizado_por, fecha_bloqueo, observacion) " +
                        "VALUES (?, ?, ?, ?, 'RESERVADO', ?, CURRENT_TIMESTAMP, ?) RETURNING id";
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, bloqueo.getIdGrupo());
                    if (bloqueo.getIdSolicitud() != null) {
                        ps.setInt(2, bloqueo.getIdSolicitud());
                    } else {
                        ps.setNull(2, Types.INTEGER);
                    }
                    ps.setInt(3, bloqueo.getCantidad());
                    ps.setString(4, bloqueo.getMotivo());
                    if (bloqueo.getAutorizadoPor() != null) {
                        ps.setInt(5, bloqueo.getAutorizadoPor());
                    } else {
                        ps.setNull(5, Types.INTEGER);
                    }
                    ps.setString(6, bloqueo.getObservacion());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("No se pudo registrar el bloqueo.");
                        }
                        bloqueo.setId(rs.getInt(1));
                    }
                }

                insertarHistorial(
                        con, bloqueo, estadoSolicitud, "BLOQUEO_CUPO",
                        "Se reservaron " + bloqueo.getCantidad()
                        + " cupo(s) en el grupo " + bloqueo.getIdGrupo()
                        + " por: " + bloqueo.getMotivo());
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                throw ex;
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

    public BloqueoGrupo liberarCupo(
            int idBloqueo,
            String observacion,
            int idAutorizador
    )
            throws SQLException {
        try (Connection con = Conexion.getInstancia().EstablecerConexion()) {
            con.setAutoCommit(false);
            try {
                BloqueoGrupo bloqueo = readById(con, idBloqueo, true);
                if (bloqueo == null) {
                    throw new IllegalArgumentException(
                            "No existe el bloqueo con ID " + idBloqueo + ".");
                }
                if (!"RESERVADO".equalsIgnoreCase(bloqueo.getEstado())) {
                    throw new IllegalArgumentException(
                            "El bloqueo " + idBloqueo + " ya no está reservado. "
                            + "Estado actual: " + bloqueo.getEstado() + ".");
                }
                String estadoSolicitud = obtenerEstadoSolicitud(
                        con, bloqueo.getIdSolicitud());

                String grupoSql = "UPDATE grupo SET cupos_disponibles = cupos_disponibles + ? " +
                        "WHERE id = ? AND cupos_disponibles + ? <= cupo_maximo";
                try (PreparedStatement ps = con.prepareStatement(grupoSql)) {
                    ps.setInt(1, bloqueo.getCantidad());
                    ps.setInt(2, bloqueo.getIdGrupo());
                    ps.setInt(3, bloqueo.getCantidad());
                    if (ps.executeUpdate() != 1) {
                        throw new IllegalStateException(
                                "No se pueden reintegrar los cupos: la disponibilidad del grupo "
                                + "alcanzaría o superaría su cupo máximo.");
                    }
                }

                String updateSql = "UPDATE bloqueo_grupo SET estado = 'LIBERADO', " +
                        "fecha_resolucion = CURRENT_TIMESTAMP, observacion = ? " +
                        "WHERE id = ? AND estado = 'RESERVADO'";
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setString(1, observacion);
                    ps.setInt(2, idBloqueo);
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "El bloqueo cambió de estado mientras se procesaba; no se liberaron cupos.");
                    }
                }

                bloqueo.setEstado("LIBERADO");
                bloqueo.setObservacion(observacion);
                bloqueo.setAutorizadoPor(idAutorizador);
                insertarHistorial(
                        con, bloqueo, estadoSolicitud,
                        "LIBERACION_CUPO",
                        "Se liberaron " + bloqueo.getCantidad()
                        + " cupo(s) del grupo " + bloqueo.getIdGrupo()
                        + ". Observación: " + observacion);
                con.commit();
                return bloqueo;
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                throw ex;
            }
        }
    }

    private BloqueoGrupo readById(
            Connection con,
            int idBloqueo,
            boolean bloquearFila
    ) throws SQLException {
        String sql = "SELECT id, id_grupo, id_solicitud, cantidad, motivo, estado, autorizado_por, fecha_bloqueo, fecha_resolucion, observacion " +
                     "FROM bloqueo_grupo WHERE id = ?" + (bloquearFila ? " FOR UPDATE" : "");
        try (PreparedStatement ps = con.prepareStatement(sql)) {
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

    private String obtenerEstadoSolicitud(Connection con, Integer idSolicitud)
            throws SQLException {
        if (idSolicitud == null) {
            return null;
        }
        String sql = "SELECT estado FROM solicitud WHERE id = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("estado");
                }
            }
        }
        throw new IllegalArgumentException(
                "No existe la solicitud con ID " + idSolicitud + ".");
    }

    private String validarSolicitudParaBloqueo(
            Connection con,
            Integer idSolicitud,
            int idGrupo
    ) throws SQLException {
        if (idSolicitud == null) {
            throw new IllegalArgumentException(
                    "El bloqueo debe estar asociado a una solicitud aprobada.");
        }
        String sql = "SELECT estado FROM solicitud WHERE id = ? FOR UPDATE";
        String estado;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                            "No existe la solicitud con ID " + idSolicitud + ".");
                }
                estado = rs.getString("estado");
            }
        }
        if (!"APROBADA_DIRECCION".equalsIgnoreCase(estado)) {
            throw new IllegalArgumentException(
                    "Solo se pueden reservar cupos para solicitudes aprobadas "
                    + "por Dirección. Estado actual: " + estado + ".");
        }

        String grupoSql = "SELECT EXISTS (SELECT 1 FROM solicitud_grupo " +
                "WHERE id_solicitud = ? AND id_grupo = ?)";
        try (PreparedStatement ps = con.prepareStatement(grupoSql)) {
            ps.setInt(1, idSolicitud);
            ps.setInt(2, idGrupo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || !rs.getBoolean(1)) {
                    throw new IllegalArgumentException(
                            "El grupo " + idGrupo + " no fue solicitado en el expediente "
                            + idSolicitud + ".");
                }
            }
        }
        return estado;
    }

    private void insertarHistorial(
            Connection con,
            BloqueoGrupo bloqueo,
            String estadoSolicitud,
            String accion,
            String observacion
    ) throws SQLException {
        if (bloqueo.getIdSolicitud() == null) {
            return;
        }
        String sql = "INSERT INTO historial_solicitud " +
                "(id_solicitud, id_usuario, estado_anterior, estado_nuevo, accion, observacion, fecha_evento) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, bloqueo.getIdSolicitud());
            if (bloqueo.getAutorizadoPor() != null) {
                ps.setInt(2, bloqueo.getAutorizadoPor());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, estadoSolicitud);
            ps.setString(4, estadoSolicitud);
            ps.setString(5, accion);
            ps.setString(6, observacion);
            ps.executeUpdate();
        }
    }

    private void rollback(Connection con, Exception original) {
        try {
            con.rollback();
        } catch (SQLException rollbackException) {
            original.addSuppressed(rollbackException);
        }
    }
}
