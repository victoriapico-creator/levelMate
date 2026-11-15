# Logbook de Victoria
## VP-01

- **Date and time:** 2026-11-15 13:00
- **Author:** Victoria
- **Tool:** Gemini Agent, new chat
- **Files touched:** (a rellenar después)
- **Result:** (a rellenar después)
- **How I checked it:**
  - What the AI ran: (a rellenar)
  - What I checked myself: (a rellenar)
- **Prompt:**

  Context: Compose app from the course skeleton. MainActivity.kt shows a placeholder screen with the app name. The theme is in ui/theme/. Navigation Compose is already in the dependencies.
  Task: Create a navigation graph in ui/navigation/AppNavGraph.kt with empty screens named Projects, ProjectForm, ProjectDetail, Camera, Settings and Shared, each in its own file under ui/screens/ and showing only its name. Projects is the start destination, with buttons to reach the others. Replace the placeholder in MainActivity.kt with this graph. In ui/theme/, replace the template's purple with a palette based on orange and dark grey (workshop style) for light and dark themes.
  Facts: The app is called LevelMate. Routes are plain text; ProjectDetail will receive a project id later, so name its route "projectDetail/{projectId}".
  Limits: Only touch the new files, MainActivity.kt and ui/theme/. Do not change any version or add any plugin.
  Done when: I run the app, see Projects, can open every other screen with Back returning to Projects, and the colours are not the template's purple in either theme.

- **What happened:**
Files touched: AppNavGraph.kt, las seis pantallas, MainActivity.kt, Color.kt y Theme.kt.
Result: aceptado tal cual (o con los cambios que hayas hecho).
How I checked it: separa lo que hizo la IA de lo tuyo, por ejemplo: “git status mostraba solo los 10 archivos esperados; ejecuté la app, cada botón abre su pantalla y el Back del sistema vuelve a Projects; los colores son naranja y gris oscuro”.
What happened: si no hubo fallos, escribe que salió a la primera. Si echaste en falta un botón de volver en pantalla, apúntalo.

## VP-02

- **Date and time:** 2026-11-15 13:10
- **Author:** Victoria
- **Tool:** Gemini Agent, new chat
- **Files touched:** (a rellenar después)
- **Result:** (a rellenar después)
- **How I checked it:**
  - What the AI ran: (a rellenar)
  - What I checked myself: (a rellenar)
- **Prompt:**

  Context: Compose app called LevelMate. ProjectFormScreen.kt in ui/screens/ is an empty screen that only shows its name. The navigation graph is in ui/navigation/AppNavGraph.kt and the theme is in ui/theme/.
  Task: Build the project form in ProjectFormScreen.kt with three fields (name, description, deadline) and a Save button. Create a new file ui/components/ProjectFormFields.kt with a stateless composable ProjectFormFields that receives each value, each error message and a callback per change, and draws the three fields with their error message under them. ProjectFormScreen owns the state with rememberSaveable and passes it down. Create a new file ui/screens/ProjectFormValidation.kt with plain Kotlin functions, with no Android code, that validate each field and return an error message or null.
  Facts: Name is required, maximum 40 characters. Description is optional, maximum 200 characters. Deadline is optional, in the format dd/MM/yyyy, and must be a real date. If you parse it with java.time, catch DateTimeParseException or the app closes. Error messages are in English. Save is disabled until all fields are valid. For now Save does nothing, because the database does not exist yet.
  Limits: Only touch ProjectFormScreen.kt and the two new files. Do not change AppNavGraph.kt, any version or any plugin. Use the theme's colours.
  Done when: I open the form, type a name and rotate the emulator, and the text is still there. With an empty name, or a deadline like 31/02/2026 or "abc", I see an error under the field and Save is disabled. With a valid name and an empty or valid deadline, Save is enabled.

- **What happened:**
The form works. One thing I do not like: "Name is required" appears as soon as the form opens, before the user has typed anything. [añade aquí cualquier otro fallo o arreglo a mano]. The push was rejected again because my teammate had pushed; I fixed it with git pull and push.

## VP-03

- **Date and time:** 2026-11-15 13:20
- **Author:** Victoria
- **Tool:** Gemini Agent (Gemini 3.6 Flash), new chat
- **Files touched:** (a rellenar después)
- **Result:** (a rellenar después)
- **How I checked it:**
  - What the AI ran: (a rellenar)
  - What I checked myself: (a rellenar)
- **Prompt:**

  Context: Compose app called LevelMate. CameraScreen.kt in ui/screens/ is an empty screen that only shows its name. Navigation is in ui/navigation/AppNavGraph.kt. CameraX is already in the project dependencies. AndroidManifest.xml has no camera permission yet.
  Task: Build the camera screen in CameraScreen.kt. Add the CAMERA permission to AndroidManifest.xml. When the screen opens, ask for the permission if it is not granted. With the permission, show the CameraX live preview and a shutter button. Tapping the shutter takes a photo and saves it as a JPEG in filesDir/notes/general/, with the file name photo_yyyyMMdd_HHmmss.jpg, creating the folder if it does not exist. After saving, show a short message with the file name. If the permission is denied, show a text that explains why the camera is needed, a button to ask again and a button that opens the app's settings. Put the file-saving logic in a new file ui/camera/PhotoStorage.kt, not inside the composable.
  Facts: filesDir/notes/ is the only folder the project's FileProvider exposes, so photos must live there. Use the CameraX libraries already in the catalog and do not add libraries. The emulator is Android 17, API 37.
  Limits: Only touch CameraScreen.kt, the new file PhotoStorage.kt and the one permission line in AndroidManifest.xml. Do not change AppNavGraph.kt, any version or any plugin.
  Done when: I open Camera on the emulator, the permission dialog appears, I allow it and see the preview. I tap the shutter and see the message with the file name. I deny the permission and see the explanation with both buttons.

- **What happened:**
- **Files touched:** ui/screens/CameraScreen.kt (modified), ui/camera/PhotoStorage.kt (new), AndroidManifest.xml (CAMERA permission line only)
- **Result:** Accepted as proposed (commit message: VP-03 Camera with permission).
- **How I checked it:**
  - What the AI ran: [confirma: Gemini only created and edited the files; it did not build or run the app on the emulator]
  - What I checked myself: git status showed only CameraScreen.kt, PhotoStorage.kt and AndroidManifest.xml, and git diff of the manifest showed only the CAMERA permission line. On the emulator the permission dialog appeared, I allowed it and saw the live preview. I tapped the shutter and saw the message with the file name. In Device Explorer the .jpg was in files/notes/general/. With the permission denied, the screen showed the explanation with the two buttons (ask again and open settings). I rotated the emulator and the camera kept working.

## VP-04

- **Date and time:** 2026-11-15 13:35
- **Author:** Victoria
- **Tool:** Gemini Agent (Gemini 3.6 Flash), new chat
- **Files touched:** (a rellenar después)
- **Result:** (a rellenar después)
- **How I checked it:**
  - What the AI ran: (a rellenar)
  - What I checked myself: (a rellenar)
- **Prompt:**

  Context: Compose app called LevelMate. CameraScreen.kt in ui/screens/ shows the CameraX preview and a shutter button. Photo saving is in ui/camera/PhotoStorage.kt and must keep working.
  Task: Add a level over the camera preview, using the accelerometer. Create a new file ui/camera/LevelCalculator.kt with plain Kotlin functions, with no Android code, that turn the accelerometer x and y values into a tilt angle in degrees, apply a simple low-pass smoothing, and say whether the angle is within a tolerance. Create a new file ui/camera/LevelIndicator.kt with a composable that shows the angle (for example "2.3°") and a bar or line that tilts with the phone. It is green when the phone is straight and orange when it is not. In CameraScreen.kt, read the accelerometer with SensorManager, register the listener when the screen is shown and unregister it when it leaves, and show the indicator over the preview without covering the shutter button.
  Facts: With the phone upright in portrait, the accelerometer gives x close to 0 and y close to 9.8. The tilt angle in degrees is atan2(x, y) converted from radians. The tolerance is a constant of 1.5 degrees, defined in one place. If the phone has no accelerometer, show a short message "Level not available on this device" instead of the indicator and keep the camera working.
  Limits: Only touch CameraScreen.kt and the two new files. Do not change PhotoStorage.kt, AppNavGraph.kt, any version or any plugin.
  Done when: On the emulator, I open Camera and move the virtual phone in the extended controls (Virtual sensors). The angle changes and the indicator tilts. It is green within 1.5 degrees and orange outside. I can still take a photo.

- **What happened:**
- **Files touched:** ui/screens/CameraScreen.kt (modified), ui/camera/LevelCalculator.kt (new), ui/camera/LevelIndicator.kt (new)
- **Result:** Accepted as proposed (commit message: VP-04 Level with accelerometer).
- **How I checked it:**
  - What the AI ran: [confirma: Gemini only created and edited the files; it did not run the app on the emulator]
  - What I checked myself: git status showed only those three files. On the emulator I opened Camera and moved the virtual phone in Virtual sensors, Device pose. The angle changed and the indicator tilted. It was green when the phone was straight and orange when it was tilted. I could still take a photo, and leaving and re-entering Camera did not crash.
- **What happened:** It worked on the first try, with no changes by hand.

## VP-05

- **Date and time:** 2026-11-15 13:50
- **Author:** Victoria
- **Tool:** Gemini Agent (Gemini 3.6 Flash), new chat
- **Files touched:** (a rellenar después)
- **Result:** (a rellenar después)
- **How I checked it:**
  - What the AI ran: (a rellenar)
  - What I checked myself: (a rellenar)
- **Prompt:**

  Context: Compose app called LevelMate. CameraScreen.kt in ui/screens/ shows the CameraX preview, the shutter button and the level. Saving photos is in ui/camera/PhotoStorage.kt and must keep working. The ML Kit Text Recognition library is already in the project dependencies.
  Task: Add a Scan button next to the shutter button in CameraScreen.kt. Tapping it takes a photo, saves it with the existing PhotoStorage code, reads the text in that photo with ML Kit Text Recognition and shows the result in a bottom sheet. Create a new file ui/camera/TextScanner.kt that calls ML Kit and returns the recognized lines with the confidence of each one. Create a new file ui/camera/RecognizedTextFilter.kt with plain Kotlin functions, with no Android code, that keep only the lines whose confidence is at or above the threshold. Create a new file ui/camera/ScanResultSheet.kt with the composable that lists the kept lines, one per row.
  Facts: Use the bundled library already in the version catalog as libs.text.recognition (com.google.mlkit:text-recognition 16.0.1). Do not swap it for play-services-mlkit-text-recognition, because with an older Google Play services the confidence comes back as 0. Confidence is a number between 0 and 1. The threshold is a constant of 0.7, defined in one place. Cases to handle without crashing: no text found, show "No text found. Try again with more light."; text found but all below the threshold, show "The text is not clear enough. Try again."; ML Kit fails, show "Could not read the photo.". If libs.text.recognition is not wired in app/build.gradle.kts, say so and stop, without editing build files.
  Limits: Only touch CameraScreen.kt and the three new files. Do not change PhotoStorage.kt, AppNavGraph.kt, any version or any plugin, and do not add libraries.
  Done when: On the emulator I point the camera at a sheet with printed text, tap Scan and see the sheet with the recognized lines. With a scene with no text I see the "No text found" message. The shutter button and the level still work.

- **What happened:**
- **Files touched:** ui/screens/CameraScreen.kt (modified), ui/camera/TextScanner.kt, ui/camera/RecognizedTextFilter.kt and ui/camera/ScanResultSheet.kt (new)
- **Result:** Accepted as proposed (commit message: VP-05 Text scan with ML Kit).
- **How I checked it:**
  - What the AI ran: [confirma: Gemini only created and edited the files; it did not run the app on the emulator]
  - What I checked myself: git status showed only those four files, with no change in build files or versions. On the emulator, with the virtual scene and no text, Scan showed "No text found. Try again with more light." and the app did not crash. After setting the back camera to Webcam0, I pointed it at printed text, tapped Scan and the sheet listed the recognized lines. [confirma: qué pasó con texto borroso o lejano, si lo probaste]. The shutter button and the level still work.
- **What happened:** [confirma: it worked on the first try, with no changes by hand. Añade cualquier fallo real, por ejemplo si tuviste que configurar la webcam]
