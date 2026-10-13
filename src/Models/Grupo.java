package Models;

public class Grupo {
    private int id;
    private int idMateria;
    private int idPeriodo;
    private String siglaGrupo; // e.g. "SA", "SB", "SC"
    private String docente;
    private int cupoMaximo;
    private int cuposDisponibles;
    private int cupoExcepcionalMaximo;
    private String estado; // ABIERTO, CERRADO, BLOQUEADO

    public Grupo() {
        this.estado = "ABIERTO";
    }

    public Grupo(int id, int idMateria, int idPeriodo, String siglaGrupo, String docente,
                 int cupoMaximo, int cuposDisponibles, int cupoExcepcionalMaximo, String estado) {
        this.id = id;
        this.idMateria = idMateria;
        this.idPeriodo = idPeriodo;
        this.siglaGrupo = siglaGrupo;
        this.docente = docente;
        this.cupoMaximo = cupoMaximo;
        this.cuposDisponibles = cuposDisponibles;
        this.cupoExcepcionalMaximo = cupoExcepcionalMaximo;
        this.estado = estado != null ? estado : "ABIERTO";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(int idMateria) {
        this.idMateria = idMateria;
    }

    public int getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(int idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public String getSiglaGrupo() {
        return siglaGrupo;
    }

    public void setSiglaGrupo(String siglaGrupo) {
        this.siglaGrupo = siglaGrupo;
    }

    public String getDocente() {
        return docente;
    }

    public void setDocente(String docente) {
        this.docente = docente;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public void setCupoMaximo(int cupoMaximo) {
        this.cupoMaximo = cupoMaximo;
    }

    public int getCuposDisponibles() {
        return cuposDisponibles;
    }

    public void setCuposDisponibles(int cuposDisponibles) {
        this.cuposDisponibles = cuposDisponibles;
    }

    public int getCupoExcepcionalMaximo() {
        return cupoExcepcionalMaximo;
    }

    public void setCupoExcepcionalMaximo(int cupoExcepcionalMaximo) {
        this.cupoExcepcionalMaximo = cupoExcepcionalMaximo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
