package Services;

import DAO.GrupoDAO;
import Models.Grupo;

import java.sql.SQLException;
import java.util.List;

public class GrupoService {

    private GrupoDAO dao;

    public GrupoService() {
        this.dao = new GrupoDAO();
    }

    public void crearGrupo(List<String> params) throws SQLException {
        if (params.size() < 6) {
            throw new IllegalArgumentException(
                    "Formato: grupo agregar id_materia,id_periodo,sigla_grupo,docente,cupo_maximo,cupo_excepcional_maximo"
            );
        }
        int idMateria = Integer.parseInt(params.get(0).trim());
        int idPeriodo = Integer.parseInt(params.get(1).trim());
        String sigla = params.get(2).trim().toUpperCase();
        String docente = params.get(3).trim();
        int max = Integer.parseInt(params.get(4).trim());
        int exc = Integer.parseInt(params.get(5).trim());

        Grupo g = new Grupo(0, idMateria, idPeriodo, sigla, docente, max, max, exc, "ABIERTO");
        dao.create(g);
    }

    public List<String[]> listarGrupos() throws SQLException {
        return dao.listAll();
    }

    public String[] consultarCupo(int idGrupo) throws SQLException {
        return dao.readById(idGrupo);
    }

    public String getComandosGrupo() {
        return "Comandos Grupo / Cupos (CU4):\n" +
                "- grupo listar\n" +
                "- grupo agregar id_materia,id_periodo,sigla_grupo,docente,cupo_maximo,cupo_excepcional_maximo\n" +
                "- grupo consultar id_grupo";
    }
}
