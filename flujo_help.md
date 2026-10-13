# GUÍA DE COMANDOS POR CORREO ELECTRÓNICO (FICCT - UAGRM)
## SISTEMA DE GESTIÓN DE CASOS ESPECIALES ACADÉMICOS - GRUPO 01 SA

Para interactuar con el sistema a través de correo electrónico, envíe el comando deseado en el **SUBJECT** (Asunto) del mensaje enviado a:
`grupo01sa@tecnoweb.org.bo`

---

### CU1. Gestión de Usuarios y Roles (Director, Académico, Estudiantes y CPD)
```text
usuario help
usuario listar
usuario agregar nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento(yyyy-MM-dd),password
usuario modificar id,nombre,apellidos,ci,email,telefono,direccion,fecha_nacimiento,password,estado
usuario eliminar id

rol help
rol listar
rol agregar nombre,descripcion
rol asignar id_usuario,id_rol

estudiante help
estudiante listar
estudiante agregar id_usuario,registro_universitario,id_carrera,semestre_actual

personal_carrera help
personal_carrera listar
personal_carrera agregar id_usuario,cargo,id_carrera

director_carrera help
director_carrera listar
director_carrera agregar id_usuario,id_carrera,gestion_inicio(yyyy-MM-dd),gestion_fin(yyyy-MM-dd)
```

---

### CU2. Gestión del Registro y Envío de Solicitud de Caso Especial
```text
solicitud help
solicitud listar
solicitud mis_casos
solicitud mis_casos registro,password
solicitud pendientes
solicitud pendientes registro,password
solicitud registrar id_estudiante,id_periodo,tipo_caso,motivo
solicitud registrar id_periodo,tipo_caso,motivo
solicitud consultar id_solicitud

solicitud_grupo agregar id_solicitud,id_grupo,motivo
solicitud_grupo listar id_solicitud

archivo_solicitud agregar id_solicitud,nombre_archivo,tipo,ruta_o_descripcion
archivo_solicitud listar id_solicitud
```

---

### CU3. Gestión de la Evaluación de Solicitud por Dirección de Carrera
```text
evaluacion help
evaluacion listar
evaluacion registrar id_solicitud,id_evaluador,decision(APROBADA|RECHAZADA|OBSERVADA),justificacion,observaciones
```

---

### CU4. Gestión del Bloqueo y Control de Cupos de Materias y Paralelos
```text
carrera help
carrera listar
carrera agregar codigo,nombre,facultad

periodo help
periodo listar
periodo agregar gestion,fecha_inicio,fecha_fin

materia help
materia listar
materia agregar sigla,nombre,nivel_semestre,creditos

grupo help
grupo listar
grupo consultar id_grupo
grupo agregar id_materia,id_periodo,sigla_grupo,docente,cupo_maximo,cupo_excepcional_maximo

bloqueo_grupo help
bloqueo_grupo listar
bloqueo_grupo bloquear id_grupo,id_solicitud,cantidad,motivo,id_autorizador
bloqueo_grupo liberar id_bloqueo,observacion
```

---

### CU5. Gestión del Procesamiento y Remisión de Casos Especiales a CPD
```text
oficio_cpd help
oficio_cpd listar
oficio_cpd generar id_solicitud,destinatario,referencia,detalle,id_responsable
```

---

### CU6. Gestión del Registro de Confirmación y Habilitación de CPD
```text
respuesta_cpd help
respuesta_cpd listar
respuesta_cpd registrar id_oficio,id_solicitud,resultado(HABILITADA|CONFIRMADA_CPD|OBSERVADA_CPD|RECHAZADA_CPD),referencia_respaldo,observaciones,id_responsable
```

---

### CU7. Gestión de Historial y Trazabilidad
```text
historial help
historial listar
historial consultar id_solicitud

notificacion listar
```

---

### CU8. Reportes y Estadísticas Consolidadas
```text
reporte help
reporte general
reporte estados
reporte periodos
reporte cupos
```
