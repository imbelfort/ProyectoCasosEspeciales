/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Comunication;

//import com.juanvladimir13.template.MaryTemplate;
import Database.ConfigEmailServer;
import Utils.Email;
import java.io.File;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.internet.MimeUtility;
import proyectomailgrupo01sa.Principal;

public class SendEmail {

    private final static int PORT_SMTP = parsePort(ConfigEmailServer.PORT_SMTP, 25);
    private final static String SERVER = ConfigEmailServer.HOST;
    private final static String user_emisor = ConfigEmailServer.MAIL;
    private static String user_receptor;
    private static Socket socket;
    private static BufferedReader entrada;
    private static DataOutputStream salida;

    public SendEmail() {
        this.user_receptor = "";

    }

    public String getUser_receptor() {
        return user_receptor;
    }

    public void setUser_receptor(String user_receptor) {
        this.user_receptor = user_receptor;
    }

    public boolean responseUser(Email email, String mensaje) {
        // Estableciendo variables
        BufferedReader reader;
        DataOutputStream writer;
        String command;
        try {
            // Estableciendo Conexion Socket
            Socket socket = new Socket(SERVER, PORT_SMTP);
            socket.setSoTimeout(15000);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new DataOutputStream(socket.getOutputStream());

            if (socket != null && reader != null && writer != null) {
                readSmtpResponse(reader);

                command = "HELO " + SERVER + "\r\n";
                sendSmtpCommand(writer, command);
                readSmtpResponse(reader);

                command = "EHLO " + SERVER + "\r\n";
                sendSmtpCommand(writer, command);
                readSmtpResponse(reader);
                System.out.println("[SMTP] Envío sin autenticación.");

                command = "MAIL FROM:<" + user_emisor + ">\r\n";
                sendSmtpCommand(writer, command);
                validarCodigo(readSmtpResponse(reader), "250", "remitente");

                if (Principal.cbx_emailsReceptores != null && Principal.cbx_emailsReceptores.isSelected()) {
                    // Obtener destinatarios desde JTextArea
                    String[] receptores = getEmailsReceptores();

                    // Usar Set para evitar duplicados y mantener orden
                    Set<String> destinatarios = new LinkedHashSet<>();
                    // Incluir el remitente original (quien enviÃ³ el comando) como destinatario
                    if (esEmailValido(email.getFrom())) {
                        destinatarios.add(email.getFrom().trim());
                    }

                    for (String r : receptores) {
                        if (esEmailValido(r)) {
                            destinatarios.add(r.trim());
                        }
                    }

                    // Enviar RCPT TO para cada destinatario (usar <> segÃºn RFC)
                    for (String destinatario : destinatarios) {
                        command = "RCPT TO:<" + destinatario + ">\r\n";
                        sendSmtpCommand(writer, command);
                        String respuesta = readSmtpResponse(reader);
                        if (!respuestaSmtpExitosa(respuesta)) {
                            throw new IOException("el servidor SMTP rechazó al destinatario");
                        }
                    }
                } else {
                    // Solo al remitente original
                    String origen = email.getFrom();
                    if (esEmailValido(origen)) {
                        command = "RCPT TO:<" + origen.trim() + ">\r\n";
                        sendSmtpCommand(writer, command);
                        String respuesta = readSmtpResponse(reader);
                        if (!respuestaSmtpExitosa(respuesta)) {
                            throw new IOException("el servidor SMTP rechazó al destinatario");
                        }
                    } else {
                        // Si no hay destinatario vÃ¡lido, cancelar
                        System.out.println("Correo de respuesta no enviado: no hay destinatarios vÃ¡lidos");
                        writer.close();
                        reader.close();
                        socket.close();
                        return false;
                    }
                }

                // Escribir Mensaje
                // Comenzar DATA
                command = "DATA\r\n";
                sendSmtpCommand(writer, command);
                if (!respuestaSmtpExitosa(readSmtpResponse(reader))) {
                    throw new IOException("el servidor SMTP rechazó el contenido");
                }

                // Construir cabeceras mÃ­nimas vÃ¡lidas (From, To, Subject, MIME)
                StringBuilder data = new StringBuilder();
                data.append("From: <").append(user_emisor).append(">\r\n");

                // Reconstruir lista To a partir de los RCPT enviados (usar getEmailsReceptores + remitente)
                List<String> toList = new ArrayList<>();
                if (Principal.cbx_emailsReceptores != null && Principal.cbx_emailsReceptores.isSelected()) {
                    if (esEmailValido(email.getFrom())) toList.add(email.getFrom().trim());
                    for (String r : getEmailsReceptores()) {
                        if (esEmailValido(r)) toList.add(r.trim());
                    }
                } else {
                    if (esEmailValido(email.getFrom())) toList.add(email.getFrom().trim());
                }

                if (!toList.isEmpty()) {
                    data.append("To: ");
                    for (int i = 0; i < toList.size(); i++) {
                        if (i > 0) data.append(", ");
                        data.append("<").append(toList.get(i)).append(">");
                    }
                    data.append("\r\n");
                }

                // Sanitize subject: remove or replace any internal newlines to avoid header injection
                String subject = "";
                if (email.getSubject() != null) {
                    // Use \R to replace any Unicode linebreak sequences with a single space
                    subject = email.getSubject().toString().replaceAll("\\R+", " ").trim();
                }
                data.append("Subject: ").append(subject).append("\r\n");

                // Cuerpo: normalizar saltos de lÃ­nea a CRLF y aplicar dot-stuffing solo al cuerpo
                InlineEmailContent inlineContent = prepararContenidoInline(mensaje);

                String cuerpoSeguridad = normalizeForSmtp(inlineContent.htmlSanitizado)
                        .replace("\r\n.", "\r\n..");
                data.append("MIME-Version: 1.0\r\n");

                if (inlineContent.hasImages()) {
                    data.append("Content-Type: multipart/related; boundary=related-boundary-1\r\n");
                } else {
                    data.append("Content-Type: text/html; charset=\"UTF-8\"\r\n");
                }

                data.append("\r\n"); 

                if (inlineContent.hasImages()) {
                    data.append("--related-boundary-1\r\n");
                    data.append("Content-Type: text/html; charset=\"UTF-8\"\r\n");
                    data.append("Content-Transfer-Encoding: 8bit\r\n\r\n");
                    data.append(cuerpoSeguridad);
                    data.append("\r\n");

                    for (InlineImage inlineImage : inlineContent.imagenes) {
                        data.append("--related-boundary-1\r\n");
                        data.append("Content-Type: ").append(inlineImage.contentType).append("\r\n");
                        data.append("Content-Transfer-Encoding: base64\r\n");
                        data.append("Content-ID: <").append(inlineImage.cid).append(">\r\n");
                        data.append("Content-Disposition: inline; filename=\"").append(inlineImage.cid).append("\"\r\n\r\n");
                        data.append(inlineImage.base64).append("\r\n");
                    }
                    data.append("--related-boundary-1--\r\n");
                } else {
                    data.append(cuerpoSeguridad);
                    if (!cuerpoSeguridad.endsWith("\r\n")) data.append("\r\n");
                }

                data.append(".\r\n");

                String finalData = data.toString();

                writer.write(finalData.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                writer.flush();
                String dataResponse = readSmtpResponse(reader);
                if (!respuestaSmtpExitosa(dataResponse)) {
                    throw new IOException("el servidor SMTP no aceptó el mensaje");
                }

                command = "QUIT\r\n";
                sendSmtpCommand(writer, command);
                readSmtpResponse(reader);

                System.out.println("Correo de respuesta enviado exitosamente a: " + email.getFrom());

            }

            // Cerrar Conexion
            writer.close();
            reader.close();
            socket.close();
            return true;
        } catch (UnknownHostException e) {
            System.out.println("Correo de respuesta no enviado: no se pudo conectar con el servidor indicado");
        } catch (IOException ex) {
            System.out.println("Correo de respuesta no enviado: " + mensajeErrorBreve(ex));
        } catch (Exception ex) {
            System.out.println("Correo de respuesta no enviado: " + mensajeErrorBreve(ex));
        }
        return false;
    }

    private static void sendSmtpCommand(DataOutputStream writer, String command) throws IOException {
        String visible = command.replace("\r", "").replace("\n", "");
        if (visible.startsWith("AUTH") || visible.matches("^[A-Za-z0-9+/=]{8,}$")) {
            visible = "[credencial SMTP]";
        }
        System.out.println("[SMTP][C] " + visible);
        writer.writeBytes(command);
        writer.flush();
    }

    private static String readSmtpResponse(BufferedReader reader) throws IOException {
        StringBuilder response = new StringBuilder();
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("el servidor SMTP cerró la conexión");
        }
        System.out.println("[SMTP][S] " + line);
        response.append(line);
        while (line.length() >= 4 && line.charAt(3) == '-') {
            line = reader.readLine();
            if (line == null) {
                throw new IOException("el servidor SMTP cerró la conexión");
            }
            System.out.println("[SMTP][S] " + line);
            response.append('\n').append(line);
        }
        return response.toString();
    }

    private static boolean respuestaSmtpExitosa(String response) {
        return response != null
                && response.length() >= 3
                && (response.charAt(0) == '2' || response.charAt(0) == '3');
    }

    private static void validarCodigo(String respuesta, String codigo, String operacion) throws IOException {
        if (respuesta == null || !respuesta.startsWith(codigo)) {
            throw new IOException("el servidor SMTP rechazó " + operacion);
        }
    }

    private static String mensajeErrorBreve(Exception ex) {
        String mensaje = ex.getMessage();
        return mensaje == null || mensaje.trim().isEmpty() ? "error de comunicación" : mensaje.trim();
    }

    private static int parsePort(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    static protected String getMultiline(BufferedReader in) throws IOException {
        String lines = "";
        while (true) {
            String line = in.readLine();
            if (line == null) {
                // Server closed connection
                throw new IOException(" S : Server unawares closed the connection.");
            }
            if (line.charAt(3) == ' ') {
                lines = lines + "\n" + line;
                // No more lines in the server response
                break;
            }
            // Add read line to the list of lines
            lines = lines + "\n" + line;
        }
        return lines;
    }
private boolean esEmailValido(String email) {
    return email != null && email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");
}

    ///////////////////////////////////////////////////////////////
    public void responseEmail(String emailReceptor, String codigoHTML) {
        try {
            Properties p = new Properties();
            p.put("mail.smtp.host", "smtp.gmail.com");
            p.put("mail.smtp.starttls.enable", "true");
            p.put("mail.smtp.ssl.trust", "smtp.gmail.com");
            p.put("mail.smtp.port", "587");
            p.put("mail.smtp.auth", "true");
            Session s = Session.getDefaultInstance(p);

            MimeMessage mensaje = new MimeMessage(s);
            mensaje.setFrom(new InternetAddress("medinytalimber.123@gmail.com"));
            mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(emailReceptor));
            mensaje.setSubject("NOTIFICACION");
            //mensaje.setText("informacion");
            mensaje.setContent(codigoHTML, "text/html");//
            Transport t = s.getTransport("smtp");
            t.connect("medinytalimber.123@gmail.com", "xgyxhcqqwxjkmydj");
            System.out.println("enviando...");
            t.sendMessage(mensaje, mensaje.getAllRecipients());
            t.close();
            System.out.println("mensaje enviado");
        } catch (MessagingException ex) {
            System.out.println("Correo no enviado: error de comunicación con el servidor SMTP");
        }
    }

    public boolean sendmail(String receptor, String subject, String htmlContent) {
        if (receptor == null || receptor.trim().isEmpty()) {
            System.out.println("[SendEmail] Destinatario vacio.");
            return false;
        }

        Email email = new Email(receptor.trim(), subject);
        return responseUser(email, htmlContent);
    }

    public boolean sendmailDirect(String receptor, String subject, String htmlContent) {
        if (!esEmailValido(receptor)) {
            System.out.println("[SMTP] Notificación no enviada: el correo del estudiante no es válido.");
            return false;
        }
        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", SERVER);
            properties.put("mail.smtp.port", String.valueOf(PORT_SMTP));
            properties.put("mail.smtp.auth", "false");
            properties.put("mail.smtp.starttls.enable", "false");
            Session session = Session.getInstance(properties);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user_emisor));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(receptor));
            message.setSubject(subject.replaceAll("\\R+", " ").trim(), "UTF-8");
            message.setContent(htmlContent, "text/html; charset=UTF-8");
            try (javax.mail.Transport transport = session.getTransport("smtp")) {
                transport.connect(SERVER, PORT_SMTP, null, null);
                transport.sendMessage(message, message.getAllRecipients());
            }
            System.out.println("[SMTP] Notificación enviada a: " + receptor);
            return true;
        } catch (Exception ex) {
            System.out.println("[SMTP] Notificación no enviada: " + mensajeErrorBreve(ex));
            return false;
        }
    }

    public boolean sendmailWithAttachment(
            String receptor,
            String subject,
            String htmlContent,
            File attachment
    ) {
        return sendmailWithAttachments(
                receptor, subject, htmlContent, java.util.Collections.singletonList(attachment));
    }

    public boolean sendmailWithAttachments(
            String receptor,
            String subject,
            String htmlContent,
            List<File> attachments
    ) {
        if (!esEmailValido(receptor)) {
            System.out.println("[SMTP] Oficio no enviado: el correo de CPD no es válido.");
            return false;
        }
        if (attachments == null || attachments.isEmpty()
                || attachments.stream().anyMatch(attachment ->
                        attachment == null || !attachment.isFile() || !attachment.canRead())) {
            System.out.println(
                    "[SMTP] Oficio no enviado: falta un documento adjunto o no se puede leer.");
            return false;
        }

        try {
            Properties properties = new Properties();
            properties.put("mail.smtp.host", SERVER);
            properties.put("mail.smtp.port", String.valueOf(PORT_SMTP));
            properties.put("mail.smtp.auth", "false");
            properties.put("mail.smtp.starttls.enable", "false");
            Session session = Session.getInstance(properties);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user_emisor));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(receptor));
            message.setSubject(subject.replaceAll("\\R+", " ").trim(), "UTF-8");

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(htmlContent, "text/html; charset=UTF-8");
            MimeMultipart multipart = new MimeMultipart("mixed");
            multipart.addBodyPart(htmlPart);
            for (File attachment : attachments) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setDataHandler(new DataHandler(new FileDataSource(attachment)));
                attachmentPart.setFileName(MimeUtility.encodeText(
                        attachment.getName(), "UTF-8", null));
                multipart.addBodyPart(attachmentPart);
            }
            message.setContent(multipart);

            try (javax.mail.Transport transport = session.getTransport("smtp")) {
                transport.connect(SERVER, PORT_SMTP, null, null);
                transport.sendMessage(message, message.getAllRecipients());
            }
            System.out.println("[SMTP] Oficio enviado exitosamente a CPD: " + receptor);
            return true;
        } catch (Exception ex) {
            System.out.println("[SMTP] Oficio no enviado: " + mensajeErrorBreve(ex));
            return false;
        }
    }

  public void sendmail(String receptor, String subject) {
    try {
        // Configuración SMTP
        Properties p = new Properties();
        p.put("mail.smtp.host", "smtp.gmail.com");
        p.put("mail.smtp.starttls.enable", "true");
        p.put("mail.smtp.ssl.trust", "smtp.gmail.com");
        p.put("mail.smtp.port", "587");
        p.put("mail.smtp.auth", "true");

        Session s = Session.getDefaultInstance(p);

        MimeMessage mensaje = new MimeMessage(s);
        mensaje.setFrom(new InternetAddress("medinytalimber.123@gmail.com"));
        mensaje.setSubject(subject);
        mensaje.setText("enviado");

        // Agregar destinatarios según estado del checkbox
        if (Principal.cbx_emailsReceptores != null && Principal.cbx_emailsReceptores.isSelected()) {
            Set<String> destinatarios = new HashSet<>();

            // Agregar receptor por parámetro
            if (esEmailValido(receptor)) {
                destinatarios.add(receptor.trim());
            }

            // Agregar correos del JTextArea
            String[] receptores = getEmailsReceptores();
            for (String email : receptores) {
                if (esEmailValido(email)) {
                    destinatarios.add(email.trim());
                }
            }

            // AÃ±adir todos como TO
            for (String email : destinatarios) {
                mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
            }
        } else {
            // Solo enviar al receptor pasado si es vÃ¡lido
            if (esEmailValido(receptor)) {
                mensaje.addRecipient(Message.RecipientType.TO, new InternetAddress(receptor.trim()));
            } else {
                System.out.println("Email invÃ¡lido: " + receptor);
                return;
            }
        }

        // EnvÃ­o del mensaje
        Transport t = s.getTransport("smtp");
        t.connect("medinytalimber.123@gmail.com", "xgyxhcqqwxjkmydj");
        System.out.println("Enviando...");
        t.sendMessage(mensaje, mensaje.getAllRecipients());
        t.close();
        System.out.println("Mensaje enviado");

    } catch (MessagingException ex) {
        System.out.println("Correo no enviado: error de comunicación con el servidor SMTP");
    }
}


    public String mensajeHtml(String mensaje) {
        String html = "<h2>%s</h2>";
        return String.format(html, mensaje);
    }

    public String cargarHtml(String s) {
        String html = "<center>\n"
                + "  <br>\n" + s + "  <br>\n"
                + "</center>";
        return html;
    }

    public String[] getEmailsReceptores() {
        if (Principal.ta_emailsReceptores == null) {
            return new String[0];
        }
        String emailsReceptores = Principal.ta_emailsReceptores.getText();

        if (emailsReceptores == null || emailsReceptores.trim().isEmpty()) {
            return new String[0]; // Retorna arreglo vacÃ­o si no hay nada
        }

        // Separar por saltos de lÃ­nea y limpiar cada email
        String[] lineas = emailsReceptores.split("\\r?\\n");

        List<String> resultado = new ArrayList<>();
        for (String linea : lineas) {
            String email = linea.trim();
            if (!email.isEmpty()) {
                resultado.add(email);
            }
        }

        return resultado.toArray(new String[0]);
    }

        // Normaliza saltos de lÃ­nea a CRLF y aplica dot-stuffing para DATA SMTP
        private String normalizeForSmtp(String s) {
            if (s == null) return "";
            // Normalize various line endings to LF first
            String tmp = s.replace("\r\n", "\n").replace("\r", "\n");
            // Convert LF to CRLF
            String crlf = tmp.replace("\n", "\r\n");
            // Dot-stuffing: lines that start with a dot must be prefixed with another dot
            if (crlf.startsWith(".")) crlf = "." + crlf;
            crlf = crlf.replace("\r\n.", "\r\n..");
            return crlf;
        }

        private InlineEmailContent prepararContenidoInline(String html) {
            InlineEmailContent content = new InlineEmailContent();

            if (html == null || html.trim().isEmpty()) {
                content.htmlSanitizado = "";
                return content;
            }

            Pattern pattern = Pattern.compile(
                    "<img([^>]*)src=[\"']data:(image/[^;]+);base64,([^\"']+)[\"']([^>]*)>",
                    Pattern.CASE_INSENSITIVE
            );

            Matcher matcher = pattern.matcher(html);
            StringBuffer sanitized = new StringBuffer();

            int contador = 0;

            while (matcher.find()) {
                String antesSrc = matcher.group(1);
                String contentType = matcher.group(2);
                String base64 = matcher.group(3);
                String despuesSrc = matcher.group(4);

                base64 = limpiarBase64(base64);

                String cid = "imagen_inline_" + contador;

                content.imagenes.add(new InlineImage(cid, contentType, cortarBase64(base64)));

                String replacement =
                        "<img" + antesSrc +
                        "src=\"cid:" + cid + "\"" +
                        despuesSrc +
                        ">";

                matcher.appendReplacement(sanitized, Matcher.quoteReplacement(replacement));

                contador++;
            }

            matcher.appendTail(sanitized);
            content.htmlSanitizado = sanitized.toString();

            return content;
        }

        private String limpiarBase64(String base64) {
            if (base64 == null) {
                return "";
            }

            return base64
                    .replace("\r", "")
                    .replace("\n", "")
                    .replace(" ", "")
                    .trim();
        }

        private String cortarBase64(String base64) {
            if (base64 == null || base64.isEmpty()) {
                return "";
            }

            String limpio = limpiarBase64(base64);
            StringBuilder sb = new StringBuilder();

            int limite = 76;

            for (int i = 0; i < limpio.length(); i += limite) {
                int fin = Math.min(i + limite, limpio.length());
                sb.append(limpio, i, fin).append("\r\n");
            }

            return sb.toString();
        }

        private static class InlineImage {
            private final String cid;
            private final String contentType;
            private final String base64;

            private InlineImage(String cid, String contentType, String base64) {
                this.cid = cid;
                this.contentType = contentType;
                this.base64 = base64;
            }
        }

        private static class InlineEmailContent {
            private String htmlSanitizado = "";
            private final List<InlineImage> imagenes = new ArrayList<>();

            private boolean hasImages() {
                return !imagenes.isEmpty();
            }
        }

public String comandosHelp() {
    return HtmlBuilder.generarAyuda(
            "Comandos Disponibles del Sistema",
            "CU1. GESTION DE USUARIOS Y ROLES:\n" +
            "  - usuario listar\n" +
            "  - estudiantes listar | academicos listar | directores listar | cpd listar\n" +
            "  - estudiante agregar datos_personales,registro_universitario,id_carrera,semestre\n" +
            "  - estudiante editar id,datos_personales,estado,registro_universitario,id_carrera,semestre\n" +
            "  - academico agregar datos_personales,cargo,id_carrera | academico editar id,datos_personales,estado,cargo,id_carrera\n" +
            "  - director agregar datos_personales,id_carrera,gestion_inicio,gestion_fin | director editar id,datos_personales,estado,id_carrera,gestion_inicio,gestion_fin\n" +
            "  - cpd agregar datos_personales | cpd editar id,datos_personales,estado\n" +
            "  Los roles se asignan automaticamente con cada alta.\n\n" +
            "CU2. SOLICITUD DE CASO ESPECIAL:\n" +
            "  - solicitud listar periodo (Director/Personal Academico)\n" +
            "  - solicitud mis_casos [registro,password]\n" +
            "  - solicitud pendientes listar,periodo (Director/Personal Academico)\n" +
            "  - solicitud pendientes [registro,password]\n" +
            "  - solicitud periodos id_periodo_o_gestion (Director/Personal Academico)\n" +
            "  - enviar solicitud gestion,tipo_caso,motivo (ejemplo: 2-2026,TIPO_CASO,motivo; estudiante identificado por correo)\n" +
            "  - solicitar materia_grupo id_solicitud,periodo,sigla_materia,sigla_grupo,motivo (se puede repetir)\n" +
            "  - solicitud consultar id_solicitud\n" +
            "  - solicitud_grupo listar id_solicitud\n" +
            "  - archivo_solicitud adjuntar id_solicitud,tipo_documento (adjunte uno o varios archivos al correo)\n" +
            "  - pago adjuntar id_solicitud,COMPROBANTE_PAGO (solicitud aceptada; adjunte exactamente un archivo)\n" +
            "  - pago listar (alias: mis_pagos listar)\n" +
            "  - archivo_solicitud listar id_solicitud\n\n" +
            "CU3. EVALUACION DIRECCION DE CARRERA:\n" +
            "  - solicitud periodos id_periodo_o_gestion\n" +
            "  - solicitud consultar id_solicitud (expediente con grupos y documentos; Director/Personal Académico recibe los archivos adjuntos)\n" +
            "  - evaluacion listar\n" +
            "  - evaluacion registrar id_solicitud,APROBADA|RECHAZADA,motivo[,observaciones] (evaluador identificado por sesion)\n\n" +
            "CU4. CONTROL Y BLOQUEO DE CUPOS:\n" +
            "  - carrera listar | carrera agregar codigo,nombre,facultad\n" +
            "  - periodo listar | periodo agregar gestion,fecha_inicio,fecha_fin\n" +
            "  - materia listar | materia agregar sigla,nombre,semestre,creditos\n" +
            "  - grupo listar | grupo consultar id_grupo\n" +
            "  - grupo agregar id_materia,id_periodo,sigla_grupo,docente,cupo_max,cupo_exc\n" +
            "  - bloqueo_grupo listar\n" +
            "  - bloqueo_grupo bloquear id_grupo,id_solicitud,cantidad,motivo (Director/Academico identificado por sesion)\n" +
            "  - bloqueo_grupo liberar id_bloqueo,observacion\n\n" +
            "CU5. REMISION A CPD:\n" +
            "  - oficio_cpd listar\n" +
            "  - oficio_cpd remitir gestion,correo_cpd,referencia,detalle (ejemplo: 2-2026; genera CSV y adjunta comprobantes de esa gestión)\n" +
            "  - oficio_cpd generar id_oficio_pendiente (reintenta el envío)\n\n" +
            "CU6. CONFIRMACION Y HABILITACION CPD:\n" +
            "  - cpd pendientes (oficios y solicitudes remitidas sin respuesta)\n" +
            "  - oficio_cpd consultar id_oficio (materias y nombres de comprobantes)\n" +
            "  - cpd responder id_oficio,id_solicitud,HABILITADO|OBSERVADO|RECHAZADO,codigo_respuesta,observacion\n" +
            "    Notifica al estudiante y al personal académico de la carrera.\n" +
            "  - respuesta_cpd listar\n\n" +
            "CU7. HISTORIAL Y NOTIFICACIONES:\n" +
            "  - historial listar | historial consultar id_solicitud\n" +
            "  - notificacion listar\n\n" +
            "CU8. REPORTES Y ESTADISTICAS:\n" +
            "  - reporte general | reporte estados | reporte periodos | reporte cupos\n"
    );
}


    public String mensajeComandos(String mensajeHtml) {
        String[] partes = mensajeHtml.split("<br>");
        String mensaje = "";
        for (int i = 0; i < partes.length; i++) {
            mensaje = mensaje + partes[i] + "\r\n";
        }
        return mensaje;
    }

}
