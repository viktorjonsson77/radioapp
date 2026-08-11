#!/usr/bin/env bash
set -euo pipefail

project_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
android_root="$project_root/android"
generated_config="$android_root/app/build/generated/source/buildConfig/debug/se/radioapp/app/BuildConfig.java"
source_apk="$android_root/app/build/outputs/apk/debug/app-debug.apk"
output_dir="$android_root/app/build/outputs/apk/default-receiver"
output_apk="$output_dir/radioapp-default-receiver-debug.apk"

"$android_root/gradlew" -p "$android_root" clean :app:assembleDebug -PCAST_RECEIVER_MODE=DEFAULT

grep -Fq 'CAST_RECEIVER_MODE = "DEFAULT"' "$generated_config"
grep -Fq 'CAST_RECEIVER_APP_ID = "REPLACE_ME"' "$generated_config"

mkdir -p "$output_dir"
cp "$source_apk" "$output_apk"

echo "DEFAULT_MEDIA_RECEIVER APK: $output_apk"
sha256sum "$output_apk"
