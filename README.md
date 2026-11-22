# Ball Drop: Chain Reaction

**Status: Android buildable source prototype, NOT store-ready.**

This repository has a new native Android Kotlin source baseline for an offline chain-reaction physics puzzle. It provides JBox2D gravity/collision, rigid dominoes, spring impulses, a hinged seesaw, target detection, tool placement, Run/Pause/Reset/Undo, local progress, a 20-stage draft catalog, and English/Arabic/Brazilian Portuguese menus.

The baseline renders the blue workshop, wood, bolts, glossy balls and controls *procedurally*. It is not the previously created premium image-assets ZIP, and is not a Jetpack Compose implementation. All 20 level solutions and on-device performance still need evaluation. There is no music or Internet permission.

## Building a test APK
Open [Android build workflow](../../actions/workflows/android.yml). Each commit to app/ runs an automated Gradle debug build and publishes the `BallDropChainReaction-debug-apk` artifact **only if compilation succeeds**. Debug builds are for tests, not store release.

Package: `com.mhmmad.balldropchainreaction`
Target API 36; minimum API 23; JDK 17; Android Gradle Plugin 8.13.2; Gradle 8.13; JBox2D 2.2.1.1.

## Remaining acceptance tasks
- Import previously designed premium sprites, backgrounds, icon and animations from the separate project archive
- Test and revise actual physics level solutions on Android devices
- Add Huawei Ads with valid game-specific unit IDs and required privacy consent, without general advertising opt-out prompts
- Provide an actual privacy-policy URL and localized information
- Obtain existing release signing credentials privately via GitHub Actions secrets, never commit keystore
- Verify a signed release APK and extract SHA-256 from its certificate
- Produce real screenshots and a music-free gameplay trailer only after actual device run

See [SOURCE_STATUS.md](SOURCE_STATUS.md). No signed production APK has been generated or validated yet.
