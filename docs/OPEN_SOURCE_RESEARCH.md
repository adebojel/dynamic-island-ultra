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

## Offline-only policy

- No INTERNET or ACCESS_NETWORK_STATE permission.
- No remote model endpoint, API key, telemetry SDK, analytics, advertising, remote config or cloud sync.
- Notes and the rule-based utility assistant process data locally.
- Future generative models must be imported as local files and checked for license, quantization, RAM, CPU/GPU support and storage limits before use.

## Component intake checklist

1. Pin a reviewed upstream commit/release.
2. Verify exact license SPDX file and NOTICE obligations.
3. Inspect transitive dependencies for networking, trackers, broad permissions and unsafe behaviour.
4. Test target SDK and OEM compatibility.
5. Measure APK/model storage, peak RAM, latency, battery and accessibility.
6. Verify offline operation with network disabled.
7. Add unit/instrumentation tests and document module privacy.
