
package Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InicializarDB {

    // ==========================================
    // INICIALIZAR CONEXION A LA BASE DE DATOS
    // ==========================================

    public static boolean inicializarSistema() {

        System.out.println(
            "======================================");
        System.out.println(
            " SISTEMA DE CASOS ESPECIALES FICCT");
        System.out.println(
            "======================================");

        try (Connection con = Conexion.getInstancia()
                .EstablecerConexion()) {

            if (con == null || con.isClosed()) {
                System.err.println(
                    "[ERROR] No se pudo conectar con PostgreSQL.");
                return false;
            }

            // Comprobar que PostgreSQL responde.
            String sql = "SELECT 1";

            try (PreparedStatement ps =
                    con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                if (!rs.next() || rs.getInt(1) != 1) {
                    System.err.println(
                        "[ERROR] PostgreSQL no responde.");
                    return false;
                }
            }

            System.out.println(
                "[OK] Conexion a PostgreSQL establecida.");

            try (PreparedStatement ps = con.prepareStatement(
                    "ALTER TABLE sesion_usuario " +
                    "ADD COLUMN IF NOT EXISTS remitente_email VARCHAR(150)")) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "CREATE UNIQUE INDEX IF NOT EXISTS idx_sesion_remitente_activa " +
                    "ON sesion_usuario(LOWER(remitente_email)) " +
                    "WHERE estado = 'ACTIVA'")) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "ALTER TABLE oficio_cpd " +
                    "ADD COLUMN IF NOT EXISTS ruta_documento TEXT")) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS oficio_cpd_solicitud (" +
                    "id_oficio INTEGER NOT NULL REFERENCES oficio_cpd(id) ON DELETE CASCADE, " +
                    "id_solicitud INTEGER NOT NULL REFERENCES solicitud(id), " +
                    "PRIMARY KEY (id_oficio, id_solicitud))")) {
                ps.executeUpdate();
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "CREATE INDEX IF NOT EXISTS idx_oficio_cpd_solicitud_solicitud " +
                    "ON oficio_cpd_solicitud(id_solicitud)")) {
                ps.executeUpdate();
            }

            System.out.println(
                "[OK] Base de datos disponible.");

            System.out.println(
                "[OK] Inicializacion finalizada.");

            System.out.println(
                "======================================");

            return true;

        } catch (SQLException e) {

            System.err.println(
                "[ERROR] Fallo la conexion a la base de datos.");

            // El detalle tecnico queda en la consola.
            e.printStackTrace();

            return false;
        }
    }

    // ==========================================
    // EJECUCION INDEPENDIENTE
    // ==========================================

    public static void main(String[] args) {

        boolean conectado = inicializarSistema();

        if (conectado) {
            System.out.println(
                "El sistema puede comenzar a operar.");
        } else {
            System.err.println(
                "No es posible iniciar el sistema.");
            System.exit(1);
        }
    }
}
