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

The checked-in development value is `CAST_RECEIVER_APP_ID=REPLACE_ME`. Supply a
real ID through `android/local.properties`, a Gradle property, or the environment:

```properties
CAST_RECEIVER_APP_ID=YOUR_APP_ID
```

With the placeholder, the app remains buildable and shows `Custom receiver not
configured`; it does not accidentally load an SR stream through Google's Default
Media Receiver. See [docs/CAST_SETUP.md](docs/CAST_SETUP.md) for registration and
device-test instructions.

## Current verification boundary

- The shared catalog currently contains 36 official SR live channels, including
  all 25 regional P4 services. Android and receiver use SR's documented
  right-now API with bounded cache and metadata-independent playback.
- Build and local unit tests verify schemas, mappings, fallbacks and artifacts.
- A real registered Cast/Nest Hub is required to verify discovery, launch,
  receiver playback, Media Browse, touch, CORS behavior and session persistence.
- No real-device verification is claimed by this repository.

`EXTERNAL_BLOCKER`: Google Cast Developer Console identity/payment verification
currently prevents developer registration. This is not a code, build or
architecture failure. **REAL CAST DEVICE NOT YET VERIFIED.**
