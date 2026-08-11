# Sveriges Radio metadata

Metadata is optional enrichment. Stream loading never waits for the metadata API and playback continues through API errors, timeouts, malformed responses and missing fields.

## Official endpoint

RadioApp uses the documented SR Open API v2 endpoint:

```text
GET https://api.sr.se/api/v2/scheduledepisodes/rightnow
    ?channelid=<numeric SR channel ID>
    &format=json
```

Documentation: [SR API – Tablå, Just nu-information](https://api.sr.se/api/documentation/v2/metoder/tabla.html). The response directly supplies `currentscheduledepisode` and `nextscheduledepisode`; RadioApp does not infer the next program from an independently sorted schedule.

The adapters normalize SR JSON into `NowPlayingMetadata`: internal channel ID, program ID/name/description, program image, start/end instants, optional next program and update time. Microsoft JSON dates such as `/Date(1786478580000)/` are supported, as are ISO-8601 instants. UI and Cast mapping never consume SR's raw shape.

Image priority is current program `socialimage`/`imageurl`, then the channel image stored in the catalog, then RadioApp's local SVG/vector fallback. Invalid or non-HTTPS API image URLs are ignored.

## Cache and refresh

Android and receiver use an in-memory per-channel cache. A successful entry refreshes 15 seconds before the known end time, bounded to 30 seconds minimum and 5 minutes maximum. If end time is missing, the refresh interval is 2 minutes. Android refreshes only the selected channel; the receiver refreshes only the loaded channel and can continue after the sender disconnects.

On a temporary API failure, cached metadata may be reused only until the earlier of program end plus 2 minutes or fetch time plus 30 minutes. This avoids presenting an old program indefinitely. Without a still-relevant cache, the UI falls back to channel name, `Sveriges Radio` and `LIVE`.

## CORS

On 2026-08-11, requests carrying `Origin: https://viktorjonsson77.github.io` to both `channels` and `scheduledepisodes/rightnow` returned HTTP 200 and `Access-Control-Allow-Origin: *`. The right-now response advertised `Cache-Control: public,max-age=10`. Browser/receiver access therefore needs no backend or proxy. Physical Cast runtime behavior is still **REAL CAST DEVICE NOT YET VERIFIED**.

## Deterministic tests and live smoke test

Fixtures cover current/next parsing, missing image/description, malformed shape, network failure, timeout, fresh cache and stale fallback. Unit tests never call SR. The optional `node tools/sr-live-smoke-test.mjs` separately checks every stream, P3's current response shape and CORS.
