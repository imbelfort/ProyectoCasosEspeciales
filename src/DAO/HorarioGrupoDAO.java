package DAO;

import Database.Conexion;
import Models.HorarioGrupo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HorarioGrupoDAO {

    public void create(HorarioGrupo hg) throws SQLException {
        String sql = "INSERT INTO horario_grupo (id_grupo, dia, hora_inicio, hora_fin, aula) VALUES (?, ?, ?::time, ?::time, ?)";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, hg.getIdGrupo());
            ps.setString(2, hg.getDia());
            ps.setString(3, hg.getHoraInicio());
            ps.setString(4, hg.getHoraFin());
            ps.setString(5, hg.getAula());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    hg.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<String[]> listByGrupo(int idGrupo) throws SQLException {
        String sql = "SELECT h.id, g.sigla_grupo, h.dia, h.hora_inicio, h.hora_fin, h.aula " +
                     "FROM horario_grupo h " +
                     "JOIN grupo g ON h.id_grupo = g.id " +
                     "WHERE h.id_grupo = ? ORDER BY h.dia";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idGrupo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new String[]{
                            String.valueOf(rs.getInt("id")),
                            rs.getString("sigla_grupo"),
                            rs.getString("dia"),
                            rs.getString("hora_inicio"),
                            rs.getString("hora_fin"),
                            rs.getString("aula")
                    });
                }
            }
        }
        return lista;
    }
}
