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

## Temporary Default Receiver physical test

Use `docs/DEFAULT_RECEIVER_TEST.md` and record results in
`test-results/DEFAULT_RECEIVER_DEVICE_TEST_TEMPLATE.md`. This may verify only
Android Sender → Google Cast → SR stream. It cannot verify any Custom Receiver,
Media Browse, Hub-touch browser, Hub-only navigation or GitHub Pages behavior.

## Optional live smoke verification

```bash
node tools/sr-live-smoke-test.mjs
```

This checks all stream headers with bounded requests, then P3 right-now response shape and browser-origin CORS. It is deliberately excluded from ordinary builds because live network state must not make deterministic tests fail.

## REAL_DEVICE_REQUIRED

Record device, firmware, commit and APK hash in `test-results/`. Verify discovery, custom receiver launch, direct AAC playback, program/artwork updates, landing and in-player Browse, Hub touch LOAD, reconnect, sender disconnect, stop/restart/idle and API/stream failure behavior.

Automated and browser tests must be reported as **BUILD VERIFIED** / **LOCAL TEST VERIFIED** only. Until registration succeeds: **REAL CAST DEVICE NOT YET VERIFIED**.
