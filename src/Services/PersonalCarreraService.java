package Services;

import DAO.PersonalCarreraDAO;
import Models.PersonalCarrera;

import java.sql.SQLException;
import java.util.List;

public class PersonalCarreraService {

    private PersonalCarreraDAO dao;

    public PersonalCarreraService() {
        this.dao = new PersonalCarreraDAO();
    }

    public void registrarPersonal(List<String> params) throws SQLException {
        if (params.size() < 3) {
            throw new IllegalArgumentException("Formato: personal_carrera agregar id_usuario,cargo,id_carrera");
        }
        int id = Integer.parseInt(params.get(0).trim());
        String cargo = params.get(1).trim();
        int idCarrera = Integer.parseInt(params.get(2).trim());

        dao.create(new PersonalCarrera(id, cargo, idCarrera));
    }

    public List<String[]> listarPersonal() throws SQLException {
        return dao.listAll();
    }

    public String getComandosPersonal() {
        return "Comandos CU1 - Personal Académico:\n" +
                "- personal_carrera listar\n" +
                "- personal_carrera agregar id_usuario,cargo,id_carrera";
    }
}
