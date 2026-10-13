package Services;

import DAO.MateriaDAO;
import Models.Materia;

import java.sql.SQLException;
import java.util.List;

public class MateriaService {

    private MateriaDAO dao;

    public MateriaService() {
        this.dao = new MateriaDAO();
    }

    public void crearMateria(List<String> params) throws SQLException {
        if (params.size() < 4) {
            throw new IllegalArgumentException("Formato: materia agregar sigla,nombre,nivel_semestre,creditos");
        }
        String sigla = params.get(0).trim().toUpperCase();
        String nombre = params.get(1).trim();
        int nivel = Integer.parseInt(params.get(2).trim());
        int creditos = Integer.parseInt(params.get(3).trim());

        dao.create(new Materia(0, sigla, nombre, nivel, creditos));
    }

    public List<String[]> listarMaterias() throws SQLException {
        return dao.listAll();
    }

    public String getComandosMateria() {
        return "Comandos Materia (CU4):\n" +
                "- materia listar\n" +
                "- materia agregar sigla,nombre,nivel_semestre,creditos";
    }
}
