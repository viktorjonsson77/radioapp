# RadioApp – UI Guidelines

## Product direction

RadioApp should feel like a modern digital table radio: immediate, calm,
live-focused and readable. It is neither a Cast diagnostic dashboard nor a
copy of Sveriges Radio's application. The primary theme is light and uses one
small RadioApp color system with red as the live/action accent.

## Android screen hierarchy

The Android application remains a scrollable single screen:

1. RadioApp top bar and the standard AndroidX `MediaRouteButton`.
2. Now Playing as the main visual focus.
3. Compact, horizontally scrolling favorites.
4. National channels.
5. A compact default-P4 entry and an expandable alphabetical P4 list.
6. Other live channels.
7. Receiver-mode diagnostics in debug builds only.

The initial favorites remain P1, P3 and P4 Malmöhus. Favorite and default-P4
choices are stored locally by stable channel ID. Changing default P4 uses a
small dialog; it does not introduce an application navigation stack.

## Now Playing

The active card prioritizes artwork, channel, program and LIVE. Program times
and the next program are secondary. Artwork uses `ContentScale.Fit` to avoid
aggressively cropping faces or channel marks and follows this fallback order:

1. current program image;
2. channel image;
3. local RadioApp image.

Metadata loading never blocks channel selection or Cast controls. If metadata
is unavailable, the stable state is channel name, “Sveriges Radio” and LIVE.
With no active channel, the card calmly says “Ingen kanal spelas”.

## Channel controls

Channel rows are compact and omit long descriptions. Selection is communicated
with color and “LIVE · Spelas nu”, not color alone. Favorite actions have
contextual accessibility labels. Interactive controls use at least 48 dp touch
targets and scalable Material typography.

## Cast presentation

The standard Cast button is the only route picker. When connected, Now Playing
may show “Spelar på” and the receiver's friendly name. Receiver mode names and
configuration diagnostics are limited to debug builds; product UI never asks a
listener to understand DEFAULT versus CUSTOM.

The dedicated AppCompat MediaRouteButton theme remains opaque. Do not replace
its theme context with a transparent Material/Compose context.

## Receiver and browser

The browser page uses the same hierarchy and identity, but explicitly labels
itself as browser mode rather than a Cast emulator. The Custom Receiver runtime
continues to use CAF's `cast-media-player` and existing LOAD/metadata behavior.

For a Nest Hub-like landscape screen, artwork and Now Playing sit beside a
small number of large choices. Favorites are immediately visible; all 25 P4
stations sit behind a deliberate expand action. At narrower widths the layout
becomes one column and favorites scroll horizontally. Keyboard focus, semantic
headings, status regions, contrast and reduced-motion preferences are honored.

## Branding

Use RadioApp's own simple R/radio identity. Do not reproduce SR logos beyond
official channel/program imagery supplied for the content being played.
