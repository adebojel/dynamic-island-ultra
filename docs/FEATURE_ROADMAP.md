# Dynamic Island Ultra — Feature Roadmap

This roadmap separates source implementation from behaviour not yet physically validated.

## Current source baseline
- Kotlin / Compose dashboard and custom RGB/glass launcher illustration.
- Draggable floating island service with pulse, saved position, battery indicator and notification-preview bridge.
- Seven floating menu sections with Android settings/app shortcuts and local utility activities.
- Launcher-visible installed-app collection (limited package visibility).
- Local notes, calculator and deterministic offline command assistant.
- App-owned PiP workspace activity.
- Offline manifest/dependency policy guard, local assistant unit tests, APK size guard and SHA-256 release.

## Next modules
1. Physical-device lifecycle, permission and OEM test matrix.
2. Notification filters, allowlist and privacy controls.
3. MediaSession metadata/artwork/playback controls.
4. Supported incoming/ongoing call activity cards.
5. Additional internal floating tools: image/document preview, timers, QR/barcode and sensor kit.
6. Window positioning/resizing for app-owned internal tools; edge docking and gestures.
7. TalkBack, contrast, larger text and keyboard navigation.
8. Offline speech recognition and text-to-speech models when compatible with device storage and RAM.
9. Generative on-device LLM integration only as a user-imported local model with license, quantization, memory, CPU/GPU and 400 MiB storage review.

## Engineering gates
- Unit/instrumentation tests for business logic and UI flows.
- Manifest/dependency audit proving no Internet permission or network SDKs.
- APK/model size and runtime RAM budgets.
- Lifecycle cleanup, memory, battery and crash review.
- Android API/OEM compatibility checks and manual physical-device smoke tests.
- Production signing key held outside repository.
