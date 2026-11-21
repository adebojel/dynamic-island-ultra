# Dynamic Island Ultra — Feature Catalog

## Floating island and assistive hub

- User-started foreground overlay service.
- Draggable floating pill, pulse animation and persisted X/Y position.
- Live battery percentage/charging state.
- Local notification preview after notification access is granted.
- Categorized shortcut hub for Android settings and installed apps.

## Offline modules

- **Installed app collection:** launcher-visible activity list using a narrow manifest query; launches selected app.
- **Offline notes:** stores note text in app-private preferences.
- **Offline calculator:** evaluates basic arithmetic, parentheses and unary signs locally.
- **Built-in local assistant:** deterministic command-routing engine, math expression parser, system shortcut selection, YouTube/update routing and read-only diagnostics.
- **Local diagnostics:** reports Android API/version, overlay permission, notification access/permission, WebView availability, battery and app cache space without transmitting information.
- **PiP workspace:** Android-managed PiP for this app's own activity.

The built-in assistant is not a generative LLM. It does not download or import models, call an AI service or require an account.

## Explicit network modules

- **YouTube browser:** HTTPS WebView restricted to YouTube top-level navigation. A subresource allowlist supports YouTube playback and required Google/YouTube content delivery. JavaScript is used only because the YouTube player requires it; file/content access is disabled, cleartext is denied, and SSL errors are cancelled.
- **GitHub Update Center:** checks only the latest release of this repository, reads release metadata, downloads the APK and SHA-256 asset, verifies the digest, then opens Android's package installer.
- The Android package declares INTERNET for those two modules. Network use is not class-level OS permission: CI rejects HTTP clients, URLs and endpoints in `ai/`, and rejects network clients in any other source package.
- `ACCESS_NETWORK_STATE`, ads, analytics, telemetry, cloud sync and remote AI endpoints are not used.

## Android compatibility and release

- Minimum Android: 8.0 Oreo / API 26.
- Compile/target SDK: 36.
- Release `versionCode` comes from the monotonic GitHub Actions run number.
- Each signed main build publishes a unique release tag `v0.0.N`.
- Stable signing secrets are required to publish an update-compatible release. APK updates still require the same signing identity and Android's installer consent.
- CI validates local assistant tests, minimum API, scoped network policy, APK build and 400 MiB size ceiling; SHA-256 and release metadata accompany signed releases.

## Platform boundaries

- A third-party app cannot be forced into PiP by this app.
- The app list is limited to launcher-visible activities; no QUERY_ALL_PACKAGES permission.
- Android controls the package installer and may require user authorization for installing from this source.
- Boot restore and long-running overlays remain subject to Android/OEM restrictions.
