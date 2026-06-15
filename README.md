# Σύνταγμα της Ελλάδας — Greek Constitution

An Android app for reading the Constitution of Greece, built with Jetpack Compose and Material 3.

## Features

- 📖 Full text of the Greek Constitution, organized for easy navigation
- 🎨 Theme selector — System / Light / Dark, plus Material You dynamic color
- 🔠 Adjustable reading font size with live preview
- 🌐 Bilingual interface — Greek / English (constitution body text stays Greek)
- ⚙️ Settings persisted with DataStore

## Tech stack

| | |
|---|---|
| Language | Kotlin 2.2.10 |
| UI | Jetpack Compose (BOM 2024.10.01), Material 3 |
| Persistence | Room 2.8.4, DataStore 1.1.1 |
| Build | AGP 9.2.1, KSP |
| SDK | `minSdk` 26 · `targetSdk` 36 · `compileSdk` 36 |

## Build & run

Requires JDK 17+ and the Android SDK (API 36).

```bash
# Clone
git clone https://github.com/KostasC22/GreekConstitution.git
cd GreekConstitution

# Build a debug APK
./gradlew :app:assembleDebug

# Install on a connected device/emulator
./gradlew :app:installDebug
```

Create a `local.properties` with your SDK path (Android Studio does this automatically):

```properties
sdk.dir=/path/to/Android/Sdk
```

## Tests

```bash
./gradlew :app:testDebugUnitTest            # unit tests
./gradlew :app:connectedDebugAndroidTest    # instrumented / Compose UI tests
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Released under the [MIT License](LICENSE).
