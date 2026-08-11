# RadioApp – MVP 0.1

## Syfte
Bevisa kedjan:

`Android sender → Google Cast → Custom Web Receiver → officiell SR-liveström`

## Första kanaler
- P1
- P2
- P3
- P4 Malmöhus
- P4 Kristianstad

AAC 128 kbps är standard där officiell sådan ström finns.

## Android
- Compose / Material 3
- Cast-knapp enligt Cast UX
- Kanallista och Now Playing
- Start av kanal på aktiv Cast-session
- Lokala favoriter
- Standard-P4
- Central Receiver Application ID-konfiguration

## Receiver
- Custom Web Receiver
- Live audio
- Kanalmetadata
- Now Playing
- Felhantering/loggning
- Capability-detektering
- Media Browse PoC

## Riktig Nest Hub ska verifiera
1. Receiver startar.
2. SR-ström spelas.
3. Metadata visas.
4. Sender kan lämnas enligt normal Cast-modell.
5. Touch fungerar enligt capability/API.
6. Media Browse visar kanaler.
7. Kanalval på Hub fungerar om plattformen stödjer det.

## Definition of Done
Separera BUILD VERIFIED, LOCAL TEST VERIFIED och REAL CAST DEVICE VERIFIED.
