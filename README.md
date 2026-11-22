# Dynamic Island Ultra

<div align="center">

<img src="assets/dynamic-island-offline-hero.svg" alt="Dynamic Island Ultra — floating RGB glass interface" width="100%"/>

**Dynamic Island · Floating Assistive Hub · Built-in Offline Assistant · DuckDuckGo Browser · GitHub Updater**

[![Build](https://github.com/adebojel/dynamic-island-ultra/actions/workflows/android.yml/badge.svg)](https://github.com/adebojel/dynamic-island-ultra/actions/workflows/android.yml)
[![Release](https://img.shields.io/github/v/release/adebojel/dynamic-island-ultra?label=release)](https://github.com/adebojel/dynamic-island-ultra/releases)
[![Android](https://img.shields.io/badge/Android-8%2B%20(API%2026)-43E8FF?logo=android&logoColor=white)](https://developer.android.com/)
[![Network](https://img.shields.io/badge/Network-Internet%20Enabled-42DFA5)](docs/NETWORK_AND_UPDATES.md)

[**Download latest signed APK**](https://github.com/adebojel/dynamic-island-ultra/releases/latest/download/app-release.apk) · [SHA-256](https://github.com/adebojel/dynamic-island-ultra/releases/latest/download/app-release.apk.sha256) · [All releases](https://github.com/adebojel/dynamic-island-ultra/releases) · [Official website](https://adebojel.github.io/dynamic-island-ultra/) · [Website source](https://github.com/adebojel/dynamic-island-ultra/tree/main/site)

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
| Dynamic Island | Draggable floating pill, soft pulse, saved position and optional visible auto-dim after 8 seconds of inactivity |
| Battery | Live percentage/charging indicator while overlay is active |
| Notification bridge & privacy | Local preview settings, hide content by default, app allowlist, secret/ongoing/group-summary filtering, and generic call status cards |
| Assistive hub | Categorized Android settings and app shortcuts |
| Global System Controls | User-triggered Home, Back, Recents, Notifications, Quick Settings, Power, Lock and Screenshot; media volume; flashlight; brightness and auto-rotation with required Android consent |
| App collection | Lists launcher-visible apps and launches selected apps |
| Local notes | Saves notes in app-private storage |
| Local calculator | Parses arithmetic expressions locally |
| Smart Toolbox | Local unit conversion, counter, stopwatch with lap recording, countdown/Pomodoro timer and accelerometer-based two-axis spirit level with user calibration |
| Built-in offline assistant | Local command decision engine, calculator routing and feature/device diagnostics; no imported model, API key or cloud AI |
| App health checks | Reads overlay, notification, WebView, battery, storage and version readiness locally |
| PiP workspace | Android-managed PiP for this app's own workspace activity |
| DuckDuckGo browser | HTTPS navigation, Desktop/Mobile mode, local bookmarks, capped local history and DownloadManager-backed HTTPS downloads |
| GitHub update center (GitHub flavor only) | Checks the latest project release, downloads the signed APK and SHA-256 file, verifies the hash, then opens Android's installer |
| Media session | Metadata/artwork, play/pause, previous/next and seek when the player publishes a supported session |
| QR/barcode scanner | Google Play services Code Scanner; app does not request camera permission; scanner module may download on first use |
| Image/PDF preview | Android document picker and local image/first-page PDF preview |
| File Manager | User-selected SAF folder browsing, search, sorting, folder creation, open/share/rename/delete, file copy/move to another selected folder and ZIP export; no broad storage permission |
| Encrypted Backup | Password-derived AES-256-GCM encrypted export/import of selected app-local preferences, notes, browser library, notification privacy and reminders; excludes purchases and OS permissions |
| Persistent Reminders | Up to 30 local reminders scheduled with AlarmManager; survive app process closure and are re-scheduled after reboot, subject to Android Doze and notification permissions |
| Clipboard History | Explicit user-triggered capture of plain-text clipboard entries, up to 50 locally stored items with copy/delete/clear; no background polling |
| Floating Window Manager | Up to four app-owned mini windows for Notes, Calculator and Timer with drag, minimize/restore, resize preset and close |
| Sensor kit | Live supported sensors while the screen is foregrounded; listeners stop on pause |
| Voice assistance | Android 12+ on-device recognition when available, explicit mic permission and device speech read-out |

## Internet access, AdMob and Premium

The APK requests both `INTERNET` and `ACCESS_NETWORK_STATE`, so future online features can use the network without changing the platform compatibility floor. Android treats these as normal install-time permissions, not runtime popups.

- **DuckDuckGo Browser:** DuckDuckGo is the default home page and search engine. The address bar also opens other HTTPS websites. The mode control switches between mobile and desktop website layouts using the WebView User-Agent and viewport settings.
- **GitHub Update Center:** available only in the GitHub APK distribution flavor. It checks the latest project release, downloads the signed APK and metadata, verifies SHA-256, then opens Android's package installer.
- **Google Play updates:** the Play distribution flavor disables the GitHub APK installer and opens the Play Store listing for app updates.
- AdMob and Google Play Billing use network access only for their respective monetization flows. The built-in AI assistant, local diagnostics, notes and calculator remain offline by architecture and CI checks.

Cleartext HTTP is disabled; HTTPS is preferred. Network permission being available to the app does not mean each feature should upload user data. Modules must disclose their purpose and minimize transmitted data.

**AdMob is integrated using Google Mobile Ads Next-Gen SDK 1.7.0 and UMP SDK 4.0.0.** The manifest contains the publisher App ID and all four publisher ad units are configured. Debug APKs use Google's official test ad units; release APKs use production ad units. This separation is required: Google says always use test ads during development and warns that using live ads in testing may result in account suspension.

UMP consent information is refreshed at app launch, applicable consent forms are shown before ad requests, and every request is gated by `canRequestAds()`. A privacy-options entry point is shown whenever UMP requires it. The publisher must still publish consent messages under AdMob → Privacy & messaging and complete AdMob app review/eligibility before expecting production ad fill.

**Ad placement and frequency:** Banner and Native Advanced appear only on the dedicated Bonus & Premium screen. Native cards are clearly labelled “IKLAN”. Rewarded Ads and Rewarded Interstitial are requested only after a direct user action; Rewarded Interstitial has a disclosure dialog with “Lanjutkan” and “Lewati”. The application applies a combined cap of two video reward impressions per rolling 24 hours and a one-per-24-hour cap for Rewarded Interstitial. The app does not schedule its own Banner reload timer; any unit-level refresh behaviour must be reviewed in AdMob. No ads are requested from the floating island, assistant, microphone, notifications, browser, permission centre or Android system controls. Delivered video duration is determined by the ad creative and cannot be guaranteed by the app.

### Premium and feature access

The core app remains free. Premium customization includes Advanced RGB Themes (Cyan, Violet, Emerald, Amber), Island Pulse FX (Soft, Normal, Fast, Off), Island Size, and Compact Panel layout. A successfully earned Rewarded Ads or Rewarded Interstitial reward grants these cosmetic options for 24 hours. These local ad rewards are not account-backed or fraud-resistant.

Paid Premium uses Google Play Billing Library 9.1.0 and subscription product ID `diu_premium`. The app queries active offers and shows the localized price returned by Google Play; it does not hardcode a purchasable price or simulate a successful purchase. Before paid plans appear, the publisher must create the product and activate base plans in Play Console. The intended starting price target is Rp2,000; final localized pricing and terms are controlled by Play Console.

Standard prepaid subscription durations include one week, four weeks, one, two, three, four, six, or eight months, and one year. Exact two-week, three-week, and two-year single base plans are not standard Indonesian Play Console subscription durations. Those need a separate one-time pass/secure entitlement design; this build does not display them as purchasable. Configure the intended Play base plans as prepaid if the goal is to avoid automatic renewal. Always display renewal, expiry, and cancellation terms returned by Google Play.

The current paid entitlement is checked with the Play Billing client on device and acknowledged according to Play Billing requirements. Before scaling valuable rewards or allowing accounts to synchronize Premium across devices, add a trusted backend that verifies purchase tokens with the Google Play Developer API.

See [Network and Update Design](docs/NETWORK_AND_UPDATES.md).

## Built-in AI and offline operation

The APK includes a deterministic local decision engine. It interprets supported commands, launches approved app/system surfaces, evaluates arithmetic and runs read-only diagnostics. It needs no model import, account, API key or internet.

**Technical distinction:** this engine is not a general-purpose generative LLM. A compact experimental candidate is Gemma 3 270M IT in Q4_K_M GGUF format (about 253 MB in a community quantization). Combined with the current 19.1 MB APK, the weights alone would use about 272 MB of the 400 MB managed-storage budget, leaving roughly 128 MB for the native inference runtime, tokenizer/assets, cache and updates. The Gemma family advertises broad multilingual capability, but the 270M variant's Indonesian quality has not been independently validated for this app. Gemma is governed by the Gemma Terms of Use, not Apache-2.0; distributing weights or derivatives requires the prescribed use restrictions, a copy of the Terms and a Notice file. Therefore no model or inference runtime is bundled yet. A device benchmark, language evaluation, memory/thermal test and license packaging review are still required. The existing assistant is accurately described as a deterministic offline command engine, not a generative LLM.

## Distribution-specific update channels

The project now builds two product flavors with the same production application ID and stable release signing key:

| Artifact | Flavor | Update method | Installer permission |
|---|---|---|---|
| `app-release.apk` | `github` | GitHub Releases updater, SHA-256 verification, then Android's package installer | Declared only by `app/src/github/AndroidManifest.xml` |
| `app-release.aab` | `play` | Updates are handed off to Google Play | `REQUEST_INSTALL_PACKAGES` is absent from the shared and Play manifests |

The GitHub-only self-updater is not included as an active update path in the Play flavor. The Play dashboard's update action opens the Google Play listing; `AppUpdateActivity` also guards against running the GitHub updater when the Play flavor is used. **Do not upload the earlier v0.0.3 AAB to Play Console:** it was built before this manifest/flavor separation and still inherited the self-update permission. Upload only an AAB from a successfully verified post-separation `bundlePlayRelease` CI run.

### GitHub APK updates without uninstalling

The GitHub flavor's updater checks the latest release, compares its version code, downloads the APK and checksum, and verifies SHA-256 before passing it to Android's package installer.

Every successful signed build from `main` increments the patch tag from the latest published `v0.0.N`, so the next release after `v0.0.0` is `v0.0.1`, then `v0.0.2`, and so on. The internal Android `versionCode` uses the monotonically increasing GitHub Actions run number, so cancelled or failed CI runs cannot cause Android downgrade checks.

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

The app requests overlay access and notification access/permission only when their features need them. Notification content is hidden by default and can be disabled or restricted to an app allowlist. Voice input requests microphone access only after the user activates it and uses the on-device recognizer only where Android reports it available. QR scanning delegates to Google Play services and adds no app CAMERA permission. Image/PDF selection uses Android's document picker, not broad storage access. Install-from-this-source authorization exists only in the GitHub distribution APK, not in the Play distribution AAB. It does not request SMS, contacts, call logs or broad package visibility. INTERNET and ACCESS_NETWORK_STATE are normal install-time network permissions.

DuckDuckGo searches and visited sites are handled by WebView and the selected website providers. GitHub update requests reveal ordinary connection metadata to GitHub. The offline assistant does not transmit commands or diagnostics.

## Build and release

Toolchain:
- JDK 17
- Android Gradle Plugin 9.3.1
- Gradle 9.9.1
- AndroidX Fragment 1.9.1
- Compile SDK 36
- Compose BOM 2026.06.00 (Compose 1.11.x line, compatible with compile SDK 36)

Local debug build:

```bash
gradle --no-daemon testGithubDebugUnitTest testPlayDebugUnitTest assembleGithubDebug assemblePlayDebug
```

A push to `main` runs unit tests for both distribution flavors. With stable signing secrets configured, CI builds `assembleGithubRelease` for the GitHub APK and `bundlePlayRelease` for the Play AAB. It validates that `REQUEST_INSTALL_PACKAGES` is absent from the shared manifest and present only in the GitHub flavor manifest, verifies the signed artifacts, calculates SHA-256, and publishes canonical `app-release.apk`, `app-release.aab`, checksum files, and `release-info.json`. Debug variants use Google's official test ad units only; release variants use the publisher's production ad units. Without stable signing secrets CI uploads a debug-preview artifact but does not overwrite the update channel.

APK size is capped at 400 MiB. SHA-256 is provided for every APK artifact.

## Official website and policies

The public policy pages and official website source are stored in `site/`.

**GitHub Pages mirror:** latest policy deployment completed successfully in [Pages workflow run #14](https://github.com/adebojel/dynamic-island-ultra/actions/runs/45013720877) on 22 November 2026. GitHub validated the website, uploaded the site artifact and deployed it to [the official project site](https://adebojel.github.io/dynamic-island-ultra/). Verify each policy URL in a normal browser on the target device before submitting it to Play Console or AdMob.

- [Privacy Policy](https://adebojel.github.io/dynamic-island-ultra/privacy.html)
- [Terms and Conditions](https://adebojel.github.io/dynamic-island-ultra/terms.html)
- [Ads Disclosure](https://adebojel.github.io/dynamic-island-ultra/ads.html)
- [Premium information](https://adebojel.github.io/dynamic-island-ultra/premium.html)
- [Help and Support](https://adebojel.github.io/dynamic-island-ultra/support.html)

**Root-domain app-ads.txt:** AdMob requests `https://<hostname>/app-ads.txt`; this GitHub Pages project URL is under `/dynamic-island-ultra/`, so a file stored only in this repository does not satisfy the hostname-root lookup. The repository also prepares Firebase Hosting to deploy the same site and seller file at a root-level `*.web.app` hostname. Configure GitHub Actions variable `FIREBASE_PROJECT_ID` and secret `FIREBASE_SERVICE_ACCOUNT`, then run **Deploy official website and app-ads.txt to Firebase Hosting**. See [Firebase app-ads.txt setup](docs/APP_ADS_TXT_FIREBASE_SETUP.md).

The seller entry in `site/app-ads.txt` is derived from the numeric publisher component of the supplied AdMob App ID. Compare it character-for-character with the personalized AdMob snippet before production; AdMob's snippet is authoritative. New AdMob apps created from January 2025 need app-ads.txt verification before full ad serving. This is an AdMob monetization verification gate, not a Google Play Console upload prerequisite. Do not claim AdMob verification until its console status confirms it.

Review these documents whenever permissions, SDKs, data processing, subscriptions, or ad placements change. Never send signing keys, passwords, OTPs, or tokens to support.

## Source and roadmap

- [Feature catalog](docs/FEATURE_CATALOG.md)
- [Feature roadmap](docs/FEATURE_ROADMAP.md)
- [Open-source research and license review](docs/OPEN_SOURCE_RESEARCH.md)
- [Network and update design](docs/NETWORK_AND_UPDATES.md)
- [Google Play Console & monetization checklist](docs/PLAY_CONSOLE_CHECKLIST.md)
- [Physical-device release smoke test](docs/DEVICE_SMOKE_TEST.md)
- [Firebase root-host/app-ads.txt setup](docs/APP_ADS_TXT_FIREBASE_SETUP.md)

## Validation status

The latest verified signed release is [v0.0.13](https://github.com/adebojel/dynamic-island-ultra/releases/tag/v0.0.13) (**versionCode 134**, published 22 November 2026). [Android CI run #134](https://github.com/adebojel/dynamic-island-ultra/actions/runs/45019191147) completed successfully. Both distribution-flavor test suites passed; signed GitHub APK and Play AAB builds passed; CI verified the installer-permission split, stable APK signature, AAB certificate match, SHA-256 generation, artifact upload and Release publication. Version v0.0.13 adds an optional 8-second visible auto-dim for the floating bubble; it remains touchable, restores on interaction, and pauses while the category panel is open.

Release assets:
- [Signed GitHub APK for direct Android installation](https://github.com/adebojel/dynamic-island-ultra/releases/download/v0.0.13/app-release.apk)
- [Signed Play AAB for Google Play Console](https://github.com/adebojel/dynamic-island-ultra/releases/download/v0.0.13/app-release.aab)
- [GitHub APK SHA-256 file](https://github.com/adebojel/dynamic-island-ultra/releases/download/v0.0.13/app-release.apk.sha256)
- [Play AAB SHA-256 file](https://github.com/adebojel/dynamic-island-ultra/releases/download/v0.0.13/app-release.aab.sha256)
- [Release metadata](https://github.com/adebojel/dynamic-island-ultra/releases/download/v0.0.13/release-info.json)

GitHub's release API reports APK digest `sha256:ae4f965d5a116c751b07e610ad65f91789b2b5dc307224896afd981e27c5df4c` (19,108,214 bytes) and AAB digest `sha256:9cbf78f6cbd419290575a364b720a1e27d225cbb8a0857fff1dd12ee24bb987e` (18,646,482 bytes). The signing certificate SHA-256 remains `7b2c2fe9ccc24320133e78402c7171d7a835e0d7b425f9119f8d8f1d871a8a0e`; CI verified the APK and AAB use the matching stable certificate.

**Do not upload the old v0.0.3 AAB.** It predates the distribution-flavor split and contains the self-update permission. Upload only the `v0.0.9 app-release.aab`, built from `bundlePlayRelease`, to Play Console. Its Play merged manifest was checked to exclude `REQUEST_INSTALL_PACKAGES`.

The signed artifacts use the stable release key. The certificate match confirms GitHub APK/AAB signing consistency, but does not decide which app-signing key Google Play uses for APKs served to users. Follow the Play App Signing guidance in [the Play Console checklist](docs/PLAY_CONSOLE_CHECKLIST.md).

**Hardening in v0.0.4–v0.0.8:** UMP privacy-options completion waits for the real SDK callback; adaptive banners respond to width changes; the foreground-service notification exposes a direct Stop action; Billing retains its client for automatic reconnection; v0.0.7 adds the assistive toolbox; v0.0.8 adds call-card allowlist enforcement. **v0.0.9** adds bubble gesture/appearance controls; **v0.0.10** adds Quick Favorites; **v0.0.11** adds accordion navigation; **v0.0.12** corrects adaptive icon and touch-cancellation lifecycle; **v0.0.13** adds optional visible auto-dim.



**Not yet verified:** installation and real-world operation on a physical Android device, update-over-existing-install behaviour, production AdMob ad fill, Play Console review, Play Billing purchase flows, and independent HTTP access to every deployed website page. GitHub Pages deployment of the latest policy updates succeeded in [Pages run #14](https://github.com/adebojel/dynamic-island-ultra/actions/runs/45013720877); public reachability and page contents still need to be checked from a normal browser/device. Firebase Hosting root deployment remains pending because `FIREBASE_PROJECT_ID` and `FIREBASE_SERVICE_ACCOUNT` are not configured; the root-host `app-ads.txt` URL is therefore not live. Do not claim AdMob app-ads.txt verification until AdMob Console confirms it.

CI tests and APK assembly do not replace physical-device testing. The next device gate is to smoke-test overlay lifecycle, notification access, DuckDuckGo search/navigation and media playback, package-installer handoff, data retention across a signed update, and Android 8/9/10 plus current Android OEM variants.

---

<div align="center">

**Offline AI core · Full network capability · User-controlled Android integration**

[Repository](https://github.com/adebojel/dynamic-island-ultra) · [Issues](https://github.com/adebojel/dynamic-island-ultra/issues)

</div>

## Floating bubble controls

The floating overlay now supports configurable local gesture routing:

- Tap (opens the bubble menu by default), double tap (offline assistant by default), and long press.
- Optional swipe up/down/left/right actions. Swipe gestures are disabled by default so dragging remains available until the user assigns an action.
- Local action choices: bubble menu, offline assistant, media controls, notification privacy settings, app-owned PiP workspace, or bubble settings.
- Four configurable Quick Favorites appear above the category list; choose from offline assistant, notes, calculator, installed apps, media, QR, document preview, sensors, workspace, and settings.
- Bubble categories now expand/collapse as accessible accordion sections; category items close the overlay panel before launching their destination.
- Bubble size (80–140%) and opacity (40–100%) are adjustable from Island Interaction settings. Changes refresh the active overlay without restarting the service.
- The gesture router uses a fixed allowlist. It does not execute arbitrary shell commands, change protected Android settings, or control third-party apps.

These controls are implemented in the existing overlay service and persist in app-private preferences. Physical drag, haptic, accessibility, and OEM behaviour still require device validation.

## Adaptive launcher icon

The launcher artwork now uses a transparent adaptive foreground with a neon cyan/violet floating-island waveform and a separate dark background color. Both round and standard Android adaptive icon definitions share the same background resource, allowing launchers to apply their own icon masks.

### Floating bubble auto-dim

The floating bubble can optionally dim after 8 seconds without interaction. The dimmed bubble remains visible and touchable; this is not a system overlay hide/removal or a guarantee that Android/OEM power management will keep the service alive. Touching the bubble restores its normal appearance, and an expanded category panel is never dimmed.

### Floating bubble inactivity dimming

Auto-dim is optional and disabled by default. When enabled, the bubble dims after eight seconds without touch but remains visible and interactive. Touch restores its configured appearance. An expanded category panel is never dimmed.


## Smart Toolbox additions

The floating bubble now includes a **Smart Toolbox** entry. It can also be assigned to a gesture or one of the four Quick Favorites. The toolbox currently contains:
- Local unit conversion for length, mass, volume and temperature.
- Increment/decrement counter.
- Stopwatch using Android's monotonic elapsed-realtime clock.
- Countdown timer with a 25-minute Pomodoro preset.

These tools do not require network access or additional Android permissions. Timer and stopwatch are in-app session tools; they are not system alarms and are not guaranteed to keep notifying after the app is force-stopped. Physical-device validation is listed in `docs/DEVICE_SMOKE_TEST.md`.


## File Manager — scoped local storage

The File Manager uses Android's Storage Access Framework. The user selects a folder through the system folder picker, and the app persists access only to that selected tree. It supports folder navigation, name search, folder-first sorting (name or newest modification), folder creation, file opening through compatible installed apps, sharing, rename and deletion with confirmation. It does not request `MANAGE_EXTERNAL_STORAGE` or silently scan the device. Provider capabilities can vary; a provider may reject an operation, and the app must not claim success when the provider refuses it. Copy/move, archive management and document editing are still separate backlog items.


### Browser library and downloads

The browser stores bookmarks and up to 100 recent HTTPS visits in app-private preferences. Downloads are handed to Android DownloadManager and stored in the app-specific external Downloads directory; no broad storage permission is requested. The browser does not bypass TLS errors or permit active-content schemes outside HTTP/HTTPS. Multiple independent tabs, private browsing sessions, and a full bookmark/download management editor remain planned work.


## Global Android navigation controls

The optional Accessibility Service provides explicit Home, Back, Recent Apps, Notifications, Quick Settings and Power Dialog actions, plus Lock Screen on Android 9/API 28 and newer. The user must enable it manually in Android Accessibility settings. Its configuration disables window-content retrieval and its event callback intentionally does not inspect or retain screen content. Actions are executed only after a direct tap in the Global System Controls screen or its mapped app-owned entry point. Android/OEM policy may still restrict an individual action. Screenshot, screen recording, global volume/brightness sliders and flashlight toggles are not part of this service yet.


### Custom accent editor

Island Interaction settings now include a local HSV accent editor (hue, saturation and brightness) with a live color preview and reset action. The saved accent is applied to the floating pill and dashboard on the next activity render. Preset Advanced RGB themes, pulse styles, size and compact-panel Premium options remain in Bonus & Premium.


### Global screenshot action

The optional Accessibility Service offers a user-triggered system screenshot action on Android 9/API 28 and newer. The app only calls the Android global action after the user taps the control; it does not retrieve, inspect or save screen contents itself.


## App-owned Floating Window Manager

The bubble panel can open up to four app-owned overlay mini windows: Notes, a four-operation calculator, and a countdown timer. Each window has a draggable title bar, minimize/restore, compact/large size toggle and close action. Notes are saved locally and included in encrypted backup. Mini timers are session-only and stop when their window closes or the foreground service stops. These are app-owned tools; they do not embed third-party apps or bypass Android freeform-window limitations.


### Hardware and display controls

Global System Controls also provides a media-stream volume slider, a flashlight toggle (camera permission is requested only when the user activates it), and system brightness/auto-rotation after the user grants Android's special Modify System Settings access. Camera hardware is optional and the app does not open a camera preview or capture images. No protected setting or runtime permission is silently enabled.
