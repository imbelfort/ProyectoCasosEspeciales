package DAO;

import Database.Conexion;
import Models.Materia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MateriaDAO {

    public void create(Materia materia) throws SQLException {
        String sql = "INSERT INTO materia (sigla, nombre, nivel_semestre, creditos) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, materia.getSigla());
            ps.setString(2, materia.getNombre());
            ps.setInt(3, materia.getNivelSemestre());
            ps.setInt(4, materia.getCreditos());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    materia.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT id, sigla, nombre, nivel_semestre, creditos FROM materia ORDER BY nivel_semestre, sigla";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("sigla") != null ? rs.getString("sigla") : "",
                        rs.getString("nombre") != null ? rs.getString("nombre") : "",
                        String.valueOf(rs.getInt("nivel_semestre")),
                        String.valueOf(rs.getInt("creditos"))
                });
            }
        }
        return lista;
    }
}
