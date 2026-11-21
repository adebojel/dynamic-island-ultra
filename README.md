# Dynamic Island Ultra

<div align="center">

<img src="assets/dynamic-island-offline-hero.svg" alt="Dynamic Island Ultra — floating RGB glass interface" width="100%"/>

**Dynamic Island · Floating Assistive Hub · Built-in Offline Assistant · DuckDuckGo Browser · GitHub Updater**

[![Build](https://github.com/adebojel/dynamic-island-ultra/actions/workflows/android.yml/badge.svg)](https://github.com/adebojel/dynamic-island-ultra/actions/workflows/android.yml)
[![Release](https://img.shields.io/github/v/release/adebojel/dynamic-island-ultra?label=release)](https://github.com/adebojel/dynamic-island-ultra/releases)
[![Android](https://img.shields.io/badge/Android-8%2B%20(API%2026)-43E8FF?logo=android&logoColor=white)](https://developer.android.com/)
[![Network](https://img.shields.io/badge/Network-Feature%20Scoped-42DFA5)](docs/NETWORK_AND_UPDATES.md)

[**Download latest signed APK**](https://github.com/adebojel/dynamic-island-ultra/releases/latest/download/app-release.apk) · [SHA-256](https://github.com/adebojel/dynamic-island-ultra/releases/latest/download/app-release.apk.sha256) · [All releases](https://github.com/adebojel/dynamic-island-ultra/releases)

</div>

---

## Overview

Dynamic Island Ultra is an Android floating island and assistive launcher with local utility tools, an in-app browser with DuckDuckGo as the default search engine and desktop/mobile modes, and a GitHub release updater. Its interface uses a dark glass/RGB design.

<img src="assets/floating-hub-offline-toolbox.svg" alt="Floating hub categories and tools" width="100%"/>

## Android compatibility

- **Minimum supported Android: Android 8.0 Oreo / API 26.**
- Compile SDK / target SDK: API 36.
- Android 8, 9, 10 and newer releases remain within the declared minimum compatibility range, subject to device/OEM behaviour.
- The CI gate checks `minSdk = 26`; it must not be raised to API 34.

## Current implementation

| Module | Included behaviour |
|---|---|
| Dynamic Island | Draggable floating pill, soft pulse and saved position |
| Battery | Live percentage/charging indicator while overlay is active |
| Notification bridge | Notification previews after the user grants notification-listener access |
| Assistive hub | Categorized Android settings and app shortcuts |
| App collection | Lists launcher-visible apps and launches selected apps |
| Local notes | Saves notes in app-private storage |
| Local calculator | Parses arithmetic expressions locally |
| Built-in offline assistant | Local command decision engine, calculator routing and feature/device diagnostics; no imported model, API key or cloud AI |
| App health checks | Reads overlay, notification, WebView, battery, storage and version readiness locally |
| PiP workspace | Android-managed PiP for this app's own workspace activity |
| DuckDuckGo browser | General HTTPS browser with DuckDuckGo default search, desktop/mobile mode, history navigation and reload |
| GitHub update center | Checks the latest project release, downloads the APK and SHA-256 file, verifies the hash, then opens Android's installer |

## Internet access and AdMob readiness

The APK requests both `INTERNET` and `ACCESS_NETWORK_STATE`, so future online features can use the network without changing the platform compatibility floor. Android treats these as normal install-time permissions, not runtime popups.

- **DuckDuckGo Browser:** DuckDuckGo is the default home page and search engine. The address bar also opens other HTTPS websites. The mode control switches between mobile and desktop website layouts using the WebView User-Agent and viewport settings.
- **GitHub Update Center:** checks the latest project release, downloads the signed APK and metadata, verifies SHA-256, then opens Android's package installer.
- Future online modules, such as AdMob, may use internet access. The built-in AI assistant, local diagnostics, notes and calculator remain offline by architecture and CI checks.

Cleartext HTTP is disabled; HTTPS is preferred. Network permission being available to the app does not mean each feature should upload user data. Modules must disclose their purpose and minimize transmitted data.

**AdMob is prepared at the permission level but is not yet integrated.** Actual ads require the real AdMob App ID, ad-unit IDs, Google Mobile Ads SDK setup and a privacy/consent flow. Google's current UMP guidance requires requesting updated consent information at each app launch, showing applicable messages, and checking `canRequestAds()` before an ad request. No placeholder publisher IDs or live ads are embedded.

See [Network and Update Design](docs/NETWORK_AND_UPDATES.md).

## Built-in AI and offline operation

The APK includes a deterministic local decision engine. It interprets supported commands, launches approved app/system surfaces, evaluates arithmetic and runs read-only diagnostics. It needs no model import, account, API key or internet.

**Technical distinction:** this engine is not a general-purpose generative LLM. A generative on-device model needs model weights plus an inference runtime. Official LiteRT-LM Android guidance describes model files from hundreds of MB to multiple GB and stores the model as a device file rather than APK assets. That would exceed or consume most of the current 400 MiB APK budget and would not run reliably on every Android 8+ device. We do not falsely label the built-in decision engine as a generative model.

## GitHub updates without uninstalling

The app's GitHub Update Center checks the latest release, compares its version code, downloads the APK and checksum, and verifies SHA-256 before passing it to Android's package installer.

Every successful signed build from `main` uses the GitHub Actions run number as a monotonically increasing `versionCode`, and generates a release tag such as `v0.0.39`, `v0.0.40`, and so on. No future release should reuse the fixed `v0.0.0` tag.

For Android to accept an update without uninstalling, all of these must remain true:
- Same application ID: `com.adebojel.dynamicislandultra`.
- Same release signing certificate (or a valid key-rotation lineage).
- New version code greater than or equal to the installed version.
- The user accepts Android's package-installer confirmation when shown.

### One-time GitHub signing setup

The stable private signing key must never be committed to this public repository. Add these repository **Actions secrets**:

- `RELEASE_KEYSTORE_BASE64` — base64 content of the private JKS/PKCS12 keystore.
- `RELEASE_STORE_PASSWORD` — keystore password.
- `RELEASE_KEY_ALIAS` — key alias.
- `RELEASE_KEY_PASSWORD` — key password.

Without all four secrets, CI builds a debug preview artifact only and deliberately does not publish it as an update-compatible release. Keep a secure offline backup of the keystore: if the private key is lost, Android will reject future updates signed with a different key.

The pre-existing `v0.0.0` asset was built before stable signing was configured. Moving from that legacy debug signature to a new stable key may require a one-time reinstall. After the stable key is configured and the user installs its first signed release, future releases can update in place and retain app data.

## PiP and Android boundaries

- PiP applies only to activities that declare and implement PiP support. This app cannot force other apps into PiP.
- The installed-app collection uses a narrow launcher query; apps without launcher activities may not be listed.
- A regular app cannot silently grant privileged access, force-stop immunity, arbitrary mobile-data/Wi-Fi toggles, or unrestricted control over other apps.
- Background restore remains subject to Android and device-maker restrictions.

## Permissions and privacy

The app requests overlay access, notification access/permission and install-from-this-source authorization only when their features need them. It does not request SMS, contacts, call logs, microphone capture, broad storage, broad package visibility or unrestricted network-state access.

YouTube page data and cookies are handled by Android WebView when the user opens the browser. GitHub update requests reveal ordinary connection metadata to GitHub. The offline assistant does not transmit commands or diagnostics.

## Build and release

Toolchain:
- JDK 17
- Android Gradle Plugin 9.3.1
- Gradle 9.9.1
- Compile SDK 36
- Compose BOM 2026.09.00

Local debug build:

```bash
gradle --no-daemon testDebugUnitTest assembleDebug
```

A push to `main` runs the tests and build. With stable signing secrets configured, CI publishes `app-release.apk`, `app-release.apk.sha256`, and `release-info.json` under a new versioned release. Without the signing secrets it uploads a debug-preview artifact but does not overwrite the latest update channel.

APK size is capped at 400 MiB. SHA-256 is provided for every APK artifact.

## Source and roadmap

- [Feature catalog](docs/FEATURE_CATALOG.md)
- [Feature roadmap](docs/FEATURE_ROADMAP.md)
- [Open-source research and license review](docs/OPEN_SOURCE_RESEARCH.md)
- [Network and update design](docs/NETWORK_AND_UPDATES.md)

## Validation status

CI tests and APK assembly are necessary but do not replace physical-device testing. The next verification gate is smoke-testing overlay lifecycle, notification access, YouTube playback, package-installer handoff, data retention across a signed update, Android 8/9/10 and current Android OEM variants.

---

<div align="center">

**Offline-first core · Explicit network use · User-controlled Android integration**

[Repository](https://github.com/adebojel/dynamic-island-ultra) · [Issues](https://github.com/adebojel/dynamic-island-ultra/issues)

</div>
