# Contributing to Timber

Thanks for your interest in Timber. This document covers how to get a working build, how to run
the tests, and the conventions the codebase already follows.

## Getting set up

### Prerequisites

- **JDK 21.** The Gradle daemon's JVM is pinned in `gradle/gradle-daemon-jvm.properties`
  (`toolchainVendor=JETBRAINS`, `toolchainVersion=21`). If no matching JVM is installed, Gradle
  downloads one via the [foojay resolver](https://github.com/gradle/foojay-toolchains) configured in
  `settings.gradle.kts`, so the first build may take a while.
- **Android Studio** (Narwhal or newer) with the Android SDK for **API 36** installed.
- **An emulator or device on API 34 or higher.** `minSdk` is 34.

### Do not upgrade the Gradle wrapper past 9.5.0

`gradle/wrapper/gradle-wrapper.properties` pins **Gradle 9.5.0** deliberately. Gradle 9.7.1 fails to
sync against the project's AGP version with `Service 'SystemInfo' is not available`. If Android
Studio offers to upgrade the wrapper, decline it.

The distribution's SHA-256 is pinned alongside the URL via `distributionSha256Sum`, so if you do
change the Gradle version you have to update the checksum too — get it from
`https://services.gradle.org/distributions/gradle-<version>-bin.zip.sha256`. A stale checksum makes
the wrapper refuse to run rather than fail obscurely later.

### Build and run

```bash
./gradlew assembleDebug        # build
./gradlew installDebug         # build and install on the connected device
```

A fresh install starts with an empty workout history. Only the reference catalog — the 55 default
exercises and the ten default templates — is seeded, by `DatabaseInitializer.ensureCatalogSeeded()`
on startup.

If you need populated charts to work on the stats or history screens, debug builds expose a
**Developer** section at the bottom of Settings with *Generate 60 days of demo history* and *Clear
workout history*. The generated data is deterministic (`Random(42)`) and regenerating replaces the
previous set rather than stacking onto it. The section is compiled out of release builds via the
`DEVELOPER_TOOLS` flag in `app/build.gradle.kts`.

## Running the tests

Both suites should pass before you open a pull request.

```bash
./gradlew testDebugUnitTest        # JUnit 4 + MockK + Turbine, no device needed
./gradlew connectedDebugAndroidTest # Compose UI + Room, needs a running emulator/device
./gradlew lintDebug                # Android Lint
```

Instrumentation tests run through a custom runner (`HiltKaspressoRunner`) and use Kaspresso on top
of the Compose test APIs. They manage their own fixtures through `DatabaseSeedingRule`, which calls
the production `DatabaseSeeder.seedCatalog()`; startup auto-seeding detects the instrumentation
environment and stays out of the way.

### Writing instrumentation tests

- **Use the helpers in `app/src/androidTest/java/com/android/timberworkoutlogs/util/Helpers.kt`**
  rather than asserting directly. Every test starts with ten seeded templates and 55 seeded
  exercises, so anything you create in a test is usually pushed below the fold.
  `scrollToAndAssertElement(...)` and `scrollToAndAssertClickElement(...)` scroll it into view first
  and retry briefly to absorb animation settling. A bare `onNodeWithText(x).assertIsDisplayed()`
  will pass locally and fail on a smaller screen.
- **Grant `POST_NOTIFICATIONS`** with a `GrantPermissionRule` in any test that touches the workout
  screen — it starts the rest timer, which is a foreground service.

## Architecture and conventions

The class diagram in [`docs/uml/timber_classes.png`](docs/uml/timber_classes.png) is the quickest
way to get oriented (the editable source is `timber_classes.uml` alongside it).

- **MVVM with a repository layer.** ViewModels never talk to a DAO directly — go through the
  matching `*Repository`, and add a pass-through method there if one doesn't exist yet.
- **One package per feature** under `ui/screen/<feature>/`, with shared composables in a nested
  `components/` package.
- **State hoisting for previews.** Screens follow a `@Immutable *ScreenState` /
  `@Immutable *ScreenActions` pair fed into a private, stateless `*ScreenContent` composable. The
  public composable does the `collectAsStateWithLifecycle` wiring. Keep this shape — it's what makes
  `@Preview` work without a ViewModel.
- **Hilt for injection.** ViewModels are `@HiltViewModel` with constructor injection.
- **Offline-first is a hard constraint.** The manifest declares no `INTERNET` permission. Don't add
  one, and don't add a dependency that needs one.
- **Settings live in DataStore**, not SharedPreferences — see `SettingsRepository`.

### Touching the database

- The Room schema is at **version 5**. Any change to an entity needs the version bumped *and* a
  `Migration` added to the `addMigrations(...)` call in `Database.kt`. There are three existing
  migrations to copy the style from.
- `ExerciseSet` is a sealed interface persisted as polymorphic JSON in a single TEXT column via a
  kotlinx.serialization `@TypeConverter`. New set types must be registered in the sealed hierarchy,
  not added as columns.
- **Adding a method to `WorkoutDao` also means updating `FakeWorkoutDao`** in
  `WorkoutHistoryScreen.kt` — it's a preview-only implementation of the full interface, and the
  build breaks without it.

### Weights and units

Weight is stored per-`WorkoutExercise` alongside the unit it was entered in. Always convert through
`WeightUnitConverter` rather than multiplying inline; it also clamps to the app's 2-decimal display
precision, including on the same-unit path.

## Pull requests

- Branch off `main`.
- Keep commits focused, and split unrelated concerns into separate commits.
- Match the existing commit subject style: a short imperative sentence, prefixed with `Fix: ` for
  bug fixes (`Fix: swipe-to-delete a set could remove the wrong set when values duplicate`).
- Cover new behaviour with tests where it's practical — a ViewModel unit test for logic, an
  instrumentation test for a new screen or flow.
- Make sure `testDebugUnitTest`, `connectedDebugAndroidTest`, and `lintDebug` all pass. CI runs the
  unit tests and lint on every PR; the instrumentation suite currently needs to be run locally.

## Reporting bugs

Open an issue using the bug report template and include your device/emulator API level. If the bug
involves weights, say which unit you were in — a good number of past bugs have been unit-conversion
edge cases.
