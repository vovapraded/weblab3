#!/bin/bash

set -e

if [ $# -ne 1 ]; then
  echo "Usage: $0 <env file, e.g. env.dev>"
  exit 1
fi

ENV_FILE="$1"

if [ ! -f "$ENV_FILE" ]; then
  echo "Environment file '$ENV_FILE' not found!"
  exit 1
fi

JAVA_VERSION=$(grep "^JAVA_VERSION=" "$ENV_FILE" | cut -d '=' -f2)

if ! [[ "$JAVA_VERSION" =~ ^[0-9]+$ ]]; then
  echo "Invalid or missing JAVA_VERSION in $ENV_FILE"
  exit 1
fi

SUFFIX=$(basename "$ENV_FILE" | sed 's/\.env$//;s/^env\.//')

./gradlew clean buildWarWithEnv \
  -PenvJavaVersion="$JAVA_VERSION" \
  -PenvSuffix="$SUFFIX" \
  -PenvFilePath="$ENV_FILE"
