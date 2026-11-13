
package Services;

import DAO.AccesoDAO;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;

public class AccesoService {

    private final AccesoDAO dao = new AccesoDAO();
    private final UsuarioService usuarios = new UsuarioService();

    private static final SecureRandom RANDOM =
        new SecureRandom();

    private static final int DURACION_HORAS = 2;

    public record ResultadoAcceso(
        String mensaje,
        String token,
        Set<String> roles
    ) {}

    public record Contexto(
        int idUsuario,
        String email,
        Set<String> roles
    ) {
        public boolean esAdministrativo() {
            return roles.contains("DIRECTOR") ||
                   roles.contains("PERSONAL_ACADEMICO");
        }

        public boolean tieneRol(String rol) {
            return roles.contains(rol);
        }
    }

    // ==========================================
    // INICIAR SESION
    // ==========================================
    public ResultadoAcceso iniciarSesion(
            String correo,
            String password,
            String remitente
    ) throws SQLException {

        String email = validarEmail(correo);
        String remitenteEmail = validarEmail(remitente);

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                "Debe introducir su contrasena.");
        }

        AccesoDAO.Cuenta cuenta = dao.buscarCuenta(email);

        // Mensaje uniforme para no revelar cuentas existentes.
        if (cuenta == null ||
            !"ACTIVO".equals(cuenta.estado())) {
            throw new SecurityException(
                "Credenciales incorrectas o cuenta no disponible.");
        }

        if (!usuarios.verificarContrasena(
                cuenta.id(), password)) {
            throw new SecurityException(
                "Credenciales incorrectas o cuenta no disponible.");
        }

        Set<String> roles = dao.obtenerRoles(cuenta.id());

        if (roles.isEmpty()) {
            throw new SecurityException(
                "La cuenta no tiene un rol activo asignado.");
        }

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes);

        dao.crearSesion(
            cuenta.id(),
            hashToken(token),
            LocalDateTime.now().plusHours(DURACION_HORAS),
            remitenteEmail
        );

        return new ResultadoAcceso(
            "Acceso autorizado para " + remitenteEmail + ".\n" +
            "Sesion valida durante 2 horas. Ya puede enviar " +
            "sus comandos sin incluir una credencial.",
            token,
            roles
        );
    }

    public Contexto validarPorRemitente(String remitente)
            throws SQLException {
        String remitenteEmail = validarEmail(remitente);
        AccesoDAO.Sesion sesion =
            dao.validarSesionPorRemitente(remitenteEmail);

        if (sesion == null) {
            throw new SecurityException(
                "No hay una sesion activa para este remitente. " +
                "Inicie sesion con 'acceder sistema correo,password'.");
        }

        dao.actualizarUltimoUsoPorRemitente(remitenteEmail);
        return new Contexto(
            sesion.idUsuario(),
            sesion.email(),
            Set.copyOf(sesion.roles())
        );
    }

    // ==========================================
    // VALIDAR SESION
    // ==========================================
    public Contexto validar(
            String token
    ) throws SQLException {

        validarToken(token);
        String hash = hashToken(token);

        AccesoDAO.Sesion sesion =
            dao.validarSesion(hash);

        if (sesion == null) {
            throw new SecurityException(
                "Su sesion no existe, ha expirado " +
                "o no corresponde a esta cuenta. " +
                "Inicie sesion nuevamente.");
        }

        dao.actualizarUltimoUso(hash);

        return new Contexto(
            sesion.idUsuario(),
            sesion.email(),
            Set.copyOf(sesion.roles())
        );
    }

    // ==========================================
    // CERRAR SESION
    // ==========================================
    public String cerrarSesion(
            String token
    ) throws SQLException {

        Contexto contexto = validar(token);

        dao.cerrarSesion(
            hashToken(token),
            contexto.idUsuario()
        );

        return "Su sesion ha sido cerrada correctamente.";
    }

    public String cerrarSesionPorRemitente(String remitente)
            throws SQLException {
        String remitenteEmail = validarEmail(remitente);
        Contexto contexto = validarPorRemitente(remitenteEmail);
        dao.cerrarSesionPorRemitente(remitenteEmail);
        return "Sesion cerrada para " + contexto.email() + ".";
    }

    // ==========================================
    // AUTORIZAR COMANDO
    // ==========================================
    public void autorizar(
            Contexto usuario,
            String entidad,
            String accion
    ) {

        entidad = entidad.toLowerCase(Locale.ROOT);
        accion = accion.toLowerCase(Locale.ROOT);

        // Director y academico: todos los comandos
        // funcionales, siempre sujetos a reglas de negocio.
        if (usuario.esAdministrativo()) {
            return;
        }

        if (usuario.tieneRol("ESTUDIANTE")) {

            boolean permitido =
                switch (entidad + " " + accion) {

                    case "solicitud help",
                         "solicitud registrar",
                         "solicitud mis_casos",
                         "solicitud consultar",
                         "solicitud enviar",
                         "solicitud cancelar",
                         "solicitud_grupo agregar",
                         "solicitud_grupo listar",
                         "archivo_solicitud agregar",
                         "archivo_solicitud listar",
                         "pago agregar",
                         "pago listar",
                         "pago help",
                         "historial consultar",
                         "grupo listar",
                         "grupo consultar",
                         "materia listar",
                         "periodo listar",
                         "carrera listar",
                         "notificacion listar",
                         "estudiante help",
                         "solicitud_grupo help",
                         "archivo_solicitud help" -> true;

                    default -> false;
                };

            if (permitido) return;
        }

        if (usuario.tieneRol("CPD")) {

            boolean permitido =
                switch (entidad + " " + accion) {

                    case "respuesta_cpd help",
                         "respuesta_cpd listar",
                         "respuesta_cpd registrar",
                         "cpd help",
                         "cpd pendientes",
                         "cpd oficios",
                         "cpd responder",
                         "oficio_cpd listar",
                         "oficio_cpd consultar",
                         "solicitud consultar",
                         "historial consultar",
                         "notificacion listar" -> true;

                    default -> false;
                };

            if (permitido) return;
        }

        throw new SecurityException(
            "ACCESO DENEGADO. Su rol no tiene " +
            "permisos para ejecutar el comando: " +
            entidad + " " + accion);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (Exception e) {
            throw new IllegalStateException(
                "No fue posible procesar la sesion.");
        }
    }

    private void validarToken(String token) {

        if (token == null ||
            !token.matches("^[A-Za-z0-9_-]{43}$")) {
            throw new SecurityException(
                "Credencial de sesion invalida.");
        }
    }

    private String validarEmail(String valor) {

        if (valor == null) {
            throw new IllegalArgumentException(
                "El correo es obligatorio.");
        }

        String email = valor.trim()
            .toLowerCase(Locale.ROOT);

        if (email.length() > 150 ||
            !email.matches(
                "^[a-z0-9_+%-]+(?:\\.[a-z0-9_+%-]+)*" +
                "@[a-z0-9-]+(?:\\.[a-z0-9-]+)+$")) {
            throw new IllegalArgumentException(
                "El correo electronico no es valido.");
        }

        return email;
    }
}
