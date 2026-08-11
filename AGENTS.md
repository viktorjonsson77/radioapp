# AGENTS.md – RadioApp

## Projektmål
RadioApp ska göra Google Nest Hub till en modern, fristående radio för Sveriges Radios livekanaler. Detta är inte en simpel Cast-app.

## Regler
1. Arbeta endast inom `/opt/radioapp`.
2. Läs relevanta dokument under `docs/` före större ändringar.
3. Använd aktuell officiell Google Cast-dokumentation.
4. Använd Sveriges Radios officiella streamlista som källa.
5. Cast-enheten ska hämta ljudströmmen direkt; telefonen får inte vara ljudproxy.
6. Metadata får aldrig vara ett krav för playback.
7. Gamla Google Assistant Conversational Actions får inte användas.
8. Ingen Firebase, cloud-backend eller streamingproxy utan dokumenterat behov.
9. Ingen fysisk Cast/Nest Hub-funktion får markeras verifierad utan riktig device-test.
10. Secrets får inte checkas in.
11. Skriv inte över okända befintliga ändringar.

## Teknik
Android: Kotlin, Jetpack Compose, Material 3, Google Cast Application Framework.
Receiver: Custom Web Receiver med aktuell Google Cast Web Receiver SDK.
Shared: gemensam kanaldefinition.

## Verifiering
Separera alltid:
- BUILD VERIFIED
- LOCAL TEST VERIFIED
- REAL CAST DEVICE VERIFIED / NOT YET VERIFIED
