# Signed release (C2)

## Submission artifact

The signed Android App Bundle is delivered separately as `LevelMate-release.aab`. Submit this file through the coursework submission channel. The Git repository contains the source and documentation; signing material stays outside it.

| Property | Verified value |
|---|---|
| Application ID | `com.example.notesbysubject` |
| Version | 1.0, version code 1 |
| Build type | release |
| Debuggable | false |
| Minimum SDK | 26 |
| Target SDK | 37 |
| File size | 35,062,926 bytes |
| SHA-256 | `eb03086f6c5bbf92d6dbb647a5b2a13347329584e1df8a2164101f277a11f90b` |

Application sources match commit `8aefb8f`. The final delivery changes documentation and development records, without changing Android source files or dependency versions.

## Build and checks

The release build used Gradle 9.5.0, SDK Platform 37.0, Build Tools 36.0.0 and JDK 25. The equivalent wrapper command is:

```powershell
.\gradlew.bat :app:bundleRelease :app:testDebugUnitTest :app:lintRelease
```

The build completed with `BUILD SUCCESSFUL` on 25 November 2026.

| Check | Result |
|---|---|
| Unit tests | 37 passed, 0 failures, 0 errors and 0 skipped; 8 suites |
| Release lint | 0 errors, 12 warnings and 3 hints |
| Google Services | Release configuration processed successfully |
| Bundle structure | `bundletool validate` passed |
| Signature | `jarsigner -verify -strict` passed against the release keystore and alias |
| Manifest | Application ID, version code, SDK levels and `debuggable=false` checked |

## Signing key

| Property | Value |
|---|---|
| Keystore file | `levelmate-upload.jks` |
| Alias | `levelmate-upload` |
| Certificate subject | `CN=LevelMate` |
| Key algorithm | RSA, 2048 bits |
| Signature algorithm | SHA256withRSA |
| Valid from | 25 November 2026 |
| Valid until | 12 April 2054 |
| Certificate SHA-256 | `C8:A2:EB:DB:4A:1B:AE:D8:77:BA:09:7D:42:1E:A3:CC:CD:F1:C3:51:7C:82:6A:27:7C:F8:00:8F:74:E0:4B:E7` |

The private keystore and its passwords are kept in the separate `LevelMate_PRIVATE_signing_backup.zip`, outside the repository. Keep this backup private for future builds. It is not part of the coursework submission.

The signed bundle uses this release certificate, not the automatically generated Android debug certificate. Debug certificates are intended for local development and cannot be used to publish an application on Google Play.

## Build another signed release

1. Extract the private signing backup into a folder outside the project.
2. In Android Studio, choose **Build → Generate Signed Bundle / APK → Android App Bundle**.
3. Select the **app** module and the existing `levelmate-upload.jks` file.
4. Enter alias `levelmate-upload` and the passwords stored in the private backup.
5. Select the **release** build type, choose an output folder and finish the build.
6. Submit the signed `.aab` file and keep the private backup separate.

Without a signing configuration, the command-line `bundleRelease` output needs a separate signing step. This delivery was signed with `jarsigner` after compilation. App bundles use JAR signing; `apksigner` is for APKs.

## References

- [Android: sign your app](https://developer.android.com/studio/publish/app-signing)
- [Android: build and sign from the command line](https://developer.android.com/build/building-cmdline)
