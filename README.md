# Timber: Workout Logs

Timber is a modern, offline-first workout logging application for Android, built with 100% Kotlin
and Jetpack Compose. It's designed for fitness enthusiasts who want a simple, clean, and efficient
way to track their strength training progress.

## Features

- **Intuitive Logging:** Easily log your workouts, including exercises, sets, reps, and weight.
- **Exercise Library:** A pre-loaded, customizable library of common strength training exercises.
- **Workout Templates:** Create and save your own workout routines to start logging faster.
- **Pre-built Programs:** Comes with popular, proven workout templates like StrongLifts 5x5 and
  Push/Pull/Legs (PPL) to get you started.
- **History & Progress:** (Coming Soon) Visualize your progress over time with charts and
  statistics.
- **Offline First:** Designed to work completely offline. Your data is stored locally and is always
  available.
- **Material Design 3:** A clean, modern, and dynamic user interface built with the latest Material
  Design components.

## Screenshots

| Workout List | Exercise Library | Logging Screen |
| :----------: | :--------------: | :------------: |
| *(Image of the main workout history screen)* | *(Image of the exercise library with search)* | *(Image of the screen where a user logs sets/reps)* |

## Tech Stack & Architecture

Timber is built using modern Android development practices and libraries.

- **UI:** 100% [Jetpack Compose](https://developer.android.com/jetpack/compose) using the Material 3
  design system.
- **Architecture:** Follows a standard MVVM (Model-View-ViewModel) pattern with a Repository layer
  to abstract data sources.
- **Database:** [Room](https://developer.android.com/training/data-storage/room) for local,
  persistent storage in a SQLite database.
- **Asynchronous Programming:
  ** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
  and [Flow](https://kotlinlang.org/docs/flow.html) for managing background threads and handling
  streams of data.
- **Dependency Injection:** [Hilt](https://dagger.dev/hilt/) for managing dependencies and
  decoupling components.
- **Navigation:** [Compose Navigation](https://developer.android.com/jetpack/compose/navigation) for
  handling screen transitions within the app.

## Getting Started

To get the project up and running on your local machine, follow these steps:

1. **Prerequisites:** Make sure you have the latest stable version
   of [Android Studio](https://developer.android.com/studio) installed.
2. **Clone the repository:**
   ```bash
   git clone https://github.com/your-username/Timber.git
   ```
3. **Open in Android Studio:** Open the cloned project in Android Studio.
4. **Generate the Database:**
    - The app requires a pre-populated database file to function.
    - Find the `DatabaseSeedingTest.kt` file located in the `app/src/androidTest` directory.
    - Run the `seedDatabaseAndCreateFile()` test on an Android emulator or a physical device.
    - Once the test runs, use the Android Studio **Device Explorer** to locate the generated
      database file. It will be in
      `/data/data/com.android.timberworkoutlogs/databases/timber_database.db`.
    - Download this file from the device and place it in the `app/src/main/assets/database/`
      directory. You may need to create the `database` folder.
5. **Build and Run:** Build and run the `app` module on an emulator or physical device.

## Contributing

Contributions are welcome! If you have a suggestion or find a bug, please feel free to open an issue
or submit a pull request.

Please follow the existing code style and ensure that all new features are covered by tests where
applicable.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.
