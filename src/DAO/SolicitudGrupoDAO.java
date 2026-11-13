package DAO;

import Database.Conexion;
import Models.SolicitudGrupo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SolicitudGrupoDAO {

    public void create(SolicitudGrupo sg) throws SQLException {
        String sql = "INSERT INTO solicitud_grupo (id_solicitud, id_grupo, motivo, estado) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, sg.getIdSolicitud());
            ps.setInt(2, sg.getIdGrupo());
            ps.setString(3, sg.getMotivo());
            ps.setString(4, sg.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    sg.setId(rs.getInt(1));
                }
            }
        }
    }

    public void createForEstudiante(SolicitudGrupo sg, int idEstudiante)
            throws SQLException {
        String sql = "INSERT INTO solicitud_grupo (id_solicitud, id_grupo, motivo, estado) " +
                     "SELECT s.id, g.id, ?, ? " +
                     "FROM solicitud s " +
                     "JOIN estudiantes e ON e.id_estudiante = s.id_estudiante " +
                     "JOIN grupo g ON g.id = ? AND g.id_periodo = s.id_periodo " +
                     "WHERE s.id = ? AND s.id_estudiante = ? AND s.estado = 'ENVIADA' " +
                     "AND EXISTS (SELECT 1 FROM materia_carrera mc " +
                     "WHERE mc.id_materia = g.id_materia AND mc.id_carrera = e.carrera_id)";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sg.getMotivo());
            ps.setString(2, sg.getEstado());
            ps.setInt(3, sg.getIdGrupo());
            ps.setInt(4, sg.getIdSolicitud());
            ps.setInt(5, idEstudiante);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "El grupo no existe o no corresponde al período y carrera de la solicitud.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    sg.setId(rs.getInt(1));
                }
            }
        }
    }

    public void createForEstudiante(
            SolicitudGrupo sg,
            int idEstudiante,
            String gestion,
            String siglaMateria,
            String siglaGrupo
    ) throws SQLException {
        try (Connection con = Conexion.getInstancia().EstablecerConexion()) {
            con.setAutoCommit(false);
            try {
                int idPeriodoSolicitud;
                int idCarrera;
                String gestionSolicitud;
                String estadoSolicitud;
                String solicitudSql = "SELECT s.id_periodo, p.gestion, s.estado, e.carrera_id " +
                        "FROM solicitud s " +
                        "JOIN estudiantes e ON e.id_estudiante = s.id_estudiante " +
                        "JOIN periodos p ON p.id = s.id_periodo " +
                        "WHERE s.id = ? AND s.id_estudiante = ? FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(solicitudSql)) {
                    ps.setInt(1, sg.getIdSolicitud());
                    ps.setInt(2, idEstudiante);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "La solicitud no existe o no pertenece al estudiante remitente.");
                        }
                        idPeriodoSolicitud = rs.getInt("id_periodo");
                        gestionSolicitud = rs.getString("gestion");
                        estadoSolicitud = rs.getString("estado");
                        idCarrera = rs.getInt("carrera_id");
                    }
                }
                if (!"ENVIADA".equalsIgnoreCase(estadoSolicitud)) {
                    throw new IllegalArgumentException(
                            "Solo se pueden modificar solicitudes que aún estén en estado ENVIADA.");
                }
                if (!gestionSolicitud.equalsIgnoreCase(gestion)) {
                    throw new IllegalArgumentException(
                            "El período indicado no coincide con el período de la solicitud.");
                }

                int idGrupo;
                String estadoGrupo;
                int cuposDisponibles;
                String grupoSql = "SELECT g.id, g.id_periodo, g.estado, g.cupos_disponibles " +
                        "FROM grupo g " +
                        "JOIN materia m ON m.id = g.id_materia " +
                        "JOIN periodos p ON p.id = g.id_periodo " +
                        "WHERE LOWER(p.gestion) = LOWER(?) " +
                        "AND LOWER(m.sigla) = LOWER(?) " +
                        "AND LOWER(g.sigla_grupo) = LOWER(?) FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(grupoSql)) {
                    ps.setString(1, gestion);
                    ps.setString(2, siglaMateria);
                    ps.setString(3, siglaGrupo);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "No existe el grupo " + siglaGrupo + " para la materia "
                                    + siglaMateria + " en el período " + gestion + ".");
                        }
                        idGrupo = rs.getInt("id");
                        estadoGrupo = rs.getString("estado");
                        cuposDisponibles = rs.getInt("cupos_disponibles");
                        if (rs.getInt("id_periodo") != idPeriodoSolicitud) {
                            throw new IllegalArgumentException(
                                    "El grupo indicado no corresponde al período de la solicitud.");
                        }
                    }
                }
                if ("BLOQUEADO".equalsIgnoreCase(estadoGrupo)) {
                    throw new IllegalArgumentException(
                            "El grupo " + siglaGrupo + " está bloqueado y no puede agregarse.");
                }
                if (cuposDisponibles <= 0) {
                    throw new IllegalArgumentException(
                            "El grupo " + siglaGrupo + " no tiene cupos disponibles.");
                }

                String carreraSql = "SELECT EXISTS (SELECT 1 FROM materia_carrera mc " +
                        "JOIN grupo g ON g.id_materia = mc.id_materia " +
                        "WHERE g.id = ? AND mc.id_carrera = ?)";
                try (PreparedStatement ps = con.prepareStatement(carreraSql)) {
                    ps.setInt(1, idGrupo);
                    ps.setInt(2, idCarrera);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next() || !rs.getBoolean(1)) {
                            throw new IllegalArgumentException(
                                    "La materia no pertenece a la carrera del estudiante.");
                        }
                    }
                }

                String insertSql = "INSERT INTO solicitud_grupo " +
                        "(id_solicitud, id_grupo, motivo, estado) VALUES (?, ?, ?, ?) " +
                        "RETURNING id";
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, sg.getIdSolicitud());
                    ps.setInt(2, idGrupo);
                    ps.setString(3, sg.getMotivo());
                    ps.setString(4, sg.getEstado());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("No se pudo agregar el grupo a la solicitud.");
                        }
                        sg.setId(rs.getInt(1));
                        sg.setIdGrupo(idGrupo);
                    }
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
    }

    public List<String[]> listBySolicitud(int idSolicitud) throws SQLException {
        String sql = "SELECT sg.id, s.codigo, m.sigla AS materia, g.sigla_grupo, sg.motivo, sg.estado " +
                     "FROM solicitud_grupo sg " +
                     "JOIN solicitud s ON sg.id_solicitud = s.id " +
                     "JOIN grupo g ON sg.id_grupo = g.id " +
                     "JOIN materia m ON g.id_materia = m.id " +
                     "WHERE sg.id_solicitud = ?";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                            String.valueOf(rs.getInt("id")),
                            rs.getString("codigo"),
                            rs.getString("materia"),
                            rs.getString("sigla_grupo"),
                            rs.getString("motivo") != null ? rs.getString("motivo") : "",
                            rs.getString("estado") != null ? rs.getString("estado") : ""
                    });
                }
            }
        }
        return lista;
    }
}
