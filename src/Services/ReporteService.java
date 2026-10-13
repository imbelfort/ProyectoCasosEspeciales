package Services;

import DAO.ReporteDAO;

import java.sql.SQLException;
import java.util.List;

public class ReporteService {

    private final ReporteDAO reporteDAO;

    public ReporteService() {
        this.reporteDAO = new ReporteDAO();
    }

    public List<String[]> reporteGeneral() throws SQLException {
        return reporteDAO.reporteGeneral();
    }

    public List<String[]> reporteEstados() throws SQLException {
        return reporteDAO.reportePorEstado();
    }

    public List<String[]> reportePeriodos() throws SQLException {
        return reporteDAO.reportePorPeriodo();
    }

    public List<String[]> reporteCupos() throws SQLException {
        return reporteDAO.reporteCupos();
    }

    public void registrarAuditoriaReporte(int idUsuario, String tipo, String params) throws SQLException {
        reporteDAO.registrarReporte(idUsuario, tipo, params);
    }

    public String getComandosReporte() {
        return "Comandos CU8 - Reportes y Estadísticas:\n" +
                "- reporte general\n" +
                "- reporte estados\n" +
                "- reporte periodos\n" +
                "- reporte cupos\n" +
                "- reporte help";
    }
}