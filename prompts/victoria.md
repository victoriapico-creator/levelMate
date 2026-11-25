# Logbook of Victoria

Implementation review: verified on 25 November 2026 against the linked commits and integrated build. Original prompts, dates, author and tool names are preserved. Manual results refer to the existing development notes.

## VP-01

- **Date and time:** 2026-11-15 13:00
- **Author:** Victoria
- **Tool:** Gemini Agent, new chat
- **Files touched:** `app/src/main/java/com/example/notesbysubject/MainActivity.kt`, `app/src/main/java/com/example/notesbysubject/ui/navigation/AppNavGraph.kt`, `app/src/main/java/com/example/notesbysubject/ui/screens/CameraScreen.kt`, `app/src/main/java/com/example/notesbysubject/ui/screens/ProjectDetailScreen.kt`, `app/src/main/java/com/example/notesbysubject/ui/screens/ProjectFormScreen.kt`, `app/src/main/java/com/example/notesbysubject/ui/screens/ProjectsScreen.kt`, `app/src/main/java/com/example/notesbysubject/ui/screens/SettingsScreen.kt`, `app/src/main/java/com/example/notesbysubject/ui/screens/SharedScreen.kt`, `app/src/main/java/com/example/notesbysubject/ui/theme/Color.kt`, `app/src/main/java/com/example/notesbysubject/ui/theme/Theme.kt`
- **Result:** Implementation verified — navigation and workshop theme.
- **Commit:** [f8fcde9](https://github.com/victoriapico-creator/levelMate/commit/f8fcde9)
- **How I checked it:**
  - Implementation review: verified — navigation routes and light/dark colour palettes.
  - Integrated checks: confirmed — successful build; see docs/VALIDATION.md.
- **Prompt:**

  Context: Compose app from the course skeleton. MainActivity.kt shows a placeholder screen with the app name. The theme is in ui/theme/. Navigation Compose is already in the dependencies.
  Task: Create a navigation graph in ui/navigation/AppNavGraph.kt with empty screens named Projects, ProjectForm, ProjectDetail, Camera, Settings and Shared, each in its own file under ui/screens/ and showing only its name. Projects is the start destination, with buttons to reach the others. Replace the placeholder in MainActivity.kt with this graph. In ui/theme/, replace the template's purple with a palette based on orange and dark grey (workshop style) for light and dark themes.
  Facts: The app is called LevelMate. Routes are plain text; ProjectDetail will receive a project id later, so name its route "projectDetail/{projectId}".
  Limits: Only touch the new files, MainActivity.kt and ui/theme/. Do not change any version or add any plugin.
  Done when: I run the app, see Projects, can open every other screen with Back returning to Projects, and the colours are not the template's purple in either theme.

- **What happened:**

The navigation graph, six destination screens and workshop theme were added in the implementation commit. Projects is the start destination, and ProjectDetail uses a project-ID route. Navigation and theme definitions were checked during the integrated source review.

## VP-02

- **Date and time:** 2026-11-15 13:10
- **Author:** Victoria
- **Tool:** Gemini Agent, new chat
- **Files touched:** `app/src/main/java/com/example/notesbysubject/ui/components/ProjectFormFields.kt`, `app/src/main/java/com/example/notesbysubject/ui/screens/ProjectFormScreen.kt`, `app/src/main/java/com/example/notesbysubject/ui/screens/ProjectFormValidation.kt`
- **Result:** Implementation verified — project form and validation.
- **Commit:** [7e905c3](https://github.com/victoriapico-creator/levelMate/commit/7e905c3)
- **How I checked it:**
  - Implementation review: verified — saved form state, input limits and deadline validation.
  - Form check: confirmed — the existing note reports a working form. Name and date validation are covered by the passing unit tests.
- **Prompt:**

  Context: Compose app called LevelMate. ProjectFormScreen.kt in ui/screens/ is an empty screen that only shows its name. The navigation graph is in ui/navigation/AppNavGraph.kt and the theme is in ui/theme/.
  Task: Build the project form in ProjectFormScreen.kt with three fields (name, description, deadline) and a Save button. Create a new file ui/components/ProjectFormFields.kt with a stateless composable ProjectFormFields that receives each value, each error message and a callback per change, and draws the three fields with their error message under them. ProjectFormScreen owns the state with rememberSaveable and passes it down. Create a new file ui/screens/ProjectFormValidation.kt with plain Kotlin functions, with no Android code, that validate each field and return an error message or null.
  Facts: Name is required, maximum 40 characters. Description is optional, maximum 200 characters. Deadline is optional, in the format dd/MM/yyyy, and must be a real date. If you parse it with java.time, catch DateTimeParseException or the app closes. Error messages are in English. Save is disabled until all fields are valid. For now Save does nothing, because the database does not exist yet.
  Limits: Only touch ProjectFormScreen.kt and the two new files. Do not change AppNavGraph.kt, any version or any plugin. Use the theme's colours.
  Done when: I open the form, type a name and rotate the emulator, and the text is still there. With an empty name, or a deadline like 31/02/2026 or "abc", I see an error under the field and Save is disabled. With a valid name and an empty or valid deadline, Save is enabled.

- **What happened:**

The form, stateless fields component and validation functions were added. The original note records the early name-error message and a rejected push resolved with git pull followed by git push. The Save action was connected to Room in the later integration work.

## VP-03

- **Date and time:** 2026-11-15 13:20
- **Author:** Victoria
- **Tool:** Gemini Agent (Gemini 3.6 Flash), new chat
- **Files touched:** `app/src/main/AndroidManifest.xml`, `app/src/main/java/com/example/notesbysubject/ui/camera/PhotoStorage.kt`, `app/src/main/java/com/example/notesbysubject/ui/screens/CameraScreen.kt`
- **Result:** Confirmed — camera, permissions and photo storage.
- **Commit:** [438cca2](https://github.com/victoriapico-creator/levelMate/commit/438cca2)
- **How I checked it:**
  - Implementation review: verified against the linked commit.
  - Recorded checks: confirmed — permission dialog, live preview, saved JPEG, denied-permission actions and camera rotation.
- **Prompt:**

  Context: Compose app called LevelMate. CameraScreen.kt in ui/screens/ is an empty screen that only shows its name. Navigation is in ui/navigation/AppNavGraph.kt. CameraX is already in the project dependencies. AndroidManifest.xml has no camera permission yet.
  Task: Build the camera screen in CameraScreen.kt. Add the CAMERA permission to AndroidManifest.xml. When the screen opens, ask for the permission if it is not granted. With the permission, show the CameraX live preview and a shutter button. Tapping the shutter takes a photo and saves it as a JPEG in filesDir/notes/general/, with the file name photo_yyyyMMdd_HHmmss.jpg, creating the folder if it does not exist. After saving, show a short message with the file name. If the permission is denied, show a text that explains why the camera is needed, a button to ask again and a button that opens the app's settings. Put the file-saving logic in a new file ui/camera/PhotoStorage.kt, not inside the composable.
  Facts: filesDir/notes/ is the only folder the project's FileProvider exposes, so photos must live there. Use the CameraX libraries already in the catalog and do not add libraries. The emulator is Android 17, API 37.
  Limits: Only touch CameraScreen.kt, the new file PhotoStorage.kt and the one permission line in AndroidManifest.xml. Do not change AppNavGraph.kt, any version or any plugin.
  Done when: I open Camera on the emulator, the permission dialog appears, I allow it and see the preview. I tap the shutter and see the message with the file name. I deny the permission and see the explanation with both buttons.

- **What happened:**

CameraX preview, permission handling, internal JPEG storage and the denied-permission actions were added. The original entry records acceptance. Later integration connected photos to Room and project detail; those later edits are recorded separately in Sofia's logbook.

## VP-04

- **Date and time:** 2026-11-15 13:35
- **Author:** Victoria
- **Tool:** Gemini Agent (Gemini 3.6 Flash), new chat
- **Files touched:** `app/src/main/java/com/example/notesbysubject/ui/camera/LevelCalculator.kt`, `app/src/main/java/com/example/notesbysubject/ui/camera/LevelIndicator.kt`, `app/src/main/java/com/example/notesbysubject/ui/screens/CameraScreen.kt`
- **Result:** Confirmed — accelerometer level.
- **Commit:** [0e0d1c6](https://github.com/victoriapico-creator/levelMate/commit/0e0d1c6)
- **How I checked it:**
  - Implementation review: verified against the linked commit and level-calculation tests.
  - Recorded checks: confirmed — virtual-sensor movement, changing angle and indicator colour, photo capture and reopening Camera.
- **Prompt:**

  Context: Compose app called LevelMate. CameraScreen.kt in ui/screens/ shows the CameraX preview and a shutter button. Photo saving is in ui/camera/PhotoStorage.kt and must keep working.
  Task: Add a level over the camera preview, using the accelerometer. Create a new file ui/camera/LevelCalculator.kt with plain Kotlin functions, with no Android code, that turn the accelerometer x and y values into a tilt angle in degrees, apply a simple low-pass smoothing, and say whether the angle is within a tolerance. Create a new file ui/camera/LevelIndicator.kt with a composable that shows the angle (for example "2.3°") and a bar or line that tilts with the phone. It is green when the phone is straight and orange when it is not. In CameraScreen.kt, read the accelerometer with SensorManager, register the listener when the screen is shown and unregister it when it leaves, and show the indicator over the preview without covering the shutter button.
  Facts: With the phone upright in portrait, the accelerometer gives x close to 0 and y close to 9.8. The tilt angle in degrees is atan2(x, y) converted from radians. The tolerance is a constant of 1.5 degrees, defined in one place. If the phone has no accelerometer, show a short message "Level not available on this device" instead of the indicator and keep the camera working.
  Limits: Only touch CameraScreen.kt and the two new files. Do not change PhotoStorage.kt, AppNavGraph.kt, any version or any plugin.
  Done when: On the emulator, I open Camera and move the virtual phone in the extended controls (Virtual sensors). The angle changes and the indicator tilts. It is green within 1.5 degrees and orange outside. I can still take a photo.

- **What happened:**

The accelerometer calculation, smoothing and level indicator were added without changing photo storage or navigation. The original note states that it worked on the first try with no changes by hand. The tolerance was later connected to DataStore during integration.

## VP-05

- **Date and time:** 2026-11-15 13:50
- **Author:** Victoria
- **Tool:** Gemini Agent (Gemini 3.6 Flash), new chat
- **Files touched:** `app/src/main/java/com/example/notesbysubject/ui/camera/RecognizedTextFilter.kt`, `app/src/main/java/com/example/notesbysubject/ui/camera/ScanResultSheet.kt`, `app/src/main/java/com/example/notesbysubject/ui/camera/TextScanner.kt`, `app/src/main/java/com/example/notesbysubject/ui/screens/CameraScreen.kt`
- **Result:** Confirmed — text recognition and its recorded error case.
- **Commit:** [a899748](https://github.com/victoriapico-creator/levelMate/commit/a899748)
- **How I checked it:**
  - Implementation review: verified against the linked commit.
  - Recorded checks: confirmed — printed-text recognition through Webcam0, the no-text message, and continued shutter/level operation.
- **Prompt:**

  Context: Compose app called LevelMate. CameraScreen.kt in ui/screens/ shows the CameraX preview, the shutter button and the level. Saving photos is in ui/camera/PhotoStorage.kt and must keep working. The ML Kit Text Recognition library is already in the project dependencies.
  Task: Add a Scan button next to the shutter button in CameraScreen.kt. Tapping it takes a photo, saves it with the existing PhotoStorage code, reads the text in that photo with ML Kit Text Recognition and shows the result in a bottom sheet. Create a new file ui/camera/TextScanner.kt that calls ML Kit and returns the recognized lines with the confidence of each one. Create a new file ui/camera/RecognizedTextFilter.kt with plain Kotlin functions, with no Android code, that keep only the lines whose confidence is at or above the threshold. Create a new file ui/camera/ScanResultSheet.kt with the composable that lists the kept lines, one per row.
  Facts: Use the bundled library already in the version catalog as libs.text.recognition (com.google.mlkit:text-recognition 16.0.1). Do not swap it for play-services-mlkit-text-recognition, because with an older Google Play services the confidence comes back as 0. Confidence is a number between 0 and 1. The threshold is a constant of 0.7, defined in one place. Cases to handle without crashing: no text found, show "No text found. Try again with more light."; text found but all below the threshold, show "The text is not clear enough. Try again."; ML Kit fails, show "Could not read the photo.". If libs.text.recognition is not wired in app/build.gradle.kts, say so and stop, without editing build files.
  Limits: Only touch CameraScreen.kt and the three new files. Do not change PhotoStorage.kt, AppNavGraph.kt, any version or any plugin, and do not add libraries.
  Done when: On the emulator I point the camera at a sheet with printed text, tap Scan and see the sheet with the recognized lines. With a scene with no text I see the "No text found" message. The shutter button and the level still work.

- **What happened:**

The Scan action captures a saved photo, reads its text with the bundled ML Kit library and displays lines meeting the confidence threshold. The existing note records the no-text and readable-text checks. The recognition flow and no-text handling are confirmed by the existing development notes.
