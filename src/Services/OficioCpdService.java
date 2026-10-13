package Services;

import DAO.HistorialSolicitudDAO;
import DAO.NotificacionDAO;
import DAO.OficioCpdDAO;
import DAO.SolicitudDAO;
import Models.HistorialSolicitud;
import Models.Notificacion;
import Models.OficioCpd;

import java.sql.SQLException;
import java.util.List;

public class OficioCpdService {

    private OficioCpdDAO oficioDAO;
    private SolicitudDAO solicitudDAO;
    private HistorialSolicitudDAO historialDAO;
    private NotificacionDAO notificacionDAO;

    public OficioCpdService() {
        this.oficioDAO = new OficioCpdDAO();
        this.solicitudDAO = new SolicitudDAO();
        this.historialDAO = new HistorialSolicitudDAO();
        this.notificacionDAO = new NotificacionDAO();
    }

    public void remitirACpd(List<String> params, String remitenteEmail) throws SQLException {
        if (params.size() < 5) {
            throw new IllegalArgumentException(
                    "Formato: oficio_cpd generar id_solicitud,destinatario,referencia,detalle,id_responsable"
            );
        }
        int idSol = Integer.parseInt(params.get(0).trim());
        String dest = params.get(1).trim();
        String ref = params.get(2).trim();
        String detalle = params.get(3).trim();
        int idResp = Integer.parseInt(params.get(4).trim());

        String numOficio = "OF-CPD-2026-" + String.format("%04d", (int) (System.currentTimeMillis() % 10000));

        OficioCpd of = new OficioCpd();
        of.setNumeroOficio(numOficio);
        of.setIdSolicitud(idSol);
        of.setDestinatario(dest);
        of.setReferencia(ref);
        of.setDetalle(detalle);
        of.setIdResponsableRemision(idResp);
        of.setEstado("EMITIDO");

        oficioDAO.create(of);

        // Actualizar estado de solicitud
        solicitudDAO.updateEstado(idSol, "REMITIDA_A_CPD", idResp, "Remitido a CPD mediante oficio " + numOficio, null);

        // Historial
        HistorialSolicitud hist = new HistorialSolicitud();
        hist.setIdSolicitud(idSol);
        hist.setIdUsuario(idResp);
        hist.setEstadoAnterior("APROBADA_DIRECCION");
        hist.setEstadoNuevo("REMITIDA_A_CPD");
        hist.setAccion("REMISION_CPD");
        hist.setObservacion("Oficio emitido a CPD (" + dest + ") Ref: " + ref);
        historialDAO.create(hist);

        // Notificación a CPD y estudiante
        Notificacion notif = new Notificacion();
        notif.setIdSolicitud(idSol);
        notif.setIdUsuario(idResp);
        notif.setDestinatario("cpd@uagrm.edu.bo");
        notif.setTipo("REMISION_CPD");
        notif.setAsunto("Remision de caso especial: Oficio " + numOficio);
        notif.setMensaje("Se ha remitido a CPD el expediente de caso especial correspondiente a la solicitud ID " + idSol + ".");
        notificacionDAO.create(notif);
    }

    public List<String[]> listarOficios() throws SQLException {
        return oficioDAO.listAll();
    }

    public String getComandosOficio() {
        return "Comandos CU5 - Remisión a CPD:\n" +
                "- oficio_cpd listar\n" +
                "- oficio_cpd generar id_solicitud,destinatario,referencia,detalle,id_responsable";
    }
}
