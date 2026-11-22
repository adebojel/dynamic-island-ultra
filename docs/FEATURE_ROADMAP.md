# Dynamic Island Ultra — Feature Roadmap

## Current source baseline

- RGB/glass dashboard, draggable island, pulse, saved position and battery indicator.
- Notification preview bridge and categorized shortcut menu.
- Installed-app collection, private local notes, calculator and app-owned PiP workspace.
- Built-in offline command decision engine with arithmetic, feature routing and read-only diagnostics.
- DuckDuckGo default browser with general HTTPS navigation and Desktop/Mobile mode.
- Distribution split: `github` APK includes SHA-256-verified GitHub self-updates; `play` AAB excludes `REQUEST_INSTALL_PACKAGES` and routes updates through Google Play.
- Android 8+ minimum (API 26), cleartext traffic disabled, AI source package remains offline by CI policy.
- GitHub Actions uses the latest successful `v0.0.N` tag to increment the next patch release, while the Android `versionCode` uses the monotonically increasing workflow run number.
- Stable signing key is read from private GitHub Actions secrets; no key material belongs in source control.
- INTERNET and ACCESS_NETWORK_STATE permissions are present for application networking, AdMob/UMP consent and Google Play Billing.

## Next engineering modules

1. Physical-device matrix: Android 8/9/10, API 26–29, modern API 33–36 and OEM background restrictions.
2. Browser smoke tests on WebView variants: DDG search, HTTPS sites, desktop/mobile User-Agent, cookies, video playback and lifecycle.
3. Signed update regression: same application ID/signature, versionCode monotonicity, installer consent and app-data retention.
4. AdMob production readiness: publish UMP privacy messages, complete app review/eligibility, publish root-host app-ads.txt, verify production ad serving without self-clicking, and audit placement/frequency compliance.
5. Notification filters, allowlist and privacy controls.
6. MediaSession metadata/artwork/progress/playback controls.
7. Supported incoming/ongoing call activity cards.
8. More app-owned floating tools: image/document preview, QR/barcode scanner and sensor kit.
9. Internal window positioning/resizing, edge docking, gestures, haptics and theme editor.
10. TalkBack, larger text, high contrast and keyboard navigation.

## AI scope

The bundled offline decision engine does not import model weights and has no network access. It remains clearly distinguished from a generative LLM. A future on-device generative model needs license, runtime, model-size, RAM, CPU/GPU and Android 8+ compatibility review; no such capability will be advertised until genuinely implemented and measured.

## Current monetization state

- AdMob/UMP SDK and publisher IDs are integrated; debug uses Google's official test units and signed release uses production units. Production fill and account eligibility are not yet independently verified.
- Premium cosmetic themes, pulse speed, island size and compact layout are implemented. Paid subscription offers require the Play Console product/base plans to exist and become active.
- The GitHub Pages policy site is source-complete but deployment must succeed before its URLs are used in Play Console or AdMob. AdMob app-ads.txt requires a file at the developer hostname root, not only the project Pages subpath.
- Current paid entitlement is client-side Billing only; add secure backend purchase-token verification before account-synced or transferable entitlement.

## Release gates

- Unit/instrumentation and UI tests.
- CI gate for API 26 minimum and offline-AI source isolation.
- Repeatable signed `github` APK and `play` AAB builds with a stable private key.
- Play merged manifest excludes `REQUEST_INSTALL_PACKAGES`; GitHub flavor alone contains it.
- Play Console must review the `specialUse` foreground service declaration for the user-enabled floating island and the `SYSTEM_ALERT_WINDOW` special access.
- The ongoing floating-island notification exposes a direct Stop action as well as the dashboard Stop control.
- APK hash, 400 MiB size, version monotonicity and release-asset verification.
- Physical-device tests for in-place update and retained local data.
- Memory, lifecycle, battery, crash and accessibility review.
