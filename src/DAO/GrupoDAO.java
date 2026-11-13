package DAO;

import Database.Conexion;
import Models.Grupo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GrupoDAO {

    public void create(Grupo grupo) throws SQLException {
        String sql = "INSERT INTO grupo (id_materia, id_periodo, sigla_grupo, docente, " +
                     "cupo_maximo, cupos_disponibles, cupo_excepcional_maximo, estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, grupo.getIdMateria());
            ps.setInt(2, grupo.getIdPeriodo());
            ps.setString(3, grupo.getSiglaGrupo());
            ps.setString(4, grupo.getDocente());
            ps.setInt(5, grupo.getCupoMaximo());
            ps.setInt(6, grupo.getCuposDisponibles());
            ps.setInt(7, grupo.getCupoExcepcionalMaximo());
            ps.setString(8, grupo.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    grupo.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT g.id, m.sigla AS materia, p.gestion, g.sigla_grupo, g.docente, " +
                     "g.cupo_maximo, g.cupos_disponibles, g.cupo_excepcional_maximo, g.estado " +
                     "FROM grupo g " +
                     "JOIN materia m ON g.id_materia = m.id " +
                     "JOIN periodos p ON g.id_periodo = p.id " +
                     "ORDER BY m.sigla, g.sigla_grupo";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("materia") != null ? rs.getString("materia") : "",
                        rs.getString("gestion") != null ? rs.getString("gestion") : "",
                        rs.getString("sigla_grupo") != null ? rs.getString("sigla_grupo") : "",
                        rs.getString("docente") != null ? rs.getString("docente") : "",
                        String.valueOf(rs.getInt("cupo_maximo")),
                        String.valueOf(rs.getInt("cupos_disponibles")),
                        String.valueOf(rs.getInt("cupo_excepcional_maximo")),
                        rs.getString("estado") != null ? rs.getString("estado") : ""
                });
            }
        }
        return lista;
    }

    public String[] readById(int idGrupo) throws SQLException {
        String sql = "SELECT g.id, m.sigla AS materia, p.gestion, g.sigla_grupo, g.docente, " +
                     "g.cupo_maximo, g.cupos_disponibles, g.cupo_excepcional_maximo, g.estado " +
                     "FROM grupo g " +
                     "JOIN materia m ON g.id_materia = m.id " +
                     "JOIN periodos p ON g.id_periodo = p.id " +
                     "WHERE g.id = ?";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idGrupo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                            String.valueOf(rs.getInt("id")),
                            rs.getString("materia"),
                            rs.getString("gestion"),
                            rs.getString("sigla_grupo"),
                            rs.getString("docente"),
                            String.valueOf(rs.getInt("cupo_maximo")),
                            String.valueOf(rs.getInt("cupos_disponibles")),
                            String.valueOf(rs.getInt("cupo_excepcional_maximo")),
                            rs.getString("estado")
                    };
                }
            }
        }
        return null;
    }
}
