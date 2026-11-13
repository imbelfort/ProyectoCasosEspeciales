
package Comunication;

import Services.AccesoService;
import Services.RegistroEstudianteService;
import Services.UsuarioService;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ControlAccesoComandos {

    private final AccesoService acceso =
        new AccesoService();

    private final RegistroEstudianteService registro =
        new RegistroEstudianteService();

    private final UsuarioService usuarios =
        new UsuarioService();

    public String procesar(
            String asunto,
            String remitente,
            String nombreAdjunto,
            String rutaInterna
    ) {

        try {
            if (asunto == null || asunto.isBlank()) {
                throw new IllegalArgumentException(
                    "El asunto del correo esta vacio.");
            }

            String comando = asunto.trim().replaceFirst("^\\$\\s*", "").trim();

            // ======================================
            // COMANDOS PUBLICOS
            // ======================================

            if (comando.equalsIgnoreCase("acceder ayuda")) {
                return ayuda();
            }

            if (comando.toLowerCase(Locale.ROOT)
                    .startsWith("acceder sistema ")) {

                String args = comando.substring(
                    "acceder sistema ".length());

                String[] valores = args.split(",", -1);

                if (valores.length != 2) {
                    throw new IllegalArgumentException(
                        "Formato correcto: acceder sistema " +
                        "correo,password");
                }

                AccesoService.ResultadoAcceso resultado =
                    acceso.iniciarSesion(
                        valores[0].trim(),
                        valores[1],
                        remitente
                    );

                return resultado.mensaje() + "\n" +
                    "Roles: " + resultado.roles() + "\n" +
                    "Desde este correo ya puede enviar comandos directamente, " +
                    "por ejemplo: usuario listar.\n" +
                    "Para cerrar sesion: acceder salir.";
            }

            if (comando.toLowerCase(Locale.ROOT)
                    .startsWith("estudiante registrar ")) {

                String args = comando.substring(
                    "estudiante registrar ".length());

                List<String> params = parsearParametros(args);

                return registro.registrar(params);
            }

            if (comando.toLowerCase(Locale.ROOT)
                    .startsWith("estudiante register ")) {

                String args = comando.substring(
                    "estudiante register ".length());

                List<String> params = parsearParametros(args);

                return registro.registrar(params);
            }

            if (comando.equalsIgnoreCase("acceder salir")) {
                return acceso.cerrarSesionPorRemitente(remitente);
            }

            String token = null;
            String comandoReal = comando;
            AccesoService.Contexto contexto;
            if (comando.toLowerCase(Locale.ROOT).startsWith("token ")) {
                int separador = comando.indexOf('|');
                if (separador < 0) {
                    throw new IllegalArgumentException(
                        "Formato: token CREDENCIAL | comando");
                }

                String[] tokenPartes = comando
                    .substring(0, separador).trim().split("\\s+");
                if (tokenPartes.length != 2) {
                    throw new IllegalArgumentException(
                        "La credencial de sesion es incorrecta.");
                }
                token = tokenPartes[1];
                comandoReal = comando.substring(separador + 1).trim();
                contexto = acceso.validar(token);
            } else {
                contexto = acceso.validarPorRemitente(remitente);
            }

            if (comandoReal.isBlank()) {
                throw new IllegalArgumentException(
                    "Debe indicar el comando que desea ejecutar.");
            }

            if (comandoReal.equalsIgnoreCase("acceder salir")) {
                return token == null
                        ? acceso.cerrarSesionPorRemitente(remitente)
                        : acceso.cerrarSesion(token);
            }

            if (comandoReal.equalsIgnoreCase("acceder perfil")) {
                return "USUARIO AUTENTICADO\n" +
                    "ID: " + contexto.idUsuario() + "\n" +
                    "Correo: " + contexto.email() + "\n" +
                    "Roles: " + contexto.roles();
            }

            if (comandoReal.equalsIgnoreCase("acceder comandos")) {
                return comandosPermitidos(contexto);
            }

            String[] partes =
                comandoReal.split("\\s+", 3);

            if (partes.length < 2) {
                throw new IllegalArgumentException(
                    "El comando esta incompleto.");
            }

            String entidad =
                partes[0].toLowerCase(Locale.ROOT);

            String accion =
                partes[1].toLowerCase(Locale.ROOT);

            // Verificacion central de permisos.
            acceso.autorizar(contexto, entidad, accion);

            String argumentos = partes.length > 2 ? partes[2].trim() : "";
            return ejecutarComandoAutorizado(entidad, accion, argumentos, contexto);
            
        } catch (SecurityException ex) {
            return "ACCESO DENEGADO:\n" + ex.getMessage();

        } catch (IllegalArgumentException ex) {
            return "ERROR DE VALIDACION:\n" + ex.getMessage();

        } catch (SQLException ex) {
            System.err.println("Error de base de datos al procesar comando '"
                    + asunto + "':");
            ex.printStackTrace(System.err);
            return "ERROR DEL SISTEMA:\n" +
                describirErrorBaseDatos(ex);

        } catch (Exception ex) {
            System.err.println("Error inesperado al procesar comando '"
                    + asunto + "':");
            ex.printStackTrace(System.err);
            String detalle = ex.getMessage();
            if (detalle == null || detalle.isBlank()) {
                detalle = "No se obtuvo una descripción adicional.";
            }
            return "ERROR INESPERADO:\n" +
                detalle;
        }
    }

    private String describirErrorBaseDatos(SQLException ex) {
        String estado = ex.getSQLState();
        if (estado == null) {
            estado = "";
        }

        return switch (estado) {
            case "23505" ->
                "Ya existe un registro con uno de los datos únicos enviados " +
                "(por ejemplo, CI, correo o registro universitario).";
            case "23503" ->
                "La carrera u otro registro relacionado indicado no existe.";
            case "23502" ->
                "Falta un dato obligatorio requerido por la base de datos.";
            case "23514" ->
                "Uno de los datos no cumple las restricciones permitidas.";
            case "22001" ->
                "Uno de los datos supera la longitud máxima permitida.";
            case "08001", "08003", "08006" ->
                "No se pudo establecer o mantener la conexión con la base de datos.";
            default -> {
                String mensaje = ex.getMessage();
                if (mensaje != null && !mensaje.isBlank()
                        && mensaje.startsWith("No ")) {
                    yield mensaje;
                }
                yield "No se pudo completar la operación en la base de datos."
                        + (estado.isBlank() ? "" : " Código: " + estado + ".");
            }
        };
    }

    private String ejecutarComandoAutorizado(
            String entidad,
            String accion,
            String argumentos,
            AccesoService.Contexto contexto
    ) throws SQLException {

        String nombreEntidad = entidad.toLowerCase(Locale.ROOT);
        String nombreAccion = accion.toLowerCase(Locale.ROOT);

        switch (nombreEntidad) {
            case "usuario":
                return ejecutarComandoUsuario(nombreAccion, argumentos);
            case "estudiante":
            case "estudiantes":
                return ejecutarComandoEstudiante(nombreAccion, argumentos);
            case "academico":
            case "academicos":
                return ejecutarComandoAcademico(nombreAccion, argumentos);
            case "director":
            case "directores":
                return ejecutarComandoDirector(nombreAccion, argumentos);
            case "cpd":
                return ejecutarComandoCpd(nombreAccion, argumentos);
            default:
                throw new IllegalArgumentException(
                    "El comando no esta implementado " +
                    "o no es valido para este rol: " +
                    entidad + " " + accion);
        }
    }

    private String ejecutarComandoUsuario(
            String accion,
            String argumentos
    ) throws SQLException {

        if ("listar".equals(accion)) {
        if (argumentos != null && !argumentos.isBlank()) {
            throw new IllegalArgumentException(
                    "Formato: usuario listar");
        }
        return formatearLista(
                "Lista de Usuarios",
                HtmlBuilder.HEADERS_USER,
                usuarios.listarUsuarios());
    }

        throw new IllegalArgumentException(
            "Solo se permite 'usuario listar'. Los usuarios deben crearse " +
            "mediante el comando de alta del rol correspondiente.");
    }

    private String ejecutarComandoEstudiante(
            String accion,
            String argumentos
    ) throws SQLException {

        if ("listar".equals(accion)) {
            return formatearLista(
                    "Lista de Estudiantes",
                    HtmlBuilder.HEADERS_ESTUDIANTE,
                    usuarios.listarEstudiantes());
        }

        if ("agregar".equals(accion) || "crear".equals(accion)) {
            usuarios.crearEstudiante(parsearParametros(argumentos));
            return "ESTUDIANTE CREADO CORRECTAMENTE";
        }

        if ("editar".equals(accion) || "modificar".equals(accion)) {
            usuarios.editarEstudiante(parsearParametros(argumentos));
            return "ESTUDIANTE ACTUALIZADO CORRECTAMENTE";
        }

        throw new IllegalArgumentException(
            "Operacion no soportada para estudiante: " + accion);
    }

    private String ejecutarComandoAcademico(
            String accion,
            String argumentos
    ) throws SQLException {

        if ("listar".equals(accion)) {
            return formatearLista(
                    "Lista de Personal Academico",
                    HtmlBuilder.HEADERS_PERSONAL_CARRERA,
                    usuarios.listarAcademicos());
        }

        if ("agregar".equals(accion) || "crear".equals(accion)) {
            usuarios.crearAcademico(parsearParametros(argumentos));
            return "ACADEMICO CREADO CORRECTAMENTE";
        }

        if ("editar".equals(accion) || "modificar".equals(accion)) {
            usuarios.editarAcademico(parsearParametros(argumentos));
            return "ACADEMICO ACTUALIZADO CORRECTAMENTE";
        }

        throw new IllegalArgumentException(
            "Operacion no soportada para academico: " + accion);
    }

    private String ejecutarComandoDirector(
            String accion,
            String argumentos
    ) throws SQLException {

        if ("listar".equals(accion)) {
            return formatearLista(
                    "Lista de Directores",
                    HtmlBuilder.HEADERS_DIRECTOR_CARRERA,
                    usuarios.listarDirectores());
        }

        if ("agregar".equals(accion) || "crear".equals(accion)) {
            usuarios.crearDirector(parsearParametros(argumentos));
            return "DIRECTOR CREADO CORRECTAMENTE";
        }

        if ("editar".equals(accion) || "modificar".equals(accion)) {
            usuarios.editarDirector(parsearParametros(argumentos));
            return "DIRECTOR ACTUALIZADO CORRECTAMENTE";
        }

        throw new IllegalArgumentException(
            "Operacion no soportada para director: " + accion);
    }

    private String ejecutarComandoCpd(
            String accion,
            String argumentos
    ) throws SQLException {

        if ("listar".equals(accion)) {
            return formatearLista(
                    "Personal CPD",
                    new String[]{"ID", "NOMBRE", "APELLIDOS", "CI", "EMAIL",
                        "TELEFONO", "DIRECCION", "ESTADO", "FECHA NACIMIENTO", "ROL"},
                    usuarios.listarCpd());
        }

        if ("agregar".equals(accion) || "crear".equals(accion)) {
            usuarios.crearCpd(parsearParametros(argumentos));
            return "CPD CREADO CORRECTAMENTE";
        }

        if ("editar".equals(accion) || "modificar".equals(accion)) {
            usuarios.editarCpd(parsearParametros(argumentos));
            return "CPD ACTUALIZADO CORRECTAMENTE";
        }

        throw new IllegalArgumentException(
            "Operacion no soportada para cpd: " + accion);
    }

    private List<String> parsearParametros(String argumentos) {
        if (argumentos == null || argumentos.isBlank()) {
            return List.of();
        }

        String[] valores = argumentos.split(",", -1);
        for (int i = 0; i < valores.length; i++) {
            valores[i] = valores[i].trim();
            if (valores[i].length() >= 2
                    && valores[i].startsWith("`")
                    && valores[i].endsWith("`")) {
                valores[i] = valores[i].substring(1, valores[i].length() - 1).trim();
            }
        }
        return Arrays.asList(valores);
    }

    private String formatearLista(
            String titulo,
            String[] encabezados,
            List<String[]> registros
    ) {
        return HtmlBuilder.generarTabla(titulo, encabezados, registros);
    }

    private String ayuda() {
        return HtmlBuilder.generarAyuda(
                "Acceso al sistema",
                """
            ACCESO AL SISTEMA FICCT-UAGRM

            COMANDOS PUBLICOS:
            acceder sistema correo,password
            estudiante register nombre,apellido,ci,email,telefono,direccion,fecha_nacimiento,password,registro_universitario,id_carrera,semestre
            estudiante registrar nombre,apellido,ci,email,telefono,direccion,fecha_nacimiento,password,registro_universitario,id_carrera,semestre
            Inicie sesion con 'acceder sistema' para ver
            los comandos de gestion disponibles.
            """);
    }

    private String comandosPermitidos(
            AccesoService.Contexto contexto
    ) {

        if (contexto.esAdministrativo()) {
            return "ROL ADMINISTRATIVO\n\n"
                + usuarios.getComandosUsuario()
                + """

                CU2: solicitud listar periodo,
                     solicitud pendientes listar,periodo,
                     solicitud_grupo,
                     archivo_solicitud,
                     pago listar
                CU3: solicitud periodos id_periodo_o_gestion,
                     solicitud consultar id_solicitud,
                     evaluacion registrar id_solicitud,decision,motivo[,observaciones]
                CU4: bloqueo_grupo bloquear/liberar cupos,
                     carrera, periodo, materia, grupo
                CU5: oficio_cpd remitir gestion,correo_cpd,referencia,detalle,
                     oficio_cpd listar/consultar, oficio_cpd generar id_oficio_pendiente
                CU6: oficio_cpd listar/consultar,
                     cpd pendientes,
                     cpd oficios,
                     cpd responder id_oficio,id_solicitud,resultado,
                     codigo_respuesta,observacion,
                     respuesta_cpd listar
                CU7: historial, notificacion
                CU8: reporte

                Consulte 'entidad help' para conocer
                los comandos de cada modulo.
                """;
        }

        if (contexto.tieneRol("ESTUDIANTE")) {
            return """
                ROL ESTUDIANTE

                enviar solicitud gestion,tipo_caso,motivo
                solicitar materia_grupo id_solicitud,periodo,sigla_materia,sigla_grupo,motivo
                archivo_solicitud adjuntar id_solicitud,tipo_documento
                pago adjuntar id_solicitud,COMPROBANTE_PAGO
                pago listar (alias: mis_pagos listar)
                solicitud mis_casos
                solicitud consultar
                solicitud_grupo listar
                archivo_solicitud listar
                historial consultar
                grupo listar
                grupo consultar
                materia listar
                periodo listar
                carrera listar
                notificacion listar

                Solo puede consultar sus propios registros.
                """;
        }

        if (contexto.tieneRol("CPD")) {
            return """
                ROL CPD

                cpd pendientes
                cpd responder id_oficio,id_solicitud,HABILITADO|OBSERVADO|RECHAZADO,codigo_respuesta,observacion
                respuesta_cpd listar
                oficio_cpd listar
                oficio_cpd consultar id_oficio
                solicitud consultar
                historial consultar
                notificacion listar

                Acceso limitado a expedientes autorizados.
                """;
        }

        return "No tiene comandos habilitados.";
    }
}
