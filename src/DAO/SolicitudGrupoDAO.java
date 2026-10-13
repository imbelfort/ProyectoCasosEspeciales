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
