package Database;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 * @author Usuario
 */
public class Conexion {

    private static Conexion instancia;
    private static URLClassLoader driverLoader;
    private static boolean driverInicializado;
    private Connection con;

    public static synchronized boolean inicializarDriverJdbc() {
        if (driverInicializado) {
            return true;
        }

        try {
            Class.forName(ConfigDB.DRIVER);
            driverInicializado = true;
            return true;
        } catch (ClassNotFoundException ignored) {
            // Se intenta cargar el driver desde la carpeta lib del proyecto.
        }

        File libDir = new File(System.getProperty("user.dir"), "lib");
        if (!libDir.exists() || !libDir.isDirectory()) {
            return false;
        }

        File[] jars = libDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".jar"));
        if (jars == null || jars.length == 0) {
            return false;
        }

        try {
            URL[] urls = new URL[jars.length];
            for (int i = 0; i < jars.length; i++) {
                urls[i] = jars[i].toURI().toURL();
            }

            driverLoader = new URLClassLoader(urls, Conexion.class.getClassLoader());
            Driver driver = (Driver) Class.forName(
                    ConfigDB.DRIVER, true, driverLoader).getDeclaredConstructor().newInstance();
            DriverManager.registerDriver(new DriverDelegado(driver));
            Thread.currentThread().setContextClassLoader(driverLoader);
            driverInicializado = true;
            return true;
        } catch (Exception e) {
            System.err.println("Error cargando driver JDBC local: " + e.getMessage());
            return false;
        }
    }

    public Conexion(){
        this.con = null;
    }

    public Connection EstablecerConexion(){
        try {
            if (!inicializarDriverJdbc()) {
                System.err.println("Error DB: no se pudo cargar el driver JDBC de PostgreSQL.");
                return null;
            }
            this.con = DriverManager.getConnection(
                ConfigDB.URL + ConfigDB.DB,
                ConfigDB.USER,
                ConfigDB.PASSWORD
            );
        } catch (SQLException e) {
            System.out.println("Error DB: "+e);
        }
        return this.con;
    }

    private static final class DriverDelegado implements Driver {
        private final Driver delegado;

        private DriverDelegado(Driver delegado) {
            this.delegado = delegado;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            return delegado.connect(url, info);
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return delegado.acceptsURL(url);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
                throws SQLException {
            return delegado.getPropertyInfo(url, info);
        }

        @Override
        public int getMajorVersion() {
            return delegado.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return delegado.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return delegado.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            return delegado.getParentLogger();
        }
    }

    public void CerrarConexion(){
        try {
            if (this.con != null) {
                this.con.close();
            }
        } catch (SQLException e) {
            System.out.println("Error DB: "+e);
        }
    }

    public static Conexion getInstancia(){
        if(instancia == null){
            instancia = new Conexion();
        }
        return instancia;
    }

}
