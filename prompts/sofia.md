# Logbook de sofiahuch

## SH-01

- **Date and time:** 2026-11-22; la interfaz no conserva la hora del prompt original. Registro al obtener acceso al código: 21:56 Europe/Madrid.
- **Author:** sofiahuch, con asistencia de Codex.
- **Tool:** ChatGPT Codex, misma conversación; hubo varios intentos de acceso antes de implementar.
- **Files touched:** data/local/, data/settings/, data/templates/, data/sharing/, reminders/, LevelMateApplication.kt, ui/screens/, ui/photos/, ui/ProjectActionsViewModel.kt, ui/SharingViewModel.kt, ui/ObservedData.kt, ui/camera/PhotoStorage.kt, ui/navigation/AppNavGraph.kt, AndroidManifest.xml, app/build.gradle.kts, strings.xml, icono de notificación, tests, reglas y configuración Firestore, README.md, docs/, .gitignore y este archivo. También constan en los commits SH-01.
- **Result:** accepted with changes (entrega técnica de Codex; aceptación de la persona y revisión manual pendientes).
- **How I checked it:**
  - What the AI ran: clonación y `git pull --ff-only` (Already up to date). Intentó `bash gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`; la descarga de Gradle falló con `Network is unreachable` antes de compilar. Después se resolvieron la descarga de Gradle y el SDK 37.0 sin cambiar las versiones del repositorio. :app:testDebugUnitTest pasó 21 pruebas; :app:assembleDebug, :app:lintDebug (0 errores) y :app:assembleDebugAndroidTest terminaron correctamente. El emulador Firestore pasó las 10 comprobaciones de scripts/check-firestore-rules.mjs. Las pruebas Android instrumentadas se compilaron, pero no se ejecutaron en dispositivo. Se validaron XML, JSON, igualdad de catálogos y conservación de versiones y HttpClient.kt.
  - What I checked myself: pendiente de revisión de la persona; no se atribuyen pruebas manuales a la autora.

### Prompt

haz el siguiente trabajo: [https://github.com/victoriapico-creator/levelMate](https://github.com/victoriapico-creator/levelMate) : Hola! Te cuento cómo vamos con LevelMate y qué te toca.

YA HECHO (en el repo, haz git pull):
- Navegación entre pantallas y tema (Projects, ProjectForm, ProjectDetail, Camera, Settings, Shared)
- Formulario de proyecto con validación (nombre, descripción, fecha límite)
- Cámara con permiso y fotos guardadas en filesDir/notes/general/
- Nivel con el acelerómetro
- Escaneo de texto con ML Kit

TE TOCA A TI, por este orden:
1. A4 · Room (lo primero, de esto depende todo): tablas de proyectos, materiales, pasos y fotos.
   Propuesta de columnas para que encajen con mi formulario:
   - Project: id, name, description, deadline (puede ser null)
   - Material: id, projectId, name, quantity
   - Step: id, projectId, text, done, position
   - Photo: id, projectId (puede ser null), path (texto), takenAt
   Cuando esté, yo conecto mi formulario y mis fotos.
2. A3 · DataStore: ajustes (hora del recordatorio y tolerancia del nivel, que ahora es una constante de 1,5°)
3. A5 · Retrofit: cargar plantillas de proyecto desde un JSON publicado (por ejemplo en GitHub Pages), con una versión grabada en assets y un cambio de una línea entre real y grabada
4. A2 · Lista de proyectos, detalle y botón de compartir una foto con un intent
5. A7 · Recordatorios con WorkManager y notificación
6. A6 · Login y Firestore para compartir proyectos con otras personas

HOY, si puedes: crea el proyecto de Firebase (plan gratuito Spark, nunca Blaze). Registra la app con el paquete com.example.notesbysubject, baja google-services.json a la carpeta app y súbelo al repo. Firestore en modo producción, no en modo test.

REGLAS:
- git pull ANTES de empezar cada prompt (nos pasa que el push se rechaza si no)
- Un prompt = una tarea pequeña, chat nuevo cada vez, y apúntalo en tu logbook (prompts/tu-nombre.md) al enviarlo
- No cambies versiones ni plugins, aunque Android Studio o la IA lo sugieran
- Nada de contraseñas ni claves en prompts ni en el repo
- AppNavGraph.kt solo lo toca uno a la vez: avísame antes de cambiarlo
- Cada fila necesita su "caso incómodo" (sin red, lista vacía, entrada inválida...), si no, no da los 2 puntos

### What happened

El registro se pudo escribir después de obtener el código, no al enviar el primer prompt: antes fallaban la clonación y el acceso a GitHub con 404. No se inventan horas ni conversaciones separadas. Se conserva el contenido del encargo; los espacios de formato de la interfaz se han normalizado al copiarlo aquí.

La persona confirmó explícitamente «Completa las seis por orden», por lo que se trabaja en seis commits en esta conversación. Se avisó antes de modificar AppNavGraph.kt. La configuración remota de Firebase depende de conectar la cuenta de Google; no se crea una configuración ficticia.

#### A4 · Room

Implementado: entidades y relaciones, consultas observables, operaciones suspendidas y transacciones. Las fechas se guardan como ISO yyyy-MM-dd o null; las cantidades son texto para admitir unidades. Al borrar un proyecto se eliminan sus materiales y pasos y sus fotos pasan a generales. No se borran archivos de fotos al borrar un proyecto. Se añadieron pruebas instrumentadas para base vacía, clave foránea inválida, borrado, transacción inválida y recordatorios de proyectos completos; su ejecución necesita Android.

#### A3 · DataStore

Preferencias de hora y tolerancia persistentes, validación de HH:mm y rango 0,1–15°, guardado atómico y tolerancia aplicada a la cámara. Se conserva 1,5° como valor inicial. Caso incómodo: 25:70, NaN o tolerancia fuera del rango impiden guardar. Inicio de la aplicación importa las fotos previas sin duplicarlas.

#### A5 · Retrofit

Catálogo propio en docs/templates.json y copia idéntica en assets/recorded/project_templates.json. Una constante elige red o grabación, con el mismo Json(ignoreUnknownKeys=true) en ambos caminos y el httpClient del esqueleto. El modo de entrega es grabado. La URL real apunta a la rama de esta entrega y se activará al publicarla; GitHub rechazó el push desde la integración con 403. Caso incómodo: sin red, error HTTP o versión/formato inválido usan la grabación y se identifican como fallback. Se añadieron pruebas de esos caminos y de cancelación; ejecución de Gradle pendiente por red.

#### A2 · Lista, detalle y compartir foto

Lista y detalle conectados a Room; creación y edición, materiales con unidades y pasos ordenados con done. Guardado del formulario en ViewModel para sobrevivir a rotaciones. Plantillas crean todas las filas en una transacción. Fotos generales y por proyecto, recuperación de fotos antiguas y envío con FileProvider, ClipData y permiso temporal de lectura. Caso incómodo: lista vacía, proyecto eliminado, foto desaparecida, cámara sin almacenamiento o app receptora ausente. Se avisó antes de modificar AppNavGraph.kt. Se reemplazó el nombre de archivo sólo por segundos por un nombre temporal único para impedir sobrescrituras.

#### A7 · WorkManager

Trabajo periódico único con retraso inicial hasta la hora local elegida, canal y permiso de notificaciones en Android 13+, activación y cancelación desde ajustes. No se duplica al abrir la app. Consulta Room para proyectos vencidos con pasos pendientes; sin proyectos o con permiso denegado termina sin notificar. Cambiar hora/zona reprograma el recordatorio. WorkManager no garantiza una hora exacta. Se añadieron pruebas del cálculo horario, incluido el cambio de hora de Madrid.

#### A6 · Login y Firestore

Login y alta por email con contraseña mantenida sólo en memoria y borrada del campo al enviar. Copias de proyectos compartidas por UID, escucha con retirada de listeners, caché identificada, importación a Room, actualización y revocación sólo por el propietario. Se excluyen fotos y rutas locales del payload. Reglas de producción con denegación por defecto y comprobaciones de pertenencia, propietario y campos. Pruebas unitarias de payload inválido y sin fotos; script de 10 comprobaciones contra el emulador de Firestore. Caso incómodo: sin configuración, sin sesión, sin red, datos remotos corruptos y lector intentando modificar. No se añadieron plugins ni se cambiaron versiones: el plugin Google Services ya declarado se activa de forma condicional al existir el archivo real de configuración. Crear Spark, habilitar Authentication y publicar reglas requiere acceso a Firebase; queda pendiente si la conexión no llega durante el trabajo.

## SH-02 · Aclaraciones y desbloqueo

- **Date and time:** 2026-11-22; horas de los mensajes no disponibles en la interfaz; anotados en orden al recuperar el código, a las 21:56 Europe/Madrid.
- **Author:** sofiahuch.
- **Tool:** ChatGPT Codex, misma conversación que SH-01.
- **Files touched:** este logbook; la implementación corresponde a SH-01.
- **Result:** aceptado como aclaración de alcance y acceso.
- **How I checked it:** la IA comprobó que el repositorio pasó de privado/404 a público y pudo clonarlo; no se declaran pruebas de la persona.

### Prompt

Mensajes de la persona, en orden (cada línea es un envío independiente):

1. Completa las seis por orden
2. ponme lo de autorizar
3. ya esta
4. prueba ahora que es publico
5. Sí, conectaré Firebase

### What happened

Primero se conectó GitHub. La instalación de sofiahuch no daba acceso al repositorio privado de otra cuenta. Tras hacerse público, la clonación y el pull funcionaron. Los avisos automáticos de conexión de la interfaz no se presentan como prompts de la persona.

### Revisión final de SH-01

Se corrigieron el permiso de cámara al volver de ajustes, la retirada del sensor al pausar, el estado de notificaciones al volver de ajustes y el aislamiento del listado compartido al cambiar de cuenta. Un primer intento de aplicar el parche A3 fue rechazado por dos operaciones sobre el mismo archivo; se corrigió el parche antes de aplicarlo. Lint encontró la ausencia de uses-feature para cámara opcional; se añadió, y lint pasó sin errores. Sus sugerencias de actualizar versiones no se aplicaron.

Las descargas de dependencias necesitaron el proxy/certificados del entorno y una caché temporal externa de JUnit/Hamcrest, con sus versiones oficiales originales. Esto no modifica las dependencias del repositorio. El detalle de resultados y pruebas manuales pendientes está en docs/VALIDATION.md.

Se ejecutó de nuevo git pull --ff-only origin main antes de cerrar: Already up to date. La creación de la rama remota recibió 403 Resource not accessible by integration. Se entrega el código con su historial en un bundle y un parche, además del APK debug, en lugar de afirmar una subida que no ocurrió. Firebase sigue sin conexión confirmada: no se ha creado un proyecto, google-services.json ni se han publicado reglas remotas. No se han introducido credenciales ni se ha activado facturación.

## SH-03 · Versión local para Android Studio

- **Date and time:** 2026-11-22 (fecha del contexto del cliente; hora exacta del envío no disponible). Registro técnico posterior al comenzar el cambio: 2026-11-23T00:01+01:00 Europe/Madrid.
- **Author:** sofiahuch, con asistencia de Codex.
- **Tool:** ChatGPT Codex, misma conversación.
- **Files touched:** dependencias del módulo app, data/sharing/, SharingViewModel.kt, SharedScreen.kt, LevelMateApplication.kt, pruebas, README, docs y configuración Firebase retirada. AppNavGraph.kt se conserva.
- **Result:** accepted with changes (entrega técnica; revisión manual de la persona pendiente).
- **How I checked it:**
  - What the AI ran: git pull --ff-only origin main: Already up to date. Pasaron 31 pruebas unitarias, assembleDebug, lintDebug (0 errores) y assembleDebugAndroidTest. Se verificó la ausencia de los SDK Authentication/Firestore en el APK. AppNavGraph.kt, catálogo de versiones, plugins raíz y wrapper se conservan. No se ejecutó en un dispositivo Android.
  - What I checked myself: pendiente; no se atribuyen pruebas manuales a la persona.

### Prompt

Mensajes de la persona, por orden:

1. puedes no usar firebase?
2. osea la app que se usa es android studio
3. Local, sin cuentas ni configuración

### What happened

La persona eligió explícitamente una aplicación local. Se mantiene el proyecto Android para abrir en Android Studio, los datos en Room y el resto de funciones, y se sustituye la parte de cuentas/nube por exportación e importación de proyectos mediante archivos. Esta alternativa no cumple literalmente la A6 original, que pedía login y Firestore; se informó de ello antes de confirmar la elección. Se conserva el historial anterior en lugar de reescribir lo que se pidió o comprobó.

Se retiraron las dependencias directas del backend y su aplicación condicional de Google Services, las clases y pantalla de autenticación y las reglas/configuración/pruebas Firestore. Las referencias históricas del logbook y la guía original se conservan como historial, no como configuración activa. Se añadieron archivos de proyecto versionados, exportación mediante selector/intent, lectura limitada, vista previa y validación antes de insertar en Room con identificadores nuevos. Las 13 pruebas nuevas cubren formato, contenido, límites, orden y datos corruptos. La publicación de la rama sigue dependiendo del permiso de escritura GitHub; la variante local se entrega como proyecto para Android Studio y APK debug.

## SH-04 · Recuperar Firebase y terminar la configuración real

- **Date and time:** 2026-11-24; la hora exacta de los mensajes no está disponible.
- **Author:** sofiahuch, con asistencia de Codex.
- **Tool:** ChatGPT Codex, misma conversación.
- **Prompt:** «vale pues tenemos que configurar esas cosas firebase, park y google-services.json, todo lo que falte hacerlo bien». La persona informa después de que el complemento no se puede instalar.
- **Scope:** restablecer A6 con Authentication y Firestore y crear/configurar el proyecto real en el plan gratuito Spark, sin facturación Blaze. Mantener el intercambio por archivos ya probado, las versiones originales y la navegación existente.
- **Status:** implementación y configuración Firebase completadas; aplicación en el ordenador, push GitHub y recorridos manuales finales pendientes. Se ejecutó git pull --no-rebase origin main: Already up to date. El complemento Firebase no llegó a instalarse; se utilizó la consola oficial y acceso seguro a Google. No se registran credenciales ni códigos de acceso.
- **Manual checks reported by the user before this change:** la app se ejecuta en un Pixel 6 virtual API 37; se conservan proyecto, materiales, pasos marcados y foto después de detener/abrir la app; ajustes 18:30 y tolerancia 2 persistentes, con rechazo de 25:00; creación desde la plantilla incluida; apertura del menú de compartir foto; exportación/importación con copia independiente y conservación de materiales y pasos. La llegada del recordatorio y el modo de plantillas en línea no se han confirmado.

- **Result:** proyecto real `levelmate-56e8c` creado en Spark, sin facturación ni Blaze; app `com.example.notesbysubject` registrada; Authentication por correo/contraseña habilitado; Firestore Standard en Madrid creado en producción con reglas privadas publicadas. La persona descargó y adjuntó la configuración Android real; se validó y añadió a `app/google-services.json`.
- **Code:** se recupera el acceso y la lista de Firestore en una pestaña de la pantalla existente, conservando el intercambio de archivos. La publicación usa `SetOptions.merge()` y `arrayUnion` para crear sin una lectura inicial denegada y conservar los participantes. Se refuerzan la validación de miembros y el espacio de identificadores por propietario. Se conservan catálogos, versiones, plugins raíz, wrapper, entidades Room y AppNavGraph.kt respecto a sus referencias indicadas en VALIDATION.md.
- **How checked:** 37 pruebas unitarias correctas; APK debug, procesamiento de Google Services y APK instrumentado compilados; lint 0 errores, 13 advertencias y 3 sugerencias. No se ejecutaron tests instrumentados. Las reglas pasaron 21 comprobaciones en el emulador demo-levelmate. El backend real pasó 9 comprobaciones REST de registro/acceso, publicación y permisos; documento y dos cuentas temporales eliminados con HTTP 200. Ninguna contraseña o token de esas pruebas se registra en esta entrega.
- **Publication/delivery:** la rama remota y el JSON real de plantillas siguen pendientes del push: GitHub rechaza la escritura de la integración y la terminal no tiene credenciales. Se entrega actualización por bundle/patch y proyecto para Android Studio, con instrucciones para conservar la carpeta y datos existentes. README y VALIDATION.md distinguen lo comprobado de los pasos pendientes. Aún no se atribuyen pruebas visuales nuevas de Firebase ni recepción de una notificación a la persona.


## SH-05 · Cierre de las pruebas y documentación de la entrega

- **Date and time:** 2026-11-25, Europe/Madrid; hora exacta del cierre no registrada.
- **Author:** sofiahuch, con asistencia de Codex.
- **Tool:** ChatGPT Codex, misma conversación.
- **Prompt:** continuar las comprobaciones una vez funciona el emulador: «vale ya me va ahora sigamos con lo que hay que hacer ahora que va». La persona confirmó después el intercambio entre cuentas, la llegada del recordatorio y las plantillas con internet y sin conexión.
- **Scope:** actualizar únicamente README, docs/VALIDATION.md y este logbook con las comprobaciones realmente comunicadas y la publicación de la rama. Se conserva el historial SH-01 a SH-04 como registro de su momento.
- **Before work:** git pull --no-rebase origin main: Already up to date; git pull --ff-only origin codex/levelmate-local: Already up to date. Se parte de la actualización SH-04 publicada, con árbol de trabajo limpio.
- **What the person checked:** nuevo Pixel 6 virtual Android 14/API 34; creación e inicio de sesión de dos cuentas; compartir y guardar una copia local del proyecto; recorrido guiado con credenciales incorrectas y recuperación; notificación recibida tras elegir una hora futura de 24 horas; carga real de plantillas y fallback con creación sin red. La persona confirmó los recorridos; no se atribuye a Codex control directo de ese emulador ni ejecución de pruebas instrumentadas.
- **What Codex checked:** rama remota publicada por la persona; endpoint JSON público con HTTP 200, versión 1 y tres plantillas; revisión del código de programación, permisos y selección de proyectos del recordatorio. Se mantienen como resultados automáticos históricos de SH-04 las 37 pruebas unitarias, APK, Google Services, lint sin errores, APK instrumentado compilado, 21 comprobaciones de reglas y 9 de backend. En SH-05 solo se verifica el diff de documentación y la conservación de la guía original.
- **Result:** quedan documentadas las seis tareas A4, A3, A5, A2, A7 y A6. La rama publicada incluye Firebase Spark, Firestore en producción y google-services.json; no se utilizan Blaze ni Firebase Storage. El primer intento del recordatorio estaba programado para las 18:05 durante una prueba de madrugada y se corrigió la configuración, sin modificar el código.
- **Files touched:** README.md, docs/VALIDATION.md, prompts/sofia.md. Sin cambios en código Android, versiones, plugins, Room o AppNavGraph.kt. No se registran contraseñas, tokens, correos de las cuentas de prueba ni claves privadas.
- **Publication:** la integración GitHub volvió a rechazar la escritura del árbol de documentación con HTTP 403 (Resource not accessible by integration). El cierre queda en un commit y un bundle incremental que parte del commit publicado 3746d9e; se puede incorporar y subir desde el ordenador de la persona. No se presenta como publicado ni integrado en main desde esta sesión.
- **Limits:** tests instrumentados no ejecutados por Codex, nivel físico/OCR sin nueva verificación y release firmada C2 fuera de alcance. La incorporación a main queda sujeta a la revisión de la rama.
