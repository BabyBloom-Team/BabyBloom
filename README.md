# BabyBloom

An Android graduation project that combines interactive early-learning activities, adaptive session planning, and parent-facing progress insights.

Developed by the BabyBloom project team. Organization: [BabyBloom-Team](https://github.com/BabyBloom-Team).

## Explore the project

- Child profiles, an initial assessment, and personalized learning sessions.
- Counting, matching, dragging, tracing, listening, speech, and story activities.
- Adaptive activity recommendations and progress tracking.
- Parent dashboards, child analytics, notifications, and parental controls.
- Gemini-powered educational insights for parents when configured.

This repository contains a graduation-project implementation. Public release preparation is in progress; see the [publishing checklist](docs/PUBLISHING.md) for outstanding work.

## Technology

| Area | Implementation |
| --- | --- |
| Android interface | Kotlin, Jetpack Compose, Material 3 |
| UI state | ViewModels, coroutines, Flow |
| Dependency injection | Hilt |
| Local persistence | Room and DataStore |
| Learning logic | Assessment and session planners, adaptive algorithm |
| Camera and face detection | CameraX and ML Kit |
| Media | Media3 / ExoPlayer |
| Parent insights | Gemini API |

## Run locally

1. Clone or download this repository and open its root folder in Android Studio.
2. Install Android SDK Platform 36. Use a Gradle JDK compatible with Android Gradle Plugin 8.13.2; JDK 17 is the baseline requirement.
3. Let Android Studio configure the SDK location in your local `local.properties` file and sync Gradle. The project includes the Gradle 8.13 wrapper.
4. For local AI development, append the entries from [local.properties.example](local.properties.example) to `local.properties`. Preserve the existing SDK setting. An empty key allows compilation but cannot generate new AI insights.
5. Run the `app` configuration on an emulator or device running Android 9 / API 28 or newer.

Use fictional child profiles during development and demonstrations. Camera and microphone features need their corresponding runtime permissions. AI generation requires internet access, and the current speech activity flow also checks connectivity.

### Build and test

From PowerShell at the repository root:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest
```

On macOS or Linux, use `./gradlew` instead of `.\gradlew.bat`. The debug APK is generated under `app/build/outputs/apk/debug/`. Debug builds are for development; use the signing and verification steps in the publishing checklist before distributing an app.

These are local validation commands, not a claim that every current test passes.

## Data and accounts

Room stores each installation's users, child profiles, sessions, and progress on that device. The current registration and login are local: they do not create a cloud account or synchronize data between phones.

Room is suitable for a published Android application. Cloud account recovery and shared family access would require additional authentication, backend storage, and synchronization. Android backup behavior depends on device settings and the app's backup rules; it is not cross-device account synchronization.

The app is not entirely offline: configured AI generation sends insight context to Gemini, and some speech functionality needs connectivity. Review data handling before using real child information.

## Code map

```text
app/src/main/java/com/babybloom/
  data/           Room entities, DAOs, repositories, and seed data
  di/             Dependency injection and database configuration
  domain/         Learning algorithms, models, progress, and insights
  navigation/     App navigation
  presentation/   Compose screens, components, and ViewModels
  ui/             Shared UI resources
  util/           Supporting utilities
app/src/main/assets/   Bundled learning content
app/src/test/          JVM unit tests
app/schemas/           Room schema history
```

## Release preparation

Before public distribution, address the embedded Gemini key, password storage, database migration safety, backup policy, and child-data handling. Keeping a key out of Git does not protect it inside an APK: the current build compiles the configured key into the application.

See [docs/PUBLISHING.md](docs/PUBLISHING.md) for the organization setup, source-publication checks, and signed Android release process.

## Team and reuse

Preserve the original project's Git history and contributor attribution when publishing under the organization. Team membership and acknowledgments should be completed with the project contributors.

A project license has not yet been selected. Public visibility alone does not grant general permission to redistribute or modify the project. Before presenting it as open source, the project owners should agree on a license and verify the rights and attribution requirements for bundled images, audio, stories, fonts, and other third-party material.
