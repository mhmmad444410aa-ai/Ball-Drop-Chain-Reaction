# Ball Drop: Chain Reaction

Original offline Android 2D physics-puzzle project, Kotlin + Jetpack Compose/Material3 + pure-Java JBox2D, with a five-world/20-level *development* catalog.

**Status: source-complete prototype, unbuilt and untested on an Android device. Do not upload to stores yet.** A structural validation script and independent Kotlin compilation of the catalog passed. Physics outcomes, stage solvability, UI functionality, performance, accessibility and release signing must still be verified using the Actions build and devices.

## Project identity

- App ID: `com.mhmmad.balldropchainreaction`
- Version: `1.0.0` / `1`
- Android SDK: min 23, target 36, compile 36
- JDK 17, Gradle 8.13, Android Gradle Plugin 8.13.2, Kotlin 2.3.0
- Compose BOM 2026.04.01 (Compose 1.11). Latest Compose BOM requires compileSdk 37; intentionally pinned compatible family.
- DataStore persists progress, stars, language and sound preference
- Local gameplay only; **no** `INTERNET` permission, Firebase, Google services, ads SDK, in-game music or third-party brand imagery.

## Implemented source modules

1. `LevelCatalog.kt`: twenty hand-authored scenarios in five worlds with original name, geometry, tools, and hint positions. These are **not certified solvable** until play-testing.
2. `PhysicsScene.kt`: actual JBox2D simulated ball/rigid bodies, dynamic dominoes, hinges for seesaws, friction, gravity, restitution, spring impulses, goal and loss logic, fixed 1/60s step.
3. `PhysicsGame.kt`: stage rendering from independent game sprites, drag/tap to place, ghost preview, quarter-unit snap, rotation, run, pause, reset, undo, hints and zoom controls.
4. `MainActivity.kt`: main menu, world and level selection, victory/failure, language and SFX settings, About page; decorative menu and victory motion.
5. `SaveStore.kt`: local progress, language and stars through Preferences DataStore.
6. `ArtStore.kt`: lazy-loaded separate sprites/backgrounds.
7. `GameAudio.kt`: original short mono effect samples, **no music**.

## Assets and previews

- `app/src/main/assets/art`: 26 independent RGBA sprites plus six UI backgrounds.
- `app/src/main/assets/sfx`: original tap/bounce/domino/win WAV effects.
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`: 512×512 32-bit RGBA, 239 KB.
- `previews`: concept mockups (`main_menu_concept.jpg`, `gameplay_concept.jpg`), **not game screenshots**; plus a `1024x500` feature illustration. Never submit concept mockups as gameplay screenshots.
- `tools/prepare_art.py`, `tools/make_previews.py`: original local design extraction scripts. Regeneration assumes previously generated source images in `/mnt/data`. Preprocessed assets are included, so regeneration is not needed for a build.

## How to build

**Option A – GitHub Actions (recommended from mobile)**

1. Create a new GitHub repository specifically for this game. Do not upload the zip as one file; upload its *contents* with `.github/workflows/android.yml` and all folders intact.
2. Go to **Actions → Android validation and unsigned build → Run workflow** or push to `main`.
3. The CI installs Android API 36, compiles and runs JVM tests, and uploads a `ball-drop-debug-apk` artifact after a **successful** build. It also attempts an unsigned release build.
4. Download the artifact ZIP, extract `app-debug.apk`, and install for testing. Debug is NOT a publishable release.

**Option B – Android Studio / computer**

Install JDK 17 and Android SDK 36. Run `./gradlew :app:assembleDebug` (or invoke `gradle` directly). `gradlew` is a portable Gradle 8.13 bootstrap launcher and **not** the official Gradle wrapper jar. Once Gradle runs, invoke `gradle wrapper --gradle-version 8.13` to generate and commit the official wrapper JAR/scripts, replacing the bootstrap launcher. This limitation is due to the original artifact environment lacking Gradle and an Android SDK.

## Signing a legitimate release in GitHub Actions

The workflow only signs when ALL four GitHub Actions repository secrets are provided:

- `RELEASE_KEYSTORE_BASE64`: base64-encoded **existing** release keystore, without uploading the keystore as a repository file
- `RELEASE_STORE_PASSWORD`: keystore password
- `RELEASE_KEY_ALIAS`: key alias
- `RELEASE_KEY_PASSWORD`: key password

After verification, a real signed artifact is labelled `ball-drop-signed-release-apk`. CI invokes `apksigner verify` and prints the signing certificate SHA-256 fingerprint. Otherwise, release output is explicitly **unsigned**.

For AppGallery updates, retain the original keystore for all future versions. Don't invent fingerprints or change package name/signing keys.

## Outstanding blockers before store submission

- **Build has not run:** no Android SDK/Gradle on the creation host, and no dedicated writable GitHub repository for this game was found.
- **All 20 levels are authored, but not physically completed and validated.** Fix unreachable layouts based on actual simulation traces; do not treat hints as proof of solvability.
- Ads are deliberately **not integrated**: no per-app Huawei Ad Unit IDs or valid consent configuration. There is no startup opt-in/opt-out prompt. Integrate the official SDK and legally required consent before monetization.
- The real privacy policy URL is missing. The About screen currently states that publication configuration is pending.
- Accessibility, responsive landscape screens, EMUI compatibility, benchmark 60 FPS, multi-device testing, store acceptance, legal review and on-device screenshots remain unverified.
- The optional promo video must use real captured gameplay, not a generated animation pretending to be gameplay.

## Build troubleshooting

When a build fails, inspect the first actual `e:`/Gradle error in the Actions step; fix that cause and rebuild. Do not repeatedly rerun unchanged failing code. An install failure `Library is not found` must be diagnosed from the concrete exception/ABI/package validation, not guessed from a screenshot.

## Licence and compliance

The commissioned UI art and sound effects were generated for this project. JBox2D 2.2.1.1 is under the Simplified BSD / BSD-2-Clause licence; AndroidX/Compose and Gradle are subject to their respective official licences. Review the full dependency manifests and privacy policy before publishing.

## Art asset import, 22 Nov 2026
Uses separately painted WebP gameplay assets and illustrated backgrounds from the approved visual concepts, not simplistic flat primitives. Image assets retain alpha and the physics bodies are rendered through JBox2D. The gameplay and all levels remain unverified on a physical device until tested.
