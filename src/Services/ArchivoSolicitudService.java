package Services;

import DAO.ArchivoSolicitudDAO;
import Models.ArchivoSolicitud;

import java.sql.SQLException;
import java.util.List;

public class ArchivoSolicitudService {

    private ArchivoSolicitudDAO dao;

    public ArchivoSolicitudService() {
        this.dao = new ArchivoSolicitudDAO();
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

    public List<String[]> listarArchivosDeSolicitud(int idSolicitud) throws SQLException {
        return dao.listBySolicitud(idSolicitud);
    }

    public String getComandosArchivo() {
        return "Comandos Archivo de Solicitud (CU2):\n" +
                "- archivo_solicitud listar id_solicitud\n" +
                "- archivo_solicitud agregar id_solicitud,nombre_archivo,tipo_archivo,ruta_o_descripcion";
    }
}
