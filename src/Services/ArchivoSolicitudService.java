package Services;

import DAO.ArchivoSolicitudDAO;
import Models.ArchivoSolicitud;
import Utils.Email;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class ArchivoSolicitudService {

    private ArchivoSolicitudDAO dao;
    private DAO.SolicitudDAO solicitudDAO;
    private DAO.EstudianteDAO estudianteDAO;

    public ArchivoSolicitudService() {
        this.dao = new ArchivoSolicitudDAO();
        this.solicitudDAO = new DAO.SolicitudDAO();
        this.estudianteDAO = new DAO.EstudianteDAO();
    }

    public void adjuntarArchivo(List<String> params) throws SQLException {
        if (params.size() < 4) {
            throw new IllegalArgumentException("Formato: archivo_solicitud agregar id_solicitud,nombre_archivo,tipo_archivo,ruta_o_descripcion");
        }
        int idSol = Integer.parseInt(params.get(0).trim());
        String nombre = params.get(1).trim();
        String tipo = params.get(2).trim();
        String ruta = params.get(3).trim();

        dao.create(new ArchivoSolicitud(0, idSol, nombre, tipo, ruta, null));
    }

    public void adjuntarArchivos(
            List<String> params,
            String remitenteEmail,
            List<Email.Attachment> adjuntos
    ) throws SQLException, IOException {
        if (params.size() != 2 && params.size() != 4) {
            throw new IllegalArgumentException(
                    "Formato: archivo_solicitud adjuntar id_solicitud,tipo_documento " +
                    "y adjunte uno o varios archivos al correo.");
        }

        int idSolicitud = Integer.parseInt(params.get(0).trim());
        Integer idEstudiante = estudianteDAO.findIdByEmail(remitenteEmail);
        if (idEstudiante == null) {
            throw new IllegalArgumentException(
                    "No se encontró un estudiante asociado al correo remitente '" +
                    remitenteEmail + "'.");
        }
        String tipo = params.size() == 2
                ? params.get(1).trim().toUpperCase(Locale.ROOT) : "";
        if ("COMPROBANTE_PAGO".equals(tipo)) {
            throw new IllegalArgumentException(
                    "Los pagos se registran con: pago adjuntar "
                    + "id_solicitud,COMPROBANTE_PAGO.");
        }
        solicitudDAO.validarSolicitudEditable(idSolicitud, idEstudiante);

        if (params.size() == 4) {
            String nombre = params.get(1).trim();
            String tipoDocumento = params.get(2).trim();
            String ruta = params.get(3).trim();
            if (dao.exists(idSolicitud, nombre, tipoDocumento)) {
                throw new IllegalArgumentException(
                        "Ese documento ya está registrado en la solicitud.");
            }
            dao.createForEstudiante(
                    new ArchivoSolicitud(
                            0, idSolicitud, nombre, tipoDocumento, ruta, null),
                    idEstudiante);
            return;
        }

        if (adjuntos == null || adjuntos.isEmpty()) {
            throw new IllegalArgumentException(
                    "El correo debe incluir al menos un archivo adjunto.");
        }

        if (tipo.isEmpty()) {
            throw new IllegalArgumentException("El tipo de documento es obligatorio.");
        }

        List<ArchivoSolicitud> archivos = new ArrayList<>();
        List<byte[]> contenidos = new ArrayList<>();
        Set<String> nombresEnCorreo = new HashSet<>();
        for (Email.Attachment adjunto : adjuntos) {
            String nombre = nombreSeguro(adjunto.getFileName());
            String clave = nombre.toLowerCase(Locale.ROOT);
            if (!nombresEnCorreo.add(clave)) {
                throw new IllegalArgumentException(
                        "El correo contiene archivos duplicados con el nombre '" + nombre + "'.");
            }
            if (dao.exists(idSolicitud, nombre, tipo)) {
                throw new IllegalArgumentException(
                        "El archivo '" + nombre + "' ya está registrado con el tipo " + tipo + ".");
            }
            if (adjunto.getContentBase64() == null
                    || adjunto.getContentBase64().trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "No se pudo leer el contenido del archivo '" + nombre + "'.");
            }
            byte[] contenido;
            try {
                contenido = Base64.getDecoder().decode(adjunto.getContentBase64());
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "El archivo adjunto '" + nombre + "' tiene contenido inválido.", ex);
            }
            if (contenido.length == 0) {
                throw new IllegalArgumentException(
                        "El archivo adjunto '" + nombre + "' está vacío.");
            }
            contenidos.add(contenido);
            archivos.add(new ArchivoSolicitud(
                    0, idSolicitud, nombre, tipo, null, null));
        }

        Path directorio = Paths.get(
                System.getProperty("user.dir"),
                "archivos_solicitudes",
                String.valueOf(idSolicitud))
                .toAbsolutePath()
                .normalize();
        Files.createDirectories(directorio);

        for (int i = 0; i < adjuntos.size(); i++) {
            Email.Attachment adjunto = adjuntos.get(i);
            ArchivoSolicitud archivo = archivos.get(i);
            byte[] contenido = contenidos.get(i);
            Path destino = directorio.resolve(
                    UUID.randomUUID().toString() + "_" + nombreParaDisco(archivo.getNombreArchivo()))
                    .normalize();
            if (!destino.startsWith(directorio)) {
                throw new IOException("La ruta del archivo adjunto no es válida.");
            }
            try {
                Files.write(destino, contenido);
            } catch (IOException ex) {
                eliminarArchivoParcial(destino, ex);
                throw ex;
            }
            archivo.setRutaArchivo(destino.toString());
            try {
                dao.createForEstudiante(archivo, idEstudiante);
            } catch (SQLException | RuntimeException ex) {
                eliminarArchivoParcial(destino, ex);
                throw ex;
            }
        }
    }

    private void eliminarArchivoParcial(Path ruta, Exception errorOriginal) {
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException cleanupException) {
            errorOriginal.addSuppressed(cleanupException);
        }
    }

    private String nombreSeguro(String nombreOriginal) {
        if (nombreOriginal == null || nombreOriginal.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Todos los archivos adjuntos deben tener un nombre.");
        }
        String nombre = Paths.get(nombreOriginal).getFileName().toString().trim();
        if (nombre.isEmpty() || ".".equals(nombre) || "..".equals(nombre)) {
            throw new IllegalArgumentException("El nombre de un archivo adjunto no es válido.");
        }
        return nombre;
    }

    private String nombreParaDisco(String nombre) {
        String seguro = nombre.replaceAll("[^A-Za-z0-9._-]", "_");
        return seguro.isEmpty() ? "documento" : seguro;
    }

    public List<String[]> listarArchivosDeSolicitud(int idSolicitud) throws SQLException {
        return dao.listBySolicitud(idSolicitud);
    }

    public List<File> obtenerArchivosDescargables(int idSolicitud)
            throws SQLException {
        List<ArchivoSolicitud> documentos = dao.listDetailsBySolicitud(idSolicitud);
        List<File> archivos = new ArrayList<>();
        for (ArchivoSolicitud documento : documentos) {
            String ruta = documento.getRutaArchivo();
            if (ruta == null || ruta.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "El documento '" + documento.getNombreArchivo()
                        + "' no tiene un archivo almacenado para descargar.");
            }
            File archivo = new File(ruta).getAbsoluteFile();
            if (!archivo.isFile() || !archivo.canRead()) {
                throw new IllegalArgumentException(
                        "No se puede descargar el documento '"
                        + documento.getNombreArchivo() + "': el archivo no está disponible.");
            }
            archivos.add(archivo);
        }
        return archivos;
    }

    public String getComandosArchivo() {
        return "Comandos Archivo de Solicitud (CU2):\n" +
                "- archivo_solicitud listar id_solicitud\n" +
                "- archivo_solicitud adjuntar id_solicitud,tipo_documento (adjunte uno o varios archivos al correo)\n" +
                "- Para registrar pagos use: pago adjuntar id_solicitud,COMPROBANTE_PAGO";
    }
}
