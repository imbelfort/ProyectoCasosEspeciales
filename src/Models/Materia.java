package Models;

public class Materia {
    private int id;
    private String sigla;
    private String nombre;
    private int nivelSemestre;
    private int creditos;

    public Materia() {
    }

    public Materia(int id, String sigla, String nombre, int nivelSemestre, int creditos) {
        this.id = id;
        this.sigla = sigla;
        this.nombre = nombre;
        this.nivelSemestre = nivelSemestre;
        this.creditos = creditos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSigla() {
        return sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getNivelSemestre() {
        return nivelSemestre;
    }

    public void setNivelSemestre(int nivelSemestre) {
        this.nivelSemestre = nivelSemestre;
    }

    public int getCreditos() {
        return creditos;
    }

    public void setCreditos(int creditos) {
        this.creditos = creditos;
    }
}
