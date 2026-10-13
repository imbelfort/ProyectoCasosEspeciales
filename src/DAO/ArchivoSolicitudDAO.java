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

    public List<String[]> listBySolicitud(int idSolicitud) throws SQLException {
        String sql = "SELECT id, id_solicitud, nombre_archivo, tipo_archivo, fecha_subida FROM archivo_solicitud WHERE id_solicitud = ?";
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
}
