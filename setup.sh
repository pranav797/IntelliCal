#!/usr/bin/env bash
set -euo pipefail

ANDROID_SDK_PATH="${ANDROID_HOME:-}"

if [[ -z "$ANDROID_SDK_PATH" && -d "$HOME/Library/Android/sdk" ]]; then
    ANDROID_SDK_PATH="$HOME/Library/Android/sdk"
elif [[ -z "$ANDROID_SDK_PATH" && -d "$HOME/Android/Sdk" ]]; then
    ANDROID_SDK_PATH="$HOME/Android/Sdk"
fi

if [[ -z "$ANDROID_SDK_PATH" ]]; then
    echo "Android SDK not found. Install Android Studio or set ANDROID_HOME."
    exit 1
fi

if [[ -f local.properties ]]; then
    echo "local.properties already exists; leaving it unchanged."
else
    cp local.properties.example local.properties
    sed -i.bak "s|YOUR_ANDROID_SDK_PATH|$ANDROID_SDK_PATH|" local.properties
    rm -f local.properties.bak
    echo "Created local.properties from local.properties.example."
    echo "Add your own Supabase URL, Supabase anon key, and Gemini API key before running the app."
fi

echo "Setup complete. Build with: ./gradlew assembleDebug"
