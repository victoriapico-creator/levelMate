# LevelMate

App Android de proyectos de taller, con Jetpack Compose, Room, DataStore, CameraX y Firebase. Paquete: `com.example.notesbysubject`.

## Cómo ejecutarla

1. Abre el repositorio en Android Studio y sincroniza Gradle usando las versiones del repositorio. El proyecto requiere SDK 37 y la JVM 25 indicada en `gradle/gradle-daemon-jvm.properties`. No aceptes propuestas de actualización de versiones o plugins.
2. Ejecuta `app` en Android 8/API 26 o superior. Para probar permisos de notificación usa Android 13/API 33 o superior. Cámara y nivel se prueban mejor en un teléfono.
3. En Projects pulsa **New project**, rellena el formulario y guarda. El detalle permite editar, añadir materiales con unidades, marcar pasos y tomar o adjuntar fotos. **General photos** reúne fotos sin proyecto y recupera las guardadas antes de incorporar Room.
4. En **Settings** guarda la hora `HH:mm`, la tolerancia de 0,1 a 15 grados y, si quieres, activa el recordatorio. Se pide permiso para notificar. Android puede retrasar la ejecución.
5. **Shared projects** necesita la configuración Firebase de la sección siguiente. Las funciones locales no requieren una cuenta.

## Firebase: estado y configuración pendiente

El login, las copias compartidas y las reglas están implementados. **No se ha creado aún el proyecto remoto ni se ha descargado `app/google-services.json`: hace falta conectar la cuenta Firebase.** No hay una configuración ficticia ni un proyecto de pago.

Para completar la configuración con una cuenta autorizada:

1. Crea un proyecto Firebase **Spark**, sin vincular facturación ni activar Blaze.
2. Registra una aplicación Android con paquete `com.example.notesbysubject` y descarga su `google-services.json` a `app/google-services.json`. Es la configuración de cliente solicitada; no añadas cuentas de servicio, claves privadas ni credenciales de administración. El plugin Google Services ya presente en el proyecto se activa automáticamente sólo cuando existe ese archivo; sus versiones no cambian.
3. Habilita Authentication, proveedor **Email/Password**. Las contraseñas se introducen exclusivamente en la app y no se guardan en el repositorio ni en el logbook.
4. Crea Cloud Firestore en **modo producción**. Publica el archivo `firestore.rules` de este repositorio, por ejemplo con `firebase deploy --only firestore:rules --project TU_PROJECT_ID`. No uses reglas de modo test.
5. Ejecuta de nuevo la app, crea dos cuentas desde Shared projects y copia el UID de la segunda. Desde la primera, selecciona un proyecto y comparte con ese UID. La segunda verá la copia. Sólo el propietario puede publicar actualizaciones o retirarla; los lectores pueden importarla como un proyecto local independiente.

No se suben fotos ni rutas de archivos a Firestore, y no se usa Firebase Storage. Si se retira una copia compartida, las copias locales ya importadas siguen existiendo. Para actualizar una copia compartida, su propietario vuelve a pulsar Share / update shared copy. Cada copia admite hasta 10 participantes. Los UID se intercambian con la otra persona, no son contraseñas.

## Llamada real o grabada (A5)

En `data/templates/TemplateRepository.kt`, cambia únicamente:

```kotlin
const val USE_RECORDED_TEMPLATES = true
```

- `true`: usa `app/src/main/assets/recorded/project_templates.json`. Es el **modo de entrega**.
- `false`: tras subir la rama, Retrofit descarga el JSON de [esta URL](https://raw.githubusercontent.com/victoriapico-creator/levelMate/codex/levelmate-complete/docs/templates.json). Si la red, HTTP, JSON o versión fallan, muestra la copia grabada con un aviso. GitHub rechazó publicar la rama desde esta conexión (403), por lo que esta URL aún está pendiente de publicación. La URL estará disponible mientras exista esta rama; al integrarla en main puede apuntarse la ruta de `TemplateApi` a main.

La copia publicada y la grabada son idénticas. El catálogo admite materiales o pasos vacíos. Un formato no compatible se rechaza. El mismo `Json { ignoreUnknownKeys = true }` se utiliza en Retrofit y en assets. Las peticiones usan el `httpClient` original y su User-Agent.

## Tabla de cobertura

| Fila | Función | Archivos principales | Pantalla | Caso incómodo |
|---|---|---|---|---|
| A1 | Navegación y tema ya existentes; conexiones de datos añadidas | `ui/navigation/AppNavGraph.kt`, `ui/theme/` | Todas | Ruta de proyecto inexistente muestra Project not found |
| A2 | Lista, detalle, edición y envío de una foto por intent | `ui/screens/ProjectsScreen.kt`, `ProjectDetailScreen.kt`, `ui/photos/` | Projects, ProjectDetail | Lista vacía, archivo desaparecido o aplicación receptora ausente |
| A3 | Hora, activación y tolerancia persistentes | `data/settings/`, `ui/screens/SettingsScreen.kt` | Settings, Camera | `25:70`, `NaN`, cero o tolerancia excesiva no se guardan |
| A4 | Proyectos, materiales, pasos y fotos con relaciones y transacciones | `data/local/` | Projects, ProjectForm, ProjectDetail | Proyecto borrado, clave foránea inexistente, carga vacía y plantilla inválida |
| A5 | Plantillas Retrofit con copia local; publicación remota pendiente del push | `data/templates/`, `assets/recorded/project_templates.json`, `docs/templates.json` | Projects | Sin red o JSON inválido usa la copia local; catálogo vacío tiene mensaje |
| A6 | Login, alta, copias compartidas por UID y reglas de propietario; configuración remota pendiente | `data/sharing/`, `ui/SharingViewModel.kt`, `SharedScreen.kt`, `firestore.rules` | Shared | Sin configuración o sesión, sin red, lector sin permiso de escritura y datos corruptos |
| A7 | Recordatorio diario único con notificación | `reminders/`, `SettingsScreen.kt`, `AndroidManifest.xml` | Settings, notificación | Permiso denegado o ningún proyecto vencido: no notifica; cambiar ajustes no duplica trabajos |
| A8 | Cámara ya existente; asociación a Room añadida | `CameraScreen.kt`, `ui/camera/PhotoStorage.kt` | Camera, ProjectDetail | Permiso denegado, espacio insuficiente y capturas sin sobrescribir archivos |
| A9 | Nivel existente con tolerancia de DataStore | `ui/camera/LevelCalculator.kt`, `LevelIndicator.kt`, `CameraScreen.kt` | Camera | Sin acelerómetro se conserva la cámara y aparece un aviso |
| A10 | Escaneo de texto existente, conservado | `ui/camera/TextScanner.kt`, `RecognizedTextFilter.kt`, `ScanResultSheet.kt` | Camera | Sin texto, baja confianza o fallo del reconocimiento |

## Decisiones de diseño

1. Room guarda la fecha opcional como ISO `yyyy-MM-dd`; el formulario sigue usando `dd/MM/yyyy`. Las cantidades son texto para admitir unidades. Al borrar proyectos, materiales y pasos se borran en cascada y las fotos quedan como generales, con sus archivos intactos.
2. Se comparten instantáneas por UID y con acceso de lectura para participantes. Sólo el propietario actualiza o revoca la copia. Evita que cambios en un proyecto local sobrescriban silenciosamente el trabajo de otras personas. Las fotos siguen siendo locales.
3. Plantillas reales y grabadas usan el mismo formato y validaciones. La entrega funciona sin red y deja visible cuándo se ha utilizado la alternativa grabada.

## Validación y pendientes

Los resultados reales y el recorrido manual están en [docs/VALIDATION.md](docs/VALIDATION.md). El registro de prompts está en [prompts/sofia.md](prompts/sofia.md). No se han modificado `gradle/libs.versions.toml`, el wrapper ni las versiones/plugins del esqueleto. `HttpClient.kt` se conserva.

```sh
bash gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
bash gradlew :app:connectedDebugAndroidTest
firebase emulators:exec --only firestore --project demo-levelmate "node scripts/check-firestore-rules.mjs"
```

Las pruebas instrumentadas requieren teléfono o emulador. El script de reglas se limita al emulador local y no se ejecuta contra producción.

## Release (C2)

No se ha solicitado ni generado una release firmada ni una clave de firma. C2 queda pendiente. Las claves de firma deben mantenerse fuera del repositorio; Google Play no acepta una clave debug. En Android Studio usa **Build > Generate Signed Bundle/APK > Android App Bundle**.

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
