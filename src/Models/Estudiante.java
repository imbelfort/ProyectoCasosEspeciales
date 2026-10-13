package Models;

public class Estudiante {
    private int idEstudiante; // FK Usuario(id_usuario)
    private String registroUniversitario;
    private int idCarrera;
    private int semestreActual;

    public Estudiante() {
    }

    public Estudiante(int idEstudiante, String registroUniversitario, int idCarrera, int semestreActual) {
        this.idEstudiante = idEstudiante;
        this.registroUniversitario = registroUniversitario;
        this.idCarrera = idCarrera;
        this.semestreActual = semestreActual;
    }

    public int getIdEstudiante() {
        return idEstudiante;
    }

    public void setIdEstudiante(int idEstudiante) {
        this.idEstudiante = idEstudiante;
    }

    public String getRegistroUniversitario() {
        return registroUniversitario;
    }

    public void setRegistroUniversitario(String registroUniversitario) {
        this.registroUniversitario = registroUniversitario;
    }

    public int getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(int idCarrera) {
        this.idCarrera = idCarrera;
    }

    public int getSemestreActual() {
        return semestreActual;
    }

    public void setSemestreActual(int semestreActual) {
        this.semestreActual = semestreActual;
    }
}
