# LevelMate · Android Studio y Firebase

App Android en Kotlin y Jetpack Compose. Paquete: `com.example.notesbysubject`. Los proyectos, materiales, pasos y fotos se guardan en Room, y los ajustes en DataStore. Authentication y Firestore permiten compartir los datos del proyecto con otras personas. También se conserva la alternativa de compartir archivos sin cuentas.

## Abrir y ejecutar

1. Abre la carpeta del proyecto en Android Studio: debe contener `settings.gradle.kts`, `app` y `gradlew.bat`. En la entrega anterior de Windows se llama `LevelMate-final`.
2. Sincroniza Gradle conservando las versiones existentes: SDK 37 y JVM 25. Ignora las sugerencias de actualizar Kotlin o AGP.
3. Selecciona un teléfono o emulador Android API 26 o superior y pulsa Run. Para comprobar el permiso de notificaciones utiliza API 33 o superior.
4. `app/google-services.json` ya corresponde al proyecto real LevelMate. El plugin Google Services que ya estaba declarado se aplica cuando encuentra ese archivo. No hace falta instalar un complemento de Firebase en ChatGPT ni en Android Studio para ejecutar la app.

## Firebase configurado

| Dato | Valor |
|---|---|
| Proyecto | LevelMate |
| ID | `levelmate-56e8c` |
| Plan | Spark, sin cuenta de facturación vinculada |
| App Android | `com.example.notesbysubject` |
| Inicio de sesión | Correo electrónico y contraseña |
| Firestore | Standard, base `(default)`, región `europe-southwest1` (Madrid) |
| Seguridad | Creada en modo producción; reglas privadas publicadas desde `firestore.rules` |

La configuración Android está en `app/google-services.json`. Contiene los identificadores públicos del cliente Firebase; no contiene una contraseña, clave privada ni cuenta de servicio. Nunca añadas credenciales de administración, contraseñas de usuarios o claves de firma al repositorio. Las fotos permanecen en el teléfono y se comparten mediante intents; no se utiliza Firebase Storage ni Blaze.

- [Configuración de la app Android](https://console.firebase.google.com/project/levelmate-56e8c/settings/general/android:com.example.notesbysubject)
- [Proveedor de acceso](https://console.firebase.google.com/project/levelmate-56e8c/authentication/providers)
- [Reglas de Firestore](https://console.firebase.google.com/project/levelmate-56e8c/firestore/databases/-default-/security/rules)

## Compartir con otra persona (A6)

1. Abre **Share / import projects → With people**.
2. Escribe tu correo y una contraseña de al menos 6 caracteres. Pulsa **Create account** la primera vez; posteriormente usa **Sign in**. No uses tu contraseña de Google: esta es una cuenta propia de LevelMate.
3. La otra persona también crea su cuenta en LevelMate. Copia el código que aparece en **Your sharing code** usando **Copy your code** y te lo pasa.
4. Selecciona uno de tus proyectos, introduce su código en **Other person's sharing code** y pulsa **Share / update shared copy**.
5. La otra persona verá el proyecto en su lista de compartidos. Puede consultar materiales, pasos y fecha, o pulsar **Save as a local project** para guardar una copia independiente en Room.
6. Para publicar cambios posteriores, vuelve a compartir el mismo proyecto. Se conservan los participantes existentes, hasta 10 en total. Solo el propietario modifica o retira la copia de Firestore.
7. **Stop sharing** pide confirmación y retira la copia compartida; las copias locales que ya guardaron los participantes permanecen.

No se envían fotos ni rutas privadas a Firestore. Sin conexión se identifica la lista guardada en caché y los errores son recuperables. Cambiar de cuenta elimina de la pantalla los datos de la cuenta anterior. El resto de funciones locales no requiere iniciar sesión.

## Compartir mediante archivos

En **Share / import projects → Project files**, selecciona un proyecto y usa **Share project file** o **Save project file**. El archivo contiene nombre, descripción, fecha, materiales y pasos con su estado. Las fotos se comparten por separado desde el detalle.

**Choose project file** permite revisar el archivo antes de **Import as a new project**. Cada importación genera identificadores nuevos; cancelar no cambia Room. Se validan formato, versión, UTF-8, fechas, cantidades, tamaño máximo de 256 KB, hasta 50 materiales y 100 pasos. Un archivo inválido no crea un proyecto parcial.

## Otras funciones

- **New project:** nombre, descripción y fecha opcional. El detalle permite editar, añadir materiales con unidades, marcar pasos y tomar o adjuntar fotos.
- **General photos:** fotos sin proyecto y recuperación de capturas anteriores a Room. Borrar un proyecto conserva sus fotos como generales.
- **Settings:** hora, activación del recordatorio y tolerancia del nivel entre 0,1 y 15 grados. Android decide el momento exacto de WorkManager, por lo que la notificación puede llegar tarde.

Para probar el recordatorio, deja un proyecto con fecha de hoy o anterior y al menos un paso pendiente. Guarda una hora futura en formato de 24 horas usando el reloj del emulador: a medianoche `00:45` es distinto de `12:45`. El aviso aparece en el panel de notificaciones de Android; el canal normal no exige una ventana emergente. La persona confirmó su recepción en el nuevo Pixel 6 virtual con Android 14/API 34.

## Llamada real o grabada (A5)

En `data/templates/TemplateRepository.kt` se cambia una sola línea:

```kotlin
const val USE_RECORDED_TEMPLATES = true
```

- `true`: utiliza `app/src/main/assets/recorded/project_templates.json`, sin red. Es el modo inicial de la entrega.
- `false`: utiliza Retrofit para leer `docs/templates.json` de la rama `codex/levelmate-local` publicada en GitHub. Si fallan la red, HTTP, JSON o la versión, muestra un aviso y utiliza la copia grabada.

El JSON está publicado en [la rama de entrega](https://raw.githubusercontent.com/victoriapico-creator/levelMate/codex/levelmate-local/docs/templates.json). Se verificó HTTP 200, versión 1 y tres plantillas. La persona confirmó en el emulador tanto la carga por internet como el aviso de fallback y la creación desde la copia incluida sin conexión. Ambas copias del catálogo son idénticas y utilizan el mismo parser con `ignoreUnknownKeys = true` y el `httpClient` original.

## Cobertura y casos incómodos

| Fila | Implementación | Caso incómodo |
|---|---|---|
| A2 | Lista, detalle, edición e intent para compartir foto | Lista vacía, foto desaparecida o receptor ausente |
| A3 | Hora del recordatorio y tolerancia en DataStore | `25:00` y tolerancias fuera de 0,1–15 rechazadas |
| A4 | Room: proyectos, materiales, pasos y fotos con relaciones | Fecha nula, clave foránea inválida y transacción sin datos parciales |
| A5 | Plantillas Retrofit, JSON publicado y copia en assets | Sin red o JSON inválido activa la copia grabada |
| A6 | Login, Firestore y permisos por propietario/participantes; archivos como alternativa | Credenciales inválidas, sin red, lista vacía y usuario ajeno sin acceso |
| A7 | Trabajo diario único y notificación de proyectos pendientes | Sin permiso o sin pendientes no notifica; no duplica trabajos |
| A8–A10 existentes | Cámara, nivel configurable y reconocimiento ML Kit | Permiso denegado, ausencia de acelerómetro o texto no reconocido |

Las cantidades incluyen unidades. Room guarda las fechas en ISO `yyyy-MM-dd`; el formulario utiliza `dd/MM/yyyy`. Los pasos conservan su posición y estado.

## Validación y registro

Consulta [docs/VALIDATION.md](docs/VALIDATION.md) y [prompts/sofia.md](prompts/sofia.md), entradas SH-04 y SH-05. Se distinguen las pruebas automáticas de los recorridos que ha confirmado la persona en el emulador.

Desde la terminal de Android Studio en Windows:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
.\gradlew.bat :app:connectedDebugAndroidTest
```

Las pruebas instrumentadas necesitan un dispositivo. Las reglas se comprueban con Firebase CLI y el emulador local, sin tocar la base real:

```shell
firebase emulators:exec --only firestore --project demo-levelmate "node scripts/check-firestore-rules.mjs"
```

El catálogo de versiones, los plugins raíz y el wrapper conservan sus versiones. `AppNavGraph.kt` no se modifica en SH-04 ni en SH-05; la opción de Firebase se integra dentro de la pantalla existente de compartir.

## Release (C2)

El APK de desarrollo es debug. No se ha generado una release firmada ni una clave de firma; C2 queda fuera de estas seis tareas. Las claves de firma permanecen fuera del repositorio.

## Referencia original del esqueleto

El apartado siguiente se conserva sin cambios como referencia del curso. La implementación de LevelMate aplica el intercambio de Firestore a proyectos con materiales y pasos.

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
