# LevelMate validation

## Build and automated tests

The Firebase build checks on 24 November 2026 used the repository versions and the real Android configuration:

| Check | Recorded result |
|---|---|
| Unit tests | 37 passed, 0 failures, 0 errors and 0 skipped; 8 suites |
| Debug APK | Built with Authentication and Firestore |
| Google Services | `processDebugGoogleServices` completed |
| Lint | 0 errors, 13 warnings and 3 hints |
| Instrumented-test APK | Built successfully |
| Versions | Original version catalogue, root plugins and wrapper preserved |

Build and check command:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:lintDebug :app:assembleDebugAndroidTest
```

The build used SDK 37, Build Tools 36.0.0 and JVM 25. The 37 tests cover project validation, settings, level calculations, reminder times, templates, 13 file-transfer cases and 6 shared-project cases.

### Device tests

On 25 November 2026, the following command was run on Windows with the emulator open:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest
```

The reported terminal result was `BUILD SUCCESSFUL`. The development device was a Pixel 6 virtual device running Android 14/API 34. The XML report and individual test count were not attached to this record.

`ProjectDatabaseTest` covers an empty database, a missing project, deletion while keeping general photos, invalid foreign keys, atomic creation and exclusion of completed projects from reminders.

## Firebase

| Setting | Verified value |
|---|---|
| Project | `levelmate-56e8c` |
| Plan | Spark, with no billing account attached |
| Android application | `com.example.notesbysubject` |
| Authentication | Email and password |
| Firestore | Standard, `(default)` database, `europe-southwest1`, created in production mode |
| Rules | Published from `firestore.rules`, with owner/participant access |

`app/google-services.json` contains the project's Android client configuration. Photos remain on the device; Firebase Storage is not used.

### Rules and services

| Test | Result | Coverage |
|---|---|---|
| Local Firestore emulator | 21 checks passed | Publishing with `arrayUnion`, participant preservation and limits, ownership, filtered queries, unexpected fields and denied anonymous/outsider access |
| Live services through HTTPS REST | 9 checks passed | Two temporary accounts, login, incorrect password, publishing, recipient reads and permissions |

The temporary document and two accounts were deleted afterwards; all three cleanup requests returned HTTP 200. Test passwords and tokens stayed in memory and are not included in the repository.

`scripts/check-firestore-rules.mjs` only accepts localhost and the demonstration project `demo-levelmate`.

## Manual checks

The initial checks used a Pixel 6 virtual device on API 37. Final checks used Android 14/API 34.

| Feature | Check | Recorded result |
|---|---|---|
| Project | Create `Test`, enter a description and deadline, then open the detail | Data displayed in the detail |
| Materials | Add a material with quantity and units | Material and quantity saved |
| Steps | Mark a step, stop the app and reopen it | Done state preserved |
| Photos | Capture a photo, stop the app and reopen it | Photo saved and displayed |
| Settings | Save 18:30 and tolerance 2, then try 25:00 | Valid values persisted; invalid time rejected |
| Photo sharing | Open the intent from a project photo | Receiving-app chooser displayed |
| Files | Export and import a project | Independent copy with materials and steps |
| Firestore | Use two accounts, share through the recipient code and import a local copy | Sharing and project-content preservation confirmed |
| Incorrect credentials | Try an incorrect password, then the correct one | Error/recovery flow confirmed as a whole; no individual screenshot |
| Reminder | Select a future time, grant permission and leave a due project with an unfinished step | Notification received |
| Recorded templates | Load the built-in catalogue and create a project | Materials and steps available offline |
| Online templates | Build with `USE_RECORDED_TEMPLATES = false` and load with a connection | Actual internet download confirmed |
| Template fallback | Keep `false`, enable airplane mode and turn off Wi-Fi | Built-in-copy message and offline project creation confirmed |

The reminder check used the emulator's clock in 24-hour format. WorkManager may delay execution and does not provide an exact alarm.

Victoria's historical camera, virtual-sensor and text-recognition observations are preserved in [prompts/victoria.md](../prompts/victoria.md). File lists are linked to their original implementation commits. Additional personal observations are not inferred from successful compilation.

## Publication

[Pull request #1](https://github.com/victoriapico-creator/levelMate/pull/1) was merged into `main` on 25 November 2026. The six tasks A4, A3, A5, A2, A7 and A6 are integrated in the main branch.

The [remote template catalogue](https://raw.githubusercontent.com/victoriapico-creator/levelMate/codex/levelmate-local/docs/templates.json) returned HTTP 200 with version 1 and three templates matching the recorded asset. This address uses `codex/levelmate-local`, so that branch is retained. The default is `USE_RECORDED_TEMPLATES = true`.

## Signed release

On 25 November 2026, `bundleRelease`, `testDebugUnitTest` and `lintRelease` completed with `BUILD SUCCESSFUL`. All 37 unit tests passed again; release lint reported 0 errors, 12 warnings and 3 hints. The signed bundle passed strict JAR-signature verification and `bundletool validate`; its manifest has `debuggable=false`.

The artifact hash, release certificate and rebuild instructions are recorded in [RELEASE.md](RELEASE.md). `LevelMate-release.aab` is delivered separately from Git. Its private signing backup is stored outside the project.

## Scope

- Build checks, backend checks and manual observations are recorded separately.
- Denied notification permission, no pending projects and unique work are handled in code; this record does not include an individual manual screenshot of every case.
- Separate observations of physical-level accuracy and blurred/distant-text recognition are only included when recorded in the development logbook.
