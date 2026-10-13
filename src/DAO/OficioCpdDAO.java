package DAO;

import Database.Conexion;
import Models.OficioCpd;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OficioCpdDAO {

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
        String sql = "SELECT o.id, o.numero_oficio, s.codigo AS solicitud, o.destinatario, " +
                     "o.referencia, u.nombre || ' ' || u.apellidos AS responsable, " +
                     "o.fecha_remision, o.estado " +
                     "FROM oficio_cpd o " +
                     "JOIN solicitud s ON o.id_solicitud = s.id " +
                     "JOIN usuario u ON o.id_responsable_remision = u.id_usuario " +
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
}
