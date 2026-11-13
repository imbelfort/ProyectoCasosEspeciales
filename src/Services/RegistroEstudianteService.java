package Services;

import java.sql.SQLException;
import java.util.List;

public class RegistroEstudianteService {

    private final UsuarioService usuarioService = new UsuarioService();

    public String registrar(List<String> params)
            throws SQLException {
        if (params == null || params.size() != 11) {
            throw new IllegalArgumentException(
                    "Formato: estudiante registrar nombre,apellidos,ci,email," +
                    "telefono,direccion,fecha_nacimiento,password," +
                    "registro_universitario,id_carrera,semestre");
        }

        int id = usuarioService.crearEstudiante(params);
        return "REGISTRO COMPLETADO\n"
                + "Su usuario fue creado correctamente.\n"
                + "ID de usuario: " + id + "\n"
                + "Rol asignado: ESTUDIANTE\n"
                + "Registro universitario: " + params.get(8).trim() + "\n"
                + "Ya puede iniciar sesion en el sistema.";
    }
}
