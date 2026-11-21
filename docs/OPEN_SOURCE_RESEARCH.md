# Open-Source and App Reference Research

This inventory records feature concepts and implementation candidates. It is not a claim that proprietary source code has been copied. Each component requires independent source, dependency, security, license, privacy and size review.

## Google Play references

| Reference | Feature areas researched |
|---|---|
| [Floating Apps free](https://play.google.com/store/apps/details?id=com.lwi.android.flapps) | Floating mini-app windows, notes, calculator, browser, PDF/document viewer, resize/minimize |
| [Floating Apps full](https://play.google.com/store/apps/details?id=com.lwi.android.flappsfull) | Expanded floating apps, widgets, file/media utilities and window manager |
| [Floating Assistant](https://play.google.com/store/apps/details?id=com.glgjing.floating.apps.assistive.touch) | Assistive touch, quick actions, favourite apps and system panels |
| [Floating Assistant Pro](https://play.google.com/store/apps/details?id=com.glgjing.floating.apps.assistive.touch.pro) | Floating clock/countdown, device monitor and assistive touch |
| [Overlays](https://play.google.com/store/apps/details?id=com.applay.overlay) | Floating widgets, sidebar, launcher and notification/volume concepts |
| [Mini Phone](https://play.google.com/store/apps/details?id=com.blackhole.miniphone) | Floating menu, compact app launcher and visual customization |
| [All Tools](https://play.google.com/store/apps/details?id=com.pradhyu.alltoolseveryutility) | QR/barcode, measurement, sensors, connectivity and productivity |
| [Smart Tools](https://play.google.com/store/apps/details?id=com.pcmehanik.smarttoolbox) | Compass, level, measurement, tuner, counters and notepad |
| [EasyTouch](https://play.google.com/store/apps/details?id=com.easytouch.assistivetouch) | Assistive touch and quick-access menu |
| [Dynamic Bar](https://play.google.com/store/apps/details?id=com.callcenter.dynamic.notch) | Dynamic notification island |
| [Dynamic Notch](https://play.google.com/store/apps/details?id=com.dynamicisland.notchscreenview) | Dynamic notch visuals and quick interaction |
| [Floating package reference](https://play.google.com/store/apps/details?id=com.ksxkq.floating) | Exact app identity and feature scope require separate verification |
| [Package reference](https://play.google.com/store/apps/details?id=com.media720.games2020) | Supplied package was not established as a floating utility in initial inspection |

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

- The app declares INTERNET and ACCESS_NETWORK_STATE so network-capable modules can function and future AdMob integration can be added.
- DuckDuckGo is the default browser/search engine; it supports general HTTPS browsing and Desktop/Mobile website modes.
- GitHub Update Center retrieves versioned APK releases and validates SHA-256 before opening Android's package installer.
- The built-in decision assistant, notes, calculator and diagnostics remain local-only. They do not invoke online models, send prompts, or transmit local diagnostic results.
- AdMob is not installed yet: a real App ID, ad-unit IDs, Google Mobile Ads SDK, UMP consent flow and privacy disclosures are needed before serving advertisements.
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

### AdMob preparation
- INTERNET and ACCESS_NETWORK_STATE are in the manifest. The SDK is not yet included because the publisher App ID and ad-unit IDs have not been supplied.
- Google's current AdMob guidance uses Google Mobile Ads SDK plus the UMP privacy SDK. Consent information must be refreshed at app launch, applicable forms presented, and `canRequestAds()` checked before requesting ad inventory.
- Ad requests must use test IDs/devices during development and avoid covering key controls or interfering with accessibility.

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
- Compose BOM stable coordinate (2026.09.00): https://developer.android.com/develop/ui/compose/bom
- AdMob Android quick start: https://developers.google.com/admob/android/quick-start
- UMP Android consent flow: https://developers.google.com/admob/android/privacy
- LiteRT-LM Android implementation: https://github.com/google-ai-edge/LiteRT-LM
