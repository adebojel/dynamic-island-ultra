# Reference feature matrix — Dynamic Island Ultra

This matrix maps the screenshots and supplied store listings to this repository. They are design/feature references only: use original code, artwork and interface design; do not copy proprietary APKs, source or brand assets.

## Visual direction
- Dark translucent glass surfaces, restrained cyan/violet RGB edges, compact pill and categorized floating assistive hub.
- Long-press expansion, smooth battery-conscious motion, optional haptics, accessible labels, scalable text and reduced-motion support.
- The floating service stays user-controlled and must expose Android's required foreground notification and Stop action.

## Screenshot-to-code coverage

| Reference | Verified current implementation | Remaining gap |
|---|---|---|
| Dynamic Island and floating alerts | Draggable overlay, saved position, pulse, auto-dim, eight categories and Quick Favorites | Physical-device/OEM, accessibility and lifecycle validation |
| Bluetooth headset connection/battery | No dedicated accessory card verified | Public Bluetooth APIs and runtime consent where required; only show battery if accessory reports it |
| GPS turn-by-turn card | No in-app navigation engine/provider verified | Deep-link to a navigation app; turn instructions/ETA need a legitimate provider and location consent |
| Charging animation | Live battery percentage and charging state | Custom charging artwork/animation remains unimplemented |
| Music controls | MediaSession metadata/artwork and supported transport actions | Depends on player publishing MediaSession; no private PCM access |
| Music visualizer | No visualizer verified | Needs explicit compliant audio source; no covert capture of other apps' audio |
| Notification quick reply | Notification preview and privacy filters | Reply only when source notification exposes RemoteInput; not currently verified |
| Smart call alerts | Generic call-category notification card | No answer/reject controls; cellular calls remain under Android Telecom/system UI |
| Live activities/app shortcuts | Categorized shortcuts and app-owned hub | No arbitrary third-party screen scraping |
| Long-press expand | Configurable bubble gestures and category panel | Test drag/long-press conflicts on devices |
| Floating apps/assistive touch | User-started overlay, app-owned mini windows, PiP workspace, installed-app launcher | Third-party apps cannot be embedded or forced into PiP/freeform; OS support is best-effort |
| 14-language selector | No complete 14-locale translation set or selector verified | Do not claim 14 languages until strings, plurals, settings and accessibility labels are translated/tested |
| Manual/strobe/music flashlight | User-triggered flashlight control exists | Strobe/music sync not implemented; any future strobe needs warning, strict rate limits and immediate stop |
| 40+ Smart Tools | Converter, counter, stopwatch, countdown/Pomodoro, spirit level; sensor kit for accelerometer, gyroscope, magnetometer, light, proximity and pressure; plus calculator, QR, files, notes, clipboard, reminders, backup, browser and system tools | Not a verified 40+ toolbox. Add each tool only with working implementation, permission review and tests |
| All-in-one hub | Existing floating categories route to implemented modules | Consolidate search/navigation without dummy or duplicate entries |

## Source inventory
- Floating island and bubble configuration: overlay/
- Notification privacy, call-category cards, media session and equalizer: notifications/
- Smart Toolbox: floating/SmartToolsActivity.kt
- Sensor kit: floating/SensorKitActivity.kt
- App collection, notes, calculator, QR, document preview, scoped file manager and app-owned PiP: floating/
- System shortcuts, flashlight and screen recording: system/
- Offline assistant and Wicara: ai/

## Android and privacy constraints
1. Android does not provide one valid popup for every permission. Runtime permissions and special access are requested separately, at point of use, with explanation.
2. An ordinary app cannot make itself a privileged/system app, silently grant permissions, bypass Play policy, or guarantee immunity from force-stop, battery management, OEM task killers or crashes.
3. Boot restoration is best-effort and remains subject to Android/OEM restrictions; users retain a Stop control.
4. Notification reply exists only when the originating notification publishes a supported action. Cellular call controls stay with Android Telecom/system UI.
5. Location, Bluetooth, microphone, camera/flashlight and notification data require relevant user consent.
6. The 400 MiB target is a package/model budget, not a limit on arbitrary user files, downloads or backups. CI must measure APK and bundled-model size.

## Release acceptance
- [ ] Every visible shortcut opens a real destination; no dummy cards or simulated sensor values.
- [ ] Each permission has a narrow purpose, consent flow and privacy documentation.
- [ ] Unit/UI tests pass; emulator-host failures are distinguished from app assertion failures.
- [ ] APK plus bundled model remain within the 400 MiB budget.
- [ ] Stable-signed APK, SHA-256 and release metadata are published.
- [ ] README, feature catalog and release notes are updated with changes.
- [ ] Physical-device results are recorded separately from CI results.
