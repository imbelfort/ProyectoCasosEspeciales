package Services;

import DAO.ArchivoSolicitudDAO;
import DAO.EstudianteDAO;
import Models.ArchivoSolicitud;
import Utils.Email;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class PagoService {

    private final ArchivoSolicitudDAO archivoDAO;
    private final EstudianteDAO estudianteDAO;

    public PagoService() {
        this.archivoDAO = new ArchivoSolicitudDAO();
        this.estudianteDAO = new EstudianteDAO();
    }

    public void adjuntarPago(
            List<String> params,
            String remitenteEmail,
            List<Email.Attachment> adjuntos
    ) throws SQLException, IOException {
        if (params == null || params.size() != 2) {
            throw new IllegalArgumentException(
                    "Formato: pago adjuntar id_solicitud,tipo_documento "
                    + "y adjunte exactamente un comprobante.");
        }
        if (!"COMPROBANTE_PAGO".equals(
                params.get(1).trim().toUpperCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                    "El tipo de documento del pago debe ser COMPROBANTE_PAGO.");
        }
        if (adjuntos == null || adjuntos.size() != 1) {
            throw new IllegalArgumentException(
                    "Adjunte exactamente un comprobante de pago al correo.");
        }

        int idSolicitud;
        try {
            idSolicitud = Integer.parseInt(params.get(0).trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "El ID de la solicitud debe ser un número entero.", ex);
        }
        Integer idEstudiante = estudianteDAO.findIdByEmail(remitenteEmail);
        if (idEstudiante == null) {
            throw new IllegalArgumentException(
                    "No se encontró un estudiante asociado al correo remitente '"
                    + remitenteEmail + "'.");
        }

        Email.Attachment adjunto = adjuntos.get(0);
        String nombre = nombreSeguro(adjunto.getFileName());
        if (adjunto.getContentBase64() == null
                || adjunto.getContentBase64().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "No se pudo leer el comprobante de pago.");
        }
        byte[] contenido;
        try {
            contenido = Base64.getDecoder().decode(adjunto.getContentBase64());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "El comprobante de pago tiene contenido inválido.", ex);
        }
        if (contenido.length == 0) {
            throw new IllegalArgumentException("El comprobante de pago está vacío.");
        }

        Path directorio = Paths.get(
                System.getProperty("user.dir"),
                "archivos_solicitudes",
                String.valueOf(idSolicitud)).toAbsolutePath().normalize();
        Files.createDirectories(directorio);
        Path destino = directorio.resolve(
                UUID.randomUUID() + "_" + nombreParaDisco(nombre)).normalize();
        if (!destino.startsWith(directorio)) {
            throw new IOException("La ruta del comprobante no es válida.");
        }
        try {
            Files.write(destino, contenido);
            archivoDAO.createPaymentReceiptForEstudiante(
                    new ArchivoSolicitud(
                            0, idSolicitud, nombre, "COMPROBANTE_PAGO",
                            destino.toString(), null),
                    idEstudiante);
        } catch (SQLException | RuntimeException | IOException ex) {
            eliminarArchivoParcial(destino, ex);
            throw ex;
        }
    }

    public List<String[]> listarMisPagos(String remitenteEmail)
            throws SQLException {
        Integer idEstudiante = estudianteDAO.findIdByEmail(remitenteEmail);
        if (idEstudiante == null) {
            throw new IllegalArgumentException(
                    "No se encontró ningún estudiante registrado con el correo remitente '"
                    + remitenteEmail + "'.");
        }
        return archivoDAO.listPaymentsByEstudiante(idEstudiante);
    }

    public String getComandosPago() {
        return "Comandos de Pagos:\n"
                + "- pago adjuntar id_solicitud,COMPROBANTE_PAGO "
                + "(adjunte exactamente un comprobante)\n"
                + "- pago listar (alias: mis_pagos listar)";
    }

    private String nombreSeguro(String nombreOriginal) {
        if (nombreOriginal == null || nombreOriginal.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El comprobante debe tener un nombre de archivo.");
        }
        String nombre = Paths.get(nombreOriginal).getFileName().toString().trim();
        if (nombre.isEmpty() || ".".equals(nombre) || "..".equals(nombre)) {
            throw new IllegalArgumentException(
                    "El nombre del comprobante no es válido.");
        }
        return nombre;
    }

    private String nombreParaDisco(String nombre) {
        String seguro = nombre.replaceAll("[^A-Za-z0-9._-]", "_");
        return seguro.isEmpty() ? "comprobante_pago" : seguro;
    }

    private void eliminarArchivoParcial(Path ruta, Exception errorOriginal) {
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException cleanupException) {
            errorOriginal.addSuppressed(cleanupException);
        }
    }
}
