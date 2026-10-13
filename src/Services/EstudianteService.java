package Services;

import DAO.EstudianteDAO;
import Models.Estudiante;

import java.sql.SQLException;
import java.util.List;

public class EstudianteService {

    private EstudianteDAO estudianteDAO;

    public EstudianteService() {
        this.estudianteDAO = new EstudianteDAO();
    }

    public void registrarEstudiante(List<String> params) throws SQLException {
        if (params.size() < 4) {
            throw new IllegalArgumentException("Formato: estudiante agregar id_usuario,registro_universitario,id_carrera,semestre_actual");
        }
        int idUsuario = Integer.parseInt(params.get(0).trim());
        String reg = params.get(1).trim();
        int idCarrera = Integer.parseInt(params.get(2).trim());
        int sem = Integer.parseInt(params.get(3).trim());

        Estudiante e = new Estudiante(idUsuario, reg, idCarrera, sem);
        estudianteDAO.create(e);
    }

    public List<String[]> listarEstudiantes() throws SQLException {
        return estudianteDAO.listAll();
    }

    public String getComandosEstudiante() {
        return "Comandos CU1 - Estudiante:\n" +
                "- estudiante listar\n" +
                "- estudiante agregar id_usuario,registro_universitario,id_carrera,semestre_actual";
    }
}
