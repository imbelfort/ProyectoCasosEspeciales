package Models;

public class HorarioGrupo {
    private int id;
    private int idGrupo;
    private String dia; // LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO
    private String horaInicio; // "07:00"
    private String horaFin; // "09:15"
    private String aula;

    public HorarioGrupo() {
    }

    public HorarioGrupo(int id, int idGrupo, String dia, String horaInicio, String horaFin, String aula) {
        this.id = id;
        this.idGrupo = idGrupo;
        this.dia = dia;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.aula = aula;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }

    public String getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }

    public String getAula() {
        return aula;
    }

    public void setAula(String aula) {
        this.aula = aula;
    }
}
