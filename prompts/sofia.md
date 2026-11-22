# Logbook de sofiahuch

## SH-01

- **Date and time:** 2026-11-22; la interfaz no conserva la hora del prompt original. Registro al obtener acceso al código: 21:56 Europe/Madrid.
- **Author:** sofiahuch, con asistencia de Codex.
- **Tool:** ChatGPT Codex, misma conversación; hubo varios intentos de acceso antes de implementar.
- **Files touched:** se detallan por bloque en «What happened» y en los commits SH-01.
- **Result:** en curso.
- **How I checked it:**
  - What the AI ran: clonación y `git pull --ff-only` (Already up to date). Intentó `bash gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug`; la descarga de Gradle falló con `Network is unreachable` antes de compilar. Se anotarán las demás comprobaciones reales al ejecutarlas.
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

Catálogo propio en docs/templates.json y copia idéntica en assets/recorded/project_templates.json. Una constante elige red o grabación, con el mismo Json(ignoreUnknownKeys=true) en ambos caminos y el httpClient del esqueleto. El modo de entrega es grabado. La URL real apunta a la rama de esta entrega. Caso incómodo: sin red, error HTTP o versión/formato inválido usan la grabación y se identifican como fallback. Se añadieron pruebas de esos caminos y de cancelación; ejecución de Gradle pendiente por red.

#### A2 · Lista, detalle y compartir foto

Lista y detalle conectados a Room; creación y edición, materiales con unidades y pasos ordenados con done. Guardado del formulario en ViewModel para sobrevivir a rotaciones. Plantillas crean todas las filas en una transacción. Fotos generales y por proyecto, recuperación de fotos antiguas y envío con FileProvider, ClipData y permiso temporal de lectura. Caso incómodo: lista vacía, proyecto eliminado, foto desaparecida, cámara sin almacenamiento o app receptora ausente. Se avisó antes de modificar AppNavGraph.kt. Se reemplazó el nombre de archivo sólo por segundos por un nombre temporal único para impedir sobrescrituras.

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
