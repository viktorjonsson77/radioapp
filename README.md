# RadioApp

RadioApp is a proof-of-concept Android sender and Custom Web Receiver for a
touch-friendly Sveriges Radio experience on Google Cast smart displays. The
Cast device fetches the official SR HTTPS audio stream directly; the phone is
never an audio proxy.

## Repository

```text
android/       Kotlin, Jetpack Compose and Google Cast Android Sender
receiver/      Vanilla TypeScript CAF Custom Web Receiver and Media Browse PoC
shared/        Canonical machine-readable channel catalog
docs/          Architecture, setup, PoC findings and test plan
tools/         Local validation utilities
test-results/  Reserved for generated/manual test evidence
```

## Quick start

Android (JDK 17+ and Android SDK 36):

```bash
cd android
./gradlew test
./gradlew assembleDebug
```

Receiver (Node.js 20+):

```bash
cd receiver
npm ci
npm run dev
npm test
npm run build
```

Browser mode is only a visual/data development aid. It does not emulate CAF,
Cast sessions, Nest Hub touch, Media Browse, or physical stream playback.

## Cast configuration

The default receiver mode is `CUSTOM`. Supply the real RadioApp receiver ID
through ignored `android/local.properties`, a Gradle property, or the environment:

```properties
CAST_RECEIVER_MODE=CUSTOM
CAST_RECEIVER_APP_ID=YOUR_APP_ID
```

With a missing/placeholder custom ID, the app remains buildable but blocks media
LOAD with `Custom receiver not configured`.

While Google developer registration is externally blocked, an explicit
temporary physical-test mode is available:

```properties
CAST_RECEIVER_MODE=DEFAULT
```

This uses Google's SDK constant for the Default Media Receiver and needs no
Application ID. It tests only sender-to-stream fundamentals, never RadioApp's
Custom Receiver. See [docs/DEFAULT_RECEIVER_TEST.md](docs/DEFAULT_RECEIVER_TEST.md)
and the current [project status](docs/PROJECT_STATUS.md).

## Current verification boundary

- The shared catalog currently contains 36 official SR live channels, including
  all 25 regional P4 services. Android and receiver use SR's documented
  right-now API with bounded cache and metadata-independent playback.
- Build and local unit tests verify schemas, mappings, fallbacks and artifacts.
- Google Default Media Receiver passed a real Samsung/Nest Hub test on
  2026-08-11 for discovery, connection, P1, channel switching, P3 and P4
  Malmöhus playback.
- A registered Custom Receiver is still required to verify RadioApp's receiver,
  Media Browse, Hub touch, receiver metadata and Custom Receiver persistence.

`EXTERNAL_BLOCKER`: Google Cast Developer Console identity/payment verification
currently prevents developer registration. This is not a code, build or
architecture failure. **CUSTOM RECEIVER REAL DEVICE NOT YET VERIFIED.**
