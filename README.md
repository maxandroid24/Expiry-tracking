# Expiry Tracker

Expiry Tracker is a modern Android application written in Kotlin with traditional Android Views and XML layouts. It helps users track product expiry dates for food, medicines, cosmetics, groceries, and other consumables.

## Setup

1. Open this folder in Android Studio.
2. Let Android Studio sync Gradle dependencies.
3. Optional AI extraction: add a Gemini API key to `~/.gradle/gradle.properties`:

   ```properties
   GEMINI_API_KEY=your_key_here
   ```

4. Run the `app` configuration on an emulator or device.

## Architecture

The app follows Clean Architecture with MVVM:

- `presentation`: Activities, fragments, adapters, ViewModels, UI state.
- `domain`: Entities, repository interfaces, use cases, business rules.
- `data`: SQLite storage, OCR/AI parsing implementations, repositories, backup, image storage.
- `di`: Hilt dependency injection modules.
- `worker`: WorkManager expiry reminder scheduling.

Dependencies point inward: presentation depends on domain contracts, data implements those contracts, and Hilt wires implementations at runtime.

## Libraries

- Kotlin, Coroutines, StateFlow
- XML Views, ViewBinding, RecyclerView, Navigation Component
- Material Design 3 components and Material date pickers
- Hilt dependency injection
- SQLite via `SQLiteOpenHelper`
- WorkManager notifications
- ML Kit Text Recognition for offline OCR
- Gemini REST integration for online extraction
- Coil for image loading

## Folder Structure

```text
app/src/main/java/com/expirytracker/app
├── data
├── di
├── domain
├── presentation
├── util
└── worker
```

## Build

```bash
./gradlew assembleDebug
```

This environment does not include Java or Gradle, so compilation should be performed from Android Studio or a machine with the Android toolchain installed.
