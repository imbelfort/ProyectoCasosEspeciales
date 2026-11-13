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
    private DAO.PeriodoDAO periodoDAO;

    private DAO.EstudianteDAO estudianteDAO;

    public SolicitudService() {
        this.solicitudDAO = new SolicitudDAO();
        this.solicitudGrupoDAO = new SolicitudGrupoDAO();
        this.historialDAO = new HistorialSolicitudDAO();
        this.notificacionDAO = new NotificacionDAO();
        this.periodoDAO = new DAO.PeriodoDAO();
        this.estudianteDAO = new DAO.EstudianteDAO();
    }

    public Solicitud registrarSolicitud(List<String> params, String remitenteEmail) throws SQLException {
        if (params.size() < 3) {
            throw new IllegalArgumentException(
                    "Formato: enviar solicitud gestion,tipo_caso,motivo "
                    + "(ejemplo: 2-2026,TIPO_CASO,motivo)");
        }

        Integer idEstudiante = estudianteDAO.findIdByEmail(remitenteEmail);
        if (idEstudiante == null) {
            throw new IllegalArgumentException(
                    "No se encontró un estudiante asociado al correo remitente '" +
                    remitenteEmail + "'.");
        }

        String gestion = params.get(0).trim();
        if (!gestion.matches("[12]-\\d{4}")) {
            throw new IllegalArgumentException(
                    "La gestión debe tener el formato 1-2026 o 2-2026.");
        }
        Integer idPeriodo = periodoDAO.findIdByGestion(gestion);
        if (idPeriodo == null) {
            throw new IllegalArgumentException(
                    "No existe el período académico con gestión " + gestion + ".");
        }
        String tipoCaso = params.get(1).trim().toUpperCase(java.util.Locale.ROOT);
        String motivo = String.join(", ", params.subList(2, params.size())).trim();
        if (tipoCaso.isEmpty() || motivo.isEmpty()) {
            throw new IllegalArgumentException(
                    "El tipo de caso y el motivo son obligatorios.");
        }

        Solicitud sol = new Solicitud();
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
        notif.setAsunto("Constancia de recepcion - Solicitud " + sol.getCodigo());
        notif.setMensaje("Su solicitud de caso especial " + sol.getCodigo() + " (" + tipoCaso + ") ha sido recibida formalmente.");
        notificacionDAO.create(notif);

        return sol;
    }

    public void agregarGrupoASolicitud(List<String> params, String remitenteEmail)
            throws SQLException {
        if (params.size() < 3) {
            throw new IllegalArgumentException(
                    "Formato: solicitar materia_grupo id_solicitud,periodo,sigla_materia,sigla_grupo,motivo");
        }
        int idSolicitud = Integer.parseInt(params.get(0).trim());
        Integer idEstudiante = estudianteDAO.findIdByEmail(remitenteEmail);
        if (idEstudiante == null) {
            throw new IllegalArgumentException(
                    "No se encontró un estudiante asociado al correo remitente '" +
                    remitenteEmail + "'.");
        }
        solicitudDAO.validarSolicitudEditable(idSolicitud, idEstudiante);

        boolean formatoNuevo = params.size() >= 5;
        SolicitudGrupo sg;
        try {
            if (formatoNuevo) {
                String periodo = params.get(1).trim();
                String siglaMateria = params.get(2).trim().toUpperCase(java.util.Locale.ROOT);
                String siglaGrupo = params.get(3).trim().toUpperCase(java.util.Locale.ROOT);
                String motivo = String.join(", ", params.subList(4, params.size())).trim();
                if (periodo.isEmpty() || siglaMateria.isEmpty()
                        || siglaGrupo.isEmpty() || motivo.isEmpty()) {
                    throw new IllegalArgumentException(
                            "El período, la sigla de la materia, el grupo y el motivo son obligatorios.");
                }
                sg = new SolicitudGrupo(0, idSolicitud, 0, motivo, "PENDIENTE");
                solicitudGrupoDAO.createForEstudiante(
                        sg, idEstudiante, periodo, siglaMateria, siglaGrupo);
            } else {
                int idGrupo = Integer.parseInt(params.get(1).trim());
                String motivo = String.join(", ", params.subList(2, params.size())).trim();
                sg = new SolicitudGrupo(0, idSolicitud, idGrupo, motivo, "PENDIENTE");
                solicitudGrupoDAO.createForEstudiante(sg, idEstudiante);
            }
        } catch (SQLException ex) {
            if ("23505".equals(ex.getSQLState())) {
                throw new IllegalArgumentException(
                        "Ese grupo ya fue agregado a la solicitud.", ex);
            }
            throw ex;
        }
    }

    public List<String[]> listarSolicitudes() throws SQLException {
        return solicitudDAO.listAll();
    }

    public List<String[]> listarSolicitudesPorPeriodo(String periodo) throws SQLException {
        return solicitudDAO.listByPeriodo(periodo);
    }

    public List<String[]> listarSolicitudesPorGestion(String gestion) throws SQLException {
        validarGestion(gestion);
        return solicitudDAO.listByPeriodo(gestion);
    }

    public List<String[]> listarSolicitudesPendientesPorPeriodo(String periodo)
            throws SQLException {
        validarGestion(periodo);
        List<String[]> solicitudes = solicitudDAO.listByPeriodo(periodo);
        List<String[]> pendientes = new java.util.ArrayList<String[]>();
        for (String[] solicitud : solicitudes) {
            String estado = solicitud[6];
            if (!"HABILITADA".equalsIgnoreCase(estado)
                    && !"RECHAZADA".equalsIgnoreCase(estado)) {
                pendientes.add(solicitud);
            }
        }
        return pendientes;
    }

    private void validarGestion(String periodo) {
        if (periodo == null || !periodo.trim().matches("[12]-\\d{4}")) {
            throw new IllegalArgumentException(
                    "Indique el período con el formato 1-2026 o 2-2026, no el ID.");
        }
    }

    public String[] consultarSolicitud(int id) throws SQLException {
        return solicitudDAO.readById(id);
    }

    public String[] consultarSolicitudPropia(int id, String remitenteEmail)
            throws SQLException {
        Integer idEstudiante = estudianteDAO.findIdByEmail(remitenteEmail);
        if (idEstudiante == null) {
            throw new IllegalArgumentException(
                    "No se encontró un estudiante asociado al correo remitente.");
        }
        return solicitudDAO.readByIdForEstudiante(id, idEstudiante);
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
                "- enviar solicitud gestion,tipo_caso,motivo (ejemplo: 2-2026,TIPO_CASO,motivo)\n" +
                "- solicitar materia_grupo id_solicitud,periodo,sigla_materia,sigla_grupo,motivo (se puede repetir)\n" +
                "- archivo_solicitud adjuntar id_solicitud,tipo_documento (adjunte el archivo al correo; se puede repetir)\n" +
                "- pago adjuntar id_solicitud,COMPROBANTE_PAGO (adjunte exactamente un comprobante)\n" +
                "- pago listar (alias: mis_pagos listar)\n" +
                "- solicitud listar periodo (solo Director/Personal Académico)\n" +
                "- solicitud periodos id_periodo_o_gestion (solo Director/Personal Académico)\n" +
                "- solicitud mis_casos [registro,password]\n" +
                "- solicitud pendientes listar,periodo (solo Director/Personal Académico)\n" +
                "- solicitud pendientes [registro,password]\n" +
                "- solicitud consultar id_solicitud (Director/Personal Académico recibe los documentos adjuntos)\n" +
                "- solicitud_grupo listar id_solicitud";
    }
}
