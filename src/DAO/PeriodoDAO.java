package DAO;

import Database.Conexion;
import Models.Periodo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PeriodoDAO {

    public void create(Periodo periodo) throws SQLException {
        String sql = "INSERT INTO periodos (gestion, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, periodo.getGestion());
            ps.setDate(2, periodo.getFechaInicio());
            ps.setDate(3, periodo.getFechaFin());
            ps.setString(4, periodo.getEstado());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    periodo.setId(rs.getInt(1));
                }
            }
        }
    }

    public List<String[]> listAll() throws SQLException {
        String sql = "SELECT id, gestion, fecha_inicio, fecha_fin, estado FROM periodos ORDER BY id DESC";
        List<String[]> lista = new ArrayList<>();
        try (Connection con = Conexion.getInstancia().EstablecerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new String[]{
                        String.valueOf(rs.getInt("id")),
                        rs.getString("gestion") != null ? rs.getString("gestion") : "",
                        rs.getDate("fecha_inicio") != null ? rs.getDate("fecha_inicio").toString() : "",
                        rs.getDate("fecha_fin") != null ? rs.getDate("fecha_fin").toString() : "",
                        rs.getString("estado") != null ? rs.getString("estado") : ""
                });
            }
        }
        return lista;
    }
}
