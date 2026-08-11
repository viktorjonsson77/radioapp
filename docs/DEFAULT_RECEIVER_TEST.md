# Temporary Default Media Receiver device test

> **TEMPORARY TEST MODE:** Google Default Media Receiver is used only while
> Google Cast Developer registration is externally blocked. RadioApp's accepted
> product architecture remains the Custom Web Receiver.

This mode verifies only the basic chain:

```text
Android Sender → Google Cast Default Media Receiver → official SR AAC stream
```

The Cast device receives the official SR stream URL and fetches audio directly.
Android never proxies audio.

## Configuration

Create the ignored file `/opt/radioapp/android/local.properties` with exactly:

```properties
sdk.dir=/opt/android-sdk
CAST_RECEIVER_MODE=DEFAULT
```

No Cast Developer Application ID is needed in this mode. `DEFAULT` maps to the
official SDK constant
[`CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID`](https://developers.google.com/android/reference/com/google/android/gms/cast/CastMediaControlIntent#DEFAULT_MEDIA_RECEIVER_APPLICATION_ID);
its literal value is not copied into project configuration. Android still
supplies the selected receiver ID through the documented
[`CastOptionsProvider`](https://developers.google.com/cast/docs/android_sender/integrate#initialize_the_cast_context).

Build and install:

```bash
cd /opt/radioapp/android
./gradlew test lintDebug assembleDebug
adb install -r /opt/radioapp/android/app/build/outputs/apk/debug/app-debug.apk
```

The Android UI must show:

```text
Receiver: Google Default Media Receiver
TEMPORARY TEST MODE · Inte RadioApps Custom Receiver
```

Record the physical run in
`test-results/DEFAULT_RECEIVER_DEVICE_TEST_TEMPLATE.md`.

## What this mode can test

- standard Cast discovery and connection;
- P1/P2/P3/P4 direct AAC playback;
- standard MediaInfo channel/program metadata and artwork supported by Google;
- play, pause, stop and Android-driven channel changes;
- sender background/close behavior and CAF session recovery.

## What this mode cannot test

- RadioApp's Custom Web Receiver or its lifecycle;
- the GitHub Pages receiver;
- Cast Media Browse;
- Hub touch channel browser or Hub-only navigation;
- custom receiver UI and touch behavior;
- receiver-side SR metadata refresh.

The test result must never be reported as verification of the real Custom
Receiver. **REAL CUSTOM CAST RECEIVER: NOT YET VERIFIED.**

## Android diagnostics

Capture relevant entries without credentials:

```bash
adb logcat | grep -E 'RadioAppCast|Cast|RemoteMediaClient'
```

`RadioAppCast` records session start/resume/suspend/disconnect, channel LOAD,
media status and receiver-reported playback errors.

## Return to Custom Receiver

Custom remains the default when `CAST_RECEIVER_MODE` is omitted. Once a real
Application ID is available, use:

```properties
sdk.dir=/opt/android-sdk
CAST_RECEIVER_MODE=CUSTOM
CAST_RECEIVER_APP_ID=<real RadioApp Custom Receiver Application ID>
```
