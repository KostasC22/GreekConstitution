# Contributing

Thanks for your interest in improving the Greek Constitution app. Contributions of all kinds are welcome — bug reports, fixes, features, and docs.

## Getting started

1. Fork the repo and clone your fork.
2. Make sure you can build: `./gradlew :app:assembleDebug`.
3. Create a branch off `main`: `git checkout -b feature/short-description`.

## Development

- **Language style:** Kotlin official code style (`kotlin.code.style=official`).
- **UI:** Jetpack Compose + Material 3. Reuse existing composables and theme tokens.
- **Strings:** add user-facing text to `ui/strings/AppStrings.kt` — do not hardcode strings in screens. Constitution body text stays Greek.
- **Persistence:** settings go through DataStore; data through Room.

## Before opening a PR

Run the checks locally and make sure they pass:

```bash
./gradlew :app:compileDebugKotlin
./gradlew :app:testDebugUnitTest
./gradlew :app:connectedDebugAndroidTest   # if your change touches UI/behavior
```

## Pull requests

- Branch naming: `feature/...`, `fix/...`, or `chore/...`.
- Keep PRs focused; one logical change per PR.
- Describe **what** changed and **why**. Link any related issue.
- CI must be green before merge.

## Reporting bugs

Open an issue with: device + Android version, app version, steps to reproduce, and what you expected vs. what happened. Logs or screenshots help.

## License

By contributing, you agree that your contributions are licensed under the project's [MIT License](LICENSE).
