# Dynamic Island Ultra — Feature Catalog

## Present in current source (physical-device validation still required)

### Dynamic Island and assistive overlay
- User-started foreground overlay service.
- Draggable floating pill, pulse animation and persisted X/Y position.
- Live battery percentage/charging state while service is active.
- Locally forwarded notification preview after notification access is granted.
- Scrollable floating shortcut hub.

### Android shortcuts

| Category | Shortcuts |
|---|---|
| Quick connectivity | Wi-Fi panel, internet panel, Bluetooth settings, sound settings |
| Communication | Phone dialer, app notification settings, notification listener settings |
| Media/capture | Default music, camera, browser apps |
| Productivity | Calculator, calendar, clock/alarms, five-minute timer, Android file picker |
| Device/accessibility | Display, battery, accessibility, app settings, app info, system settings |

Shortcuts open Android settings or existing installed apps; they are not privileged system toggles.

### Offline tools
- **Installed app collection:** queries launcher-visible activities using a limited manifest query; launches selected app.
- **Offline notes:** saves one local note in app-private preferences.
- **Offline calculator:** parses basic arithmetic, parentheses and unary signs locally.
- **Offline utility assistant:** deterministic Indonesian/English keyword routing, arithmetic and system battery status. This is not a generative LLM.
- **PiP workspace:** Android-managed PiP for an app-owned workspace that launches internal notes and calculator activities.

### Offline privacy / build
- No INTERNET or ACCESS_NETWORK_STATE manifest permissions.
- No online AI, analytics, ads, telemetry, cloud sync or remote configuration SDK.
- CI checks network permissions/dependencies, tests local logic, builds APK, enforces 400 MiB APK ceiling and generates SHA-256.

## Platform boundaries
- Third-party apps cannot be forced into PiP by this app.
- Installed app list is limited to launcher-visible activities; no QUERY_ALL_PACKAGES permission.
- Boot restoration remains best-effort and subject to Android/OEM restrictions.

## Not yet implemented
- Generative local LLM runtime and separately imported local model.
- Offline speech recognition and TTS models.
- Multiple independent floating app windows and floating document/browser widgets.
- MediaSession artwork/progress/playback controls and call activity cards.
- Screen capture/recording, QR/barcode scanner, document scanner and hardware sensor suite.
- User theme editor, RGB picker, gestures, edge docking, haptics.
- Automated UI/device instrumentation and physical-device OEM matrix.

## Permission policy
Request permissions only when the feature is implemented and its use is disclosed. No SMS, contacts, call log, microphone capture, broad storage, broad package visibility or network permissions are requested.
