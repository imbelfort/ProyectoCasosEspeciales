-- ============================================================
-- BASE DE DATOS: SISTEMA DE GESTIÓN DE CASOS ESPECIALES ACADÉMICOS
-- FICCT - UAGRM | GRUPO 02 SC
-- PostgreSQL 12+ / Compatible con entorno mail.tecnoweb.org.bo
-- ============================================================

-- ============================================================
-- 1. USUARIOS Y ROLES (CU1)
-- ============================================================

CREATE TABLE usuario (
    id_usuario SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100),
    ci VARCHAR(30) UNIQUE,
    email VARCHAR(150) UNIQUE,
    telefono VARCHAR(30),
    direccion TEXT,
    foto TEXT,
    foto_perfil TEXT,
    usuario_sistema VARCHAR(100) UNIQUE,
    password VARCHAR(255) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fecha_nacimiento DATE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_usuario_estado CHECK (estado IN ('ACTIVO', 'INACTIVO', 'BLOQUEADO'))
);

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_roles_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE usuario_roles (
    id_usuario INTEGER NOT NULL,
    id_rol INTEGER NOT NULL,
    fecha_asignacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT fk_usuario_roles_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_usuario_roles_rol FOREIGN KEY (id_rol) REFERENCES roles(id) ON DELETE CASCADE,
    CONSTRAINT chk_usuario_roles_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

-- ============================================================
-- 2. CARRERAS Y PERFILES ACADÉMICOS (CU1)
-- ============================================================

CREATE TABLE carrera (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    facultad VARCHAR(100) NOT NULL
);

CREATE TABLE estudiantes (
    id_estudiante INTEGER PRIMARY KEY,
    registro_universitario VARCHAR(30) NOT NULL UNIQUE,
    carrera_id INTEGER NOT NULL,
    semestre_actual INTEGER DEFAULT 1,
    CONSTRAINT fk_estudiantes_usuario FOREIGN KEY (id_estudiante) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_estudiantes_carrera FOREIGN KEY (carrera_id) REFERENCES carrera(id)
);

CREATE TABLE personal_carrera (
    id_personal INTEGER PRIMARY KEY,
    cargo VARCHAR(100) NOT NULL,
    carrera_id INTEGER NOT NULL,
    CONSTRAINT fk_personal_usuario FOREIGN KEY (id_personal) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_personal_carrera FOREIGN KEY (carrera_id) REFERENCES carrera(id)
);

CREATE TABLE director_carrera (
    id_director INTEGER PRIMARY KEY,
    carrera_id INTEGER NOT NULL,
    gestion_inicio DATE,
    gestion_fin DATE,
    CONSTRAINT fk_director_usuario FOREIGN KEY (id_director) REFERENCES usuario(id_usuario) ON DELETE CASCADE,
    CONSTRAINT fk_director_carrera FOREIGN KEY (carrera_id) REFERENCES carrera(id)
);

-- ============================================================
-- 3. CATÁLOGO ACADÉMICO, PERIODOS, MATERIAS Y CUPOS (CU4)
-- ============================================================

CREATE TABLE periodos (
    id SERIAL PRIMARY KEY,
    gestion VARCHAR(20) NOT NULL UNIQUE,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    CONSTRAINT chk_periodos_estado CHECK (estado IN ('ACTIVO', 'CERRADO'))
);

CREATE TABLE materia (
    id SERIAL PRIMARY KEY,
    sigla VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    nivel_semestre INTEGER DEFAULT 1,
    creditos INTEGER DEFAULT 4
);

CREATE TABLE materia_carrera (
    id_materia INTEGER NOT NULL,
    id_carrera INTEGER NOT NULL,
    PRIMARY KEY (id_materia, id_carrera),
    CONSTRAINT fk_mc_materia FOREIGN KEY (id_materia) REFERENCES materia(id) ON DELETE CASCADE,
    CONSTRAINT fk_mc_carrera FOREIGN KEY (id_carrera) REFERENCES carrera(id) ON DELETE CASCADE
);

CREATE TABLE grupo (
    id SERIAL PRIMARY KEY,
    id_materia INTEGER NOT NULL,
    id_periodo INTEGER NOT NULL,
    sigla_grupo VARCHAR(10) NOT NULL,
    docente VARCHAR(100),
    cupo_maximo INTEGER NOT NULL DEFAULT 40,
    cupos_disponibles INTEGER NOT NULL DEFAULT 40,
    cupo_excepcional_maximo INTEGER NOT NULL DEFAULT 5,
    estado VARCHAR(30) NOT NULL DEFAULT 'ABIERTO',
    CONSTRAINT fk_grupo_materia FOREIGN KEY (id_materia) REFERENCES materia(id) ON DELETE CASCADE,
    CONSTRAINT fk_grupo_periodo FOREIGN KEY (id_periodo) REFERENCES periodos(id) ON DELETE CASCADE,
    CONSTRAINT chk_grupo_estado CHECK (estado IN ('ABIERTO', 'CERRADO', 'BLOQUEADO'))
);

CREATE TABLE horario_grupo (
    id SERIAL PRIMARY KEY,
    id_grupo INTEGER NOT NULL,
    dia VARCHAR(20) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    aula VARCHAR(50),
    CONSTRAINT fk_horario_grupo FOREIGN KEY (id_grupo) REFERENCES grupo(id) ON DELETE CASCADE
);

-- ============================================================
-- 4. SOLICITUDES DE CASOS ESPECIALES (CU2)
-- ============================================================

CREATE TABLE solicitud (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    id_estudiante INTEGER NOT NULL,
    id_periodo INTEGER NOT NULL,
    tipo_caso VARCHAR(100) NOT NULL,
    motivo TEXT NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'ENVIADA',
    fecha_solicitud TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_revision TIMESTAMP,
    id_revisor INTEGER,
    motivo_rechazo TEXT,
    observaciones TEXT,
    CONSTRAINT fk_solicitud_estudiante FOREIGN KEY (id_estudiante) REFERENCES estudiantes(id_estudiante),
    CONSTRAINT fk_solicitud_periodo FOREIGN KEY (id_periodo) REFERENCES periodos(id),
    CONSTRAINT fk_solicitud_revisor FOREIGN KEY (id_revisor) REFERENCES usuario(id_usuario)
);

CREATE TABLE solicitud_grupo (
    id SERIAL PRIMARY KEY,
    id_solicitud INTEGER NOT NULL,
    id_grupo INTEGER NOT NULL,
    motivo TEXT,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    CONSTRAINT fk_solicitud_grupo_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id) ON DELETE CASCADE,
    CONSTRAINT fk_solicitud_grupo_grupo FOREIGN KEY (id_grupo) REFERENCES grupo(id)
);

CREATE TABLE archivo_solicitud (
    id SERIAL PRIMARY KEY,
    id_solicitud INTEGER NOT NULL,
    nombre_archivo VARCHAR(150) NOT NULL,
    tipo_archivo VARCHAR(50),
    ruta_archivo TEXT,
    fecha_subida TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_archivo_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id) ON DELETE CASCADE
);

-- ============================================================
-- 5. EVALUACIONES POR DIRECCIÓN DE CARRERA (CU3)
-- ============================================================

CREATE TABLE evaluacion_solicitud (
    id SERIAL PRIMARY KEY,
    id_solicitud INTEGER NOT NULL,
    id_evaluador INTEGER NOT NULL,
    decision VARCHAR(50) NOT NULL,
    justificacion TEXT,
    observaciones TEXT,
    fecha_evaluacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evaluacion_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id) ON DELETE CASCADE,
    CONSTRAINT fk_evaluacion_evaluador FOREIGN KEY (id_evaluador) REFERENCES usuario(id_usuario),
    CONSTRAINT chk_evaluacion_decision CHECK (decision IN ('APROBADA', 'RECHAZADA', 'OBSERVADA'))
);

-- ============================================================
-- 6. BLOQUEO Y CONTROL DE CUPOS (CU4)
-- ============================================================

CREATE TABLE bloqueo_grupo (
    id SERIAL PRIMARY KEY,
    id_grupo INTEGER NOT NULL,
    id_solicitud INTEGER,
    cantidad INTEGER NOT NULL DEFAULT 1,
    motivo TEXT NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'RESERVADO',
    autorizado_por INTEGER,
    fecha_bloqueo TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_resolucion TIMESTAMP,
    observacion TEXT,
    CONSTRAINT fk_bloqueo_grupo FOREIGN KEY (id_grupo) REFERENCES grupo(id) ON DELETE CASCADE,
    CONSTRAINT fk_bloqueo_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id),
    CONSTRAINT fk_bloqueo_autorizador FOREIGN KEY (autorizado_por) REFERENCES usuario(id_usuario),
    CONSTRAINT chk_bloqueo_estado CHECK (estado IN ('RESERVADO', 'LIBERADO', 'CONFIRMADO'))
);

-- ============================================================
-- 7. OFICIOS Y REMISIÓN A CPD (CU5)
-- ============================================================

CREATE TABLE oficio_cpd (
    id SERIAL PRIMARY KEY,
    numero_oficio VARCHAR(50) NOT NULL UNIQUE,
    id_solicitud INTEGER NOT NULL,
    destinatario VARCHAR(100) NOT NULL,
    referencia VARCHAR(200) NOT NULL,
    detalle TEXT,
    id_responsable_remision INTEGER NOT NULL,
    fecha_remision TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'EMITIDO',
    CONSTRAINT fk_oficio_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id),
    CONSTRAINT fk_oficio_responsable FOREIGN KEY (id_responsable_remision) REFERENCES usuario(id_usuario)
);

-- ============================================================
-- 8. RESPUESTAS Y HABILITACIÓN DE CPD (CU6)
-- ============================================================

CREATE TABLE oficio_respuesta_cpd (
    id SERIAL PRIMARY KEY,
    id_oficio INTEGER NOT NULL,
    id_solicitud INTEGER NOT NULL,
    codigo_respuesta VARCHAR(50) NOT NULL UNIQUE,
    resultado VARCHAR(50) NOT NULL,
    observaciones TEXT,
    referencia_respaldo VARCHAR(150),
    fecha_respuesta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_habilitacion_efectiva TIMESTAMP,
    id_responsable_cpd INTEGER,
    estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADO',
    CONSTRAINT fk_respuesta_oficio FOREIGN KEY (id_oficio) REFERENCES oficio_cpd(id),
    CONSTRAINT fk_respuesta_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id),
    CONSTRAINT fk_respuesta_responsable FOREIGN KEY (id_responsable_cpd) REFERENCES usuario(id_usuario)
);

-- ============================================================
-- 9. HISTORIAL Y TRAZABILIDAD (CU7)
-- ============================================================

CREATE TABLE historial_solicitud (
    id SERIAL PRIMARY KEY,
    id_solicitud INTEGER NOT NULL,
    id_usuario INTEGER,
    estado_anterior VARCHAR(50),
    estado_nuevo VARCHAR(50) NOT NULL,
    accion VARCHAR(100) NOT NULL,
    observacion TEXT,
    fecha_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_historial_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id) ON DELETE CASCADE,
    CONSTRAINT fk_historial_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);

-- ============================================================
-- 10. NOTIFICACIONES POR CORREO (CU7)
-- ============================================================

CREATE TABLE notificaciones (
    id SERIAL PRIMARY KEY,
    id_usuario INTEGER,
    id_solicitud INTEGER,
    destinatario VARCHAR(150) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    asunto VARCHAR(200) NOT NULL,
    mensaje TEXT NOT NULL,
    canal VARCHAR(30) NOT NULL DEFAULT 'EMAIL',
    estado VARCHAR(30) NOT NULL DEFAULT 'ENVIADO',
    intentos INTEGER NOT NULL DEFAULT 1,
    ultimo_error TEXT,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_envio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_lectura TIMESTAMP,
    CONSTRAINT fk_notificaciones_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_notificaciones_solicitud FOREIGN KEY (id_solicitud) REFERENCES solicitud(id)
);

-- ============================================================
-- 11. REPORTES GENERADOS (CU8)
-- ============================================================

CREATE TABLE reporte (
    id SERIAL PRIMARY KEY,
    id_usuario INTEGER NOT NULL,
    tipo_reporte VARCHAR(100) NOT NULL,
    parametros TEXT,
    fecha_generacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reporte_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
);
