# Dynamic Island Ultra — Feature Roadmap

## Current source baseline

- RGB/glass dashboard and floating island with drag, pulse, saved position and battery indicator.
- Notification preview bridge and categorized shortcut menu.
- Installed-app collection, private local notes, calculator and app-owned PiP workspace.
- Built-in offline command decision engine with arithmetic, feature routing and read-only local diagnostics.
- YouTube-only WebView and a GitHub updater that verifies SHA-256 before invoking Android's package installer.
- Android 8+ minimum (API 26), cleartext disabled, AI package network-free by CI gate.
- Versioned release workflow based on monotonically increasing GitHub Actions run numbers.
- Stable release signing configured through private GitHub Actions secrets; no private signing material in source control.

## Next engineering modules

1. Physical-device test matrix: Android 8/9/10, API 26–29, modern API 33–36 and OEM background restrictions.
2. YouTube playback smoke tests on WebView versions and network allowlist refinement.
3. Signed update regression: verify application ID, signature, monotonic version code, user-consent flow and local-data retention.
4. Notification filters, app allowlist and privacy controls.
5. MediaSession metadata/artwork/progress/playback controls.
6. Supported incoming/ongoing call activity cards.
7. More app-owned floating tools: image/document preview, QR/barcode scanner and sensor kit.
8. Internal window positioning/resizing, edge docking, gestures, haptics and theme editor.
9. TalkBack, larger text, high contrast and keyboard navigation.
10. Offline speech recognition/TTS as optional device-compatible components with explicit storage/RAM testing.

## AI scope

The built-in local decision engine is intentionally deterministic and does not import a model or contact an AI cloud. A generative on-device LLM is a separate engineering decision: model and runtime sizes, license, Android API compatibility, RAM, CPU/GPU capability, startup latency and the 400 MiB APK budget must be proven before integrating it. No generative capability should be advertised until that is implemented and tested.

## Release gates

- Unit/instrumentation and UI tests.
- CI check for Android 8/API26 floor and strict network package allowlist.
- Repeatable signed release builds from the same stable private keystore.
- APK hash validation, 400 MiB budget, version-code monotonicity and release asset verification.
- Install/update tests on physical Android devices, including a same-key in-place update.
- Lifecycle cleanup, memory, battery, crash and accessibility review.
