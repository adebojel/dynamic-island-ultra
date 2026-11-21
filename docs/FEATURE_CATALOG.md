# Dynamic Island Ultra — v0.0.0 Feature Catalog

## Present in the current source

### Application shell
- Android native app built with Kotlin and Jetpack Compose.
- Dark navy dashboard with cyan/violet accents and launcher branding.
- User-guided permission center with links to Android's overlay, notification-listener, notification, and battery settings surfaces.

### Dynamic Island and assistive bubble
- Foreground overlay service started by explicit user action.
- Floating pill with a draggable touch target.
- Gentle pulse animation while the service is active.
- Bubble X/Y position saved in local preferences.
- Battery percentage and charging marker delivered by the system battery broadcast.
- Notification title/text preview forwarded locally after notification access is granted.
- Expanded, scrollable shortcut hub sized to the device screen.

### Floating shortcut categories
| Category | Shortcuts |
|---|---|
| Quick connectivity | Wi-Fi panel, Internet connectivity, Bluetooth settings, Sound & volume |
| Communication | Phone dialer, App notifications, Notification access |
| Media & capture | Default music app, Camera app, Web browser |
| Productivity | Calculator app, Calendar app, Clock & alarms, 5-minute timer, Android system file picker |
| Device & accessibility | Display settings, Battery settings, Accessibility settings, Application settings, App info, System settings |

These entries open Android-owned settings panels or compatible installed apps. They are launch shortcuts, not custom floating mini-app windows.

### Lifecycle and delivery
- Ongoing notification for the active foreground service.
- Explicit Stop control.
- Best-effort restoration after boot when the user previously enabled the bubble and overlay special access remains granted.
- GitHub Actions build on the main branch.
- Debug APK and SHA-256 checksum in the v0.0.0 Release.
- CI size ceiling of 400 MiB for the APK.

## Not yet implemented
- Real floating mini-app windows (notes, calculator, browser, PDF/document viewer).
- MediaSession metadata, artwork, seek bar and playback controls inside the island.
- Incoming/ongoing call card and call actions.
- Rich download/live-progress and screen-recording status cards.
- Editable themes, RGB editor, transparent style controls, resize/edge docking and user-defined gestures.
- Virtual Home/Back/Recents, lock screen and screenshot shortcuts requiring role/accessibility APIs.
- Sensor toolbox, document scanner, QR/barcode generator, audio recorder and screen recorder.
- Speech recognition, text-to-speech, AI assistant, and offline model support.
- Automated UI/instrumentation suite and physical-device compatibility matrix.

## Permission policy
Only request a permission when the related feature is implemented and the user understands why it is needed. The current app does not request SMS, contacts, call log, broad storage, microphone, or broad package visibility. Android settings and app shortcuts are used instead of bypassing protected system controls.
