# Dynamic Island Ultra — Feature Roadmap

## Current source baseline

- RGB/glass dashboard, draggable island, pulse, saved position and battery indicator.
- Notification preview bridge with privacy controls and app allowlisting; MediaSession playback controls; generic call activity card derived from notifications.
- Floating toolbox additions: QR/barcode scanner, image/PDF preview and foreground-only sensor kit.
- On-device voice command input when a local recognizer is exposed, speech read-out, edge docking and haptics.
- Installed-app collection, private local notes, calculator, app-owned PiP workspace and Smart Toolbox (unit converter, counter, stopwatch and countdown/Pomodoro timer).
- Built-in offline command decision engine with arithmetic, feature routing and read-only diagnostics.
- DuckDuckGo default browser with general HTTPS navigation and Desktop/Mobile mode.
- Distribution split: `github` APK includes SHA-256-verified GitHub self-updates; `play` AAB excludes `REQUEST_INSTALL_PACKAGES` and routes updates through Google Play.
- Android 8+ minimum (API 26), cleartext traffic disabled, AI source package remains offline by CI policy.
- GitHub Actions uses the latest successful `v0.0.N` tag to increment the next patch release, while the Android `versionCode` uses the monotonically increasing workflow run number.
- Stable signing key is read from private GitHub Actions secrets; no key material belongs in source control.
- INTERNET and ACCESS_NETWORK_STATE permissions are present for application networking, AdMob/UMP consent and Google Play Billing.

## Latest release verification

- Version v0.0.11 adds accessible accordion navigation for the eight bubble categories and closes the floating panel before a category action launches.

- Latest signed release: [v0.0.13](https://github.com/adebojel/dynamic-island-ultra/releases/tag/v0.0.13), Android versionCode 134.
- [Android CI run #134](https://github.com/adebojel/dynamic-island-ultra/actions/runs/45019191147) passed both flavor unit-test suites, signed APK/AAB assembly, installer permission separation, stable certificate verification, checksum generation, artifact upload and release publication.
- Release asset metadata reports a signed APK of 19,108,214 bytes and Play AAB of 18,646,482 bytes; each has a SHA-256 sidecar.
- Version v0.0.10 adds four configurable Quick Favorites at the top of the floating bubble, with ten allowlisted app-owned shortcuts and persistent local preferences.
- Call-status notifications follow the same global privacy switch and per-app allowlist as other notification previews.
- Physical-device/OEM validation is still outstanding.

## Next engineering modules

1. Physical-device matrix: Android 8/9/10, API 26–29, modern API 33–36 and OEM background restrictions.
2. Browser smoke tests on WebView variants: DDG search, HTTPS sites, desktop/mobile User-Agent, cookies, video playback and lifecycle.
3. Signed update regression: same application ID/signature, versionCode monotonicity, installer consent and app-data retention.
4. AdMob production readiness: publish UMP privacy messages, complete app review/eligibility, publish root-host app-ads.txt, verify production ad serving without self-clicking, and audit placement/frequency compliance.
5. Full app-owned floating mini-window manager, radial/favourites menu, custom shortcut editor and complete theme editor.
6. Broader TalkBack, dynamic text scaling, high-contrast and keyboard-navigation validation.
7. Optional generative on-device LLM remains a separate module requiring model licence, runtime, RAM/storage and Android 8+ hardware evaluation.

## AI scope

The bundled offline decision engine does not import model weights and has no network access. It remains clearly distinguished from a generative LLM. A future on-device generative model needs license, runtime, model-size, RAM, CPU/GPU and Android 8+ compatibility review; no such capability will be advertised until genuinely implemented and measured.

## Current monetization state

- AdMob/UMP SDK and publisher IDs are integrated; debug uses Google's official test units and signed release uses production units. Production fill and account eligibility are not yet independently verified.
- Premium cosmetic themes, pulse speed, island size and compact layout are implemented. Paid subscription offers require the Play Console product/base plans to exist and become active.
- GitHub Pages deployment succeeded in [Pages workflow run #14](https://github.com/adebojel/dynamic-island-ultra/actions/runs/45013720877); verify public reachability before submitting URLs. Firebase root-domain app-ads.txt still requires the publisher's Firebase project and service-account configuration. AdMob app-ads.txt requires a file at the developer hostname root, not only the project Pages subpath.
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


## Combined backlog status

Implemented in the current source: local notification privacy settings and app allowlisting; generic call status from Android call-category notifications; media metadata and transport controls for supported MediaSessions; QR/barcode scanner; local image/PDF preview; foreground-only sensor readings; optional Android 12+ on-device speech input and device speech output; island edge docking and opt-in haptics.

Not marked as complete: real-device/OEM testing, TalkBack user testing, Play Console foreground-service review, AdMob production fill and publisher review, Firebase root-host app-ads.txt confirmation, Play Billing product/base-plan activation and backend purchase-token verification.

A generative offline LLM is not the existing deterministic command router. It remains a separate module requiring model licensing, runtime integration, device RAM/storage/thermal testing and API 26+ compatibility evaluation. Ordinary apps cannot silently switch protected Wi-Fi/mobile-data settings or take privileged control over other apps; those actions must route through Android settings and user confirmation.

## Implemented in the current bubble-controls update

- Added configurable tap, double-tap, long-press and four-direction swipe mapping.
- Added allowlisted routing to the app's own assistant, media, notification privacy, workspace and settings screens.
- Added bubble size and opacity controls with live appearance refresh.
- Added unit tests for safe gesture defaults, invalid stored actions, and swipe/drag coexistence.

Still outstanding: long-running OEM/device validation, TalkBack interaction testing on physical devices, auto-hide/restore behaviour, multi-window positioning tests, and the full custom shortcut editor.

## External product and platform research snapshot — 22 November 2026

- Ten supplied product/package references were checked against their discoverable store pages. The feature comparison and suitability notes are in [Open-source research and app reference matrix](OPEN_SOURCE_RESEARCH.md).
- The supplied com.media720.games2020 reference resolves to an offline puzzle-games listing, not a floating utility; exclude it from feature benchmarking.
- com.ksxkq.floating is confirmed by the Xiaomi App Store package listing. The corresponding Google Play listing could not be independently verified in this review.
- Floating Apps is the strongest supplied multi-window reference, but its proprietary implementation is not copied. Overlays explicitly disclaims arbitrary third-party freeform windows; Dynamic Island Ultra will limit floating windows to its own activities/tools and Android-supported surfaces.
- Open-source gesture/edge-menu design references include Halo (MIT) and AssistiveDotApp (Apache-2.0). Neither source was copied into this release; license notices and API compatibility must be rechecked at the exact commit before any code intake.

## Local model feasibility snapshot — 22 November 2026

- Current assistant remains an offline deterministic intent/rule engine, not a generative LLM.
- SmolLM2 360M Instruct Q4_K_M GGUF is listed at about 271 MB and Apache-2.0, but its model card says it primarily understands and generates English. It is therefore not accepted as the Indonesian assistant model without language-quality evaluation.
- Qwen2.5 0.5B Instruct Q4_K_M GGUF is listed at about 398 MB, leaving essentially no room under the combined 400 MB package/model budget for the existing app, runtime, tokenizer, cache or updates.
- For future GGUF experimentation, evaluate one runtime only (llama.cpp Android/NDK) against the actual target device, using an optional, user-managed model file. Do not add it to release builds until storage, peak RAM, CPU latency, temperature, battery, Indonesian command tests and offline isolation all pass.
- LiteRT-LM is a separate runtime/model format; do not combine both engines without a measured need. Its integration guidance describes local .litertlm model files from hundreds of MB to several GB; evaluate it separately, not as an additional dependency in the current release.

## Quick Favorites update

- Added four configurable shortcut slots to the top of the floating bubble panel.
- Added an allowlisted catalog of ten app-owned destinations, including offline notes/calculator, assistant, media, QR, document preview and sensor kit.
- Added persistent slot configuration and unit tests for defaults, invalid keys and accessible labels.
- The favorites row is not a third-party freeform window manager or privileged system action surface.

## Bubble category UX update

- Converted the eight category groups into expandable/collapsible accordion sections; sections start closed while the Quick Favorites row stays visible.
- Added full-width focusable category headers with explicit accessibility descriptions and state text.
- Category actions close the floating panel before launching their destination, preventing a stale overlay from covering the opened screen.
- Physical TalkBack, focus, rotation, and OEM overlay tests remain in the device checklist.

## Launcher identity and gesture cancellation

- Rebuilt the launcher mark as a transparent adaptive foreground over a separate launcher background.
- Fixed long-press feedback to obey the user haptic toggle.
- Added cancellation cleanup for pending tap/long-press callbacks and restored the pre-drag coordinates on ACTION_CANCEL.
- Release CI will verify resource compilation and both distribution variants.


## v0.0.12 adaptive icon and gesture-lifecycle update

- The launcher foreground is now transparent adaptive artwork with a separate dark background color resource shared by regular and round launcher icons.
- Long-press haptics obey the saved haptic toggle.
- ACTION_CANCEL removes pending tap/long-press callbacks and restores the pre-drag coordinates.
- Android CI run #133 passed both distribution test suites, signed builds, manifest separation, certificate validation, checksum generation and release publication.
- Physical launcher-mask, drag-cancellation and haptic tests remain in docs/DEVICE_SMOKE_TEST.md.

## Floating bubble auto-dim update

- Added an opt-in eight-second inactivity dim control; default remains off.
- Dimmed alpha remains non-zero and touchable. Touch restores the selected size, opacity and pulse style.
- Expanded menu panels suspend dimming; closing a panel starts a fresh timer.
- Added unit tests for disabled state, expanded state, detached bubble, delay and visible alpha floor.
- Physical visibility, reachability and OLED/readability checks remain on the device checklist.

## v0.0.13 optional floating-bubble auto-dim

- Eight-second idle timer, opt-in and disabled by default.
- Dimmed bubble remains visible and touchable; interaction restores the selected opacity and pulse style.
- Open category panel pauses dimming; closing the panel restarts the idle interval.
- Unit tests cover disabled state, menu exclusion, detached overlay, timeout and minimum visible alpha.
- Physical OEM, sunlight readability and reachability checks remain outstanding.


## Smart Toolbox delivery

The main-line implementation exposes Smart Toolbox directly through the floating bubble and configurable gesture/favorite allowlists. Implemented locally: length/mass/volume/temperature conversions, counter, stopwatch and countdown timer/Pomodoro preset. Unit conversion has deterministic unit tests. It intentionally does not claim calibrated ruler/protractor or sound/light meter readings: those need device-specific calibration and real hardware validation.

Still requiring separate implementation and/or device tests: persistent alarms/reminders after process death, multi-window third-party app overlays, clipboard history, encrypted backup/restore, bookmarks/history/private browser sessions, broad OCR/object recognition and a generative local LLM. These are not represented as completed features.


## Phase 1 delivery update — scoped File Manager

Implemented in source on `main`:
- SAF folder picker with persisted access to the user-selected document tree.
- Folder navigation, filename search, folder-first sorting by name or modification time.
- Create folder, open/share file, rename item and delete with confirmation.
- Dashboard and Floating Bubble entry points.
- Filename validation unit tests; no broad storage permission added.

Still not complete for the wider Phase 1 scope: file copy/move, ZIP tools, full document editing, browser tabs/bookmarks/history/download manager, app-owned multi-window manager, global Accessibility Service controls, complete theme editor, and physical-device/OEM validation. These must not be marked done based solely on this module's CI result.


## Phase 1 browser enhancement

Implemented:
- Local HTTPS bookmark persistence.
- Capped 100-entry HTTPS browsing history.
- HTTPS downloads delegated to Android DownloadManager and scoped to app-specific external Downloads storage.
- Smoke-test checklist for persistence, download hand-off and absence of broad storage permission.

Still outstanding: independent multi-tab WebView lifecycle, private browsing mode with isolated storage/cookie policy, bookmark CRUD/search, download history controls, and physical WebView/device compatibility tests.


## Phase 1 system navigation controls

Implemented in source:
- Android Accessibility Service for explicitly tapped global navigation actions.
- Home, Back, Recents, Notification Shade, Quick Settings and Power Dialog; Lock Screen on API 28+.
- Manual enablement through Android Accessibility settings, with no window-content retrieval and no screen-content processing in the event callback.
- Dashboard and Floating Bubble entry points.

Still outstanding: screenshot and screen-recording workflows, flashlight/rotation/brightness/volume control surfaces, full accessibility user experience, TalkBack usability review and OEM validation. Protected settings must continue to use Android's own user-controlled settings surfaces.


## Phase 1 Smart Toolbox sensor utility

Implemented:
- Two-axis spirit level (pitch and roll) from the actual accelerometer.
- User-settable local zero reference and reset calibration.
- Listener is registered while Smart Toolbox is resumed and removed on pause.
- Unit tests cover flat sample, a known 45-degree pitch sample and invalid sensor samples.

Still outstanding: ruler/protractor hardware and display calibration, distance measurement, altimeter accuracy, sound/light meters with permission and hardware review, compass calibration, persistent reminders, multi-timer, stopwatch lap and clipboard tools. Physical-device sensor validation remains required.


## Stopwatch lap recording

Implemented in Smart Toolbox:
- Records lap split durations while the monotonic stopwatch is running.
- Lap records are shown newest-first and cleared only by Reset.
- Lap capture is disabled while paused; no wall-clock timer is used for elapsed measurement.
- The feature is session-local and is not persisted after closing the screen.
