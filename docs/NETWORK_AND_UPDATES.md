# Network access, browser, AdMob readiness and in-app updates

## Application network permission

The Android manifest declares:

- `android.permission.INTERNET`
- `android.permission.ACCESS_NETWORK_STATE`

Android documents both as normal install-time permissions. They do not cause a runtime permission dialog. They make network connectivity available to application modules. Cleartext HTTP remains disabled; HTTPS is preferred.

The built-in AI package is still isolated from network calls by design and CI checks. Local commands, arithmetic, diagnostics, notes and calculator work without internet. Other explicitly networked modules can be added when needed, with their data use documented.

## Default browser: DuckDuckGo

The built-in browser opens DuckDuckGo as its home page and search engine. The address field accepts search queries or HTTPS website URLs. It includes Back, Forward, Reload, Home and a Desktop/Mobile mode switch.

Desktop mode changes the WebView User-Agent and viewport layout; mobile mode restores the WebView's platform User-Agent. User-Agent switching is a site-compatibility mechanism, not a full desktop operating system. Some websites may still provide mobile-only experiences or require sign-in/cookies.

Web content executes in Android WebView. File access and content-provider access are disabled, mixed content is blocked, Safe Browsing is enabled when supported, and TLS certificate errors are cancelled. Cleartext HTTP is disabled by the app network security config.

## AdMob readiness (not yet integrated)

Network permissions are in place for future Google Mobile Ads SDK connectivity. Actual AdMob implementation must wait for:

1. The publisher's AdMob App ID and ad-unit IDs.
2. Google Mobile Ads SDK dependency and manifest App ID metadata.
3. Google User Messaging Platform (UMP) consent flow and a privacy-options entry point where required.
4. Test ad units/devices during development, followed by policy-compliant production IDs.
5. Correct privacy policy, data-safety disclosures, and placement/layout that does not obstruct core app controls.

Google's current guidance requires requesting consent information at every app launch, displaying any required consent form, and checking `canRequestAds()` before requesting ads. Until the IDs and consent flow are configured, this repository does not display ads or embed fake IDs.

Official setup references:
- Android network permissions: https://developer.android.com/develop/connectivity/network-ops/connecting
- AdMob Android SDK: https://developers.google.com/admob/android/quick-start
- UMP privacy and consent: https://developers.google.com/admob/android/privacy
- WebView User-Agent and viewport: https://developer.android.com/reference/android/webkit/WebSettings

## AI and local feature boundary

The in-APK assistant is a deterministic on-device decision engine with command routing, calculations and read-only diagnostics. It has no HTTP client, endpoint, API key, telemetry or remote inference. CI rejects network URLs and network clients from the `ai/` source package.

This built-in decision engine is not a general-purpose generative LLM. A generative offline LLM requires model weights and an inference runtime. Those files may be hundreds of MB to several GB, so they must be evaluated against the 400 MiB package budget, runtime RAM, device CPU/GPU and Android 8+ support before claiming generative AI.

## GitHub updates and signing

The updater reads the latest release of `adebojel/dynamic-island-ultra`, reads `release-info.json`, downloads the APK plus SHA-256 asset, checks the digest and opens Android's package installer.

For a non-destructive in-place update Android requires:
- unchanged application ID;
- same signing certificate (or valid proof-of-rotation);
- a version code greater than the installed version;
- user approval in Android's installer UI when requested.

CI uses the monotonically increasing GitHub Actions run number as `versionCode` and version name `0.0.N`. Signed releases are published only when a stable private keystore is configured in GitHub Actions secrets:

- `RELEASE_KEYSTORE_BASE64`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Without those secrets CI builds a debug preview but does not publish it as an update-compatible release. The signing key must be backed up securely and must never be committed to the repository. Moving from the earlier debug-signed preview to the first stable-signed release may require a one-time reinstall; future releases using the same key can update in place and preserve app data.

The app cannot silently install APKs or bypass Android's package installer/user-consent controls.
