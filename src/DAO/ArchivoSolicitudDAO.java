package DAO;

import Database.Conexion;
import Models.ArchivoSolicitud;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArchivoSolicitudDAO {

    public void create(ArchivoSolicitud archivo) throws SQLException {
        String sql = "INSERT INTO archivo_solicitud (id_solicitud, nombre_archivo, tipo_archivo, ruta_archivo, fecha_subida) " +
                     "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, archivo.getIdSolicitud());
            ps.setString(2, archivo.getNombreArchivo());
            ps.setString(3, archivo.getTipoArchivo());
            ps.setString(4, archivo.getRutaArchivo());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    archivo.setId(rs.getInt(1));
                }
            }
        }
    }

    public void createForEstudiante(ArchivoSolicitud archivo, int idEstudiante)
            throws SQLException {
        String sql = "INSERT INTO archivo_solicitud " +
                     "(id_solicitud, nombre_archivo, tipo_archivo, ruta_archivo, fecha_subida) " +
                     "SELECT s.id, ?, ?, ?, CURRENT_TIMESTAMP FROM solicitud s " +
                     "WHERE s.id = ? AND s.id_estudiante = ? AND s.estado = 'ENVIADA'";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, archivo.getNombreArchivo());
            ps.setString(2, archivo.getTipoArchivo());
            ps.setString(3, archivo.getRutaArchivo());
            ps.setInt(4, archivo.getIdSolicitud());
            ps.setInt(5, idEstudiante);
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "La solicitud dejó de estar disponible para adjuntar documentos.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    archivo.setId(rs.getInt(1));
                }
            }
        }
    }

    public void createPaymentReceiptForEstudiante(
            ArchivoSolicitud archivo,
            int idEstudiante
    ) throws SQLException {
        try (Connection con = Conexion.getInstancia().EstablecerConexion()) {
            con.setAutoCommit(false);
            try {
                String solicitudSql = "SELECT estado FROM solicitud " +
                        "WHERE id = ? AND id_estudiante = ? FOR UPDATE";
                try (PreparedStatement ps = con.prepareStatement(solicitudSql)) {
                    ps.setInt(1, archivo.getIdSolicitud());
                    ps.setInt(2, idEstudiante);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new IllegalArgumentException(
                                    "La solicitud no existe o no pertenece al estudiante remitente.");
                        }
                        if (!"APROBADA_DIRECCION".equalsIgnoreCase(rs.getString("estado"))) {
                            throw new IllegalArgumentException(
                                    "El comprobante solo puede adjuntarse a una solicitud aceptada "
                                    + "por Dirección.");
                        }
                    }
                }

                String existingSql = "SELECT 1 FROM archivo_solicitud " +
                        "WHERE id_solicitud = ? AND UPPER(tipo_archivo) = 'COMPROBANTE_PAGO' " +
                        "LIMIT 1";
                try (PreparedStatement ps = con.prepareStatement(existingSql)) {
                    ps.setInt(1, archivo.getIdSolicitud());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            throw new IllegalArgumentException(
                                    "Ya existe un comprobante de pago para esta solicitud.");
                        }
                    }
                }

                String insertSql = "INSERT INTO archivo_solicitud " +
                        "(id_solicitud, nombre_archivo, tipo_archivo, ruta_archivo, fecha_subida) " +
                        "VALUES (?, ?, 'COMPROBANTE_PAGO', ?, CURRENT_TIMESTAMP) RETURNING id";
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, archivo.getIdSolicitud());
                    ps.setString(2, archivo.getNombreArchivo());
                    ps.setString(3, archivo.getRutaArchivo());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "No se pudo registrar el comprobante de pago.");
                        }
                        archivo.setId(rs.getInt(1));
                    }
                }
                String updateSql = "UPDATE solicitud SET estado = 'APROBADA', "
                        + "fecha_revision = CURRENT_TIMESTAMP "
                        + "WHERE id = ? AND estado = 'APROBADA_DIRECCION'";
                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setInt(1, archivo.getIdSolicitud());
                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "No se pudo actualizar la solicitud después de registrar el pago.");
                    }
                }
                String historialSql = "INSERT INTO historial_solicitud "
                        + "(id_solicitud, id_usuario, estado_anterior, estado_nuevo, "
                        + "accion, observacion, fecha_evento) "
                        + "VALUES (?, ?, 'APROBADA_DIRECCION', 'APROBADA', "
                        + "'REGISTRO_PAGO', ?, CURRENT_TIMESTAMP)";
                try (PreparedStatement ps = con.prepareStatement(historialSql)) {
                    ps.setInt(1, archivo.getIdSolicitud());
                    ps.setInt(2, idEstudiante);
                    ps.setString(3, "Comprobante de pago registrado: "
                            + archivo.getNombreArchivo());
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
    }

    public List<String[]> listPaymentsByEstudiante(int idEstudiante)
            throws SQLException {
        String sql = "SELECT a.id, a.id_solicitud, s.codigo, p.gestion, "
                + "a.nombre_archivo, a.tipo_archivo, a.fecha_subida, s.estado "
                + "FROM archivo_solicitud a "
                + "JOIN solicitud s ON s.id = a.id_solicitud "
                + "JOIN periodos p ON p.id = s.id_periodo "
                + "WHERE s.id_estudiante = ? "
                + "AND UPPER(a.tipo_archivo) = 'COMPROBANTE_PAGO' "
                + "ORDER BY a.id DESC";
        List<String[]> pagos = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idEstudiante);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    pagos.add(new String[]{
                            String.valueOf(rs.getInt("id")),
                            String.valueOf(rs.getInt("id_solicitud")),
                            rs.getString("codigo"),
                            rs.getString("gestion"),
                            rs.getString("nombre_archivo"),
                            rs.getString("tipo_archivo"),
                            rs.getTimestamp("fecha_subida") != null
                                    ? rs.getTimestamp("fecha_subida").toString() : "",
                            rs.getString("estado")
                    });
                }
            }
        }
        return pagos;
    }

    public boolean exists(int idSolicitud, String nombreArchivo, String tipoArchivo)
            throws SQLException {
        String sql = "SELECT 1 FROM archivo_solicitud " +
                     "WHERE id_solicitud = ? AND LOWER(nombre_archivo) = LOWER(?) " +
                     "AND LOWER(COALESCE(tipo_archivo, '')) = LOWER(COALESCE(?, '')) LIMIT 1";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            ps.setString(2, nombreArchivo);
            ps.setString(3, tipoArchivo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public List<String[]> listBySolicitud(int idSolicitud) throws SQLException {
        String sql = "SELECT id, id_solicitud, nombre_archivo, tipo_archivo, fecha_subida " +
                     "FROM archivo_solicitud WHERE id_solicitud = ? ORDER BY id";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                            String.valueOf(rs.getInt("id")),
                            String.valueOf(rs.getInt("id_solicitud")),
                            rs.getString("nombre_archivo") != null ? rs.getString("nombre_archivo") : "",
                            rs.getString("tipo_archivo") != null ? rs.getString("tipo_archivo") : "",
                            rs.getTimestamp("fecha_subida") != null ? rs.getTimestamp("fecha_subida").toString() : ""
                    });
                }
            }
        }
        return lista;
    }

    public List<ArchivoSolicitud> listDetailsBySolicitud(int idSolicitud)
            throws SQLException {
        String sql = "SELECT id, id_solicitud, nombre_archivo, tipo_archivo, " +
                     "ruta_archivo, fecha_subida " +
                     "FROM archivo_solicitud WHERE id_solicitud = ? ORDER BY id";
        List<ArchivoSolicitud> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idSolicitud);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ArchivoSolicitud(
                            rs.getInt("id"),
                            rs.getInt("id_solicitud"),
                            rs.getString("nombre_archivo"),
                            rs.getString("tipo_archivo"),
                            rs.getString("ruta_archivo"),
                            rs.getTimestamp("fecha_subida")));
                }
            }
        }
        return lista;
    }
}
