# Sveriges Radio streams

All PoC URLs were verified on 2026-08-11 against Sveriges Radio's official
[Länkar till ljudströmmar](https://om.sverigesradio.se/lankar-till-ljudstrommar-for-alla-kanaler)
page. That page publishes HTTPS AAC 32/128/320 kbps and MP3 96 kbps links and
identifies 128 kbps as AAC-LC.

| Channel | Canonical ID | AAC 128 URL |
|---|---|---|
| P1 | `p1` | `https://live1.sr.se/p1-aac-128` |
| P2 | `p2` | `https://live1.sr.se/p2-aac-128` |
| P3 | `p3` | `https://live1.sr.se/p3-aac-128` |
| P4 Malmöhus | `p4-malmo` | `https://live1.sr.se/p4malm-aac-128` |
| P4 Kristianstad | `p4-kristianstad` | `https://live1.sr.se/p4krist-aac-128` |

`shared/channels.json` is authoritative in the codebase. The current format and
quality are `AAC` / `AAC_128`; enums already reserve AAC 32, AAC 320 and MP3 96.
SR's optional latency query is not added in this PoC so device behavior can first
be measured against the canonical URL.

On 2026-08-11, `tools/validate-streams.mjs` received HTTP 200 for all five URLs.
Following SR's redirect, each endpoint reported `Content-Type: audio/aac` and
`Access-Control-Allow-Origin: *`. This verifies current HTTP reachability and
supports the chosen mapping; it is not a Cast playback/device test.

The receiver schema rejects HTTP and non-`live1.sr.se` stream hosts. This is a
PoC safety check, not a permanent assertion that SR can never change hostnames;
future official changes require source verification and a reviewed schema update.

## Metadata

Playback currently uses channel metadata and a non-blocking
`SrMetadataProvider` fixture returning no program. A future SR API adapter should
map current/next scheduled episode, program image and start/end instants. It must
use timeouts/cache and return `Result` so any API outage degrades metadata only.
No API key is invented or required by the current build.
