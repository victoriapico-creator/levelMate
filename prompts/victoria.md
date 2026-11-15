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

- **What happened:** (a rellenar después)
