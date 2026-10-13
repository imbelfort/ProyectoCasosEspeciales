package Services;

import DAO.DirectorCarreraDAO;
import Models.DirectorCarrera;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class DirectorCarreraService {

    private DirectorCarreraDAO dao;

    public DirectorCarreraService() {
        this.dao = new DirectorCarreraDAO();
    }

    public void registrarDirector(List<String> params) throws SQLException {
        if (params.size() < 4) {
            throw new IllegalArgumentException("Formato: director_carrera agregar id_usuario,id_carrera,gestion_inicio(yyyy-MM-dd),gestion_fin(yyyy-MM-dd)");
        }
        int id = Integer.parseInt(params.get(0).trim());
        int idCarrera = Integer.parseInt(params.get(1).trim());
        Date ini = Date.valueOf(params.get(2).trim());
        Date fin = Date.valueOf(params.get(3).trim());

        dao.create(new DirectorCarrera(id, idCarrera, ini, fin));
    }

    public List<String[]> listarDirectores() throws SQLException {
        return dao.listAll();
    }

    public String getComandosDirector() {
        return "Comandos CU1 - Director de Carrera:\n" +
                "- director_carrera listar\n" +
                "- director_carrera agregar id_usuario,id_carrera,gestion_inicio,gestion_fin";
    }
}
