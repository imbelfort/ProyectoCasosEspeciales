package Utils;

public class Token {

    private int name;
    private int attribute;

    public static final int CU = 0;
    public static final int ACTION = 1;
    public static final int PARAMS = 2;
    public static final int END = 3;
    public static final int ERROR = 4;

    // Entidades / Casos de Uso (100 - 199)
    public static final int USUARIO = 100;
    public static final int ROL = 101;
    public static final int ESTUDIANTE = 102;
    public static final int PERSONAL_CARRERA = 103;
    public static final int DIRECTOR_CARRERA = 104;
    public static final int CARRERA = 105;
    public static final int PERIODO = 106;
    public static final int MATERIA = 107;
    public static final int GRUPO = 108;
    public static final int HORARIO_GRUPO = 109;
    public static final int SOLICITUD = 110;
    public static final int SOLICITUD_GRUPO = 111;
    public static final int ARCHIVO_SOLICITUD = 112;
    public static final int EVALUACION = 113;
    public static final int BLOQUEO_GRUPO = 114;
    public static final int OFICIO_CPD = 115;
    public static final int RESPUESTA_CPD = 116;
    public static final int HISTORIAL = 117;
    public static final int NOTIFICACION = 118;
    public static final int REPORTE = 119;

    // Acciones (200 - 299)
    public static final int ELIMINAR = 200;
    public static final int MODIFICAR = 201;
    public static final int LISTAR = 202;
    public static final int AGREGAR = 203;
    public static final int MOSTRAR = 204;
    public static final int GENERAR = 205;
    public static final int HELP = 206;
    public static final int CONSULTAR = 207;
    public static final int REGISTRAR = 208;
    public static final int EVALUAR = 209;
    public static final int APROBAR = 210;
    public static final int RECHAZAR = 211;
    public static final int OBSERVAR = 212;
    public static final int BLOQUEAR = 213;
    public static final int LIBERAR = 214;
    public static final int ASIGNAR = 215;
    public static final int GENERAL = 216;
    public static final int ESTADOS = 217;
    public static final int PERIODOS = 218;
    public static final int CUPOS = 219;
    public static final int MIS_CASOS = 220;
    public static final int PENDIENTES = 221;

    public static final int ERROR_COMMAND = 300;
    public static final int ERROR_CHARACTER = 301;

    public static final String LEXEME_CU = "caso de uso";
    public static final String LEXEME_ACTION = "action";
    public static final String LEXEME_PARAMS = "params";
    public static final String LEXEME_END = "end";
    public static final String LEXEME_ERROR = "error";

    public static final String LEXEME_USUARIO = "usuario";
    public static final String LEXEME_ROL = "rol";
    public static final String LEXEME_ESTUDIANTE = "estudiante";
    public static final String LEXEME_PERSONAL_CARRERA = "personal_carrera";
    public static final String LEXEME_DIRECTOR_CARRERA = "director_carrera";
    public static final String LEXEME_CARRERA = "carrera";
    public static final String LEXEME_PERIODO = "periodo";
    public static final String LEXEME_MATERIA = "materia";
    public static final String LEXEME_GRUPO = "grupo";
    public static final String LEXEME_HORARIO_GRUPO = "horario_grupo";
    public static final String LEXEME_SOLICITUD = "solicitud";
    public static final String LEXEME_SOLICITUD_GRUPO = "solicitud_grupo";
    public static final String LEXEME_ARCHIVO_SOLICITUD = "archivo_solicitud";
    public static final String LEXEME_EVALUACION = "evaluacion";
    public static final String LEXEME_BLOQUEO_GRUPO = "bloqueo_grupo";
    public static final String LEXEME_OFICIO_CPD = "oficio_cpd";
    public static final String LEXEME_RESPUESTA_CPD = "respuesta_cpd";
    public static final String LEXEME_HISTORIAL = "historial";
    public static final String LEXEME_NOTIFICACION = "notificacion";
    public static final String LEXEME_REPORTE = "reporte";

    public static final String LEXEME_ELIMINAR = "eliminar";
    public static final String LEXEME_MODIFICAR = "modificar";
    public static final String LEXEME_LISTAR = "listar";
    public static final String LEXEME_AGREGAR = "agregar";
    public static final String LEXEME_MOSTRAR = "mostrar";
    public static final String LEXEME_GENERAR = "generar";
    public static final String LEXEME_CONSULTAR = "consultar";
    public static final String LEXEME_HELP = "help";
    public static final String LEXEME_REGISTRAR = "registrar";
    public static final String LEXEME_EVALUAR = "evaluar";
    public static final String LEXEME_APROBAR = "aprobar";
    public static final String LEXEME_RECHAZAR = "rechazar";
    public static final String LEXEME_OBSERVAR = "observar";
    public static final String LEXEME_BLOQUEAR = "bloquear";
    public static final String LEXEME_LIBERAR = "liberar";
    public static final String LEXEME_ASIGNAR = "asignar";
    public static final String LEXEME_GENERAL = "general";
    public static final String LEXEME_ESTADOS = "estados";
    public static final String LEXEME_PERIODOS = "periodos";
    public static final String LEXEME_CUPOS = "cupos";
    public static final String LEXEME_MIS_CASOS = "mis_casos";
    public static final String LEXEME_PENDIENTES = "pendientes";

    public static final String LEXEME_ERROR_COMMAND = "UNKNOWN COMMAND";
    public static final String LEXEME_ERROR_CHARACTER = "UNKNOWN CHARACTER";

    public Token() {
    }

    public Token(String token) {
        int id = findByLexeme(token);

        if (id != -1) {
            if (100 <= id && id < 200) {
                this.name = CU;
                this.attribute = id;
            } else if (200 <= id && id < 300) {
                this.name = ACTION;
                this.attribute = id;
            } else {
                this.name = ERROR;
                this.attribute = ERROR_COMMAND;
            }
        } else {
            this.name = ERROR;
            this.attribute = ERROR_COMMAND;
        }
    }

    public Token(int name) {
        this.name = name;
    }

    public Token(int name, int attribute) {
        this.name = name;
        this.attribute = attribute;
    }

    public int getName() {
        return name;
    }

    public void setName(int name) {
        this.name = name;
    }

    public int getAttribute() {
        return attribute;
    }

    public void setAttribute(int attribute) {
        this.attribute = attribute;
    }

    @Override
    public String toString() {
        if (0 <= name && name <= 1) {
            return "< " + getStringToken(name) + " , " + getStringToken(attribute) + ">";
        } else if (name == 2) {
            return "< " + getStringToken(name) + " , " + attribute + ">";
        } else if (name == 3) {
            return "< " + getStringToken(name) + " , _______ >";
        } else if (name == 4) {
            return "< " + getStringToken(name) + " , " + getStringToken(attribute) + ">";
        }
        return "< TOKEN , DESCONOCIDO>";
    }

    public String getStringToken(int token) {
        switch (token) {
            case CU: return LEXEME_CU;
            case ACTION: return LEXEME_ACTION;
            case PARAMS: return LEXEME_PARAMS;
            case END: return LEXEME_END;
            case ERROR: return LEXEME_ERROR;

            case USUARIO: return LEXEME_USUARIO;
            case ROL: return LEXEME_ROL;
            case ESTUDIANTE: return LEXEME_ESTUDIANTE;
            case PERSONAL_CARRERA: return LEXEME_PERSONAL_CARRERA;
            case DIRECTOR_CARRERA: return LEXEME_DIRECTOR_CARRERA;
            case CARRERA: return LEXEME_CARRERA;
            case PERIODO: return LEXEME_PERIODO;
            case MATERIA: return LEXEME_MATERIA;
            case GRUPO: return LEXEME_GRUPO;
            case HORARIO_GRUPO: return LEXEME_HORARIO_GRUPO;
            case SOLICITUD: return LEXEME_SOLICITUD;
            case SOLICITUD_GRUPO: return LEXEME_SOLICITUD_GRUPO;
            case ARCHIVO_SOLICITUD: return LEXEME_ARCHIVO_SOLICITUD;
            case EVALUACION: return LEXEME_EVALUACION;
            case BLOQUEO_GRUPO: return LEXEME_BLOQUEO_GRUPO;
            case OFICIO_CPD: return LEXEME_OFICIO_CPD;
            case RESPUESTA_CPD: return LEXEME_RESPUESTA_CPD;
            case HISTORIAL: return LEXEME_HISTORIAL;
            case NOTIFICACION: return LEXEME_NOTIFICACION;
            case REPORTE: return LEXEME_REPORTE;

            case ELIMINAR: return LEXEME_ELIMINAR;
            case MODIFICAR: return LEXEME_MODIFICAR;
            case LISTAR: return LEXEME_LISTAR;
            case AGREGAR: return LEXEME_AGREGAR;
            case MOSTRAR: return LEXEME_MOSTRAR;
            case GENERAR: return LEXEME_GENERAR;
            case CONSULTAR: return LEXEME_CONSULTAR;
            case HELP: return LEXEME_HELP;
            case REGISTRAR: return LEXEME_REGISTRAR;
            case EVALUAR: return LEXEME_EVALUAR;
            case APROBAR: return LEXEME_APROBAR;
            case RECHAZAR: return LEXEME_RECHAZAR;
            case OBSERVAR: return LEXEME_OBSERVAR;
            case BLOQUEAR: return LEXEME_BLOQUEAR;
            case LIBERAR: return LEXEME_LIBERAR;
            case ASIGNAR: return LEXEME_ASIGNAR;
            case GENERAL: return LEXEME_GENERAL;
            case ESTADOS: return LEXEME_ESTADOS;
            case PERIODOS: return LEXEME_PERIODOS;
            case CUPOS: return LEXEME_CUPOS;
            case MIS_CASOS: return LEXEME_MIS_CASOS;
            case PENDIENTES: return LEXEME_PENDIENTES;

            case ERROR_COMMAND: return LEXEME_ERROR_COMMAND;
            case ERROR_CHARACTER: return LEXEME_ERROR_CHARACTER;
            default: return "N: " + token;
        }
    }

    private int findByLexeme(String lexeme) {
        if (lexeme == null) {
            return -1;
        }

        String normalized = lexeme.trim().toLowerCase();

        switch (normalized) {
            case LEXEME_CU: return CU;
            case LEXEME_ACTION: return ACTION;
            case LEXEME_PARAMS: return PARAMS;
            case LEXEME_END: return END;
            case LEXEME_ERROR: return ERROR;

            case LEXEME_USUARIO: return USUARIO;
            case LEXEME_ROL: return ROL;
            case LEXEME_ESTUDIANTE: return ESTUDIANTE;
            case LEXEME_PERSONAL_CARRERA: return PERSONAL_CARRERA;
            case LEXEME_DIRECTOR_CARRERA: return DIRECTOR_CARRERA;
            case LEXEME_CARRERA: return CARRERA;
            case LEXEME_PERIODO: return PERIODO;
            case LEXEME_MATERIA: return MATERIA;
            case LEXEME_GRUPO: return GRUPO;
            case LEXEME_HORARIO_GRUPO: return HORARIO_GRUPO;
            case LEXEME_SOLICITUD: return SOLICITUD;
            case LEXEME_SOLICITUD_GRUPO: return SOLICITUD_GRUPO;
            case LEXEME_ARCHIVO_SOLICITUD: return ARCHIVO_SOLICITUD;
            case LEXEME_EVALUACION: return EVALUACION;
            case LEXEME_BLOQUEO_GRUPO: return BLOQUEO_GRUPO;
            case LEXEME_OFICIO_CPD: return OFICIO_CPD;
            case LEXEME_RESPUESTA_CPD: return RESPUESTA_CPD;
            case LEXEME_HISTORIAL: return HISTORIAL;
            case LEXEME_NOTIFICACION: return NOTIFICACION;
            case LEXEME_REPORTE: return REPORTE;

            case LEXEME_ELIMINAR: return ELIMINAR;
            case LEXEME_MODIFICAR: return MODIFICAR;
            case LEXEME_LISTAR: return LISTAR;
            case LEXEME_AGREGAR: return AGREGAR;
            case LEXEME_MOSTRAR: return MOSTRAR;
            case LEXEME_GENERAR: return GENERAR;
            case LEXEME_CONSULTAR: return CONSULTAR;
            case LEXEME_HELP: return HELP;
            case LEXEME_REGISTRAR: return REGISTRAR;
            case LEXEME_EVALUAR: return EVALUAR;
            case LEXEME_APROBAR: return APROBAR;
            case LEXEME_RECHAZAR: return RECHAZAR;
            case LEXEME_OBSERVAR: return OBSERVAR;
            case LEXEME_BLOQUEAR: return BLOQUEAR;
            case LEXEME_LIBERAR: return LIBERAR;
            case LEXEME_ASIGNAR: return ASIGNAR;
            case LEXEME_GENERAL: return GENERAL;
            case LEXEME_ESTADOS: return ESTADOS;
            case LEXEME_PERIODOS: return PERIODOS;
            case LEXEME_CUPOS: return CUPOS;
            case LEXEME_MIS_CASOS: return MIS_CASOS;
            case LEXEME_PENDIENTES: return PENDIENTES;

            case LEXEME_ERROR_COMMAND: return ERROR_COMMAND;
            case LEXEME_ERROR_CHARACTER: return ERROR_CHARACTER;
            default: return -1;
        }
    }
}