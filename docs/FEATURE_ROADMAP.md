# Dynamic Island Ultra — Feature Roadmap

This roadmap distinguishes implemented behavior from product goals. “Included” means present in source and compiled by CI; device-specific runtime behavior still needs hands-on testing.

## Implemented baseline — v0.0.0
- Kotlin / Jetpack Compose Android application scaffold (min SDK 26, target SDK 35).
- Dark cyan/violet dashboard and custom adaptive launcher icon.
- User-started foreground service for the overlay.
- Draggable Dynamic Island / assistive bubble with a soft pulse animation.
- Bubble X/Y position saved in local preferences.
- Battery percentage and charging marker delivered by the system battery broadcast.
- Notification listener bridge that forwards a short notification preview locally to the bubble after the user grants notification access.
- Scrollable floating shortcut hub with five categories: connectivity, communication, media/capture, productivity, and device/accessibility.
- Twenty-one shortcut actions that open Android settings panels or installed apps where available.
- Boot receiver for previously enabled overlay, best-effort within Android background-start restrictions.
- GitHub Actions debug APK build, 400 MiB size guard, SHA-256 generation, workflow artifact, and v0.0.0 release asset.

## Planned feature modules
- Dynamic Island state machine: collapsed, expanded, transient, pinned, and dismissible activity cards.
- Media session integration: current track, artwork, progress, elapsed/total time, play/pause/next/previous.
- Call activity cards through supported Android call/telecom surfaces and user-granted roles where applicable.
- Rich battery / charging activity card, low-battery notifications, charging states.
- Notification grouping, app allowlist, privacy filters, per-app controls, supported inline actions.
- Real floating mini-windows: notes, calculator, browser, document viewer, media player, and user-selected widgets.
- Assistive-touch actions only where Android APIs and explicit user consent permit them.
- Gesture map, edge docking, configurable hit areas, haptics, drag/resize behavior.
- Theme editor, RGB/gradient/glass effects, reduced-motion mode, accessibility labels and TalkBack traversal.
- Optional speech and AI modules with explicit opt-in, local processing where feasible, and offline alternatives.
- Local data settings, export/import, and module-level controls.

## Engineering gates
A module is not marked complete until it has:
1. A documented permission and privacy rationale.
2. API-level and OEM compatibility notes.
3. Unit or instrumentation tests where practical.
4. Lifecycle cleanup and memory-leak review.
5. Battery and performance measurements.
6. A successful CI build.
7. Manual smoke testing on representative physical devices before a production release.
