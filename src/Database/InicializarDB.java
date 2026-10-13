package Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InicializarDB {

    private static void ejecutarSentencias(Statement stmt, String... sentencias) throws SQLException {
        for (String sentencia : sentencias) {
            stmt.executeUpdate(sentencia);
        }
    }

    public static void eliminarTodasLasTablas() {
        Connection con = null;
        Statement stmt = null;

        try {
            con = Conexion.getInstancia().EstablecerConexion();
            if (con == null) {
                System.err.println("No hay conexion disponible.");
                return;
            }

            stmt = con.createStatement();

            ejecutarSentencias(stmt,
                    "DROP TABLE IF EXISTS reporte CASCADE",
                    "DROP TABLE IF EXISTS notificaciones CASCADE",
                    "DROP TABLE IF EXISTS historial_solicitud CASCADE",
                    "DROP TABLE IF EXISTS oficio_respuesta_cpd CASCADE",
                    "DROP TABLE IF EXISTS oficio_cpd CASCADE",
                    "DROP TABLE IF EXISTS bloqueo_grupo CASCADE",
                    "DROP TABLE IF EXISTS evaluacion_solicitud CASCADE",
                    "DROP TABLE IF EXISTS archivo_solicitud CASCADE",
                    "DROP TABLE IF EXISTS solicitud_grupo CASCADE",
                    "DROP TABLE IF EXISTS solicitud CASCADE",
                    "DROP TABLE IF EXISTS horario_grupo CASCADE",
                    "DROP TABLE IF EXISTS grupo CASCADE",
                    "DROP TABLE IF EXISTS materia_carrera CASCADE",
                    "DROP TABLE IF EXISTS materia CASCADE",
                    "DROP TABLE IF EXISTS periodos CASCADE",
                    "DROP TABLE IF EXISTS director_carrera CASCADE",
                    "DROP TABLE IF EXISTS personal_carrera CASCADE",
                    "DROP TABLE IF EXISTS estudiantes CASCADE",
                    "DROP TABLE IF EXISTS carrera CASCADE",
                    "DROP TABLE IF EXISTS usuario_roles CASCADE",
                    "DROP TABLE IF EXISTS roles CASCADE",
                    "DROP TABLE IF EXISTS usuario CASCADE"
            );

            System.out.println("[OK] Tablas del sistema eliminadas correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al eliminar tablas: " + e.getMessage());
        } finally {
            cerrarRecursos(stmt, con);
        }
    }

    public static void crearTablasSiNoExisten() {
        Connection con = null;
        Statement stmt = null;

        try {
            con = Conexion.getInstancia().EstablecerConexion();
            if (con == null) {
                System.err.println("No hay conexion disponible.");
                return;
            }

            stmt = con.createStatement();

            // 1. USUARIOS Y ROLES (CU1)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS usuario (" +
                    "id_usuario SERIAL PRIMARY KEY, " +
                    "nombre VARCHAR(100) NOT NULL, " +
                    "apellidos VARCHAR(100), " +
                    "ci VARCHAR(30) UNIQUE, " +
                    "email VARCHAR(150) UNIQUE, " +
                    "telefono VARCHAR(30), " +
                    "direccion TEXT, " +
                    "foto TEXT, " +
                    "foto_perfil TEXT, " +
                    "usuario_sistema VARCHAR(100) UNIQUE, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO', " +
                    "fecha_nacimiento DATE, " +
                    "fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS roles (" +
                    "id SERIAL PRIMARY KEY, " +
                    "nombre VARCHAR(50) NOT NULL UNIQUE, " +
                    "descripcion TEXT, " +
                    "estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO', " +
                    "fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS usuario_roles (" +
                    "id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario) ON DELETE CASCADE, " +
                    "id_rol INTEGER NOT NULL REFERENCES roles(id) ON DELETE CASCADE, " +
                    "fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO', " +
                    "PRIMARY KEY (id_usuario, id_rol)" +
                    ")"
            );

            // 2. CARRERAS Y PERFILES (CU1)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS carrera (" +
                    "id SERIAL PRIMARY KEY, " +
                    "codigo VARCHAR(20) NOT NULL UNIQUE, " +
                    "nombre VARCHAR(100) NOT NULL, " +
                    "facultad VARCHAR(100) NOT NULL" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS estudiantes (" +
                    "id_estudiante INTEGER PRIMARY KEY REFERENCES usuario(id_usuario) ON DELETE CASCADE, " +
                    "registro_universitario VARCHAR(30) NOT NULL UNIQUE, " +
                    "carrera_id INTEGER NOT NULL REFERENCES carrera(id), " +
                    "semestre_actual INTEGER DEFAULT 1" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS personal_carrera (" +
                    "id_personal INTEGER PRIMARY KEY REFERENCES usuario(id_usuario) ON DELETE CASCADE, " +
                    "cargo VARCHAR(100) NOT NULL, " +
                    "carrera_id INTEGER NOT NULL REFERENCES carrera(id)" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS director_carrera (" +
                    "id_director INTEGER PRIMARY KEY REFERENCES usuario(id_usuario) ON DELETE CASCADE, " +
                    "carrera_id INTEGER NOT NULL REFERENCES carrera(id), " +
                    "gestion_inicio DATE, " +
                    "gestion_fin DATE" +
                    ")"
            );

            // 3. PERIODOS, MATERIAS, GRUPOS Y CUPOS (CU4)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS periodos (" +
                    "id SERIAL PRIMARY KEY, " +
                    "gestion VARCHAR(20) NOT NULL UNIQUE, " +
                    "fecha_inicio DATE NOT NULL, " +
                    "fecha_fin DATE NOT NULL, " +
                    "estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO'" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS materia (" +
                    "id SERIAL PRIMARY KEY, " +
                    "sigla VARCHAR(20) NOT NULL UNIQUE, " +
                    "nombre VARCHAR(100) NOT NULL, " +
                    "nivel_semestre INTEGER DEFAULT 1, " +
                    "creditos INTEGER DEFAULT 4" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS materia_carrera (" +
                    "id_materia INTEGER NOT NULL REFERENCES materia(id) ON DELETE CASCADE, " +
                    "id_carrera INTEGER NOT NULL REFERENCES carrera(id) ON DELETE CASCADE, " +
                    "PRIMARY KEY (id_materia, id_carrera)" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS grupo (" +
                    "id SERIAL PRIMARY KEY, " +
                    "id_materia INTEGER NOT NULL REFERENCES materia(id) ON DELETE CASCADE, " +
                    "id_periodo INTEGER NOT NULL REFERENCES periodos(id) ON DELETE CASCADE, " +
                    "sigla_grupo VARCHAR(10) NOT NULL, " +
                    "docente VARCHAR(100), " +
                    "cupo_maximo INTEGER NOT NULL DEFAULT 40, " +
                    "cupos_disponibles INTEGER NOT NULL DEFAULT 40, " +
                    "cupo_excepcional_maximo INTEGER NOT NULL DEFAULT 5, " +
                    "estado VARCHAR(30) NOT NULL DEFAULT 'ABIERTO', " +
                    "CONSTRAINT uk_grupo UNIQUE (id_materia, id_periodo, sigla_grupo)" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS horario_grupo (" +
                    "id SERIAL PRIMARY KEY, " +
                    "id_grupo INTEGER NOT NULL REFERENCES grupo(id) ON DELETE CASCADE, " +
                    "dia VARCHAR(20) NOT NULL, " +
                    "hora_inicio TIME NOT NULL, " +
                    "hora_fin TIME NOT NULL, " +
                    "aula VARCHAR(50)" +
                    ")"
            );

            // 4. SOLICITUDES DE CASOS ESPECIALES (CU2)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS solicitud (" +
                    "id SERIAL PRIMARY KEY, " +
                    "codigo VARCHAR(50) NOT NULL UNIQUE, " +
                    "id_estudiante INTEGER NOT NULL REFERENCES estudiantes(id_estudiante), " +
                    "id_periodo INTEGER NOT NULL REFERENCES periodos(id), " +
                    "tipo_caso VARCHAR(100) NOT NULL, " +
                    "motivo TEXT NOT NULL, " +
                    "estado VARCHAR(50) NOT NULL DEFAULT 'ENVIADA', " +
                    "fecha_solicitud TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "fecha_revision TIMESTAMP, " +
                    "id_revisor INTEGER REFERENCES usuario(id_usuario), " +
                    "motivo_rechazo TEXT, " +
                    "observaciones TEXT" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS solicitud_grupo (" +
                    "id SERIAL PRIMARY KEY, " +
                    "id_solicitud INTEGER NOT NULL REFERENCES solicitud(id) ON DELETE CASCADE, " +
                    "id_grupo INTEGER NOT NULL REFERENCES grupo(id), " +
                    "motivo TEXT, " +
                    "estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE'" +
                    ")"
            );

            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS archivo_solicitud (" +
                    "id SERIAL PRIMARY KEY, " +
                    "id_solicitud INTEGER NOT NULL REFERENCES solicitud(id) ON DELETE CASCADE, " +
                    "nombre_archivo VARCHAR(150) NOT NULL, " +
                    "tipo_archivo VARCHAR(50), " +
                    "ruta_archivo TEXT, " +
                    "fecha_subida TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")"
            );

            // 5. EVALUACIONES POR DIRECCIÓN (CU3)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS evaluacion_solicitud (" +
                    "id SERIAL PRIMARY KEY, " +
                    "id_solicitud INTEGER NOT NULL REFERENCES solicitud(id) ON DELETE CASCADE, " +
                    "id_evaluador INTEGER NOT NULL REFERENCES usuario(id_usuario), " +
                    "decision VARCHAR(50) NOT NULL, " +
                    "justificacion TEXT, " +
                    "observaciones TEXT, " +
                    "fecha_evaluacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")"
            );

            // 6. BLOQUEO Y CONTROL DE CUPOS (CU4)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS bloqueo_grupo (" +
                    "id SERIAL PRIMARY KEY, " +
                    "id_grupo INTEGER NOT NULL REFERENCES grupo(id) ON DELETE CASCADE, " +
                    "id_solicitud INTEGER REFERENCES solicitud(id), " +
                    "cantidad INTEGER NOT NULL DEFAULT 1, " +
                    "motivo TEXT NOT NULL, " +
                    "estado VARCHAR(30) NOT NULL DEFAULT 'RESERVADO', " +
                    "autorizado_por INTEGER REFERENCES usuario(id_usuario), " +
                    "fecha_bloqueo TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "fecha_resolucion TIMESTAMP, " +
                    "observacion TEXT" +
                    ")"
            );

            // 7. OFICIOS A CPD (CU5)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS oficio_cpd (" +
                    "id SERIAL PRIMARY KEY, " +
                    "numero_oficio VARCHAR(50) NOT NULL UNIQUE, " +
                    "id_solicitud INTEGER NOT NULL REFERENCES solicitud(id), " +
                    "destinatario VARCHAR(100) NOT NULL, " +
                    "referencia VARCHAR(200) NOT NULL, " +
                    "detalle TEXT, " +
                    "id_responsable_remision INTEGER NOT NULL REFERENCES usuario(id_usuario), " +
                    "fecha_remision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "estado VARCHAR(30) NOT NULL DEFAULT 'EMITIDO'" +
                    ")"
            );

            // 8. RESPUESTA Y HABILITACIÓN DE CPD (CU6)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS oficio_respuesta_cpd (" +
                    "id SERIAL PRIMARY KEY, " +
                    "id_oficio INTEGER NOT NULL REFERENCES oficio_cpd(id), " +
                    "id_solicitud INTEGER NOT NULL REFERENCES solicitud(id), " +
                    "codigo_respuesta VARCHAR(50) NOT NULL UNIQUE, " +
                    "resultado VARCHAR(50) NOT NULL, " +
                    "observaciones TEXT, " +
                    "referencia_respaldo VARCHAR(150), " +
                    "fecha_respuesta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "fecha_habilitacion_efectiva TIMESTAMP, " +
                    "id_responsable_cpd INTEGER REFERENCES usuario(id_usuario), " +
                    "estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADO'" +
                    ")"
            );

            // 9. HISTORIAL (CU7)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS historial_solicitud (" +
                    "id SERIAL PRIMARY KEY, " +
                    "id_solicitud INTEGER NOT NULL REFERENCES solicitud(id) ON DELETE CASCADE, " +
                    "id_usuario INTEGER REFERENCES usuario(id_usuario), " +
                    "estado_anterior VARCHAR(50), " +
                    "estado_nuevo VARCHAR(50) NOT NULL, " +
                    "accion VARCHAR(100) NOT NULL, " +
                    "observacion TEXT, " +
                    "fecha_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")"
            );

            // 10. NOTIFICACIONES (CU7)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS notificaciones (" +
                    "id SERIAL PRIMARY KEY, " +
                    "id_usuario INTEGER REFERENCES usuario(id_usuario), " +
                    "id_solicitud INTEGER REFERENCES solicitud(id), " +
                    "destinatario VARCHAR(150) NOT NULL, " +
                    "tipo VARCHAR(50) NOT NULL, " +
                    "asunto VARCHAR(200) NOT NULL, " +
                    "mensaje TEXT NOT NULL, " +
                    "canal VARCHAR(30) NOT NULL DEFAULT 'EMAIL', " +
                    "estado VARCHAR(30) NOT NULL DEFAULT 'ENVIADO', " +
                    "intentos INTEGER NOT NULL DEFAULT 1, " +
                    "ultimo_error TEXT, " +
                    "fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "fecha_lectura TIMESTAMP" +
                    ")"
            );

            // 11. REPORTES (CU8)
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS reporte (" +
                    "id SERIAL PRIMARY KEY, " +
                    "id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario), " +
                    "tipo_reporte VARCHAR(100) NOT NULL, " +
                    "parametros TEXT, " +
                    "fecha_generacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                    ")"
            );

            System.out.println("[OK] Tablas del Sistema de Casos Especiales creadas correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al crear tablas: " + e.getMessage());
        } finally {
            cerrarRecursos(stmt, con);
        }
    }

    public static void insertarDatosIniciales() {
        Connection con = null;
        Statement stmt = null;

        try {
            con = Conexion.getInstancia().EstablecerConexion();
            if (con == null) return;

            stmt = con.createStatement();

            // Roles
            stmt.executeUpdate(
                    "INSERT INTO roles (nombre, descripcion) VALUES " +
                    "('DIRECTOR', 'Director de Carrera'), " +
                    "('PERSONAL_ACADEMICO', 'Personal administrativo y academico de Carrera'), " +
                    "('ESTUDIANTE', 'Estudiante universitario solicitante'), " +
                    "('CPD', 'Centro de Procesamiento de Datos') " +
                    "ON CONFLICT (nombre) DO NOTHING"
            );

            // Carreras
            stmt.executeUpdate(
                    "INSERT INTO carrera (codigo, nombre, facultad) VALUES " +
                    "('187-3', 'Ingenieria en Sistemas', 'Facultad de Ciencias de la Computacion y Telecomunicaciones'), " +
                    "('187-4', 'Ingenieria Informatica', 'Facultad de Ciencias de la Computacion y Telecomunicaciones'), " +
                    "('187-5', 'Ingenieria en Redes y Telecomunicaciones', 'Facultad de Ciencias de la Computacion y Telecomunicaciones') " +
                    "ON CONFLICT (codigo) DO NOTHING"
            );

            // Periodos
            stmt.executeUpdate(
                    "INSERT INTO periodos (gestion, fecha_inicio, fecha_fin, estado) VALUES " +
                    "('1-2026', '2026-02-15', '2026-07-15', 'ACTIVO') " +
                    "ON CONFLICT (gestion) DO NOTHING"
            );

            // Materias
            stmt.executeUpdate(
                    "INSERT INTO materia (sigla, nombre, nivel_semestre, creditos) VALUES " +
                    "('INF210', 'Sistemas de Informacion I', 4, 5), " +
                    "('INF220', 'Estructuras de Datos II', 4, 5), " +
                    "('MAT101', 'Calculo I', 1, 5), " +
                    "('INF110', 'Introduccion a la Informatica', 1, 4), " +
                    "('INF310', 'Sistemas de Informacion II', 6, 5) " +
                    "ON CONFLICT (sigla) DO NOTHING"
            );

            // Usuarios Semilla
            stmt.executeUpdate(
                    "INSERT INTO usuario (id_usuario, nombre, apellidos, ci, email, telefono, direccion, usuario_sistema, password, estado, fecha_nacimiento) VALUES " +
                    "(1, 'Evans', 'Balcazar Veizaga', '1111111', 'director@tecnoweb.org.bo', '70011111', 'Av. Bush FICCT', 'director', '123456', 'ACTIVO', '1980-05-10'), " +
                    "(2, 'Maria Rene', 'Flores Roca', '2222222', 'academico@tecnoweb.org.bo', '70022222', 'Av. Centenario', 'academico', '123456', 'ACTIVO', '1988-08-20'), " +
                    "(3, 'Carlos', 'Mendez Torrico', '3333333', 'carlos.mendez@tecnoweb.org.bo', '70033333', 'Plan 3000', 'cmendez', '123456', 'ACTIVO', '2002-03-15'), " +
                    "(4, 'Ana Sofia', 'Perez Hurtado', '4444444', 'ana.perez@tecnoweb.org.bo', '70044444', 'Barrio Sirari', 'aperez', '123456', 'ACTIVO', '2003-09-22'), " +
                    "(5, 'Roberto', 'Silva Gutierrez', '5555555', 'cpd@tecnoweb.org.bo', '70055555', 'Campus Universitario CPD', 'cpd_admin', '123456', 'ACTIVO', '1975-11-30') " +
                    "ON CONFLICT (id_usuario) DO NOTHING"
            );

            // Asignación de Roles
            stmt.executeUpdate(
                    "INSERT INTO usuario_roles (id_usuario, id_rol) VALUES " +
                    "(1, 1), (2, 2), (3, 3), (4, 3), (5, 4) " +
                    "ON CONFLICT (id_usuario, id_rol) DO NOTHING"
            );

            // Perfiles
            stmt.executeUpdate(
                    "INSERT INTO director_carrera (id_director, carrera_id, gestion_inicio, gestion_fin) VALUES " +
                    "(1, 1, '2025-01-01', '2027-12-31') " +
                    "ON CONFLICT (id_director) DO NOTHING"
            );

            stmt.executeUpdate(
                    "INSERT INTO personal_carrera (id_personal, cargo, carrera_id) VALUES " +
                    "(2, 'Secretaria Academica', 1) " +
                    "ON CONFLICT (id_personal) DO NOTHING"
            );

            stmt.executeUpdate(
                    "INSERT INTO estudiantes (id_estudiante, registro_universitario, carrera_id, semestre_actual) VALUES " +
                    "(3, '220011223', 1, 4), " +
                    "(4, '220033445', 1, 4) " +
                    "ON CONFLICT (id_estudiante) DO NOTHING"
            );

            // Grupos
            stmt.executeUpdate(
                    "INSERT INTO grupo (id_materia, id_periodo, sigla_grupo, docente, cupo_maximo, cupos_disponibles, cupo_excepcional_maximo, estado) VALUES " +
                    "(1, 1, 'SA', 'Ing. Juan Perez', 45, 5, 5, 'ABIERTO'), " +
                    "(1, 1, 'SC', 'Ing. Evans Balcazar', 40, 2, 5, 'ABIERTO'), " +
                    "(2, 1, 'SB', 'Ing. Mario Lopez', 45, 8, 5, 'ABIERTO') " +
                    "ON CONFLICT (id_materia, id_periodo, sigla_grupo) DO NOTHING"
            );

            // Sincronizar secuencias de PostgreSQL para evitar colisiones de ID al insertar nuevos registros
            stmt.executeUpdate("SELECT setval(pg_get_serial_sequence('usuario', 'id_usuario'), COALESCE(MAX(id_usuario), 1)) FROM usuario");
            stmt.executeUpdate("SELECT setval(pg_get_serial_sequence('roles', 'id'), COALESCE(MAX(id), 1)) FROM roles");
            stmt.executeUpdate("SELECT setval(pg_get_serial_sequence('carrera', 'id'), COALESCE(MAX(id), 1)) FROM carrera");
            stmt.executeUpdate("SELECT setval(pg_get_serial_sequence('periodos', 'id'), COALESCE(MAX(id), 1)) FROM periodos");
            stmt.executeUpdate("SELECT setval(pg_get_serial_sequence('materia', 'id'), COALESCE(MAX(id), 1)) FROM materia");
            stmt.executeUpdate("SELECT setval(pg_get_serial_sequence('grupo', 'id'), COALESCE(MAX(id), 1)) FROM grupo");

            System.out.println("[OK] Datos iniciales insertados correctamente.");

        } catch (SQLException e) {
            System.err.println("Error al insertar datos iniciales: " + e.getMessage());
        } finally {
            cerrarRecursos(stmt, con);
        }
    }

    private static void cerrarRecursos(Statement stmt, Connection con) {
        try {
            if (stmt != null) stmt.close();
            if (con != null) con.close();
        } catch (SQLException ignored) {
        }
    }
}