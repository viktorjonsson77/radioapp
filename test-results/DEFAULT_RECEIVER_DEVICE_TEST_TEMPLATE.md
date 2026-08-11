# RadioApp Default Media Receiver device test

Date:
Nest Hub model:
Nest Hub firmware:
Android device:
Android version:
RadioApp commit:
APK SHA-256:
Receiver mode:
DEFAULT_MEDIA_RECEIVER

## Test 1 – Discovery

Expected:
Nest Hub visas i standard Cast device picker.

Result:

## Test 2 – Connect

Expected:
Cast-session etableras med Google Default Media Receiver.

Result:

## Test 3 – P1

Expected:
P1 AAC 128 börjar spela på Nest Hub.

Result:

## Test 4 – Metadata

Expected:
Hubben visar minst relevant kanalmetadata som Default Receiver stödjer.

Result:

## Test 5 – P3 channel switch

Expected:
P3 ersätter P1 utan ny Cast-session.

Result:

## Test 6 – P4 Malmöhus

Expected:
P4 Malmöhus spelar.

Result:

## Test 7 – Pause/play

Result:

## Test 8 – Stop

Result:

## Test 9 – Android background

Expected:
Playback fortsätter när Android går till bakgrunden enligt normal Cast-modell.

Result:

## Test 10 – Android process/app closed

Record actual behavior.

Result:

## Test 11 – Reopen Android

Expected:
Appen återansluter eller återupptäcker existerande Cast-session enligt CAF.

Result:

## Test 12 – Session recovery

Result:

## Diagnostics

Receiver/Hub errors:

Android logcat errors:

## Overall

- [ ] PASS
- [ ] FAIL
- [ ] INCONCLUSIVE
