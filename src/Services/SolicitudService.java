package Services;

import DAO.HistorialSolicitudDAO;
import DAO.NotificacionDAO;
import DAO.SolicitudDAO;
import DAO.SolicitudGrupoDAO;
import Models.HistorialSolicitud;
import Models.Notificacion;
import Models.Solicitud;
import Models.SolicitudGrupo;

import java.sql.SQLException;
import java.util.List;

public class SolicitudService {

    private SolicitudDAO solicitudDAO;
    private SolicitudGrupoDAO solicitudGrupoDAO;
    private HistorialSolicitudDAO historialDAO;
    private NotificacionDAO notificacionDAO;

    private DAO.EstudianteDAO estudianteDAO;

    public SolicitudService() {
        this.solicitudDAO = new SolicitudDAO();
        this.solicitudGrupoDAO = new SolicitudGrupoDAO();
        this.historialDAO = new HistorialSolicitudDAO();
        this.notificacionDAO = new NotificacionDAO();
        this.estudianteDAO = new DAO.EstudianteDAO();
    }

    public Solicitud registrarSolicitud(List<String> params, String remitenteEmail) throws SQLException {
        int idEstudiante;
        int idPeriodo;
        String tipoCaso;
        String motivo;

        if (params.size() >= 4) {
            idEstudiante = Integer.parseInt(params.get(0).trim());
            idPeriodo = Integer.parseInt(params.get(1).trim());
            tipoCaso = params.get(2).trim().toUpperCase();
            motivo = params.get(3).trim();
        } else if (params.size() == 3) {
            // Se infiere el estudiante por su correo remitente
            Integer idInferido = estudianteDAO.findIdByEmail(remitenteEmail);
            if (idInferido == null) {
                throw new IllegalArgumentException(
                        "No se encontró un estudiante asociado al correo '" + remitenteEmail +
                        "'. Utilice: solicitud registrar id_estudiante,id_periodo,tipo_caso,motivo"
                );
            }
            idEstudiante = idInferido;
            idPeriodo = Integer.parseInt(params.get(0).trim());
            tipoCaso = params.get(1).trim().toUpperCase();
            motivo = params.get(2).trim();
        } else {
            throw new IllegalArgumentException(
                    "Formato: solicitud registrar id_estudiante,id_periodo,tipo_caso,motivo (o: solicitud registrar id_periodo,tipo_caso,motivo)"
            );
        }

        String codigo = "SOL-2026-" + String.format("%04d", (int) (System.currentTimeMillis() % 10000));

        Solicitud sol = new Solicitud();
        sol.setCodigo(codigo);
        sol.setIdEstudiante(idEstudiante);
        sol.setIdPeriodo(idPeriodo);
        sol.setTipoCaso(tipoCaso);
        sol.setMotivo(motivo);
        sol.setEstado("ENVIADA");

        solicitudDAO.create(sol);

        // Registrar en historial (CU7)
        HistorialSolicitud hist = new HistorialSolicitud();
        hist.setIdSolicitud(sol.getId());
        hist.setIdUsuario(idEstudiante);
        hist.setEstadoAnterior("INICIAL");
        hist.setEstadoNuevo("ENVIADA");
        hist.setAccion("REGISTRO_SOLICITUD");
        hist.setObservacion("Solicitud de caso especial registrada por estudiante con motivo: " + motivo);
        historialDAO.create(hist);

        // Registrar notificación (CU7)
        Notificacion notif = new Notificacion();
        notif.setIdUsuario(idEstudiante);
        notif.setIdSolicitud(sol.getId());
        notif.setDestinatario(remitenteEmail != null ? remitenteEmail : "estudiante@uagrm.edu.bo");
        notif.setTipo("RECEPCION_SOLICITUD");
        notif.setAsunto("Constancia de recepcion - Solicitud " + codigo);
        notif.setMensaje("Su solicitud de caso especial " + codigo + " (" + tipoCaso + ") ha sido recibida formalmente.");
        notificacionDAO.create(notif);

        return sol;
    }

    public void agregarGrupoASolicitud(List<String> params) throws SQLException {
        if (params.size() < 3) {
            throw new IllegalArgumentException("Formato: solicitud_grupo agregar id_solicitud,id_grupo,motivo");
        }
        int idSolicitud = Integer.parseInt(params.get(0).trim());
        int idGrupo = Integer.parseInt(params.get(1).trim());
        String motivo = params.get(2).trim();

        SolicitudGrupo sg = new SolicitudGrupo(0, idSolicitud, idGrupo, motivo, "PENDIENTE");
        solicitudGrupoDAO.create(sg);
    }

    public List<String[]> listarSolicitudes() throws SQLException {
        return solicitudDAO.listAll();
    }

    public String[] consultarSolicitud(int id) throws SQLException {
        return solicitudDAO.readById(id);
    }

    public List<String[]> listarGruposDeSolicitud(int idSolicitud) throws SQLException {
        return solicitudGrupoDAO.listBySolicitud(idSolicitud);
    }

    public List<String[]> listarMisCasos(String emailRemitente, List<String> params) throws SQLException {
        if (params.size() >= 2) {
            String registro = params.get(0).trim();
            String password = params.get(1).trim();
            if (!estudianteDAO.autenticar(registro, password)) {
                throw new IllegalArgumentException(
                        "Error de Autenticación: La contraseña o el registro universitario '" + registro +
                        "' son incorrectos. Verifique sus credenciales e intente nuevamente."
                );
            }
            return solicitudDAO.listByRegistro(registro);
        } else if (params.size() == 1) {
            String registro = params.get(0).trim();
            List<String[]> list = solicitudDAO.listByRegistro(registro);
            if (list.isEmpty()) {
                throw new IllegalArgumentException("No se encontraron solicitudes registradas para el registro universitario: '" + registro + "'.");
            }
            return list;
        } else {
            Integer idInferido = estudianteDAO.findIdByEmail(emailRemitente);
            if (idInferido == null) {
                throw new IllegalArgumentException(
                        "No se encontró ningún estudiante registrado con el correo remitente '" + emailRemitente + "'. " +
                        "Si está enviando desde otro correo, autentíquese con: solicitud mis_casos [registro_universitario,contraseña]"
                );
            }
            return solicitudDAO.listByEstudianteEmail(emailRemitente);
        }
    }

    public List<String[]> listarMisPendientes(String emailRemitente, List<String> params) throws SQLException {
        if (params.size() >= 2) {
            String registro = params.get(0).trim();
            String password = params.get(1).trim();
            if (!estudianteDAO.autenticar(registro, password)) {
                throw new IllegalArgumentException(
                        "Error de Autenticación: La contraseña o el registro universitario '" + registro +
                        "' son incorrectos. Verifique sus credenciales e intente nuevamente."
                );
            }
            List<String[]> todas = solicitudDAO.listByRegistro(registro);
            List<String[]> pendientes = new java.util.ArrayList<String[]>();
            for (String[] sol : todas) {
                String estado = sol[6];
                if (!"HABILITADA".equalsIgnoreCase(estado) && !"RECHAZADA".equalsIgnoreCase(estado)) {
                    pendientes.add(sol);
                }
            }
            return pendientes;
        } else if (params.size() == 1) {
            String registro = params.get(0).trim();
            List<String[]> todas = solicitudDAO.listByRegistro(registro);
            List<String[]> pendientes = new java.util.ArrayList<String[]>();
            for (String[] sol : todas) {
                String estado = sol[6];
                if (!"HABILITADA".equalsIgnoreCase(estado) && !"RECHAZADA".equalsIgnoreCase(estado)) {
                    pendientes.add(sol);
                }
            }
            return pendientes;
        } else {
            Integer idInferido = estudianteDAO.findIdByEmail(emailRemitente);
            if (idInferido == null) {
                throw new IllegalArgumentException(
                        "No se encontró ningún estudiante registrado con el correo remitente '" + emailRemitente + "'. " +
                        "Si está enviando desde otro correo, consulte con: solicitud pendientes [registro_universitario,contraseña]"
                );
            }
            return solicitudDAO.listPendientesByEstudianteEmail(emailRemitente);
        }
    }

    public String getComandosSolicitud() {
        return "Comandos CU2 - Registro de Solicitud de Caso Especial:\n" +
                "- solicitud listar\n" +
                "- solicitud mis_casos [registro,password]\n" +
                "- solicitud pendientes [registro,password]\n" +
                "- solicitud registrar id_estudiante,id_periodo,tipo_caso,motivo\n" +
                "- solicitud registrar id_periodo,tipo_caso,motivo (identifica estudiante por correo)\n" +
                "- solicitud consultar id_solicitud\n" +
                "- solicitud_grupo agregar id_solicitud,id_grupo,motivo\n" +
                "- solicitud_grupo listar id_solicitud";
    }
}
