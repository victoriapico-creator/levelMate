# Validación de LevelMate

## Compilación y pruebas automáticas

Resultados de la compilación con Firebase del 24/11/2026, utilizando las versiones del repositorio y la configuración Android real:

| Comprobación | Resultado |
|---|---|
| Pruebas unitarias | 37 correctas, 0 fallos, 0 errores y 0 omitidas; 8 suites |
| APK debug | Compilado con Authentication y Firestore |
| Google Services | `processDebugGoogleServices` completada |
| Lint | 0 errores, 13 advertencias y 3 sugerencias |
| APK instrumentado | Compilado correctamente |
| Versiones | Catálogo, plugins raíz y wrapper conservados respecto al esqueleto |

Comando de compilación y comprobación:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest
```

La compilación utilizó SDK 37, Build Tools 36.0.0 y JVM 25. Las 37 pruebas incluyen validación de proyectos, ajustes, cálculo del nivel, horario de recordatorios, plantillas, 13 casos de intercambio por archivos y 6 de proyectos compartidos.

### Pruebas en el dispositivo

El 25/11/2026 se ejecutó en Windows, con el emulador abierto:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

El resultado final comunicado fue `BUILD SUCCESSFUL`. La ejecución se realizó en el equipo de desarrollo con un Pixel 6 virtual Android 14/API 34; el informe XML y el recuento individual de resultados no están adjuntos a este registro.

La clase `ProjectDatabaseTest` comprueba la base vacía, el proyecto inexistente, el borrado con conservación de fotos generales, el rechazo de claves foráneas inválidas, la creación sin datos parciales y la exclusión de proyectos terminados del recordatorio.

## Firebase

| Configuración | Valor comprobado |
|---|---|
| Proyecto | `levelmate-56e8c` |
| Plan | Spark, sin facturación vinculada |
| App Android | `com.example.notesbysubject` |
| Authentication | Correo electrónico y contraseña |
| Firestore | Standard, base `(default)`, región `europe-southwest1`, creada en producción |
| Reglas | `firestore.rules` publicadas, con acceso por propietario y participantes |

`app/google-services.json` contiene la configuración Android del proyecto. Las fotos permanecen en el dispositivo; no se utiliza Firebase Storage.

### Reglas y servicios

| Prueba | Resultado | Cobertura |
|---|---|---|
| Emulador local de Firestore | 21 comprobaciones correctas | Publicación con `arrayUnion`, conservación y límite de participantes, propietario, consulta filtrada, rechazo de campos inesperados, accesos anónimos y usuarios ajenos |
| Servicios reales mediante HTTPS REST | 9 comprobaciones correctas | Alta y acceso de dos cuentas temporales, contraseña incorrecta, publicación, lectura del destinatario y permisos |

El documento y las dos cuentas temporales se eliminaron al terminar, con HTTP 200 en las tres operaciones. Los tokens y contraseñas de prueba se mantuvieron en memoria y no forman parte del repositorio.

El script `scripts/check-firestore-rules.mjs` solo admite localhost y el proyecto de demostración `demo-levelmate`.

## Pruebas manuales

Recorridos comprobados durante el desarrollo. Las primeras pruebas se hicieron con un Pixel 6 virtual API 37 y las pruebas finales con Android 14/API 34.

| Función | Comprobación | Resultado |
|---|---|---|
| Proyecto | Crear `Test`, introducir descripción y fecha y abrir el detalle | Datos visibles en el detalle |
| Materiales | Añadir `Madera · 2 tablas` | Material y cantidad guardados |
| Pasos | Marcar un paso y detener/abrir la app | Estado conservado |
| Fotos | Tomar una captura y detener/abrir la app | Foto guardada y visible |
| Ajustes | Guardar hora 18:30 y tolerancia 2; probar 25:00 | Persistencia de valores válidos y rechazo de la hora inválida |
| Compartir foto | Abrir el intent desde una foto del detalle | Selector de aplicaciones receptoras abierto |
| Archivos | Exportar e importar un proyecto | Copia independiente con materiales y pasos |
| Firestore | Usar dos cuentas, compartir con el código destinatario e importar una copia local | Intercambio y conservación del contenido correctos |
| Acceso incorrecto | Probar contraseña incorrecta y después la correcta | Recorrido de error y recuperación confirmado en conjunto; sin captura individual |
| Recordatorio | Elegir una hora futura, conceder permiso y dejar un proyecto vencido con un paso pendiente | Notificación recibida |
| Plantillas grabadas | Cargar el catálogo incluido y crear un proyecto | Materiales y pasos disponibles sin red |
| Plantillas por internet | Compilar con `USE_RECORDED_TEMPLATES = false` y cargar con conexión | Descarga real confirmada |
| Fallback de plantillas | Mantener `false`, activar modo avión y apagar Wi-Fi | Aviso de la copia incluida y creación de proyecto sin conexión |

En la prueba del recordatorio se utilizó el reloj del emulador en formato de 24 horas. WorkManager puede retrasar el aviso; no funciona como una alarma exacta.

## Publicación

El [pull request #1](https://github.com/victoriapico-creator/levelMate/pull/1) se incorporó a `main` el 25/11/2026. El código de las seis tareas A4, A3, A5, A2, A7 y A6 está integrado en la rama principal.

El [catálogo remoto de plantillas](https://raw.githubusercontent.com/victoriapico-creator/levelMate/codex/levelmate-local/docs/templates.json) devolvió HTTP 200, versión 1 y tres plantillas, con el mismo contenido que la copia de assets. La dirección utiliza `codex/levelmate-local`, por lo que esa rama se conserva. El modo inicial es `USE_RECORDED_TEMPLATES = true`.

## Alcance de la validación

- La compilación automática, las comprobaciones del backend y los recorridos manuales se registran por separado.
- Los casos de permiso de notificaciones denegado, ausencia de proyectos pendientes y trabajo único están contemplados en el código. Este registro no incluye una captura manual individual de cada caso.
- La precisión del nivel físico y la calidad del OCR requieren una comprobación visual específica; no se añaden aquí resultados nuevos de esas pruebas.
- El APK disponible es debug. El bundle `.aab` firmado de C2 sigue pendiente.
