#!/usr/bin/env bash
set -euo pipefail

GRADLE_VERSION="8.13"
BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
DIST_DIR="${BASE_DIR}/.gradle-dist"
ZIP_NAME="gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_HOME="${DIST_DIR}/gradle-${GRADLE_VERSION}"
ZIP_PATH="${DIST_DIR}/${ZIP_NAME}"

mkdir -p "${DIST_DIR}"

if [ ! -d "${GRADLE_HOME}" ]; then
  if [ ! -f "${ZIP_PATH}" ]; then
    echo "Descargando Gradle ${GRADLE_VERSION}..."
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL "https://services.gradle.org/distributions/${ZIP_NAME}" -o "${ZIP_PATH}"
    elif command -v wget >/dev/null 2>&1; then
      wget -q "https://services.gradle.org/distributions/${ZIP_NAME}" -O "${ZIP_PATH}"
    else
      echo "Necesitas curl o wget para descargar Gradle."
      exit 1
    fi
  fi
  echo "Descomprimiendo Gradle ${GRADLE_VERSION}..."
  unzip -q -o "${ZIP_PATH}" -d "${DIST_DIR}"
fi

exec "${GRADLE_HOME}/bin/gradle" "$@"
