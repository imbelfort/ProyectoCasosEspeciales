package Utils;

import java.sql.SQLException;
import java.util.*;

public class ErrorExplainer {

    public static class ParamInfo {
        private final String nombre;
        private final String tipo;
        private final String descripcion;

        public ParamInfo(String nombre, String tipo, String descripcion) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.descripcion = descripcion;
        }

        public String getNombre() { return nombre; }
        public String getTipo() { return tipo; }
        public String getDescripcion() { return descripcion; }
    }

    public static class ErrorDetail {
        private String titulo = "Error al Procesar el Comando";
        private String comandoRecibido = "";
        private String queFallo = "";
        private String sugerenciaSemejanza = null;
        private String sintaxisCorrecta = "";
        private String ejemploReal = "";
        private List<ParamInfo> parametros = new ArrayList<ParamInfo>();
        private String modulo = "";

        public String getTitulo() { return titulo; }
        public void setTitulo(String titulo) { this.titulo = titulo; }

        public String getComandoRecibido() { return comandoRecibido; }
        public void setComandoRecibido(String comandoRecibido) { this.comandoRecibido = comandoRecibido; }

        public String getQueFallo() { return queFallo; }
        public void setQueFallo(String queFallo) { this.queFallo = queFallo; }

        public String getSugerenciaSemejanza() { return sugerenciaSemejanza; }
        public void setSugerenciaSemejanza(String sugerenciaSemejanza) { this.sugerenciaSemejanza = sugerenciaSemejanza; }

        public String getSintaxisCorrecta() { return sintaxisCorrecta; }
        public void setSintaxisCorrecta(String sintaxisCorrecta) { this.sintaxisCorrecta = sintaxisCorrecta; }

        public String getEjemploReal() { return ejemploReal; }
        public void setEjemploReal(String ejemploReal) { this.ejemploReal = ejemploReal; }

        public List<ParamInfo> getParametros() { return parametros; }
        public void addParametro(String nombre, String tipo, String descripcion) {
            this.parametros.add(new ParamInfo(nombre, tipo, descripcion));
        }

        public String getModulo() { return modulo; }
        public void setModulo(String modulo) { this.modulo = modulo; }
    }

    private static final String[] ENTIDADES_VALIDAS = {
            "usuario", "rol", "estudiante", "personal_carrera", "director_carrera",
            "carrera", "periodo", "materia", "grupo", "solicitud", "solicitud_grupo",
            "archivo_solicitud", "evaluacion", "bloqueo_grupo", "oficio_cpd",
            "respuesta_cpd", "historial", "notificacion", "reporte", "pago", "help"
    };

    private static final String[] ACCIONES_VALIDAS = {
            "listar", "agregar", "modificar", "eliminar", "consultar", "registrar",
            "evaluar", "bloquear", "liberar", "asignar", "mis_casos", "pendientes",
            "general", "estados", "periodos", "cupos", "mis_pagos", "oficios", "help"
    };

    /**
     * Diagnostica excepciones producidas durante la ejecución del comando.
     */
    public static ErrorDetail diagnosticarExcepcion(String subject, Exception ex, String emailFrom) {
        ErrorDetail ed = new ErrorDetail();
        ed.setComandoRecibido(subject != null ? subject.trim() : "");

        String rawMsg = ex.getMessage() != null ? ex.getMessage().trim() : ex.getClass().getSimpleName();
        String cmdLower = subject != null ? subject.trim().toLowerCase() : "";
        String[] tokens = cmdLower.split("\\s+");
        String entidad = tokens.length > 0 ? tokens[0] : "";
        String accion = tokens.length > 1 ? tokens[1] : "";
        ed.setModulo(entidad);

        if (ex instanceof NumberFormatException) {
            ed.setTitulo("Tipo de Dato Incorrecto: Se Esperaba un Número");
            String valorErroneo = extraerValorNumeroErroneo(rawMsg);
            if (valorErroneo != null && !valorErroneo.isEmpty()) {
                ed.setQueFallo("El valor <b>'" + valorErroneo + "'</b> no es un número entero válido. " +
                        "Uno de los parámetros enviados requiere un identificador numérico (ID) entero positivo.");
            } else {
                ed.setQueFallo("Uno de los parámetros ingresados debe ser un número entero (ID numérico), pero se recibió texto o caracteres no válidos.");
            }
            completarSintaxisPorComando(ed, entidad, accion);

        } else if (ex instanceof IllegalArgumentException) {
            ed.setTitulo("Parámetros Inválidos o Incompletos");

            if (rawMsg.contains("Formato:") || rawMsg.contains("Cantidad de parametros") || rawMsg.contains("Debe especificar")) {
                ed.setQueFallo("Los parámetros enviados no cumplen con la cantidad u orden requerido para este comando.<br>" +
                        "<b>Detalle del sistema:</b> " + rawMsg);
            } else if (rawMsg.contains("Decision invalida")) {
                ed.setTitulo("Valor de Decisión No Permitido");
                ed.setQueFallo("La decisión indicada no es válida. Las únicas decisiones aceptadas por Dirección de Carrera son: <b>APROBADA</b>, <b>RECHAZADA</b> u <b>OBSERVADA</b>.");
            } else if (rawMsg.toLowerCase(Locale.ROOT).contains("resultado")) {
                ed.setTitulo("Resultado de CPD Inválido");
                ed.setQueFallo("El resultado debe ser <b>HABILITADO</b>, <b>OBSERVADO</b> o <b>RECHAZADO</b>. Use <b>cpd responder id_oficio,id_solicitud,resultado,codigo_respuesta,observacion</b>.");
            } else {
                ed.setQueFallo(rawMsg);
            }
            completarSintaxisPorComando(ed, entidad, accion);

        } else if (ex instanceof SQLException) {
            ed.setTitulo("Error en Base de Datos");
            String msgLower = rawMsg.toLowerCase();

            if (msgLower.contains("violates foreign key constraint") || msgLower.contains("foreign key")) {
                ed.setTitulo("Registro Referenciado No Encontrado (Clave Foránea)");
                ed.setQueFallo("Uno de los identificadores (ID) referenciados (estudiante, materia, grupo, periodo o solicitud) <b>no existe en la base de datos</b>.");
            } else if (msgLower.contains("duplicate key") || msgLower.contains("unique constraint") || msgLower.contains("ya existe")) {
                ed.setTitulo("Registro Duplicado");
                ed.setQueFallo("Ya existe un registro con esos datos en el sistema (por ejemplo: CI, Email, Sigla o Código duplicado).<br><b>Detalle:</b> " + rawMsg);
            } else if (msgLower.contains("connection refused") || msgLower.contains("conexión") || msgLower.contains("connection")) {
                ed.setTitulo("Servidor de Base de Datos No Disponible");
                ed.setQueFallo("No se pudo establecer conexión con PostgreSQL en el servidor. Por favor comuníquese con el administrador.");
            } else if (msgLower.contains("no hay cupos suficientes")) {
                ed.setTitulo("Cupos Insuficientes en Grupo");
                ed.setQueFallo(rawMsg);
            } else {
                ed.setQueFallo("Ocurrió un error al ejecutar la operación en la base de datos: " + rawMsg);
            }
            completarSintaxisPorComando(ed, entidad, accion);

        } else {
            ed.setTitulo("Error Inesperado Durante la Ejecución");
            ed.setQueFallo("Ocurrió una excepción inesperada: <b>" + rawMsg + "</b>. Revise la sintaxis del comando enviado.");
            completarSintaxisPorComando(ed, entidad, accion);
        }

        return ed;
    }

    /**
     * Diagnostica comandos desconocidos o acciones inexistentes.
     */
    public static ErrorDetail diagnosticarComandoDesconocido(String subject, String contextoEntidad, String accionesValidas) {
        ErrorDetail ed = new ErrorDetail();
        ed.setComandoRecibido(subject != null ? subject.trim() : "");
        ed.setModulo(contextoEntidad);

        String trimmed = subject != null ? subject.trim() : "";
        String[] partes = trimmed.split("\\s+");

        if (contextoEntidad != null && !contextoEntidad.trim().isEmpty()) {
            if (partes.length <= 1) {
                // El usuario envió solo la entidad, ej: "solicitud"
                ed.setTitulo("Falta la Acción para el Módulo '" + contextoEntidad + "'");
                ed.setQueFallo("Ha especificado el módulo <b>" + contextoEntidad + "</b> pero <b>no indicó qué acción desea ejecutar</b> (por ejemplo: listar, registrar, consultar, etc.).");
                ed.setSintaxisCorrecta(contextoEntidad + " [accion] [parametros]");
                ed.setEjemploReal(contextoEntidad + " listar");
            } else {
                String accionIngresada = partes[1];
                ed.setTitulo("Acción No Válida: '" + accionIngresada + "'");
                ed.setQueFallo("La acción <b>'" + accionIngresada + "'</b> no existe para el módulo <b>" + contextoEntidad + "</b>.");

                String similar = buscarSemejanza(accionIngresada, ACCIONES_VALIDAS);
                if (similar != null) {
                    ed.setSugerenciaSemejanza("¿Quiso escribir <b>" + contextoEntidad + " " + similar + "</b>?");
                }
            }
            completarSintaxisPorComando(ed, contextoEntidad, partes.length > 1 ? partes[1] : "help");
        } else {
            ed.setTitulo("Comando No Reconocido");
            ed.setQueFallo("El texto ingresado no corresponde a ningún comando o módulo válido del sistema.");
            ed.setSintaxisCorrecta("[modulo] [accion] [parametros]");
            ed.setEjemploReal("solicitud listar");
        }

        return ed;
    }

    /**
     * Diagnostica errores léxicos o sintácticos reportados por el analizador.
     */
    public static ErrorDetail diagnosticarSintaxis(String subject, String tokenInvalido) {
        ErrorDetail ed = new ErrorDetail();
        ed.setComandoRecibido(subject != null ? subject.trim() : "");

        String trimmed = subject != null ? subject.trim() : "";
        if (trimmed.isEmpty()) {
            ed.setTitulo("Comando Vacío");
            ed.setQueFallo("El asunto (Subject) del correo llegó vacío. Debe enviar un comando para que el sistema pueda procesarlo.");
            ed.setSintaxisCorrecta("[modulo] [accion] [parametros]");
            ed.setEjemploReal("solicitud listar");
            return ed;
        }

        ed.setTitulo("Sintaxis No Válida o Término No Reconocido");

        if (tokenInvalido != null && !tokenInvalido.trim().isEmpty()) {
            String token = tokenInvalido.trim().toLowerCase();

            // Detectar si usó separadores incorrectos como punto y coma o barras
            if (token.contains(";") || token.contains("|")) {
                ed.setTitulo("Separador de Parámetros Incorrecto");
                ed.setQueFallo("Se detectó el carácter '" + token + "'. En este sistema los parámetros deben separarse exclusivamente con <b>comas (,)</b> y no con punto y coma (;) ni barras (|).");
                ed.setSintaxisCorrecta("[modulo] [accion] param1,param2,param3");
                ed.setEjemploReal("solicitud registrar 1,1,CORRECCION_NOTA,Error en nota");
                return ed;
            }

            // Detectar alias frecuentes
            String alias = resolverAlias(token);
            if (alias != null) {
                ed.setQueFallo("El término <b>'" + tokenInvalido + "'</b> no es el identificador oficial. En este sistema el módulo se denomina <b>" + alias + "</b>.");
                ed.setSugerenciaSemejanza("Utilice <b>" + alias + "</b> en su lugar. Ejemplo: <code>" + alias + " listar</code>");
                completarSintaxisPorComando(ed, alias, "listar");
                return ed;
            }

            // Buscar semejanza con entidades
            String similar = buscarSemejanza(token, ENTIDADES_VALIDAS);
            if (similar != null) {
                ed.setQueFallo("El módulo o palabra <b>'" + tokenInvalido + "'</b> no fue reconocido.");
                ed.setSugerenciaSemejanza("¿Quiso decir <b>" + similar + "</b>? (Ejemplo: <code>" + similar + " listar</code>)");
                completarSintaxisPorComando(ed, similar, "listar");
                return ed;
            }

            ed.setQueFallo("La palabra o símbolo <b>'" + tokenInvalido + "'</b> no corresponde a ningún módulo ni acción válida del sistema.");
        } else {
            ed.setQueFallo("La estructura del comando enviado no sigue el formato estándar: <b>[modulo] [accion] [parametros]</b>.");
        }

        ed.setSintaxisCorrecta("[modulo] [accion] [parametros]");
        ed.setEjemploReal("solicitud listar");
        return ed;
    }

    private static String resolverAlias(String token) {
        if ("alumnos".equals(token) || "alumno".equals(token) || "estudiantes".equals(token)) return "estudiante";
        if ("materias".equals(token) || "asignatura".equals(token) || "asignaturas".equals(token)) return "materia";
        if ("grupos".equals(token) || "paralelo".equals(token) || "paralelos".equals(token)) return "grupo";
        if ("solicitudes".equals(token) || "caso".equals(token) || "casos".equals(token)) return "solicitud";
        if ("usuarios".equals(token)) return "usuario";
        if ("roles".equals(token)) return "rol";
        if ("carreras".equals(token)) return "carrera";
        if ("periodos".equals(token) || "gestion".equals(token) || "semestre".equals(token)) return "periodo";
        if ("evaluaciones".equals(token) || "evaluar".equals(token)) return "evaluacion";
        if ("oficios".equals(token) || "oficio".equals(token)) return "oficio_cpd";
        if ("respuestas".equals(token) || "respuesta".equals(token)) return "respuesta_cpd";
        if ("cpd".equals(token)) return "respuesta_cpd";
        if ("reportes".equals(token)) return "reporte";
        if ("notificaciones".equals(token)) return "notificacion";
        if ("ayuda".equals(token) || "manual".equals(token) || "info".equals(token)) return "help";
        return null;
    }

    public static String buscarSemejanza(String palabra, String[] opciones) {
        if (palabra == null || palabra.trim().isEmpty()) return null;
        String p = palabra.trim().toLowerCase();
        String mejorCoincidencia = null;
        int menorDistancia = Integer.MAX_VALUE;

        for (String opcion : opciones) {
            int d = distanciaLevenshtein(p, opcion.toLowerCase());
            if (d < menorDistancia && d <= 3) {
                menorDistancia = d;
                mejorCoincidencia = opcion;
            }
        }
        return mejorCoincidencia;
    }

    private static int distanciaLevenshtein(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[s1.length()][s2.length()];
    }

    private static String extraerValorNumeroErroneo(String msg) {
        if (msg == null) return null;
        int idx = msg.indexOf("For input string: \"");
        if (idx != -1) {
            int start = idx + "For input string: \"".length();
            int end = msg.indexOf("\"", start);
            if (end != -1) {
                return msg.substring(start, end);
            }
        }
        return null;
    }

    private static void completarSintaxisPorComando(ErrorDetail ed, String entidad, String accion) {
        String ent = entidad != null ? entidad.toLowerCase().trim() : "";
        String act = accion != null ? accion.toLowerCase().trim() : "";

        if ("solicitud".equals(ent)) {
            if ("registrar".equals(act) || "agregar".equals(act)) {
                ed.setSintaxisCorrecta("enviar solicitud gestion,tipo_caso,motivo");
                ed.setEjemploReal("solicitud registrar 1, 1, CORRECCION_NOTA, Error en acta final de calificaciones");
                ed.addParametro("id_estudiante", "Número Entero", "ID único del estudiante en el sistema");
                ed.addParametro("gestion", "Texto", "Gestión académica con formato 1-2026 o 2-2026");
                ed.addParametro("tipo_caso", "Texto", "Tipo de solicitud (ej. CORRECCION_NOTA, LEVANTE_REQUISITO, INCLUSION_MATERIA)");
                ed.addParametro("motivo", "Texto", "Explicación y justificativo académico del caso especial");
            } else if ("consultar".equals(act)) {
                ed.setSintaxisCorrecta("solicitud consultar id_solicitud");
                ed.setEjemploReal("solicitud consultar 1");
                ed.addParametro("id_solicitud", "Número Entero", "ID de la solicitud que desea consultar");
            } else if ("periodos".equals(act)) {
                ed.setSintaxisCorrecta("solicitud periodos id_periodo_o_gestion");
                ed.setEjemploReal("solicitud periodos 2-2026");
                ed.addParametro("id_periodo_o_gestion", "ID o gestión", "Período académico que desea consultar");
            } else {
                ed.setSintaxisCorrecta("solicitud periodos id_periodo_o_gestion | solicitud consultar id_solicitud");
                ed.setEjemploReal("solicitud periodos 2-2026");
            }
        } else if ("usuario".equals(ent)) {
            ed.setSintaxisCorrecta("usuario listar");
            ed.setEjemploReal("usuario listar");
        } else if ("estudiante".equals(ent)) {
            if ("agregar".equals(act)) {
                ed.setSintaxisCorrecta("estudiante agregar id_usuario,registro_universitario,id_carrera,semestre_actual");
                ed.setEjemploReal("estudiante agregar 2, 219012345, 1, 6");
                ed.addParametro("id_usuario", "Número Entero", "ID de la cuenta de usuario existente");
                ed.addParametro("registro_universitario", "Texto / Número", "Registro de estudiante UAGRM");
                ed.addParametro("id_carrera", "Número Entero", "ID de la carrera asignada");
                ed.addParametro("semestre_actual", "Número Entero", "Nivel de semestre en curso (1 al 10)");
            } else {
                ed.setSintaxisCorrecta("estudiante listar | estudiante agregar id_usuario,registro,id_carrera,semestre");
                ed.setEjemploReal("estudiante listar");
            }
        } else if ("materia".equals(ent)) {
            if ("agregar".equals(act)) {
                ed.setSintaxisCorrecta("materia agregar sigla,nombre,nivel_semestre,creditos");
                ed.setEjemploReal("materia agregar INF210, Programacion II, 2, 5");
                ed.addParametro("sigla", "Texto", "Sigla oficial de la materia (ej. INF210)");
                ed.addParametro("nombre", "Texto", "Nombre de la materia");
                ed.addParametro("nivel_semestre", "Número Entero", "Semestre de la malla curricular");
                ed.addParametro("creditos", "Número Entero", "Cantidad de créditos académicos");
            } else {
                ed.setSintaxisCorrecta("materia listar | materia agregar sigla,nombre,semestre,creditos");
                ed.setEjemploReal("materia listar");
            }
        } else if ("grupo".equals(ent)) {
            if ("agregar".equals(act)) {
                ed.setSintaxisCorrecta("grupo agregar id_materia,id_periodo,sigla_grupo,docente,cupo_max,cupo_exc");
                ed.setEjemploReal("grupo agregar 1, 1, SC, Ing. Perez, 45, 10");
                ed.addParametro("id_materia", "Número Entero", "ID de la materia");
                ed.addParametro("id_periodo", "Número Entero", "ID de la gestión/periodo");
                ed.addParametro("sigla_grupo", "Texto", "Sigla del grupo (ej. SA, SB, SC)");
                ed.addParametro("docente", "Texto", "Nombre del docente asignado");
                ed.addParametro("cupo_max", "Número Entero", "Cupo máximo ordinario");
                ed.addParametro("cupo_exc", "Número Entero", "Cupo excepcional reservado para casos especiales");
            } else if ("consultar".equals(act)) {
                ed.setSintaxisCorrecta("grupo consultar id_grupo");
                ed.setEjemploReal("grupo consultar 1");
                ed.addParametro("id_grupo", "Número Entero", "ID del grupo a consultar disponibilidad");
            } else {
                ed.setSintaxisCorrecta("grupo listar | grupo consultar id_grupo | grupo agregar ...");
                ed.setEjemploReal("grupo listar");
            }
        } else if ("evaluacion".equals(ent)) {
            if ("registrar".equals(act) || "evaluar".equals(act)) {
                ed.setSintaxisCorrecta("evaluacion registrar id_solicitud,APROBADA|RECHAZADA,motivo[,observaciones]");
                ed.setEjemploReal("evaluacion registrar 1,APROBADA,Cumple con los requisitos academicos,Remitir a CPD");
                ed.addParametro("id_solicitud", "Número Entero", "ID de la solicitud evaluada");
                ed.addParametro("decision", "Enum (APROBADA|RECHAZADA)", "Resolución de Dirección de Carrera");
                ed.addParametro("motivo", "Texto", "Motivo obligatorio del dictamen");
                ed.addParametro("observaciones", "Texto (Opcional)", "Instrucciones o notas adicionales");
            } else {
                ed.setSintaxisCorrecta("evaluacion listar | evaluacion registrar ...");
                ed.setEjemploReal("evaluacion listar");
            }
        } else if ("bloqueo_grupo".equals(ent)) {
            if ("bloquear".equals(act) || "agregar".equals(act)) {
                ed.setSintaxisCorrecta("bloqueo_grupo bloquear id_grupo,id_solicitud,cantidad,motivo");
                ed.setEjemploReal("bloqueo_grupo bloquear 1,1,1,Reserva de cupo para caso aprobado");
            } else if ("liberar".equals(act)) {
                ed.setSintaxisCorrecta("bloqueo_grupo liberar id_bloqueo,observacion");
                ed.setEjemploReal("bloqueo_grupo liberar 1, Estudiante regularizó inscripción por ventanilla");
            } else {
                ed.setSintaxisCorrecta("bloqueo_grupo listar | bloqueo_grupo bloquear ... | bloqueo_grupo liberar ...");
                ed.setEjemploReal("bloqueo_grupo listar");
            }
        } else if ("oficio_cpd".equals(ent)) {
            ed.setSintaxisCorrecta(
                    "oficio_cpd remitir gestion,correo_cpd,referencia,detalle | "
                    + "oficio_cpd listar | oficio_cpd generar id_oficio_pendiente");
            ed.setEjemploReal(
                    "oficio_cpd remitir 2-2026,cpd@uagrm.edu.bo,Habilitacion de casos especiales, "
                    + "Solicitudes aceptadas con comprobante de pago");
        } else if ("respuesta_cpd".equals(ent)) {
            ed.setSintaxisCorrecta(
                    "cpd pendientes | oficio_cpd consultar id_oficio | "
                    + "cpd responder id_oficio,id_solicitud,"
                    + "HABILITADO|OBSERVADO|RECHAZADO,codigo_respuesta,observacion");
            ed.setEjemploReal(
                    "cpd responder 5,25,HABILITADO,CPD-2026-0015,"
                    + "Algebra I habilitada e inscrita correctamente");
        } else if ("historial".equals(ent)) {
            ed.setSintaxisCorrecta("historial listar | historial consultar id_solicitud");
            ed.setEjemploReal("historial consultar 1");
        } else if ("reporte".equals(ent)) {
            ed.setSintaxisCorrecta("reporte general | reporte estados | reporte periodos | reporte cupos");
            ed.setEjemploReal("reporte general");
        } else {
            ed.setSintaxisCorrecta("[modulo] [accion] [parametros]");
            ed.setEjemploReal("solicitud listar");
        }
    }
}
