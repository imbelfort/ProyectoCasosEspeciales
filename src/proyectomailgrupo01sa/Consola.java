package proyectomailgrupo01sa;

import Comunication.Home;
import Database.ConfigDB;
import Database.ConfigEmailServer;
import Database.Conexion;
import Database.InicializarDB;
import Utils.Email;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

/**
 * Modo Consola / Terminal para el Servidor de Casos Especiales Académicos.
 * Permite ejecutar el sistema sin entorno gráfico (Swing), manteniendo activo
 * el monitoreo continuo de correos electrónicos en segundo plano y permitiendo
 * ingresar comandos de prueba directamente por consola.
 */
public class Consola {

    private static void cargarDriverJDBC() {
        if (!Conexion.inicializarDriverJdbc()) {
            System.err.println("[WARN] No se pudo cargar el driver JDBC de PostgreSQL.");
        }
    }

    public static void main(String[] args) {
        cargarDriverJDBC();

        System.out.println("=================================================================");
        System.out.println("  SISTEMA DE CASOS ESPECIALES ACADÉMICOS - FICCT UAGRM (GRUPO 01 SA)");
        System.out.println("                     MODO TERMINAL / CONSOLA                     ");
        System.out.println("=================================================================");

        mostrarEstadoConexiones();

        // 1. Inicialización de la base de datos
        System.out.println("[INFO] Inicializando base de datos PostgreSQL...");
        try {
            if (InicializarDB.inicializarSistema()) {
                System.out.println("[OK] Base de datos verificada y lista.");
            } else {
                System.err.println("[ERROR] No se pudo conectar con PostgreSQL.");
            }
        } catch (Exception e) {
            System.err.println("[WARN] No se pudo conectar a la BD o ya estaba inicializada: " + e.getMessage());
        }

        // 2. Inicio del hilo de monitoreo de correos (POP3 / SMTP)
        System.out.println("[INFO] Iniciando demonio de monitoreo de correos POP3...");
        try {
            Home.iniciar();
            System.out.println("[OK] Demonio de correo ACTIVO (cuenta: " + ConfigEmailServer.MAIL + ")");
            System.out.println("[OK] Revisando servidor cada 5 segundos.");
        } catch (Exception e) {
            System.err.println("[ERROR] Error al iniciar el hilo de correo: " + e.getMessage());
        }

        System.out.println("=================================================================");
        System.out.println("El sistema está activo respondiendo correos entrantes.");
        System.out.println("El servidor permanece activo y procesa comandos recibidos por correo.");
        System.out.println("=================================================================");

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
                System.out.println("[INFO] Servidor detenido.")));
        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void mostrarEstadoConexiones() {
        final String hostCorreo = ConfigEmailServer.HOST;
        final int portSmtp = parsePort(ConfigEmailServer.PORT_SMTP, 25);
        final int portPop3 = parsePort(ConfigEmailServer.PORT_POP3, 110);
        final int portImap = parsePort(ConfigEmailServer.PORT_IMAP, 143);
        final int timeoutMs = 5000;

        System.out.println();
        System.out.println("--------------- ESTADO DE CONEXIONES ---------------------------");
        System.out.println("[INFO] Verificando servicios externos...");

        verificarBaseDeDatos();
        verificarSocket("SMTP", hostCorreo, portSmtp, timeoutMs);
        verificarSocket("POP3", hostCorreo, portPop3, timeoutMs);
        verificarSocket("IMAP", hostCorreo, portImap, timeoutMs);

        System.out.println("---------------------------------------------------------------");
        System.out.println();
    }

    private static void verificarBaseDeDatos() {
        try {
            if (!Conexion.inicializarDriverJdbc()) {
                System.err.println("[ERROR]  PostgreSQL | driver JDBC no encontrado: "
                        + ConfigDB.DRIVER);
                return;
            }
            DriverManager.setLoginTimeout(5);

            try (Connection connection = DriverManager.getConnection(
                    ConfigDB.URL + ConfigDB.DB,
                    ConfigDB.USER,
                    ConfigDB.PASSWORD)) {
                System.out.println("[OK]     PostgreSQL | " + ConfigDB.URL + ConfigDB.DB
                        + " | conexión establecida");
            }
        } catch (Exception e) {
            System.err.println("[ERROR]  PostgreSQL | no disponible: " + describirError(e));
        }
    }

    private static void verificarSocket(String servicio, String host, int puerto, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, puerto), timeoutMs);
            socket.setSoTimeout(timeoutMs);

            String respuesta = null;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()))) {
                respuesta = reader.readLine();
            }

            if (respuesta == null || respuesta.trim().isEmpty()) {
                System.out.println("[OK]     " + servicio + " | " + host + ":" + puerto
                        + " | puerto accesible");
            } else {
                System.out.println("[OK]     " + servicio + " | " + host + ":" + puerto
                        + " | " + respuesta);
            }
        } catch (Exception e) {
            System.err.println("[ERROR]  " + servicio + " | " + host + ":" + puerto
                    + " | " + describirError(e));
        }
    }

    private static String describirError(Exception error) {
        String mensaje = error.getMessage();
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return error.getClass().getSimpleName();
        }
        return mensaje.trim();
    }

    private static int parsePort(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
