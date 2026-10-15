package Comunication;

import Interfaces.IEmailEventListener;
import Interfaces.ITokenCasoUsoEventListener;
import Models.Solicitud;
import Services.*;
import Utils.Email;
import Utils.Interpreter;
import Utils.Token;
import proyectomailgrupo01sa.Principal;

import java.sql.SQLException;
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
        final RolService rolService = new RolService();
        final EstudianteService estudianteService = new EstudianteService();
        final PersonalCarreraService personalCarreraService = new PersonalCarreraService();
        final DirectorCarreraService directorCarreraService = new DirectorCarreraService();
        final CarreraService carreraService = new CarreraService();
        final PeriodoService periodoService = new PeriodoService();
        final MateriaService materiaService = new MateriaService();
        final GrupoService grupoService = new GrupoService();
        final SolicitudService solicitudService = new SolicitudService();
        final ArchivoSolicitudService archivoSolicitudService = new ArchivoSolicitudService();
        final EvaluacionService evaluacionService = new EvaluacionService();
        final BloqueoGrupoService bloqueoGrupoService = new BloqueoGrupoService();
        final OficioCpdService oficioCpdService = new OficioCpdService();
        final RespuestaCpdService respuestaCpdService = new RespuestaCpdService();
        final HistorialService historialService = new HistorialService();
        final NotificacionService notificacionService = new NotificacionService();
        final ReporteService reporteService = new ReporteService();

        String subjectText = email.getSubject() != null ? email.getSubject().trim() : "";
        System.out.println("[COMANDO RECIBIDO] De: " + email.getFrom() + " | Subject: " + subjectText);

        // Comando HELP global
        if ("help".equalsIgnoreCase(subjectText) || "ayuda".equalsIgnoreCase(subjectText)) {
            String menuAyuda = getMenuGeneralAyuda();
            mostrarYEnviarHELP(email, respuesta, menuAyuda);
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
                        case Token.AGREGAR:
                            usuarioService.crearUsuario(event.getParams(), email.getAttachmentContentBase64());
                            List<String[]> listAdd = usuarioService.listarUsuarios();
                            enviarTabla(email, respuesta, "Usuario agregado exitosamente", HtmlBuilder.HEADERS_USER, listAdd);
                            break;
                        case Token.MODIFICAR:
                            usuarioService.actualizarUsuario(event.getParams(), email.getAttachmentContentBase64());
                            List<String[]> listMod = usuarioService.listarUsuarios();
                            enviarTabla(email, respuesta, "Usuario modificado exitosamente", HtmlBuilder.HEADERS_USER, listMod);
                            break;
                        case Token.ELIMINAR:
                            if (event.getParams().isEmpty()) throw new IllegalArgumentException("Debe especificar id: usuario eliminar id");
                            int id = Integer.parseInt(event.getParams().get(0).trim());
                            usuarioService.eliminarUsuario(id);
                            List<String[]> listDel = usuarioService.listarUsuarios();
                            enviarTabla(email, respuesta, "Usuario eliminado exitosamente", HtmlBuilder.HEADERS_USER, listDel);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, usuarioService.getComandosUsuario());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "usuario", "listar, agregar, modificar, eliminar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            @Override
            public void rol(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = rolService.listarRoles();
                            enviarTabla(email, respuesta, "Lista de Roles Institucionales", HtmlBuilder.HEADERS_ROL, list);
                            break;
                        case Token.AGREGAR:
                            rolService.crearRol(event.getParams());
                            List<String[]> listAdd = rolService.listarRoles();
                            enviarTabla(email, respuesta, "Rol creado exitosamente", HtmlBuilder.HEADERS_ROL, listAdd);
                            break;
                        case Token.ASIGNAR:
                            rolService.asignarRol(event.getParams());
                            enviarExito(email, respuesta, "Asignacion de Rol", "Rol asignado correctamente al usuario.");
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, rolService.getComandosRol());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "rol", "listar, agregar, asignar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
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
                            List<String[]> list = solicitudService.listarSolicitudes();
                            enviarTabla(email, respuesta, "Solicitudes de Casos Especiales", HtmlBuilder.HEADERS_SOLICITUD, list);
                            break;
                        case Token.MIS_CASOS:
                            List<String[]> misCasos = solicitudService.listarMisCasos(email.getFrom(), event.getParams());
                            enviarTabla(email, respuesta, "Mis Solicitudes de Casos Especiales", HtmlBuilder.HEADERS_SOLICITUD, misCasos);
                            break;
                        case Token.PENDIENTES:
                            List<String[]> misPendientes = solicitudService.listarMisPendientes(email.getFrom(), event.getParams());
                            enviarTabla(email, respuesta, "Mis Solicitudes Pendientes", HtmlBuilder.HEADERS_SOLICITUD, misPendientes);
                            break;
                        case Token.REGISTRAR:
                        case Token.AGREGAR:
                            Solicitud sol = solicitudService.registrarSolicitud(event.getParams(), email.getFrom());
                            List<String[]> listAdd = solicitudService.listarSolicitudes();
                            enviarTabla(email, respuesta, "Solicitud registrada con codigo " + sol.getCodigo(), HtmlBuilder.HEADERS_SOLICITUD, listAdd);
                            break;
                        case Token.CONSULTAR:
                            if (event.getParams().isEmpty()) throw new IllegalArgumentException("Formato: solicitud consultar id_solicitud");
                            int id = Integer.parseInt(event.getParams().get(0).trim());
                            String[] solInfo = solicitudService.consultarSolicitud(id);
                            if (solInfo == null) {
                                enviarErrorDetallado(email, respuesta, "Consulta Solicitud", "No se encontró ninguna solicitud con el ID: " + id, "Verifique el número de ID o consulte sus solicitudes con 'solicitud mis_casos'.");
                            } else {
                                java.util.ArrayList<String[]> solList = new java.util.ArrayList<String[]>();
                                solList.add(solInfo);
                                enviarTabla(email, respuesta, "Detalle de Solicitud " + solInfo[1], HtmlBuilder.HEADERS_SOLICITUD, solList);
                            }
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, solicitudService.getComandosSolicitud());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "solicitud", "listar, mis_casos, pendientes, registrar, consultar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            @Override
            public void solicitudGrupo(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.AGREGAR:
                            solicitudService.agregarGrupoASolicitud(event.getParams());
                            int idSol = Integer.parseInt(event.getParams().get(0).trim());
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
                            mostrarYEnviarHELP(email, respuesta, "Comandos Solicitud-Grupo:\n- solicitud_grupo listar id_solicitud\n- solicitud_grupo agregar id_solicitud,id_grupo,motivo");
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "solicitud_grupo", "agregar, listar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            @Override
            public void archivoSolicitud(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.AGREGAR:
                            archivoSolicitudService.adjuntarArchivo(event.getParams());
                            int idSol = Integer.parseInt(event.getParams().get(0).trim());
                            List<String[]> list = archivoSolicitudService.listarArchivosDeSolicitud(idSol);
                            enviarTabla(email, respuesta, "Documento adjuntado a la Solicitud ID " + idSol, HtmlBuilder.HEADERS_ARCHIVO_SOLICITUD, list);
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
                    manejarExcepcion(email, respuesta, ex);
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
                            enviarTabla(email, respuesta, "Evaluaciones de Dirección de Carrera", HtmlBuilder.HEADERS_EVALUACION, list);
                            break;
                        case Token.REGISTRAR:
                        case Token.EVALUAR:
                            evaluacionService.evaluarSolicitud(event.getParams(), email.getFrom());
                            List<String[]> listEv = evaluacionService.listarEvaluaciones();
                            enviarTabla(email, respuesta, "Evaluacion registrada exitosamente", HtmlBuilder.HEADERS_EVALUACION, listEv);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, evaluacionService.getComandosEvaluacion());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "evaluacion", "listar, registrar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
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
                            enviarTabla(email, respuesta, "Control y Bloqueos de Cupos", HtmlBuilder.HEADERS_BLOQUEO_GRUPO, list);
                            break;
                        case Token.BLOQUEAR:
                        case Token.AGREGAR:
                            bloqueoGrupoService.bloquearCupo(event.getParams());
                            List<String[]> listB = bloqueoGrupoService.listarBloqueos();
                            enviarTabla(email, respuesta, "Cupo bloqueado/reservado exitosamente", HtmlBuilder.HEADERS_BLOQUEO_GRUPO, listB);
                            break;
                        case Token.LIBERAR:
                            bloqueoGrupoService.liberarCupo(event.getParams());
                            List<String[]> listLib = bloqueoGrupoService.listarBloqueos();
                            enviarTabla(email, respuesta, "Cupo liberado exitosamente", HtmlBuilder.HEADERS_BLOQUEO_GRUPO, listLib);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, bloqueoGrupoService.getComandosBloqueo());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "bloqueo_grupo", "listar, bloquear, liberar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
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
                            List<String[]> list = oficioCpdService.listarOficios();
                            enviarTabla(email, respuesta, "Oficios Remitidos a CPD", HtmlBuilder.HEADERS_OFICIO_CPD, list);
                            break;
                        case Token.GENERAR:
                        case Token.AGREGAR:
                            oficioCpdService.remitirACpd(event.getParams(), email.getFrom());
                            List<String[]> listGen = oficioCpdService.listarOficios();
                            enviarTabla(email, respuesta, "Caso remitido a CPD exitosamente", HtmlBuilder.HEADERS_OFICIO_CPD, listGen);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, oficioCpdService.getComandosOficio());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "oficio_cpd", "listar, generar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
                }
            }

            // ============================================================
            // CU6: CONFIRMACIÓN Y HABILITACIÓN DE CPD
            // ============================================================

            @Override
            public void respuestaCpd(TokenEvent event) {
                try {
                    switch (event.getAction()) {
                        case Token.LISTAR:
                            List<String[]> list = respuestaCpdService.listarRespuestas();
                            enviarTabla(email, respuesta, "Respuestas y Habilitaciones de CPD", HtmlBuilder.HEADERS_RESPUESTA_CPD, list);
                            break;
                        case Token.REGISTRAR:
                        case Token.AGREGAR:
                            respuestaCpdService.registrarRespuestaCpd(event.getParams(), email.getFrom());
                            List<String[]> listReg = respuestaCpdService.listarRespuestas();
                            enviarTabla(email, respuesta, "Respuesta de CPD registrada exitosamente", HtmlBuilder.HEADERS_RESPUESTA_CPD, listReg);
                            break;
                        case Token.HELP:
                            mostrarYEnviarHELP(email, respuesta, respuestaCpdService.getComandosRespuesta());
                            break;
                        default:
                            comandoDesconocido(email, respuesta, "respuesta_cpd", "listar, registrar, help");
                    }
                } catch (Exception ex) {
                    manejarExcepcion(email, respuesta, ex);
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
                enviarErrorExplainer(email, respuesta, ed);
            }

            @Override
            public void posibleserrores(TokenEvent event) {
                error(event);
            }
        });

        // Ejecutar interpretacion sintactica
        interpreter.run();
    }

    // ============================================================
    // METODOS DE ENVIO Y COMUNICACION
    // ============================================================

    private static void enviarTabla(Email email, SendEmail respuesta, String titulo, String[] headers, List<String[]> filas) {
        String html = HtmlBuilder.generarTabla(titulo, headers, filas);
        mostraRespuesta(html);
        if (email.getFrom() != null && !email.getFrom().trim().isEmpty()) {
            respuesta.sendmail(email.getFrom(), titulo, html);
        }
        System.out.println("[RESPUESTA TABLA] '" + titulo + "' (" + (filas != null ? filas.size() : 0) + " registros) -> " + email.getFrom());
    }

    private static void enviarExito(Email email, SendEmail respuesta, String titulo, String mensaje) {
        String html = HtmlBuilder.generarMensajeExito(titulo, mensaje);
        mostraRespuesta(html);
        if (email.getFrom() != null && !email.getFrom().trim().isEmpty()) {
            respuesta.sendmail(email.getFrom(), titulo, html);
        }
        System.out.println("[RESPUESTA EXITO] '" + titulo + "' -> " + email.getFrom() + " | " + mensaje);
    }

    private static void enviarErrorExplainer(Email email, SendEmail respuesta, Utils.ErrorExplainer.ErrorDetail ed) {
        String html = HtmlBuilder.generarMensajeErrorCompleto(ed);
        mostraRespuesta(html);
        if (email != null && email.getFrom() != null && !email.getFrom().trim().isEmpty()) {
            respuesta.sendmail(email.getFrom(), "Error: " + ed.getTitulo(), html);
        }
        System.err.println("[RESPUESTA ERROR] '" + ed.getTitulo() + "' -> " + (email != null ? email.getFrom() : "") + " | " + ed.getQueFallo());
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
        System.out.println("[RESPUESTA AYUDA] Guía de comandos enviada a " + email.getFrom());
    }

    private static void comandoDesconocido(Email email, SendEmail respuesta, String contextoEntidad, String accionesValidas) {
        String subject = email != null ? email.getSubject() : "";
        Utils.ErrorExplainer.ErrorDetail ed = Utils.ErrorExplainer.diagnosticarComandoDesconocido(subject, contextoEntidad, accionesValidas);
        enviarErrorExplainer(email, respuesta, ed);
    }

    private static void manejarExcepcion(Email email, SendEmail respuesta, Exception ex) {
        String subject = email != null ? email.getSubject() : "";
        String from = email != null ? email.getFrom() : "";
        Utils.ErrorExplainer.ErrorDetail ed = Utils.ErrorExplainer.diagnosticarExcepcion(subject, ex, from);
        enviarErrorExplainer(email, respuesta, ed);
        ex.printStackTrace();
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

    private static String getMenuGeneralAyuda() {
        return "====================================================\n" +
               "SISTEMA DE GESTION DE CASOS ESPECIALES ACADEMICOS\n" +
               "====================================================\n" +
               "Envie en el SUBJECT del correo uno de los siguientes comandos:\n\n" +
               "CU1. GESTION DE USUARIOS Y ROLES:\n" +
               "  - usuario listar\n" +
               "  - usuario agregar nombre,apellidos,ci,email,telefono,direccion,fecha_nac(yyyy-MM-dd),password\n" +
               "  - usuario modificar id,nombre,apellidos,ci,email,telefono,direccion,fecha_nac,password,estado\n" +
               "  - usuario eliminar id\n" +
               "  - rol listar\n" +
               "  - rol agregar nombre,descripcion\n" +
               "  - rol asignar id_usuario,id_rol\n" +
               "  - estudiante listar\n" +
               "  - estudiante agregar id_usuario,registro_universitario,id_carrera,semestre_actual\n" +
               "  - personal_carrera listar\n" +
               "  - personal_carrera agregar id_usuario,cargo,id_carrera\n" +
               "  - director_carrera listar\n" +
               "  - director_carrera agregar id_usuario,id_carrera,gestion_inicio,gestion_fin\n\n" +
               "CU2. SOLICITUD DE CASO ESPECIAL:\n" +
               "  - solicitud listar\n" +
               "  - solicitud mis_casos [registro,password]\n" +
               "  - solicitud pendientes [registro,password]\n" +
               "  - solicitud registrar id_estudiante,id_periodo,tipo_caso,motivo\n" +
               "  - solicitud consultar id_solicitud\n" +
               "  - solicitud_grupo agregar id_solicitud,id_grupo,motivo\n" +
               "  - solicitud_grupo listar id_solicitud\n" +
               "  - archivo_solicitud agregar id_solicitud,nombre_archivo,tipo,ruta_o_descripcion\n" +
               "  - archivo_solicitud listar id_solicitud\n\n" +
               "CU3. EVALUACION POR DIRECCION DE CARRERA:\n" +
               "  - evaluacion listar\n" +
               "  - evaluacion registrar id_solicitud,id_evaluador,decision(APROBADA|RECHAZADA|OBSERVADA),justificacion,observaciones\n\n" +
               "CU4. BLOQUEO Y CONTROL DE CUPOS:\n" +
               "  - carrera listar | carrera agregar codigo,nombre,facultad\n" +
               "  - periodo listar | periodo agregar gestion,fecha_inicio,fecha_fin\n" +
               "  - materia listar | materia agregar sigla,nombre,nivel_semestre,creditos\n" +
               "  - grupo listar | grupo consultar id_grupo\n" +
               "  - grupo agregar id_materia,id_periodo,sigla_grupo,docente,cupo_max,cupo_exc\n" +
               "  - bloqueo_grupo listar\n" +
               "  - bloqueo_grupo bloquear id_grupo,id_solicitud,cantidad,motivo,id_autorizador\n" +
               "  - bloqueo_grupo liberar id_bloqueo,observacion\n\n" +
               "CU5. REMISION A CPD:\n" +
               "  - oficio_cpd listar\n" +
               "  - oficio_cpd generar id_solicitud,destinatario,referencia,detalle,id_responsable\n\n" +
               "CU6. CONFIRMACION Y HABILITACION DE CPD:\n" +
               "  - respuesta_cpd listar\n" +
               "  - respuesta_cpd registrar id_oficio,id_solicitud,resultado(HABILITADA|CONFIRMADA_CPD|OBSERVADA_CPD|RECHAZADA_CPD),referencia_respaldo,observaciones,id_responsable\n\n" +
               "CU7. HISTORIAL Y TRAZABILIDAD:\n" +
               "  - historial listar\n" +
               "  - historial consultar id_solicitud\n" +
               "  - notificacion listar\n\n" +
               "CU8. REPORTES Y ESTADISTICAS:\n" +
               "  - reporte general\n" +
               "  - reporte estados\n" +
               "  - reporte periodos\n" +
               "  - reporte cupos\n";
    }
}
