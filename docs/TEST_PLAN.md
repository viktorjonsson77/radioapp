# Test plan

## Automated local tests

Android `./gradlew test`:

- canonical channel asset parsing and repository lookup;
- official HTTPS host and channel ordering through repository assertions;
- favorite add/remove set logic;
- live AAC `MediaInfo` mapping with SR title/artist metadata.

Android `./gradlew assembleDebug` verifies manifest merge, CAF dependency,
Compose compilation, resources, shared asset packaging and debug APK creation.

Receiver `npm test`:

- strict channel catalog parsing and URL/schema rejection;
- content type and live metadata mapping;
- entity round trip;
- landing favorites and in-player channel browse structure.

Receiver `npm run build` performs strict TypeScript checking and produces the
static Vite artifact. `npm run dev` is for ordinary browser UI/data work only.

## REAL_DEVICE_REQUIRED

Record device model, firmware, receiver build commit and Android build variant in
`test-results/` for each run. Verify:

1. standard Cast discovery and picker UX;
2. Custom Receiver launch and ready log;
3. direct P1/P2/P3/P4 AAC playback, startup time and stability;
4. Android play, pause, stop, reconnect and session-loss UI;
5. landing favorites before playback;
6. swipe-up in-player channels during playback;
7. touch channel change and LOAD interceptor logs;
8. phone background, process kill and sender disconnect behavior;
9. metadata/artwork and error overlay;
10. audio-only Cast target behavior separately, if available.

Automated tests must never be reported as physical Cast verification.
