# Comandos del sistema por CU

Los comandos se envían en el **Subject/Asunto** del correo. Los parámetros se
separan con comas. Cuando un parámetro de texto contiene comas, colóquelo entre
comillas invertidas: `` `texto, con comas` ``.

Los datos de los ejemplos son ficticios y solo sirven como referencia. Las
operaciones que incluyen archivos requieren que el archivo esté adjunto al
correo.

## 1. Ayuda, registro y acceso

### Ayuda general

Comandos tal cual:

```text
help
ayuda
acceder ayuda
acceder comandos
acceder perfil
acceder salir
```

Ejemplos:

```text
help
acceder sistema ana.lopez@example.com,ClaveDemo#48
acceder comandos
acceder perfil
acceder salir
```

`help`, `ayuda` y `acceder ayuda` muestran la ayuda pública. `acceder
comandos` muestra los comandos habilitados para la sesión activa y `acceder
perfil` muestra el usuario autenticado.

### Registro público de estudiante

Comandos tal cual:

```text
estudiante registrar nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,registro_universitario,id_carrera,semestre
estudiante register nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,registro_universitario,id_carrera,semestre
```

Ejemplo:

```text
estudiante registrar Valeria,Quispe Rojas,7845123, valeria.quispe@example.com,72123456,Avenida Las Palmeras 245,2003-08-17,Valeria#2026, RU-2026-1842,3,4
```

`register` es el alias en inglés de `registrar`. El registro crea el usuario y
le asigna automáticamente el rol `ESTUDIANTE`.

### Inicio y cierre de sesión

Comandos tal cual:

```text
acceder sistema correo,password
acceder salir
token CREDENCIAL | comando
```

Ejemplos:

```text
acceder sistema valeria.quispe@example.com,Valeria#2026
acceder salir
token AbCdEfGhIjKlMnOpQrStUvWxYz0123456789_-AbC | solicitud mis_casos
```

La sesión dura dos horas y queda asociada al correo remitente. El prefijo
`token` permite ejecutar un comando usando una credencial explícita.

### Ayuda por rol

#### Estudiante

Comandos tal cual:

```text
estudiante help
solicitud help
solicitud_grupo help
archivo_solicitud help
pago help
```

Ejemplos:

```text
estudiante help
solicitud help
pago help
```

#### Director de carrera y personal académico

Estos roles tienen acceso administrativo a los módulos. Los comandos de ayuda
disponibles son:

```text
estudiante help
personal_carrera help
director_carrera help
solicitud help
solicitud_grupo help
archivo_solicitud help
pago help
evaluacion help
carrera help
periodo help
materia help
grupo help
bloqueo_grupo help
oficio_cpd help
respuesta_cpd help
historial help
notificacion help
reporte help
```

Ejemplos:

```text
evaluacion help
bloqueo_grupo help
reporte help
```

#### CPD

Comandos tal cual:

```text
cpd help
respuesta_cpd help
```

Ejemplos:

```text
cpd help
respuesta_cpd help
```

## 2. CU1 - Gestión de usuarios y roles

### Comandos tal cual

```text
usuario listar
estudiantes listar
academicos listar
directores listar
cpd listar

estudiante agregar nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,registro_universitario,id_carrera,semestre
estudiante editar id,nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,estado,registro_universitario,id_carrera,semestre

academico agregar nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,cargo,id_carrera
academico editar id,nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,estado,cargo,id_carrera

director agregar nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,id_carrera,gestion_inicio,gestion_fin
director editar id,nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,estado,id_carrera,gestion_inicio,gestion_fin

cpd agregar nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password
cpd editar id,nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,estado

estudiante listar
personal_carrera listar
personal_carrera agregar id_usuario,cargo,id_carrera
director_carrera listar
director_carrera agregar id_usuario,id_carrera,gestion_inicio,gestion_fin
```

Ejemplos:

```text
usuario listar
estudiante agregar Mateo,Salvatierra Núñez,6938142,m.salvatierra@example.com,76543210,Calle Aroma 118,2002-11-04,Mateo#2026,RU-2026-0921,3,6
estudiante editar 42,Mateo,Salvatierra Núñez,6938142,m.salvatierra@example.com,76543210,Calle Aroma 118,2002-11-04,,ACTIVO,RU-2026-0921,3,6
academico agregar Julia,Menacho Flores,5129078,julia.menacho@example.com,70987654,Calle Beni 602,1987-02-22,Julia#Acad26,Docente Titular,3
director agregar Rodrigo,Arce Molina,4382167,rodrigo.arce@example.com,70112233,Avenida Busch 901,1979-06-15,Rodrigo#Dir26,3,2026-01-15,2026-12-20
cpd agregar Camila,Peña Vargas,6094831,camila.pena@example.com,71223344,Calle Potosí 77,1985-09-28,Camila#Cpd26
```

En una edición, deje vacío `password` para conservar la contraseña actual.

## 3. CU2 - Registro y envío de solicitud

### Comandos tal cual

```text
enviar solicitud gestion,tipo_caso,motivo
solicitar materia_grupo id_solicitud,periodo,sigla_materia,sigla_grupo,motivo
solicitud registrar gestion,tipo_caso,motivo
solicitud mis_casos [registro,password]
solicitud pendientes [registro,password]
solicitud_grupo listar id_solicitud
archivo_solicitud adjuntar id_solicitud,tipo_documento
archivo_solicitud listar id_solicitud
pago adjuntar id_solicitud,COMPROBANTE_PAGO
pago listar
mis_pagos listar
```

Ejemplos:

```text
enviar solicitud 2-2026,CONFLICTO_DE_HORARIO,`Cruce entre la materia MAT-301 y una práctica profesional obligatoria`
solicitar materia_grupo 87,2-2026,MAT-301,A,`Necesito cursar este grupo para completar mi semestre`
solicitud registrar 2-2026,SALUD,`Requiero habilitación excepcional por tratamiento médico`
solicitud mis_casos
solicitud_grupo listar 87
archivo_solicitud adjuntar 87,CERTIFICADO_MEDICO
pago adjuntar 87,COMPROBANTE_PAGO
pago listar
```

Adjunte el documento al correo para `archivo_solicitud adjuntar` y exactamente
un comprobante para `pago adjuntar`.

## 4. CU3 - Evaluación por dirección de carrera

### Comandos tal cual

```text
solicitud listar periodo
solicitud periodos id_periodo_o_gestion
solicitud pendientes listar,periodo
solicitud consultar id_solicitud
evaluacion listar
evaluacion registrar id_solicitud,APROBADA|RECHAZADA|OBSERVADA,motivo[,observaciones]
```

Ejemplos:

```text
solicitud listar 2-2026
solicitud periodos 2-2026
solicitud pendientes listar,2-2026
solicitud consultar 87
evaluacion listar
evaluacion registrar 87,APROBADA,`La documentación presentada cumple los requisitos`,`Se autoriza la inscripción en el grupo MAT-301-A`
```

## 5. CU4 - Estructura académica y bloqueo de cupos

### Comandos tal cual

```text
carrera listar
carrera agregar codigo,nombre,facultad

periodo listar
periodo agregar gestion,fecha_inicio,fecha_fin

materia listar
materia agregar sigla,nombre,nivel_semestre,creditos

grupo listar
grupo consultar id_grupo
grupo agregar id_materia,id_periodo,sigla_grupo,docente,cupo_maximo,cupo_excepcional_maximo

bloqueo_grupo listar
bloqueo_grupo bloquear id_grupo,id_solicitud,cantidad,motivo
bloqueo_grupo liberar id_bloqueo,observacion
```

Ejemplos:

```text
carrera agregar INF,Ingeniería Informática,Facultad de Ingeniería
periodo agregar 2-2026,2026-08-03,2026-12-18
materia agregar MAT-301,Matemática Discreta,3,5
grupo agregar 12,4,A,Andrea Valdez,35,4
grupo consultar 19
bloqueo_grupo bloquear 19,87,1,`Reserva de cupo por caso especial aprobado`
bloqueo_grupo listar
bloqueo_grupo liberar 31,`El estudiante confirmó su inscripción`
```

## 6. CU5 - Procesamiento y remisión a CPD

### Comandos tal cual

```text
oficio_cpd listar
oficio_cpd remitir gestion,correo_cpd,referencia,detalle
oficio_cpd generar id_oficio_pendiente
```

Ejemplos:

```text
oficio_cpd listar
oficio_cpd remitir 2-2026,cpd@universidad.edu.bo,Remisión de casos especiales,`Solicitudes aprobadas de la gestión 2-2026`
oficio_cpd generar 14
```

El comando `remitir` genera el CSV y adjunta los comprobantes de la gestión
indicada.

## 7. CU6 - Confirmación y habilitación por CPD

### Comandos tal cual

```text
cpd pendientes
cpd oficios
oficio_cpd consultar id_oficio
cpd responder id_oficio,id_solicitud,HABILITADO|OBSERVADO|RECHAZADO,codigo_respuesta,observacion
respuesta_cpd listar
```

Ejemplos:

```text
cpd pendientes
cpd oficios
oficio_cpd consultar 14
cpd responder 14,87,HABILITADO,CPD-2026-00418,`Se habilita la inscripción excepcional en MAT-301-A`
respuesta_cpd listar
```

## 8. CU7 - Historial y notificaciones

### Comandos tal cual

```text
historial listar
historial consultar id_solicitud
notificacion listar
```

Ejemplos:

```text
historial listar
historial consultar 87
notificacion listar
```

## 9. CU8 - Reportes y estadísticas

### Comandos tal cual

```text
reporte general
reporte estados
reporte periodos
reporte cupos
reporte help
```

Ejemplos:

```text
reporte general
reporte estados
reporte periodos
reporte cupos
```
