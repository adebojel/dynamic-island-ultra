
## Google Play and store references

The following matrix assesses only features described by discoverable product listings. This is a feature/design comparison, not an endorsement or a claim that proprietary code/assets may be copied.

| Supplied reference | Feature scope verified in listing | UX patterns worth learning | Dynamic Island Ultra gap / integration decision |
|---|---|---|---|
| [Floating Apps (multitasking), com.lwi.android.flapps](https://play.google.com/store/apps/details?id=com.lwi.android.flapps) | Floating browser, notes, calculator, document/PDF viewer, URL/widget-to-mini-app flow; floating menu and quick launch; normal/recent apps and shortcuts; resize, minimize, maximize, borders and transparency. | Persistent movable launcher, window title drag, bottom-bar resize, context controls and restore/minimize workflow. | Current app has a single bubble panel and app-owned PiP workspace, not general mini-windows. Build app-owned mini-tools one by one; do not claim arbitrary apps can float. |
| [Floating Apps full, com.lwi.android.flappsfull](https://play.google.com/store/apps/details?id=com.lwi.android.flappsfull) | Paid SKU for the same Floating Apps suite; listing describes 41+ mini apps including browser, document viewer, YouTube, contacts, dialer, file manager, media player, recorder, translator and drawing. | Consistent mini-app catalogue, quick launch, floating menu, resize/minimize/maximize and multi-window management. | Proprietary app: use only as a feature/UX benchmark. No code, icons, brand assets or proprietary app content copied. |
| [Floating Assistant, com.glgjing.floating.apps.assistive.touch.pro](https://play.google.com/store/apps/details?id=com.glgjing.floating.apps.assistive.touch.pro) | Assistive-touch quick access, virtual Home/Back/Recent/Volume, lock/screenshot/flashlight/rotation/brightness actions, floating clock/countdown and CPU/RAM/battery monitoring. Its listing says Accessibility is used for certain system actions. | Separate compact overlays for clock and device monitor; configurable quick actions; explicit setup for overlay and Accessibility access. | Only implement actions supported by public Android APIs or explicitly consented Accessibility. Current app has no Accessibility Service and does not emulate global Home/Back/lock/screenshot actions. |
| [Overlays - Floating Launcher, com.applay.overlay](https://play.google.com/store/apps/details?id=com.applay.overlay) | Floating widgets, shortcuts, browser, launcher, notification history, media/volume controls, sidebar and multiple utility overlays; per-orientation sizing, transparency, movement and triggers. The listing explicitly says it does not support real third-party freeform app windows. | Window manager patterns, multiple overlay instances, opacity/size controls, sidebar, and conditional triggers. | Suitable reference for app-owned overlays, not a promise of arbitrary third-party app floating windows. Automation triggers require deliberate, user-visible Android-compliant implementation. |
| [Mini Phone - Floating Apps, com.blackhole.miniphone](https://play.google.com/store/apps/details?id=com.blackhole.miniphone) | Floating windows and compact launcher for internet browsing, online video and quick search while another app is in use; themes, transparency, size and position. | Quick-search entry point and sidebar customization. | Listing is focused on online multitasking, not a complete offline toolbox. Its Data Safety section declares possible sharing/collection categories; do not copy its data model. |
| [Floating Menu / Floating Ball, com.ksxkq.floating](https://play.google.com/store/apps/details?id=com.ksxkq.floating) | Package is confirmed by the [Xiaomi App Store listing](https://app.mi.com/details?id=com.ksxkq.floating): gesture-based floating menu, one-handed controls, message notices, quick actions and mini-window sizing. | Gesture combinations, reversible gestures, configurable menu and one-hand access. | The supplied Google Play listing could not be independently retrieved in this audit, so the Xiaomi listing is the verified source. Its store permission list includes broad device/network identifiers and network controls; those permissions are not imported. |
| [All Tools: 70+ Utilities, com.pradhyu.alltoolseveryutility](https://play.google.com/store/apps/details?id=com.pradhyu.alltoolseveryutility) | Broad offline-first utility collection; listing mentions local file transfer, synchronized group music, Wi-Fi walkie-talkie and measuring/sensor utilities. | Tool grouping and separate shortcuts for frequently used utilities. | Current project already has QR scanning and a basic sensor kit. Add each tool only after sensor availability, calibration, permission, privacy and offline behavior are tested. |
| [Smart Tools - All In One, com.pcmehanik.smarttoolbox](https://play.google.com/store/apps/details?id=com.pcmehanik.smarttoolbox) | 40+ ruler/level/protractor/magnifier, sound-level, altimeter, distance, magnetometer, vibration, light, compass, battery, network, QR/text/NFC, accelerometer, metronome, tuner and other tools. | Sensor-dependent tool groups, per-tool shortcuts and calibration guidance. | Do not fabricate unavailable sensors or present approximate readings as calibrated measurements. Gate every tool on actual hardware support and show calibration/accuracy limits. |
| [Package com.media720.games2020](https://play.google.com/store/apps/details?id=com.media720.games2020) | The current store page resolves to an offline puzzle-games collection, not a floating-app/utility tool. | None for the Dynamic Island floating hub. | Excluded from product benchmarking; likely supplied package mismatch. |
| [Assistive Touch for Android, com.easytouch.assistivetouch](https://play.google.com/store/apps/details?id=com.easytouch.assistivetouch) | Virtual Home/Back/Recent and Volume controls, favourites, settings and quick toggles, screenshot/flashlight/rotation, screen recorder and single/double/long-press actions; listing describes Accessibility use for selected global actions. | Simple quick menu, familiar virtual-button metaphors and gesture settings. | Current release only maps gestures to app-owned screens. Any future global action needs a disclosed Accessibility Service, explicit user opt-in, policy review and physical-device testing. |

## Open-source design candidates

| Project | License / verified scope | Assessment |
|---|---|---|
| [Halo](https://github.com/yogeshpaliyal/Halo) | MIT in repository; Kotlin/Compose edge bubble, long-press radial favourites/recents, slide-to-select, tap/double-tap actions, drag/snap, haptics, size/opacity, folders and boot restoration. | Strong reference for an edge gesture state machine and radial favourites menu. No code was copied in v0.0.9. Recheck exact commit, app permissions, minSdk and NOTICE before copying or integrating. |
| [AssistiveDotApp](https://github.com/abdallahnatsheh/AssistiveDotApp) | Apache-2.0 in repository; tap/double-tap/long-press/drag, edge snap, custom size/colour and Accessibility-backed global actions; README says Android 14+. | Helpful accessibility and permission-disclosure reference, but its Android version floor is not a direct API-26 implementation target. No code was copied. |
| [Islet](https://github.com/dylanneve1/islet) | Apache-2.0 license file previously verified; island, notification and media ideas. | Design reference only pending exact-commit and dependency review. |
| [SmartIsland](https://github.com/agupta07505/SmartIsland) | GPL-3.0 license file previously verified. | Copyleft obligations apply if derived code is integrated; avoid copying unless the project accepts GPL compliance. |
| [Aura Island](https://github.com/Sensui-moksha/Aura-Island) | GPL-3.0 license file previously verified. | Copyleft obligations apply if derived code is integrated; design reference only for now. |
| [Overlay PowerToys](https://github.com/8bitgentleman/OverlayPowerToys) | Exact license at selected commit remains unverified. | Do not copy code until license and dependency review complete. |
| [Sparrow](https://github.com/ilhan-gunduz/Sparrow) | License and Android applicability require fresh verification. | Research lead only. |
| [PocketLLM](https://github.com/CaviraOSS/PocketLLM) | Software and individual model terms need separate verification. | Research lead only; not a runtime integrated in this release. |
| [LiteRT-LM](https://github.com/google-ai-edge/LiteRT-LM) | Google on-device LLM runtime; model format and model license are separate. | Evaluate separately from GGUF runtimes; no inference runtime added in v0.0.9. |

## Lightweight local LLM feasibility

The 400 MB limit is for the app plus models/data managed by the project, not just the download APK. The current v0.0.9 APK is about 19.1 MB (decimal).

| Candidate | Quantized model file reported by repository | License / language caution | Decision |
|---|---:|---|---|
| [SmolLM2 360M Instruct Q4_K_M GGUF](https://huggingface.co/tensorblock/SmolLM2-360M-Instruct-GGUF) | About 0.271 GB (271 MB). | Base model card is Apache-2.0 but says the model primarily understands/generates English. The quantized GGUF repository's packaging and exact upstream revision must also be checked. | Best small-size benchmark candidate only; not accepted for Bahasa Indonesia without quality tests. |
| [Qwen2.5 0.5B Instruct Q4_K_M GGUF](https://huggingface.co/tensorblock/Qwen2.5-0.5B-Instruct-GGUF) | About 0.398 GB (398 MB). | The file alone nearly consumes the full 400 MB budget before the APK, runtime, tokenizer, cache or update headroom. Confirm the exact base-model and quantization terms before redistribution. | Reject for bundled/default configuration under the current combined storage cap. |

For GGUF experiments, evaluate one Android runtime at a time; llama.cpp is the corresponding candidate for GGUF. Build/benchmark it on real API-26+ devices before adding native binaries. LiteRT-LM uses a different model format and must not be added alongside llama.cpp without measured need. No generative model is bundled or downloaded by this release; all existing assistant actions remain local and deterministic.

## Android platform constraints

- [TYPE_APPLICATION_OVERLAY](https://developer.android.com/reference/android/view/WindowManager.LayoutParams#TYPE_APPLICATION_OVERLAY) requires user-granted SYSTEM_ALERT_WINDOW; Android may change an overlay's position, size or visibility.
- [Overlay permission](https://developer.android.com/reference/android/Manifest.permission#SYSTEM_ALERT_WINDOW) must be granted by the user through Settings for modern target versions.
- [Foreground-service changes](https://developer.android.com/develop/background-work/services/fgs/changes): Android 15 narrows background FGS start exceptions for overlay apps; a visible overlay is required for that exception when targeting Android 15+.
- [NotificationListenerService](https://developer.android.com/reference/android/service/notification/NotificationListenerService) requires the declared protected binding permission and user approval in system settings.
- [Picture-in-Picture](https://developer.android.com/develop/ui/views/picture-in-picture) only applies to activities that explicitly support PiP. It does not permit this app to force arbitrary third-party apps into windows.

## GitHub candidates and concepts

| Repository | Research use | License notes |
|---|---|---|
| [Islet](https://github.com/dylanneve1/islet) | Dynamic Island, notification, media and cutout design | Apache-2.0 license file verified; review selected commit/dependencies |
| [SmartIsland](https://github.com/agupta07505/SmartIsland) | Battery, timers, media, call and notification concepts | GPL-3.0 file verified; derivative use needs copyleft compliance |
| [Aura Island](https://github.com/Sensui-moksha/Aura-Island) | Overlay, gesture and glass/animation patterns | GPL-3.0 file verified; review obligations before integration |
| [Overlay PowerToys](https://github.com/8bitgentleman/OverlayPowerToys) | Overlay mini-tools, notes, calculator and drawing concepts | Verify license at exact commit before copying |
| [Sparrow](https://github.com/ilhan-gunduz/Sparrow) | Offline assistant/launcher design ideas | Verify license and Android applicability |
| [PocketLLM](https://github.com/CaviraOSS/PocketLLM) | Local LLM size, quantization and runtime exploration | Verify software and model terms before adoption |
| [LiteRT-LM](https://github.com/google-ai-edge/LiteRT-LM) | Google on-device LLM runtime for Android | Preserve upstream license/NOTICE; model terms are separate |
| [Droidtools](https://github.com/Velocity081/droidtools) | Dynamic island and edge-bar concepts | Repository describes MIT; root license was not independently verified |
| [Droidbubble](https://github.com/Shikateki/Dynamic-island) | Media, recording and progress cards | License status unverified |
| [IslandOne](https://github.com/richyrach/IslandOne) | Media, notification and battery concepts | License status unverified |
| [DigiIsland](https://github.com/JensonVivish/dynamicisland) | Media/call/charging concepts | License status unverified |

## Android platform constraints

- [Android Picture-in-Picture](https://developer.android.com/develop/ui/views/picture-in-picture): Android-managed PiP applies to activities that declare/support PiP. An app cannot force arbitrary third-party apps into PiP or create unlimited PiP windows.
- [Package visibility](https://developer.android.com/training/package-visibility): a narrow launcher intent query avoids unrestricted QUERY_ALL_PACKAGES.
- [Foreground services](https://developer.android.com/develop/background-work/services/foreground-services): overlay and foreground service lifecycle remain subject to Android background-start and user-visible requirements.
- [LiteRT-LM](https://github.com/google-ai-edge/LiteRT-LM): local inference runtime. Model files remain separate and require their own storage/RAM/license checks.

## Network-enabled application policy

- The app declares INTERNET and ACCESS_NETWORK_STATE for browser, integrated AdMob/UMP and Google Play Billing. The built-in assistant stays offline by architecture and CI policy.
- DuckDuckGo is the default browser/search engine; it supports general HTTPS browsing and Desktop/Mobile website modes.
- GitHub Update Center retrieves versioned APK releases and validates SHA-256 before opening Android's package installer.
- The built-in decision assistant, notes, calculator and diagnostics remain local-only. They do not invoke online models, send prompts, or transmit local diagnostic results.
- Google Mobile Ads Next-Gen 1.7.0, publisher IDs/ad units and UMP 4.0.0 are integrated. Debug uses Google's official test units; signed release uses publisher IDs. Production fill, account review and app-ads.txt verification remain external gates.
- Future generative on-device models must be assessed for model license, quantization, RAM, CPU/GPU support, Android 8+ compatibility and APK/storage budget.

## Component intake checklist

1. Pin a reviewed upstream commit/release.
2. Verify exact license SPDX file and NOTICE obligations.
3. Inspect transitive dependencies for networking, trackers, broad permissions and unsafe behaviour.
4. Test target SDK and OEM compatibility.
5. Measure APK/model storage, peak RAM, latency, battery and accessibility.
6. Verify offline operation with network disabled.
7. Add unit/instrumentation tests and document module privacy.


## November 2026 network, browser, update and embedded-assistant review

### Browser and network permissions
- Android's INTERNET and ACCESS_NETWORK_STATE are normal install-time permissions that permit app networking. They apply to the package as a whole, not to an individual Kotlin class.
- DuckDuckGo is used as the default home/search engine. The in-app WebView is a general HTTPS browser with Back/Forward/Reload/Home and desktop/mobile modes controlled by User-Agent and viewport settings.
- Cleartext HTTP remains disabled, file/content URL access is disabled, mixed content is blocked, Safe Browsing is enabled on supported API levels, and TLS certificate errors are cancelled.
- CI retains a source-level network isolation gate for the `ai/` package so built-in AI processing stays local even though other app modules can use the internet.

### AdMob integration and remaining publisher gates
- Google Mobile Ads Next-Gen 1.7.0, UMP 4.0.0, publisher App ID and ad units are integrated.
- Debug variants use Google's official test units; signed release variants use publisher production IDs.
- UMP consent refresh/forms and `canRequestAds()` request gating are implemented.
- Production fill, AdMob review/eligibility, published privacy messages and root-domain app-ads.txt remain external gates.

### In-place update requirements
Android checks package identity, signing identity and version code before accepting an update. The release keystore must remain stable and private, and each build must increase versionCode. This project uses GitHub Actions run numbers to create `v0.0.N` versions. Without the stable signing secrets CI generates a debug preview only and does not publish an incompatible APK as an update release.

### Embedded AI reality check
- The current in-APK assistant is a local deterministic decision engine with arithmetic, action routing and diagnostics. It has no model file, imported model, external inference endpoint or API key.
- A true generative offline LLM needs model weights plus a compatible inference runtime. Google's LiteRT-LM Android integration requires local model assets and broad hardware/RAM evaluation; model sizes can be hundreds of MB to multiple GB. Do not claim the current local decision engine is a generative chatbot or autonomous OS repair agent.

### Verified primary references
- Android network permission and secure transport: https://developer.android.com/develop/connectivity/network-ops/connecting
- Android WebView User-Agent/viewport: https://developer.android.com/reference/android/webkit/WebSettings
- Android WebView Safe Browsing: https://developer.android.com/develop/ui/views/layout/webapps/managing-webview
- WebView SSL-error handling: https://developer.android.com/reference/android/webkit/WebViewClient
- Android app update signing/version rules: https://developer.android.com/google/play/app-updates
- Android unknown-source installer permission: https://developer.android.com/reference/android/content/pm/PackageManager#canRequestPackageInstalls()
- Compose BOM stable coordinate (2026.06.00; Compose 1.11.3 compatible with compile SDK 36): https://developer.android.com/develop/ui/compose/bom
- AdMob Android quick start: https://developers.google.com/admob/android/quick-start
- UMP Android consent flow: https://developers.google.com/admob/android/privacy
- LiteRT-LM Android implementation: https://github.com/google-ai-edge/LiteRT-LM
