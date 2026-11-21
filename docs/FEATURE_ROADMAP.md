# Dynamic Island Ultra — Feature Roadmap

This roadmap distinguishes the product vision from features implemented in the current source tree.

## Current baseline (0.0.0)
- Compose dashboard and permission entry points.
- User-started foreground overlay service.
- Draggable floating pill / assistive bubble.
- Categorized Android settings shortcuts.
- Notification listener preview bridge.
- Boot receiver for previously enabled experience (best effort).
- GitHub Actions debug APK build and release automation.

## Planned modules
- Island state machine: collapsed, expanded, transient, pinned.
- Media session controller: metadata, transport controls, artwork.
- Call activity cards using supported Android telecom state APIs.
- Battery and charging events.
- Notification grouping, app allowlist, privacy filters, per-app controls.
- Floating utility launcher with calculator, timer, notes, shortcuts, and user-selected app launchers.
- Gesture map, edge docking, haptic feedback.
- Theme engine, color presets, animation profiles, reduced-motion mode.
- Accessibility labels, TalkBack traversal, large text and contrast modes.
- Optional speech and AI modules with explicit opt-in and offline alternatives.

## Engineering gates
A module is not marked complete until it has:
1. A documented permission and privacy rationale.
2. API-level and OEM compatibility notes.
3. Unit or instrumentation tests where practical.
4. Lifecycle cleanup and memory-leak review.
5. Battery and performance measurements.
6. A successful CI build.
