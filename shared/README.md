# Shared data

`channels.json` is the canonical complete live-channel catalog consumed directly by the
Android asset source set and copied into the receiver build by its sync script.
Add future channels here rather than duplicating data in either application.

`channels.schema.json` is the JSON Schema. Schema changes must increment
`schemaVersion` and be covered by schema, Android and receiver tests.
