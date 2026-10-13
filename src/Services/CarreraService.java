package Services;

import DAO.CarreraDAO;
import Models.Carrera;

import java.sql.SQLException;
import java.util.List;

public class CarreraService {

    private CarreraDAO dao;

    public CarreraService() {
        this.dao = new CarreraDAO();
    }

    public void crearCarrera(List<String> params) throws SQLException {
        if (params.size() < 3) {
            throw new IllegalArgumentException("Formato: carrera agregar codigo,nombre,facultad");
        }
        Carrera c = new Carrera();
        c.setCodigo(params.get(0).trim().toUpperCase());
        c.setNombre(params.get(1).trim());
        c.setFacultad(params.get(2).trim());
        dao.create(c);
    }

    public List<String[]> listarCarreras() throws SQLException {
        return dao.listAll();
    }

    public String getComandosCarrera() {
        return "Comandos Carrera:\n" +
                "- carrera listar\n" +
                "- carrera agregar codigo,nombre,facultad";
    }
}
