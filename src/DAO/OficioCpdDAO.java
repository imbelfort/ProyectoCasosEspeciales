package DAO;

import Database.Conexion;
import Models.OficioCpd;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OficioCpdDAO {

    public String obtenerGestionPeriodo(int idPeriodo) throws SQLException {
        String sql = "SELECT gestion FROM periodos WHERE id = ?";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPeriodo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("gestion") : null;
            }
        }
    }

    public List<String[]> listarSolicitudesPagadasElegibles(String gestion)
            throws SQLException {
        String sql = "SELECT s.id, s.codigo, p.gestion, " +
                "u.nombre || ' ' || u.apellidos AS estudiante, " +
                "e.registro_universitario, u.email, s.tipo_caso, s.motivo, " +
                "COALESCE(string_agg(DISTINCT m.sigla || ' - ' || m.nombre || " +
                "' (' || g.sigla_grupo || ')', '; '), 'Sin grupo') AS grupos, " +
                "string_agg(DISTINCT a.nombre_archivo, '; ') AS comprobantes " +
                "FROM solicitud s " +
                "JOIN estudiantes e ON e.id_estudiante = s.id_estudiante " +
                "JOIN usuario u ON u.id_usuario = e.id_estudiante " +
                "JOIN periodos p ON p.id = s.id_periodo " +
                "LEFT JOIN solicitud_grupo sg ON sg.id_solicitud = s.id " +
                "LEFT JOIN grupo g ON g.id = sg.id_grupo " +
                "LEFT JOIN materia m ON m.id = g.id_materia " +
                "JOIN archivo_solicitud a ON a.id_solicitud = s.id " +
                "AND UPPER(a.tipo_archivo) = 'COMPROBANTE_PAGO' " +
                "WHERE s.estado = 'APROBADA' " +
                "AND NOT EXISTS (SELECT 1 FROM oficio_cpd_solicitud os " +
                "WHERE os.id_solicitud = s.id) " +
                "AND LOWER(p.gestion) = LOWER(?) " +
                "GROUP BY s.id, s.codigo, p.gestion, u.nombre, u.apellidos, " +
                "e.registro_universitario, u.email, s.tipo_caso, s.motivo " +
                "ORDER BY s.id";
        List<String[]> casos = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, gestion);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    casos.add(new String[]{
                            String.valueOf(rs.getInt("id")),
                            rs.getString("codigo"),
                            rs.getString("gestion"),
                            rs.getString("estudiante"),
                            rs.getString("registro_universitario"),
                            rs.getString("email"),
                            rs.getString("tipo_caso"),
                            rs.getString("motivo"),
                            rs.getString("grupos"),
                            rs.getString("comprobantes")
                    });
                }
            }
        }
        return casos;
    }

    public String obtenerGestionPeriodoOficio(int idOficio) throws SQLException {
        String sql = "SELECT p.gestion FROM oficio_cpd o " +
                "LEFT JOIN oficio_cpd_solicitud os ON os.id_oficio = o.id " +
                "JOIN solicitud s ON s.id = COALESCE(os.id_solicitud, o.id_solicitud) " +
                "JOIN periodos p ON p.id = s.id_periodo " +
                "WHERE o.id = ? ORDER BY s.id LIMIT 1";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOficio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("gestion") : null;
            }
        }
    }

    public void registrarOficioPendiente(
            OficioCpd oficio,
            List<Integer> idsSolicitudes,
            String gestion
    )
            throws SQLException {
        if (idsSolicitudes == null || idsSolicitudes.isEmpty()) {
            throw new IllegalArgumentException(
                    "No hay solicitudes aceptadas con comprobante de pago para remitir.");
        }
        try (Connection con = Conexion.getInstancia().EstablecerConexion()) {
            con.setAutoCommit(false);
            try {
                String insertSql = "INSERT INTO oficio_cpd " +
                        "(numero_oficio, id_solicitud, destinatario, referencia, detalle, " +
                        "id_responsable_remision, fecha_remision, estado, ruta_documento) " +
                        "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, 'PENDIENTE_ENVIO', ?) " +
                        "RETURNING id";
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setString(1, oficio.getNumeroOficio());
                    ps.setInt(2, idsSolicitudes.get(0));
                    ps.setString(3, oficio.getDestinatario());
                    ps.setString(4, oficio.getReferencia());
                    ps.setString(5, oficio.getDetalle());
                    ps.setInt(6, oficio.getIdResponsableRemision());
                    ps.setString(7, oficio.getRutaDocumento());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("No se pudo registrar el oficio.");
                        }
                        oficio.setId(rs.getInt(1));
                    }
                }

                String lockSql = "SELECT s.estado, p.gestion, EXISTS " +
                        "(SELECT 1 FROM archivo_solicitud a " +
                        "WHERE a.id_solicitud = s.id AND UPPER(a.tipo_archivo) = " +
                        "'COMPROBANTE_PAGO') AS tiene_pago " +
                        "FROM solicitud s JOIN periodos p ON p.id = s.id_periodo " +
                        "WHERE s.id = ? FOR UPDATE";
                String linkSql = "INSERT INTO oficio_cpd_solicitud (id_oficio, id_solicitud) " +
                        "VALUES (?, ?)";
                String stateSql = "UPDATE solicitud SET estado = 'PENDIENTE_REMISION_CPD' " +
                        "WHERE id = ? AND estado = 'APROBADA'";
                for (int idSolicitud : idsSolicitudes) {
                    try (PreparedStatement ps = con.prepareStatement(lockSql)) {
                        ps.setInt(1, idSolicitud);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (!rs.next()
                                    || !"APROBADA".equals(rs.getString("estado"))
                                    || !gestion.equalsIgnoreCase(rs.getString("gestion"))
                                    || !rs.getBoolean("tiene_pago")) {
                                throw new SQLException(
                                        "La solicitud " + idSolicitud
                                        + " ya no está aceptada y pagada para el período indicado; "
                                        + "vuelva a intentar.");
                            }
                        }
                    }
                    try (PreparedStatement ps = con.prepareStatement(linkSql)) {
                        ps.setInt(1, oficio.getId());
                        ps.setInt(2, idSolicitud);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(stateSql)) {
                        ps.setInt(1, idSolicitud);
                        if (ps.executeUpdate() != 1) {
                            throw new SQLException(
                                    "No se pudo reservar la solicitud " + idSolicitud
                                    + " para el oficio.");
                        }
                    }
                    insertarHistorial(
                            con,
                            idSolicitud,
                            oficio.getIdResponsableRemision(),
                            "APROBADA",
                            "PENDIENTE_REMISION_CPD",
                            "PREPARACION_REMISION_CPD",
                            "Incluida en el oficio " + oficio.getNumeroOficio()
                                    + "; envío pendiente.");
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

    public String[] obtenerOficioPendiente(int idOficio) throws SQLException {
        String sql = "SELECT id, numero_oficio, destinatario, referencia, detalle, " +
                "ruta_documento FROM oficio_cpd WHERE id = ? AND estado = 'PENDIENTE_ENVIO'";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOficio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? new String[]{
                                String.valueOf(rs.getInt("id")),
                                rs.getString("numero_oficio"),
                                rs.getString("destinatario"),
                                rs.getString("referencia"),
                                rs.getString("detalle"),
                                rs.getString("ruta_documento")
                        }
                        : null;
            }
        }
    }

    public int contarSolicitudesDeOficio(int idOficio) throws SQLException {
        String sql = "SELECT COUNT(*) FROM oficio_cpd_solicitud WHERE id_oficio = ?";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOficio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<String> listarRutasComprobantesOficio(int idOficio) throws SQLException {
        String sql = "SELECT DISTINCT a.ruta_archivo FROM oficio_cpd_solicitud os " +
                "JOIN archivo_solicitud a ON a.id_solicitud = os.id_solicitud " +
                "WHERE os.id_oficio = ? AND UPPER(a.tipo_archivo) = 'COMPROBANTE_PAGO' " +
                "ORDER BY a.ruta_archivo";
        List<String> rutas = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOficio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rutas.add(rs.getString(1));
                }
            }
        }
        return rutas;
    }

    public String obtenerRutaDocumentoOficio(int idOficio) throws SQLException {
        String sql = "SELECT ruta_documento FROM oficio_cpd "
                + "WHERE id = ? AND estado = 'EMITIDO'";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOficio);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("ruta_documento") : null;
            }
        }
    }

    public int marcarOficioEnviado(int idOficio) throws SQLException {
        try (Connection con = Conexion.getInstancia().EstablecerConexion()) {
            con.setAutoCommit(false);
            try {
                int idResponsable;
                String officeSql = "UPDATE oficio_cpd SET estado = 'EMITIDO' " +
                        "WHERE id = ? AND estado = 'PENDIENTE_ENVIO' " +
                        "RETURNING id_responsable_remision";
                try (PreparedStatement ps = con.prepareStatement(officeSql)) {
                    ps.setInt(1, idOficio);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "El oficio no existe o ya fue remitido.");
                        }
                        idResponsable = rs.getInt(1);
                    }
                }
                List<Integer> ids = new ArrayList<>();
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id_solicitud FROM oficio_cpd_solicitud " +
                        "WHERE id_oficio = ? ORDER BY id_solicitud")) {
                    ps.setInt(1, idOficio);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            ids.add(rs.getInt(1));
                        }
                    }
                }
                if (ids.isEmpty()) {
                    throw new SQLException("El oficio no tiene solicitudes asociadas.");
                }
                for (int idSolicitud : ids) {
                    try (PreparedStatement ps = con.prepareStatement(
                            "UPDATE solicitud SET estado = 'REMITIDA_A_CPD' " +
                            "WHERE id = ? AND estado = 'PENDIENTE_REMISION_CPD'")) {
                        ps.setInt(1, idSolicitud);
                        if (ps.executeUpdate() != 1) {
                            throw new SQLException(
                                    "No se pudo confirmar la remisión de la solicitud "
                                    + idSolicitud + ".");
                        }
                    }
                    insertarHistorial(
                            con,
                            idSolicitud,
                            idResponsable,
                            "PENDIENTE_REMISION_CPD",
                            "REMITIDA_A_CPD",
                            "REMISION_CPD",
                            "Oficio remitido a CPD: " + idOficio + ".");
                }
                con.commit();
                return ids.size();
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

    private void insertarHistorial(
            Connection con,
            int idSolicitud,
            int idUsuario,
            String estadoAnterior,
            String estadoNuevo,
            String accion,
            String observacion
    ) throws SQLException {
        String sql = "INSERT INTO historial_solicitud " +
                "(id_solicitud, id_usuario, estado_anterior, estado_nuevo, accion, observacion) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            ps.setInt(2, idUsuario);
            ps.setString(3, estadoAnterior);
            ps.setString(4, estadoNuevo);
            ps.setString(5, accion);
            ps.setString(6, observacion);
            ps.executeUpdate();
        }
    }

    public void create(OficioCpd of) throws SQLException {
        String sql = "INSERT INTO oficio_cpd (numero_oficio, id_solicitud, destinatario, referencia, detalle, " +
                     "id_responsable_remision, fecha_remision, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?) RETURNING id";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, of.getNumeroOficio());
            ps.setInt(2, of.getIdSolicitud());
            ps.setString(3, of.getDestinatario());
            ps.setString(4, of.getReferencia());
            ps.setString(5, of.getDetalle());
            ps.setInt(6, of.getIdResponsableRemision());
            ps.setString(7, of.getEstado() != null ? of.getEstado() : "EMITIDO");

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    of.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<String[]> listAll() throws SQLException {
        return listarOficios(false);
    }

    public List<String[]> listarOficiosRemitidos() throws SQLException {
        return listarOficios(true);
    }

    public List<String[]> listarSolicitudesPendientesCpd() throws SQLException {
        String sql = "SELECT o.id AS id_oficio, o.numero_oficio, s.id AS id_solicitud, " +
                "s.codigo AS codigo_solicitud, " +
                "u.nombre || ' ' || u.apellidos AS estudiante, p.gestion AS periodo, " +
                "s.estado FROM oficio_cpd o " +
                "JOIN oficio_cpd_solicitud os ON os.id_oficio = o.id " +
                "JOIN solicitud s ON s.id = os.id_solicitud " +
                "JOIN estudiantes e ON e.id_estudiante = s.id_estudiante " +
                "JOIN usuario u ON u.id_usuario = e.id_estudiante " +
                "JOIN periodos p ON p.id = s.id_periodo " +
                "WHERE o.estado = 'EMITIDO' AND s.estado = 'REMITIDA_A_CPD' " +
                "UNION ALL " +
                "SELECT o.id, o.numero_oficio, s.id, s.codigo, " +
                "u.nombre || ' ' || u.apellidos, p.gestion, s.estado " +
                "FROM oficio_cpd o " +
                "JOIN solicitud s ON s.id = o.id_solicitud " +
                "JOIN estudiantes e ON e.id_estudiante = s.id_estudiante " +
                "JOIN usuario u ON u.id_usuario = e.id_estudiante " +
                "JOIN periodos p ON p.id = s.id_periodo " +
                "WHERE o.estado = 'EMITIDO' AND s.estado = 'REMITIDA_A_CPD' " +
                "AND NOT EXISTS (SELECT 1 FROM oficio_cpd_solicitud os " +
                "WHERE os.id_oficio = o.id) " +
                "ORDER BY 1, 3";
        List<String[]> pendientes = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pendientes.add(new String[]{
                        String.valueOf(rs.getInt("id_oficio")),
                        rs.getString("numero_oficio"),
                        String.valueOf(rs.getInt("id_solicitud")),
                        rs.getString("codigo_solicitud"),
                        rs.getString("estudiante"),
                        rs.getString("periodo"),
                        rs.getString("estado")
                });
            }
        }
        return pendientes;
    }

    private List<String[]> listarOficios(boolean soloRemitidos) throws SQLException {
        String sql = "SELECT o.id, o.numero_oficio, " +
                     "string_agg(DISTINCT s.id || ' - ' || s.codigo, ', ' " +
                     "ORDER BY s.id || ' - ' || s.codigo) AS solicitud, o.destinatario, " +
                     "string_agg(DISTINCT p.gestion, ', ' ORDER BY p.gestion) AS periodo, " +
                     "o.referencia, u.nombre || ' ' || u.apellidos AS responsable, " +
                     "o.fecha_remision, o.estado " +
                     "FROM oficio_cpd o " +
                     "LEFT JOIN oficio_cpd_solicitud os ON os.id_oficio = o.id " +
                     "JOIN solicitud s ON s.id = COALESCE(os.id_solicitud, o.id_solicitud) " +
                     "JOIN periodos p ON p.id = s.id_periodo " +
                     "JOIN usuario u ON o.id_responsable_remision = u.id_usuario " +
                     (soloRemitidos ? "WHERE o.estado = 'EMITIDO' " : "") +
                     "GROUP BY o.id, u.nombre, u.apellidos " +
                     "ORDER BY o.id DESC";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("numero_oficio"),
                        rs.getString("solicitud"),
                        rs.getString("periodo"),
                        rs.getString("destinatario"),
                        rs.getString("referencia"),
                        rs.getString("responsable"),
                        rs.getTimestamp("fecha_remision") != null ? rs.getTimestamp("fecha_remision").toString() : "",
                        rs.getString("estado")
                });
            }
        }
        return lista;
    }

    public List<String[]> listarSolicitudesDeOficio(int idOficio)
            throws SQLException {
        String sql = "SELECT s.id, s.codigo, u.nombre || ' ' || u.apellidos AS estudiante, " +
                "p.gestion, COALESCE(string_agg(DISTINCT m.sigla || ' - ' || m.nombre " +
                "|| ' (' || g.sigla_grupo || ')', '; '), 'Sin grupo') AS grupos, " +
                "COALESCE(string_agg(DISTINCT a.nombre_archivo, '; '), 'Sin comprobante') " +
                "AS comprobantes, s.estado " +
                "FROM oficio_cpd o " +
                "LEFT JOIN oficio_cpd_solicitud os ON os.id_oficio = o.id " +
                "JOIN solicitud s ON s.id = COALESCE(os.id_solicitud, o.id_solicitud) " +
                "JOIN usuario u ON u.id_usuario = s.id_estudiante " +
                "JOIN periodos p ON p.id = s.id_periodo " +
                "LEFT JOIN solicitud_grupo sg ON sg.id_solicitud = s.id " +
                "LEFT JOIN grupo g ON g.id = sg.id_grupo " +
                "LEFT JOIN materia m ON m.id = g.id_materia " +
                "LEFT JOIN archivo_solicitud a ON a.id_solicitud = s.id " +
                "AND UPPER(a.tipo_archivo) = 'COMPROBANTE_PAGO' " +
                "WHERE o.id = ? AND o.estado = 'EMITIDO' " +
                "GROUP BY s.id, s.codigo, u.nombre, u.apellidos, p.gestion " +
                "ORDER BY s.id";
        List<String[]> solicitudes = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOficio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    solicitudes.add(new String[]{
                            String.valueOf(rs.getInt("id")),
                            rs.getString("codigo"),
                            rs.getString("estudiante"),
                            rs.getString("gestion"),
                            rs.getString("grupos"),
                            rs.getString("comprobantes"),
                            rs.getString("estado")
                    });
                }
            }
        }
        return solicitudes;
    }
}
