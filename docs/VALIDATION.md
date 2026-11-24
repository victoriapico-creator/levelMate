# Validación de LevelMate con Firebase

Actualización SH-04, 24/11/2026. La persona pidió recuperar la A6 original después de probar la variante local. Esta entrega conserva también el intercambio mediante archivos.

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

No se atribuyen a la persona pruebas manuales de la nueva pantalla Firebase ni la recepción de un recordatorio. El nivel físico y la calidad del OCR tampoco se presentan como pruebas manuales nuevas de SH-04.

## Pendiente de aplicar/publicar y comprobar en el emulador

1. Actualizar la carpeta existente mediante el bundle de esta entrega, sincronizar Gradle y ejecutar desde Android Studio. No es necesario desinstalar la app ni cambiar la base de Room.
2. La rama `codex/levelmate-local` no se ha podido publicar desde esta sesión: la integración GitHub respondió 403 y la terminal no dispone de credenciales GitHub. Los cambios y `google-services.json` están en el commit local y en el bundle. El comando `git push -u origin HEAD:codex/levelmate-local` desde el ordenador de la persona publica la rama y el JSON de plantillas.
3. Comprobar la descarga real de A5 después del push, cambiando solo `USE_RECORDED_TEMPLATES` a `false`. El modo inicial grabado y el fallback están probados automáticamente; la URL sin publicar no se presenta como una llamada real completada.
4. En la app actualizada, crear/iniciar sesión con dos cuentas y compartir un proyecto. La copia recibida debe poder guardarse como proyecto local independiente; cambiar de cuenta no debe mostrar los compartidos de la anterior.
5. Probar A7 con permiso concedido, recordatorio activado, hora próxima a la del emulador y un proyecto sin terminar con fecha de hoy o anterior. Volver a Home y mantener el emulador encendido; WorkManager puede retrasarse y no equivale a una alarma exacta. Comprobar también el estado con permiso denegado.

El APK de esta compilación es debug. La release firmada C2 y la ejecución de tests instrumentados en un dispositivo no se incluyen como completadas.
