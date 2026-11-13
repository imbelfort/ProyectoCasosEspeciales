package Interfaces;

import Comunication.TokenEvent;

public interface ITokenCasoUsoEventListener {

    void usuario(TokenEvent event);

    void rol(TokenEvent event);

    void estudiante(TokenEvent event);

    void personalCarrera(TokenEvent event);

    void directorCarrera(TokenEvent event);

    void carrera(TokenEvent event);

    void periodo(TokenEvent event);

    void materia(TokenEvent event);

    void grupo(TokenEvent event);

    void horarioGrupo(TokenEvent event);

    void solicitud(TokenEvent event);

    void solicitudGrupo(TokenEvent event);

    void archivoSolicitud(TokenEvent event);

    void pago(TokenEvent event);

    void evaluacion(TokenEvent event);

    void bloqueoGrupo(TokenEvent event);

    void oficioCpd(TokenEvent event);

    void respuestaCpd(TokenEvent event);

    void historial(TokenEvent event);

    void notificacion(TokenEvent event);

    void reporte(TokenEvent event);

    void error(TokenEvent event);

    void posibleserrores(TokenEvent event);
}