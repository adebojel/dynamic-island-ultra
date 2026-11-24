# Reference app research and implementation boundaries

Research snapshot: 24 November 2026. This document maps public store descriptions and the user-supplied screenshots to a lawful, Android-native implementation plan. It does not copy proprietary source code, assets, trademarks, or private APIs.

## Dynamic Island reference patterns

| Reference pattern | Native implementation route | Current repository status |
|---|---|---|
| Compact pill, tap/hold expansion, gestures | Application overlay service and gesture configuration | Implemented in FloatingIslandService and BubbleGestureConfig |
| Notification and quick reply | NotificationListenerService and RemoteInput actions exposed by the originating notification | Notification previews exist; inline reply is limited to actions explicitly supplied by the source notification |
| Music metadata, artwork, seek, previous/play/pause/next | Android MediaSession via notification access | Implemented in ActiveMediaSession and MediaControlsActivity |
| Battery and charging | ACTION_BATTERY_CHANGED | Bubble battery status implemented |
| Bluetooth device state | Android Bluetooth APIs and runtime Nearby Devices consent where required | Settings shortcut exists; live connected-device identity/battery is not yet verified as implemented |
| GPS turn-by-turn cards | Supported navigation app notifications / public navigation intents | Generic notification preview exists; structured turn-by-turn extraction is not claimed |
| Call alerts and controls | Telecom/notification APIs and platform call action intents | Call activity preview is partial; answer/decline capability depends on supported system action and permissions |
| Timers, downloads, screen recording and other progress | Notifications and app-owned foreground service state | Notification preview and app-owned screen recorder exist; per-source progress parsing needs more coverage |
| Shape, position, opacity, RGB themes and animations | Overlay WindowManager plus saved settings | Position, gestures, accent, pulse and opacity controls exist; complete theme/preset editor remains future work |

Public examples used for feature research:
- [dynamicSpot](https://play.google.com/store/apps/details?id=com.jamworks.dynamicspot)
- [Dynamic Island Notch Screen View](https://play.google.com/store/apps/details?id=com.dynamicisland.notchscreenview)
- [Dynamic Notch](https://play.google.com/store/apps/details?id=com.callcenter.dynamic.notch)
- [TheNotch feature pattern reference](https://play.google.com/store/apps/details?id=com.dattatech.thenotch)

## Floating multitasking and Assistive Touch patterns

Public Floating Apps descriptions advertise mini browser, notes, calculator, document/PDF viewer, contacts/dialer, file manager, media player, image viewer, translation, maps, and launcher shortcuts. Assistive Touch examples advertise configurable favorite apps, Home/Back/Recents, volume, flashlight, brightness, rotation, screenshots, and settings shortcuts. Overlay-style launchers add sidebar access, minimized bubbles and user-created URL/widget shortcuts.

Current implementation already includes:
- One overlay Assistive Bubble and categorized menu with favorite slots and gesture mapping.
- App-owned mini Notes, Calculator and Timer windows; drag, minimize, resize and close.
- Installed app collection, now with a Live App Dock in the floating hub.
- Best-effort Android native freeform launch bounds from Installed Apps. Android/OEM support determines whether a third-party app actually appears as a freeform window; this feature does not embed or repackage the other app.
- Local browser, notes, calculator, file manager, QR scanner, sensor kit, document preview, screen recorder, global navigation, clipboard, reminders and encrypted backup.

Still not represented as completed:
- Floating browser/PDF/media player as independent app-owned overlay windows.
- Floating contacts/dialer, translator, map and custom widget/URL mini-app framework.
- Reliable recent-app usage ranking and per-app hide/show profiles.
- Per-window transparency, z-order controls, multiple independent note documents and persistent mini-window restore.
- A full 3x3 multi-page assistive-touch action editor.

## Utility catalog research

The public All Tools and Smart Tools descriptions include file transfer, local Wi-Fi tools, QR/barcode, OCR, magnifier, color detector, ruler, protractor, compass, level, speedometer, altitude, pressure, metal/magnetic-field detector, vibration, sound/light meters, stopwatch, counter, converters, pedometer, and other hardware-dependent tools.

Current local toolbox coverage includes unit conversion, counter, stopwatch/laps, timer/Pomodoro, spirit level, sensor kit, QR/barcode scan, flashlight, screen recording and device controls. Hardware-dependent measurements must be calibrated and tested on the target device before being advertised as accurate. Medical-like readings are excluded from medical decision-making and must carry clear non-medical disclaimers.

Public sources:
- [Floating Apps (multitasking)](https://play.google.com/store/apps/details?id=com.lwi.android.flapps)
- [Floating Apps full version](https://play.google.com/store/apps/details?id=com.lwi.android.flappsfull)
- [Floating Assistant / Assistive Touch](https://play.google.com/store/apps/details?id=com.glgjing.floating.apps.assistive.touch.pro)
- [Overlays - Floating Launcher](https://play.google.com/store/apps/details?id=com.applay.overlay)
- [Mini Phone - Floating Apps](https://play.google.com/store/apps/details?id=com.blackhole.miniphone)
- [Floating Menu](https://play.google.com/store/apps/details?id=com.ksxkq.floating)
- [All Tools](https://play.google.com/store/apps/details?id=com.pradhyu.alltoolseveryutility)
- [Smart Tools - All In One](https://play.google.com/store/apps/details?id=com.pcmehanik.smarttoolbox)
- [Easy Touch Assistive Touch](https://play.google.com/store/apps/details?id=com.easytouch.assistivetouch)

## Latest reference-driven code additions

- Live App Dock: up to six real installed launcher apps displayed in the floating panel.
- Freeform launch: request Android launch bounds; OS/OEM may ignore them.
- Live Music Visualizer: permission-gated Android Visualizer FFT output bars, released on activity stop, no audio file recording/storage.
- RGB glass launcher icon SVG source in assets/dynamic-island-ultra-icon.svg.

## Non-negotiable Android platform constraints

- Runtime and special permissions are user-controlled. The app cannot silently grant them or show a single universal prompt for every permission.
- Android does not let a regular APK convert itself into a privileged system app.
- Foreground services, boot receivers and battery settings improve continuity but cannot defeat force-stop, OEM task killers, Doze or user revocation.
- Play Protect and Google Play policy checks cannot be bypassed or made “immune”; the release must remain policy-compliant and transparently signed.
- Freeform launch, global audio visualizer capture, navigation metadata and call actions vary by Android release, target app and OEM firmware.
- Keep the GitHub package under the established 400 MiB budget; the Wicara GGUF itself remains capped at 100,000,000 bytes. Avoid adding bundled media packs or large SDKs without a size and licensing review.
