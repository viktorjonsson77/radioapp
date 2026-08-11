# Nest Hub and Cast Media Browse PoC

Sources rechecked on 2026-08-11 for this PoC:

- [Cast Media Browse](https://developers.google.com/cast/docs/web_receiver/media-browse)
- [Optimize for Smart Displays](https://developers.google.com/cast/docs/web_receiver/optimize-smart-displays)
- [Web Receiver system capabilities](https://developers.google.com/cast/docs/reference/web_receiver/cast.framework.system)
- [Add core receiver features](https://developers.google.com/cast/docs/web_receiver/core_features)

## What the documented API can do

CAF's Google-owned Media Browse template is available on smart displays. The
receiver supplies a `BrowseContent` title and up to 30 flat `BrowseItem` values.
Items can carry entity, title, subtitle, duration, image, image type, media badge
and a shared target aspect ratio. Media Browse appears as:

- landing-page browse while a receiver containing `cast-media-player` is IDLE;
- in-player browse, reached by swiping up during playback.

Selecting a BrowseItem makes the Web Receiver SDK send a `LOAD` based on its
entity. RadioApp intercepts that LOAD, maps `radioapp://channel/<id>` to the
canonical SR URL and live metadata, and returns it to CAF. The Hub therefore can
switch channels without the Android sender remaining connected, while the Web
Receiver session remains alive.

## Implemented content-layer policy

- Landing carousel: **Favoriter** — P1, P3 and P4 Malmöhus.
- In-player carousel: **Utvalda kanaler** — all 11 non-local live channels plus default P4 Malmöhus (12 total).
- Each item is marked LIVE and uses square artwork.
- LOAD interceptor resolves entities using the locally loaded shared catalog.
- `TOUCH_INPUT_SUPPORTED` is read from `getDeviceCapabilities()` after READY and
  exposed as `body[data-touch]` for future capability-driven UI.
- CAF built-in player supplies standard play/pause/media controls according to
  supported media commands.

## Google-owned versus application-controlled UI

Google controls layout, interaction, entry points, typography and carousel
behavior. RadioApp controls the list title, item order, entity, title, subtitle,
image, live badge and aspect-ratio choice. The `cast-media-player` variables
allow supported branding, but do not turn Media Browse into arbitrary HTML.

## Important limitations

- The published `BrowseContent` API is one flat carousel, not a hierarchy. It
  documents no root-item/container/category tree and no simultaneous Favoriter +
  Kanaler sections. The PoC uses the two official entry points instead of
  pretending unsupported hierarchy exists.
- Maximum item count is 30. The complete 36-channel catalog therefore uses the
  deterministic subset above. Android and browser mode expose all channels.
- The sender is required to discover and initially launch an unpublished
  receiver. It is not required for BrowseItem LOAD handling after launch.
- A receiver is not guaranteed to run forever without a sender. Device/session
  idle policy and recovery after receiver termination need product design.
- Touch capability does not imply identical UI across every firmware/device.
- Audio-only Cast targets have no display/touch browse surface, but still accept
  normal live audio media if the console registration enables audio-only devices.
- Chromecast/Google TV has a display and potentially remote/DPAD input, but CMB
  is specifically documented as a smart-display experience. Do not infer Nest
  Hub behavior from TV/browser tests.

## REAL_DEVICE_REQUIRED

The following remain unverified until a registered physical Nest Hub is used:

- receiver launch and persistence;
- full-catalog AAC behavior and direct SR metadata/CORS inside the Custom Receiver runtime (P1, P3 and P4 Malmöhus audio is verified separately with Google Default Media Receiver);
- actual Media Browse landing and swipe-up rendering;
- touch selection producing LOAD and changing channels;
- play/pause/stop behavior on the Hub surface;
- behavior after Android is backgrounded, killed or disconnected;
- firmware-specific safe area and artwork rendering.
