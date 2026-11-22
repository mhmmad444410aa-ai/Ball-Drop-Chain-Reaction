#!/usr/bin/env sh
# Portable launcher for Gradle 8.13. This bootstraps the Gradle distribution if needed.
# It is intentionally NOT the official wrapper jar (which must be generated with `gradle wrapper`).
set -eu
if command -v gradle >/dev/null 2>&1; then exec gradle "$@"; fi
BASE="${GRADLE_USER_HOME:-$HOME/.gradle}/ball-drop-bootstrap"
BIN="$BASE/gradle-8.13/bin/gradle"
if [ ! -x "$BIN" ]; then
  mkdir -p "$BASE"
  URL='https://services.gradle.org/distributions/gradle-8.13-bin.zip'
  if command -v curl >/dev/null 2>&1; then curl -fL "$URL" -o "$BASE/gradle.zip"
  elif command -v wget >/dev/null 2>&1; then wget "$URL" -O "$BASE/gradle.zip"
  else echo 'Install Gradle 8.13 or curl/wget to run this project.' >&2; exit 1; fi
  unzip -q "$BASE/gradle.zip" -d "$BASE"
  rm "$BASE/gradle.zip"
fi
exec "$BIN" "$@"
