# LevelMate

Android application built with Kotlin and Jetpack Compose to organise workshop projects. It stores materials, steps and photos, loads project templates, displays a camera level and sends deadline reminders. Projects are stored in Room; reminder settings and level tolerance are stored in DataStore. Firebase Authentication and Firestore allow project plans to be shared between accounts.

## Open and run

1. Open the project folder containing `settings.gradle.kts`, `app` and `gradlew.bat` in Android Studio.
2. Sync Gradle using the repository versions: SDK 37 and JVM 25.
3. In Device Manager, start a phone or emulator running Android API 26 or later. Notification checks used a Pixel 6 virtual device running Android 14/API 34.
4. Select the **app** configuration and the device, then click **Run**.

The application ID is `com.example.notesbysubject`. Firebase configuration is in `app/google-services.json`; the Google Services plugin is applied when this file is present. Dependency, plugin and wrapper versions remain the same as in the original project.

## Firebase

| Setting | Value |
|---|---|
| Project | LevelMate |
| Project ID | `levelmate-56e8c` |
| Plan | Spark, with no billing account attached |
| Android application | `com.example.notesbysubject` |
| Authentication | Email and password |
| Firestore | Standard, `(default)` database, `europe-southwest1` region (Madrid) |
| Rules | Database created in production mode; access limited to the owner and participants |

The Android configuration contains public client identifiers. Passwords, service accounts and private signing keys are kept outside the repository. Photos are stored on the device and shared through intents. The application uses Spark and does not require Firebase Storage.

- [Android application settings](https://console.firebase.google.com/project/levelmate-56e8c/settings/general/android:com.example.notesbysubject)
- [Authentication provider](https://console.firebase.google.com/project/levelmate-56e8c/authentication/providers)
- [Firestore rules](https://console.firebase.google.com/project/levelmate-56e8c/firestore/databases/-default-/security/rules)

## Using the application

- **New project:** create a project with a name, description and optional deadline. Names allow up to 40 characters; descriptions allow up to 200. Enter deadlines as `dd/MM/yyyy`.
- **Projects:** open a project to edit it, add materials with quantities and units, mark steps as done or attach photos.
- **Camera:** open the camera preview, view the level and use **Scan** to read text from a captured photo.
- **General photos:** view photos without a project and reload earlier captures. Deleting a project moves its photos to this list.
- **Settings:** save a reminder time in 24-hour format, enable reminders and set the level tolerance between 0.1 and 15 degrees.
- **Share / import projects:** share a project with another account or exchange a JSON file.

### Share with another account

1. Open **Share / import projects → With people**. Use **Create account** to register an email and a password with at least 6 characters, then use **Sign in** for later access.
2. The recipient signs in and uses **Copy your code** to copy their sharing code.
3. In the owner's account, select a project, paste the recipient's code into **Other person's sharing code** and click **Share / update shared copy**.
4. The recipient can read the materials, steps and deadline. **Save as a local project** creates an independent copy in Room.

A project supports up to 10 participants, including its owner. Share the same project again to publish later changes. **Stop sharing** asks for confirmation and removes the Firestore copy; imported local copies remain available. Photos are shared separately from the project detail.

### Share project files

In **Share / import projects → Project files**, select a project and use **Share project file** or **Save project file**. **Choose project file** opens a preview before **Import as a new project** saves it. Every import creates new identifiers.

Imports validate the format version, UTF-8, dates and size limits: 256 KB per file, 50 materials and 100 steps. Invalid files do not create partial projects. The JSON includes the name, description, deadline, materials and steps with their done state; photos are sent separately.

## Templates: recorded mode, internet and offline checks

Open [TemplateRepository.kt](app/src/main/java/com/example/notesbysubject/data/templates/TemplateRepository.kt). The constant is near the start of the file, immediately before `interface TemplateApi`. Edit the value on that existing line:

```kotlin
const val USE_RECORDED_TEMPLATES = true
```

| Value | Template source | On-screen message |
|---|---|---|
| `true` | `app/src/main/assets/recorded/project_templates.json` | `Built-in templates` |
| `false`, with a connection | Published JSON downloaded through Retrofit | `Templates loaded from the internet` |
| `false`, when the download fails | Recorded copy from assets | `Online templates unavailable. Showing the built-in copy.` |

The repository default is `true`. The remote catalogue is [docs/templates.json on codex/levelmate-local](https://raw.githubusercontent.com/victoriapico-creator/levelMate/codex/levelmate-local/docs/templates.json). Keep that branch available while this address is used. The remote JSON and asset contain the same catalogue: version 1 and three templates. Both paths use the same parser with `ignoreUnknownKeys = true` and the project's HTTP client.

### Check recorded mode

1. Save the constant as `true` and click **Run** to install the updated build.
2. On the main screen, click **Load templates**. The message should be **Built-in templates**.
3. Select **Use template**. The project should open with its materials and steps.

### Check the internet download

Change the constant to `false`:

```kotlin
const val USE_RECORDED_TEMPLATES = false
```

Save the file, click **Run**, keep the device connected to the internet and click **Load templates**. The message should be **Templates loaded from the internet**, followed by the templates. Select one and check that it creates a project.

### Check the offline fallback

Keep `false` in the installed build. Enable airplane mode on the emulator and turn off Wi-Fi as well. Click **Load templates** again. The recorded-copy message should appear, and templates should still create projects. Network failures, HTTP errors, invalid JSON and unsupported catalogue versions use this fallback.

To restore the delivery default, set the constant to `true`, save and click **Run**.

## Checks for the other features

| Feature | Steps | Expected result |
|---|---|---|
| Form | Leave the name empty and enter `31/02/2026` as the deadline. Then use a valid name and date. | Invalid input shows errors and disables **Save**; valid input can be saved. |
| Projects and Room | Create a project, add `Wood · 2 boards`, a step and a photo. Mark the step, stop the app and open it again. | The project, material, step state and photo remain available. |
| Editing and deletion | Edit and save a project. Try **Delete project** with a test copy. | Changes are saved; deletion asks for confirmation and keeps the photos in **General photos**. |
| Settings | Save `18:30` and tolerance `2`, reopen Settings, then try `25:00`. | Valid settings persist and the invalid time disables **Save settings**. |
| Photo sharing | Open a photo in a project and click **Share photo**. | Android opens the receiving-app chooser with temporary permission to read the image. |
| File sharing | Export a project, select it with **Choose project file** and import it. | An independent copy is created with materials, steps and done states. |
| Authentication and Firestore | Use two accounts, share with the recipient's code and save a local copy from that account. Try an incorrect password as well. | The recipient can read the project; incorrect credentials show a recoverable error. |
| Camera | Grant permission and capture a photo. Try denying permission as well. | The photo is saved; denied permission shows an explanation and buttons to request it or open settings. |
| Level | In Camera, move the device or use **Extended Controls → Virtual sensors**. Change the tolerance in Settings. | The angle and indicator colour respond to movement and the saved tolerance. |
| Text recognition | Point the camera at a readable page and click **Scan**. Repeat with an image containing no text or unclear text. | Recognised text is shown, or the corresponding message appears when there is insufficient readable text. |

This table explains how to repeat the checks. Recorded results and their scope are listed in [docs/VALIDATION.md](docs/VALIDATION.md).

### Check the reminder

1. Create a project due today or earlier and leave at least one step unfinished.
2. In **Settings**, enable **Daily deadline reminder** and grant notification permission.
3. Read the emulator's clock and save a future time in 24-hour format. For example, at `00:40`, choose `00:45`.
4. Return to the emulator's home screen, keep it running and check Android's notification panel.

WorkManager lets Android choose the execution time, so the notification can arrive late. Projects without a deadline, with a future deadline or with all steps completed are excluded. Notifications are not posted without permission. A single daily work request prevents duplicates.

### Automated checks

Run from the project terminal with a configured JDK:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

With an emulator or phone connected:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

Check the result at the end of the terminal output. Reports are generated under `app/build/reports/`. The device-test command completed with `BUILD SUCCESSFUL` on 25 November 2026.

If the terminal reports that `JAVA_HOME` is not set, keep Android Studio open and run this PowerShell block:

```powershell
$studioExe = (Get-Process studio64 | Select-Object -First 1).Path
$javaStudio = Join-Path (Split-Path (Split-Path $studioExe -Parent) -Parent) "jbr"
$env:JAVA_HOME = $javaStudio
.\gradlew.bat :app:connectedDebugAndroidTest
```

Use Firebase CLI to check the rules against the local demonstration emulator:

```shell
firebase emulators:exec --only firestore --project demo-levelmate "node scripts/check-firestore-rules.mjs"
```

## Coverage and awkward cases

The file paths below are relative to `app/src/main/java/com/example/notesbysubject/`.

| Row | Feature | Main files | Screen | Awkward case |
|---|---|---|---|---|
| A1 | Navigation and validated form state | `ui/navigation/AppNavGraph.kt`, `ui/screens/ProjectFormScreen.kt`, `ui/screens/ProjectFormValidation.kt` | Projects, New project | Empty name, invalid deadline and device rotation |
| A2 | Project list, detail, editing and photo-sharing intent | `ui/screens/ProjectsScreen.kt`, `ui/screens/ProjectDetailScreen.kt`, `ui/photos/PhotoSharing.kt` | Projects, detail | Empty list, deleted project or missing photo |
| A3 | Persistent reminder and tolerance settings | `data/settings/SettingsRepository.kt`, `ui/screens/SettingsScreen.kt` | Settings, Camera | Invalid time and tolerance outside 0.1–15 degrees |
| A4 | Related projects, materials, steps and photos in Room | `data/local/Entities.kt`, `data/local/ProjectDao.kt`, `data/local/ProjectRepository.kt` | Projects, detail | Null deadline, invalid foreign key and atomic creation |
| A5 | Retrofit and recorded project templates | `data/templates/TemplateRepository.kt`, `data/templates/ProjectTemplate.kt` | Project templates | No network, HTTP error or incompatible catalogue: assets fallback |
| A6 | Authentication and shared Firestore projects | `data/sharing/SharingRepository.kt`, `ui/CloudSharingViewModel.kt`, `ui/screens/CloudSharingScreen.kt` | With people | Invalid credentials, offline cache and denied outsider access |
| A7 | Daily WorkManager reminder and notification | `reminders/ReminderScheduler.kt`, `reminders/ReminderWorker.kt` | Settings, notifications | Denied permission, no pending projects and duplicate prevention |
| A8 | Camera and photos in internal storage | `ui/screens/CameraScreen.kt`, `ui/camera/PhotoStorage.kt` | Camera, detail | Denied permission or unavailable storage |
| A9 | Accelerometer level and configurable tolerance | `ui/camera/LevelCalculator.kt`, `ui/camera/LevelIndicator.kt`, `ui/screens/CameraScreen.kt` | Camera | Missing accelerometer and listener removal when paused |
| A10 | ML Kit text recognition from a saved photo | `ui/camera/TextScanner.kt`, `ui/camera/RecognizedTextFilter.kt`, `ui/camera/ScanResultSheet.kt` | Camera, Scan | No text, insufficient confidence or recognition failure |

## Design decisions

1. Room stores projects and their relationships on the device so local work can continue offline. Template and file imports use a transaction to avoid partial data.
2. Photos remain under `filesDir/notes/` and are shared through FileProvider with temporary read permission. This keeps storage local and supports Firebase Spark.
3. Remote and recorded templates use the same format and parser. The asset copy keeps the feature available when a download fails.

## Validation and development records

[docs/VALIDATION.md](docs/VALIDATION.md) records build, test, backend and manual-check results. Development records are in [prompts/sofia.md](prompts/sofia.md) and [prompts/victoria.md](prompts/victoria.md).

## Signed release (C2)

The release deliverable is an Android App Bundle named `LevelMate-release.aab`. It is submitted separately from the GitHub repository. Signing and verification details are recorded in [docs/RELEASE.md](docs/RELEASE.md).

The release uses alias `levelmate-upload` in `levelmate-upload.jks`. The keystore and its passwords are kept outside the repository in the separate private signing backup. Keep that backup for future releases. To build another signed bundle in Android Studio, choose **Build → Generate Signed Bundle / APK → Android App Bundle**, select the existing keystore and the **release** build type.

A debug certificate is automatically generated for development and is not accepted for publishing on Google Play. C2 uses a separate release certificate. A bundle produced by `bundleRelease` without a signing configuration still needs to be signed before submission.

## Course reference

The following section is the unmodified course reference for the original skeleton. LevelMate uses project templates and shared project plans for the corresponding features.

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
