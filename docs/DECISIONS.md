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

SR officially publishes AAC-LC 128 kbps for all five PoC channels and Google
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

## D009 — Metadata fixture first

`SrMetadataProvider` exists, but playback does not wait on network metadata. A
real SR adapter follows after stream and Hub browse behavior are proven.
