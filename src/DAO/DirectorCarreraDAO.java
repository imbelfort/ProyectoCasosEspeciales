package DAO;

import Database.Conexion;
import Models.DirectorCarrera;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DirectorCarreraDAO {

    public void create(DirectorCarrera director) throws SQLException {
        String sql = "INSERT INTO director_carrera (id_director, carrera_id, gestion_inicio, gestion_fin) " +
                     "VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, director.getIdDirector());
            ps.setInt(2, director.getIdCarrera());
            ps.setDate(3, director.getGestionInicio());
            ps.setDate(4, director.getGestionFin());
            ps.executeUpdate();
        }
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT d.id_director, u.nombre, u.apellidos, u.ci, u.email, c.nombre AS carrera, " +
                     "d.gestion_inicio, d.gestion_fin " +
                     "FROM director_carrera d " +
                     "JOIN usuario u ON d.id_director = u.id_usuario " +
                     "JOIN carrera c ON d.carrera_id = c.id " +
                     "ORDER BY d.id_director";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id_director")),
                        rs.getString("nombre") != null ? rs.getString("nombre") : "",
                        rs.getString("apellidos") != null ? rs.getString("apellidos") : "",
                        rs.getString("ci") != null ? rs.getString("ci") : "",
                        rs.getString("email") != null ? rs.getString("email") : "",
                        rs.getString("carrera") != null ? rs.getString("carrera") : "",
                        rs.getDate("gestion_inicio") != null ? rs.getDate("gestion_inicio").toString() : "",
                        rs.getDate("gestion_fin") != null ? rs.getDate("gestion_fin").toString() : ""
                });
            }
        }
        return lista;
    }
}
