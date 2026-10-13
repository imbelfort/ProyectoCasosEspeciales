package Services;

import DAO.PeriodoDAO;
import Models.Periodo;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class PeriodoService {

    private PeriodoDAO dao;

    public PeriodoService() {
        this.dao = new PeriodoDAO();
    }

    public void crearPeriodo(List<String> params) throws SQLException {
        if (params.size() < 3) {
            throw new IllegalArgumentException("Formato: periodo agregar gestion(e.g. 1-2026),fecha_inicio(yyyy-MM-dd),fecha_fin(yyyy-MM-dd)");
        }
        String gestion = params.get(0).trim();
        Date ini = Date.valueOf(params.get(1).trim());
        Date fin = Date.valueOf(params.get(2).trim());
        String estado = params.size() > 3 ? params.get(3).trim().toUpperCase() : "ACTIVO";

        dao.create(new Periodo(0, gestion, ini, fin, estado));
    }

    public List<String[]> listarPeriodos() throws SQLException {
        return dao.listAll();
    }

    public String getComandosPeriodo() {
        return "Comandos Periodo Académico:\n" +
                "- periodo listar\n" +
                "- periodo agregar gestion,fecha_inicio,fecha_fin";
    }
}
