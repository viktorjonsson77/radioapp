# Decisions

## D001 — Custom CAF Web Receiver

Chosen because smart-display Media Browse, receiver-side LOAD interception,
touch capability detection and tailored radio branding exceed the Default or
Styled Media Receiver scope. Old Conversational Actions and Actions SDK are not
part of the architecture.

## D002 — No backend or phone proxy

The Nest Hub/Cast device receives and fetches the official SR HTTPS URL. Static
receiver hosting is enough for the PoC. Firebase, accounts, sync and a streaming
proxy add no current value.

## D003 — One canonical channel JSON

Android packages the root `shared/` directory as assets; receiver scripts copy it
into static public assets. Both sides test the same schema. This keeps expansion
to the full SR list mechanical.

## D004 — AAC 128 default

SR officially publishes AAC-LC 128 kbps for all supported live channels and Google
Cast lists LC-AAC support. Quality/format enums keep later options explicit.

## D005 — Compose + modest layering, no DI framework

The current object graph is small and constructed in `MainActivity`. Domain
contracts make later injection easy; Hilt/Koin would add setup without reducing
PoC risk today.

## D006 — DataStore favorites

Favorites are device-local with P1, P3 and P4 Malmöhus defaults. No login or cloud
sync. The default P4 setting is represented independently by channel ID so UI is
not structurally tied to Malmöhus.

## D007 — Two Media Browse entry points, no invented hierarchy

Google documents one flat `BrowseContent` list at each entry point and a 30-item
limit. Favoriter is the landing list; Kanaler is the in-player list. A future
combined multi-section layout requires a newly documented Cast capability or a
different supported UI approach.

## D008 — Safe placeholder behavior

When the custom Application ID is `REPLACE_ME`, CAF initializes using Google's
default receiver ID so the app can build and render its standard Cast component,
but RadioApp blocks stream loading with a clear configuration error. This avoids
claiming the custom receiver is active.

## D009 — SR right-now metadata adapter

The documented `scheduledepisodes/rightnow` method directly returns current and
next scheduled episodes. Both runtimes normalize it behind a provider and keep
fixtures for deterministic tests. Playback starts before metadata work.

## D010 — Complete official stream-list scope

The catalog includes the 36 named live services on SR's public stream page and
does not import API-only extra/event channels. All defaults are HTTPS AAC-LC 128.
The official stream page and channel API were re-audited on 2026-08-11; the
generator reproduced the checked-in catalog without differences.

## D011 — Bounded program-aware cache

Refresh occurs near program end with 30-second/5-minute bounds. A short stale
window improves resilience without displaying an old program indefinitely.

## D012 — Deterministic Media Browse subset

Google still documents one `BrowseContent` list with at most 30 items. Landing
browse contains the three development favorites. In-player browse contains all
non-local channels plus default P4 Malmöhus (12 items). Android/browser mode
expose all 36. This clear policy is preferred to arbitrary truncation.

## D013 — Direct receiver metadata over CORS

SR returned wildcard CORS to the deployed GitHub Pages origin, so receiver-side
refresh needs no proxy. Failure falls back locally and never affects audio.

## D014 — Temporary Default Media Receiver device-test mode

Status: temporary while Google Cast Developer identity/payment verification is
externally blocked. Android may explicitly select `DEFAULT` and then uses
`CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID` for basic physical
sender/stream testing without an Application ID. `CUSTOM` remains the default,
and D001 remains the accepted product architecture. Default Receiver results do
not verify GitHub Pages, custom UI/lifecycle, Media Browse, touch navigation or
receiver-side metadata refresh.

On 2026-08-11 this temporary test harness passed a physical Samsung → Cast →
Nest Hub → SR live AAC test for route selection, connection, P1, channel change,
P3 and P4 Malmöhus. This result does not change D001 or promote Default Media
Receiver to product architecture.
