# Open-Source and App Reference Research

This file records feature inspiration and source-code candidates. A mention here is not approval to copy or integrate a repository. Every dependency or source file must pass license, security, maintenance, Android compatibility, privacy, and size review first.

## Google Play feature references

### Floating multitasking and overlay launchers
- [Floating Apps (free)](https://play.google.com/store/apps/details?id=com.lwi.android.flapps): floating mini-app windows, notes, calculator, PDF/document viewing, browser, quick launch, floating menu, window resize/minimize/maximize.
- [Floating Apps (full)](https://play.google.com/store/apps/details?id=com.lwi.android.flappsfull): expanded floating mini-app collection, floating widgets, window management, quick launch, floating file manager and media player.
- [Overlays — Floating Launcher](https://play.google.com/store/apps/details?id=com.applay.overlay): floating widgets, shortcuts, browser, launcher, notification history, player/volume controls, sidebar, maps and automation triggers.
- [Mini Phone — Floating Apps](https://play.google.com/store/apps/details?id=com.blackhole.miniphone): floating-window launcher, quick search and configurable theme/position/transparency.
- [Floating Assistant](https://play.google.com/store/apps/details?id=com.glgjing.floating.apps.assistive.touch): assistive touch, virtual navigation/volume shortcuts, favorite apps and Android settings.
- [Floating Assistant Pro](https://play.google.com/store/apps/details?id=com.glgjing.floating.apps.assistive.touch.pro): floating clock/countdown and CPU/RAM/battery monitor concepts in addition to assistive touch.
- [All Tools: 70+ Utilities](https://play.google.com/store/apps/details?id=com.pradhyu.alltoolseveryutility): measurement, QR/barcode, scanning, sensors, connectivity, productivity and audio utilities.
- [Smart Tools — All In One](https://play.google.com/store/apps/details?id=com.pcmehanik.smarttoolbox): measurement kits, compass, level, tuner, notepad, translator, counters and sensor tools.
- [EasyTouch Assistive Touch](https://play.google.com/store/apps/details?id=com.easytouch.assistivetouch): assistive-touch and quick-access concept; audit exact feature set and permissions before implementation.
- [Floating app package reference](https://play.google.com/store/apps/details?id=com.ksxkq.floating): package supplied for follow-up research; exact identity and feature scope remain to be verified.
- [Package reference](https://play.google.com/store/apps/details?id=com.media720.games2020): package supplied for follow-up research; the currently surfaced result did not establish that it is a floating-utility app. Verify package identity before using it as a design source.

### Dynamic Island references
- [Dynamic Bar](https://play.google.com/store/apps/details?id=com.callcenter.dynamic.notch)
- [Dynamic Notch](https://play.google.com/store/apps/details?id=com.dynamicisland.notchscreenview)

The app references are used to understand interaction patterns and user expectations. Their proprietary source code, names, logos, screenshots, and other protected assets are not copied.

## Open-source repository candidates

| Candidate | Use as research input | License status |
|---|---|---|
| [Islet](https://github.com/dylanneve1/islet) | Dynamic Island, notifications, media and cutout handling | Apache-2.0 license file verified; audit version and dependencies before use |
| [SmartIsland](https://github.com/agupta07505/SmartIsland) | Notification, music, calls, battery, timers and navigation concepts | GPL-3.0 license file verified; copyleft obligations require legal review before derivative integration |
| [Aura Island](https://github.com/Sensui-moksha/Aura-Island) | Overlay, gesture, camera-ring and multitasking concepts | GPL-3.0 license file verified; do not incorporate without license review |
| [Droidtools](https://github.com/Velocity081/droidtools) | Dynamic Island and edge-bar interaction patterns | Repository describes itself as MIT; root LICENSE file not independently verified in this pass |
| [Droidbubble](https://github.com/Shikateki/Dynamic-island) | Media activity, recording indicator, progress and notification cards | License status must be verified before any reuse |
| [IslandOne](https://github.com/richyrach/IslandOne) | Media player, notification previews, battery and position controls | License status must be verified before any reuse |
| [DigiIsland](https://github.com/JensonVivish/dynamicisland) | Lightweight SDK-only media, calls and charging concepts | License status must be verified before any reuse |
| [Sileo Android](https://github.com/bikash1376/sileo-android) | Physics-based and gooey animation concepts | License status must be verified before any reuse |

## Component intake rules

1. Prefer maintained, narrowly scoped libraries over copying entire applications.
2. Verify the exact license file at the selected commit/tag; a README badge or search snippet is not sufficient.
3. Preserve copyright notices and NOTICE files; comply with attribution, source-disclosure, and copyleft obligations.
4. Pin dependency versions and inspect transitive dependencies and security advisories.
5. Exclude code that requests broader permissions than the feature needs.
6. Measure APK size, startup time, memory, battery impact and accessibility before acceptance.
7. Keep rejected and pending candidates in this inventory; do not ship them until review is complete.

## Current integration status

No third-party application source code has been copied into the v0.0.0 source tree. The current application uses Android platform intents and its own Kotlin implementation for the floating interface and shortcut hub. The research list is a backlog for future, separately reviewed modules.
