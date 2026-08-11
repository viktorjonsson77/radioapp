# Sveriges Radio streams

RadioApp uses only the current official [SR stream list](https://om.sverigesradio.se/lankar-till-ljudstrommar-for-alla-kanaler). The catalog was refreshed and re-audited on 2026-08-11 and contains 36 live channels: 3 national channels, 25 local P4 channels and 8 other live channels. AAC-LC 128 kbps over HTTPS is the default for every entry. The schema/model also supports SR's documented AAC 32, AAC 320 and MP3 96 alternatives for future selection without changing today's default.

`shared/channels.json` is the canonical runtime catalog. `shared/channels.schema.json` defines schema version 2. `tools/generate-channel-catalog.mjs` cross-checks the configured mapping against both the official stream page and SR's channel API before regenerating the catalog. Regeneration is an explicit maintenance operation because changes to stable internal IDs must be reviewed.

## Validation

Run from the repository root:

```bash
cd receiver
npm ci
npm run validate:channels
cd ..
node tools/validate-streams.mjs
```

The stream validator uses a bounded GET with a one-byte Range request, a 12-second timeout and concurrency three. It immediately cancels the response body after headers. It checks HTTPS, HTTP 2xx/206, an AAC-compatible content type, an SR-owned final host and an unchanged channel path after redirects. It never downloads a live stream in full.

Live result on 2026-08-11: **36/36 HTTP 200**, `Content-Type: audio/aac`. Canonical `live1.sr.se` requests redirected to `edge1.sr.se` or `edge2.sr.se` while preserving the expected channel path. This validates HTTP reachability for the full catalog. P1, P3 and P4 Malmöhus AAC playback is additionally real-device verified through the temporary Google Default Media Receiver; the full catalog and Custom Receiver remain unverified on-device.

SR Extra event channels are not included because they are absent from the official public stream list. P2 Musik and P4 Södertälje are not treated as separate streams for the same reason. The official `P4 Digital` stream maps to SR channel ID 5283, whose channel API entry currently has the legacy display name `P4 Södertälje` and describes digital event broadcasts.
