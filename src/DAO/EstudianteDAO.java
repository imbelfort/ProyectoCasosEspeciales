package DAO;

import Database.Conexion;
import Models.Estudiante;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstudianteDAO {

    public void create(Estudiante estudiante) throws SQLException {
        try (Connection con = Conexion.getInstancia().EstablecerConexion()) {
            if (con == null) {
                throw new SQLException("No se pudo conectar con PostgreSQL.");
            }
            create(con, estudiante);
        }
    }

    public void create(Connection con, Estudiante estudiante)
            throws SQLException {
        String sql = "INSERT INTO estudiantes (id_estudiante, registro_universitario, carrera_id, semestre_actual) " +
                     "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, estudiante.getIdEstudiante());
            ps.setString(2, estudiante.getRegistroUniversitario());
            ps.setInt(3, estudiante.getIdCarrera());
            ps.setInt(4, estudiante.getSemestreActual());
            ps.executeUpdate();
        }
    }

    public void update(Connection con, Estudiante estudiante)
            throws SQLException {
        String sql = "UPDATE estudiantes SET registro_universitario = ?, " +
                "carrera_id = ?, semestre_actual = ? WHERE id_estudiante = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estudiante.getRegistroUniversitario());
            ps.setInt(2, estudiante.getIdCarrera());
            ps.setInt(3, estudiante.getSemestreActual());
            ps.setInt(4, estudiante.getIdEstudiante());
            if (ps.executeUpdate() == 0) {
                throw new IllegalArgumentException(
                        "No existe el perfil del estudiante indicado.");
            }
        }
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT e.id_estudiante, u.nombre, u.apellidos, u.ci, u.email, " +
                     "e.registro_universitario, c.nombre AS carrera, e.semestre_actual " +
                     "FROM estudiantes e " +
                     "JOIN usuario u ON e.id_estudiante = u.id_usuario " +
                     "JOIN carrera c ON e.carrera_id = c.id " +
                     "ORDER BY e.id_estudiante";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id_estudiante")),
                        rs.getString("nombre") != null ? rs.getString("nombre") : "",
                        rs.getString("apellidos") != null ? rs.getString("apellidos") : "",
                        rs.getString("ci") != null ? rs.getString("ci") : "",
                        rs.getString("email") != null ? rs.getString("email") : "",
                        rs.getString("registro_universitario") != null ? rs.getString("registro_universitario") : "",
                        rs.getString("carrera") != null ? rs.getString("carrera") : "",
                        String.valueOf(rs.getInt("semestre_actual"))
                });
            }
        }
        return lista;
    }

    public Integer findIdByEmail(String email) throws SQLException {
        if (email == null || email.trim().isEmpty()) return null;
        String sql = "SELECT e.id_estudiante FROM estudiantes e " +
                     "JOIN usuario u ON e.id_estudiante = u.id_usuario " +
                     "WHERE LOWER(u.email) = LOWER(?) LIMIT 1";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_estudiante");
                }
            }
        }
        return null;
    }

    public Integer findIdByRegistro(String registro) throws SQLException {
        if (registro == null || registro.trim().isEmpty()) return null;
        String sql = "SELECT id_estudiante FROM estudiantes " +
                     "WHERE LOWER(registro_universitario) = LOWER(?) LIMIT 1";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, registro.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_estudiante");
                }
            }
        }
        return null;
    }

    public boolean autenticar(String registro, String password) throws SQLException {
        if (registro == null || password == null) return false;
        String sql = "SELECT 1 FROM estudiantes e " +
                     "JOIN usuario u ON e.id_estudiante = u.id_usuario " +
                     "WHERE LOWER(e.registro_universitario) = LOWER(?) AND u.password = ? LIMIT 1";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, registro.trim());
            ps.setString(2, password.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
