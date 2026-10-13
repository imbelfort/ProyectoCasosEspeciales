# Sistema de Gestión de Casos Especiales Académicos

## 1. Descripción general

El sistema permitirá gestionar las solicitudes de casos especiales
presentadas por estudiantes, su evaluación por la Dirección de Carrera,
el control de cupos en materias y paralelos, la remisión de los casos
aprobados a CPD y el registro de su confirmación y habilitación.

La aplicación se desarrollará en Java e incorporará el envío de correos
electrónicos para notificar a los usuarios sobre la recepción,
evaluación, aprobación, observación, remisión y resolución de sus
solicitudes, según corresponda.

> **Nota:** CPD se conserva como denominación institucional. Su
> significado, funciones y responsabilidades deberán confirmarse con la
> institución.

## 2. Actores del sistema

-   **Director:** administra o consulta la información y participa en
    las actividades que le correspondan según los permisos
    institucionales.
-   **Personal académico:** gestiona las solicitudes y realiza las
    evaluaciones o tareas asignadas según sus permisos.
-   **Estudiante:** registra solicitudes de casos especiales, adjunta
    información requerida y consulta el estado de sus trámites.
-   **Dirección de Carrera:** evalúa las solicitudes y registra su
    decisión.
-   **CPD:** recibe los casos especiales remitidos, registra la
    confirmación y comunica o registra la habilitación correspondiente.
-   **Servicio de correo electrónico:** componente externo que envía
    notificaciones automáticas del sistema.

Los roles definitivos y sus permisos se ajustarán a las reglas reales de
la institución.

## 3. Casos de uso aprobados

### CU1. Gestión de usuarios (Director, Académico y Estudiantes) -- Fotos

**Objetivo:** administrar las cuentas de los usuarios del sistema y sus
fotografías de perfil.

**Actores:** Director, personal académico y estudiantes; administrador
autorizado, si corresponde.

**Funciones principales:** - Registrar usuarios y actualizar sus
datos. - Consultar y editar la información permitida según el rol. -
Registrar, cambiar o quitar la fotografía de perfil. - Activar,
desactivar o bloquear cuentas, de acuerdo con las políticas
institucionales. - Asignar roles y permisos a usuarios autorizados. -
Validar los datos obligatorios y evitar cuentas duplicadas. - Enviar
correo de bienvenida, activación o recuperación de acceso, si se
habilitan estas funciones.

**Reglas y validaciones:** - Cada usuario debe tener un identificador
único. - Los usuarios solo podrán acceder a las funciones autorizadas
para su rol. - Deben establecerse formatos, tamaño máximo y reglas de
almacenamiento para las fotografías. - Las contraseñas no deben
almacenarse en texto plano.

### CU2. Gestionar el registro y envío de solicitud de caso especial

**Objetivo:** permitir que el estudiante registre una solicitud de caso
especial y la envíe formalmente para su evaluación.

**Actor principal:** estudiante.

**Funciones principales:** - Crear una solicitud con los datos y el
motivo del caso especial. - Seleccionar o registrar las materias y
paralelos relacionados, cuando corresponda. - Adjuntar documentación de
respaldo. - Guardar un borrador, si el procedimiento institucional lo
permite. - Validar que la solicitud contenga los datos y documentos
obligatorios. - Enviar la solicitud y generar un número de
seguimiento. - Registrar la fecha, hora y usuario que realizó el
envío. - Enviar un correo de recepción al estudiante y una notificación
al área responsable.

**Reglas y validaciones:** - Una solicitud enviada no debe modificarse
libremente; las correcciones deben quedar controladas. - Debe existir un
estado inicial definido, por ejemplo, `ENVIADA`. - El sistema debe
conservar el historial de cambios de estado. - Deben definirse los tipos
de casos especiales permitidos, los documentos requeridos y los plazos
de presentación.

### CU3. Gestionar la evaluación de solicitud por Dirección de Carrera

**Objetivo:** permitir que la Dirección de Carrera revise la solicitud y
registre una decisión fundamentada.

**Actor principal:** Dirección de Carrera.

**Funciones principales:** - Consultar solicitudes pendientes de
evaluación. - Revisar los datos y documentos adjuntos. - Registrar
observaciones o solicitar documentación adicional. - Aprobar o rechazar
la solicitud, según las atribuciones autorizadas. - Registrar el
responsable, fecha, decisión y justificación. - Notificar por correo al
estudiante el resultado o las observaciones. - Remitir las solicitudes
aprobadas a la siguiente etapa del proceso.

**Reglas y validaciones:** - Solo usuarios autorizados pueden evaluar
solicitudes. - Las decisiones deben guardar quién las tomó y cuándo. -
Un rechazo debe registrar una justificación. - Si se solicitan
correcciones, debe definirse si la solicitud vuelve al estudiante y cómo
se reanuda la evaluación. - La aprobación de Dirección no debe
confundirse con la confirmación o habilitación final de CPD.

### CU4. Gestionar el bloqueo y control de cupos de materias/paralelos

**Objetivo:** controlar la disponibilidad de cupos relacionados con las
solicitudes de casos especiales y evitar asignaciones que excedan los
límites establecidos.

**Actores:** personal académico o responsable autorizado; sistema.

**Funciones principales:** - Consultar materias, paralelos y cupos
disponibles. - Registrar la capacidad máxima, ocupación y
disponibilidad, según la información institucional. - Bloquear o
reservar temporalmente un cupo cuando el procedimiento lo requiera. -
Liberar el cupo cuando una solicitud sea rechazada, cancelada o no se
concrete, según las reglas. - Registrar el motivo, responsable y fecha
de cada bloqueo o liberación. - Validar la disponibilidad antes de
confirmar una asignación. - Notificar por correo cuando corresponda
informar una reserva, liberación o falta de cupos.

**Reglas y validaciones:** - No se debe superar la capacidad autorizada
del paralelo, salvo que exista una excepción institucional explícita. -
Debe distinguirse entre cupos disponibles, reservados y ocupados. - Las
operaciones concurrentes deben impedir que dos solicitudes consuman el
mismo cupo disponible. - Debe definirse en qué etapa se bloquea el cupo
y cuándo se convierte en una asignación definitiva.

### CU5. Gestionar el procesamiento y remisión de casos especiales a CPD

**Objetivo:** preparar y remitir a CPD los casos que hayan cumplido las
etapas previas requeridas.

**Actores:** personal académico o responsable autorizado; CPD como
destinatario.

**Funciones principales:** - Consultar los casos aprobados que estén
pendientes de remisión. - Verificar que los datos y documentos
requeridos estén completos. - Preparar el registro o expediente que se
enviará a CPD. - Registrar la fecha de remisión, responsable,
destinatario y referencia de envío. - Cambiar el estado de la solicitud
a `REMITIDA_A_CPD`, si corresponde. - Enviar un correo de notificación a
CPD con la información necesaria o un enlace seguro al expediente. -
Notificar al estudiante que su caso fue remitido, cuando corresponda. -
Registrar errores de envío y permitir reintentos controlados.

**Reglas y validaciones:** - Solo se deben remitir los casos que cumplan
los requisitos previos. - Debe evitarse la remisión duplicada del mismo
caso. - Los documentos y datos personales deben compartirse únicamente
con destinatarios autorizados. - El correo enviado no debe considerarse
por sí solo una confirmación de recepción o habilitación de CPD.

### CU6. Gestionar el registro de confirmación y habilitación de CPD

**Objetivo:** registrar la respuesta de CPD y dejar constancia de la
confirmación y habilitación del caso especial.

**Actores:** CPD o personal autorizado que registre su respuesta.

**Funciones principales:** - Consultar los casos remitidos pendientes de
respuesta. - Registrar la confirmación de recepción de CPD. - Registrar
el resultado de la revisión: confirmado, observado, rechazado o el
estado que defina la institución. - Registrar la habilitación efectiva,
con su fecha y referencia de respaldo. - Adjuntar o registrar la
constancia correspondiente, si existe. - Actualizar el estado final o
intermedio de la solicitud. - Enviar correo al estudiante y a las áreas
responsables con el resultado. - Registrar las observaciones y las
acciones pendientes.

**Reglas y validaciones:** - Deben diferenciarse la recepción, la
confirmación y la habilitación efectiva. - La habilitación solo debe
registrarse cuando exista la autorización correspondiente. - Se debe
guardar quién registró la respuesta, la fecha y el respaldo
disponible. - Los casos observados deben tener un procedimiento definido
para corregir o completar la información.

### CU7. Gestión de historial

**Objetivo:** mantener la trazabilidad de las operaciones realizadas en
las solicitudes y en los registros relevantes del sistema.

**Actores:** usuarios autorizados según sus permisos.

**Funciones principales:** - Consultar el historial de una solicitud. -
Registrar los cambios de estado, observaciones, decisiones, remisiones y
habilitaciones. - Registrar usuario responsable, fecha y hora de cada
operación. - Consultar el historial de bloqueos, reservas y liberaciones
de cupos. - Registrar los eventos de notificación por correo: pendiente,
enviado, fallido y reintentado, según la implementación. - Permitir
búsquedas por número de solicitud, estudiante, periodo, estado y fechas.

**Reglas y validaciones:** - Los registros históricos no deben poder
modificarse o eliminarse libremente. - El acceso al historial debe
respetar los permisos y la privacidad de los datos. - Debe distinguirse
entre el historial del trámite y los registros técnicos de envío de
correo. - Debe definirse el plazo de conservación de los registros.

### CU8. Reportes y estadísticas

**Objetivo:** generar información consolidada que permita supervisar el
volumen, estado y resultado de las solicitudes de casos especiales.

**Actores:** Director, personal académico y otros usuarios autorizados.

**Funciones principales:** - Consultar el total de solicitudes por
periodo académico. - Generar reportes por estado, tipo de caso, carrera,
materia, paralelo y fecha, cuando esos datos existan. - Mostrar
solicitudes pendientes de evaluación, pendientes de remisión y
pendientes de respuesta de CPD. - Consultar cantidades aprobadas,
rechazadas, observadas y habilitadas. - Consultar estadísticas de cupos
reservados, ocupados y liberados, si se registran esos datos. - Filtrar
reportes por rangos de fechas y otros criterios permitidos. - Exportar a
PDF o Excel, si se requiere. - Restringir los datos mostrados de acuerdo
con el rol del usuario.

**Reglas y validaciones:** - Los indicadores deben calcularse a partir
de los registros oficiales del sistema. - Debe definirse cómo se
contabilizan solicitudes reabiertas, anuladas o con más de una
evaluación. - Los reportes deben indicar el periodo y los filtros
aplicados.

## 4. Flujo general propuesto

1.  El estudiante registra y envía una solicitud de caso especial.
2.  El sistema registra la solicitud y envía el correo de recepción.
3.  La Dirección de Carrera revisa la solicitud y registra su decisión.
4.  Si se requieren correcciones, se notifica al estudiante y se sigue
    el procedimiento definido.
5.  Si se aprueba, se verifica y gestiona la disponibilidad o reserva
    del cupo cuando corresponda.
6.  El responsable autorizado procesa y remite el caso a CPD.
7.  CPD confirma la recepción y registra el resultado y la habilitación,
    según corresponda.
8.  El sistema notifica las novedades relevantes por correo y mantiene
    el historial.
9.  Los usuarios autorizados consultan reportes y estadísticas.

Este flujo es preliminar y deberá ajustarse al procedimiento académico
real, especialmente en el orden entre la aprobación, el bloqueo del cupo
y la habilitación de CPD.

## 5. Estados sugeridos para una solicitud

Los estados definitivos deben validarse con la institución. Como punto
de partida:

-   `BORRADOR`
-   `ENVIADA`
-   `EN_REVISION`
-   `OBSERVADA`
-   `PENDIENTE_CORRECCION`
-   `APROBADA_DIRECCION`
-   `RECHAZADA_DIRECCION`
-   `CUPO_RESERVADO`
-   `PENDIENTE_REMISION_CPD`
-   `REMITIDA_A_CPD`
-   `RECIBIDA_CPD`
-   `OBSERVADA_CPD`
-   `CONFIRMADA_CPD`
-   `HABILITADA`
-   `RECHAZADA_CPD`
-   `CANCELADA`

No es obligatorio implementar todos estos estados. Conviene definir
transiciones válidas para impedir saltos de etapa no autorizados.

## 6. Envío de correos electrónicos

El correo será un mecanismo de notificación del flujo de trabajo. No
debería ser la única fuente de verdad del trámite: el estado oficial
debe guardarse en la base de datos.

### Eventos que podrían generar correos

  -----------------------------------------------------------------------
  Evento                  Destinatario sugerido   Contenido
  ----------------------- ----------------------- -----------------------
  Cuenta creada o         Usuario                 Información de acceso o
  activada                                        activación

  Solicitud enviada       Estudiante              Número de solicitud y
                                                  constancia de recepción

  Nueva solicitud         Dirección de Carrera    Aviso de revisión
  pendiente                                       pendiente y enlace
                                                  seguro

  Solicitud observada     Estudiante              Observaciones y pasos
                                                  para corregir

  Solicitud aprobada o    Estudiante              Resultado y
  rechazada                                       justificación cuando
                                                  corresponda

  Caso remitido a CPD     CPD y, si corresponde,  Referencia del caso y
                          estudiante              enlace seguro

  Confirmación o          Estudiante y áreas      Resultado y fecha
  habilitación registrada responsables            registrada

  Error de envío de       Responsable técnico o   Aviso de fallo para
  correo                  administrativo          seguimiento
  -----------------------------------------------------------------------

### Recomendaciones técnicas

-   Usar una biblioteca Java de correo, por ejemplo, Jakarta Mail, y un
    proveedor SMTP autorizado.
-   Guardar las credenciales SMTP en variables de entorno o un gestor de
    secretos, nunca en el código fuente.
-   Crear plantillas de correo con asunto, contenido y variables
    definidas.
-   Registrar los envíos en una tabla de notificaciones.
-   Implementar reintentos controlados y evitar correos duplicados.
-   No adjuntar información sensible si es suficiente enviar un enlace
    con acceso autenticado.
-   No incluir contraseñas en los correos.
-   Diferenciar el estado del trámite del estado del correo: un correo
    fallido no debe revertir una aprobación o habilitación.
-   Si el sistema necesita entrega fiable, utilizar una cola de
    notificaciones o patrón *outbox* para procesar correos pendientes
    sin perder eventos.

## 7. Orientación inicial para el diseño de la base de datos

La estructura final depende de la base de datos existente. Como
referencia, podrían existir estas entidades:

-   `usuarios`: datos de acceso, identificación, correo, fotografía y
    estado.
-   `roles` y `usuario_roles`: permisos y roles asignados.
-   `estudiantes`: datos académicos propios del estudiante, si no están
    integrados en `usuarios`.
-   `periodos_academicos`: periodos en los que se tramitan solicitudes.
-   `materias`: catálogo de materias.
-   `paralelos`: paralelos y su capacidad autorizada.
-   `inscripciones` o `cupos`: ocupación, reserva y disponibilidad,
    según el modelo institucional.
-   `solicitudes_caso_especial`: datos principales, estudiante
    solicitante, tipo, motivo, fecha y estado.
-   `documentos_solicitud`: archivos o referencias a los documentos
    adjuntos.
-   `evaluaciones_solicitud`: evaluador, decisión, observaciones, fecha
    y justificación.
-   `remisiones_cpd`: datos de remisión, referencia y fecha.
-   `respuestas_cpd`: confirmación, observaciones, decisión y
    habilitación.
-   `historial_solicitud`: cambios de estado y acciones realizadas.
-   `notificaciones_correo`: destinatario, plantilla, evento, estado de
    envío, intentos y fechas.
-   `fotografias_usuario` o un campo de referencia a la imagen
    almacenada, según el diseño.
-   `auditoria`: registro de operaciones relevantes, si se necesita una
    auditoría transversal.

Estas son sugerencias iniciales, no un esquema definitivo. Antes de
crear tablas nuevas, se debe revisar si la base actual ya contiene
entidades equivalentes.

## 8. Criterios generales de seguridad y control

-   Autenticación segura y contraseñas almacenadas mediante hash
    robusto.
-   Autorización por rol y validación de permisos en el servidor.
-   Validación de archivos adjuntos y fotografías: tamaño, extensión y
    tipo real del archivo.
-   Acceso restringido a documentos académicos y datos personales.
-   Consultas parametrizadas y validación de entradas.
-   Registro de acciones críticas y cambios de estado.
-   Protección de credenciales, secretos y configuración SMTP.
-   Copias de seguridad y política de conservación de datos.
-   Validación transaccional de cupos para evitar sobreasignaciones.
-   Accesos a CPD y enlaces de documentos protegidos; evitar enlaces
    públicos permanentes.

## 9. Aspectos que deben confirmarse con la base de datos y la institución

1.  Motor y versión de base de datos.
2.  Tablas, columnas, claves primarias y foráneas que ya existen.
3.  Si los usuarios y datos académicos provienen de otro sistema
    institucional.
4.  Tipos de casos especiales y documentación obligatoria para cada
    tipo.
5.  Si Dirección puede aprobar, observar y rechazar, y quién puede
    registrar cada acción.
6.  Qué significa CPD en el proceso institucional y si accede
    directamente al sistema o responde por correo.
7.  En qué momento se reserva o bloquea el cupo y cuándo se libera.
8.  Si el estudiante puede editar una solicitud enviada o únicamente
    responder observaciones.
9.  Proveedor de correo, dominio remitente y requisitos de
    autenticación.
10. Reportes requeridos y formatos de exportación.
11. Reglas para fotografía, almacenamiento de documentos y conservación
    del historial.

## 10. Próximo paso

Compartir la estructura de la base de datos (por ejemplo, un archivo
`.sql`, un diagrama entidad-relación o una exportación de las tablas y
sus relaciones).

Con esa información se podrá:

-   Revisar la estructura existente.
-   Identificar tablas y campos faltantes.
-   Proponer modificaciones compatibles con los datos actuales.
-   Definir claves primarias, foráneas, restricciones e índices.
-   Diseñar las relaciones entre usuarios, solicitudes, evaluaciones,
    cupos, CPD e historial.
-   Preparar un modelo de datos ajustado al flujo de los ocho casos de
    uso.
-   Proponer la arquitectura Java y el mecanismo de envío de correos
    según el entorno elegido.

------------------------------------------------------------------------

# 11. Revisión preliminar del diagrama de base de datos

La revisión se realizó a partir de la imagen del diagrama. Como algunos
nombres, campos y cardinalidades no se distinguen con suficiente
claridad, estas observaciones son preliminares y deben confirmarse con
el archivo SQL o una exportación legible del modelo.

## 11.1 Tablas que ya existen y que conviene reutilizar

  ------------------------------------------------------------------------
  Tabla visible en el      Uso probable en los     Observación
  diagrama                 casos de uso            
  ------------------------ ----------------------- -----------------------
  `usuario`                CU1, CU7, CU8           Ya incluye datos
                                                   personales, CI, correo,
                                                   teléfono, contraseña y
                                                   fechas de auditoría.
                                                   Revisar si el campo de
                                                   contraseña guarda
                                                   únicamente un hash.

  `roles`                  CU1                     Permite definir
                                                   perfiles. Verificar
                                                   cómo se relacionan los
                                                   usuarios con uno o
                                                   varios roles.

  `estudiantes`            CU1, CU2, CU8           Mantener separada la
                                                   información académica
                                                   del perfil general si
                                                   así lo requiere el
                                                   modelo.

  `personal_carrera`       CU1, CU3, CU5, CU7      Puede representar al
                                                   personal que registra o
                                                   gestiona trámites.
                                                   Conviene precisar sus
                                                   permisos.

  `director_carrera`       CU3                     Parece representar al
                                                   responsable de
                                                   evaluación. Verificar
                                                   la relación con usuario
                                                   y carrera.

  `solicitud`              CU2, CU3, CU5, CU6,     Es la entidad central.
                           CU7, CU8                Tiene código, estado,
                                                   motivo, fechas,
                                                   revisor, motivo de
                                                   rechazo, observaciones
                                                   y auditoría básica.

  `archivo_solicitud`      CU2, CU3, CU5           Puede guardar archivos
                                                   adjuntos. Revisar si
                                                   tiene una clave foránea
                                                   hacia `solicitud` y si
                                                   admite varios archivos
                                                   por solicitud.

  `notificaciones`         CU2, CU3, CU5, CU6, CU7 Ya contempla tipo,
                                                   asunto, mensaje, canal,
                                                   estado, fecha de envío,
                                                   lectura y creación.
                                                   Verificar que registre
                                                   errores e intentos de
                                                   envío.

  `oficio_cpd`             CU5                     Puede almacenar el
                                                   oficio o expediente
                                                   remitido a CPD. Revisar
                                                   si se vincula
                                                   explícitamente con la
                                                   solicitud y quién lo
                                                   genera.

  `oficio_respuesta_cpd`   CU6                     Puede registrar el
                                                   estado, las
                                                   observaciones y el
                                                   archivo de respuesta de
                                                   CPD. Debe quedar
                                                   relacionado con el
                                                   oficio y la solicitud
                                                   correspondiente.

  `bloqueo_grupo`          CU4, CU7                Incluye motivo, estado,
                                                   autorización, fecha de
                                                   resolución y
                                                   observación. Verificar
                                                   la relación con grupo y
                                                   solicitud.

  `grupo`                  CU4                     Ya incluye código, cupo
                                                   máximo, cupos
                                                   disponibles, cupo
                                                   excepcional máximo y
                                                   bloqueo. Definir cómo
                                                   se calculan y
                                                   actualizan esos
                                                   valores.

  `materia`, `carrera`,    CU4, CU8                Proporcionan el
  `materia_carrera`                                catálogo académico y la
                                                   relación entre materias
                                                   y carreras.

  `periodos`,              CU4, CU8                Permiten organizar la
  `periodo_grupo`                                  oferta por periodo
                                                   académico.

  `horario_grupo`          CU4                     Contiene día, hora de
                                                   inicio, hora de fin y
                                                   aula.

  `inscripcion`,           CU1, CU4, CU8           Pueden servir para
  `inscripcion_grupo`,                             verificar la situación
  `estudiante_carrera`                             académica y la
                                                   ocupación de grupos.
                                                   Debe confirmarse su
                                                   función exacta.

  `solicitud_grupo`        CU2, CU4                Puede relacionar una
                                                   solicitud con uno o
                                                   varios grupos y guardar
                                                   motivo, estado y motivo
                                                   de rechazo. Confirmar
                                                   su cardinalidad y si
                                                   admite varios grupos
                                                   por solicitud.

  `listas_aprobados`,      CU4, CU5, CU8           Parecen gestionar
  `lista_detalle`,                                 listados, detalles y
  `pago_adicion`                                   pagos relacionados con
                                                   el proceso. Confirmar
                                                   las reglas de negocio
                                                   antes de modificarlas.

  `reporte`                CU8                     Registra tipo y fecha
                                                   de generación. Puede
                                                   servir como historial
                                                   de reportes generados,
                                                   aunque los datos
                                                   estadísticos deben
                                                   consultarse desde las
                                                   tablas operativas.
  ------------------------------------------------------------------------

## 11.2 Ajustes recomendados

### A. Historial de solicitudes

La tabla `solicitud` ya contiene fechas y campos de estado, pero esos
campos por sí solos no conservan todas las transiciones históricas. Se
recomienda crear `historial_solicitud` con, al menos:

-   `id`
-   `solicitud_id` (clave foránea)
-   `usuario_id` (quién realizó la acción)
-   `estado_anterior`
-   `estado_nuevo`
-   `accion`
-   `observacion`
-   `fecha_evento`

Así se podrá reconstruir quién envió, evaluó, observó, aprobó, rechazó o
remitió cada solicitud y cuándo ocurrió.

### B. Evaluaciones de Dirección de Carrera

Actualmente, `solicitud` parece contener `revisado_por`,
`motivo_rechazo` y `observaciones`. Si cada solicitud tendrá una única
evaluación definitiva, puede bastar inicialmente con esos campos más la
fecha de evaluación y una decisión explícita. Si se permiten varias
revisiones, devoluciones o reevaluaciones, conviene una tabla
`evaluacion_solicitud` relacionada con `solicitud` y `usuario`.

La decisión debe manejar valores controlados, por ejemplo: `APROBADA`,
`OBSERVADA` o `RECHAZADA`, según el procedimiento institucional.

### C. Notificaciones por correo

La tabla `notificaciones` es una buena base. Verificar o añadir, si
faltan:

-   `solicitud_id` (opcional para avisos que no correspondan a una
    solicitud).
-   `destinatario`
-   `plantilla` o `evento`
-   `intentos`
-   `ultimo_error`
-   `fecha_proximo_intento`, si se implementan reintentos programados.
-   `enviado_en`
-   `leido_en`, si se necesita confirmar lectura dentro de la
    aplicación.

Conviene que el estado del envío distinga al menos `PENDIENTE`,
`ENVIADO` y `FALLIDO`. La confirmación SMTP significa que el servidor
aceptó el mensaje, no que el destinatario lo leyó. El estado de correo
debe mantenerse separado del estado académico de la solicitud.

### D. Relación entre solicitud, oficio y respuesta de CPD

Asegurar que `oficio_cpd` tenga una relación explícita con `solicitud` y
que `oficio_respuesta_cpd` esté relacionado con el oficio al que
responde. Si un caso puede tener más de un oficio o respuesta, el modelo
debe admitirlo sin sobrescribir respuestas anteriores.

Registrar fechas diferenciadas para: - preparación del oficio; - envío o
remisión; - confirmación de recepción; - respuesta de CPD; -
habilitación efectiva.

No se debe considerar habilitado un caso únicamente porque se generó o
envió un correo.

### E. Control de cupos

`grupo` ya tiene campos de capacidad y disponibilidad y existe
`bloqueo_grupo`. Antes de agregar más tablas, definir el significado
exacto de cada campo:

-   capacidad máxima autorizada;
-   cupos ocupados por inscripciones confirmadas;
-   cupos reservados temporalmente por solicitudes;
-   cupos disponibles;
-   límite excepcional, si la institución lo autoriza.

Si `cupos_disponible` se almacena, debe actualizarse dentro de
transacciones y validarse frente a las inscripciones y reservas para
evitar inconsistencias. Definir cuándo se crea y cuándo se libera un
bloqueo, y registrar la solicitud que lo originó.

### F. Usuarios, roles y fotografías

La tabla `usuario` y la tabla `roles` cubren parte del CU1. Confirmar si
un usuario puede tener más de un rol. Si sí, se recomienda una tabla
intermedia `usuario_rol`; si cada usuario tiene exactamente un rol, una
clave foránea en `usuario` podría ser suficiente.

Para las fotografías, se puede almacenar una ruta o clave de archivo en
la base de datos y guardar el archivo en un almacenamiento protegido. No
es necesario guardar la imagen como texto Base64 en una columna. También
deben validarse el tipo real y el tamaño del archivo.

### G. Reportes

La tabla `reporte` puede conservar datos de generación, pero los totales
y estadísticas deberían calcularse a partir de las solicitudes,
evaluaciones, grupos, remisiones y respuestas de CPD. Si se guardan
archivos de reportes, conservar su ruta, tipo, usuario que lo generó y
fecha.

## 11.3 Matriz de cobertura preliminar

  -----------------------------------------------------------------------
  Caso de uso             Cobertura aparente      Trabajo pendiente
  ----------------------- ----------------------- -----------------------
  CU1. Usuarios y fotos   Parcial                 Confirmar roles,
                                                  permisos, fotografía y
                                                  recuperación de acceso.

  CU2. Registrar y enviar Parcial/alta            Validar campos
  solicitud                                       obligatorios, adjuntos,
                                                  número de seguimiento y
                                                  notificación.

  CU3. Evaluación por     Parcial                 Confirmar decisión,
  Dirección                                       fecha, autorización y
                                                  manejo de
                                                  observaciones.

  CU4. Bloqueo y cupos    Parcial/alta            Definir reservas,
                                                  ocupación, liberación y
                                                  consistencia
                                                  transaccional.

  CU5. Remisión a CPD     Parcial                 Confirmar relación de
                                                  oficio con solicitud y
                                                  trazabilidad de envío.

  CU6. Confirmación y     Parcial                 Confirmar relaciones,
  habilitación de CPD                             fechas y estados
                                                  separados para
                                                  recepción y
                                                  habilitación.

  CU7. Historial          Parcial                 Incorporar historial de
                                                  eventos y cambios de
                                                  estado si no existe
                                                  otra tabla equivalente.

  CU8. Reportes y         Parcial                 Definir indicadores,
  estadísticas                                    filtros, permisos y
                                                  formatos de
                                                  exportación.
  -----------------------------------------------------------------------

## 11.4 Qué necesito para validar el diseño

La imagen permite identificar las entidades principales, pero no
garantiza que se puedan leer todos los tipos de datos, claves foráneas,
restricciones y cardinalidades. Para recomendar cambios exactos,
compartir preferentemente:

1.  El archivo SQL de creación de tablas (`CREATE TABLE`), con claves
    foráneas e índices; o
2.  Una exportación del esquema desde el gestor de base de datos; o
3.  El archivo original editable del diagrama.

Si el SQL contiene datos personales reales, basta con enviar la
estructura sin los registros. Con el esquema se podrá indicar qué
columnas agregar, cuáles reutilizar y qué relaciones corregir, evitando
duplicar funcionalidades.
