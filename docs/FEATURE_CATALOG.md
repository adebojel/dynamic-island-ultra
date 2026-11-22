# Dynamic Island Ultra — Feature Catalog

## Floating island and assistive hub

- User-started foreground overlay service.
- Draggable floating pill, pulse animation and persisted X/Y position.
- Live battery percentage/charging state.
- Local notification preview after notification access is granted, with global disable, hidden content by default, app allowlist and filtering of secret, ongoing, group-summary and own-app notifications.
- Generic call activity pill from call-category notifications only; it obeys the global preview switch and app allowlist and uses no call logs or phone-state permission.
- MediaSession metadata, artwork, play/pause, skip and seek where the player exposes those actions.
- Categorized shortcut hub for Android settings and installed apps.

## Offline modules

- **Installed app collection:** queries launcher-visible activities using `ACTION_MAIN` + `CATEGORY_LAUNCHER` (no `QUERY_ALL_PACKAGES`); labels, icons and package names are used locally to populate the launch list and are not uploaded to a developer server or the offline assistant.
- **Offline notes:** stores note text in app-private preferences.
- **Offline calculator:** evaluates arithmetic, parentheses and unary signs locally.
- **Built-in local assistant:** deterministic command-routing engine, arithmetic, system shortcuts, browser/update launch and read-only diagnostics.
- **Local diagnostics:** reads Android API/version, overlay permission, notification access/permission, WebView availability, battery and cache space.
- **PiP workspace:** Android-managed PiP for this app's own activity.
- **QR/barcode scanner:** Google Play services Code Scanner 16.1.0; no app camera permission; first use may download its scanner module.
- **Image/PDF preview:** Android document picker, local image decoding and first-page PDF rendering.
- **Sensor kit:** foreground-only accelerometer, gyroscope, magnetometer, light, proximity and pressure readings; listeners stop on pause.
- **Voice assistance:** Android 12+ on-device SpeechRecognizer when a local service exists, explicit microphone request and device TextToSpeech response.
- **Island interaction:** persisted drag position, optional left/right edge docking and opt-in haptic feedback.

The built-in assistant and local tools work without internet, imported model files or AI API credentials. The built-in engine is not a generative LLM.

## Internet-enabled modules

- **DuckDuckGo browser:** DuckDuckGo default home/search; HTTPS address bar for general websites; Back/Forward/Reload/Home; Desktop/Mobile layout mode via User-Agent and viewport switching.
- **GitHub Update Center (GitHub flavor only):** checks the latest project release, downloads APK and metadata, verifies SHA-256, then opens Android's package installer.
- **Update command:** the offline assistant routes to the app's official update center with distribution-neutral wording. The GitHub flavor checks signed GitHub releases and the Play flavor opens the Play Store listing.
- **Google Play update route (Play flavor):** opens the Play Store listing; the Play variant does not include the package-install permission or GitHub self-updater path.
- **AdMob/UMP:** Google Mobile Ads Next-Gen, publisher App ID/ad units, UMP consent flow, privacy options, Banner, Native Advanced, Rewarded and Rewarded Interstitial are integrated. Live serving remains dependent on publisher configuration, app review/eligibility, consent and inventory.
- Shared manifest declares `INTERNET` and `ACCESS_NETWORK_STATE`; these are normal Android install-time permissions. `REQUEST_INSTALL_PACKAGES` is declared only by `app/src/github/AndroidManifest.xml`, never by the Play flavor. Cleartext HTTP remains disabled.
- CI prevents the built-in `ai/` code package from adding network clients or endpoints.

## Android compatibility and release

- Minimum Android: 8.0 Oreo / API 26.
- Compile/target SDK: 36.
- Release `versionCode` comes from the monotonically increasing GitHub Actions run number.
- Each signed main build increments the patch release tag from the latest successful `v0.0.N`; the current verified release at the time of this update is `v0.0.8`.
- Stable release signing secrets are required for the signed GitHub APK and Play AAB; both production flavors use the same application ID and stable release key.
- APK checksum and release metadata accompany signed releases.

## Platform boundaries

- Third-party apps cannot be forced into PiP.
- Installed app list is limited to launcher-visible activities.
- Android controls APK installer confirmation; app cannot silently bypass it.
- Boot restore and long-running overlays remain subject to Android/OEM restrictions.

## Floating bubble configuration update

- Tap, double-tap, long-press, and configurable four-direction swipe gesture mapping.
- Allowlisted actions route only to the app's own menu, offline assistant, media controls, notification privacy screen, PiP workspace, and bubble settings.
- Swipe mappings default to disabled to preserve normal drag behaviour.
- User-adjustable bubble size (80–140%) and opacity (40–100%); live appearance refresh is broadcast only within this app.
- Gesture action validation falls back to safe defaults for invalid or stale stored values; arbitrary commands are never accepted.

## Configurable bubble Quick Favorites

- Four user-selectable shortcuts are displayed at the top of the bubble panel.
- Available allowlisted actions: offline assistant, notes, calculator, installed apps, media controls, QR scanner, document preview, sensor kit, app-owned PiP workspace and Island settings.
- Each slot persists in app-private preferences; invalid/stale values resolve to a safe default.
- Favorites open only app-owned activities. They do not perform privileged Android actions or launch arbitrary commands.
