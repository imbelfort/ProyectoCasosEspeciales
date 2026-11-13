
package Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CrearTablasDB {

    public static void main(String[] args) {
        try {
            crearTablas();
        } catch (SQLException e) {
            System.err.println("[ERROR] No se crearon las tablas.");
            e.printStackTrace();
        }
    }

    public static void crearTablas() throws SQLException {

        Connection con = Conexion.getInstancia()
                .EstablecerConexion();

        if (con == null) {
            throw new SQLException(
                "No se pudo establecer conexion con PostgreSQL.");
        }

        try (con) {
            con.setAutoCommit(false);

            try (Statement stmt = con.createStatement()) {

                // ======================================
                // CU1 - USUARIOS Y ROLES
                // ======================================

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS usuario (
                        id_usuario SERIAL PRIMARY KEY,
                        nombre VARCHAR(100) NOT NULL,
                        apellidos VARCHAR(100) NOT NULL,
                        ci VARCHAR(30) NOT NULL UNIQUE,
                        email VARCHAR(150) NOT NULL UNIQUE,
                        telefono VARCHAR(30),
                        direccion TEXT,
                        foto_perfil TEXT,
                        usuario_sistema VARCHAR(100) UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
                        fecha_nacimiento DATE,
                        fecha_creacion TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP,
                        fecha_modificacion TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT chk_usuario_estado
                        CHECK (estado IN (
                            'ACTIVO', 'INACTIVO', 'BLOQUEADO'
                        ))
                    )
                    """);

                stmt.executeUpdate("""
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    ux_usuario_email_lower
                    ON usuario(LOWER(email))
                    """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS roles (
                        id SERIAL PRIMARY KEY,
                        nombre VARCHAR(50) NOT NULL UNIQUE,
                        descripcion TEXT,
                        estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
                        fecha_creacion TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS usuario_roles (
                        id_usuario INTEGER NOT NULL
                            REFERENCES usuario(id_usuario),
                        id_rol INTEGER NOT NULL
                            REFERENCES roles(id),
                        fecha_asignacion TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP,
                        estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
                        PRIMARY KEY(id_usuario, id_rol)
                    )
                    """);

                // ======================================
                // AUTENTICACION Y SESIONES
                // ======================================

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS sesion_usuario (
                        id BIGSERIAL PRIMARY KEY,
                        id_usuario INTEGER NOT NULL
                            REFERENCES usuario(id_usuario),
                        token_hash VARCHAR(64) NOT NULL UNIQUE,
                        fecha_inicio TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP,
                        fecha_expiracion TIMESTAMP NOT NULL,
                        fecha_ultimo_uso TIMESTAMP,
                        remitente_email VARCHAR(150),
                        estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
                        CONSTRAINT chk_sesion_estado
                        CHECK (estado IN (
                            'ACTIVA', 'CERRADA', 'EXPIRADA'
                        )),
                        CONSTRAINT chk_sesion_fechas
                        CHECK (fecha_expiracion > fecha_inicio)
                    )
                    """);

                stmt.executeUpdate("""
                    CREATE UNIQUE INDEX IF NOT EXISTS
                    idx_sesion_remitente_activa
                    ON sesion_usuario(LOWER(remitente_email))
                    WHERE estado = 'ACTIVA'
                    """);

                stmt.executeUpdate("""
                    CREATE INDEX IF NOT EXISTS
                    idx_sesion_usuario
                    ON sesion_usuario(id_usuario)
                    """);

                stmt.executeUpdate("""
                    CREATE INDEX IF NOT EXISTS
                    idx_sesion_expiracion
                    ON sesion_usuario(fecha_expiracion)
                    """);

                // ======================================
                // CARRERAS Y PERFILES
                // ======================================

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS carrera (
                        id SERIAL PRIMARY KEY,
                        codigo VARCHAR(20) NOT NULL UNIQUE,
                        nombre VARCHAR(100) NOT NULL,
                        facultad VARCHAR(150) NOT NULL
                    )
                    """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS estudiantes (
                        id_estudiante INTEGER PRIMARY KEY
                            REFERENCES usuario(id_usuario),
                        registro_universitario VARCHAR(30)
                            NOT NULL UNIQUE,
                        carrera_id INTEGER NOT NULL
                            REFERENCES carrera(id),
                        semestre_actual INTEGER NOT NULL DEFAULT 1,
                        CONSTRAINT chk_semestre
                        CHECK (semestre_actual BETWEEN 1 AND 12)
                    )
                    """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS personal_carrera (
                        id_personal INTEGER PRIMARY KEY
                            REFERENCES usuario(id_usuario),
                        cargo VARCHAR(100) NOT NULL,
                        carrera_id INTEGER NOT NULL
                            REFERENCES carrera(id)
                    )
                    """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS director_carrera (
                        id_director INTEGER PRIMARY KEY
                            REFERENCES usuario(id_usuario),
                        carrera_id INTEGER NOT NULL
                            REFERENCES carrera(id),
                        gestion_inicio DATE,
                        gestion_fin DATE,
                        CONSTRAINT chk_gestion_director
                        CHECK (
                            gestion_inicio IS NULL OR
                            gestion_fin IS NULL OR
                            gestion_fin >= gestion_inicio
                        )
                    )
                    """);

                // ======================================
                // CU4 - ESTRUCTURA ACADEMICA
                // ======================================

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS periodos (
                        id SERIAL PRIMARY KEY,
                        gestion VARCHAR(20) NOT NULL UNIQUE,
                        fecha_inicio DATE NOT NULL,
                        fecha_fin DATE NOT NULL,
                        estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
                        CONSTRAINT chk_periodo_fechas
                        CHECK (fecha_fin >= fecha_inicio)
                    )
                    """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS materia (
                        id SERIAL PRIMARY KEY,
                        sigla VARCHAR(20) NOT NULL UNIQUE,
                        nombre VARCHAR(100) NOT NULL,
                        nivel_semestre INTEGER NOT NULL DEFAULT 1,
                        creditos INTEGER NOT NULL DEFAULT 4,
                        CONSTRAINT chk_materia_nivel
                        CHECK (nivel_semestre BETWEEN 1 AND 12),
                        CONSTRAINT chk_materia_creditos
                        CHECK (creditos > 0)
                    )
                    """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS materia_carrera (
                        id_materia INTEGER NOT NULL
                            REFERENCES materia(id),
                        id_carrera INTEGER NOT NULL
                            REFERENCES carrera(id),
                        PRIMARY KEY(id_materia, id_carrera)
                    )
                    """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS grupo (
                        id SERIAL PRIMARY KEY,
                        id_materia INTEGER NOT NULL
                            REFERENCES materia(id),
                        id_periodo INTEGER NOT NULL
                            REFERENCES periodos(id),
                        sigla_grupo VARCHAR(10) NOT NULL,
                        docente VARCHAR(100),
                        cupo_maximo INTEGER NOT NULL DEFAULT 40,
                        cupos_disponibles INTEGER NOT NULL DEFAULT 40,
                        cupo_excepcional_maximo INTEGER NOT NULL DEFAULT 5,
                        estado VARCHAR(30) NOT NULL DEFAULT 'ABIERTO',
                        CONSTRAINT uk_grupo UNIQUE(
                            id_materia, id_periodo, sigla_grupo
                        ),
                        CONSTRAINT chk_grupo_cupos CHECK (
                            cupo_maximo >= 0 AND
                            cupos_disponibles >= 0 AND
                            cupos_disponibles <= cupo_maximo AND
                            cupo_excepcional_maximo >= 0
                        )
                    )
                    """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS horario_grupo (
                        id SERIAL PRIMARY KEY,
                        id_grupo INTEGER NOT NULL REFERENCES grupo(id),
                        dia VARCHAR(20) NOT NULL,
                        hora_inicio TIME NOT NULL,
                        hora_fin TIME NOT NULL,
                        aula VARCHAR(50),
                        CONSTRAINT chk_horario_valido
                        CHECK (hora_fin > hora_inicio)
                    )
                    """);

                // ======================================
                // CU2 - SOLICITUDES
                // ======================================

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS solicitud (
                        id SERIAL PRIMARY KEY,
                        codigo VARCHAR(50) NOT NULL UNIQUE,
                        id_estudiante INTEGER NOT NULL
                            REFERENCES estudiantes(id_estudiante),
                        id_periodo INTEGER NOT NULL
                            REFERENCES periodos(id),
                        tipo_caso VARCHAR(100) NOT NULL,
                        motivo TEXT NOT NULL,
                        estado VARCHAR(50) NOT NULL DEFAULT 'BORRADOR',
                        fecha_solicitud TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP,
                        fecha_revision TIMESTAMP,
                        id_revisor INTEGER REFERENCES usuario(id_usuario),
                        motivo_rechazo TEXT,
                        observaciones TEXT
                    )
                    """);

                stmt.executeUpdate("""
                    CREATE INDEX IF NOT EXISTS
                    idx_solicitud_estudiante
                    ON solicitud(id_estudiante)
                    """);

                stmt.executeUpdate("""
                    CREATE INDEX IF NOT EXISTS
                    idx_solicitud_periodo_estado
                    ON solicitud(id_periodo, estado)
                    """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS solicitud_grupo (
                        id SERIAL PRIMARY KEY,
                        id_solicitud INTEGER NOT NULL
                            REFERENCES solicitud(id) ON DELETE CASCADE,
                        id_grupo INTEGER NOT NULL REFERENCES grupo(id),
                        motivo TEXT,
                        estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
                        CONSTRAINT uk_solicitud_grupo
                        UNIQUE(id_solicitud, id_grupo)
                    )
                    """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS archivo_solicitud (
                        id SERIAL PRIMARY KEY,
                        id_solicitud INTEGER NOT NULL
                            REFERENCES solicitud(id) ON DELETE CASCADE,
                        nombre_archivo VARCHAR(150) NOT NULL,
                        tipo_archivo VARCHAR(50),
                        ruta_archivo TEXT,
                        fecha_subida TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

                // ======================================
                // CU3 - EVALUACIONES
                // ======================================

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS evaluacion_solicitud (
                        id SERIAL PRIMARY KEY,
                        id_solicitud INTEGER NOT NULL
                            REFERENCES solicitud(id) ON DELETE CASCADE,
                        id_evaluador INTEGER NOT NULL
                            REFERENCES usuario(id_usuario),
                        decision VARCHAR(50) NOT NULL,
                        justificacion TEXT,
                        observaciones TEXT,
                        fecha_evaluacion TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP,
                        CONSTRAINT chk_decision_evaluacion
                        CHECK (decision IN (
                            'APROBADA', 'RECHAZADA', 'OBSERVADA'
                        ))
                    )
                    """);

                // ======================================
                // CU4 - BLOQUEO DE CUPOS
                // ======================================

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS bloqueo_grupo (
                        id SERIAL PRIMARY KEY,
                        id_grupo INTEGER NOT NULL REFERENCES grupo(id),
                        id_solicitud INTEGER REFERENCES solicitud(id),
                        cantidad INTEGER NOT NULL DEFAULT 1,
                        motivo TEXT NOT NULL,
                        estado VARCHAR(30) NOT NULL DEFAULT 'RESERVADO',
                        autorizado_por INTEGER REFERENCES usuario(id_usuario),
                        fecha_bloqueo TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP,
                        fecha_resolucion TIMESTAMP,
                        observacion TEXT,
                        CONSTRAINT chk_bloqueo_cantidad
                        CHECK (cantidad > 0)
                    )
                    """);

                // ======================================
                // CU5 - OFICIOS AL CPD
                // ======================================

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS oficio_cpd (
                        id SERIAL PRIMARY KEY,
                        numero_oficio VARCHAR(50) NOT NULL UNIQUE,
                        id_solicitud INTEGER NOT NULL
                            REFERENCES solicitud(id),
                        destinatario VARCHAR(100) NOT NULL,
                        referencia VARCHAR(200) NOT NULL,
                        detalle TEXT,
                        id_responsable_remision INTEGER NOT NULL
                            REFERENCES usuario(id_usuario),
                        fecha_remision TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP,
                        estado VARCHAR(30) NOT NULL DEFAULT 'EMITIDO'
                    )
                    """);

                stmt.executeUpdate(
                        "ALTER TABLE oficio_cpd " +
                        "ADD COLUMN IF NOT EXISTS ruta_documento TEXT");

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS oficio_cpd_solicitud (
                        id_oficio INTEGER NOT NULL
                            REFERENCES oficio_cpd(id) ON DELETE CASCADE,
                        id_solicitud INTEGER NOT NULL
                            REFERENCES solicitud(id),
                        PRIMARY KEY (id_oficio, id_solicitud)
                    )
                    """);

                stmt.executeUpdate(
                        "CREATE INDEX IF NOT EXISTS idx_oficio_cpd_solicitud_solicitud " +
                        "ON oficio_cpd_solicitud(id_solicitud)");

                // ======================================
                // CU6 - RESPUESTAS DEL CPD
                // ======================================

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS oficio_respuesta_cpd (
                        id SERIAL PRIMARY KEY,
                        id_oficio INTEGER NOT NULL
                            REFERENCES oficio_cpd(id),
                        id_solicitud INTEGER NOT NULL
                            REFERENCES solicitud(id),
                        codigo_respuesta VARCHAR(50) NOT NULL UNIQUE,
                        resultado VARCHAR(50) NOT NULL,
                        observaciones TEXT,
                        referencia_respaldo VARCHAR(150),
                        fecha_respuesta TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP,
                        fecha_habilitacion_efectiva TIMESTAMP,
                        id_responsable_cpd INTEGER
                            REFERENCES usuario(id_usuario),
                        estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADO'
                    )
                    """);

                // ======================================
                // CU7 - HISTORIAL
                // ======================================

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS historial_solicitud (
                        id SERIAL PRIMARY KEY,
                        id_solicitud INTEGER NOT NULL
                            REFERENCES solicitud(id) ON DELETE CASCADE,
                        id_usuario INTEGER REFERENCES usuario(id_usuario),
                        estado_anterior VARCHAR(50),
                        estado_nuevo VARCHAR(50) NOT NULL,
                        accion VARCHAR(100) NOT NULL,
                        observacion TEXT,
                        fecha_evento TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS notificaciones (
                        id SERIAL PRIMARY KEY,
                        id_usuario INTEGER REFERENCES usuario(id_usuario),
                        id_solicitud INTEGER REFERENCES solicitud(id),
                        destinatario VARCHAR(150) NOT NULL,
                        tipo VARCHAR(50) NOT NULL,
                        asunto VARCHAR(200) NOT NULL,
                        mensaje TEXT NOT NULL,
                        canal VARCHAR(30) NOT NULL DEFAULT 'EMAIL',
                        estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
                        intentos INTEGER NOT NULL DEFAULT 0,
                        ultimo_error TEXT,
                        fecha_creacion TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP,
                        fecha_envio TIMESTAMP,
                        fecha_lectura TIMESTAMP
                    )
                    """);

                // ======================================
                // CU8 - REPORTES
                // ======================================

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS reporte (
                        id SERIAL PRIMARY KEY,
                        id_usuario INTEGER NOT NULL
                            REFERENCES usuario(id_usuario),
                        tipo_reporte VARCHAR(100) NOT NULL,
                        parametros TEXT,
                        fecha_generacion TIMESTAMP NOT NULL
                            DEFAULT CURRENT_TIMESTAMP
                    )
                    """);

                con.commit();

                System.out.println(
                    "[OK] Todas las tablas fueron verificadas.");
                System.out.println(
                    "[OK] Estructura academica creada.");
                System.out.println(
                    "[OK] Tabla de sesiones creada.");

            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }
}
