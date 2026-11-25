# LevelMate

Aplicación Android en Kotlin y Jetpack Compose para organizar proyectos de taller. Permite guardar materiales, pasos y fotos, consultar plantillas, ajustar el nivel de la cámara y recibir recordatorios. Los proyectos se guardan en Room; la hora del recordatorio y la tolerancia del nivel, en DataStore. Authentication y Firestore permiten compartir planes entre cuentas.

## Abrir y ejecutar

1. Abre en Android Studio la carpeta que contiene `settings.gradle.kts`, `app` y `gradlew.bat`.
2. Sincroniza Gradle con las versiones del repositorio: SDK 37 y JVM 25.
3. En Device Manager, inicia un teléfono o emulador con Android API 26 o superior. Las pruebas de notificaciones se realizaron en un Pixel 6 virtual con Android 14/API 34.
4. Selecciona la configuración **app**, el dispositivo y pulsa **Run**.

El paquete es `com.example.notesbysubject`. La configuración de Firebase está en `app/google-services.json` y el plugin Google Services se aplica al encontrar ese archivo. Se mantienen las versiones originales de dependencias, plugins y wrapper.

## Firebase

| Configuración | Valor |
|---|---|
| Proyecto | LevelMate |
| ID | `levelmate-56e8c` |
| Plan | Spark, sin facturación vinculada |
| App Android | `com.example.notesbysubject` |
| Authentication | Correo electrónico y contraseña |
| Firestore | Standard, base `(default)`, región `europe-southwest1` (Madrid) |
| Reglas | Base creada en producción; acceso limitado a propietario y participantes |

La configuración Android contiene los identificadores públicos del cliente. Las contraseñas, cuentas de servicio y claves privadas de firma se mantienen fuera del repositorio. Las fotos se guardan en el dispositivo y se comparten mediante intents; la app utiliza Spark y no requiere Firebase Storage.

- [Configuración de la app Android](https://console.firebase.google.com/project/levelmate-56e8c/settings/general/android:com.example.notesbysubject)
- [Proveedor de acceso](https://console.firebase.google.com/project/levelmate-56e8c/authentication/providers)
- [Reglas de Firestore](https://console.firebase.google.com/project/levelmate-56e8c/firestore/databases/-default-/security/rules)

## Uso de la aplicación

- **New project:** crea un proyecto con nombre, descripción y fecha opcional. El nombre admite hasta 40 caracteres y la descripción, hasta 200. La fecha se introduce como `dd/MM/yyyy`.
- **Projects:** abre el detalle de un proyecto para editarlo, añadir materiales con cantidades y unidades, marcar pasos o asociar fotos.
- **Camera:** muestra la cámara, el nivel y el botón **Scan** para leer texto de una captura.
- **General photos:** muestra las fotos sin proyecto y permite recuperar capturas anteriores. Al borrar un proyecto, sus fotos pasan a esta lista.
- **Settings:** guarda la hora en formato de 24 horas, el estado del recordatorio y la tolerancia del nivel entre 0,1 y 15 grados.
- **Share / import projects:** permite compartir con otra cuenta o intercambiar un archivo JSON.

### Compartir con otra cuenta

1. Entra en **Share / import projects → With people**. Usa **Create account** para registrar un correo y una contraseña de al menos 6 caracteres; después, **Sign in** para acceder.
2. La cuenta destinataria debe iniciar sesión y copiar su código con **Copy your code**.
3. En la cuenta propietaria, selecciona el proyecto, pega ese código en **Other person's sharing code** y pulsa **Share / update shared copy**.
4. En la cuenta destinataria, consulta materiales, pasos y fecha. **Save as a local project** guarda una copia independiente en Room.

Cada proyecto admite hasta 10 participantes, incluido su propietario. Para publicar cambios posteriores se vuelve a compartir el mismo proyecto. **Stop sharing** retira la copia de Firestore y solicita confirmación; las copias locales importadas permanecen. Las fotos se comparten por separado desde el detalle.

### Compartir mediante archivos

En **Share / import projects → Project files**, selecciona un proyecto y utiliza **Share project file** o **Save project file**. **Choose project file** permite revisar un JSON antes de pulsar **Import as a new project**. Cada importación genera identificadores nuevos.

Se comprueban la versión, UTF-8, las fechas y los límites de tamaño: 256 KB por archivo, 50 materiales y 100 pasos. Un archivo inválido no crea un proyecto parcial. El JSON incluye nombre, descripción, fecha, materiales y pasos con su estado; las fotos se envían aparte.

## Plantillas: modo grabado, internet y prueba sin conexión

Abre [TemplateRepository.kt](app/src/main/java/com/example/notesbysubject/data/templates/TemplateRepository.kt). La constante está al principio del archivo, antes de `interface TemplateApi`. Cambia el valor de esa misma línea:

```kotlin
const val USE_RECORDED_TEMPLATES = true
```

| Valor | Origen de las plantillas | Mensaje en la pantalla |
|---|---|---|
| `true` | `app/src/main/assets/recorded/project_templates.json` | `Built-in templates` |
| `false`, con conexión | JSON publicado, descargado mediante Retrofit | `Templates loaded from the internet` |
| `false`, si falla la descarga | Copia incluida en assets | `Online templates unavailable. Showing the built-in copy.` |

El modo inicial del repositorio es `true`. El catálogo remoto está en [docs/templates.json de la rama codex/levelmate-local](https://raw.githubusercontent.com/victoriapico-creator/levelMate/codex/levelmate-local/docs/templates.json). Esa rama debe conservarse mientras se utilice esta dirección. El archivo remoto y la copia de assets tienen el mismo contenido: versión 1 y tres plantillas. Ambos modos usan el mismo parser con `ignoreUnknownKeys = true` y el cliente HTTP del proyecto.

### Prueba del modo grabado

1. Guarda la constante con `true` y pulsa **Run** para instalar la compilación actualizada.
2. En la pantalla principal, pulsa **Load templates**. Debe aparecer **Built-in templates**.
3. Selecciona **Use template**. El proyecto debe abrirse con sus materiales y pasos.

### Prueba de la descarga por internet

Sustituye el valor de la constante por `false`:

```kotlin
const val USE_RECORDED_TEMPLATES = false
```

Guarda el archivo, pulsa **Run**, mantén la conexión a internet y vuelve a pulsar **Load templates**. Debe aparecer **Templates loaded from the internet** y la lista de plantillas. Selecciona una para comprobar que crea el proyecto.

### Prueba sin conexión

Mantén `false` en la compilación instalada. Activa el modo avión del emulador y apaga también su Wi-Fi. Vuelve a **Load templates**: debe aparecer el aviso de la copia incluida y deben poder crearse proyectos desde las plantillas. Los errores de red, HTTP, formato JSON o versión utilizan este mismo fallback.

Para dejar la app en el modo inicial de entrega, vuelve a poner `true`, guarda y pulsa **Run**.

## Pruebas de las demás funciones

| Apartado | Pasos | Resultado esperado |
|---|---|---|
| Formulario | Deja el nombre vacío e introduce `31/02/2026` como fecha. Después usa un nombre y una fecha válidos. | Los datos inválidos muestran errores y deshabilitan **Save**; los válidos permiten guardar. |
| Proyectos y Room | Crea un proyecto, añade `Madera · 2 tablas`, un paso y una foto. Marca el paso, detén la app y vuelve a abrirla. | El proyecto, el material, el estado del paso y la foto se conservan. |
| Edición y borrado | Modifica el proyecto y guarda. Prueba **Delete project** con una copia de prueba. | Los cambios se guardan; el borrado pide confirmación y las fotos quedan en **General photos**. |
| Ajustes | Guarda `18:30` y tolerancia `2`, vuelve a entrar en Settings y prueba después `25:00`. | Los valores válidos persisten y la hora inválida deshabilita **Save settings**. |
| Compartir foto | Abre una foto del detalle y pulsa **Share photo**. | Android abre el selector de aplicaciones receptoras con permiso temporal para leer la imagen. |
| Compartir archivo | Exporta un proyecto, selecciónalo con **Choose project file** e impórtalo. | Se crea una copia independiente con materiales, pasos y sus estados. |
| Authentication y Firestore | Usa dos cuentas, comparte un proyecto con el código destinatario y guarda una copia local desde esa cuenta. Prueba también una contraseña incorrecta. | El destinatario recibe el proyecto; el acceso incorrecto muestra un error recuperable. |
| Cámara | Concede el permiso, toma una foto y prueba también denegarlo. | La foto se guarda; con el permiso denegado se muestra la explicación y los botones para solicitarlo o abrir ajustes. |
| Nivel | En la cámara, mueve el dispositivo o usa **Extended Controls → Virtual sensors**. Cambia la tolerancia en Settings. | El ángulo y el color del nivel responden al movimiento y a la tolerancia guardada. |
| Lectura de texto | Enfoca una página legible y pulsa **Scan**. Repite con una imagen sin texto o poco clara. | Se muestra el texto reconocido o el aviso correspondiente cuando no hay texto suficiente. |

La tabla describe cómo repetir las pruebas. Los resultados registrados y el alcance de las comprobaciones están en [docs/VALIDATION.md](docs/VALIDATION.md).

### Probar el recordatorio

1. Crea un proyecto con fecha de hoy o anterior y deja al menos un paso sin marcar.
2. En **Settings**, activa **Daily deadline reminder** y concede el permiso de notificaciones.
3. Mira el reloj del emulador y guarda una hora futura en formato de 24 horas. Por ejemplo, si son las `00:40`, puedes elegir `00:45`.
4. Vuelve a la pantalla de inicio del emulador y mantenlo encendido. Comprueba el panel de notificaciones de Android.

WorkManager permite a Android elegir el momento de ejecución, por lo que el aviso puede retrasarse. Los proyectos sin fecha, con fecha futura o con todos sus pasos terminados quedan fuera del recordatorio. Sin permiso de notificaciones no se publica el aviso. Se utiliza un trabajo diario único para evitar duplicados.

### Pruebas automáticas

En la terminal del proyecto, con el JDK configurado:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Con un emulador o teléfono conectado:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

El resultado de ejecución se comprueba al final de la terminal. Los informes se generan en `app/build/reports/`. La ejecución de `connectedDebugAndroidTest` del 25/11/2026 terminó con `BUILD SUCCESSFUL`.

Si la terminal indica que `JAVA_HOME` no está configurado, mantén Android Studio abierto y ejecuta este bloque en PowerShell:

```powershell
$studioExe = (Get-Process studio64 | Select-Object -First 1).Path
$javaStudio = Join-Path (Split-Path (Split-Path $studioExe -Parent) -Parent) "jbr"
$env:JAVA_HOME = $javaStudio
.\gradlew.bat :app:connectedDebugAndroidTest
```

Las reglas de Firestore se comprueban con Firebase CLI contra el emulador local de demostración:

```shell
firebase emulators:exec --only firestore --project demo-levelmate "node scripts/check-firestore-rules.mjs"
```

## Cobertura y casos incómodos

Las rutas de la tabla parten de `app/src/main/java/com/example/notesbysubject/`.

| Fila | Función | Archivos principales | Pantalla | Caso incómodo |
|---|---|---|---|---|
| A1 | Navegación y formulario con validación y estado | `ui/navigation/AppNavGraph.kt`, `ui/screens/ProjectFormScreen.kt`, `ui/screens/ProjectFormValidation.kt` | Projects, New project | Nombre vacío, fecha inválida y rotación del dispositivo |
| A2 | Lista, detalle, edición y envío de fotos con intent | `ui/screens/ProjectsScreen.kt`, `ui/screens/ProjectDetailScreen.kt`, `ui/photos/PhotoSharing.kt` | Projects, detalle | Lista vacía, proyecto eliminado o foto desaparecida |
| A3 | Ajustes persistentes de hora y tolerancia | `data/settings/SettingsRepository.kt`, `ui/screens/SettingsScreen.kt` | Settings, Camera | Hora inválida y tolerancia fuera de 0,1–15 grados |
| A4 | Proyectos, materiales, pasos y fotos relacionados en Room | `data/local/Entities.kt`, `data/local/ProjectDao.kt`, `data/local/ProjectRepository.kt` | Projects, detalle | Fecha nula, clave foránea inválida y creación sin datos parciales |
| A5 | Plantillas por Retrofit o assets | `data/templates/TemplateRepository.kt`, `data/templates/ProjectTemplate.kt` | Project templates | Sin red, error HTTP o catálogo incompatible: fallback a assets |
| A6 | Acceso y proyectos compartidos en Firestore | `data/sharing/SharingRepository.kt`, `ui/CloudSharingViewModel.kt`, `ui/screens/CloudSharingScreen.kt` | With people | Credenciales inválidas, caché sin red y rechazo de usuarios ajenos |
| A7 | Recordatorio diario con WorkManager y notificación | `reminders/ReminderScheduler.kt`, `reminders/ReminderWorker.kt` | Settings, notificaciones | Permiso denegado, ausencia de pendientes y prevención de trabajos duplicados |
| A8 | Cámara y fotos guardadas en almacenamiento interno | `ui/screens/CameraScreen.kt`, `ui/camera/PhotoStorage.kt` | Camera, detalle | Permiso denegado o almacenamiento no disponible |
| A9 | Nivel con acelerómetro y tolerancia configurable | `ui/camera/LevelCalculator.kt`, `ui/camera/LevelIndicator.kt`, `ui/screens/CameraScreen.kt` | Camera | Acelerómetro ausente y retirada del listener al pausar |
| A10 | Lectura de texto de una foto con ML Kit | `ui/camera/TextScanner.kt`, `ui/camera/RecognizedTextFilter.kt`, `ui/camera/ScanResultSheet.kt` | Camera, Scan | Imagen sin texto, confianza insuficiente o error de lectura |

## Decisiones de diseño

1. Room guarda los proyectos y sus relaciones en el dispositivo para que el trabajo local pueda continuar sin conexión. La creación desde plantillas o archivos se hace en una transacción para evitar datos parciales.
2. Las fotos permanecen en `filesDir/notes/` y se comparten con FileProvider y un permiso temporal de lectura. Así se mantiene el almacenamiento local y el uso de Firebase Spark.
3. Las plantillas remotas y grabadas tienen el mismo formato y parser. La copia de assets permite utilizar la función cuando falla la descarga.

## Validación y registro

[docs/VALIDATION.md](docs/VALIDATION.md) recoge los resultados de compilación, tests, backend y recorridos de la app. Los registros de desarrollo están en [prompts/sofia.md](prompts/sofia.md) y [prompts/victoria.md](prompts/victoria.md).

## Release firmada (C2)

La compilación de desarrollo genera un APK debug. El bundle `.aab` firmado de C2 está pendiente; debe generarse con **Build → Generate Signed Bundle / APK → Android App Bundle**. La clave de firma se guarda fuera del repositorio. El `.aab` firmado se entrega por el canal indicado en el enunciado.

## Referencia del curso

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
