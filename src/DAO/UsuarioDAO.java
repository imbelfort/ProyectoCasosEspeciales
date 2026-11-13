
package DAO;

import Database.Conexion;
import Models.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public void create(Connection con, Usuario usuario) throws SQLException {

        String sql = "INSERT INTO usuario " +
                "(nombre, apellidos, ci, email, telefono, " +
                "direccion, password, foto_perfil, " +
                "fecha_nacimiento, estado) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = con.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellidos());
            ps.setString(3, usuario.getCi());
            ps.setString(4, usuario.getEmail());
            ps.setString(5, usuario.getTelefono());
            ps.setString(6, usuario.getDireccion());
            ps.setString(7, usuario.getContrasena());
            ps.setString(8, usuario.getFotoPerfil());
            ps.setDate(9, usuario.getFechaNacimiento());
            ps.setString(10, usuario.getEstado());

            int filas = ps.executeUpdate();

            if (filas == 0) {
                throw new SQLException(
                        "No se pudo registrar el usuario."
                );
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    usuario.setIdUsuario(rs.getInt(1));
                }
            }
        }
    }


    public String[] readById(int id) throws SQLException {

        String sql = "SELECT id_usuario, nombre, apellidos, " +
                "ci, email, telefono, direccion, foto_perfil, " +
                "estado, fecha_nacimiento, fecha_creacion, " +
                "fecha_modificacion " +
                "FROM usuario WHERE id_usuario = ?";

        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        }

        return null;
    }


    public List<String[]> listAll() throws SQLException {

        String sql = "SELECT id_usuario, nombre, apellidos, " +
                "ci, email, telefono, direccion, foto_perfil, " +
                "estado, fecha_nacimiento, fecha_creacion, " +
                "fecha_modificacion " +
                "FROM usuario ORDER BY id_usuario";

        List<String[]> usuarios = new ArrayList<>();

        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        }

        return usuarios;
    }

    public List<String[]> listByRol(String nombreRol)
            throws SQLException {

        String sql = "SELECT u.id_usuario, u.nombre, u.apellidos, " +
                "u.ci, u.email, u.telefono, u.direccion, " +
                "u.estado, u.fecha_nacimiento, " +
                "r.nombre AS rol " +
                "FROM usuario u " +
                "JOIN usuario_roles ur ON ur.id_usuario = u.id_usuario " +
                "JOIN roles r ON r.id = ur.id_rol " +
                "WHERE LOWER(r.nombre) = LOWER(?) " +
                "AND ur.estado = 'ACTIVO' " +
                "AND r.estado = 'ACTIVO' " +
                "ORDER BY u.id_usuario";

        List<String[]> usuarios = new ArrayList<>();

        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombreRol.trim());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(new String[]{
                        String.valueOf(rs.getInt("id_usuario")),
                        valor(rs.getString("nombre")),
                        valor(rs.getString("apellidos")),
                        valor(rs.getString("ci")),
                        valor(rs.getString("email")),
                        valor(rs.getString("telefono")),
                        valor(rs.getString("direccion")),
                        valor(rs.getString("estado")),
                        rs.getDate("fecha_nacimiento") != null ?
                                rs.getDate("fecha_nacimiento").toString() : "",
                        valor(rs.getString("rol"))
                    });
                }
            }
        }

        return usuarios;
    }


    public void update(Connection con, Usuario usuario)
            throws SQLException {

        String sql = "UPDATE usuario SET " +
                "nombre = ?, " +
                "apellidos = ?, " +
                "ci = ?, " +
                "email = ?, " +
                "telefono = ?, " +
                "direccion = ?, " +
                "password = COALESCE(?, password), " +
                "foto_perfil = ?, " +
                "fecha_nacimiento = ?, " +
                "estado = ?, " +
                "fecha_modificacion = CURRENT_TIMESTAMP " +
                "WHERE id_usuario = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getApellidos());
            ps.setString(3, usuario.getCi());
            ps.setString(4, usuario.getEmail());
            ps.setString(5, usuario.getTelefono());
            ps.setString(6, usuario.getDireccion());
            ps.setString(7, usuario.getContrasena());
            ps.setString(8, usuario.getFotoPerfil());
            ps.setDate(9, usuario.getFechaNacimiento());
            ps.setString(10, usuario.getEstado());
            ps.setInt(11, usuario.getIdUsuario());

            int filas = ps.executeUpdate();

            if (filas == 0) {
                throw new IllegalArgumentException(
                        "No existe un usuario con el ID: " +
                        usuario.getIdUsuario()
                );
            }
        }
    }



    public boolean existsByEmail(String email)
            throws SQLException {
        return existsByEmail(email, 0);
    }

    public boolean existsByEmail(String email, int excluirId)
            throws SQLException {

        String sql = "SELECT 1 FROM usuario " +
                "WHERE LOWER(email) = LOWER(?) " +
                "AND id_usuario <> ?";

        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setInt(2, excluirId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    public boolean existsByCi(String ci)
            throws SQLException {
        return existsByCi(ci, 0);
    }

    public boolean existsByCi(String ci, int excluirId)
            throws SQLException {

        String sql = "SELECT 1 FROM usuario " +
                "WHERE UPPER(ci) = UPPER(?) " +
                "AND id_usuario <> ?";

        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ci);
            ps.setInt(2, excluirId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }


    public Integer obtenerIdPorEmail(String email)
            throws SQLException {

        String sql = "SELECT id_usuario FROM usuario " +
                "WHERE LOWER(email) = LOWER(?) " +
                "AND estado = 'ACTIVO'";

        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_usuario");
                }
            }
        }

        return null;
    }


    public String obtenerHashContrasena(int id)
            throws SQLException {

        String sql = "SELECT password FROM usuario " +
                "WHERE id_usuario = ?";

        try (Connection con = obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
            }
        }

        return null;
    }


    private String[] mapResultSetToUsuario(ResultSet rs)
            throws SQLException {

        Date fecha = rs.getDate("fecha_nacimiento");
        Timestamp creado = rs.getTimestamp("fecha_creacion");
        Timestamp modificado =
                rs.getTimestamp("fecha_modificacion");

        return new String[]{
                String.valueOf(rs.getInt("id_usuario")),
                valor(rs.getString("nombre")),
                valor(rs.getString("apellidos")),
                valor(rs.getString("ci")),
                valor(rs.getString("email")),
                valor(rs.getString("telefono")),
                valor(rs.getString("direccion")),
                valor(rs.getString("foto_perfil")),
                valor(rs.getString("estado")),
                "", // Nunca exponer contrasenas
                fecha == null ? "" : fecha.toString(),
                creado == null ? "" : creado.toString(),
                modificado == null ? "" : modificado.toString()
        };
    }

    private String valor(String texto) {
        return texto == null ? "" : texto;
    }

    private Connection obtenerConexion() throws SQLException {

        Connection con =
                Conexion.getInstancia().EstablecerConexion();

        if (con == null) {
            throw new SQLException(
                    "No fue posible establecer conexion con PostgreSQL."
            );
        }

        return con;
    }
}
