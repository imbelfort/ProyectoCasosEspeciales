
package Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class LimpiarDB {

    private static final String CONFIRMACION =
            "ELIMINAR_BASE_FICCT";

    public static void main(String[] args) {

        try {
            if (args.length != 1 ||
                    !CONFIRMACION.equals(args[0])) {

                System.err.println(
                    "Limpieza cancelada. " +
                    "Debe confirmar la operacion.");

                System.err.println(
                    "Argumento requerido: " + CONFIRMACION);
                return;
            }

            limpiarBaseDeDatos();

        } catch (Exception e) {
            System.err.println(
                "[ERROR] No se pudo limpiar la base de datos.");
            e.printStackTrace();
        }
    }

    public static void limpiarBaseDeDatos()
            throws SQLException {

        String permitido =
                System.getenv("PERMITIR_RESET_DB");

        if (!"true".equalsIgnoreCase(permitido)) {
            throw new SecurityException(
                "La limpieza esta deshabilitada. " +
                "Configure PERMITIR_RESET_DB=true " +
                "solamente en el entorno de pruebas.");
        }

        String[] tablas = {

            "reporte",
            "notificaciones",
            "historial_solicitud",

            "oficio_respuesta_cpd",
            "oficio_cpd",

            "bloqueo_grupo",
            "evaluacion_solicitud",

            "archivo_solicitud",
            "solicitud_grupo",
            "solicitud",

            "horario_grupo",
            "grupo",
            "materia_carrera",
            "materia",
            "periodos",

            "director_carrera",
            "personal_carrera",
            "estudiantes",
            "carrera",

            "sesion_usuario",
            "usuario_roles",
            "roles",
            "usuario"
        };

        Connection con = Conexion.getInstancia()
                .EstablecerConexion();

        if (con == null) {
            throw new SQLException(
                "No se pudo establecer la conexion.");
        }

        try (con) {

            con.setAutoCommit(false);

            try (Statement stmt = con.createStatement()) {

                for (String tabla : tablas) {

                    String sql =
                        "DROP TABLE IF EXISTS public." +
                        tabla + " CASCADE";

                    stmt.executeUpdate(sql);

                    System.out.println(
                        "[ELIMINADA] " + tabla);
                }

                con.commit();

                System.out.println(
                    "\n[OK] Limpieza finalizada.");

                System.out.println(
                    "La base de datos ya no contiene " +
                    "las tablas del proyecto.");

            } catch (SQLException e) {

                con.rollback();
                throw e;
            }
        }
    }
}
