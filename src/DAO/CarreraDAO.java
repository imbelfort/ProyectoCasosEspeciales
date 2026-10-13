package DAO;

import Database.Conexion;
import Models.Carrera;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CarreraDAO {

    public void create(Carrera carrera) throws SQLException {
        String sql = "INSERT INTO carrera (codigo, nombre, facultad) VALUES (?, ?, ?)";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, carrera.getCodigo());
            ps.setString(2, carrera.getNombre());
            ps.setString(3, carrera.getFacultad());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    carrera.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT id, codigo, nombre, facultad FROM carrera ORDER BY id";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("codigo") != null ? rs.getString("codigo") : "",
                        rs.getString("nombre") != null ? rs.getString("nombre") : "",
                        rs.getString("facultad") != null ? rs.getString("facultad") : ""
                });
            }
        }
        return lista;
    }
}
