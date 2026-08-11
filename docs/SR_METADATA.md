# RadioApp – Sveriges Radio Metadata

## Princip
Metadata förbättrar upplevelsen men är inte en förutsättning för playback.

## Interface
Använd ett abstraherat `SrMetadataProvider`.

## Önskad metadata
- aktuellt program
- programbild
- start/slut
- nästa program

## Fel
Vid metadatafel fortsätter live audio. Kanalnamn hämtas från lokal kanaldefinition och UI får inte visa gammal/vilseledande programinformation.

## API
Använd dokumenterade, aktuellt fungerande SR-endpoints bakom en adapter så att API-förändringar inte påverkar playback-arkitekturen.
