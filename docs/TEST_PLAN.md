# Test plan

## Deterministic local verification

Shared/schema:

```bash
cd receiver
npm ci
npm run validate:channels
```

The JSON Schema and semantic checks cover schema version, required fields, enum categories, 36 unique internal IDs, unique SR IDs, unique HTTPS streams, non-empty names and structured metadata for all 25 P4 regions.

Receiver `npm test` covers catalog parsing/rejection, category/P4 counts, current/next SR parsing, optional fields, malformed responses, network failure, timeout, cache/stale fallback, media/fallback images and the deterministic under-30 browse policy. `npm run build` performs strict TypeScript and Vite production build with base `/radioapp/`.

Android `./gradlew test` covers asset repository parsing/lookups, SR IDs/regions, metadata parsing/current/next/optional fields, network failure, timeout, cache/stale fallback, favorites, missing old favorite presentation, default P4 and Cast live-media mapping. `./gradlew lintDebug` and `./gradlew assembleDebug` verify the application artifact.

Receiver-mode tests cover explicit CUSTOM selection, DEFAULT aliases, use of
Google's official Default Receiver constant, missing Custom App ID safety and
unknown-mode rejection. MediaInfo tests verify P1 AAC live fields, standard
metadata/artwork, optional current-program metadata and absence of custom data.

`MediaRouteButtonThemeTest` verifies that the Compose-hosted MediaRouter button
receives an opaque AppCompat `colorPrimary` and that the framework view can be
constructed with its dedicated XML theme. Startup with that fix has passed
physical Samsung verification.

`MainActivityHostTest` prevents the route-dialog host from regressing away from
`FragmentActivity`. Startup, `MediaRouteButton` rendering and opening the route
chooser passed physical Samsung verification on 2026-08-11.

## DEFAULT MEDIA RECEIVER — REAL DEVICE VERIFIED

The physical result is recorded in
`test-results/default-receiver-device-test-2026-08-11.md`; retain
`test-results/DEFAULT_RECEIVER_DEVICE_TEST_TEMPLATE.md` for later runs. The test
verified Android startup, the standard route picker, Nest Hub selection and
connection, P1, Android-driven channel switching, P3 and P4 Malmöhus over the
Android Sender → Google Cast → Google Default Media Receiver → Nest Hub → SR
live AAC path.

Metadata presentation, pause/play, stop, sender background/close behavior,
reopen and session recovery were not tested in that run. The run cannot verify
the Custom Web Receiver, GitHub Pages in Cast runtime, Media Browse, Hub-touch
or Hub-only navigation, receiver-side metadata refresh, or Custom Receiver
session persistence.

## Optional live smoke verification

```bash
node tools/sr-live-smoke-test.mjs
```

This checks all stream headers with bounded requests, then P3 right-now response shape and browser-origin CORS. It is deliberately excluded from ordinary builds because live network state must not make deterministic tests fail.

## CUSTOM RECEIVER — REAL DEVICE REQUIRED

Record device, firmware, commit and APK hash in `test-results/`. Verify discovery, custom receiver launch, direct AAC playback, program/artwork updates, landing and in-player Browse, Hub touch LOAD, reconnect, sender disconnect, stop/restart/idle and API/stream failure behavior.

Automated and browser tests must be reported as **BUILD VERIFIED** / **LOCAL
TEST VERIFIED** only. Until registration succeeds: **CUSTOM RECEIVER REAL DEVICE
NOT YET VERIFIED**.
