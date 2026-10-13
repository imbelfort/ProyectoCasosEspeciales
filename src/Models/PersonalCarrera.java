package Models;

public class PersonalCarrera {
    private int idPersonal; // FK Usuario(id_usuario)
    private String cargo;
    private int idCarrera;

    public PersonalCarrera() {
    }

    public PersonalCarrera(int idPersonal, String cargo, int idCarrera) {
        this.idPersonal = idPersonal;
        this.cargo = cargo;
        this.idCarrera = idCarrera;
    }

    public int getIdPersonal() {
        return idPersonal;
    }

    public void setIdPersonal(int idPersonal) {
        this.idPersonal = idPersonal;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public int getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(int idCarrera) {
        this.idCarrera = idCarrera;
    }
}
