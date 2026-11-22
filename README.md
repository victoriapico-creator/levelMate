# LevelMate · versión local para Android Studio

Proyecto Android en Kotlin y Jetpack Compose. Se abre en **Android Studio** y se ejecuta en un teléfono o emulador Android. Paquete: `com.example.notesbysubject`.

Esta versión funciona sin cuentas, servidor ni configuración Firebase. Los proyectos se guardan en Room; los ajustes en DataStore. La persona eligió sustituir la A6 original por compartir/importar archivos. **Esta alternativa local no cumple literalmente la A6 que exigía login y Firestore.**

## Abrir y ejecutar

1. Descomprime la entrega y abre en Android Studio la carpeta `levelMate`, donde están `settings.gradle.kts`, `app` y el wrapper de Gradle.
2. Sincroniza Gradle con las versiones del repositorio. El proyecto utiliza SDK 37 y la JVM 25 indicada en `gradle/gradle-daemon-jvm.properties`.
3. Selecciona un dispositivo Android API 26 o superior y ejecuta `app` con Run. Para probar permisos de notificación usa API 33 o superior. También se incluye un APK debug para probar la app.
4. No necesitas crear un proyecto de backend, iniciar sesión ni añadir `google-services.json`. El modo inicial de plantillas usa el archivo incluido en assets, así que las funciones locales funcionan sin conexión.

## Uso

- **New project:** nombre, descripción y fecha opcional. El detalle permite editar, añadir materiales con unidades, marcar pasos y tomar o adjuntar fotos.
- **General photos:** fotos sin proyecto y recuperación de fotos tomadas antes de incorporar Room. Eliminar un proyecto conserva sus fotos como generales.
- **Settings:** hora del recordatorio, activación y tolerancia del nivel entre 0,1 y 15 grados. Android puede retrasar los recordatorios de WorkManager.
- **Share / import projects:** selecciona un proyecto y usa **Share project file** o **Save project file**. El archivo incluye nombre, descripción, fecha, materiales y pasos con su estado. Las fotos se envían por separado desde el detalle.
- **Choose project file:** selecciona un archivo `.levelmate.json`, revisa la vista previa y pulsa **Import as a new project**. Cada importación crea un proyecto independiente con identificadores nuevos; no sobrescribe proyectos actuales. Cancelar el selector o la vista previa no cambia Room.

Los archivos se validan antes de importar: formato y versión, máximo 256 KB, nombres/fechas/cantidades válidos, hasta 50 materiales y 100 pasos. Un archivo corrupto, demasiado grande o de una versión incompatible muestra un mensaje y no se guarda. Los pasos se recuperan por su posición; el archivo no incluye rutas privadas, fotos, cuentas ni identificadores de Room.

## Qué se ha retirado de Firebase

Se retiraron las dependencias directas de Authentication/Firestore y su BOM del módulo `app`, los clientes de nube, la pantalla de login, las reglas, los archivos de configuración y las pruebas exclusivas de Firestore. No se aplica Google Services ni se necesita un proyecto Firebase. Las declaraciones originales no usadas del catálogo de versiones y del plugin raíz se conservan para respetar el bloqueo de versiones/plugins del esqueleto. ML Kit, ya incluido para reconocimiento local, puede conservar bibliotecas auxiliares transitivas; no usa aquí un proyecto o cuenta Firebase.

## Llamada real o grabada (A5)

En `data/templates/TemplateRepository.kt`, cambia únicamente:

```kotlin
const val USE_RECORDED_TEMPLATES = true
```

- `true`: usa `app/src/main/assets/recorded/project_templates.json`. Es el **modo de entrega**, sin red.
- `false`: después de subir la rama `codex/levelmate-local`, descarga el JSON de [esta URL](https://raw.githubusercontent.com/victoriapico-creator/levelMate/codex/levelmate-local/docs/templates.json). Si fallan red, HTTP, JSON o versión, muestra un aviso y usa la copia grabada.

La publicación de esa URL sigue pendiente del push: GitHub rechazó la escritura desde la conexión de esta sesión con 403. Las dos copias del catálogo son idénticas; ambas usan el mismo parser tolerante a campos desconocidos. Se conserva el `httpClient` original y su User-Agent. Si se cambia de rama al publicar, hay que ajustar la ruta de `TemplateApi`.

## Tabla de cobertura

| Fila | Función | Archivos principales | Pantalla | Caso incómodo |
|---|---|---|---|---|
| A1 | Navegación y tema existentes | `ui/navigation/AppNavGraph.kt`, `ui/theme/` | Todas | Proyecto inexistente muestra un aviso |
| A2 | Lista, detalle, edición y compartir foto por intent | `ui/screens/ProjectsScreen.kt`, `ProjectDetailScreen.kt`, `ui/photos/` | Projects, ProjectDetail | Lista vacía, foto desaparecida o app receptora ausente |
| A3 | Ajustes persistentes | `data/settings/`, `SettingsScreen.kt` | Settings, Camera | Hora o tolerancia inválida no se guardan |
| A4 | Proyectos, materiales, pasos y fotos | `data/local/` | Projects, ProjectForm, ProjectDetail | Borrado con relaciones, clave foránea inválida y operación sin proyecto parcial |
| A5 | Plantillas Retrofit con copia local; publicación pendiente del push | `data/templates/`, `assets/recorded/`, `docs/templates.json` | Projects | Sin red o JSON inválido usa la grabación |
| A6 original | Sustituida por archivos locales por elección de la persona; sin login/Firestore | `data/sharing/`, `SharingViewModel.kt`, `SharedScreen.kt` | Share / import projects | JSON corrupto, versión inválida, archivo grande o selector cancelado |
| A7 | Recordatorio diario único | `reminders/`, `SettingsScreen.kt` | Settings, notificación | Sin permiso o sin pendientes no notifica; no duplica trabajos |
| A8 | Cámara y fotos locales | `CameraScreen.kt`, `PhotoStorage.kt` | Camera, ProjectDetail | Permiso denegado, almacenamiento insuficiente o capturas sucesivas |
| A9 | Nivel con tolerancia configurable | `ui/camera/`, `CameraScreen.kt` | Camera | Sin acelerómetro conserva el resto de la cámara |
| A10 | Escaneo existente conservado | `TextScanner.kt`, `RecognizedTextFilter.kt`, `ScanResultSheet.kt` | Camera | Sin texto o baja confianza |

## Decisiones

1. Room conserva la fecha opcional en ISO `yyyy-MM-dd`, mientras el formulario usa `dd/MM/yyyy`. Las cantidades incluyen unidades. Borrar un proyecto conserva sus archivos de fotos.
2. Compartir archivos permite intercambiar copias completas de los datos del proyecto sin cuentas ni servidor. La importación se revisa antes de guardar, genera identificadores nuevos y utiliza una transacción.
3. Plantillas y archivos de proyectos tienen formatos versionados. El catálogo incluye una copia grabada y los archivos importados se leen con un límite de tamaño y profundidad antes de decodificarse.

## Validación

Consulta [docs/VALIDATION.md](docs/VALIDATION.md) y el registro de cambios de esta petición en [prompts/sofia.md](prompts/sofia.md), entrada SH-03.

Desde la terminal de Android Studio en Windows:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

En Linux/macOS usa `bash gradlew` con los mismos argumentos. Las pruebas instrumentadas necesitan un dispositivo conectado. No se han cambiado versiones ni añadido plugins; no se aplica el plugin Firebase/Google Services al módulo app. `AppNavGraph.kt` se conserva respecto a la entrega anterior.

## Release (C2)

El APK incluido es debug. No se ha generado una release firmada ni una clave de firma; C2 sigue pendiente. Las claves de firma permanecen fuera del repositorio.

## Referencia original del esqueleto

El apartado siguiente se conserva sin cambios como referencia del curso. Sus indicaciones sobre Firebase/Firestore corresponden a la versión original; para esta variante local prevalece la decisión descrita arriba. No se presenta la alternativa local como cumplimiento literal de la A6 original.

## Facts you cannot guess

Keep this section as it is. It is section 10 of the skeleton's guide, copied here so that your AI assistant can read it while it works on your project.

### A5 · The timetable

No key. The recorded file, `app/src/main/assets/recorded/timetable.json`, is the same as this address:

```text
https://cunef-mia-sap-proyecto.vercel.app/data/app1/timetable.json
```

The JSON has `note`, a list `subjects` and a list `slots`:

| Field | What it holds |
|---|---|
| `note` | A sentence about the data. You do not need it. |
| `subjects[].id` | A short code such as `MOB`. The slots point to it. |
| `subjects[].name` | The subject's name. |
| `subjects[].color` | A colour as text: `#1A1F6C`. |
| `slots[].id` | A number that never repeats. |
| `slots[].subjectId` | The `id` of its subject. |
| `slots[].dayOfWeek` | 1 is Monday and 7 is Sunday. |
| `slots[].start`, `slots[].end` | Local time in Madrid, as text with two digits for the hour: `09:00`. |
| `slots[].room` | The room, or `null` when it is not known yet. |

Two cases are awkward on purpose:

- One slot has `"room": null`.
- One subject, `SEM`, has no slots, so no time ever matches it.

Decide what your app shows in both.

Careful with `dayOfWeek`. Android's `java.util.Calendar` counts from Sunday, so its `DAY_OF_WEEK` is 2 on a Monday. `java.time.DayOfWeek.getValue()` counts 1 for Monday and 7 for Sunday, like the file.

### Fields you do not read

The file has `note`, which your classes may not declare. With the plain `Json`, the app closes on the first field it does not know. Create `val lenientJson = Json { ignoreUnknownKeys = true }` (Session 8 Guide, section 3.7). Use it in both places where JSON is read:

- `lenientJson.asConverterFactory(...)` in your `Retrofit.Builder()`;
- `lenientJson.decodeFromString(...)` where you read the recorded file.

Session 8 Guide, section 7.3 explains the errors a call can bring back.

### A2 and A8 · Saving and sharing a photo

A photo saved in the app's internal files has no address that another app can open. So sharing its path does not work.

Save each photo under `filesDir/notes/`, for example in `notes/<subject id>/`. It is the only folder the skeleton's FileProvider exposes.

To share one:

1. Build its `content://` address with `FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)`.
2. Put it in a sharing `Intent`.
3. Add `Intent.FLAG_GRANT_READ_URI_PERMISSION`, so that the other app may read it.

The authority is `${applicationId}.fileprovider`, as declared in `AndroidManifest.xml`. FileProvider is not in any Guide: ask your assistant, and check what it writes against Android's reference, `https://developer.android.com/reference/androidx/core/content/FileProvider`.

### A8 · Where the photos live

The photos stay on the phone, in the internal files. Firebase Storage needs the paid Blaze plan, and the project must cost nothing.

### A6 · What is shared

Share the timetable with your classmates through Firestore. The photos are not shared.

### A7 · The reminder

WorkManager does not run at an exact time: Android picks the moment (Session 10 Guide, section 1.6), so the reminder may arrive a few minutes late. A periodic task runs at most once every 15 minutes (Session 10 Guide, section 3.4).

### A9 · The level

Use the accelerometer while the camera screen is open, so that the person sees whether the phone is straight before taking the photo.

### A10 · Text Recognition

The library is the bundled one, `com.google.mlkit:text-recognition` version 16.0.1. It is already in the catalog as `libs.text.recognition`. Do not swap it for `play-services-mlkit-text-recognition`: with an older Google Play services, the confidence comes back as 0 (Session 12 Guide, section 2.3).

As in Session 12, read the text of a saved photo (Session 12 Guide, section 4.8). Use each element's `confidence`, a number between 0 and 1 (Session 12 Guide, section 6.1). A word with a low confidence should not count as a match.

### In the emulator

- **Camera.** Edit the virtual device in Device Manager and set the back camera to Webcam0, your laptop's webcam. It must be free, so close Teams or Zoom. If the preview stays black, restart the emulator and keep its window open: a minimized emulator can give a black camera until it restarts. A webcam rarely shows a board with text. Test the recognition on a phone, or with the page of notes in `app/src/main/assets/samples/notes_page.png` (A10).
- **Accelerometer.** Open the emulator's extended controls, Virtual sensors, and move the pretend phone.
- Test A7 and A8 on an emulator with Android 13 or later.

### Call the real file from home

The class shares one internet address. In class, use the recorded file.
