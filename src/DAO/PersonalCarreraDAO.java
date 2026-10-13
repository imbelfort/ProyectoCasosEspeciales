package DAO;

import Database.Conexion;
import Models.PersonalCarrera;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonalCarreraDAO {

    public void create(PersonalCarrera personal) throws SQLException {
        String sql = "INSERT INTO personal_carrera (id_personal, cargo, carrera_id) VALUES (?, ?, ?)";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, personal.getIdPersonal());
            ps.setString(2, personal.getCargo());
            ps.setInt(3, personal.getIdCarrera());
            ps.executeUpdate();
        }
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT p.id_personal, u.nombre, u.apellidos, u.ci, u.email, p.cargo, c.nombre AS carrera " +
                     "FROM personal_carrera p " +
                     "JOIN usuario u ON p.id_personal = u.id_usuario " +
                     "JOIN carrera c ON p.carrera_id = c.id " +
                     "ORDER BY p.id_personal";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id_personal")),
                        rs.getString("nombre") != null ? rs.getString("nombre") : "",
                        rs.getString("apellidos") != null ? rs.getString("apellidos") : "",
                        rs.getString("ci") != null ? rs.getString("ci") : "",
                        rs.getString("email") != null ? rs.getString("email") : "",
                        rs.getString("cargo") != null ? rs.getString("cargo") : "",
                        rs.getString("carrera") != null ? rs.getString("carrera") : ""
                });
            }
        }
        return lista;
    }
}
