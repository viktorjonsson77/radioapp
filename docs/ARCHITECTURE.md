# Architecture

## Runtime data flow

```text
Sveriges Radio HTTPS live stream
              |
              v
       Cast device / Nest Hub
              ^
              | direct media LOAD
       Custom Web Receiver
              ^
              | CAF Cast session
       Android sender app
```

Metadata is separate from playback:

```text
Sveriges Radio metadata API (future adapter)
              |                    |
              v                    v
        Android sender       Web Receiver (only where useful)
```

The phone sends the official `streamUrl` as live `MediaInfo`. The Cast receiver
fetches the HTTPS audio itself. Metadata failure must never gate media loading.
Once media is loaded, normal Cast reconnection/session behavior allows the phone
app to be backgrounded or closed; the receiver is the player. This does not mean
the Web Receiver runs forever: CAF/device idle and session lifecycle policies
still apply and require device verification.

## Modules and responsibilities

- `shared/channels.json`: the only channel catalog. Android packages it as an
  asset. Receiver scripts copy it into the static artifact.
- Android `data/`: asset parsing, DataStore favorites, settings and metadata
  adapter implementation.
- Android `domain/`: channel/program models and repository/provider contracts.
- Android `cast/`: receiver selection, session observation and live MediaInfo.
- Android `ui/`: Compose state and light Material 3 channel browser.
- Receiver `channel.ts`: strict runtime schema and official-host validation.
- Receiver `metadata.ts`: SR channel to CAF live-media mapping.
- Receiver `browse/`: framework-neutral browse plan and CAF object adapter.
- Receiver `main.ts`: CAF lifecycle, LOAD interception, capability detection,
  receiver event logging and browser fallback.

## Failure boundaries

- Catalog failure: sender shows an error; receiver keeps an explicit error
  overlay rather than a blank page.
- No Cast session: sender asks the user to use the standard Cast button.
- Placeholder app ID: sender initializes safely but refuses `playChannel` and
  reports `Custom receiver not configured`.
- Metadata unavailable: the fixture returns no program and playback uses
  `Direktsänd radio`.
- LOAD failure/session loss: surfaced through sender state/snackbar.
- Stream/media failure: CAF remains the primary player UI and receiver logging
  exposes the failure for remote debugging.

## Trust and infrastructure

No Firebase, custom backend, streaming proxy, login or API key is used. Static
receiver hosting is the only infrastructure required for device testing.
