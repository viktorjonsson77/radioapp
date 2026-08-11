# RadioApp – Codex Workflow

## Arbetskatalog
Arbeta endast under `/opt/radioapp`.

## Före ändringar
Kontrollera `pwd`, inspektera repository, kör `git status`, förstå befintliga filer, läs `AGENTS.md` och relevanta `docs/`.

## Källor
Använd aktuell officiell dokumentation för Google Cast, Android och Sveriges Radio. Gissa inte API:er, Developer Console-menyer eller capabilities.

## Principer
Små verifierbara steg. Playback oberoende av metadata. Ett gemensamt kanalregister. Inga secrets. Ingen onödig backend eller streamingproxy. Inga gamla Assistant Actions.

## Testdisciplin
Separera BUILD VERIFIED, LOCAL TEST VERIFIED och REAL CAST DEVICE NOT YET VERIFIED / VERIFIED. Fejka aldrig device-verifiering.

## Slutrapport
Rapportera ändringar, filer, build, tester, Cast/receiver-status, begränsningar, Git-status och nästa rekommenderade steg.
