# Wicara benchmark protocol

Do not invent or extrapolate physical measurements from CI or emulator builds.

## Device record

Record device model, SoC, Android/API, ABI, RAM, build fingerprint, app version, model revision and SHA-256, ambient temperature, battery level, battery-saver state and network state.

## Repeatable workload

1. Disable Wi-Fi and mobile data.
2. Verify the installed app and Wicara metadata.
3. Activate Wicara; record time to ready and peak PSS/RSS.
4. Run 10 fixed short Indonesian prompts, three times each with unchanged context/output settings.
5. Record generation duration, generated token count, tokens/second, CPU load and temperature.
6. Repeat with a 10-minute then a 30-minute session; record throttling and responsiveness.
7. Leave the assistant, verify native unload and observe memory recovery.
8. Repeat after clean install and signed in-place update.
9. On ARMv7, confirm the non-AI app remains usable and Wicara explains the unsupported ABI.

## Required results

| Metric | Status before physical test | Evidence |
|---|---|---|
| Model load time | NOT TESTED | Stopwatch/logcat |
| Peak and steady PSS | NOT TESTED | dumpsys meminfo |
| Tokens/second | NOT TESTED | Fixed prompt timing |
| CPU utilization | NOT TESTED | Perfetto/system monitor |
| Battery drain | NOT TESTED | Battery stats |
| Temperature | NOT TESTED | Thermal service/OEM telemetry |
| Thermal throttling | NOT TESTED | 10/30-minute trace |
| Cancellation and unload | NOT TESTED | Logcat/PSS trace |
| Offline-only inference | NOT TESTED | Network disabled + request inspection |

Mark PASS only when raw evidence is attached. An emulator or successful CI build is not physical-device evidence.
