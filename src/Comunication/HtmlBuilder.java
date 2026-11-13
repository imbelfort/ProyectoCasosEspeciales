package Comunication;

import Utils.ErrorExplainer;
import Utils.ErrorExplainer.ErrorDetail;
import Utils.ErrorExplainer.ParamInfo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HtmlBuilder {

    private static final String HTML_OPEN = "<!DOCTYPE html><html>";
    private static final String HTML_CLOSE = "</html>";
    private static final String HEAD_CONTENT =
            "<head>" +
            "<meta charset='UTF-8'>" +
            "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
            "<style>" +
            "body { margin: 0; padding: 0; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; -webkit-font-smoothing: antialiased; }" +
            "table { border-collapse: collapse; }" +
            "code { font-family: 'Consolas', 'Courier New', monospace; }" +
            "</style>" +
            "</head>";

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

    public static final String[] HEADERS_PAGO = {
            "ID", "ID_SOLICITUD", "CODIGO_SOLICITUD", "GESTION",
            "COMPROBANTE", "TIPO", "FECHA_SUBIDA", "ESTADO_SOLICITUD"
    };

    public static final String[] HEADERS_EVALUACION = {
            "ID", "SOLICITUD", "EVALUADOR", "DECISION", "JUSTIFICACION", "OBSERVACIONES", "FECHA_EVALUACION"
    };

    public static final String[] HEADERS_BLOQUEO_GRUPO = {
            "ID", "MATERIA", "GRUPO", "SOLICITUD", "CANTIDAD", "MOTIVO", "ESTADO", "AUTORIZADO_POR", "FECHA_BLOQUEO", "FECHA_RESOLUCION"
    };

    public static final String[] HEADERS_OFICIO_CPD = {
            "ID", "NUM_OFICIO", "SOLICITUD", "PERIODO", "DESTINATARIO", "REFERENCIA", "RESPONSABLE", "FECHA_REMISION", "ESTADO"
    };

    public static final String[] HEADERS_SOLICITUDES_OFICIO_CPD = {
            "ID_SOLICITUD", "CODIGO", "ESTUDIANTE", "PERIODO",
            "MATERIAS Y GRUPOS", "COMPROBANTES", "ESTADO"
    };

    public static final String[] HEADERS_PENDIENTES_CPD = {
            "ID_OFICIO", "NUMERO_OFICIO", "ID_SOLICITUD", "CODIGO_SOLICITUD",
            "ESTUDIANTE", "PERIODO", "ESTADO"
    };

    public static final String[] HEADERS_RESPUESTA_CPD = {
            "ID", "NUM_OFICIO", "SOLICITUD", "COD_RESPUESTA", "RESULTADO",
            "RESPONSABLE_CPD", "FECHA_RESPUESTA", "FECHA_HABILITACION", "OBSERVACIONES"
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
    // PLANTILLAS BASE Y CABECERA INSTITUCIONAL
    // ============================================================

    private static String getHeaderHtml(String moduloPill, String submodulo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style='background: #0f172a; background-image: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #1e3a8a 100%); padding: 22px 26px; border-bottom: 3px solid #2563eb;'>");
        sb.append("<table style='width: 100%; border: none; border-collapse: collapse;'><tr>");
        sb.append("<td style='vertical-align: middle;'>");
        sb.append("<span style='background: rgba(255,255,255,0.12); color: #93c5fd; font-size: 11px; font-weight: 700; letter-spacing: 1.5px; padding: 4px 10px; border-radius: 12px; text-transform: uppercase;'>UAGRM • FICCT</span>");
        sb.append("<h1 style='color: #ffffff; font-size: 18px; font-weight: 700; margin: 8px 0 2px 0; letter-spacing: -0.3px;'>SISTEMA DE GESTION DE CASOS ESPECIALES</h1>");
        sb.append("<p style='color: #94a3b8; font-size: 12px; margin: 0;'>Servidor de Correo Electronico Institucional &bull; ").append(submodulo != null ? submodulo : "Atencion Automatizada").append("</p>");
        sb.append("</td>");
        if (moduloPill != null && !moduloPill.isEmpty()) {
            sb.append("<td style='text-align: right; vertical-align: middle;'>");
            sb.append("<span style='background: rgba(37,99,235,0.25); color: #bfdbfe; border: 1px solid rgba(147,197,253,0.3); font-size: 11px; font-weight: 600; padding: 5px 12px; border-radius: 20px;'>").append(moduloPill).append("</span>");
            sb.append("</td>");
        }
        sb.append("</tr></table>");
        sb.append("</div>");
        return sb.toString();
    }

    private static String getFooterHtml() {
        String fecha = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        return "<div style='margin-top: 26px; padding-top: 16px; border-top: 1px solid #e2e8f0; text-align: center; color: #94a3b8; font-size: 11px; line-height: 1.5;'>" +
                "Universidad Autónoma Gabriel René Moreno &bull; Facultad de Ciencias de la Computación y Telecomunicaciones<br>" +
                "Respuesta generada automáticamente el <b>" + fecha + "</b> &bull; Envíe <b>help</b> para ver la guía completa." +
                "</div>";
    }

    // ============================================================
    // 1. CONSTRUCCIÓN DE TABLAS HTML
    // ============================================================

    public static String generarTabla(String titulo, String[] headers, List<String[]> filas) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN).append(HEAD_CONTENT);
        sb.append("<body style='margin: 0; padding: 20px; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif;'>");

        sb.append("<div style='max-width: 960px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 16px rgba(15,23,42,0.08); border: 1px solid #e2e8f0;'>");
        sb.append(getHeaderHtml("CONSULTA", "Modulo de Datos"));

        sb.append("<div style='padding: 24px 26px;'>");

        // Barra de Título y Conteo
        int total = filas != null ? filas.size() : 0;
        sb.append("<div style='display: flex; justify-content: space-between; align-items: center; margin-bottom: 18px; padding-bottom: 12px; border-bottom: 2px solid #f1f5f9;'>");
        sb.append("<table style='width: 100%;'><tr>");
        sb.append("<td>");
        sb.append("<h2 style='color: #0f172a; font-size: 17px; margin: 0; font-weight: 700;'>&#128196; ").append(titulo).append("</h2>");
        sb.append("</td>");
        sb.append("<td style='text-align: right;'>");
        sb.append("<span style='background: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd; font-size: 12px; font-weight: 700; padding: 4px 10px; border-radius: 16px;'>");
        sb.append(total).append(total == 1 ? " registro" : " registros");
        sb.append("</span>");
        sb.append("</td>");
        sb.append("</tr></table>");
        sb.append("</div>");

        if (filas == null || filas.isEmpty()) {
            sb.append("<div style='background: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 8px; padding: 32px 20px; text-align: center; margin: 20px 0;'>");
            sb.append("<div style='font-size: 28px; margin-bottom: 8px;'>&#128194;</div>");
            sb.append("<strong style='color: #475569; font-size: 14px;'>No se encontraron registros disponibles</strong>");
            sb.append("<p style='color: #94a3b8; font-size: 12px; margin: 6px 0 0 0;'>Actualmente no existen datos cargados para los criterios solicitados.</p>");
            sb.append("</div>");
        } else {
            sb.append("<div style='overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 8px;'>");
            sb.append("<table style='width: 100%; border-collapse: collapse; font-size: 12px;'>");

            // Encabezado
            sb.append("<thead><tr style='background-color: #0f172a; color: #ffffff;'>");
            for (String h : headers) {
                sb.append("<th style='padding: 10px 12px; text-align: left; font-size: 11px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.6px; border: none; color: #f8fafc;'>")
                  .append(h)
                  .append("</th>");
            }
            sb.append("</tr></thead>");

            // Filas
            sb.append("<tbody>");
            boolean alternate = false;
            for (String[] fila : filas) {
                String bg = alternate ? "#f8fafc" : "#ffffff";
                sb.append("<tr style='background-color: ").append(bg).append(";'>");
                for (int i = 0; i < headers.length; i++) {
                    String valor = (i < fila.length && fila[i] != null) ? fila[i] : "";
                    sb.append("<td style='padding: 9px 12px; border-top: 1px solid #e2e8f0; color: #334155; vertical-align: middle;'>")
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

        sb.append(getFooterHtml());
        sb.append("</div>"); // Fin padding
        sb.append("</div>"); // Fin container
        sb.append("</body>");
        sb.append(HTML_CLOSE);
        return sb.toString();
    }

    public static String generarDetalleSolicitud(
            String[] solicitud,
            List<String[]> grupos,
            List<String[]> documentos,
            List<String[]> evaluaciones
    ) {
        String codigo = solicitud != null && solicitud.length > 1
                ? solicitud[1] : "";
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN).append(HEAD_CONTENT);
        sb.append("<body style='margin: 0; padding: 20px; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif;'>");
        sb.append("<div style='max-width: 960px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 16px rgba(15,23,42,0.08); border: 1px solid #e2e8f0;'>");
        sb.append(getHeaderHtml("EXPEDIENTE", "Solicitud de Caso Especial"));
        sb.append("<div style='padding: 24px 26px;'>");
        sb.append("<h2 style='color: #0f172a; font-size: 17px; margin: 0 0 18px; padding-bottom: 12px; border-bottom: 2px solid #f1f5f9;'>")
                .append("Detalle de solicitud ").append(escapeHtml(codigo)).append("</h2>");

        if (solicitud != null) {
            String[] labels = {
                "ID", "CÓDIGO", "ESTUDIANTE", "REGISTRO", "PERÍODO",
                "TIPO DE CASO", "ESTADO", "FECHA DE SOLICITUD", "MOTIVO",
                "OBSERVACIONES", "MOTIVO DE RECHAZO"
            };
            sb.append("<h3 style='color: #1e3a8a; font-size: 14px; margin: 20px 0 10px;'>Datos de la solicitud</h3>");
            sb.append("<table style='width: 100%; border: 1px solid #e2e8f0; border-collapse: collapse; font-size: 12px;'>");
            for (int i = 0; i < labels.length; i++) {
                String valor = i < solicitud.length && solicitud[i] != null
                        ? solicitud[i] : "";
                String fondo = i % 2 == 0 ? "#f8fafc" : "#ffffff";
                sb.append("<tr style='background: ").append(fondo).append(";'>")
                        .append("<th style='width: 22%; padding: 9px 12px; text-align: left; color: #475569; border-bottom: 1px solid #e2e8f0;'>")
                        .append(escapeHtml(labels[i])).append("</th>")
                        .append("<td style='padding: 9px 12px; color: #1e293b; border-bottom: 1px solid #e2e8f0; white-space: pre-wrap;'>")
                        .append(formatearValor(valor)).append("</td></tr>");
            }
            sb.append("</table>");
        }

        appendTablaExpediente(sb, "Materias y grupos solicitados",
                HEADERS_SOLICITUD_GRUPO, grupos);
        appendTablaExpediente(sb, "Documentos adjuntos",
                HEADERS_ARCHIVO_SOLICITUD, documentos);
        appendTablaExpediente(sb, "Evaluaciones de Dirección",
                HEADERS_EVALUACION, evaluaciones);

        sb.append(getFooterHtml());
        sb.append("</div></div></body>").append(HTML_CLOSE);
        return sb.toString();
    }

    private static void appendTablaExpediente(
            StringBuilder sb,
            String titulo,
            String[] headers,
            List<String[]> filas
    ) {
        sb.append("<h3 style='color: #1e3a8a; font-size: 14px; margin: 24px 0 10px;'>")
                .append(escapeHtml(titulo)).append("</h3>");
        if (filas == null || filas.isEmpty()) {
            sb.append("<div style='padding: 14px; background: #f8fafc; border: 1px dashed #cbd5e1; border-radius: 6px; color: #64748b; font-size: 12px;'>")
                    .append("No hay registros asociados.").append("</div>");
            return;
        }
        sb.append("<div style='overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 8px;'>")
                .append("<table style='width: 100%; border-collapse: collapse; font-size: 12px;'>")
                .append("<thead><tr style='background: #0f172a; color: #ffffff;'>");
        for (String header : headers) {
            sb.append("<th style='padding: 10px 12px; text-align: left; font-size: 11px; text-transform: uppercase;'>")
                    .append(escapeHtml(header)).append("</th>");
        }
        sb.append("</tr></thead><tbody>");
        boolean alternar = false;
        for (String[] fila : filas) {
            sb.append("<tr style='background: ")
                    .append(alternar ? "#f8fafc" : "#ffffff").append(";'>");
            for (int i = 0; i < headers.length; i++) {
                String valor = fila != null && i < fila.length ? fila[i] : "";
                sb.append("<td style='padding: 9px 12px; border-top: 1px solid #e2e8f0; color: #334155; vertical-align: top;'>")
                        .append(formatearValor(valor)).append("</td>");
            }
            sb.append("</tr>");
            alternar = !alternar;
        }
        sb.append("</tbody></table></div>");
    }

    // ============================================================
    // 2. CONSTRUCCIÓN DE MENSAJE DE ÉXITO
    // ============================================================

    public static String generarMensajeExito(String titulo, String mensaje) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN).append(HEAD_CONTENT);
        sb.append("<body style='margin: 0; padding: 20px; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif;'>");

        sb.append("<div style='max-width: 680px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 16px rgba(15,23,42,0.08); border: 1px solid #e2e8f0;'>");
        sb.append(getHeaderHtml("CONFIRMACIÓN", "Operación Completada"));

        sb.append("<div style='padding: 26px 28px;'>");

        // Alerta de éxito
        sb.append("<div style='background: #f0fdf4; border: 1px solid #bbf7d0; border-left: 6px solid #16a34a; border-radius: 8px; padding: 18px 20px; margin-bottom: 20px;'>");
        sb.append("<table style='width: 100%; border-collapse: collapse;'><tr>");
        sb.append("<td style='width: 38px; vertical-align: top;'>");
        sb.append("<div style='width: 32px; height: 32px; background: #16a34a; color: #ffffff; border-radius: 50%; text-align: center; line-height: 32px; font-size: 18px; font-weight: bold;'>&#10003;</div>");
        sb.append("</td>");
        sb.append("<td style='vertical-align: top; padding-left: 10px;'>");
        sb.append("<h2 style='color: #166534; font-size: 17px; margin: 0 0 6px 0; font-weight: 700;'>").append(titulo).append("</h2>");
        sb.append("<p style='font-size: 14px; color: #14532d; line-height: 1.5; margin: 0;'>").append(mensaje).append("</p>");
        sb.append("</td>");
        sb.append("</tr></table>");
        sb.append("</div>");

        sb.append("<div style='background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 6px; padding: 12px 16px; font-size: 12px; color: #64748b;'>");
        sb.append("<b>Nota:</b> La operación ha quedado registrada en la bitácora institucional de trazabilidad. Puede consultar el estado en cualquier momento enviando <code>solicitud mis_casos</code>.");
        sb.append("</div>");

        sb.append(getFooterHtml());
        sb.append("</div>"); // Fin padding
        sb.append("</div>"); // Fin container
        sb.append("</body>");
        sb.append(HTML_CLOSE);
        return sb.toString();
    }

    public static String generarResultadoEvaluacion(
            int idSolicitud,
            String codigo,
            String decision,
            String estado,
            String motivo
    ) {
        boolean aceptada = "APROBADA".equalsIgnoreCase(decision);
        boolean observada = "OBSERVADA".equalsIgnoreCase(decision);
        String color = aceptada ? "#166534" : observada ? "#854d0e" : "#991b1b";
        String fondo = aceptada ? "#f0fdf4" : observada ? "#fefce8" : "#fff1f2";
        String borde = aceptada ? "#16a34a" : observada ? "#eab308" : "#e11d48";
        String titulo = aceptada ? "Solicitud aceptada"
                : observada ? "Solicitud observada" : "Solicitud rechazada";

        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN).append(HEAD_CONTENT);
        sb.append("<body style='margin:0;padding:20px;background:#f1f5f9;font-family:Arial,sans-serif;'>")
                .append("<div style='max-width:680px;margin:0 auto;background:#fff;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;'>")
                .append(getHeaderHtml("RESULTADO", "Evaluación de Caso Especial"))
                .append("<div style='padding:26px;'>")
                .append("<div style='background:").append(fondo)
                .append(";border-left:6px solid ").append(borde)
                .append(";padding:18px;border-radius:8px;'>")
                .append("<h2 style='margin:0 0 10px;color:").append(color).append(";'>")
                .append(escapeHtml(titulo)).append("</h2>")
                .append("<p style='margin:6px 0;color:#334155;'><b>Código:</b> ")
                .append(escapeHtml(codigo)).append("</p>")
                .append("<p style='margin:6px 0;color:#334155;'><b>Estado:</b> ")
                .append(escapeHtml(estado)).append("</p>")
                .append("<p style='margin:12px 0 0;color:#334155;'><b>Motivo:</b><br>")
                .append(escapeHtml(motivo)).append("</p></div>");
        if (aceptada) {
            sb.append("<p style='margin-top:18px;color:#475569;font-size:13px;'>"
                    + "Puede adjuntar su comprobante de pago respondiendo con el asunto "
                    + "<code>pago adjuntar ")
                    .append(idSolicitud)
                    .append(",COMPROBANTE_PAGO</code> e incluyendo el comprobante como archivo adjunto.</p>");
        }
        sb.append(getFooterHtml()).append("</div></div></body>").append(HTML_CLOSE);
        return sb.toString();
    }

    public static String generarNotificacionRemisionCpd(
            String numeroOficio,
            String periodo,
            String referencia,
            String detalle,
            int cantidadSolicitudes
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN).append(HEAD_CONTENT)
                .append("<body style='margin:0;padding:20px;background:#f1f5f9;font-family:Arial,sans-serif;'>")
                .append("<div style='max-width:680px;margin:0 auto;background:#fff;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;'>")
                .append(getHeaderHtml("CU5", "Remisión a CPD"))
                .append("<div style='padding:26px;color:#334155;'>")
                .append("<h2 style='margin-top:0;color:#166534;'>Oficio remitido</h2>")
                .append("<p>Se adjunta el oficio con el listado de solicitudes aceptadas y sus comprobantes de pago.</p>")
                .append("<table style='width:100%;border-collapse:collapse;font-size:14px;'>")
                .append("<tr><td style='padding:8px;border:1px solid #e2e8f0;'><b>Número de oficio</b></td><td style='padding:8px;border:1px solid #e2e8f0;'>")
                .append(escapeHtml(numeroOficio)).append("</td></tr>")
                .append("<tr><td style='padding:8px;border:1px solid #e2e8f0;'><b>Período académico</b></td><td style='padding:8px;border:1px solid #e2e8f0;'>")
                .append(escapeHtml(periodo)).append("</td></tr>")
                .append("<tr><td style='padding:8px;border:1px solid #e2e8f0;'><b>Referencia</b></td><td style='padding:8px;border:1px solid #e2e8f0;'>")
                .append(escapeHtml(referencia)).append("</td></tr>")
                .append("<tr><td style='padding:8px;border:1px solid #e2e8f0;'><b>Solicitudes</b></td><td style='padding:8px;border:1px solid #e2e8f0;'>")
                .append(cantidadSolicitudes).append("</td></tr>")
                .append("<tr><td style='padding:8px;border:1px solid #e2e8f0;'><b>Detalle</b></td><td style='padding:8px;border:1px solid #e2e8f0;'>")
                .append(escapeHtml(detalle)).append("</td></tr></table>")
                .append(getFooterHtml()).append("</div></div></body>")
                .append(HTML_CLOSE);
        return sb.toString();
    }

    public static String generarNotificacionHabilitacionCpd(
            String codigoSolicitud,
            String numeroOficio,
            String periodo,
            String detalle
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN).append(HEAD_CONTENT)
                .append("<body style='margin:0;padding:20px;background:#f1f5f9;font-family:Arial,sans-serif;'>")
                .append("<div style='max-width:680px;margin:0 auto;background:#fff;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;'>")
                .append(getHeaderHtml("CU6", "Confirmación de CPD"))
                .append("<div style='padding:26px;color:#334155;'>")
                .append("<div style='background:#f0fdf4;border-left:6px solid #16a34a;padding:18px;border-radius:8px;'>")
                .append("<h2 style='margin:0 0 10px;color:#166534;'>Materias habilitadas e inscripción confirmada</h2>")
                .append("<p style='margin:6px 0;'><b>Solicitud:</b> ")
                .append(escapeHtml(codigoSolicitud)).append("</p>")
                .append("<p style='margin:6px 0;'><b>Oficio:</b> ")
                .append(escapeHtml(numeroOficio)).append("</p>")
                .append("<p style='margin:6px 0;'><b>Período académico:</b> ")
                .append(escapeHtml(periodo)).append("</p>")
                .append("<p style='margin:6px 0;'><b>Detalle de la confirmación:</b><br>")
                .append(escapeHtml(detalle)).append("</p></div>")
                .append("<p style='margin-top:18px;'>El Centro de Procesamiento de Datos confirmó que las materias de su solicitud ya fueron habilitadas e inscritas.</p>")
                .append(getFooterHtml()).append("</div></div></body>")
                .append(HTML_CLOSE);
        return sb.toString();
    }

    public static String generarNotificacionResultadoCpd(
            String codigoSolicitud,
            String numeroOficio,
            String periodo,
            String resultado,
            String codigoRespuesta,
            String observacion
    ) {
        boolean habilitada = "HABILITADO".equals(resultado);
        boolean observada = "OBSERVADO".equals(resultado);
        String color = habilitada ? "#166534" : observada ? "#854d0e" : "#991b1b";
        String fondo = habilitada ? "#f0fdf4" : observada ? "#fefce8" : "#fff1f2";
        String borde = habilitada ? "#16a34a" : observada ? "#eab308" : "#e11d48";
        String titulo = habilitada ? "Materias habilitadas e inscritas"
                : observada ? "Solicitud observada por CPD" : "Solicitud rechazada por CPD";

        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN).append(HEAD_CONTENT)
                .append("<body style='margin:0;padding:20px;background:#f1f5f9;font-family:Arial,sans-serif;'>")
                .append("<div style='max-width:680px;margin:0 auto;background:#fff;border:1px solid #e2e8f0;border-radius:12px;overflow:hidden;'>")
                .append(getHeaderHtml("CU6", "Respuesta del CPD"))
                .append("<div style='padding:26px;color:#334155;'>")
                .append("<div style='background:").append(fondo)
                .append(";border-left:6px solid ").append(borde)
                .append(";padding:18px;border-radius:8px;'>")
                .append("<h2 style='margin:0 0 12px;color:").append(color).append(";'>")
                .append(escapeHtml(titulo)).append("</h2>")
                .append("<p><b>Solicitud:</b> ").append(escapeHtml(codigoSolicitud)).append("</p>")
                .append("<p><b>Oficio:</b> ").append(escapeHtml(numeroOficio)).append("</p>")
                .append("<p><b>Período:</b> ").append(escapeHtml(periodo)).append("</p>")
                .append("<p><b>Resultado:</b> ").append(escapeHtml(resultado)).append("</p>")
                .append("<p><b>Código de respuesta:</b> ")
                .append(escapeHtml(codigoRespuesta)).append("</p>")
                .append("<p><b>Observación:</b><br>")
                .append(escapeHtml(observacion)).append("</p></div>")
                .append("<p style='margin-top:18px;'>")
                .append(habilitada
                        ? "CPD informa que las materias de esta solicitud ya fueron habilitadas e inscritas en el sistema académico oficial."
                        : observada
                                ? "La solicitud requiere revisión por Dirección de Carrera."
                                : "CPD informó que no fue posible habilitar las materias de esta solicitud.")
                .append("</p>")
                .append(getFooterHtml()).append("</div></div></body>")
                .append(HTML_CLOSE);
        return sb.toString();
    }

    // ============================================================
    // 3. CONSTRUCCIÓN DE MENSAJE DE ERROR ULTRA-DETALLADO
    // ============================================================

    public static String generarMensajeError(String titulo, String mensaje) {
        return generarMensajeErrorDetallado(titulo, null, mensaje, "Envie un correo con <b>help</b> para consultar la sintaxis valida de los comandos.");
    }

    public static String generarMensajeErrorDetallado(String titulo, String comandoRecibido, String queFallo, String sugerencia) {
        ErrorDetail ed = new ErrorDetail();
        ed.setTitulo(titulo != null ? titulo : "Error al Procesar el Comando");
        ed.setComandoRecibido(comandoRecibido != null ? comandoRecibido : "");
        ed.setQueFallo(queFallo != null ? queFallo : "No se especificó la causa exacta del error.");
        ed.setSintaxisCorrecta(sugerencia);
        return generarMensajeErrorCompleto(ed);
    }

    public static String generarMensajeErrorCompleto(ErrorDetail ed) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN).append(HEAD_CONTENT);
        sb.append("<body style='margin: 0; padding: 20px; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif;'>");

        sb.append("<div style='max-width: 760px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 6px 20px rgba(220,38,38,0.08); border: 1px solid #fecaca;'>");

        // Header de Error con gradiente rojo/burdeos
        sb.append("<div style='background: #7f1d1d; background-image: linear-gradient(135deg, #7f1d1d 0%, #991b1b 50%, #b91c1c 100%); padding: 22px 26px; border-bottom: 3px solid #ef4444;'>");
        sb.append("<table style='width: 100%; border: none; border-collapse: collapse;'><tr>");
        sb.append("<td style='vertical-align: middle;'>");
        sb.append("<span style='background: rgba(255,255,255,0.18); color: #fecaca; font-size: 11px; font-weight: 700; letter-spacing: 1.5px; padding: 4px 10px; border-radius: 12px; text-transform: uppercase;'>DIAGNOSTICO DE ERROR</span>");
        sb.append("<h1 style='color: #ffffff; font-size: 18px; font-weight: 700; margin: 8px 0 2px 0; letter-spacing: -0.3px;'>&#9888; ").append(ed.getTitulo()).append("</h1>");
        sb.append("<p style='color: #fca5a5; font-size: 12px; margin: 0;'>El sistema detectó un problema al analizar o ejecutar el comando enviado</p>");
        sb.append("</td>");
        sb.append("</tr></table>");
        sb.append("</div>");

        sb.append("<div style='padding: 24px 28px;'>");

        // 1. Comando recibido (estilo terminal)
        String cmd = ed.getComandoRecibido();
        if (cmd != null && !cmd.trim().isEmpty()) {
            sb.append("<div style='background: #0f172a; border: 1px solid #334155; border-radius: 8px; padding: 14px 18px; margin-bottom: 18px;'>");
            sb.append("<div style='color: #94a3b8; font-size: 11px; text-transform: uppercase; letter-spacing: 0.8px; font-weight: 700; margin-bottom: 6px;'>Asunto (Subject) Recibido:</div>");
            sb.append("<div style='font-family: Consolas, monospace; font-size: 14px; color: #f87171; font-weight: bold;'>");
            sb.append("<span style='color: #38bdf8;'>$ </span>").append(escapeHtml(cmd));
            sb.append("</div>");
            sb.append("</div>");
        }

        // 2. ¿Qué falló exactamente?
        sb.append("<div style='background: #fff1f2; border: 1px solid #fecdd3; border-left: 6px solid #e11d48; border-radius: 8px; padding: 16px 20px; margin-bottom: 18px;'>");
        sb.append("<div style='color: #9f1239; font-weight: 700; font-size: 14px; margin-bottom: 6px;'>&#10060; &#191;Cu&aacute;l es el error exacto?</div>");
        sb.append("<div style='color: #881337; font-size: 13px; line-height: 1.6;'>").append(ed.getQueFallo()).append("</div>");
        sb.append("</div>");

        // 3. Sugerencia por error tipográfico / semejanza
        if (ed.getSugerenciaSemejanza() != null && !ed.getSugerenciaSemejanza().trim().isEmpty()) {
            sb.append("<div style='background: #fefce8; border: 1px solid #fef08a; border-left: 6px solid #eab308; border-radius: 8px; padding: 14px 18px; margin-bottom: 18px;'>");
            sb.append("<div style='color: #854d0e; font-weight: 700; font-size: 13px; margin-bottom: 4px;'>&#128269; Sugerencia de correcci&oacute;n:</div>");
            sb.append("<div style='color: #713f12; font-size: 13px; line-height: 1.5;'>").append(ed.getSugerenciaSemejanza()).append("</div>");
            sb.append("</div>");
        }

        // 4. Sintaxis Correcta y Ejemplo Real
        String sintaxis = ed.getSintaxisCorrecta();
        if (sintaxis != null && !sintaxis.trim().isEmpty()) {
            sb.append("<div style='background: #f0fdf4; border: 1px solid #bbf7d0; border-left: 6px solid #16a34a; border-radius: 8px; padding: 16px 20px; margin-bottom: 18px;'>");
            sb.append("<div style='color: #166534; font-weight: 700; font-size: 14px; margin-bottom: 8px;'>&#128161; C&oacute;mo solucionarlo (Sintaxis v&aacute;lida):</div>");
            sb.append("<div style='background: #ffffff; border: 1px solid #86efac; border-radius: 6px; padding: 10px 14px; font-family: Consolas, monospace; font-size: 13px; color: #15803d; font-weight: bold; margin-bottom: 10px; word-break: break-all;'>");
            sb.append(sintaxis);
            sb.append("</div>");

            if (ed.getEjemploReal() != null && !ed.getEjemploReal().trim().isEmpty()) {
                sb.append("<div style='color: #14532d; font-size: 12px; margin-top: 6px;'>");
                sb.append("<b>Ejemplo listo para enviar:</b> <code style='background: #dcfce7; padding: 3px 8px; border-radius: 4px; font-family: Consolas, monospace; font-size: 12px; color: #166534;'>")
                  .append(escapeHtml(ed.getEjemploReal()))
                  .append("</code>");
                sb.append("</div>");
            }
            sb.append("</div>");
        }

        // 5. Parámetros detallados (si aplica)
        List<ParamInfo> params = ed.getParametros();
        if (params != null && !params.isEmpty()) {
            sb.append("<div style='margin-bottom: 18px;'>");
            sb.append("<div style='color: #334155; font-weight: 700; font-size: 13px; margin-bottom: 8px;'>Gu&iacute;a de Par&aacute;metros Requeridos:</div>");
            sb.append("<div style='overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 6px;'>");
            sb.append("<table style='width: 100%; border-collapse: collapse; font-size: 12px;'>");
            sb.append("<thead><tr style='background: #f8fafc; color: #475569; text-align: left;'>");
            sb.append("<th style='padding: 8px 12px; border-bottom: 1px solid #e2e8f0;'>Parámetro</th>");
            sb.append("<th style='padding: 8px 12px; border-bottom: 1px solid #e2e8f0;'>Tipo Esperado</th>");
            sb.append("<th style='padding: 8px 12px; border-bottom: 1px solid #e2e8f0;'>Descripción</th>");
            sb.append("</tr></thead><tbody>");
            for (ParamInfo pi : params) {
                sb.append("<tr>");
                sb.append("<td style='padding: 8px 12px; border-bottom: 1px solid #f1f5f9; font-family: Consolas, monospace; font-weight: bold; color: #0f172a;'>").append(pi.getNombre()).append("</td>");
                sb.append("<td style='padding: 8px 12px; border-bottom: 1px solid #f1f5f9; color: #0284c7; font-weight: 600;'>").append(pi.getTipo()).append("</td>");
                sb.append("<td style='padding: 8px 12px; border-bottom: 1px solid #f1f5f9; color: #64748b;'>").append(pi.getDescripcion()).append("</td>");
                sb.append("</tr>");
            }
            sb.append("</tbody></table>");
            sb.append("</div>");
            sb.append("</div>");
        }

        // 6. Caja de ayuda rápida
        sb.append("<div style='background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 14px 18px; font-size: 12px; color: #475569;'>");
        sb.append("<b>&#191;Tiene dudas sobre otros comandos?</b> Envíe un correo con el asunto <b>help</b> para ver el manual completo, o envíe <code>[modulo] help</code> (ej: <code>solicitud help</code>).");
        sb.append("</div>");

        sb.append(getFooterHtml());
        sb.append("</div>"); // Fin padding
        sb.append("</div>"); // Fin container
        sb.append("</body>");
        sb.append(HTML_CLOSE);
        return sb.toString();
    }

    // ============================================================
    // 4. CONSTRUCCIÓN DE AYUDA (HELP) ELEGANTE
    // ============================================================

    public static String generarAyuda(String titulo, String contenidoTexto) {
        StringBuilder sb = new StringBuilder();
        sb.append(HTML_OPEN).append(HEAD_CONTENT);
        sb.append("<body style='margin: 0; padding: 20px; background-color: #f1f5f9; font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif;'>");

        sb.append("<div style='max-width: 820px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 16px rgba(15,23,42,0.08); border: 1px solid #e2e8f0;'>");
        sb.append(getHeaderHtml("MANUAL DE USUARIO", "Guia de Sintaxis de Comandos"));

        sb.append("<div style='padding: 24px 28px;'>");
        sb.append("<h2 style='color: #0f172a; font-size: 18px; margin: 0 0 12px 0; font-weight: 700;'>&#128218; ").append(titulo).append("</h2>");
        sb.append("<p style='color: #64748b; font-size: 13px; margin: 0 0 16px 0; line-height: 1.5;'>Para ejecutar cualquier acción en el sistema, envíe el comando deseado en el campo <b>SUBJECT (Asunto)</b> de su correo electrónico.</p>");

        sb.append("<div style='background: #0f172a; border: 1px solid #334155; border-radius: 8px; padding: 18px 20px; margin-bottom: 20px;'>");
        sb.append("<pre style='margin: 0; font-family: Consolas, monospace; font-size: 12px; line-height: 1.6; color: #e2e8f0; white-space: pre-wrap; word-break: break-all;'>")
          .append(escapeHtml(contenidoTexto))
          .append("</pre>");
        sb.append("</div>");

        sb.append("<div style='background: #f0f9ff; border: 1px solid #bae6fd; border-radius: 8px; padding: 14px 18px; font-size: 12px; color: #0369a1; line-height: 1.5;'>");
        sb.append("<b>Formato general de comandos:</b> <code>[modulo] [accion] [parametros separados por coma]</code><br>");
        sb.append("Ejemplo de consulta: <code>solicitud listar</code> &bull; Ejemplo con parámetros: <code>solicitud registrar 1,1,CORRECCION_NOTA,Error en nota</code>");
        sb.append("</div>");

        sb.append(getFooterHtml());
        sb.append("</div>"); // Fin padding
        sb.append("</div>"); // Fin container
        sb.append("</body>");
        sb.append(HTML_CLOSE);
        return sb.toString();
    }

    // ============================================================
    // FORMATEO DE VALORES Y BADGES DE ESTADO
    // ============================================================

    private static String formatearValor(String val) {
        if (val == null || val.trim().isEmpty()) {
            return "<span style='color: #cbd5e1;'>&mdash;</span>";
        }
        String u = val.toUpperCase().trim();

        // 1. Estados Exitosos / Activos / Aprobados
        if (u.equals("HABILITADA") || u.equals("APROBADA") || u.equals("APROBADA_DIRECCION") ||
            u.equals("ACTIVO") || u.equals("CONFIRMADA_CPD") || u.equals("COMPLETADO")) {
            return "<span style='background: #dcfce7; color: #166534; border: 1px solid #86efac; padding: 3px 9px; border-radius: 12px; font-weight: 700; font-size: 11px; display: inline-block;'>&#10003; " + val + "</span>";
        }

        // 2. Estados Rechazados / Inactivos / Bloqueados
        if (u.equals("RECHAZADA") || u.equals("RECHAZADA_DIRECCION") || u.equals("RECHAZADA_CPD") ||
            u.equals("BLOQUEADO") || u.equals("INACTIVO") || u.equals("CANCELADO")) {
            return "<span style='background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; padding: 3px 9px; border-radius: 12px; font-weight: 700; font-size: 11px; display: inline-block;'>&#10007; " + val + "</span>";
        }

        // 3. Estados En Proceso / Enviado / Remitido
        if (u.equals("ENVIADA") || u.equals("REMITIDA_A_CPD") || u.equals("REMITIDAS_CPD") ||
            u.equals("RESERVADO") || u.equals("EN_REVISION") || u.equals("ABIERTO") || u.equals("EMITIDO")) {
            return "<span style='background: #e0f2fe; color: #075985; border: 1px solid #7dd3fc; padding: 3px 9px; border-radius: 12px; font-weight: 700; font-size: 11px; display: inline-block;'>&#9874; " + val + "</span>";
        }

        // 4. Estados Observados / Pendientes
        if (u.contains("OBSERVADA") || u.equals("PENDIENTE") || u.equals("PENDIENTES_EVALUACION")) {
            return "<span style='background: #fef3c7; color: #92400e; border: 1px solid #fcd34d; padding: 3px 9px; border-radius: 12px; font-weight: 700; font-size: 11px; display: inline-block;'>&#9888; " + val + "</span>";
        }

        return escapeHtml(val);
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }
}