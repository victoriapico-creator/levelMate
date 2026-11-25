# Validación de LevelMate con Firebase

Implementación y pruebas automáticas SH-04 del 24/11/2026; cierre de pruebas manuales SH-05 del 25/11/2026. La persona pidió recuperar la A6 original después de probar la variante local. Esta entrega conserva también el intercambio mediante archivos.

## Comprobaciones automáticas de Android

Se ejecutaron con las versiones existentes y el `app/google-services.json` real:

```text
:app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest
BUILD SUCCESSFUL
```

| Comprobación | Resultado |
|---|---|
| Pruebas unitarias | 37 correctas, 0 fallos, 0 errores, 0 omitidas; 8 suites |
| APK debug | Compilado correctamente con Authentication y Firestore |
| Google Services | `processDebugGoogleServices` completada con la configuración Android real |
| Lint | 0 errores; 13 advertencias y 3 sugerencias no bloqueantes |
| APK instrumentado | Compilado; no ejecutado en un dispositivo desde este entorno |
| Versiones y plugins raíz | Catálogo, wrapper y declaraciones raíz conservados respecto al esqueleto |
| Navegación | `AppNavGraph.kt` conservado respecto a la entrega local SH-03 |

Se usaron SDK 37.0, Build Tools 36.0.0 y JVM 25 en herramientas temporales externas al repositorio. Los ajustes de red, certificados y cachés de este entorno no se incorporan al proyecto.

Las 37 pruebas incluyen validación de proyectos/materiales/pasos, cálculos del nivel, ajustes, horario de recordatorios, plantillas reales/grabadas, 13 pruebas de archivos y 6 pruebas de serialización/validación de proyectos compartidos. Los datos compartidos excluyen fotos, rutas privadas e identificadores locales de las entidades relacionadas. Las pruebas de Room que necesitan Android están en el APK instrumentado; compilarlas no equivale a ejecutarlas.

## Backend real configurado

- Proyecto: **LevelMate**, ID `levelmate-56e8c`, plan **Spark** sin facturación vinculada.
- App Android: `com.example.notesbysubject`; configuración descargada por la persona y validada antes de copiarla a `app/google-services.json`.
- Authentication: correo y contraseña habilitado.
- Firestore: edición Standard, base `(default)`, región `europe-southwest1` (Madrid), creada en **modo producción**.
- Reglas de `firestore.rules` publicadas y verificadas en la consola. Acceso por propietario/participantes; el resto de rutas se deniega.
- No se utiliza Firebase Storage, Blaze ni una cuenta de servicio.

### Reglas con emulador

`firebase emulators:exec --only firestore --project demo-levelmate "node scripts/check-firestore-rules.mjs"` completó **21 comprobaciones**. El script se niega a ejecutarse fuera de localhost y del proyecto de demostración.

Incluye primera publicación sin leer un documento todavía inexistente, creación/actualización con arrayUnion y máscara equivalente a `SetOptions.merge()`, conservación de participantes, máximo de 10 personas, exclusión del propietario inválida, duplicados y tipos inválidos, rechazo de rutas de fotos/campos inesperados, prohibición de apropiarse del espacio de documentos de otro usuario, lecturas limitadas a miembros, consulta filtrada permitida, consulta general denegada, modificación/borrado exclusivo del propietario y rechazo de acceso anónimo/ajeno.

### Prueba contra los servicios reales

Pasaron **9 comprobaciones mediante HTTPS REST**: alta de dos cuentas temporales, inicio de sesión correcto, rechazo de contraseña incorrecta, publicación por el propietario, lectura por destinatario, rechazo de acceso anónimo, rechazo de edición por destinatario y consulta de pertenencia válida.

El documento temporal y ambas cuentas se eliminaron al terminar; las tres operaciones de limpieza devolvieron HTTP 200. Las contraseñas y tokens se generaron o recibieron solo en memoria y no forman parte del código, logbook ni entrega. Esta prueba comprueba el backend y las reglas reales; no se presenta como una prueba visual de la pantalla Android.

## Pruebas que ha confirmado la persona

En el Pixel 6 virtual API 37 de su Android Studio, antes de SH-04:

| Recorrido | Resultado comunicado |
|---|---|
| Arranque de LevelMate | Funciona tras un Cold Boot del emulador; hubo bloqueos iniciales de Pixel Launcher/System UI |
| Proyecto `Test`, descripción y fecha | Creado y abierto en el detalle |
| Material `Madera · 2 tablas` | Guardado y visible |
| Paso `pegar tablas` marcado | Estado conservado al detener y volver a ejecutar |
| Foto del proyecto | Capturada con la escena virtual y conservada al volver a ejecutar |
| Ajustes | Hora 18:30 y tolerancia 2 persistentes; 25:00 rechazada |
| Plantillas grabadas | Carga y creación de un proyecto con materiales/pasos correctas |
| Compartir foto | Menú de aplicaciones receptoras abierto |
| Archivo de proyecto | Exportación e importación como copia independiente con materiales y pasos |

## Cierre manual confirmado el 25/11/2026

Tras bloqueos del sistema del antiguo emulador antes de ejecutar la app, la persona creó otro Pixel 6 virtual con Android 14/API 34 y confirmó que funcionaba. Estas son sus comprobaciones comunicadas en la conversación; no se presentan como tests instrumentados ejecutados por Codex.

| Fila | Recorrido confirmado por la persona | Resultado |
|---|---|---|
| A6 | Crear e iniciar sesión con dos cuentas, compartir un proyecto con el código de la segunda cuenta y guardar una copia local | La persona confirmó que el intercambio y la copia conservan los datos del proyecto |
| A6, caso incómodo | El recorrido guiado incluyó intentar entrar con contraseña incorrecta y recuperar el acceso con la correcta | La persona confirmó el recorrido en conjunto; no se dispone de captura individual de cada resultado |
| A7 | Guardar una hora futura, con permisos y recordatorio activados, en un proyecto con fecha de hoy y un paso pendiente | La persona confirmó que recibió la notificación |
| A5 | Cambiar una línea a modo real y pulsar Load templates con conexión | La persona confirmó la carga desde internet |
| A5, caso incómodo | Modo avión y Wi-Fi apagado, volver a cargar y crear desde una plantilla | La persona confirmó el fallback a la copia de assets y el funcionamiento sin conexión |

En la primera comprobación de A7, la captura mostraba `18:05` como hora elegida cuando el emulador estaba alrededor de `00:40`. Se explicó la diferencia entre el campo de hora programada y el reloj actual en formato de 24 horas. Después de guardar una hora futura, la persona comunicó que el aviso había llegado. No hizo falta modificar el código del recordatorio.

La persona publicó la rama con Git desde su ordenador. Codex verificó que `codex/levelmate-local` contiene la actualización Firebase y que el endpoint público devuelve HTTP 200 con versión 1 y tres plantillas. El JSON coincide con la grabación incluida. El modo predeterminado del repositorio es `USE_RECORDED_TEMPLATES = true`; para el recorrido real se cambia únicamente ese valor.

## Estado de la entrega y límites

- A4, A3, A5, A2, A7 y A6 están implementadas en `codex/levelmate-local`; Firebase está configurado en Spark y Firestore en producción. La rama incluye el `app/google-services.json` real y el JSON público de plantillas.
- Las pruebas automáticas del 24/11/2026 y las comprobaciones manuales comunicadas el 25/11/2026 se mantienen diferenciadas. El cierre SH-05 solo actualiza README, validación y logbook; no cambia código, dependencias, plugins ni navegación y no repite la compilación Android.
- Los casos de notificaciones denegadas, ausencia de proyectos pendientes y ausencia de duplicados están contemplados en la implementación. No se afirma aquí una prueba manual nueva e individual de cada uno.
- Las pruebas instrumentadas de Room están compiladas, pero no se han ejecutado desde este entorno. Tampoco se afirma una nueva prueba física del nivel o de la calidad del OCR.
- La integración de la rama en `main` corresponde a la revisión del cambio; publicar la rama no la incorpora automáticamente a la principal.
- El APK es debug. La release firmada C2 queda fuera de estas seis tareas y no se presenta como completada.
