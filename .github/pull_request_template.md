## What does this change?

<!-- A short description, plus a link to the issue if there is one. -->

## Why?

<!-- What problem does it solve, or what was the bug? -->

## Checklist

- [ ] `./gradlew testDebugUnitTest` passes
- [ ] `./gradlew connectedDebugAndroidTest` passes (CI doesn't run this — please run it locally)
- [ ] `./gradlew lintDebug` passes
- [ ] New behaviour is covered by tests, or there's a note below explaining why not
- [ ] Any Room entity change bumps the schema version and adds a `Migration` in `Database.kt`
- [ ] The Gradle wrapper version is unchanged (it's pinned to 9.4.1 on purpose — see CONTRIBUTING.md)

## Screenshots

<!-- For UI changes. Before/after is ideal. -->
