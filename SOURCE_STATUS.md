# Project status

This commit adds a **playable Android source baseline**, built in the repository with Kotlin and JBox2D. It is an intentionally reduced procedural-graphics build because the separate 4 MB premium-art archive could not be transferred into GitHub through the available repository connector. The original ZIP remains available in the conversation.

Features in source: physics balls, rotating seesaw hinge, spring impulses, domino rigid bodies, collision, gravity, 20 distinct parameterised draft stage layouts in five difficulty bands, tool placement/rotation, undo, hint marker, reset/pause/run, English/Arabic/Portuguese selection, local level progress.

Known limitations: This is not the previously prepared Compose/image-asset version; the 20 levels are **not individually verified solvable**, and no APK is verified until GitHub Actions completes. No real signed release, advertisement IDs, runtime device results, or store-ready screenshots are included. No music or Internet permission.

Build via Actions > Android build and physics prototype. Art and retail-quality UI still require importing the original ZIP.
