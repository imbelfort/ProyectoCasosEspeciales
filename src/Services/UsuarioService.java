package Services;

import DAO.UsuarioDAO;
import Models.Usuario;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class UsuarioService {

    private UsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public void crearUsuario(List<String> params) throws SQLException {
        crearUsuario(params, null);
    }

    public void crearUsuario(List<String> params, String fotoPerfil) throws SQLException {
        if (params.size() < 8) {
            throw new IllegalArgumentException(
                    "Cantidad de parametros invalida. Formato: usuario agregar " +
                    "nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password [foto adjunta]"
            );
        }

        String ci = params.get(2) != null ? params.get(2).trim().toUpperCase() : null;
        String email = params.get(3) != null ? params.get(3).trim().toLowerCase() : null;
        Date fechaNacimiento = parseFechaNacimiento(params.get(6));

        if (ci == null || ci.trim().isEmpty()) {
            throw new IllegalArgumentException("El CI es obligatorio para Usuario");
        }

        if (!esEmailValido(email)) {
            throw new SQLException("Formato de email invalido: " + email);
        }

        if (usuarioDAO.existsByCi(ci)) {
            throw new SQLException("Ya existe un usuario con el CI: " + ci);
        }

        if (usuarioDAO.existsByEmail(email)) {
            throw new SQLException("Ya existe un usuario con el email: " + email);
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(params.get(0).trim());
        usuario.setApellidos(params.get(1).trim());
        usuario.setCi(ci);
        usuario.setEmail(email);
        usuario.setTelefono(params.get(4).trim());
        usuario.setDireccion(params.get(5).trim());
        usuario.setFechaNacimiento(fechaNacimiento);
        usuario.setContrasena(params.get(7).trim());
        usuario.setFotoPerfil(fotoPerfil != null && !fotoPerfil.trim().isEmpty() ? fotoPerfil : null);
        usuario.setEstado("ACTIVO");

        usuarioDAO.create(usuario);
    }

    public String[] obtenerUsuarioPorId(int id) throws SQLException {
        return usuarioDAO.readById(id);
    }

    public List<String[]> listarUsuarios() throws SQLException {
        return usuarioDAO.listAll();
    }

    public void actualizarUsuario(List<String> params) throws SQLException {
        actualizarUsuario(params, null);
    }

    public void actualizarUsuario(List<String> params, String fotoPerfil) throws SQLException {
        if (params.size() < 10) {
            throw new IllegalArgumentException(
                    "Parametros insuficientes. Formato: usuario modificar " +
                    "id,nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,estado"
            );
        }

        int idUsuario = Integer.parseInt(params.get(0).trim());
        String ci = params.get(3) != null ? params.get(3).trim().toUpperCase() : null;
        String email = params.get(4) != null ? params.get(4).trim().toLowerCase() : null;

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(idUsuario);
        usuario.setNombre(params.get(1).trim());
        usuario.setApellidos(params.get(2).trim());
        usuario.setCi(ci);
        usuario.setEmail(email);
        usuario.setTelefono(params.get(5).trim());
        usuario.setDireccion(params.get(6).trim());
        usuario.setFechaNacimiento(parseFechaNacimiento(params.get(7)));
        usuario.setContrasena(params.get(8).trim());
        usuario.setEstado(params.get(9).trim().toUpperCase());

        if (fotoPerfil == null || fotoPerfil.trim().isEmpty()) {
            String[] actual = usuarioDAO.readById(idUsuario);
            if (actual != null && actual.length > 7) {
                usuario.setFotoPerfil(actual[7]);
            }
        } else {
            usuario.setFotoPerfil(fotoPerfil);
        }

        usuarioDAO.update(usuario);
    }

    public void eliminarUsuario(int id) throws SQLException {
        usuarioDAO.delete(id);
    }

    public String getComandosUsuario() {
        return "Comandos CU1 - Gestión de Usuarios:\n" +
                "- usuario listar\n" +
                "- usuario agregar nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password [foto adjunta]\n" +
                "- usuario modificar id,nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,estado [foto adjunta]\n" +
                "- usuario eliminar id";
    }

    private boolean esEmailValido(String email) {
        String patron = "^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
        return email != null && email.matches(patron);
    }

    private Date parseFechaNacimiento(String valor) throws SQLException {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }
        try {
            return Date.valueOf(valor.trim());
        } catch (IllegalArgumentException ex) {
            throw new SQLException("Formato de fecha_nacimiento invalido: " + valor + " (usa yyyy-MM-dd)");
        }
    }
}