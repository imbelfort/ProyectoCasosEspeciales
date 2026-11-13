package Services;

import Comunication.HtmlBuilder;
import Comunication.SendEmail;
import DAO.OficioCpdDAO;
import Models.OficioCpd;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OficioCpdService {

    public record ResultadoRemision(
            String numeroOficio,
            int cantidadSolicitudes,
            boolean enviado,
            String mensaje
    ) {}

    private final OficioCpdDAO oficioDAO;

    public OficioCpdService() {
        this.oficioDAO = new OficioCpdDAO();
    }

    public ResultadoRemision remitirACpd(
            List<String> params,
            int idResponsable
    ) throws SQLException, IOException {
        if (params == null || params.size() < 4) {
            throw new IllegalArgumentException(
                    "Formato: oficio_cpd remitir gestion,correo_cpd,referencia,detalle");
        }
        String gestion = params.get(0).trim();
        if (!gestion.matches("[12]-\\d{4}")) {
            throw new IllegalArgumentException(
                    "La gestión debe tener el formato 1-2026 o 2-2026.");
        }
        String destinatario = params.get(1).trim();
        validarCorreo(destinatario);
        String referencia = obligatorio(params.get(2), "La referencia del oficio es obligatoria.");
        String detalle = obligatorio(
                String.join(", ", params.subList(3, params.size())),
                "El detalle del oficio es obligatorio.");

        List<String[]> casos =
                oficioDAO.listarSolicitudesPagadasElegibles(gestion);
        if (casos.isEmpty()) {
            throw new IllegalArgumentException(
                    "No hay solicitudes aceptadas con comprobante de pago pendientes "
                    + "de remisión para la gestión " + gestion + ".");
        }

        String numero = "OF-CPD-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Path ruta = generarDocumento(numero, gestion, casos);

        OficioCpd oficio = new OficioCpd();
        oficio.setNumeroOficio(numero);
        oficio.setIdSolicitud(Integer.parseInt(casos.get(0)[0]));
        oficio.setDestinatario(destinatario);
        oficio.setReferencia(referencia);
        oficio.setDetalle(detalle);
        oficio.setIdResponsableRemision(idResponsable);
        oficio.setRutaDocumento(ruta.toString());

        List<Integer> ids = new ArrayList<>();
        for (String[] caso : casos) {
            ids.add(Integer.parseInt(caso[0]));
        }
        try {
            oficioDAO.registrarOficioPendiente(oficio, ids, gestion);
        } catch (SQLException | RuntimeException ex) {
            eliminarDocumento(ruta, ex);
            throw ex;
        }
        return enviarOficio(
                oficio.getId(), numero, destinatario, referencia, detalle,
                gestion, ruta, ids.size());
    }

    public ResultadoRemision reenviarOficioPendiente(
            List<String> params
    ) throws SQLException {
        if (params == null || params.size() != 1) {
            throw new IllegalArgumentException(
                    "Formato: oficio_cpd generar id_oficio_pendiente");
        }
        int idOficio;
        try {
            idOficio = Integer.parseInt(params.get(0).trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El ID del oficio debe ser un número entero.", ex);
        }
        String[] datos = oficioDAO.obtenerOficioPendiente(idOficio);
        if (datos == null) {
            throw new IllegalArgumentException(
                    "No existe un oficio pendiente de envío con ID " + idOficio + ".");
        }
        Path ruta = Paths.get(datos[5]).toAbsolutePath().normalize();
        if (!Files.isRegularFile(ruta) || !Files.isReadable(ruta)) {
            throw new IllegalArgumentException(
                    "No se encuentra el documento del oficio pendiente: " + ruta + ".");
        }
        int cantidad = oficioDAO.contarSolicitudesDeOficio(idOficio);
        String periodo = oficioDAO.obtenerGestionPeriodoOficio(idOficio);
        if (periodo == null) {
            throw new IllegalArgumentException(
                    "No se pudo determinar el período académico del oficio "
                    + idOficio + ".");
        }
        return enviarOficio(
                idOficio, datos[1], datos[2], datos[3], datos[4],
                periodo, ruta, cantidad);
    }

    private ResultadoRemision enviarOficio(
            int idOficio,
            String numero,
            String destinatario,
            String referencia,
            String detalle,
            String periodo,
            Path ruta,
            int cantidad
    ) throws SQLException {
        List<File> adjuntos = obtenerAdjuntosOficio(idOficio, ruta);
        String html = HtmlBuilder.generarNotificacionRemisionCpd(
                numero, periodo, referencia, detalle, cantidad);
        boolean enviado = new SendEmail().sendmailWithAttachments(
                destinatario,
                "Remisión de casos especiales - " + numero,
                html,
                adjuntos);
        if (!enviado) {
            return new ResultadoRemision(
                    numero, cantidad, false,
                    "Oficio registrado con estado PENDIENTE_ENVIO. El servidor SMTP no confirmó "
                    + "la entrega; reintente con oficio_cpd generar " + idOficio + ".");
        }
        try {
            oficioDAO.marcarOficioEnviado(idOficio);
        } catch (SQLException ex) {
            throw new SQLException(
                    "El correo del oficio " + numero + " fue enviado, pero no se pudo confirmar "
                    + "la remisión en la base de datos. Revise el estado antes de reenviar.",
                    ex);
        }
        return new ResultadoRemision(
                numero, cantidad, true,
                "Oficio " + numero + " del período " + periodo
                + " remitido exitosamente a " + destinatario
                + " con " + cantidad + " solicitud(es) y el documento adjunto.");
    }

    private List<File> obtenerAdjuntosOficio(int idOficio, Path documento)
            throws SQLException {
        List<File> adjuntos = new ArrayList<>();
        adjuntos.add(documento.toFile());
        Path raizArchivos = Paths.get(
                System.getProperty("user.dir"), "archivos_solicitudes")
                .toAbsolutePath().normalize();
        for (String ruta : oficioDAO.listarRutasComprobantesOficio(idOficio)) {
            if (ruta == null || ruta.isBlank()) {
                throw new IllegalArgumentException(
                        "Un comprobante asociado al oficio no tiene una ruta de archivo.");
            }
            Path comprobante = Paths.get(ruta).toAbsolutePath().normalize();
            if (!comprobante.startsWith(raizArchivos)
                    || !Files.isRegularFile(comprobante)
                    || !Files.isReadable(comprobante)) {
                throw new IllegalArgumentException(
                        "No se puede leer un comprobante incluido en el oficio: "
                        + comprobante + ".");
            }
            adjuntos.add(comprobante.toFile());
        }
        if (adjuntos.size() == 1) {
            throw new IllegalArgumentException(
                    "El oficio no tiene comprobantes de pago adjuntos.");
        }
        return adjuntos;
    }

    private Path generarDocumento(
            String numero,
            String periodo,
            List<String[]> casos
    ) throws IOException {
        Path directorio = Paths.get(
                System.getProperty("user.dir"), "oficios_cpd").toAbsolutePath().normalize();
        Files.createDirectories(directorio);
        Path documento = directorio.resolve(numero + ".csv").normalize();
        if (!documento.startsWith(directorio)) {
            throw new IOException("La ruta del documento de remisión no es válida.");
        }

        StringBuilder csv = new StringBuilder("\uFEFF")
                .append("Período del oficio,")
                .append(celdaCsv(periodo))
                .append("\r\n");
        csv.append("Código solicitud,Periodo,Estudiante,Registro universitario,Correo,")
                .append("Tipo de caso,Motivo,Materias y grupos,Comprobante(s)\r\n");
        for (String[] caso : casos) {
            for (int i = 1; i < caso.length; i++) {
                if (i > 1) {
                    csv.append(',');
                }
                csv.append(celdaCsv(caso[i]));
            }
            csv.append("\r\n");
        }
        Files.write(documento, csv.toString().getBytes(StandardCharsets.UTF_8));
        return documento;
    }

    private String celdaCsv(String valor) {
        String seguro = valor == null ? "" : valor;
        String sinEspaciosIniciales = seguro.stripLeading();
        if (!sinEspaciosIniciales.isEmpty()
                && "=+-@".indexOf(sinEspaciosIniciales.charAt(0)) >= 0) {
            seguro = "'" + seguro;
        }
        return "\"" + seguro.replace("\"", "\"\"") + "\"";
    }

    private String obligatorio(String valor, String error) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(error);
        }
        return valor.trim();
    }

    private void validarCorreo(String correo) {
        if (correo.isEmpty()) {
            throw new IllegalArgumentException("El correo destinatario de CPD es obligatorio.");
        }
        try {
            InternetAddress address = new InternetAddress(correo, true);
            address.validate();
        } catch (AddressException ex) {
            throw new IllegalArgumentException(
                    "El correo destinatario de CPD no tiene un formato válido.", ex);
        }
    }

    private void eliminarDocumento(Path ruta, Exception errorOriginal) {
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException cleanupException) {
            errorOriginal.addSuppressed(cleanupException);
        }
    }

    public List<String[]> listarOficios() throws SQLException {
        return oficioDAO.listAll();
    }

    public String getComandosOficio() {
        return "Comandos CU5 - Procesamiento y remisión a CPD:\n" +
                "- oficio_cpd listar\n" +
                "- oficio_cpd remitir gestion,correo_cpd,referencia,detalle " +
                "(ejemplo: 2-2026; incluye solicitudes aprobadas con comprobante; adjunta CSV y comprobantes)\n" +
                "- oficio_cpd generar id_oficio_pendiente (reintenta un envío pendiente)";
    }
}
