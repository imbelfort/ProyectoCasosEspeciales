package Services;

import DAO.DirectorCarreraDAO;
import DAO.EstudianteDAO;
import DAO.PersonalCarreraDAO;
import DAO.UsuarioDAO;
import Database.Conexion;
import Models.DirectorCarrera;
import Models.Estudiante;
import Models.PersonalCarrera;
import Models.Usuario;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class UsuarioService {

    private static final int ITERACIONES = 600000;
    private static final int LONGITUD_HASH = 256;
    private static final int LONGITUD_SAL = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final EstudianteDAO estudianteDAO = new EstudianteDAO();
    private final PersonalCarreraDAO personalDAO = new PersonalCarreraDAO();
    private final DirectorCarreraDAO directorDAO = new DirectorCarreraDAO();
    private final RolService rolService = new RolService();

    @FunctionalInterface
    private interface PerfilOperation {
        void ejecutar(Connection con, int idUsuario) throws SQLException;
    }

    public List<String[]> listarUsuarios() throws SQLException {
        return usuarioDAO.listAll();
    }

    public List<String[]> listarEstudiantes() throws SQLException {
        return estudianteDAO.listAll();
    }

    public List<String[]> listarAcademicos() throws SQLException {
        return personalDAO.listAll();
    }

    public List<String[]> listarDirectores() throws SQLException {
        return directorDAO.listAll();
    }

    public List<String[]> listarCpd() throws SQLException {
        return usuarioDAO.listByRol("CPD");
    }

    public String getComandosUsuario() {
        return """
                COMANDOS CU1 (requieren sesion):
                usuario listar
                estudiantes listar
                academicos listar
                directores listar
                cpd listar
                estudiante agregar nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,registro_universitario,id_carrera,semestre
                estudiante editar id,nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,estado,registro_universitario,id_carrera,semestre
                academico agregar nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,cargo,id_carrera
                academico editar id,nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,estado,cargo,id_carrera
                director agregar nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,id_carrera,gestion_inicio,gestion_fin
                director editar id,nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,estado,id_carrera,gestion_inicio,gestion_fin
                cpd agregar nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password
                cpd editar id,nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,estado

                En ediciones, deja vacio password para conservar la clave actual.
                Los roles se asignan automaticamente en cada alta.
                """;
    }

    public int crearEstudiante(List<String> params) throws SQLException {
        exigirCantidad(params, 11,
                "estudiante agregar nombre,apellidos,ci,email,telefono,direccion," +
                "fecha_nacimiento,password,registro_universitario,id_carrera,semestre");

        String registro = validarRegistroUniversitario(params.get(8));
        int idCarrera = validarEnteroPositivo(params.get(9), "id_carrera");
        int semestre = validarSemestre(params.get(10));

        return crearUsuarioConPerfil(
                params.subList(0, 8),
                "ESTUDIANTE",
                (con, id) -> estudianteDAO.create(
                        con, new Estudiante(id, registro, idCarrera, semestre)));
    }

    public void editarEstudiante(List<String> params) throws SQLException {
        exigirCantidad(params, 13,
                "estudiante editar id,nombre,apellidos,ci,email,telefono,direccion," +
                "fecha_nacimiento,password,estado,registro_universitario,id_carrera,semestre");

        int id = validarEnteroPositivo(params.get(0), "id_estudiante");
        String registro = validarRegistroUniversitario(params.get(10));
        int idCarrera = validarEnteroPositivo(params.get(11), "id_carrera");
        int semestre = validarSemestre(params.get(12));

        actualizarUsuarioConPerfil(
                id,
                params.subList(1, 9),
                params.get(9),
                "ESTUDIANTE",
                (con, usuarioId) -> estudianteDAO.update(
                        con, new Estudiante(usuarioId, registro, idCarrera, semestre)));
    }

    public void crearAcademico(List<String> params) throws SQLException {
        exigirCantidad(params, 10,
                "academico agregar nombre,apellidos,ci,email,telefono,direccion," +
                "fecha_nacimiento,password,cargo,id_carrera");

        String cargo = validarTextoObligatorio(params.get(8), "cargo", 100);
        int idCarrera = validarEnteroPositivo(params.get(9), "id_carrera");

        crearUsuarioConPerfil(
                params.subList(0, 8),
                "PERSONAL_ACADEMICO",
                (con, id) -> personalDAO.create(
                        con, new PersonalCarrera(id, cargo, idCarrera)));
    }

    public void editarAcademico(List<String> params) throws SQLException {
        exigirCantidad(params, 12,
                "academico editar id,nombre,apellidos,ci,email,telefono,direccion," +
                "fecha_nacimiento,password,estado,cargo,id_carrera");

        int id = validarEnteroPositivo(params.get(0), "id_academico");
        String cargo = validarTextoObligatorio(params.get(10), "cargo", 100);
        int idCarrera = validarEnteroPositivo(params.get(11), "id_carrera");

        actualizarUsuarioConPerfil(
                id,
                params.subList(1, 9),
                params.get(9),
                "PERSONAL_ACADEMICO",
                (con, usuarioId) -> personalDAO.update(
                        con, new PersonalCarrera(usuarioId, cargo, idCarrera)));
    }

    public void crearDirector(List<String> params) throws SQLException {
        exigirCantidad(params, 11,
                "director agregar nombre,apellidos,ci,email,telefono,direccion," +
                "fecha_nacimiento,password,id_carrera,gestion_inicio,gestion_fin");

        int idCarrera = validarEnteroPositivo(params.get(8), "id_carrera");
        Date inicio = validarFechaOpcional(params.get(9), "gestion_inicio");
        Date fin = validarFechaOpcional(params.get(10), "gestion_fin");
        validarRangoGestion(inicio, fin);

        crearUsuarioConPerfil(
                params.subList(0, 8),
                "DIRECTOR",
                (con, id) -> directorDAO.create(
                        con, new DirectorCarrera(id, idCarrera, inicio, fin)));
    }

    public void editarDirector(List<String> params) throws SQLException {
        exigirCantidad(params, 13,
                "director editar id,nombre,apellidos,ci,email,telefono,direccion," +
                "fecha_nacimiento,password,estado,id_carrera,gestion_inicio,gestion_fin");

        int id = validarEnteroPositivo(params.get(0), "id_director");
        int idCarrera = validarEnteroPositivo(params.get(10), "id_carrera");
        Date inicio = validarFechaOpcional(params.get(11), "gestion_inicio");
        Date fin = validarFechaOpcional(params.get(12), "gestion_fin");
        validarRangoGestion(inicio, fin);

        actualizarUsuarioConPerfil(
                id,
                params.subList(1, 9),
                params.get(9),
                "DIRECTOR",
                (con, usuarioId) -> directorDAO.update(
                        con, new DirectorCarrera(usuarioId, idCarrera, inicio, fin)));
    }

    public void crearCpd(List<String> params) throws SQLException {
        exigirCantidad(params, 8,
                "cpd agregar nombre,apellidos,ci,email,telefono,direccion," +
                "fecha_nacimiento,password");

        crearUsuarioConPerfil(params, "CPD", (con, id) -> {});
    }

    public void editarCpd(List<String> params) throws SQLException {
        exigirCantidad(params, 10,
                "cpd editar id,nombre,apellidos,ci,email,telefono,direccion," +
                "fecha_nacimiento,password,estado");

        int id = validarEnteroPositivo(params.get(0), "id_cpd");
        actualizarUsuarioConPerfil(
                id,
                params.subList(1, 9),
                params.get(9),
                "CPD",
                (con, usuarioId) -> {});
    }

    private int crearUsuarioConPerfil(
            List<String> datosUsuario,
            String nombreRol,
            PerfilOperation guardarPerfil
    ) throws SQLException {
        Usuario usuario = crearModeloUsuario(datosUsuario);

        try (Connection con = obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                if (usuarioDAO.existsByCi(usuario.getCi())) {
                    throw new IllegalArgumentException(
                            "Ya existe un usuario registrado con el CI indicado.");
                }
                if (usuarioDAO.existsByEmail(usuario.getEmail())) {
                    throw new IllegalArgumentException(
                            "Ya existe un usuario registrado con el correo indicado.");
                }

                usuarioDAO.create(con, usuario);
                if (usuario.getIdUsuario() <= 0) {
                    throw new SQLException(
                            "La base de datos no devolvio el ID del usuario creado.");
                }
                rolService.asignarRolAutomaticamente(
                        con, usuario.getIdUsuario(), nombreRol);
                guardarPerfil.ejecutar(con, usuario.getIdUsuario());
                con.commit();
                return usuario.getIdUsuario();
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                if (ex instanceof SQLException sqlEx) {
                    throw traducirErrorBD(sqlEx);
                }
                throw ex;
            }
        }
    }

    private void actualizarUsuarioConPerfil(
            int id,
            List<String> datosUsuario,
            String estado,
            String nombreRol,
            PerfilOperation actualizarPerfil
    ) throws SQLException {
        validarId(id);
        Usuario usuario = crearModeloUsuarioEdicion(id, datosUsuario, estado);

        try (Connection con = obtenerConexion()) {
            con.setAutoCommit(false);
            try {
                String[] actual = usuarioDAO.readById(id);
                if (actual == null) {
                    throw new IllegalArgumentException(
                            "No existe un usuario con el ID: " + id);
                }
                if (!rolService.tieneRol(con, id, nombreRol)) {
                    throw new IllegalArgumentException(
                            "El usuario indicado no tiene el rol " + nombreRol + ".");
                }
                if (usuarioDAO.existsByCi(usuario.getCi(), id)) {
                    throw new IllegalArgumentException(
                            "El CI ya pertenece a otro usuario.");
                }
                if (usuarioDAO.existsByEmail(usuario.getEmail(), id)) {
                    throw new IllegalArgumentException(
                            "El correo ya pertenece a otro usuario.");
                }

                usuario.setFotoPerfil(actual[7]);
                usuarioDAO.update(con, usuario);
                actualizarPerfil.ejecutar(con, id);
                con.commit();
            } catch (SQLException | RuntimeException ex) {
                rollback(con, ex);
                if (ex instanceof SQLException sqlEx) {
                    throw traducirErrorBD(sqlEx);
                }
                throw ex;
            }
        }
    }

    private Usuario crearModeloUsuario(List<String> datos) {
        exigirCantidad(datos, 8,
                "nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password");

        Usuario usuario = new Usuario();
        usuario.setNombre(validarNombre(datos.get(0)));
        usuario.setApellidos(validarApellidos(datos.get(1)));
        usuario.setCi(validarCI(datos.get(2)));
        usuario.setEmail(validarEmail(datos.get(3)));
        usuario.setTelefono(validarTelefono(datos.get(4)));
        usuario.setDireccion(validarDireccion(datos.get(5)));
        usuario.setFechaNacimiento(validarFechaNacimiento(datos.get(6)));
        usuario.setContrasena(generarHash(validarContrasena(datos.get(7))));
        usuario.setEstado("ACTIVO");
        return usuario;
    }

    private Usuario crearModeloUsuarioEdicion(
            int id,
            List<String> datos,
            String estado
    ) {
        exigirCantidad(datos, 8,
                "nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(id);
        usuario.setNombre(validarNombre(datos.get(0)));
        usuario.setApellidos(validarApellidos(datos.get(1)));
        usuario.setCi(validarCI(datos.get(2)));
        usuario.setEmail(validarEmail(datos.get(3)));
        usuario.setTelefono(validarTelefono(datos.get(4)));
        usuario.setDireccion(validarDireccion(datos.get(5)));
        usuario.setFechaNacimiento(validarFechaNacimiento(datos.get(6)));
        usuario.setContrasena(datos.get(7) == null || datos.get(7).isBlank()
                ? null
                : generarHash(validarContrasena(datos.get(7))));
        usuario.setEstado(validarEstado(estado));
        return usuario;
    }

    public boolean verificarContrasena(int idUsuario, String password)
            throws SQLException {
        validarId(idUsuario);
        if (password == null || password.isEmpty()) {
            return false;
        }

        String almacenado = usuarioDAO.obtenerHashContrasena(idUsuario);
        if (almacenado == null || !almacenado.startsWith("PBKDF2-SHA256:")) {
            return false;
        }

        try {
            String[] partes = almacenado.split(":");
            if (partes.length != 4) {
                return false;
            }

            int iteraciones = Integer.parseInt(partes[1]);
            if (iteraciones < 10000 || iteraciones > 2000000) {
                return false;
            }

            byte[] sal = Base64.getDecoder().decode(partes[2]);
            byte[] esperado = Base64.getDecoder().decode(partes[3]);
            if (sal.length != LONGITUD_SAL
                    || esperado.length != LONGITUD_HASH / 8) {
                return false;
            }

            char[] caracteres = password.toCharArray();
            PBEKeySpec spec = new PBEKeySpec(
                    caracteres, sal, iteraciones, LONGITUD_HASH);
            try {
                byte[] obtenido = SecretKeyFactory
                        .getInstance("PBKDF2WithHmacSHA256")
                        .generateSecret(spec)
                        .getEncoded();
                return MessageDigest.isEqual(esperado, obtenido);
            } finally {
                spec.clearPassword();
                Arrays.fill(caracteres, '\0');
            }
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            return false;
        }
    }

    private Connection obtenerConexion() throws SQLException {
        Connection con = Conexion.getInstancia().EstablecerConexion();
        if (con == null) {
            throw new SQLException("No fue posible conectar con PostgreSQL.");
        }
        return con;
    }

    private void rollback(Connection con, Throwable original) {
        try {
            con.rollback();
        } catch (SQLException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }

    private void exigirCantidad(List<String> params, int esperada, String formato) {
        if (params == null || params.size() != esperada) {
            throw new IllegalArgumentException("Formato: " + formato);
        }
    }

    private String validarNombre(String valor) {
        String nombre = normalizarTexto(valor);
        if (nombre.length() < 2 || nombre.length() > 100
                || !nombre.matches("^[\\p{L}]+(?: [\\p{L}]+)*$")) {
            throw new IllegalArgumentException(
                    "El nombre debe contener entre 2 y 100 letras y espacios.");
        }
        return nombre;
    }

    private String validarApellidos(String valor) {
        String apellidos = normalizarTexto(valor);
        if (apellidos.length() < 2 || apellidos.length() > 100
                || !apellidos.matches("^[\\p{L}]+(?: [\\p{L}]+)*$")) {
            throw new IllegalArgumentException(
                    "Los apellidos deben contener entre 2 y 100 letras y espacios.");
        }
        return apellidos;
    }

    private String validarCI(String valor) {
        String ci = limpiar(valor).toUpperCase(Locale.ROOT);
        if (!ci.matches("^[0-9]{5,12}(-[A-Z0-9]{1,3})?$")) {
            throw new IllegalArgumentException("El CI no es valido.");
        }
        return ci;
    }

    private String validarEmail(String valor) {
        String email = limpiar(valor).toLowerCase(Locale.ROOT);
        if (email.length() > 150
                || !email.matches("^[a-z0-9_+%-]+(?:\\.[a-z0-9_+%-]+)*"
                        + "@[a-z0-9-]+(?:\\.[a-z0-9-]+)+")
                || email.contains("..")) {
            throw new IllegalArgumentException(
                    "El correo electronico no es valido. Verifique el formato "
                    + "usuario@dominio.com y que no haya un punto antes de @.");
        }
        return email;
    }

    private String validarTelefono(String valor) {
        String telefono = limpiar(valor);
        if (!telefono.matches("^(?:\\+591[67][0-9]{7}|[0-9]{8})$")) {
            throw new IllegalArgumentException(
                    "El telefono debe tener 8 digitos o formato +591 seguido de un celular.");
        }
        return telefono;
    }

    private String validarDireccion(String valor) {
        String direccion = normalizarTexto(valor);
        if (direccion.length() < 5 || direccion.length() > 500) {
            throw new IllegalArgumentException(
                    "La direccion debe contener entre 5 y 500 caracteres.");
        }
        return direccion;
    }

    private Date validarFechaNacimiento(String valor) {
        LocalDate fecha = parsearFecha(valor, "fecha_nacimiento");
        if (fecha.isAfter(LocalDate.now())
                || fecha.isBefore(LocalDate.of(1900, 1, 1))) {
            throw new IllegalArgumentException("La fecha de nacimiento no es valida.");
        }
        return Date.valueOf(fecha);
    }

    private Date validarFechaOpcional(String valor, String campo) {
        return limpiar(valor).isEmpty()
                ? null
                : Date.valueOf(parsearFecha(valor, campo));
    }

    private LocalDate parsearFecha(String valor, String campo) {
        try {
            return LocalDate.parse(limpiar(valor));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(
                    "El campo " + campo + " debe tener formato yyyy-MM-dd.");
        }
    }

    private void validarRangoGestion(Date inicio, Date fin) {
        if (inicio != null && fin != null && fin.before(inicio)) {
            throw new IllegalArgumentException(
                    "gestion_fin no puede ser anterior a gestion_inicio.");
        }
    }

    private String validarContrasena(String valor) {
        if (valor == null || valor.length() < 12 || valor.length() > 128
                || valor.contains(",")
                || valor.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(
                    "La contrasena debe tener entre 12 y 128 caracteres, sin comas.");
        }
        return valor;
    }

    private String validarEstado(String valor) {
        String estado = limpiar(valor).toUpperCase(Locale.ROOT);
        if (!List.of("ACTIVO", "INACTIVO", "BLOQUEADO").contains(estado)) {
            throw new IllegalArgumentException(
                    "El estado debe ser ACTIVO, INACTIVO o BLOQUEADO.");
        }
        return estado;
    }

    private String validarRegistroUniversitario(String valor) {
        String registro = limpiar(valor);
        if (!registro.matches("^[0-9]{6,15}$")) {
            throw new IllegalArgumentException(
                    "El registro universitario debe tener entre 6 y 15 digitos.");
        }
        return registro;
    }

    private int validarSemestre(String valor) {
        int semestre = validarEnteroPositivo(valor, "semestre");
        if (semestre > 12) {
            throw new IllegalArgumentException("El semestre debe estar entre 1 y 12.");
        }
        return semestre;
    }

    private int validarEnteroPositivo(String valor, String campo) {
        try {
            int numero = Integer.parseInt(limpiar(valor));
            if (numero <= 0) {
                throw new NumberFormatException();
            }
            return numero;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    campo + " debe ser un numero entero positivo.");
        }
    }

    private String validarTextoObligatorio(
            String valor,
            String campo,
            int longitudMaxima
    ) {
        String texto = normalizarTexto(valor);
        if (texto.isEmpty() || texto.length() > longitudMaxima) {
            throw new IllegalArgumentException(
                    campo + " es obligatorio y no debe superar "
                            + longitudMaxima + " caracteres.");
        }
        return texto;
    }

    private String generarHash(String password) {
        byte[] sal = new byte[LONGITUD_SAL];
        RANDOM.nextBytes(sal);
        char[] caracteres = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(
                caracteres, sal, ITERACIONES, LONGITUD_HASH);
        try {
            byte[] hash = SecretKeyFactory
                    .getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
            return "PBKDF2-SHA256:" + ITERACIONES + ":"
                    + Base64.getEncoder().encodeToString(sal) + ":"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException(
                    "No fue posible proteger la contrasena.", ex);
        } finally {
            spec.clearPassword();
            Arrays.fill(caracteres, '\0');
        }
    }

    private void validarId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                    "El ID debe ser un numero entero positivo.");
        }
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String normalizarTexto(String valor) {
        return limpiar(valor).replaceAll("\\s+", " ");
    }

    private SQLException traducirErrorBD(SQLException ex) {
        return switch (ex.getSQLState() == null ? "" : ex.getSQLState()) {
            case "23505" -> new SQLException(
                    "Ya existe un usuario con alguno de los datos unicos indicados.",
                    ex.getSQLState(), ex);
            case "23502" -> new SQLException(
                    "Faltan datos obligatorios para completar la operacion.",
                    ex.getSQLState(), ex);
            case "23503" -> new SQLException(
                    "No existe una carrera o referencia relacionada indicada.",
                    ex.getSQLState(), ex);
            case "22001" -> new SQLException(
                    "Uno de los datos supera la longitud permitida.",
                    ex.getSQLState(), ex);
            default -> new SQLException(
                    "No fue posible completar la operacion en la base de datos.",
                    ex.getSQLState(), ex);
        };
    }
}
