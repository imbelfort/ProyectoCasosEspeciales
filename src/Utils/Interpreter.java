package Utils;

import Interfaces.ITokenCasoUsoEventListener;
import Comunication.TokenEvent;

public class Interpreter implements Runnable {

    private ITokenCasoUsoEventListener listener;
    private Analex analex;

    private String command;
    private String sender;

    public Interpreter(String command, String sender) {
        this.command = command;
        this.sender = sender;
    }

    public ITokenCasoUsoEventListener getListener() {
        return listener;
    }

    public void setListener(ITokenCasoUsoEventListener listener) {
        this.listener = listener;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    private void filterEvent(TokenCommand tokenCommand) {
        TokenEvent tokenEvent = new TokenEvent(this, sender);
        tokenEvent.setAction(tokenCommand.getAction());

        int countParams = tokenCommand.countParams();

        for (int i = 0; i < countParams; i++) {
            int pos = tokenCommand.getParams(i);
            tokenEvent.addParams(analex.getParam(pos));
        }

        switch (tokenCommand.getName()) {
            case Token.USUARIO:
                listener.usuario(tokenEvent);
                break;
            case Token.ROL:
                listener.rol(tokenEvent);
                break;
            case Token.ESTUDIANTE:
                listener.estudiante(tokenEvent);
                break;
            case Token.PERSONAL_CARRERA:
                listener.personalCarrera(tokenEvent);
                break;
            case Token.DIRECTOR_CARRERA:
                listener.directorCarrera(tokenEvent);
                break;
            case Token.CARRERA:
                listener.carrera(tokenEvent);
                break;
            case Token.PERIODO:
                listener.periodo(tokenEvent);
                break;
            case Token.MATERIA:
                listener.materia(tokenEvent);
                break;
            case Token.GRUPO:
                listener.grupo(tokenEvent);
                break;
            case Token.HORARIO_GRUPO:
                listener.horarioGrupo(tokenEvent);
                break;
            case Token.SOLICITUD:
                listener.solicitud(tokenEvent);
                break;
            case Token.SOLICITUD_GRUPO:
                listener.solicitudGrupo(tokenEvent);
                break;
            case Token.ARCHIVO_SOLICITUD:
                listener.archivoSolicitud(tokenEvent);
                break;
            case Token.EVALUACION:
                listener.evaluacion(tokenEvent);
                break;
            case Token.BLOQUEO_GRUPO:
                listener.bloqueoGrupo(tokenEvent);
                break;
            case Token.OFICIO_CPD:
                listener.oficioCpd(tokenEvent);
                break;
            case Token.RESPUESTA_CPD:
                listener.respuestaCpd(tokenEvent);
                break;
            case Token.HISTORIAL:
                listener.historial(tokenEvent);
                break;
            case Token.NOTIFICACION:
                listener.notificacion(tokenEvent);
                break;
            case Token.REPORTE:
                listener.reporte(tokenEvent);
                break;
            default:
                listener.error(tokenEvent);
                break;
        }
    }

    private void tokenError(Token token, String lexeme) {
        TokenEvent tokenEvent = new TokenEvent(this, sender);
        tokenEvent.setAction(token.getAttribute());
        tokenEvent.addParams(lexeme);
        listener.error(tokenEvent);
    }

    @Override
    public void run() {
        analex = new Analex(stripAttachmentMarkers(command));
        TokenCommand tokenCommand = new TokenCommand();
        Token token;

        while ((token = analex.Preanalisis()).getName() != Token.END && token.getName() != Token.ERROR) {
            if (token.getName() == Token.CU) {
                tokenCommand.setName(token.getAttribute());
            } else if (token.getName() == Token.ACTION) {
                tokenCommand.setAction(token.getAttribute());
            } else if (token.getName() == Token.PARAMS) {
                tokenCommand.addParams(token.getAttribute());
            }
            analex.next();
        }

        if (token.getName() == Token.END) {
            filterEvent(tokenCommand);
        } else if (token.getName() == Token.ERROR) {
            tokenError(token, analex.lexeme());
        }
    }

        private String stripAttachmentMarkers(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        return input.replace("[", " ")
                .replace("]", " ")
                .replace("{", " ")
                .replace("}", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}