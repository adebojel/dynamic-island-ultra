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
- **Offline calculator:** evaluates arithmetic, parentheses and unary signs locally.
- **Built-in local assistant:** deterministic command-routing engine, arithmetic, system shortcuts, browser/update launch and read-only diagnostics.
- **Local diagnostics:** reads Android API/version, overlay permission, notification access/permission, WebView availability, battery and cache space.
- **PiP workspace:** Android-managed PiP for this app's own activity.

The built-in assistant and local tools work without internet, imported model files or AI API credentials. The built-in engine is not a generative LLM.

## Internet-enabled modules

- **DuckDuckGo browser:** DuckDuckGo default home/search; HTTPS address bar for general websites; Back/Forward/Reload/Home; Desktop/Mobile layout mode via User-Agent and viewport switching.
- **GitHub Update Center:** checks the latest project release, downloads APK and metadata, verifies SHA-256, then opens Android's package installer.
- **Future AdMob:** network permissions are ready, but Google Mobile Ads SDK, publisher App ID/ad-unit IDs, UMP consent handling and privacy disclosures are not yet installed.
- Manifest declares `INTERNET` and `ACCESS_NETWORK_STATE`; these are normal Android install-time permissions. Cleartext HTTP remains disabled.
- CI prevents the built-in `ai/` code package from adding network clients or endpoints.

## Android compatibility and release

- Minimum Android: 8.0 Oreo / API 26.
- Compile/target SDK: 36.
- Release `versionCode` comes from the monotonically increasing GitHub Actions run number.
- Each signed main build increments the patch release tag from the latest successful `v0.0.N` (v0.0.0 → v0.0.1 → v0.0.2).
- Stable release signing secrets are required for update-compatible GitHub assets.
- APK checksum and release metadata accompany signed releases.

## Platform boundaries

- Third-party apps cannot be forced into PiP.
- Installed app list is limited to launcher-visible activities.
- Android controls APK installer confirmation; app cannot silently bypass it.
- Boot restore and long-running overlays remain subject to Android/OEM restrictions.
