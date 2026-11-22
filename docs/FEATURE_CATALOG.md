# Dynamic Island Ultra — Feature Catalog

## Floating island and assistive hub

- User-started foreground overlay service.
- Draggable floating pill, pulse animation and persisted X/Y position.
- Live battery percentage/charging state.
- Local notification preview after notification access is granted, with global disable, hidden content by default, app allowlist and filtering of secret, ongoing, group-summary and own-app notifications.
- Generic call activity pill from call-category notifications only; it obeys the global preview switch and app allowlist and uses no call logs or phone-state permission.
- MediaSession metadata, artwork, play/pause, skip and seek where the player exposes those actions.
- Categorized shortcut hub for Android settings and installed apps.

## Offline modules

- **Installed app collection:** queries launcher-visible activities using `ACTION_MAIN` + `CATEGORY_LAUNCHER` (no `QUERY_ALL_PACKAGES`); labels, icons and package names are used locally to populate the launch list and are not uploaded to a developer server or the offline assistant.
- **Offline notes:** stores note text in app-private preferences.
- **Offline calculator:** evaluates arithmetic, parentheses and unary signs locally.
- **Built-in local assistant:** deterministic command-routing engine, arithmetic, system shortcuts, browser/update launch and read-only diagnostics.
- **Local diagnostics:** reads Android API/version, overlay permission, notification access/permission, WebView availability, battery and cache space.
- **PiP workspace:** Android-managed PiP for this app's own activity.
- **QR/barcode scanner:** Google Play services Code Scanner 16.1.0; no app camera permission; first use may download its scanner module.
- **Image/PDF preview:** Android document picker, local image decoding and first-page PDF rendering.
- **Sensor kit:** foreground-only accelerometer, gyroscope, magnetometer, light, proximity and pressure readings; listeners stop on pause.
- **Voice assistance:** Android 12+ on-device SpeechRecognizer when a local service exists, explicit microphone request and device TextToSpeech response.
- **Island interaction:** persisted drag position, optional left/right edge docking and opt-in haptic feedback.

The built-in assistant and local tools work without internet, imported model files or AI API credentials. The built-in engine is not a generative LLM.

## Smart Toolbox (floating bubble integration)

- Local unit converter for length, mass, volume and temperature, including metric and common imperial units.
- Counter with increment, decrement and reset.
- Stopwatch based on Android's monotonic elapsed-realtime clock, with recorded lap splits.
- Countdown timer up to 24 hours and a 25-minute Pomodoro preset.
- The toolbox is reachable from bubble category 09, a configurable gesture, or a Quick Favorite.
- Conversion logic is a pure local module with unit tests; no permission or network dependency.
- Timer/stopwatch are in-app session utilities, not a replacement for AlarmClock or persistent scheduled reminders.

## Internet-enabled modules

- **DuckDuckGo browser:** DuckDuckGo default home/search; HTTPS address bar for general websites; Back/Forward/Reload/Home; Desktop/Mobile layout mode via User-Agent and viewport switching.
- **GitHub Update Center (GitHub flavor only):** checks the latest project release, downloads APK and metadata, verifies SHA-256, then opens Android's package installer.
- **Update command:** the offline assistant routes to the app's official update center with distribution-neutral wording. The GitHub flavor checks signed GitHub releases and the Play flavor opens the Play Store listing.
- **Google Play update route (Play flavor):** opens the Play Store listing; the Play variant does not include the package-install permission or GitHub self-updater path.
- **AdMob/UMP:** Google Mobile Ads Next-Gen, publisher App ID/ad units, UMP consent flow, privacy options, Banner, Native Advanced, Rewarded and Rewarded Interstitial are integrated. Live serving remains dependent on publisher configuration, app review/eligibility, consent and inventory.
- Shared manifest declares `INTERNET` and `ACCESS_NETWORK_STATE`; these are normal Android install-time permissions. `REQUEST_INSTALL_PACKAGES` is declared only by `app/src/github/AndroidManifest.xml`, never by the Play flavor. Cleartext HTTP remains disabled.
- CI prevents the built-in `ai/` code package from adding network clients or endpoints.

## Android compatibility and release

- Minimum Android: 8.0 Oreo / API 26.
- Compile/target SDK: 36.
- Release `versionCode` comes from the monotonically increasing GitHub Actions run number.
- Each signed main build increments the patch release tag from the latest successful `v0.0.N`; the current verified release at the time of this update is `v0.0.8`.
- Stable release signing secrets are required for the signed GitHub APK and Play AAB; both production flavors use the same application ID and stable release key.
- APK checksum and release metadata accompany signed releases.

## Platform boundaries

- Third-party apps cannot be forced into PiP.
- Installed app list is limited to launcher-visible activities.
- Android controls APK installer confirmation; app cannot silently bypass it.
- Boot restore and long-running overlays remain subject to Android/OEM restrictions.

## Floating bubble configuration update

- Tap, double-tap, long-press, and configurable four-direction swipe gesture mapping.
- Allowlisted actions route only to the app's own menu, offline assistant, media controls, notification privacy screen, PiP workspace, and bubble settings.
- Swipe mappings default to disabled to preserve normal drag behaviour.
- User-adjustable bubble size (80–140%) and opacity (40–100%); live appearance refresh is broadcast only within this app.
- Gesture action validation falls back to safe defaults for invalid or stale stored values; arbitrary commands are never accepted.

## Configurable bubble Quick Favorites

- Four user-selectable shortcuts are displayed at the top of the bubble panel.
- Available allowlisted actions: offline assistant, notes, calculator, installed apps, media controls, QR scanner, document preview, sensor kit, app-owned PiP workspace and Island settings.
- Each slot persists in app-private preferences; invalid/stale values resolve to a safe default.
- Favorites open only app-owned activities. They do not perform privileged Android actions or launch arbitrary commands.

## Bubble category accordion and launch lifecycle

- All eight bubble categories are expandable/collapsible sections; they start collapsed to keep the bubble panel compact.
- Category headers are full-width, keyboard-focusable controls with expanded/collapsed text and accessibility descriptions.
- Opening/closing a category changes only its local panel content; it does not trigger system settings or external app automation.
- Selecting a category action closes the overlay panel before starting the selected destination, avoiding the panel remaining above the target screen.

## Launcher identity and gesture input hardening

- Replaced the full-canvas foreground background with transparent layered floating-island/waveform artwork.
- Added a dedicated launcher background color for the Android adaptive icon; regular and round definitions share it.
- Long-press haptic feedback respects the haptic preference.
- ACTION_CANCEL removes delayed gesture callbacks and restores a drag to its original position.

## Floating bubble inactivity dimming

- Opt-in setting: “Auto-dim setelah 8 detik”, disabled by default.
- After eight seconds without input, the bubble dims to a non-zero alpha and remains visible/touchable.
- ACTION_DOWN restores the configured appearance; ACTION_UP/ACTION_CANCEL starts a fresh idle interval.
- The category panel is not dimmed; closing it restarts the idle countdown.
- Dimming cancels the pulse animator while idle and restarts the configured pulse when interacted with.
- This is not full hide/auto-removal and does not promise immunity from Android/OEM service lifecycle controls.


## File Manager (scoped storage)

- Select a folder through Android's Storage Access Framework and persist access to that selected document tree only.
- Navigate subfolders; search names; sort folders first by name or most recently modified.
- Create folders; open files with a compatible installed application; share files using a temporary read grant; rename items; delete with explicit confirmation.
- No broad storage permission or full-device file scan is requested.
- Copy/move, ZIP extraction/creation, file preview/editor integration and provider-specific capability handling beyond the supported operations are not yet implemented.


## Browser library and download handling

- Persistent bookmarks stored in app-private preferences for HTTPS pages.
- Local browsing history capped at 100 unique HTTPS URLs; revisits move the entry to the most recent position.
- HTTPS downloads are delegated to Android DownloadManager with cookie and user-agent headers from the active WebView session.
- Download files are stored in the app-specific external Downloads directory; no broad storage permission is requested.
- TLS certificate errors remain cancelled; the browser does not accept a certificate bypass.
- Multi-tab session manager, private/incognito sessions and a full bookmark/download library editor remain unimplemented.


## Global system navigation controls

- Optional user-enabled Accessibility Service for Home, Back, Recent Apps, Notifications, Quick Settings, Power Dialog and user-triggered system screenshot (API 28+).
- Lock Screen is offered on Android 9/API 28 and newer.
- Service must be enabled manually in Android Accessibility settings; the app cannot silently enable it.
- XML configuration sets `canRetrieveWindowContent=false`; the event callback does not inspect or retain screen content. Every action is initiated by an explicit user tap.
- Screenshot, screen recording, global volume/brightness sliders and flashlight toggles are not implemented by this service.


### Smart Toolbox — accelerometer spirit level

- Two-axis pitch/roll display derived from the device's actual accelerometer readings.
- Live readings are registered only while SmartToolsActivity is resumed and unregistered on pause.
- User can set the current physical device orientation as a local zero reference and reset the reference.
- Unsupported devices show an explicit unavailable state; no simulated readings or network dependency.
- Not a certified precision instrument; case shape, sensor bias, movement and device calibration affect accuracy.


## Local RGB accent editor

- Hue, saturation and brightness sliders with a live color/hex preview.
- The custom accent is saved to app-private preferences and refreshed on the floating pill.
- Reset returns to the default cyan accent.
- The editor changes the island accent only; it is not yet a complete theme/gradient/animation or icon-pack editor.


## Encrypted Backup & Restore

- Export and restore the app's selected local preference stores: island interaction settings, offline notes, browser bookmarks/history and notification privacy settings.
- Backup file format `.diub` uses PBKDF2-HMAC-SHA256 (180,000 iterations) to derive a 256-bit key and AES-256-GCM authenticated encryption with fresh random salt and IV.
- The user chooses the file destination/source through Android's Storage Access Framework and supplies the passphrase; there is no cloud upload.
- Restore requires successful authentication and explicit confirmation; it stops and disables the floating island before replacing the included settings.
- Premium purchase entitlements, Android permission grants, SAF folder grants, media files and other apps' data are excluded intentionally.


## Persistent Reminders

- Create up to 30 one-time reminders, from 1 minute to 7 days, with a custom title.
- Scheduled using `AlarmManager.setAndAllowWhileIdle`; alarms survive ordinary app process closure.
- Pending reminders are persisted in app-private preferences and re-scheduled by the existing `BOOT_COMPLETED` receiver.
- Reminder notifications use a dedicated channel and Android's notification permission state.
- Timing is inexact and may be delayed by Doze/OEM policies; force-stop can suspend delivery until the app is opened again.
- Reminder data is included in the encrypted local backup.


## Clipboard History

- User-triggered capture only: clipboard is read only after the Capture button is tapped.
- Plain text only; URI/image/other clip payloads are not imported.
- Up to 50 items are stored locally, with duplicate text moved to the newest position.
- Supports copy back to the Android clipboard, individual deletion and clear-all.
- Does not run a background clipboard monitor; the UI warns against storing passwords, OTPs or financial secrets.
- Included in the encrypted local backup.


## App-owned Floating Window Manager

- Up to four simultaneous app-owned mini windows using Android `TYPE_APPLICATION_OVERLAY`.
- Available tools: local notes (auto-saved), four-operation calculator and countdown timer (1–240 minutes).
- Window header supports drag, minimize/restore, compact/large size preset and close.
- Windows are created only from the running floating service after Android overlay permission is granted.
- Stopping the foreground Island service closes all mini windows; Notes persist locally and are included in encrypted backup, while active mini timers are session-only.
- Does not embed or force third-party applications into freeform windows.
