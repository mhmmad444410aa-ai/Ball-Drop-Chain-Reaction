# Ball Drop: Chain Reaction

An original offline Android physics-puzzle game prototype.

**Android application ID:** `com.mhmmad.balldropchainreaction`  
**Version:** 1.0.0 (1)  
**Languages:** English (UK), Arabic (RTL), Portuguese (Brazil)  
**Source status:** 20 level definitions and game/art source exist in a separate 4 MB project archive. Build and on-device gameplay are **not yet verified**. Do not publish as a finished app.

## Import the complete source archive

This repository was created empty. The source archive prepared for this game is named **`BallDropChainReaction-source.zip`** (approximately 4 MB). It contains the entire Android project, including Kotlin, Gradle config, sprites, backgrounds, short SFX, tests and the standard Android workflow.

1. Download `BallDropChainReaction-source.zip` from the ChatGPT conversation that created this game.
2. On this repository, select **Add file → Upload files** and upload the ZIP **unchanged** to the root of the default `main` branch. Use the exact filename `BallDropChainReaction-source.zip`.
3. Commit the upload. The temporary `import-source.yml` workflow extracts the files, commits the source code into this repository, and attempts a **debug** build/test automatically.
4. Open **Actions** and inspect the **Import game source and test Android build** run. If it fails, use the actual compiler error log; do not rerun without fixing the cause.
5. After import, the standard `android.yml` CI workflow is available for future builds.

The import workflow intentionally requires the ZIP rather than pretending the full game sources are already in this repository. It uses only the repository's own GitHub Actions token. The temporary importer removes itself and the uploaded archive from the new working tree after importing; the source is checked in as ordinary files.

## Build requirements

JDK 17, Gradle 8.13, Android SDK 36. The `gradlew` inside the source archive is a bootstrap script (the official wrapper JAR wasn't generated), so CI runs Gradle through `gradle/actions/setup-gradle`.

**Signing:** debug APK is for testing only. A store-ready APK requires the legitimate release keystore and correctly configured GitHub Actions secrets. Never commit signing keys, credentials or ad network secrets to this public repository.

**Outstanding:** verify all 20 solutions, frame rate, devices, privacy policy, Huawei ad integration and legal compliance. Do not claim an APK exists until a GitHub build actually succeeds.
