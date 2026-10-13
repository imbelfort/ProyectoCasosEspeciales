package Comunication;

import java.util.List;

public class HtmlBuilder {

    private static final String HTML_OPEN = "<html>";
    private static final String HTML_CLOSE = "</html>";
    private static final String BODY_OPEN = "<body style='font-family: Arial, Helvetica, sans-serif; background-color: #f4f6f9; color: #333333; margin: 0; padding: 20px;'>";
    private static final String BODY_CLOSE = "</body>";

    // ============================================================
    // CABECERAS PARA CASOS DE USO (SISTEMA CASOS ESPECIALES)
    // ============================================================

    public static final String[] HEADERS_USER = {
            "ID", "NOMBRE", "APELLIDOS", "CI", "EMAIL", "TELEFONO", "DIRECCION",
            "FOTO", "ESTADO", "PASSWORD", "FECHA_NAC", "CREADO", "MODIFICADO"
    };

    public static final String[] HEADERS_ROL = {
            "ID", "NOMBRE", "DESCRIPCION", "ESTADO", "FECHA_CREACION"
    };

    public static final String[] HEADERS_ESTUDIANTE = {
            "ID", "NOMBRE", "APELLIDOS", "CI", "EMAIL", "REGISTRO", "CARRERA", "SEMESTRE"
    };

    public static final String[] HEADERS_PERSONAL_CARRERA = {
            "ID", "NOMBRE", "APELLIDOS", "CI", "EMAIL", "CARGO", "CARRERA"
    };

    public static final String[] HEADERS_DIRECTOR_CARRERA = {
            "ID", "NOMBRE", "APELLIDOS", "CI", "EMAIL", "CARRERA", "GESTION_INICIO", "GESTION_FIN"
    };

    public static final String[] HEADERS_CARRERA = {
            "ID", "CODIGO", "NOMBRE", "FACULTAD"
    };

    public static final String[] HEADERS_PERIODO = {
            "ID", "GESTION", "FECHA_INICIO", "FECHA_FIN", "ESTADO"
    };

    public static final String[] HEADERS_MATERIA = {
            "ID", "SIGLA", "NOMBRE", "SEMESTRE", "CREDITOS"
    };

    public static final String[] HEADERS_GRUPO = {
            "ID", "MATERIA", "GESTION", "GRUPO", "DOCENTE", "CUPO_MAX", "CUPOS_DISP", "CUPO_EXC", "ESTADO"
    };

    public static final String[] HEADERS_SOLICITUD = {
            "ID", "CODIGO", "ESTUDIANTE", "REGISTRO", "GESTION", "TIPO_CASO", "ESTADO", "FECHA_SOLICITUD", "MOTIVO"
    };

    public static final String[] HEADERS_SOLICITUD_GRUPO = {
            "ID", "COD_SOLICITUD", "MATERIA", "GRUPO", "MOTIVO", "ESTADO"
    };

    public static final String[] HEADERS_ARCHIVO_SOLICITUD = {
            "ID", "ID_SOLICITUD", "NOMBRE_ARCHIVO", "TIPO", "FECHA_SUBIDA"
    };

    public static final String[] HEADERS_EVALUACION = {
            "ID", "SOLICITUD", "EVALUADOR", "DECISION", "JUSTIFICACION", "OBSERVACIONES", "FECHA_EVALUACION"
    };

    public static final String[] HEADERS_BLOQUEO_GRUPO = {
            "ID", "MATERIA", "GRUPO", "SOLICITUD", "CANTIDAD", "MOTIVO", "ESTADO", "AUTORIZADO_POR", "FECHA_BLOQUEO", "FECHA_RESOLUCION"
    };

    public static final String[] HEADERS_OFICIO_CPD = {
            "ID", "NUM_OFICIO", "SOLICITUD", "DESTINATARIO", "REFERENCIA", "RESPONSABLE", "FECHA_REMISION", "ESTADO"
    };

    public static final String[] HEADERS_RESPUESTA_CPD = {
            "ID", "NUM_OFICIO", "SOLICITUD", "COD_RESPUESTA", "RESULTADO", "RESPALDO", "FECHA_RESPUESTA", "FECHA_HABILITACION", "OBSERVACIONES"
    };

    public static final String[] HEADERS_HISTORIAL = {
            "ID", "SOLICITUD", "USUARIO", "ESTADO_ANTERIOR", "ESTADO_NUEVO", "ACCION", "OBSERVACION", "FECHA"
    };

    public static final String[] HEADERS_NOTIFICACION = {
            "ID", "SOLICITUD", "DESTINATARIO", "TIPO", "ASUNTO", "ESTADO", "INTENTOS", "FECHA_CREACION", "FECHA_ENVIO"
    };

    public static final String[] HEADERS_REPORTE_GENERAL = {
            "TOTAL_SOLICITUDES", "PENDIENTES_EVALUACION", "APROBADAS_DIRECCION", "RECHAZADAS_DIRECCION", "REMITIDAS_CPD", "HABILITADAS_CPD", "OBSERVADAS"
    };

    public static final String[] HEADERS_REPORTE_ESTADOS = {
            "ESTADO", "CANTIDAD", "PORCENTAJE"
    };

    public static final String[] HEADERS_REPORTE_PERIODOS = {
            "GESTION", "CARRERA", "TOTAL_SOLICITUDES", "HABILITADAS"
    };

    public static final String[] HEADERS_REPORTE_CUPOS = {
            "SIGLA", "MATERIA", "GRUPO", "CUPO_MAXIMO", "DISPONIBLES", "RESERVADOS", "ESTADO"
    };

    // ============================================================
    // CONSTRUCCIÓN DE TABLAS HTML
    // ============================================================

    public static String generarTabla(String titulo, String[] headers, List<String[]> filas) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN);
        sb.append("<head><meta charset='UTF-8'></head>");
        sb.append(BODY_OPEN);

        sb.append("<div style='max-width: 950px; margin: 0 auto; background: #ffffff; padding: 25px; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>");
        sb.append("<div style='border-bottom: 2px solid #003366; padding-bottom: 12px; margin-bottom: 20px;'>");
        sb.append("<h2 style='color: #003366; margin: 0;'>SISTEMA DE GESTION DE CASOS ESPECIALES ACADEMICOS</h2>");
        sb.append("<p style='color: #666; margin: 5px 0 0 0; font-size: 14px;'>").append(titulo).append("</p>");
        sb.append("</div>");

        if (filas == null || filas.isEmpty()) {
            sb.append("<p style='color: #888; font-style: italic; padding: 20px 0;'>No se encontraron registros disponibles.</p>");
        } else {
            sb.append("<div style='overflow-x: auto;'>");
            sb.append("<table style='width: 100%; border-collapse: collapse; font-size: 13px;'>");

            // Header
            sb.append("<thead><tr style='background-color: #003366; color: #ffffff;'>");
            for (String h : headers) {
                sb.append("<th style='padding: 10px 12px; text-align: left; border: 1px solid #002244;'>")
                  .append(h)
                  .append("</th>");
            }
            sb.append("</tr></thead>");

            // Body
            sb.append("<tbody>");
            boolean alternate = false;
            for (String[] fila : filas) {
                sb.append("<tr style='background-color: ").append(alternate ? "#f9fbfd" : "#ffffff").append(";'>");
                for (int i = 0; i < headers.length; i++) {
                    String valor = (i < fila.length && fila[i] != null) ? fila[i] : "";
                    sb.append("<td style='padding: 8px 12px; border: 1px solid #e1e8ed;'>")
                      .append(formatearValor(valor))
                      .append("</td>");
                }
                sb.append("</tr>");
                alternate = !alternate;
            }
            sb.append("</tbody>");
            sb.append("</table>");
            sb.append("</div>");
        }

        sb.append("<div style='margin-top: 25px; padding-top: 15px; border-top: 1px solid #e1e8ed; color: #888; font-size: 12px; text-align: right;'>");
        sb.append("Generado automáticamente por el Servidor de Correo - Facultad de Ciencias de la Computación y Telecomunicaciones");
        sb.append("</div>");

        sb.append("</div>");
        sb.append(BODY_CLOSE);
        sb.append(HTML_CLOSE);
        return sb.toString();
    }

    public static String generarMensajeExito(String titulo, String mensaje) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN);
        sb.append("<head><meta charset='UTF-8'></head>");
        sb.append(BODY_OPEN);
        sb.append("<div style='max-width: 650px; margin: 0 auto; background: #ffffff; padding: 25px; border-radius: 8px; border-left: 6px solid #28a745; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>");
        sb.append("<h2 style='color: #28a745; margin-top: 0;'>").append(titulo).append("</h2>");
        sb.append("<p style='font-size: 15px; color: #333333; line-height: 1.5;'>").append(mensaje).append("</p>");
        sb.append("<div style='margin-top: 20px; color: #888; font-size: 12px;'>FICCT - UAGRM | Sistema de Casos Especiales</div>");
        sb.append("</div>");
        sb.append(BODY_CLOSE);
        sb.append(HTML_CLOSE);
        return sb.toString();
    }

    public static String generarMensajeError(String titulo, String mensaje) {
        return generarMensajeErrorDetallado(titulo, null, mensaje, "Envie un correo con <b>help</b> para consultar la sintaxis valida de los comandos.");
    }

    public static String generarMensajeErrorDetallado(String titulo, String comandoRecibido, String detalleError, String sugerencia) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN);
        sb.append("<head><meta charset='UTF-8'></head>");
        sb.append(BODY_OPEN);
        sb.append("<div style='max-width: 700px; margin: 0 auto; background: #ffffff; padding: 25px; border-radius: 8px; border-left: 6px solid #dc3545; box-shadow: 0 4px 6px rgba(0,0,0,0.1); font-family: Arial, sans-serif;'>");
        sb.append("<h2 style='color: #dc3545; margin-top: 0; margin-bottom: 15px; font-size: 20px;'>&#9888; ").append(titulo).append("</h2>");

        if (comandoRecibido != null && !comandoRecibido.trim().isEmpty()) {
            sb.append("<div style='background: #f8f9fa; border: 1px solid #e9ecef; border-radius: 4px; padding: 10px 14px; margin-bottom: 15px;'>");
            sb.append("<strong style='color: #495057; font-size: 13px;'>Comando recibido:</strong><br>");
            sb.append("<code style='color: #c7254e; font-size: 14px; font-weight: bold;'>").append(comandoRecibido).append("</code>");
            sb.append("</div>");
        }

        sb.append("<div style='margin-bottom: 15px;'>");
        sb.append("<strong style='color: #333333; font-size: 14px;'>¿Qué falló exactamente?:</strong>");
        sb.append("<p style='margin: 5px 0 0 0; color: #721c24; background: #f8d7da; padding: 10px 12px; border-radius: 4px; font-size: 13px; line-height: 1.5;'>").append(detalleError).append("</p>");
        sb.append("</div>");

        if (sugerencia != null && !sugerencia.trim().isEmpty()) {
            sb.append("<div style='background: #e7f3fe; border-left: 4px solid #2196F3; padding: 10px 12px; border-radius: 4px; margin-bottom: 15px;'>");
            sb.append("<strong style='color: #0b5394; font-size: 13px;'>Sintaxis correcta o sugerencia:</strong><br>");
            sb.append("<span style='color: #1a252f; font-size: 13px;'>").append(sugerencia).append("</span>");
            sb.append("</div>");
        }

        sb.append("<div style='font-size: 12px; color: #888; border-top: 1px solid #eee; padding-top: 10px; margin-top: 15px;'>");
        sb.append("FICCT - UAGRM | Sistema de Casos Especiales Académicos. Envíe <b>help</b> para ver el manual completo.");
        sb.append("</div>");
        sb.append("</div>");
        sb.append(BODY_CLOSE);
        sb.append(HTML_CLOSE);
        return sb.toString();
    }

    public static String generarAyuda(String titulo, String contenidoTexto) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN);
        sb.append("<head><meta charset='UTF-8'></head>");
        sb.append(BODY_OPEN);
        sb.append("<div style='max-width: 750px; margin: 0 auto; background: #ffffff; padding: 25px; border-radius: 8px; border-left: 6px solid #0056b3; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>");
        sb.append("<h2 style='color: #0056b3; margin-top: 0;'>").append(titulo).append("</h2>");
        sb.append("<pre style='background: #f8f9fa; padding: 15px; border-radius: 4px; font-family: Consolas, monospace; font-size: 13px; line-height: 1.6; color: #212529; white-space: pre-wrap;'>")
          .append(contenidoTexto)
          .append("</pre>");
        sb.append("<div style='margin-top: 15px; color: #888; font-size: 12px;'>Envie el comando deseado en el campo SUBJECT del correo.</div>");
        sb.append("</div>");
        sb.append(BODY_CLOSE);
        sb.append(HTML_CLOSE);
        return sb.toString();
    }

    private static String formatearValor(String val) {
        if (val == null || val.trim().isEmpty()) {
            return "<span style='color: #aaa;'>-</span>";
        }
        // Badge para estados frecuentes
        if ("HABILITADA".equalsIgnoreCase(val) || "APROBADA".equalsIgnoreCase(val) || "ACTIVO".equalsIgnoreCase(val) || "CONFIRMADA_CPD".equalsIgnoreCase(val)) {
            return "<span style='background: #d4edda; color: #155724; padding: 3px 8px; border-radius: 4px; font-weight: bold; font-size: 11px;'>" + val + "</span>";
        }
        if ("RECHAZADA".equalsIgnoreCase(val) || "RECHAZADA_DIRECCION".equalsIgnoreCase(val) || "RECHAZADA_CPD".equalsIgnoreCase(val) || "BLOQUEADO".equalsIgnoreCase(val)) {
            return "<span style='background: #f8d7da; color: #721c24; padding: 3px 8px; border-radius: 4px; font-weight: bold; font-size: 11px;'>" + val + "</span>";
        }
        if ("ENVIADA".equalsIgnoreCase(val) || "REMITIDA_A_CPD".equalsIgnoreCase(val) || "RESERVADO".equalsIgnoreCase(val) || "EN_REVISION".equalsIgnoreCase(val)) {
            return "<span style='background: #cce5ff; color: #004085; padding: 3px 8px; border-radius: 4px; font-weight: bold; font-size: 11px;'>" + val + "</span>";
        }
        if (val.contains("OBSERVADA")) {
            return "<span style='background: #fff3cd; color: #856404; padding: 3px 8px; border-radius: 4px; font-weight: bold; font-size: 11px;'>" + val + "</span>";
        }
        return val;
    }
}