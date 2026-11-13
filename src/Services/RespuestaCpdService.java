package Services;

import Comunication.HtmlBuilder;
import Comunication.SendEmail;
import DAO.NotificacionDAO;
import DAO.OficioCpdDAO;
import DAO.OficioRespuestaCpdDAO;
import Models.OficioRespuestaCpd;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RespuestaCpdService {

    public record ResultadoRespuesta(
            String codigoSolicitud,
            String numeroOficio,
            String periodo,
            String resultado,
            String estado,
            String codigoRespuesta,
            String observacion,
            int academicosNotificados,
            int correosEnviados,
            int correosPendientes,
            boolean estadosEnvioActualizados
    ) {}

    private final OficioRespuestaCpdDAO respuestaDAO = new OficioRespuestaCpdDAO();
    private final OficioCpdDAO oficioDAO = new OficioCpdDAO();
    private final NotificacionDAO notificacionDAO = new NotificacionDAO();

    public ResultadoRespuesta responder(
            List<String> params,
            int idResponsableCpd
    ) throws SQLException {
        if (params == null || params.size() < 5) {
            throw new IllegalArgumentException(
                    "Formato: cpd responder "
                    + "id_oficio,id_solicitud,resultado,codigo_respuesta,observacion");
        }
        int idOficio = parsearId(params.get(0), "El ID del oficio");
        int idSolicitud = parsearId(params.get(1), "El ID de la solicitud");
        String resultado = normalizarResultado(params.get(2));
        String codigo = params.get(3).trim();
        if (codigo.isEmpty() || codigo.length() > 50 || codigo.matches(".*[\\r\\n].*")) {
            throw new IllegalArgumentException(
                    "El código de respuesta CPD es obligatorio y debe tener hasta 50 caracteres.");
        }
        String observacion = String.join(", ", params.subList(4, params.size())).trim();
        if (observacion.isEmpty()) {
            throw new IllegalArgumentException(
                    "La observación debe describir el resultado del procedimiento realizado por CPD.");
        }

        OficioRespuestaCpd respuesta = new OficioRespuestaCpd();
        respuesta.setIdOficio(idOficio);
        respuesta.setIdSolicitud(idSolicitud);
        respuesta.setResultado(resultado);
        respuesta.setEstado(estadoDe(resultado));
        respuesta.setCodigoRespuesta(codigo);
        respuesta.setObservaciones(observacion);
        respuesta.setIdResponsableCpd(idResponsableCpd);

        OficioRespuestaCpdDAO.ResultadoRespuesta guardada =
                respuestaDAO.registrarRespuesta(respuesta);
        String html = HtmlBuilder.generarNotificacionResultadoCpd(
                guardada.codigoSolicitud(),
                guardada.numeroOficio(),
                guardada.periodo(),
                resultado,
                codigo,
                observacion);

        int enviados = 0;
        int pendientes = 0;
        boolean estadosActualizados = true;
        SendEmail email = new SendEmail();
        for (OficioRespuestaCpdDAO.DestinatarioNotificacion destinatario
                : guardada.destinatarios()) {
            String asunto = "Respuesta CPD " + resultado
                    + " - Solicitud " + guardada.codigoSolicitud();
            boolean enviado = email.sendmailDirect(
                    destinatario.email(), asunto, html);
            if (enviado) {
                enviados++;
            } else {
                pendientes++;
            }
            try {
                notificacionDAO.registrarResultadoEnvio(
                        destinatario.idNotificacion(),
                        enviado,
                        enviado ? null : "No se pudo entregar el correo por SMTP.");
            } catch (SQLException ex) {
                estadosActualizados = false;
                System.err.println(
                        "[CU6] No se pudo actualizar el envío de notificación "
                        + destinatario.idNotificacion() + ": " + ex.getMessage());
            }
        }
        return new ResultadoRespuesta(
                guardada.codigoSolicitud(),
                guardada.numeroOficio(),
                guardada.periodo(),
                resultado,
                guardada.estadoNuevo(),
                codigo,
                observacion,
                Math.max(0, guardada.destinatarios().size() - 1),
                enviados,
                pendientes,
                estadosActualizados);
    }

    public List<String[]> listarPendientes() throws SQLException {
        return oficioDAO.listarSolicitudesPendientesCpd();
    }

    private String normalizarResultado(String valor) {
        return switch (valor.trim().toUpperCase(Locale.ROOT)) {
            case "HABILITADO", "HABILITADA" -> "HABILITADO";
            case "OBSERVADO", "OBSERVADA", "OBSERVADA_CPD" -> "OBSERVADO";
            case "RECHAZADO", "RECHAZADA", "RECHAZADA_CPD" -> "RECHAZADO";
            default -> throw new IllegalArgumentException(
                    "Resultado inválido. Use HABILITADO, OBSERVADO o RECHAZADO.");
        };
    }

    private String estadoDe(String resultado) {
        return switch (resultado) {
            case "HABILITADO" -> "HABILITADA";
            case "OBSERVADO" -> "OBSERVADA_CPD";
            case "RECHAZADO" -> "RECHAZADA_CPD";
            default -> throw new IllegalArgumentException("Resultado CPD no soportado.");
        };
    }

    private int parsearId(String valor, String campo) {
        try {
            int id = Integer.parseInt(valor.trim());
            if (id <= 0) {
                throw new NumberFormatException("El ID debe ser positivo.");
            }
            return id;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    campo + " debe ser un número entero positivo.", ex);
        }
    }

    public List<String[]> listarRespuestas() throws SQLException {
        return respuestaDAO.listAll();
    }

    public List<String[]> listarOficiosRemitidos() throws SQLException {
        return oficioDAO.listarOficiosRemitidos();
    }

    public List<File> obtenerArchivosDescargablesOficio(int idOficio)
            throws SQLException {
        String documento = oficioDAO.obtenerRutaDocumentoOficio(idOficio);
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException(
                    "El oficio " + idOficio + " no tiene un documento adjunto.");
        }

        Path raizOficios = Paths.get(
                System.getProperty("user.dir"), "oficios_cpd")
                .toAbsolutePath().normalize();
        Path raizSolicitudes = Paths.get(
                System.getProperty("user.dir"), "archivos_solicitudes")
                .toAbsolutePath().normalize();
        List<File> archivos = new ArrayList<>();
        archivos.add(validarArchivo(documento, raizOficios, "documento del oficio"));
        for (String ruta : oficioDAO.listarRutasComprobantesOficio(idOficio)) {
            archivos.add(validarArchivo(
                    ruta, raizSolicitudes, "comprobante del oficio"));
        }
        return archivos;
    }

    public List<String[]> listarSolicitudesDeOficio(int idOficio)
            throws SQLException {
        List<String[]> solicitudes = oficioDAO.listarSolicitudesDeOficio(idOficio);
        if (solicitudes.isEmpty()) {
            throw new IllegalArgumentException(
                    "No existe un oficio remitido a CPD con ID " + idOficio + ".");
        }
        return solicitudes;
    }

    public String getComandosRespuesta() {
        return "Comandos CU6 - Respuestas de CPD:\n" +
                "- cpd pendientes (oficios y solicitudes remitidas que aún esperan respuesta)\n" +
                "- cpd oficios (lista todos los oficios recibidos por CPD)\n" +
                "- oficio_cpd consultar id_oficio (expediente y documentos del oficio)\n" +
                "- cpd responder id_oficio,id_solicitud,HABILITADO|OBSERVADO|RECHAZADO," +
                "codigo_respuesta,observacion\n" +
                "- respuesta_cpd listar";
    }

    private File validarArchivo(
            String ruta,
            Path raizPermitida,
            String descripcion
    ) {
        if (ruta == null || ruta.isBlank()) {
            throw new IllegalArgumentException(
                    "El " + descripcion + " no tiene una ruta registrada.");
        }
        Path archivo = Paths.get(ruta).toAbsolutePath().normalize();
        if (!archivo.startsWith(raizPermitida)
                || !Files.isRegularFile(archivo)
                || !Files.isReadable(archivo)) {
            throw new IllegalArgumentException(
                    "No se puede descargar el " + descripcion + ": "
                    + archivo + ".");
        }
        return archivo.toFile();
    }
}
