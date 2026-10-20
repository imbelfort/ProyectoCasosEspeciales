package proyectomailgrupo01sa;

import Comunication.Home;
import Database.ConfigDB;
import Database.ConfigEmailServer;
import Database.InicializarDB;
import Utils.Email;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Scanner;

/**
 * Modo Consola / Terminal para el Servidor de Casos Especiales Académicos.
 * Permite ejecutar el sistema sin entorno gráfico (Swing), manteniendo activo
 * el monitoreo continuo de correos electrónicos en segundo plano y permitiendo
 * ingresar comandos de prueba directamente por consola.
 */
public class Consola {

    public static void main(String[] args) {
        System.out.println("=================================================================");
        System.out.println("  SISTEMA DE CASOS ESPECIALES ACADÉMICOS - FICCT UAGRM (GRUPO 01 SA)");
        System.out.println("                     MODO TERMINAL / CONSOLA                     ");
        System.out.println("=================================================================");

        mostrarEstadoConexiones();

        // 1. Inicialización de la base de datos
        System.out.println("[INFO] Inicializando base de datos PostgreSQL...");
        try {
            InicializarDB.crearTablasSiNoExisten();
            InicializarDB.insertarDatosIniciales();
            System.out.println("[OK] Base de datos verificada y lista.");
        } catch (Exception e) {
            System.err.println("[WARN] No se pudo conectar a la BD o ya estaba inicializada: " + e.getMessage());
        }

        // 2. Inicio del hilo de monitoreo de correos (POP3 / SMTP)
        System.out.println("[INFO] Iniciando demonio de monitoreo de correos POP3...");
        try {
            Home.iniciar();
            System.out.println("[OK] Demonio de correo ACTIVO (cuenta: grupo01sa@tecnoweb.org.bo)");
            System.out.println("[OK] Revisando servidor cada 5 segundos.");
        } catch (Exception e) {
            System.err.println("[ERROR] Error al iniciar el hilo de correo: " + e.getMessage());
        }

        System.out.println("=================================================================");
        System.out.println("El sistema está activo respondiendo correos entrantes.");
        System.out.println("Puede escribir comandos directamente en esta consola o 'exit' para salir.");
        System.out.println("Comandos de ejemplo:");
        System.out.println("  - help");
        System.out.println("  - usuario listar");
        System.out.println("  - solicitud listar");
        System.out.println("  - grupo listar");
        System.out.println("  - reporte general");
        System.out.println("=================================================================");

        Scanner scanner = new Scanner(System.in);
        String remitenteSimulado = "admin@uagrm.edu.bo";

        while (true) {
            System.out.print("\ncasos-especiales> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String linea = scanner.nextLine().trim();

            if (linea.isEmpty()) {
                continue;
            }

            if ("exit".equalsIgnoreCase(linea) || "salir".equalsIgnoreCase(linea) || "quit".equalsIgnoreCase(linea)) {
                System.out.println("[INFO] Deteniendo servidor de consola. ¡Hasta luego!");
                System.exit(0);
                break;
            }

            try {
                Email emailSimulado = new Email(remitenteSimulado, linea);
                Home.interprete(emailSimulado);
            } catch (Exception e) {
                System.err.println("[ERROR] Ocurrió un error al procesar el comando: " + e.getMessage());
            }
        }

        scanner.close();
    }

    private static void mostrarEstadoConexiones() {
        final String hostCorreo = ConfigEmailServer.HOST;
        final int timeoutMs = 5000;

        System.out.println();
        System.out.println("--------------- ESTADO DE CONEXIONES ---------------------------");
        System.out.println("[INFO] Verificando servicios externos...");

        verificarBaseDeDatos();
        verificarSocket("SMTP", hostCorreo, 25, timeoutMs);
        verificarSocket("POP3", hostCorreo, 110, timeoutMs);
        verificarSocket("IMAP", hostCorreo, 143, timeoutMs);

        System.out.println("---------------------------------------------------------------");
        System.out.println();
    }

    private static void verificarBaseDeDatos() {
        try {
            Class.forName(ConfigDB.DRIVER);
            DriverManager.setLoginTimeout(5);

            try (Connection connection = DriverManager.getConnection(
                    ConfigDB.URL + ConfigDB.DB,
                    ConfigDB.USER,
                    ConfigDB.PASSWORD)) {
                System.out.println("[OK]     PostgreSQL | " + ConfigDB.URL + ConfigDB.DB
                        + " | conexión establecida");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR]  PostgreSQL | driver JDBC no encontrado: " + e.getMessage());
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
}
