
package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.security.SecureRandom;
import java.security.GeneralSecurityException;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class InsertarDatosDB {

    private static final SecureRandom RANDOM =
            new SecureRandom();

    public static void main(String[] args) {

        try {
            insertarDatosIniciales();

        } catch (Exception e) {
            System.err.println(
                "[ERROR] No se pudieron insertar los datos.");
            e.printStackTrace();
        }
    }

    public static void insertarDatosIniciales()
            throws SQLException {

        Connection con = Conexion.getInstancia()
                .EstablecerConexion();

        if (con == null) {
            throw new SQLException(
                "No se pudo conectar con PostgreSQL.");
        }

        try (con) {

            con.setAutoCommit(false);

            try {

                // =====================================
                // 1. ROLES
                // =====================================

                insertar(con,
                    "INSERT INTO roles(nombre,descripcion) " +
                    "VALUES (?,?) ON CONFLICT(nombre) DO NOTHING",
                    "DIRECTOR", "Director de Carrera"
                );

                insertar(con,
                    "INSERT INTO roles(nombre,descripcion) " +
                    "VALUES (?,?) ON CONFLICT(nombre) DO NOTHING",
                    "PERSONAL_ACADEMICO",
                    "Personal academico y administrativo"
                );

                insertar(con,
                    "INSERT INTO roles(nombre,descripcion) " +
                    "VALUES (?,?) ON CONFLICT(nombre) DO NOTHING",
                    "ESTUDIANTE", "Estudiante universitario"
                );

                insertar(con,
                    "INSERT INTO roles(nombre,descripcion) " +
                    "VALUES (?,?) ON CONFLICT(nombre) DO NOTHING",
                    "CPD", "Centro de Procesamiento de Datos"
                );

                // =====================================
                // 2. CARRERAS
                // =====================================

                insertarCarrera(con,
                    "187-3", "Ingenieria en Sistemas");

                insertarCarrera(con,
                    "187-4", "Ingenieria Informatica");

                insertarCarrera(con,
                    "187-5",
                    "Ingenieria en Redes y Telecomunicaciones");

                // =====================================
                // 3. PERIODO ACADEMICO
                // =====================================

                insertar(con,
                    "INSERT INTO periodos " +
                    "(gestion,fecha_inicio,fecha_fin,estado) " +
                    "VALUES (?,?,?,'ACTIVO') " +
                    "ON CONFLICT(gestion) DO NOTHING",
                    "2-2026",
                    java.sql.Date.valueOf("2026-08-01"),
                    java.sql.Date.valueOf("2026-12-15")
                );

                // =====================================
                // 4. MATERIAS
                // =====================================

                insertarMateria(con,
                    "INF210", "Sistemas de Informacion I", 4, 5);

                insertarMateria(con,
                    "INF220", "Estructuras de Datos II", 4, 5);

                insertarMateria(con,
                    "MAT101", "Calculo I", 1, 5);

                insertarMateria(con,
                    "INF110", "Introduccion a la Informatica", 1, 4);

                insertarMateria(con,
                    "INF310", "Sistemas de Informacion II", 6, 5);

                // =====================================
                // 5. IDENTIFICADORES ACADEMICOS
                // =====================================

                int idCarrera = buscarId(
                    con, "carrera", "codigo", "187-3");

                int idPeriodo = buscarId(
                    con, "periodos", "gestion", "2-2026");

                // =====================================
                // 6. ASIGNAR MATERIAS A CARRERA
                // =====================================

                for (String sigla : List.of(
                    "INF210", "INF220", "MAT101",
                    "INF110", "INF310")) {

                    int idMateria = buscarId(
                        con, "materia", "sigla", sigla);

                    insertar(con,
                        "INSERT INTO materia_carrera " +
                        "(id_materia,id_carrera) VALUES (?,?) " +
                        "ON CONFLICT DO NOTHING",
                        idMateria, idCarrera
                    );
                }

                // =====================================
                // 7. GRUPOS
                // =====================================

                int grupoSA = insertarGrupo(
                    con, "INF210", idPeriodo, "SA",
                    "Ing. Juan Perez", 45, 5, 5);

                int grupoSC = insertarGrupo(
                    con, "INF210", idPeriodo, "SC",
                    "Ing. Evans Balcazar", 40, 2, 5);

                int grupoSB = insertarGrupo(
                    con, "INF220", idPeriodo, "SB",
                    "Ing. Mario Lopez", 45, 8, 5);

                // =====================================
                // 8. HORARIOS DE EJEMPLO
                // =====================================

                insertarHorario(
                    con, grupoSA, "LUNES",
                    "07:00", "09:00", "LAB-1");

                insertarHorario(
                    con, grupoSA, "MIERCOLES",
                    "07:00", "09:00", "LAB-1");

                insertarHorario(
                    con, grupoSC, "MARTES",
                    "09:00", "11:00", "LAB-2");

                insertarHorario(
                    con, grupoSB, "LUNES",
                    "09:00", "11:00", "LAB-3");

                // =====================================
                // 9. USUARIO DIRECTOR
                // =====================================

                Integer idDirector = buscarUsuario(
                    con, "director@tecnoweb.org.bo");

                if (idDirector == null) {

                    String password = obtenerPasswordInicial(
                        "FICCT_DIRECTOR_PASSWORD");

                    idDirector = insertarUsuario(
                        con,
                        "Evans",
                        "Balcazar Veizaga",
                        "1111111",
                        "director@tecnoweb.org.bo",
                        "70011111",
                        "Av. Bush FICCT",
                        "director",
                        password,
                        "1980-05-10"
                    );
                }

                // =====================================
                // 10. USUARIO ACADEMICO
                // =====================================

                Integer idAcademico = buscarUsuario(
                    con, "academico@tecnoweb.org.bo");

                if (idAcademico == null) {

                    String password = obtenerPasswordInicial(
                        "FICCT_ACADEMICO_PASSWORD");

                    idAcademico = insertarUsuario(
                        con,
                        "Maria Rene",
                        "Flores Roca",
                        "2222222",
                        "academico@tecnoweb.org.bo",
                        "70022222",
                        "Av. Centenario",
                        "academico",
                        password,
                        "1988-08-20"
                    );
                }

                // =====================================
                // 11. ASIGNAR ROLES
                // =====================================

                int rolDirector = buscarId(
                    con, "roles", "nombre", "DIRECTOR");

                int rolAcademico = buscarId(
                    con, "roles", "nombre",
                    "PERSONAL_ACADEMICO");

                insertar(con,
                    "INSERT INTO usuario_roles " +
                    "(id_usuario,id_rol) VALUES (?,?) " +
                    "ON CONFLICT(id_usuario,id_rol) DO NOTHING",
                    idDirector, rolDirector
                );

                insertar(con,
                    "INSERT INTO usuario_roles " +
                    "(id_usuario,id_rol) VALUES (?,?) " +
                    "ON CONFLICT(id_usuario,id_rol) DO NOTHING",
                    idAcademico, rolAcademico
                );

                // =====================================
                // 12. PERFIL DIRECTOR
                // =====================================

                insertar(con,
                    "INSERT INTO director_carrera " +
                    "(id_director,carrera_id," +
                    "gestion_inicio,gestion_fin) " +
                    "VALUES (?,?,?,?) " +
                    "ON CONFLICT(id_director) DO NOTHING",
                    idDirector,
                    idCarrera,
                    java.sql.Date.valueOf("2025-01-01"),
                    java.sql.Date.valueOf("2027-12-31")
                );

                // =====================================
                // 13. PERFIL ACADEMICO
                // =====================================

                insertar(con,
                    "INSERT INTO personal_carrera " +
                    "(id_personal,cargo,carrera_id) " +
                    "VALUES (?,?,?) " +
                    "ON CONFLICT(id_personal) DO NOTHING",
                    idAcademico,
                    "Secretaria Academica",
                    idCarrera
                );

                con.commit();

                System.out.println(
                    "[OK] Datos academicos insertados.");
                System.out.println(
                    "[OK] Roles, usuarios y perfiles preparados.");
                System.out.println(
                    "[OK] Sin registros iniciales duplicados.");

            } catch (Exception e) {

                con.rollback();

                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }

                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }

                throw new SQLException(
                    "Error al insertar datos iniciales.", e);
            }
        }
    }

    // =====================================
    // METODOS AUXILIARES
    // =====================================

    private static void insertar(
            Connection con,
            String sql,
            Object... parametros) throws SQLException {

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            for (int i = 0; i < parametros.length; i++) {
                ps.setObject(i + 1, parametros[i]);
            }

            ps.executeUpdate();
        }
    }

    private static int buscarId(
            Connection con,
            String tabla,
            String columna,
            String valor) throws SQLException {

        String clave = tabla + ":" + columna;

        if (!java.util.Set.of(
                "carrera:codigo",
                "periodos:gestion",
                "materia:sigla",
                "roles:nombre"
            ).contains(clave)) {

            throw new IllegalArgumentException(
                "Consulta de identificador no permitida.");
        }

        String sql = "SELECT id FROM " + tabla +
                " WHERE " + columna + " = ?";

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, valor);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        throw new SQLException(
            "No se encontro el registro: " + valor);
    }

    private static Integer buscarUsuario(
            Connection con,
            String email) throws SQLException {

        String sql =
            "SELECT id_usuario FROM usuario " +
            "WHERE LOWER(email)=LOWER(?)";

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ?
                    rs.getInt("id_usuario") : null;
            }
        }
    }

    private static void insertarCarrera(
            Connection con, String codigo,
            String nombre) throws SQLException {

        insertar(con,
            "INSERT INTO carrera " +
            "(codigo,nombre,facultad) VALUES (?,?,?) " +
            "ON CONFLICT(codigo) DO NOTHING",
            codigo,
            nombre,
            "Facultad de Ingenieria en Ciencias " +
            "de la Computacion y Telecomunicaciones"
        );
    }

    private static void insertarMateria(
            Connection con,
            String sigla,
            String nombre,
            int nivel,
            int creditos) throws SQLException {

        insertar(con,
            "INSERT INTO materia " +
            "(sigla,nombre,nivel_semestre,creditos) " +
            "VALUES (?,?,?,?) " +
            "ON CONFLICT(sigla) DO NOTHING",
            sigla, nombre, nivel, creditos
        );
    }

    private static int insertarGrupo(
            Connection con,
            String siglaMateria,
            int idPeriodo,
            String siglaGrupo,
            String docente,
            int cupoMaximo,
            int disponibles,
            int excepcionales) throws SQLException {

        int idMateria = buscarId(
            con, "materia", "sigla", siglaMateria);

        insertar(con,
            "INSERT INTO grupo " +
            "(id_materia,id_periodo,sigla_grupo," +
            "docente,cupo_maximo,cupos_disponibles," +
            "cupo_excepcional_maximo,estado) " +
            "VALUES (?,?,?,?,?,?,?,'ABIERTO') " +
            "ON CONFLICT(id_materia,id_periodo,sigla_grupo) " +
            "DO NOTHING",
            idMateria, idPeriodo, siglaGrupo,
            docente, cupoMaximo, disponibles, excepcionales
        );

        String sql =
            "SELECT id FROM grupo " +
            "WHERE id_materia=? AND id_periodo=? " +
            "AND sigla_grupo=?";

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, idMateria);
            ps.setInt(2, idPeriodo);
            ps.setString(3, siglaGrupo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        throw new SQLException(
            "No fue posible obtener el grupo registrado.");
    }

    private static void insertarHorario(
            Connection con,
            int idGrupo,
            String dia,
            String horaInicio,
            String horaFin,
            String aula) throws SQLException {

        String sql =
            "INSERT INTO horario_grupo " +
            "(id_grupo,dia,hora_inicio,hora_fin,aula) " +
            "SELECT ?,?,?,?,? WHERE NOT EXISTS (" +
            "SELECT 1 FROM horario_grupo " +
            "WHERE id_grupo=? AND dia=? " +
            "AND hora_inicio=? AND hora_fin=?" +
            ")";

        java.sql.Time inicio =
            java.sql.Time.valueOf(horaInicio + ":00");

        java.sql.Time fin =
            java.sql.Time.valueOf(horaFin + ":00");

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(1, idGrupo);
            ps.setString(2, dia);
            ps.setTime(3, inicio);
            ps.setTime(4, fin);
            ps.setString(5, aula);

            ps.setInt(6, idGrupo);
            ps.setString(7, dia);
            ps.setTime(8, inicio);
            ps.setTime(9, fin);

            ps.executeUpdate();
        }
    }

    private static int insertarUsuario(
            Connection con,
            String nombre,
            String apellidos,
            String ci,
            String email,
            String telefono,
            String direccion,
            String usuarioSistema,
            String password,
            String fechaNacimiento) throws SQLException {

        String sql =
            "INSERT INTO usuario " +
            "(nombre,apellidos,ci,email,telefono,direccion," +
            "usuario_sistema,password,fecha_nacimiento,estado) " +
            "VALUES (?,?,?,?,?,?,?,?,?,'ACTIVO') " +
            "RETURNING id_usuario";

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, apellidos);
            ps.setString(3, ci);
            ps.setString(4, email);
            ps.setString(5, telefono);
            ps.setString(6, direccion);
            ps.setString(7, usuarioSistema);
            ps.setString(8, generarHash(password));
            ps.setDate(
                9, java.sql.Date.valueOf(fechaNacimiento));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_usuario");
                }
            }
        }

        throw new SQLException(
            "No se pudo registrar el usuario principal.");
    }

    private static String obtenerPasswordInicial(
            String variable) {

        String password = System.getenv(variable);

        if (password == null ||
            password.isBlank() ||
            password.length() < 12 ||
            password.length() > 128) {

            throw new IllegalStateException(
                "Configure la variable " + variable +
                " con una contrasena de 12 a 128 caracteres.");
        }

        return password;
    }

    // Compatible con el verificador PBKDF2
    // que utilizamos en UsuarioService.
    private static String generarHash(String password) {

        byte[] sal = new byte[16];
        RANDOM.nextBytes(sal);

        char[] caracteres = password.toCharArray();

        PBEKeySpec spec = new PBEKeySpec(
            caracteres, sal, 600000, 256);

        try {
            SecretKeyFactory factory =
                SecretKeyFactory.getInstance(
                    "PBKDF2WithHmacSHA256");

            byte[] hash =
                factory.generateSecret(spec).getEncoded();

            return "PBKDF2-SHA256:600000:" +
                Base64.getEncoder().encodeToString(sal) +
                ":" +
                Base64.getEncoder().encodeToString(hash);

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                "No se pudo proteger la contrasena.", e);

        } finally {
            spec.clearPassword();
            Arrays.fill(caracteres, '\0');
        }
    }
}
