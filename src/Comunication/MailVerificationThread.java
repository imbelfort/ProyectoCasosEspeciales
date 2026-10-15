package Comunication;

import Interfaces.IEmailEventListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.sasl.AuthenticationException;
import Utils.Command;
import Utils.Email;
import Utils.Extractor;

public class MailVerificationThread implements Runnable {

    private final static int PORT_POP = 110;
    private final static int PORT_IMAP = 143;
    private final static int PORT_SMTP = 25;

    private final static String HOST = "mail.tecnoweb.org.bo";
    private final static String USER = "grupo01sa";
    private final static String PASSWORD = "grup001grup001*";

    private final static int CONNECT_TIMEOUT_MS = 5000;
    private final static int LOOP_DELAY_MS = 5000;

    /*
     * Cambia a true solo cuando quieras depurar.
     * En false no imprimirÃ¡ conexion establecida, conexion cerrada,
     * cantidad de correos ni estado del hilo a cada rato.
     */
    private final static boolean DEBUG = false;

    private Socket socket;
    private BufferedReader input;
    private DataOutputStream output;

    private Boolean lastImapStatus;
    private Boolean lastPopStatus;
    private Boolean lastSmtpStatus;

    private IEmailEventListener emailEventListener;

    public IEmailEventListener getEmailEventListener() {
        return emailEventListener;
    }

    public void setEmailEventListener(IEmailEventListener emailEventListener) {
        this.emailEventListener = emailEventListener;
    }

    public MailVerificationThread() {
        socket = null;
        input = null;
        output = null;
        lastImapStatus = null;
        lastPopStatus = null;
        lastSmtpStatus = null;
    }

    @Override
    public void run() {
        while (true) {
            try {
                boolean imapAvailable = isPortAvailable(HOST, PORT_IMAP, CONNECT_TIMEOUT_MS);
                notifyStatusIfChanged("IMAP", PORT_IMAP, imapAvailable, 1);

                boolean smtpAvailable = isPortAvailable(HOST, PORT_SMTP, CONNECT_TIMEOUT_MS);
                notifyStatusIfChanged("SMTP", PORT_SMTP, smtpAvailable, 2);

                List<Email> emails = null;

                socket = openSocket(HOST, PORT_POP, CONNECT_TIMEOUT_MS);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new DataOutputStream(socket.getOutputStream());

                debug("Conexion POP3 establecida");

                notifyStatusIfChanged("POP3", PORT_POP, true, 0);

                authUser(USER, PASSWORD);

                int count = getEmailCount();

                if (count > 0) {
                    emails = getEmails(count);
                    debug("Correos encontrados: " + count);
                    deleteEmails(count);
                }

                output.writeBytes(Command.quit());
                input.readLine();

                cerrarConexionPop3();

                debug("Conexion POP3 cerrada");

                if (count > 0 && emailEventListener != null) {
                    emailEventListener.onReceiveEmailEvent(emails);
                }

                Thread.sleep(LOOP_DELAY_MS);

            } catch (IOException ex) {
                notifyStatusIfChanged("POP3", PORT_POP, false, 0);
                Logger.getLogger(MailVerificationThread.class.getName()).log(Level.SEVERE, null, ex);
                cerrarConexionPop3();
                safeSleep(LOOP_DELAY_MS);

            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                debug("Hilo interrumpido");
                break;
            }
        }
    }

    private Socket openSocket(String host, int port, int timeoutMs) throws IOException {
        Socket s = new Socket();
        s.connect(new InetSocketAddress(host, port), timeoutMs);
        return s;
    }

    private boolean isPortAvailable(String host, int port, int timeoutMs) {
        Socket s = new Socket();

        try {
            s.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                s.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void notifyStatusIfChanged(String protocol, int port, boolean available, int serverType) {
        Boolean last = null;

        if (serverType == 0) {
            last = lastPopStatus;
        } else if (serverType == 1) {
            last = lastImapStatus;
        } else if (serverType == 2) {
            last = lastSmtpStatus;
        }

        if (last != null && last.booleanValue() == available) {
            return;
        }

        if (serverType == 0) {
            lastPopStatus = available;
        } else if (serverType == 1) {
            lastImapStatus = available;
        } else if (serverType == 2) {
            lastSmtpStatus = available;
        }

        mostrarDashboardServidores();
    }

    private void mostrarDashboardServidores() {
        String smtpBadge = lastSmtpStatus != null && lastSmtpStatus
                ? "<span style='background: #dcfce7; color: #166534; border: 1px solid #86efac; padding: 4px 12px; border-radius: 12px; font-weight: bold; font-size: 11px;'>&#10003; ACTIVO</span>"
                : "<span style='background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; padding: 4px 12px; border-radius: 12px; font-weight: bold; font-size: 11px;'>&#10007; INACTIVO</span>";

        String imapBadge = lastImapStatus != null && lastImapStatus
                ? "<span style='background: #dcfce7; color: #166534; border: 1px solid #86efac; padding: 4px 12px; border-radius: 12px; font-weight: bold; font-size: 11px;'>&#10003; ACTIVO</span>"
                : "<span style='background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; padding: 4px 12px; border-radius: 12px; font-weight: bold; font-size: 11px;'>&#10007; INACTIVO</span>";

        String pop3Badge = lastPopStatus != null && lastPopStatus
                ? "<span style='background: #dcfce7; color: #166534; border: 1px solid #86efac; padding: 4px 12px; border-radius: 12px; font-weight: bold; font-size: 11px;'>&#10003; ACTIVO</span>"
                : "<span style='background: #fee2e2; color: #991b1b; border: 1px solid #fca5a5; padding: 4px 12px; border-radius: 12px; font-weight: bold; font-size: 11px;'>&#10007; INACTIVO</span>";

        String html = "<!DOCTYPE html><html>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body style='font-family: -apple-system, BlinkMacSystemFont, Arial, sans-serif; padding: 18px; background-color: #f1f5f9; margin: 0;'>" +
                "<div style='max-width: 700px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 16px rgba(15,23,42,0.08); border: 1px solid #e2e8f0;'>" +
                "<div style='background: #0f172a; background-image: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #1e3a8a 100%); padding: 18px 22px; border-bottom: 3px solid #2563eb;'>" +
                "<span style='background: rgba(255,255,255,0.12); color: #93c5fd; font-size: 10px; font-weight: bold; letter-spacing: 1.5px; padding: 3px 8px; border-radius: 10px; text-transform: uppercase;'>MONITOREO DE RED</span>" +
                "<h2 style='color: #ffffff; margin: 6px 0 0 0; font-size: 17px; font-weight: 700;'>ESTADO DE SERVIDORES DE CORREO</h2>" +
                "</div>" +
                "<div style='padding: 20px 22px;'>" +
                "<div style='overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 14px;'>" +
                "<table style='width: 100%; border-collapse: collapse; font-size: 13px; text-align: left;'>" +
                "<thead><tr style='background-color: #0f172a; color: #f8fafc; font-size: 11px; text-transform: uppercase; letter-spacing: 0.6px;'>" +
                "<th style='padding: 10px 14px;'>Protocolo</th>" +
                "<th style='padding: 10px 14px;'>Host</th>" +
                "<th style='padding: 10px 14px;'>Puerto</th>" +
                "<th style='padding: 10px 14px; text-align: center;'>Estado del Servicio</th>" +
                "</tr></thead>" +
                "<tbody>" +
                "<tr style='border-top: 1px solid #f1f5f9;'><td style='padding: 10px 14px; font-weight: bold; color: #0f172a;'>SMTP (Envío)</td><td style='padding: 10px 14px; color: #475569;'>" + HOST + "</td><td style='padding: 10px 14px; font-family: Consolas, monospace;'>" + PORT_SMTP + "</td><td style='padding: 10px 14px; text-align: center;'>" + smtpBadge + "</td></tr>" +
                "<tr style='border-top: 1px solid #f1f5f9; background-color: #f8fafc;'><td style='padding: 10px 14px; font-weight: bold; color: #0f172a;'>IMAP (Lectura)</td><td style='padding: 10px 14px; color: #475569;'>" + HOST + "</td><td style='padding: 10px 14px; font-family: Consolas, monospace;'>" + PORT_IMAP + "</td><td style='padding: 10px 14px; text-align: center;'>" + imapBadge + "</td></tr>" +
                "<tr style='border-top: 1px solid #f1f5f9;'><td style='padding: 10px 14px; font-weight: bold; color: #0f172a;'>POP3 (Recepción)</td><td style='padding: 10px 14px; color: #475569;'>" + HOST + "</td><td style='padding: 10px 14px; font-family: Consolas, monospace;'>" + PORT_POP + "</td><td style='padding: 10px 14px; text-align: center;'>" + popBadge(pop3Badge) + "</td></tr>" +
                "</tbody></table>" +
                "</div>" +
                "<p style='font-size: 11px; color: #94a3b8; margin: 0; text-align: right;'>Ciclo de monitoreo automático activo cada 5 segundos.</p>" +
                "</div>" +
                "</div></body></html>";

        Home.mostraRespuesta(html);
    }

    private String popBadge(String badge) {
        return badge;
    }

    private void authUser(String email, String password) throws IOException {
        if (socket != null && input != null && output != null) {
            input.readLine();

            output.writeBytes(Command.user(email));
            input.readLine();

            output.writeBytes(Command.pass(password));
            String message = input.readLine();

            if (message.contains("-ERR")) {
                throw new AuthenticationException();
            }
        }
    }

    private int getEmailCount() throws IOException {
        output.writeBytes(Command.stat());

        String line = input.readLine();
        String[] data = line.split(" ");

        return Integer.parseInt(data[1]);
    }

    private List<Email> getEmails(int count) throws IOException {
        List<Email> emails = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            output.writeBytes(Command.retr(i));

            String text = readMultiline();
            emails.add(Extractor.getEmail(text));
        }

        return emails;
    }

    private void deleteEmails(int emails) throws IOException {
        for (int i = 1; i <= emails; i++) {
            output.writeBytes(Command.dele(i));
        }
    }

    private String readMultiline() throws IOException {
        StringBuilder lines = new StringBuilder();

        boolean firstLine = true;

        while (true) {
            String line = input.readLine();

            if (line == null) {
                throw new IOException("Servidor no responde. Ocurrio un error al abrir el correo.");
            }

            if (line.equals(".")) {
                break;
            }

            if (!firstLine) {
                lines.append('\n');
            }

            lines.append(line);
            firstLine = false;
        }

        return lines.toString();
    }

    private void cerrarConexionPop3() {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException ignored) {
        }

        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException ignored) {
        }

        try {
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ignored) {
        }

        input = null;
        output = null;
        socket = null;
    }

    private void safeSleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private void debug(String mensaje) {
        if (DEBUG) {
            System.out.println(mensaje);
        }
    }
}