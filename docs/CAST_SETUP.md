# Cast setup

This procedure follows Google's current [Cast registration guide](https://developers.google.com/cast/docs/registration),
[Android Sender setup](https://developers.google.com/cast/docs/android_sender), and
[Chrome Remote Debugger guide](https://developers.google.com/cast/docs/debugging/remote_debugger).
Console wording can change; do not substitute old Assistant Actions instructions.

## 1. Host the receiver

Build `receiver/dist/` and deploy the entire directory to a static URL reachable
by the Nest Hub:

```bash
cd receiver
npm ci
npm run build
```

A publicly published Web Receiver must use HTTPS. Google's registration guide
currently allows HTTP during development if the device can reach it, but not
`localhost`; using HTTPS from the first device test avoids mixed deployment
conditions. This repository now prepares GitHub Pages at
`https://viktorjonsson77.github.io/radioapp/`; activation and live verification
remain manual. Follow `CAST_REGISTRATION_CHECKLIST.md`.

## 2. Register the Custom Web Receiver

1. Sign in to the [Google Cast SDK Developer Console](https://cast.google.com/)
   with the intended owner/team account.
2. From Overview or Applications, choose **Add New Application**.
3. Choose **Custom Receiver**.
4. Enter the RadioApp name and the hosted receiver `index.html` URL.
5. Enable **Supports casting to audio-only devices** if Nest Audio/Mini and
   other audio-only targets are in scope. Decide relay casting deliberately.
6. Save and copy the displayed Application ID.

Publishing is not required for development on a registered test device. A
Custom Receiver does require registration; Google's Default Media Receiver
cannot provide this custom UI or Media Browse implementation.

## 3. Register the Nest Hub for unpublished testing

Google requires a test Cast device to be registered before it can launch an
unpublished Web Receiver. Complete the Nest Hub's normal Google Home setup and
account linking first. Then use the Developer Console **Devices** registration
flow and the device identifier/serial number shown by Google's current device
registration instructions. Keep the application and device under the same
developer account. Allow propagation time after registration and reboot the
device if the console directs it.

This document intentionally does not invent a Nest Hub settings-menu path:
Google's current registration page is authoritative for obtaining the identifier
and console fields.

## 4. Configure and build Android

Put the Application ID in the untracked `android/local.properties`:

```properties
sdk.dir=/opt/android-sdk
CAST_RECEIVER_MODE=CUSTOM
CAST_RECEIVER_APP_ID=YOUR_APPLICATION_ID
```

Alternatively pass `-PCAST_RECEIVER_APP_ID=...` or set an environment variable.
The value is compiled once into `BuildConfig` and consumed only by
`CastOptionsProvider`; it is not duplicated.

`CUSTOM` is the default mode if `CAST_RECEIVER_MODE` is omitted. During the
external registration blocker only, follow `DEFAULT_RECEIVER_TEST.md` to use
Google Default Media Receiver without an Application ID. That temporary mode is
not a replacement for any step in Custom Receiver registration or verification.

```bash
cd android
./gradlew clean test assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Both current build types use the requested package `se.radioapp.app`. If Sender
Details are configured in the console, use that package according to the
console's current validation rules.

## 5. First session

1. Put the Android phone and Nest Hub on the same normal Wi-Fi/LAN; disable AP
   isolation. Same-network discovery is the normal Cast UX baseline.
2. Launch RadioApp and use the CAF Cast button in the top bar.
3. Select the registered Nest Hub and wait for the receiver to launch.
4. Tap P1 in Android and verify that the Hub loads the SR URL directly.
5. On the Hub, verify Now Playing, swipe-up in-player browse, and select another
   channel. Then background/close the Android app and repeat Hub controls.

## Debugging

The app and device must be registered to the same developer account, and the
receiver must first be launched by a sender. On a computer on the same network,
open `chrome://inspect` and choose the receiver's **Inspect** link. Google's guide
also documents direct access at the device IP on port `9222`. Do not leave the
remote debugger attached indefinitely; Google warns that this can exhaust
receiver resources.

Inspect console entries prefixed `[RadioApp Receiver]`. For temporary CAF debug
logging, use Google's documented `setLoggerLevel(DEBUG)` command in DevTools;
do not leave verbose logging enabled for production.
