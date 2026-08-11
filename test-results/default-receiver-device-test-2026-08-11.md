# Default Media Receiver real-device test — 2026-08-11

- Date: 2026-08-11
- Nest Hub model: Google Nest Hub (generation/model not recorded)
- Nest Hub firmware: NOT_RECORDED
- Android device: Samsung device (model not recorded)
- Android version: NOT_RECORDED
- RadioApp commit: `e9c880b`
- APK SHA-256: `89c1a368c1d80a015bb232321610cac4145acca2d87ee469c9bbf02a3deb78d5`
- Receiver mode: `DEFAULT_MEDIA_RECEIVER`

## Test 1 — Discovery and route picker

Result: **PASS**

RadioApp started without a crash, `MediaRouteButton` rendered, the button could
be tapped without a crash, Google's standard route picker opened and the Nest
Hub was available for selection.

## Test 2 — Connect

Result: **PASS**

The Nest Hub was selected and a Cast session was established with Google
Default Media Receiver.

## Test 3 — P1

Result: **PASS**

P1 played through the Nest Hub from the official SR live stream.

## Test 4 — Metadata

Result: **NOT_TESTED**

No explicit observation of title, subtitle or artwork was reported.

## Test 5 — P3 channel switch

- Result: **PASS** for channel switching and P3 playback
- Session continuity without recreation: **NOT_TESTED**

Android-driven channel switching worked and P3 played. Session reuse was not
separately instrumented.

## Test 6 — P4 Malmöhus

Result: **PASS**

P4 Malmöhus played through the Nest Hub.

## Additional channel coverage — P2

Result: **NOT_TESTED**

## Test 7 — Pause/play

Result: **NOT_TESTED**

## Test 8 — Stop

Result: **NOT_TESTED**

## Test 9 — Android background

Result: **NOT_TESTED**

## Test 10 — Android process/app closed

Result: **NOT_TESTED**

## Test 11 — Reopen Android

Result: **NOT_TESTED**

## Test 12 — Session recovery

Result: **NOT_TESTED**

## Diagnostics

- Receiver/Hub errors: NOT_RECORDED
- Android logcat errors: NOT_RECORDED

## Overall

**PASS for the tested scope.** The verified path is:

```text
Android Sender → Google Cast → Google Default Media Receiver → Nest Hub → SR live AAC
```

This run does not verify RadioApp's Custom Web Receiver, its GitHub Pages build
inside Cast runtime, Media Browse, Hub-touch or Hub-only navigation,
receiver-side metadata refresh, or Custom Receiver session persistence.
