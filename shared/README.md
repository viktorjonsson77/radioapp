# Shared data

`channels.json` is the canonical PoC channel catalog consumed directly by the
Android asset source set and copied into the receiver build by its sync script.
Add future channels here rather than duplicating data in either application.

Schema changes must increment `schemaVersion` and be covered by both Android and
receiver parsing tests.
