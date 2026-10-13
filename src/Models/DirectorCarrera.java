package Models;

import java.sql.Date;

public class DirectorCarrera {
    private int idDirector; // FK Usuario(id_usuario)
    private int idCarrera;
    private Date gestionInicio;
    private Date gestionFin;

    public DirectorCarrera() {
    }

    public DirectorCarrera(int idDirector, int idCarrera, Date gestionInicio, Date gestionFin) {
        this.idDirector = idDirector;
        this.idCarrera = idCarrera;
        this.gestionInicio = gestionInicio;
        this.gestionFin = gestionFin;
    }

    public int getIdDirector() {
        return idDirector;
    }

    public void setIdDirector(int idDirector) {
        this.idDirector = idDirector;
    }

    public int getIdCarrera() {
        return idCarrera;
    }

    public void setIdCarrera(int idCarrera) {
        this.idCarrera = idCarrera;
    }

    public Date getGestionInicio() {
        return gestionInicio;
    }

    public void setGestionInicio(Date gestionInicio) {
        this.gestionInicio = gestionInicio;
    }

    public Date getGestionFin() {
        return gestionFin;
    }

    public void setGestionFin(Date gestionFin) {
        this.gestionFin = gestionFin;
    }
}
