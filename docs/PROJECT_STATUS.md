# Project status

Baseline date: 2026-08-11

| Area | Result | Verification |
|---|---|---|
| `STARTUP_CRASH` | `FIXED` | `REAL_DEVICE_VERIFIED` |
| `CAST_ROUTE_DIALOG_CRASH` | `FIXED` | `REAL_DEVICE_VERIFIED` |
| `DEFAULT_MEDIA_RECEIVER_CAST` | `PASS` | `REAL_DEVICE_VERIFIED` |
| `SR_AAC_PLAYBACK_ON_NEST_HUB` | `PASS` | `REAL_DEVICE_VERIFIED` |
| `CUSTOM_WEB_RECEIVER` | `NOT_YET_REAL_DEVICE_VERIFIED` | External registration blocker |
| `CAST_MEDIA_BROWSE` | `NOT_YET_REAL_DEVICE_VERIFIED` | Requires Custom Receiver test |
| `GOOGLE_CAST_DEVELOPER_REGISTRATION` | `EXTERNAL_BLOCKER` | Identity/payment verification opens a blank page |

The successful physical baseline used a Samsung Android device, a real Google
Nest Hub and Google Default Media Receiver. It verified startup, the standard
route picker, connection, P1, Android-driven channel switching, P3 and P4
Malmöhus playback. See
[`test-results/default-receiver-device-test-2026-08-11.md`](../test-results/default-receiver-device-test-2026-08-11.md).

Google Default Media Receiver remains a **temporary test harness**. The accepted
product architecture is still RadioApp's Custom Web Receiver. No Custom Receiver
or Media Browse real-device verification is claimed.

`CUSTOM_RECEIVER_REAL_DEVICE_NOT_YET_VERIFIED`
