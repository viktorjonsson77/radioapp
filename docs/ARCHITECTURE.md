# Architecture

## Runtime data flow

```text
official SR AAC stream ───────────────> Cast device / Nest Hub
                                                ^
                                                | CAF media LOAD
Android sender ───────────────> Custom Web Receiver
     |                                  |
     +── SR rightnow metadata API ──────+
```

The media URL in every LOAD is the official SR HTTPS URL. The Cast device fetches audio directly; Android is never an audio proxy. Metadata calls are independent and never gate LOAD, channel switching or controls.

The checked-in/default receiver mode is `CUSTOM`. A temporary, explicit
`DEFAULT_MEDIA_RECEIVER` test branch changes only the Application ID selected by
Android's `CastOptionsProvider`:

```text
CUSTOM (product architecture) ──> RadioApp Custom Web Receiver
DEFAULT (temporary device PoC) ─> Google Default Media Receiver
```

Both branches send the same standard live `MediaInfo` and official stream URL.
The temporary branch does not execute or validate any receiver code in
`receiver/`.

## Shared content layer

- `shared/channels.json`: schema-v2 catalog with stable internal IDs, numeric SR IDs, structured P4 region data, official streams and channel images.
- `shared/channels.schema.json`: machine-readable JSON Schema.
- Android packages `shared/` as assets; receiver build syncs the catalog into `public/generated/`.
- `tools/generate-channel-catalog.mjs`, `tools/validate-streams.mjs` and `tools/sr-live-smoke-test.mjs` keep official-source checks repeatable.

## Android

- `data/channel`: strict asset parsing, uniqueness and regional consistency.
- `data/metadata`: SR HTTP adapter, raw-shape parser and bounded cache decorator.
- `domain`: channel, region, normalized now-playing/next-program models and repository/provider contracts.
- `ui`: favorites, national, expandable local P4 and other sections; selected-channel metadata refresh only.
- `cast`: direct live MediaInfo with stable `radioapp://channel/<id>` entity and metadata fallback.
- `cast/CastReceiverConfiguration`: central mode selection; CUSTOM is default,
  while DEFAULT resolves through Google's official SDK constant.

## Receiver

- `channel.ts`: runtime catalog validation.
- `metadata.ts`: SR right-now parsing/cache plus normalized CAF media mapping.
- `browse/`: framework-neutral deterministic selection and CAF object adapter.
- `main.ts`: LOAD interception, receiver-side metadata refresh, status broadcast, Media Browse, capability detection and browser development mode.

Receiver-side refresh matters because the Hub should remain useful after Android disconnects. It uses SR directly over CORS and no backend. The timer exists only for the loaded channel and is cleared on IDLE.

## Failure boundaries

- Catalog failure: sender reports a load error; receiver shows an explicit overlay.
- Metadata failure: playback continues; UI uses channel → `Sveriges Radio` → `LIVE` and local artwork.
- Placeholder Cast ID: sender refuses custom LOAD with a configuration message.
- Explicit Default Receiver mode: playback is allowed without a Custom App ID,
  but UI marks it as a temporary non-custom test.
- Stream/session failure: normal CAF error surfaces remain authoritative.
- Missing or retired favorite: filtered from presentation without crashing or creating a phantom row.

No Firebase, cloud backend, proxy, account system, podcast/on-demand layer or Assistant Actions are part of this architecture.
