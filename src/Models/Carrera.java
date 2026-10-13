package Models;

public class Carrera {
    private int id;
    private String codigo;
    private String nombre;
    private String facultad;

    public Carrera() {
    }

    public Carrera(int id, String codigo, String nombre, String facultad) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.facultad = facultad;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFacultad() {
        return facultad;
    }

    public void setFacultad(String facultad) {
        this.facultad = facultad;
    }
}
