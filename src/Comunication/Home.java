package Comunication;

import Interfaces.IEmailEventListener;
import Interfaces.ITokenCasoUsoEventListener;
import Models.Solicitud;
import Services.*;
import Utils.Email;
import Utils.Interpreter;
import Utils.Token;
import proyectomailgrupo01sa.Principal;

import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javax.swing.SwingUtilities;

public class Home {

    public static void iniciar() {
        MailVerificationThread mail = new MailVerificationThread();
        mail.setEmailEventListener(new IEmailEventListener() {
            @Override
            public void onReceiveEmailEvent(List<Email> emails) {
                for (Email email : emails) {
                    interprete(email);
                }
            }
        });
        Thread thread = new Thread(mail);
        thread.setName("Mail Verification Thread");
        thread.start();
    }

    public static void interprete(Email email) {
        SendEmail respuesta = new SendEmail();

        // Inicialización de Servicios para los 8 Casos de Uso
        final UsuarioService usuarioService = new UsuarioService();
        final EstudianteService estudianteService = new EstudianteService();
        final PersonalCarreraService personalCarreraService = new PersonalCarreraService();
        final DirectorCarreraService directorCarreraService = new DirectorCarreraService();
        final CarreraService carreraService = new CarreraService();
        final PeriodoService periodoService = new PeriodoService();
        final MateriaService materiaService = new MateriaService();
        final GrupoService grupoService = new GrupoService();
        final SolicitudService solicitudService = new SolicitudService();
        final ArchivoSolicitudService archivoSolicitudService = new ArchivoSolicitudService();
        final PagoService pagoService = new PagoService();
        final EvaluacionService evaluacionService = new EvaluacionService();
        final BloqueoGrupoService bloqueoGrupoService = new BloqueoGrupoService();
        final OficioCpdService oficioCpdService = new OficioCpdService();
        final RespuestaCpdService respuestaCpdService = new RespuestaCpdService();
        final HistorialService historialService = new HistorialService();
        final NotificacionService notificacionService = new NotificacionService();
        final ReporteService reporteService = new ReporteService();

        String subjectText = normalizarAliasCu2(
                normalizarAsuntoComando(email.getSubject()));
        String asuntoNormalizado = subjectText.toLowerCase(java.util.Locale.ROOT);

        if ("help".equals(asuntoNormalizado)
                || "ayuda".equals(asuntoNormalizado)
                || asuntoNormalizado.equals("acceder ayuda")) {
            String resultado = new ControlAccesoComandos().procesar(
                    "acceder ayuda",
                    email.getFrom(),
                    email.getAttachmentFileName(),
                    null);
            enviarRespuestaControlAcceso(email, respuesta, resultado);
            return;
        }

        if (asuntoNormalizado.startsWith("acceder ")
                || asuntoNormalizado.startsWith("estudiante registrar ")
                || asuntoNormalizado.startsWith("estudiante register ")
                || asuntoNormalizado.startsWith("token ")
                || esComandoCu1(asuntoNormalizado)) {
            String resultado = new ControlAccesoComandos().procesar(
                    subjectText,
                    email.getFrom(),
                    email.getAttachmentFileName(),
                    null);
            mostrarResultadoAccesoEnTerminal(subjectText, resultado);
            enviarRespuestaControlAcceso(email, respuesta, resultado);
            return;
        }

        final AccesoService.Contexto contextoComando;
        if (!subjectText.isBlank()) {
            try {
                AccesoService accesoService = new AccesoService();
                contextoComando =
                        accesoService.validarPorRemitente(email.getFrom());
                String[] partes = subjectText.split("\\s+", 3);
                if (partes.length < 2) {
                    throw new IllegalArgumentException(
                            "El comando esta incompleto. Envia 'help' para ver opciones.");
                }
                accesoService.autorizar(
                        contextoComando,
                        partes[0],
                        partes[1]);
            } catch (SecurityException ex) {
                registrarErrorComando(subjectText, ex.getMessage());
                enviarRespuestaControlAcceso(
                        email, respuesta, "ACCESO DENEGADO:\n" + ex.getMessage());
                return;
            } catch (IllegalArgumentException ex) {
                registrarErrorComando(subjectText, ex.getMessage());
                enviarRespuestaControlAcceso(
                        email, respuesta, "ERROR DE VALIDACION:\n" + ex.getMessage());
                return;
            } catch (SQLException ex) {
                registrarErrorComando(subjectText, ex.getMessage());
                System.err.println("Error al validar la sesión de "
                        + email.getFrom() + ":");
                ex.printStackTrace(System.err);
                enviarRespuestaControlAcceso(
                        email,
                        respuesta,
                        "ERROR DEL SISTEMA:\nNo se pudo validar la sesión " +
                        "contra la base de datos."
                        + (ex.getSQLState() == null
                                ? ""
                                : " Código: " + ex.getSQLState() + "."));
                return;
            }
        } else {
            enviarRespuestaControlAcceso(
                    email,
                    respuesta,
                    "ERROR DE VALIDACION:\nEl asunto del correo esta vacio. " +
                    "Envia 'help' para consultar los comandos disponibles.");
            return;
        }

        Interpreter interpreter = new Interpreter(subjectText, email.getFrom());
        interpreter.setListener(new ITokenCasoUsoEventListener() {

            // ============================================================
            // CU1: GESTIÓN DE USUARIOS, ROLES, ESTUDIANTES, PERSONAL, DIRECTOR
            // ============================================================

            @Override
            public void usuario(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = usuarioService.listarUsuarios();
                            enviarTabla(email, respuesta, "Lista de Usuarios", HtmlBuilder.HEADERS_USER, list);
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "usuario", "listar");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            @Override
            public void rol(TokenEvent event) {
                comandoDesconocido(
                        email, respuesta, "rol",
                        "los roles se asignan automaticamente al crear perfiles");
            }

            @Override
            public void estudiante(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = estudianteService.listarEstudiantes();
                            enviarTabla(email, respuesta, "Lista de Estudiantes", HtmlBuilder.HEADERS_ESTUDIANTE, list);
                            break;
                        case Token.AGREGAR:
                            estudianteService.registrarEstudiante(event.getParams());
                            List<String[]> listAdd = estudianteService.listarEstudiantes();
                            enviarTabla(email, respuesta, "Estudiante registrado exitosamente", HtmlBuilder.HEADERS_ESTUDIANTE, listAdd);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, estudianteService.getComandosEstudiante());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "estudiante", "listar, agregar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            @Override
            public void personalCarrera(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = personalCarreraService.listarPersonal();
                            enviarTabla(email, respuesta, "Lista de Personal de Carrera", HtmlBuilder.HEADERS_PERSONAL_CARRERA, list);
                            break;
                        case Token.AGREGAR:
                            personalCarreraService.registrarPersonal(event.getParams());
                            List<String[]> listAdd = personalCarreraService.listarPersonal();
                            enviarTabla(email, respuesta, "Personal de carrera registrado", HtmlBuilder.HEADERS_PERSONAL_CARRERA, listAdd);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, personalCarreraService.getComandosPersonal());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "personal_carrera", "listar, agregar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            @Override
            public void directorCarrera(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = directorCarreraService.listarDirectores();
                            enviarTabla(email, respuesta, "Lista de Directores de Carrera", HtmlBuilder.HEADERS_DIRECTOR_CARRERA, list);
                            break;
                        case Token.AGREGAR:
                            directorCarreraService.registrarDirector(event.getParams());
                            List<String[]> listAdd = directorCarreraService.listarDirectores();
                            enviarTabla(email, respuesta, "Director de carrera registrado", HtmlBuilder.HEADERS_DIRECTOR_CARRERA, listAdd);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, directorCarreraService.getComandosDirector());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "director_carrera", "listar, agregar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            // ============================================================
            // CU2: REGISTRO Y ENVÍO DE SOLICITUD DE CASO ESPECIAL
            // ============================================================

            @Override
            public void solicitud(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            if (event.getParams().isEmpty()) {
                                throw new IllegalArgumentException(
                                        "Formato: solicitud listar período (ejemplo: solicitud listar 2-2026).");
                            }
                            String periodoListado = String.join(" ", event.getParams()).trim();
                            List<String[]> list =
                                    solicitudService.listarSolicitudesPorGestion(periodoListado);
                            imprimirTablaConsola(
                                    "CU3 - Solicitudes de Casos Especiales del período " + periodoListado,
                                    HtmlBuilder.HEADERS_SOLICITUD, list);
                            enviarTabla(email, respuesta,
                                    "Solicitudes de Casos Especiales del período " + periodoListado,
                                    HtmlBuilder.HEADERS_SOLICITUD, list);
                            break;
                        case Token.PERIODOS:
                            if (event.getParams().isEmpty()) {
                                throw new IllegalArgumentException(
                                        "Formato: solicitud periodos id_periodo_o_gestion (ejemplo: solicitud periodos 2-2026).");
                            }
                            String periodo = String.join(" ", event.getParams()).trim();
                            List<String[]> solicitudesPeriodo =
                                    solicitudService.listarSolicitudesPorPeriodo(periodo);
                            System.out.println("[CU3 - CONSULTA POR PERIODO] "
                                    + solicitudesPeriodo.size() + " solicitud(es) encontradas "
                                    + "para el período " + periodo + ".");
                            imprimirTablaConsola(
                                    "Solicitudes del período " + periodo,
                                    HtmlBuilder.HEADERS_SOLICITUD, solicitudesPeriodo);
                            enviarTabla(email, respuesta,
                                    "Solicitudes del período " + periodo,
                                    HtmlBuilder.HEADERS_SOLICITUD, solicitudesPeriodo);
                            break;
                        case Token.MIS_CASOS:
                            List<String[]> misCasos = solicitudService.listarMisCasos(email.getFrom(), event.getParams());
                            enviarTabla(email, respuesta, "Mis Solicitudes de Casos Especiales", HtmlBuilder.HEADERS_SOLICITUD, misCasos);
                            break;
                        case Token.PENDIENTES:
                            if (!event.getParams().isEmpty()
                                    && "listar".equalsIgnoreCase(event.getParams().get(0).trim())) {
                                if (event.getParams().size() < 2) {
                                    throw new IllegalArgumentException(
                                            "Formato: solicitud pendientes listar,período "
                                            + "(ejemplo: solicitud pendientes listar,2-2026).");
                                }
                                String periodoPendientes = String.join(" ",
                                        event.getParams().subList(1, event.getParams().size())).trim();
                                List<String[]> pendientesPeriodo =
                                        solicitudService.listarSolicitudesPendientesPorPeriodo(
                                                periodoPendientes);
                                enviarTabla(email, respuesta,
                                        "Solicitudes pendientes del período " + periodoPendientes,
                                        HtmlBuilder.HEADERS_SOLICITUD, pendientesPeriodo);
                                break;
                            }
                            List<String[]> misPendientes = solicitudService.listarMisPendientes(email.getFrom(), event.getParams());
                            enviarTabla(email, respuesta, "Mis Solicitudes Pendientes", HtmlBuilder.HEADERS_SOLICITUD, misPendientes);
                            break;
                        case Token.REGISTRAR:
                        case Token.AGREGAR:
                            Solicitud sol = solicitudService.registrarSolicitud(event.getParams(), email.getFrom());
                            System.out.println("CU2 - Solicitud enviada exitosamente. Código: "
                                    + sol.getCodigo() + ", ID: " + sol.getId()
                                    + ", estado: " + sol.getEstado() + ".");
                            List<String[]> listAdd = solicitudService.listarMisCasos(
                                    email.getFrom(), java.util.Collections.emptyList());
                            enviarTabla(email, respuesta, "Solicitud " + sol.getId()
                                    + " registrada con codigo " + sol.getCodigo(),
                                    HtmlBuilder.HEADERS_SOLICITUD, listAdd);
                            break;
                        case Token.CONSULTAR:
                            if (event.getParams().isEmpty()) throw new IllegalArgumentException("Formato: solicitud consultar id_solicitud");
                            int id = Integer.parseInt(event.getParams().get(0).trim());
                            AccesoService.Contexto contextoDetalle =
                                    new AccesoService().validarPorRemitente(email.getFrom());
                            String[] solInfo = contextoDetalle.tieneRol("ESTUDIANTE")
                                    ? solicitudService.consultarSolicitudPropia(
                                            id, email.getFrom())
                                    : solicitudService.consultarSolicitud(id);
                            if (solInfo == null) {
                                throw new IllegalArgumentException(
                                        "No existe una solicitud con ID " + id + ".");
                            } else {
                                List<String[]> grupos =
                                        solicitudService.listarGruposDeSolicitud(id);
                                List<String[]> documentos =
                                        archivoSolicitudService.listarArchivosDeSolicitud(id);
                                List<String[]> evaluaciones =
                                        evaluacionService.listarEvaluacionesSolicitud(id);
                                System.out.println("[CU3 - DETALLE DE SOLICITUD] "
                                        + "Expediente " + solInfo[1] + " consultado. "
                                        + grupos.size() + " grupo(s), "
                                        + documentos.size() + " documento(s), "
                                        + evaluaciones.size() + " evaluación(es).");
                                imprimirDetalleConsola(
                                        solInfo, grupos, documentos, evaluaciones);
                                String html = HtmlBuilder.generarDetalleSolicitud(
                                        solInfo, grupos, documentos, evaluaciones);
                                mostraRespuesta(html);
                                if (email.getFrom() != null
                                        && !email.getFrom().trim().isEmpty()) {
                                    boolean puedeDescargar = contextoDetalle.tieneRol("DIRECTOR")
                                            || contextoDetalle.tieneRol("PERSONAL_ACADEMICO");
                                    List<File> archivos = puedeDescargar
                                            ? archivoSolicitudService.obtenerArchivosDescargables(id)
                                            : Collections.<File>emptyList();
                                    if (archivos.isEmpty()) {
                                        respuesta.sendmail(
                                                email.getFrom(),
                                                "Detalle de Solicitud " + solInfo[1],
                                                html);
                                    } else {
                                        respuesta.sendmailWithAttachments(
                                                email.getFrom(),
                                                "Detalle de Solicitud " + solInfo[1],
                                                html,
                                                archivos);
                                    }
                                }
                            }
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, solicitudService.getComandosSolicitud());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "solicitud", "listar, mis_casos, pendientes, registrar, consultar, help");
                    }
                } catch (Exception ex) {
                    String contexto = event.getAction() == Token.REGISTRAR
                            || event.getAction() == Token.AGREGAR
                            ? "CU2 - Error al enviar la solicitud" : null;
                    if (contexto == null && (event.getAction() == Token.PERIODOS
                            || event.getAction() == Token.CONSULTAR
                            || event.getAction() == Token.LISTAR)) {
                        contexto = "CU3 - Error al consultar solicitudes";
                    }
                    manejarExcepcion(email, respuesta, ex, contexto);
                }
            }

            @Override
            public void solicitudGrupo(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.AGREGAR:
                            solicitudService.agregarGrupoASolicitud(
                                    event.getParams(), email.getFrom());
                            int idSol = Integer.parseInt(event.getParams().get(0).trim());
                            if (event.getParams().size() >= 5) {
                                System.out.println("CU2 - Materia/grupo agregado exitosamente. "
                                        + "Solicitud ID: " + idSol + ", materia: "
                                        + event.getParams().get(2).trim() + ", grupo: "
                                        + event.getParams().get(3).trim() + ".");
                            } else {
                                System.out.println("CU2 - Materia/grupo agregado exitosamente. "
                                        + "Solicitud ID: " + idSol + ", grupo ID: "
                                        + event.getParams().get(1).trim() + ".");
                            }
                            List<String[]> list = solicitudService.listarGruposDeSolicitud(idSol);
                            enviarTabla(email, respuesta, "Grupo agregado a la Solicitud ID " + idSol, HtmlBuilder.HEADERS_SOLICITUD_GRUPO, list);
                            break;
                        case Token.LISTAR:
                            if (event.getParams().isEmpty()) throw new IllegalArgumentException("Formato: solicitud_grupo listar id_solicitud");
                            int idS = Integer.parseInt(event.getParams().get(0).trim());
                            List<String[]> listG = solicitudService.listarGruposDeSolicitud(idS);
                            enviarTabla(email, respuesta, "Grupos relacionados a la Solicitud ID " + idS, HtmlBuilder.HEADERS_SOLICITUD_GRUPO, listG);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, "Comandos Solicitud-Grupo:\n- solicitud_grupo listar id_solicitud\n- solicitud_grupo agregar id_solicitud,periodo,sigla_materia,sigla_grupo,motivo");
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "solicitud_grupo", "agregar, listar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(
                            email,
                            respuesta,
                            ex,
                            event.getAction() == Token.AGREGAR
                                    ? "CU2 - Error al agregar materia/grupo" : null);
                }
            }

            @Override
            public void archivoSolicitud(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.AGREGAR:
                            archivoSolicitudService.adjuntarArchivos(
                                    event.getParams(), email.getFrom(), email.getAttachments());
                            int idSol = Integer.parseInt(event.getParams().get(0).trim());
                            int cantidadAdjuntos = email.getAttachments() == null
                                    ? 0 : email.getAttachments().size();
                            if (cantidadAdjuntos == 0) {
                                cantidadAdjuntos = 1;
                            }

                            System.out.println("CU2 - Documento(s) adjuntado(s) "
                                    + "exitosamente. Solicitud ID: " + idSol
                                    + ", cantidad: " + cantidadAdjuntos + ".");
                            List<String[]> list = archivoSolicitudService.listarArchivosDeSolicitud(idSol);
                            enviarTabla(email, respuesta, "Documento(s) adjuntado(s) a la Solicitud ID " + idSol, HtmlBuilder.HEADERS_ARCHIVO_SOLICITUD, list);
                            break;
                        case Token.LISTAR:
                            if (event.getParams().isEmpty()) throw new IllegalArgumentException("Formato: archivo_solicitud listar id_solicitud");
                            int idS = Integer.parseInt(event.getParams().get(0).trim());
                            List<String[]> listA = archivoSolicitudService.listarArchivosDeSolicitud(idS);
                            enviarTabla(email, respuesta, "Archivos de respaldo de la Solicitud ID " + idS, HtmlBuilder.HEADERS_ARCHIVO_SOLICITUD, listA);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, archivoSolicitudService.getComandosArchivo());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "archivo_solicitud", "agregar, listar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(
                            email,
                            respuesta,
                            ex,
                            event.getAction() == Token.AGREGAR
                                    ? "CU2 - Error al adjuntar documento(s)" : null);
                }
            }

            @Override
            public void pago(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.AGREGAR:
                            pagoService.adjuntarPago(
                                    event.getParams(),
                                    email.getFrom(),
                                    email.getAttachments());
                            int idSolicitudPago = Integer.parseInt(
                                    event.getParams().get(0).trim());
                            enviarTabla(
                                    email,
                                    respuesta,
                                    "Pago registrado para la Solicitud ID "
                                            + idSolicitudPago,
                                    HtmlBuilder.HEADERS_PAGO,
                                    pagoService.listarMisPagos(email.getFrom()));
                            break;
                        case Token.LISTAR:
                            enviarTabla(
                                    email,
                                    respuesta,
                                    "Mis pagos",
                                    HtmlBuilder.HEADERS_PAGO,
                                    pagoService.listarMisPagos(email.getFrom()));
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(
                                    email, respuesta, pagoService.getComandosPago());
                            break;
                        default:
                            comandoDesconocido(
                                    email, respuesta, "pago", "agregar, listar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(
                            email, respuesta, ex,
                            event.getAction() == Token.AGREGAR
                                    ? "CU3 - Error al registrar el pago" : null);
                }
            }

            // ============================================================
            // CU3: EVALUACIÓN DE SOLICITUD POR DIRECCIÓN DE CARRERA
            // ============================================================

            @Override
            public void evaluacion(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = evaluacionService.listarEvaluaciones();
                            imprimirTablaConsola(
                                    "CU3 - Evaluaciones de Dirección de Carrera",
                                    HtmlBuilder.HEADERS_EVALUACION, list);
                            enviarTabla(email, respuesta, "Evaluaciones de Dirección de Carrera", HtmlBuilder.HEADERS_EVALUACION, list);
                            break;
                        case Token.REGISTRAR:
                        case Token.EVALUAR:
                            EvaluacionService.ResultadoEvaluacion evaluacion =
                                    evaluacionService.evaluarSolicitud(
                                    event.getParams(), contextoComando.idUsuario());
                            String htmlNotificacion = HtmlBuilder.generarResultadoEvaluacion(
                                    evaluacion.idSolicitud(),
                                    evaluacion.codigoSolicitud(),
                                    evaluacion.decision(),
                                    evaluacion.nuevoEstado(),
                                    evaluacion.motivo());
                            boolean notificacionEnviada = new SendEmail().sendmailDirect(
                                    evaluacion.correoEstudiante(),
                                    "Resultado de evaluación - Solicitud "
                                            + evaluacion.codigoSolicitud(),
                                    htmlNotificacion);
                            String errorEnvio = notificacionEnviada
                                    ? null : "El servidor SMTP no aceptó la notificación.";
                            boolean estadoNotificacionGuardado = true;
                            try {
                                evaluacionService.registrarResultadoNotificacion(
                                        evaluacion.idSolicitud(),
                                        notificacionEnviada,
                                        errorEnvio);
                            } catch (SQLException ex) {
                                estadoNotificacionGuardado = false;
                                System.err.println(
                                        "[CU3] La evaluación fue registrada, pero no se pudo "
                                        + "actualizar el estado de envío de la notificación: "
                                        + ex.getMessage());
                            }
                            String mensaje = "La solicitud " + evaluacion.codigoSolicitud()
                                    + " fue " + evaluacion.decision().toLowerCase(
                                            java.util.Locale.ROOT)
                                    + ". Estado registrado: " + evaluacion.nuevoEstado()
                                    + (notificacionEnviada
                                            ? ". Notificación enviada al estudiante."
                                            : ". No se pudo enviar la notificación al estudiante.");
                            if (!estadoNotificacionGuardado) {
                                mensaje += " No se pudo guardar el resultado del envío en la base de datos.";
                            }
                            System.out.println("[CU3 - EVALUACION REGISTRADA] " + mensaje);
                            enviarExito(email, respuesta, "Evaluación registrada exitosamente", mensaje);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, evaluacionService.getComandosEvaluacion());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "evaluacion", "listar, registrar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(
                            email, respuesta, ex,
                            "CU3 - Error al registrar/consultar evaluación");
                }
            }

            // ============================================================
            // CU4: BLOQUEO Y CONTROL DE CUPOS DE MATERIAS/PARALELOS
            // ============================================================

            @Override
            public void carrera(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = carreraService.listarCarreras();
                            enviarTabla(email, respuesta, "Catálogo de Carreras", HtmlBuilder.HEADERS_CARRERA, list);
                            break;
                        case Token.AGREGAR:
                            carreraService.crearCarrera(event.getParams());
                            List<String[]> listAdd = carreraService.listarCarreras();
                            enviarTabla(email, respuesta, "Carrera agregada", HtmlBuilder.HEADERS_CARRERA, listAdd);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, carreraService.getComandosCarrera());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "carrera", "listar, agregar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            @Override
            public void periodo(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = periodoService.listarPeriodos();
                            enviarTabla(email, respuesta, "Periodos Académicos", HtmlBuilder.HEADERS_PERIODO, list);
                            break;
                        case Token.AGREGAR:
                            periodoService.crearPeriodo(event.getParams());
                            List<String[]> listAdd = periodoService.listarPeriodos();
                            enviarTabla(email, respuesta, "Periodo agregado", HtmlBuilder.HEADERS_PERIODO, listAdd);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, periodoService.getComandosPeriodo());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "periodo", "listar, agregar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            @Override
            public void materia(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = materiaService.listarMaterias();
                            enviarTabla(email, respuesta, "Catálogo de Materias", HtmlBuilder.HEADERS_MATERIA, list);
                            break;
                        case Token.AGREGAR:
                            materiaService.crearMateria(event.getParams());
                            List<String[]> listAdd = materiaService.listarMaterias();
                            enviarTabla(email, respuesta, "Materia agregada", HtmlBuilder.HEADERS_MATERIA, listAdd);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, materiaService.getComandosMateria());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "materia", "listar, agregar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            @Override
            public void grupo(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = grupoService.listarGrupos();
                            enviarTabla(email, respuesta, "Grupos y Disponibilidad de Cupos", HtmlBuilder.HEADERS_GRUPO, list);
                            break;
                        case Token.AGREGAR:
                            grupoService.crearGrupo(event.getParams());
                            List<String[]> listAdd = grupoService.listarGrupos();
                            enviarTabla(email, respuesta, "Grupo agregado exitosamente", HtmlBuilder.HEADERS_GRUPO, listAdd);
                            break;
                        case Token.CONSULTAR:
                            if (event.getParams().isEmpty()) throw new IllegalArgumentException("Formato: grupo consultar id_grupo");
                            int idG = Integer.parseInt(event.getParams().get(0).trim());
                            String[] gInfo = grupoService.consultarCupo(idG);
                            if (gInfo == null) {
                                enviarErrorDetallado(email, respuesta, "Consulta Grupo", "No existe ningún grupo con el ID: " + idG, "Envíe 'grupo listar' para ver los grupos existentes.");
                            } else {
                                java.util.ArrayList<String[]> gList = new java.util.ArrayList<String[]>();
                                gList.add(gInfo);
                                enviarTabla(email, respuesta, "Disponibilidad de Grupo " + gInfo[3], HtmlBuilder.HEADERS_GRUPO, gList);
                            }
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, grupoService.getComandosGrupo());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "grupo", "listar, consultar, agregar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            @Override
            public void horarioGrupo(TokenEvent event) {
                mostrarYEnviarHELP(email, respuesta, "Comando horario_grupo");
            }

            @Override
            public void bloqueoGrupo(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = bloqueoGrupoService.listarBloqueos();
                            imprimirTablaConsola(
                                    "CU4 - Control y Bloqueos de Cupos",
                                    HtmlBuilder.HEADERS_BLOQUEO_GRUPO, list);
                            enviarTabla(email, respuesta, "Control y Bloqueos de Cupos", HtmlBuilder.HEADERS_BLOQUEO_GRUPO, list);
                            break;
                        case Token.BLOQUEAR:
                        case Token.AGREGAR:
                            if (!contextoComando.esAdministrativo()) {
                                throw new SecurityException(
                                        "Solo el Director o el Personal Académico puede bloquear cupos.");
                            }
                            Models.BloqueoGrupo bloqueo =
                                    bloqueoGrupoService.bloquearCupo(
                                            event.getParams(), contextoComando.idUsuario());
                            String mensajeBloqueo = "Bloqueo #" + bloqueo.getId()
                                    + " registrado. Se reservaron "
                                    + bloqueo.getCantidad() + " cupo(s) del grupo "
                                    + bloqueo.getIdGrupo() + " para la solicitud "
                                    + bloqueo.getIdSolicitud() + ".";
                            System.out.println("[CU4 - BLOQUEO EXITOSO] " + mensajeBloqueo);
                            enviarExito(
                                    email, respuesta, "Cupo bloqueado exitosamente",
                                    mensajeBloqueo);
                            break;
                        case Token.LIBERAR:
                            if (!contextoComando.esAdministrativo()) {
                                throw new SecurityException(
                                        "Solo el Director o el Personal Académico puede liberar cupos.");
                            }
                            Models.BloqueoGrupo liberado =
                                    bloqueoGrupoService.liberarCupo(
                                            event.getParams(), contextoComando.idUsuario());
                            String mensajeLiberacion = "Bloqueo #"
                                    + liberado.getId() + " liberado. Se reintegraron "
                                    + liberado.getCantidad() + " cupo(s) al grupo "
                                    + liberado.getIdGrupo() + ".";
                            System.out.println("[CU4 - LIBERACIÓN EXITOSA] "
                                    + mensajeLiberacion);
                            enviarExito(
                                    email, respuesta, "Cupo liberado exitosamente",
                                    mensajeLiberacion);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, bloqueoGrupoService.getComandosBloqueo());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "bloqueo_grupo", "listar, bloquear, liberar, help");
                    }
                } catch (Exception ex) {
                    String contexto = event.getAction() == Token.BLOQUEAR
                            || event.getAction() == Token.AGREGAR
                            ? "CU4 - Error al bloquear cupo"
                            : event.getAction() == Token.LIBERAR
                                    ? "CU4 - Error al liberar cupo"
                                    : "CU4 - Error al consultar bloqueos";
                    manejarExcepcion(email, respuesta, ex, contexto);
                }
            }

            // ============================================================
            // CU5: PROCESAMIENTO Y REMISIÓN DE CASOS ESPECIALES A CPD
            // ============================================================

            @Override
            public void oficioCpd(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = contextoComando.tieneRol("CPD")
                                    ? respuestaCpdService.listarOficiosRemitidos()
                                    : oficioCpdService.listarOficios();
                            imprimirTablaConsola(
                                    "CU5 - Oficios remitidos y pendientes a CPD",
                                    HtmlBuilder.HEADERS_OFICIO_CPD, list);
                            enviarTabla(email, respuesta, "Oficios Remitidos a CPD", HtmlBuilder.HEADERS_OFICIO_CPD, list);
                            break;
                        case Token.CONSULTAR:
                            if (event.getParams().size() != 1) {
                                throw new IllegalArgumentException(
                                        "Formato: oficio_cpd consultar id_oficio");
                            }
                            int idOficioConsulta;
                            try {
                                idOficioConsulta = Integer.parseInt(
                                        event.getParams().get(0).trim());
                            } catch (NumberFormatException ex) {
                                throw new IllegalArgumentException(
                                        "El ID del oficio debe ser un número entero.", ex);
                            }
                            List<String[]> solicitudesOficio =
                                    respuestaCpdService.listarSolicitudesDeOficio(
                                            idOficioConsulta);
                            imprimirTablaConsola(
                                    "CU6 - Solicitudes incluidas en el oficio "
                                            + idOficioConsulta,
                                    HtmlBuilder.HEADERS_SOLICITUDES_OFICIO_CPD,
                                    solicitudesOficio);
                            String htmlOficio = HtmlBuilder.generarTabla(
                                    "Solicitudes del oficio " + idOficioConsulta,
                                    HtmlBuilder.HEADERS_SOLICITUDES_OFICIO_CPD,
                                    solicitudesOficio);
                            mostraRespuesta(htmlOficio);
                            if (email.getFrom() != null
                                    && !email.getFrom().trim().isEmpty()) {
                                respuesta.sendmailWithAttachments(
                                        email.getFrom(),
                                        "Oficio CPD " + idOficioConsulta
                                                + " y documentos adjuntos",
                                        htmlOficio,
                                        respuestaCpdService
                                                .obtenerArchivosDescargablesOficio(
                                                        idOficioConsulta));
                            }
                            break;
                        case Token.REMITIR:
                            OficioCpdService.ResultadoRemision remision =
                                    oficioCpdService.remitirACpd(
                                            event.getParams(), contextoComando.idUsuario());
                            System.out.println(
                                    (remision.enviado()
                                            ? "[CU5 - REMISIÓN EXITOSA] "
                                            : "[CU5 - ERROR DE ENVÍO] ")
                                    + remision.mensaje());
                            if (remision.enviado()) {
                                enviarExito(
                                        email, respuesta, "Oficio remitido a CPD",
                                        remision.mensaje());
                            } else {
                                enviarErrorDetallado(
                                        email, respuesta, "Oficio pendiente de envío",
                                        remision.mensaje(),
                                        "Reintente con oficio_cpd generar id_oficio_pendiente.");
                            }
                            break;
                        case Token.GENERAR:
                            OficioCpdService.ResultadoRemision reintento =
                                    oficioCpdService.reenviarOficioPendiente(
                                            event.getParams());
                            System.out.println(
                                    (reintento.enviado()
                                            ? "[CU5 - REMISIÓN EXITOSA] "
                                            : "[CU5 - ERROR DE ENVÍO] ")
                                    + reintento.mensaje());
                            if (reintento.enviado()) {
                                enviarExito(
                                        email, respuesta, "Oficio remitido a CPD",
                                        reintento.mensaje());
                            } else {
                                enviarErrorDetallado(
                                        email, respuesta, "Oficio pendiente de envío",
                                        reintento.mensaje(),
                                        "Verifique la configuración SMTP y reintente.");
                            }
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, oficioCpdService.getComandosOficio());
                            break;
                        default:
                            comandoDesconocido(
                                    email, respuesta, "oficio_cpd",
                                    "remitir, listar, consultar, generar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(
                            email, respuesta, ex,
                            "CU5 - Error al procesar/remitir oficio a CPD");
                }
            }

            // ============================================================
            // CU6: CONFIRMACIÓN Y HABILITACIÓN DE CPD
            // ============================================================

            @Override
            public void respuestaCpd(TokenEvent event) {
                try {
                    if (!contextoComando.tieneRol("CPD")) {
                        throw new SecurityException(
                                "Solo una cuenta con rol CPD puede registrar respuestas "
                                + "de habilitación, observación o rechazo.");
                    }
                    switch (event.getAction()) {
                        case Token.OFICIOS:
                            List<String[]> oficiosRecibidos =
                                    respuestaCpdService.listarOficiosRemitidos();
                            imprimirTablaConsola(
                                    "CU6 - Oficios recibidos por CPD",
                                    HtmlBuilder.HEADERS_OFICIO_CPD,
                                    oficiosRecibidos);
                            enviarTabla(
                                    email,
                                    respuesta,
                                    "Oficios recibidos por CPD",
                                    HtmlBuilder.HEADERS_OFICIO_CPD,
                                    oficiosRecibidos);
                            break;
                        case Token.LISTAR:
                            List<String[]> list = respuestaCpdService.listarRespuestas();
                            imprimirTablaConsola(
                                    "CU6 - Respuestas registradas por CPD",
                                    HtmlBuilder.HEADERS_RESPUESTA_CPD, list);
                            enviarTabla(email, respuesta, "Respuestas de CPD", HtmlBuilder.HEADERS_RESPUESTA_CPD, list);
                            break;
                        case Token.PENDIENTES:
                            List<String[]> pendientesCpd =
                                    respuestaCpdService.listarPendientes();
                            System.out.println(
                                    "[CU6 - PENDIENTES CPD] "
                                    + pendientesCpd.size()
                                    + " solicitud(es) remitidas esperan respuesta.");
                            imprimirTablaConsola(
                                    "Oficios pendientes de procesamiento por CPD",
                                    HtmlBuilder.HEADERS_PENDIENTES_CPD,
                                    pendientesCpd);
                            enviarTabla(
                                    email,
                                    respuesta,
                                    "Oficios pendientes de procesamiento por CPD",
                                    HtmlBuilder.HEADERS_PENDIENTES_CPD,
                                    pendientesCpd);
                            break;
                        case Token.REGISTRAR:
                        case Token.AGREGAR:
                            RespuestaCpdService.ResultadoRespuesta respuestaCpd =
                                    respuestaCpdService.responder(
                                            event.getParams(),
                                            contextoComando.idUsuario());
                            String mensajeRespuesta =
                                    "Solicitud " + respuestaCpd.codigoSolicitud()
                                    + ": CPD registró " + respuestaCpd.resultado()
                                    + " (estado " + respuestaCpd.estado() + "). Código: "
                                    + respuestaCpd.codigoRespuesta() + ". Observación: "
                                    + respuestaCpd.observacion() + ". Se enviaron "
                                    + respuestaCpd.correosEnviados() + " correo(s); "
                                    + respuestaCpd.correosPendientes()
                                    + " quedó/quedaron pendiente(s).";
                            if (respuestaCpd.academicosNotificados() == 0) {
                                mensajeRespuesta +=
                                        " No se encontraron cuentas activas de Dirección/"
                                        + "Personal Académico para notificar.";
                            } else {
                                mensajeRespuesta += " Notificaciones para Dirección/"
                                        + "Personal Académico: "
                                        + respuestaCpd.academicosNotificados() + ".";
                            }
                            if (!respuestaCpd.estadosEnvioActualizados()) {
                                mensajeRespuesta += " No se pudo guardar el estado de uno o más correos.";
                            }
                            System.out.println(
                                    "[CU6 - RESPUESTA CPD REGISTRADA] "
                                    + mensajeRespuesta);
                            enviarExito(
                                    email,
                                    respuesta,
                                    "Respuesta de CPD registrada",
                                    mensajeRespuesta);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, respuestaCpdService.getComandosRespuesta());
                            break;
                        default:
                            comandoDesconocido(
                                    email,
                                    respuesta,
                                    "cpd",
                                    "pendientes, responder, respuesta_cpd listar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(
                            email,
                            respuesta,
                            ex,
                            "CU6 - Error al consultar pendientes o registrar respuesta de CPD");
                }
            }

            // ============================================================
            // CU7: GESTIÓN DE HISTORIAL Y NOTIFICACIONES
            // ============================================================

            @Override
            public void historial(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = historialService.listarTodoElHistorial();
                            enviarTabla(email, respuesta, "Historial y Trazabilidad General", HtmlBuilder.HEADERS_HISTORIAL, list);
                            break;
                        case Token.CONSULTAR:
                            if (event.getParams().isEmpty()) throw new IllegalArgumentException("Formato: historial consultar id_solicitud");
                            int idSol = Integer.parseInt(event.getParams().get(0).trim());
                            List<String[]> listH = historialService.consultarHistorialSolicitud(idSol);
                            enviarTabla(email, respuesta, "Historial de Solicitud ID " + idSol, HtmlBuilder.HEADERS_HISTORIAL, listH);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, historialService.getComandosHistorial());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "historial", "listar, consultar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            @Override
            public void notificacion(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = notificacionService.listarNotificaciones();
                            enviarTabla(email, respuesta, "Registro de Notificaciones por Correo", HtmlBuilder.HEADERS_NOTIFICACION, list);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, notificacionService.getComandosNotificacion());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "notificacion", "listar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            // ============================================================
            // CU8: REPORTES Y ESTADÍSTICAS
            // ============================================================

            @Override
            public void reporte(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.GENERAL:
                        case Token.LISTAR:
                            List<String[]> listG = reporteService.reporteGeneral();
                            enviarTabla(email, respuesta, "Reporte General de Solicitudes", HtmlBuilder.HEADERS_REPORTE_GENERAL, listG);
                            break;
                        case Token.ESTADOS:
                            List<String[]> listE = reporteService.reporteEstados();
                            enviarTabla(email, respuesta, "Estadisticas por Estado", HtmlBuilder.HEADERS_REPORTE_ESTADOS, listE);
                            break;
                        case Token.PERIODOS:
                            List<String[]> listP = reporteService.reportePeriodos();
                            enviarTabla(email, respuesta, "Estadisticas por Periodo Academico y Carrera", HtmlBuilder.HEADERS_REPORTE_PERIODOS, listP);
                            break;
                        case Token.CUPOS:
                            List<String[]> listC = reporteService.reporteCupos();
                            enviarTabla(email, respuesta, "Estadisticas de Ocupacion y Reserva de Cupos", HtmlBuilder.HEADERS_REPORTE_CUPOS, listC);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, reporteService.getComandosReporte());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "reporte", "general, estados, periodos, cupos, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            @Override
            public void error(TokenEvent event) {
                String tokenInvalido = (event.getParams() != null && !event.getParams().isEmpty()) ? event.getParams().get(0) : "";
                Utils.ErrorExplainer.ErrorDetail ed = Utils.ErrorExplainer.diagnosticarSintaxis(email.getSubject(), tokenInvalido);
                String asunto = normalizarAliasCu2(
                        normalizarAsuntoComando(email.getSubject()));
                enviarErrorExplainer(
                        email, respuesta, ed, contextoErrorComando(asunto));
            }

            @Override
            public void posibleserrores(TokenEvent event) {
                error(event);
            }
        });

        // Ejecutar interpretacion sintactica
        try {
            interpreter.run();
        } catch (RuntimeException ex) {
            System.err.println("Error inesperado al procesar comando de "
                    + email.getFrom() + ":");
            ex.printStackTrace(System.err);
            enviarRespuestaControlAcceso(
                    email,
                    respuesta,
                    "ERROR INESPERADO:\nNo se pudo procesar el comando. " +
                    "El error fue registrado para su revisión.");
        }
    }

    // ============================================================
    // METODOS DE ENVIO Y COMUNICACION
    // ============================================================

    private static void enviarRespuestaControlAcceso(
            Email email,
            SendEmail respuesta,
            String mensaje
    ) {
        String contenido = mensaje == null ? "" : mensaje;
        boolean esError = contenido.startsWith("ERROR")
                || contenido.startsWith("ACCESO DENEGADO");
        String html;
        if (contenido.startsWith("<!DOCTYPE html>")) {
            html = contenido;
        } else if (esError) {
            String detalle = contenido.replaceFirst(
                    "^(ERROR[^:]*:|ACCESO DENEGADO:)\\s*", "");
            html = HtmlBuilder.generarMensajeErrorDetallado(
                    "Acceso o comando no autorizado",
                    comandoVisible(email),
                    escaparHtml(detalle).replace("\n", "<br>"),
                    "Envie 'help', registrese con 'estudiante register' " +
                    "o inicie sesion con 'acceder sistema correo,password'.");
        } else {
            html = HtmlBuilder.generarMensajeExito(
                    "Respuesta del sistema",
                    escaparHtml(contenido).replace("\n", "<br>"));
        }
        mostraRespuesta(html);
        if (email.getFrom() != null && !email.getFrom().trim().isEmpty()) {
            respuesta.sendmail(
                    email.getFrom(),
                    esError ? "Error del sistema" : "Respuesta del sistema",
                    html);
        }
    }

    private static String comandoVisible(Email email) {
        String asunto = email == null || email.getSubject() == null
                ? ""
                : normalizarAsuntoComando(email.getSubject());
        String[] partesComando = asunto.split("\\s+", 3);
        if (partesComando.length < 3) {
            return asunto;
        }

        String comando = (partesComando[0] + " " + partesComando[1])
                .toLowerCase(java.util.Locale.ROOT);
        String[] parametros = partesComando[2].split(",", -1);
        int indicePassword = switch (comando) {
            case "acceder sistema" -> 1;
            case "estudiante agregar", "estudiante registrar",
                 "estudiante register", "academico agregar",
                 "director agregar", "cpd agregar" -> 7;
            case "estudiante editar", "academico editar",
                 "director editar", "cpd editar" -> 8;
            default -> -1;
        };
        if (indicePassword < 0 || parametros.length <= indicePassword) {
            return asunto;
        }

        parametros[indicePassword] = "********";
        return partesComando[0] + " " + partesComando[1] + " "
                + String.join(",", parametros);
    }

    private static String normalizarAsuntoComando(String asunto) {
        if (asunto == null) {
            return "";
        }
        return asunto.trim().replaceFirst("^\\$\\s*", "").trim();
    }

    private static String normalizarAliasCu2(String asunto) {
        String lower = asunto.toLowerCase(java.util.Locale.ROOT);
        if (lower.equals("enviar solicitud") || lower.startsWith("enviar solicitud ")) {
            return "solicitud registrar" + asunto.substring("enviar solicitud".length());
        }
        if (lower.equals("solicitar materia_grupo")
                || lower.startsWith("solicitar materia_grupo ")) {
            return "solicitud_grupo agregar"
                    + asunto.substring("solicitar materia_grupo".length());
        }
        if (lower.equals("archivo_solicitud adjuntar")
                || lower.startsWith("archivo_solicitud adjuntar ")) {
            return "archivo_solicitud agregar"
                    + asunto.substring("archivo_solicitud adjuntar".length());
        }
        if (lower.equals("pago adjuntar")
                || lower.startsWith("pago adjuntar ")) {
            return "pago agregar"
                    + asunto.substring("pago adjuntar".length());
        }
        if (lower.equals("mis_pagos listar")
                || lower.startsWith("mis_pagos listar ")) {
            return "pago listar"
                    + asunto.substring("mis_pagos listar".length());
        }
        return asunto;
    }

    private static void registrarErrorComando(String asunto, String detalle) {
        String contexto = contextoErrorComando(asunto);
        if (contexto != null) {
            System.out.println(contexto + ": " + textoConsola(detalle));
        }
    }

    private static void mostrarResultadoAccesoEnTerminal(
            String comando,
            String resultado
    ) {
        if (comando == null) {
            return;
        }

        String comandoNormalizado =
                comando.toLowerCase(java.util.Locale.ROOT);
        String mensaje = resultado == null ? "" : resultado.trim();
        if (comandoNormalizado.startsWith("estudiante registrar ")
                || comandoNormalizado.startsWith("estudiante register ")) {
            if (mensaje.startsWith("REGISTRO COMPLETADO")) {
                System.out.println("[ESTUDIANTE REGISTRADO EXITOSAMENTE]\n"
                        + mensaje.replaceFirst("^REGISTRO COMPLETADO\\s*", "").trim());
            } else {
                System.out.println("[ERROR AL REGISTRAR ESTUDIANTE] "
                        + mensaje.replaceFirst("^ERROR[^:]*:\\s*", "")
                                .replace("\n", " "));
            }
            return;
        }

        if (!comandoNormalizado.startsWith("acceder sistema ")) {
            return;
        }

        if (mensaje.startsWith("ACCESO DENEGADO")) {
            System.out.println("[ACCESO DENEGADO] " +
                    mensaje.replaceFirst("^ACCESO DENEGADO:?\\s*", "")
                            .replace("\n", " "));
        } else if (mensaje.startsWith("ERROR")) {
            System.out.println("[ERROR DE ACCESO] " +
                    mensaje.replaceFirst("^ERROR[^:]*:\\s*", "")
                            .replace("\n", " "));
        } else {
            System.out.println("[ACCESO EXITOSO]\n" + mensaje);
        }
    }

    private static String contextoErrorComando(String asunto) {
        String lower = asunto == null
                ? "" : asunto.toLowerCase(java.util.Locale.ROOT);
        if (lower.startsWith("solicitud registrar ")) {
            return "CU2 - Error al enviar la solicitud";
        }
        if (lower.startsWith("solicitud_grupo agregar ")) {
            return "CU2 - Error al agregar materia/grupo";
        }
        if (lower.startsWith("archivo_solicitud agregar ")) {
            return "CU2 - Error al adjuntar documento(s)";
        }
        if (lower.startsWith("pago agregar ")) {
            return "CU3 - Error al registrar el pago";
        }
        if (esComando(lower, "bloqueo_grupo bloquear")
                || esComando(lower, "bloqueo_grupo agregar")) {
            return "CU4 - Error al bloquear cupo";
        }
        if (esComando(lower, "bloqueo_grupo liberar")) {
            return "CU4 - Error al liberar cupo";
        }
        if (esComando(lower, "bloqueo_grupo listar")) {
            return "CU4 - Error al consultar bloqueos";
        }
        return null;
    }

    private static boolean esComando(String asunto, String comando) {
        return asunto.equals(comando) || asunto.startsWith(comando + " ");
    }

    private static boolean esComandoCu1(String asunto) {
        return asunto.startsWith("usuario ")
                || asunto.startsWith("estudiante ")
                || asunto.startsWith("estudiantes ")
                || asunto.startsWith("academico ")
                || asunto.startsWith("academicos ")
                || asunto.startsWith("director ")
                || asunto.startsWith("directores ")
                || asunto.matches("^cpd\\s+(listar|agregar|crear|editar|modificar)(\\s|$).*");
    }

    private static String escaparHtml(String texto) {
        return texto.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static void imprimirTablaConsola(
            String titulo,
            String[] headers,
            List<String[]> filas
    ) {
        System.out.println("=== " + titulo + " ===");
        System.out.println(String.join(" | ", headers));
        if (filas == null || filas.isEmpty()) {
            System.out.println("Sin registros.");
            return;
        }
        for (String[] fila : filas) {
            String[] valores = new String[headers.length];
            for (int i = 0; i < headers.length; i++) {
                valores[i] = fila != null && i < fila.length && fila[i] != null
                        ? fila[i].replaceAll("\\s+", " ").trim() : "";
            }
            System.out.println(String.join(" | ", valores));
        }
    }

    private static void imprimirDetalleConsola(
            String[] solicitud,
            List<String[]> grupos,
            List<String[]> documentos,
            List<String[]> evaluaciones
    ) {
        String[] etiquetas = {
            "ID", "Código", "Estudiante", "Registro", "Período",
            "Tipo de caso", "Estado", "Fecha", "Motivo",
            "Observaciones", "Motivo de rechazo"
        };
        for (int i = 0; i < etiquetas.length; i++) {
            String valor = i < solicitud.length && solicitud[i] != null
                    ? solicitud[i].replaceAll("\\s+", " ").trim() : "";
            System.out.println(etiquetas[i] + ": " + valor);
        }
        imprimirTablaConsola(
                "Materias y grupos", HtmlBuilder.HEADERS_SOLICITUD_GRUPO, grupos);
        imprimirTablaConsola(
                "Documentos adjuntos", HtmlBuilder.HEADERS_ARCHIVO_SOLICITUD,
                documentos);
        imprimirTablaConsola(
                "Evaluaciones", HtmlBuilder.HEADERS_EVALUACION, evaluaciones);
    }

    private static void enviarTabla(Email email, SendEmail respuesta, String titulo, String[] headers, List<String[]> filas) {
        String html = HtmlBuilder.generarTabla(titulo, headers, filas);
        mostraRespuesta(html);
        if (email.getFrom() != null && !email.getFrom().trim().isEmpty()) {
            respuesta.sendmail(email.getFrom(), titulo, html);
        }
    }

    private static void enviarExito(Email email, SendEmail respuesta, String titulo, String mensaje) {
        String html = HtmlBuilder.generarMensajeExito(titulo, mensaje);
        mostraRespuesta(html);
        if (email.getFrom() != null && !email.getFrom().trim().isEmpty()) {
            respuesta.sendmail(email.getFrom(), titulo, html);
        }
    }

    private static void enviarErrorExplainer(Email email, SendEmail respuesta, Utils.ErrorExplainer.ErrorDetail ed) {
        enviarErrorExplainer(email, respuesta, ed, null);
    }

    private static void enviarErrorExplainer(
            Email email,
            SendEmail respuesta,
            Utils.ErrorExplainer.ErrorDetail ed,
            String contextoConsola
    ) {
        ed.setComandoRecibido(comandoVisible(email));
        String html = HtmlBuilder.generarMensajeErrorCompleto(ed);
        String detalle = textoConsola(ed.getQueFallo());
        if (contextoConsola != null && !contextoConsola.trim().isEmpty()) {
            System.out.println(contextoConsola + ": " + detalle);
        } else if (ed.getTitulo().toLowerCase().contains("base de datos")
                || ed.getTitulo().toLowerCase().contains("servidor de base")) {
            System.out.println("Error de base de datos: " + detalle);
        } else {
            System.out.println("Error en el comando: " + detalle);
        }
        mostraRespuesta(html);
        if (email != null && email.getFrom() != null && !email.getFrom().trim().isEmpty()) {
            respuesta.sendmail(email.getFrom(), "Error: " + ed.getTitulo(), html);
        }
    }

    private static void enviarErrorDetallado(Email email, SendEmail respuesta, String titulo, String queFallo, String sugerencia) {
        Utils.ErrorExplainer.ErrorDetail ed = new Utils.ErrorExplainer.ErrorDetail();
        ed.setTitulo(titulo);
        ed.setComandoRecibido(email != null && email.getSubject() != null ? email.getSubject() : "");
        ed.setQueFallo(queFallo);
        ed.setSintaxisCorrecta(sugerencia);
        enviarErrorExplainer(email, respuesta, ed);
    }

    private static void enviarError(String destinatario, SendEmail respuesta, String titulo, String mensaje) {
        enviarErrorDetallado(new Email(destinatario, null), respuesta, titulo, mensaje, "Consulte los comandos validos con 'help'.");
    }

    private static void mostrarYEnviarHELP(Email email, SendEmail respuesta, String textoAyuda) {
        String html = HtmlBuilder.generarAyuda("Guía de Comandos del Sistema", textoAyuda);
        mostraRespuesta(html);
        if (email.getFrom() != null && !email.getFrom().trim().isEmpty()) {
            respuesta.sendmail(email.getFrom(), "Ayuda - Comandos del Sistema", html);
        }
    }

    private static void comandoDesconocido(Email email, SendEmail respuesta, String contextoEntidad, String accionesValidas) {
        String subject = email != null ? email.getSubject() : "";
        Utils.ErrorExplainer.ErrorDetail ed = Utils.ErrorExplainer.diagnosticarComandoDesconocido(subject, contextoEntidad, accionesValidas);
        enviarErrorExplainer(email, respuesta, ed);
    }

    private static void manejarExcepcion(Email email, SendEmail respuesta, Exception ex) {
        manejarExcepcion(email, respuesta, ex, null);
    }

    private static void manejarExcepcion(
            Email email,
            SendEmail respuesta,
            Exception ex,
            String contextoConsola
    ) {
        String subject = email != null ? email.getSubject() : "";
        String from = email != null ? email.getFrom() : "";
        Utils.ErrorExplainer.ErrorDetail ed = Utils.ErrorExplainer.diagnosticarExcepcion(subject, ex, from);
        enviarErrorExplainer(email, respuesta, ed, contextoConsola);
    }

    private static String textoConsola(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "revise el comando enviado";
        }
        String limpio = texto.replaceAll("<[^>]*>", "")
                .replaceAll("\\s+", " ")
                .trim();
        return limpio.length() > 180 ? limpio.substring(0, 177) + "..." : limpio;
    }

    private static void comandoDesconocido(String destinatario, SendEmail respuesta) {
        comandoDesconocido(new Email(destinatario, null), respuesta, null, null);
    }

    private static void manejarExcepcion(String destinatario, SendEmail respuesta, Exception ex) {
        manejarExcepcion(new Email(destinatario, null), respuesta, ex);
    }

    public static void mostraRespuesta(final String html) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (Principal.ta_respuesta != null) {
                        Principal.ta_respuesta.setContentType("text/html");
                        Principal.ta_respuesta.setText(html);
                        Principal.ta_respuesta.setCaretPosition(0);
                    }
                } catch (Exception ignored) {
                }
            }
        });
    }

}
