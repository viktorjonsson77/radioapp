# RadioApp UI state model

This document defines presentation states only. Playback, receiver selection
and the Custom Web Receiver architecture remain unchanged.

## Screen hierarchy

```text
RadioApp + Cast button
└── Now Playing
    ├── artwork
    ├── channel / program / LIVE / time
    ├── next program
    └── connected Cast destination + playback controls
├── Favorites (horizontal)
├── National channels
├── P4 local (default + expandable list/change dialog)
├── Other channels
└── Debug receiver details (debug builds only)
```

## Now Playing and metadata

| Input state | Presentation | Playback impact |
|---|---|---|
| No selected channel | “Ingen kanal spelas” and neutral artwork | None |
| Channel selected, metadata loading | Channel + “Sveriges Radio” + LIVE | Continues |
| Fresh metadata | Program artwork/name, channel, LIVE, time and optional next | Continues |
| Metadata unavailable/stale rejected | Channel + “Sveriges Radio” + LIVE | Continues |
| Image request fails | Next image in program → channel → local fallback order | None |

There is no full-screen metadata spinner. The channel catalog may use a small
placeholder while its packaged asset is loaded, but Cast controls and the
screen structure remain stable.

## Channel, favorite and P4 states

- A channel is selected when its stable ID equals the Cast state's current
  channel ID. The row then shows “LIVE · Spelas nu” and a selected container.
- Favorite state is a local set of stable IDs. Unknown legacy IDs are ignored.
- New installations start with P1, P3 and P4 Malmöhus.
- Default P4 is stored as an ID. An unavailable stored ID falls back to P4
  Malmöhus if present, then the first current P4 channel.
- The 25 P4 channels stay collapsed until explicitly expanded. The change
  dialog is an inline setting, not a new navigation destination.

## Cast and errors

| Cast state | Product presentation |
|---|---|
| Disconnected | Standard Cast button; no technical status block |
| Connected with friendly name | “Spelar på <name>” in Now Playing |
| Connected without name | “Spelar på Cast-enhet” |
| Suspended/disconnected | Destination disappears; brief useful feedback if supplied |
| Stream command fails | Short actionable message; no stack trace |

DEFAULT/CUSTOM configuration text is permitted only in debug builds. Both mode
selection paths and Google's standard MediaRouteButton remain unchanged.

## Receiver/browser states

Browser mode loads the shared catalog, shows an explicit “Webbläge · inte Cast”
label and groups all 36 channels into favorites, national, collapsed P4 and
other. Selecting a channel updates the stable fallback immediately; SR metadata
then enriches it asynchronously. Metadata failure cannot stop browser audio.

In Cast runtime the browser surface is hidden and CAF's `cast-media-player`,
existing Media Browse policy and receiver-side metadata refresh remain active.
Custom Receiver behavior is locally testable but remains real-device unverified.
