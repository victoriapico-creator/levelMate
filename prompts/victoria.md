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

- **What happened:** (a rellenar)
