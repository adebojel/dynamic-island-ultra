# Physical-device smoke test — Dynamic Island Ultra

This checklist is for a real Android device. CI success does not replace these checks. Record the result of each item as PASS, FAIL, or NOT TESTED and include a screenshot or short screen recording for failures.

## Before installation

- [ ] Confirm the release tag and package from the GitHub Release page. Use the newest successful release only.
- [ ] Download `app-release.apk` for the GitHub/sideload channel. Do not try to install the Play AAB directly.
- [ ] Keep the currently installed app until the update-over-existing-install test is complete. The GitHub APK and prior GitHub release use the same stable signing certificate.
- [ ] Verify the downloaded APK using the attached `.sha256` file or the app's own updater flow. Do not install a file if the checksum differs.
- [ ] If Android asks to allow installation from this source, grant it only to the trusted browser/file manager used to install the official GitHub APK. Revoke the permission afterwards if it is no longer needed.

## A. GitHub APK installation and update compatibility

- [ ] Install the APK over the existing GitHub-distributed app without uninstalling it first.
- [ ] Confirm Android completes an in-place update and the app opens.
- [ ] Confirm app settings, saved local notes/preferences, and the enabled/disabled state remain consistent after update.
- [ ] Open the GitHub Update Center. Confirm it reads the latest version, downloads the APK and checksum, verifies SHA-256, then hands installation to Android.
- [ ] Confirm Android displays the package installer and still requires the user's approval.
- [ ] Do not test with modified APK files or bypass checksum verification.

**Expected:** the existing installation updates in place because package ID and release certificate are unchanged. If Android reports a signature conflict, stop and capture the exact message; do not uninstall until the issue is diagnosed.

## B. Floating island, foreground service, and user controls

- [ ] Open Permission Center and grant Display over other apps only after Android shows the system settings screen.
- [ ] Grant Notification Access only after reviewing the disclosure and the Android system confirmation.
- [ ] On Android 13+, grant notification permission if requested.
- [ ] Start the island from the dashboard.
- [ ] Confirm the overlay appears above another app and remains usable.
- [ ] Confirm the ongoing notification states that Dynamic Island Ultra is active.
- [ ] Tap **Stop** in the ongoing notification. Confirm the overlay disappears and the service stops.
- [ ] Start again, then use the dashboard **Stop** control. Confirm the same result.
- [ ] Deny overlay permission once and confirm the app does not keep restarting the service or claim that the island is active.
- [ ] Restart the phone with the island disabled and confirm it remains disabled.
- [ ] If testing boot resume, enable the island intentionally before reboot. Confirm resume only occurs when the saved setting is enabled and overlay permission remains granted.

**Expected:** the foreground service is user-started, visibly notified, and can be stopped from both the notification and dashboard. If the service is interrupted, the overlay should disappear until the user starts it again.

## C. Notification preview and privacy

- [ ] Send a test notification from another app.
- [ ] Confirm the preview appears in the island only when Notification Access is enabled.
- [ ] Confirm content is masked by default and the app label is shown.
- [ ] Disable all previews, then test allowlist-only mode with one permitted app.
- [ ] Confirm secret, ongoing, group-summary and own-app notifications are filtered.
- [ ] Confirm the preview is local and is not sent to the app's AI or a remote endpoint.
- [ ] Disable Notification Access in Android settings and confirm new previews stop.
- [ ] Test a private/sensitive notification and decide whether its content should be shown in the island. Report any content that appears unexpectedly.

## D. Media, QR, document, sensor and accessibility features

- [ ] Play media in a compatible app with Notification Access enabled; inspect metadata, artwork and controls.
- [ ] Test play/pause, skip and seek only where the player exposes those actions.
- [ ] Scan a QR code and barcode; inspect local result and confirm only user-approved HTTPS links open.
- [ ] Select an image/PDF with Android's document picker and verify local preview.
- [ ] Open Sensor Kit; check readings update and stop after leaving the screen.
- [ ] Check generic call-category activity; verify the global preview switch and app allowlist both suppress calls from excluded apps. No call-log or phone-state permission should be requested.

## D. Built-in offline assistant and local tools

- [ ] Turn off mobile data and Wi-Fi.
- [ ] Open the built-in assistant and test arithmetic, help, battery, and local settings commands.
- [ ] Confirm local commands work without an internet connection.
- [ ] Confirm browser and GitHub update actions clearly require a network connection and do not pretend to work offline.
- [ ] Test invalid arithmetic and unknown commands; they should return a safe explanation and must not launch an unrelated action.
- [ ] On Android 12+, check local Indonesian recognizer availability, grant microphone permission only after tapping Voice, and test with network disabled.
- [ ] On Android 8–11, confirm the app explains the local-recognition limitation and keeps text input available.
- [ ] Test Text-to-Speech using an installed Indonesian system voice.
- [ ] Turn TalkBack on and navigate the dashboard, Permission Center, assistant and Stop controls. Report unlabeled or unreachable controls.

**Expected:** the built-in assistant is a deterministic offline command assistant, not a remote generative AI model. Do not interpret its local command replies as a claim that an external service was contacted.

## E. AdMob and consent

- [ ] Open Bonus & Premium and confirm any ad placement is clearly labelled **IKLAN**.
- [ ] Complete the UMP consent flow when it is presented.
- [ ] Open privacy options if the control is offered and confirm the page remains responsive.
- [ ] Confirm ads do not appear on the floating island, assistant, notifications, browser, or system-control screens.
- [ ] If an ad is not filled, record the time, network state, and screen. No fill can be caused by consent, app readiness, inventory, region, or account review.
- [ ] **Never click your own live ads**, ask another person to click them, or repeatedly generate impressions to test them. Observe whether ads load; use AdMob's reporting and test-device tooling for diagnostics.

**Expected:** release builds use the publisher's production unit IDs. This does not guarantee production fill or mean AdMob has approved the app. Debug builds use Google's official test units.

## F. Premium and Google Play Billing

- [ ] Open Bonus & Premium and confirm available prices and billing periods are read from Google Play, not hard-coded.
- [ ] If `diu_premium` or its base plans have not yet been created and activated in Play Console, confirm the app clearly reports that no active package is available.
- [ ] Test the purchase and restore flow only after the product/base plans are activated and the app is installed from a Play testing track with a tester account.
- [ ] Do not treat a locally rewarded cosmetic unlock as a paid subscription.

## G. Google Play flavor (internal testing)

This section applies only after the Play AAB has been uploaded to a Play internal testing track.

- [ ] Install the app from the Google Play internal testing link, not by sideloading the AAB.
- [ ] Confirm the app does not ask for Allow from this source to update itself.
- [ ] Open the update action from the dashboard and from the offline assistant. Confirm both routes guide the user to Google Play.
- [ ] Confirm the Play build does not download or install a GitHub APK.
- [ ] Test Play Billing with a licensed tester account and active product/base plans.
- [ ] Record the special-use foreground service demo requested by Play Console, including Start Island, overlay permission if needed, ongoing notification, use above another app, and Stop from notification/dashboard.

## Result log

For each failure, record:

- Release tag and version code
- Device model and Android version
- Steps immediately before the failure
- Exact Android message or app status
- Screenshot or screen recording
- Whether the issue happened on the GitHub APK or Play-installed build

Do not mark a section PASS until it has been exercised on a real device.
## Floating bubble gesture and appearance checks (v0.0.9)

- [ ] Tap the bubble once; confirm the categorized bubble panel opens.
- [ ] Double-tap; confirm the offline assistant opens and no network request is made by the assistant.
- [ ] Long-press; confirm the configured local action runs once and dragging remains possible after release.
- [ ] Open Island Interaction settings and map one swipe direction to Media Controls. Swipe in that direction; confirm only the selected in-app action opens.
- [ ] Set all swipe directions to “Tidak ada tindakan”; confirm swipe movement continues to drag the bubble instead of triggering an action.
- [ ] Check the default configuration: swipe directions are disabled, preventing accidental interception of drag gestures.
- [ ] Set bubble size to 80%, 100% and 140%; confirm the active pill updates after the setting is applied.
- [ ] Set opacity to 40% and 100%; confirm pulse animation respects the selected maximum opacity.
- [ ] Stop and restart the overlay service; confirm stored size, opacity, gesture mappings, position, edge-docking and haptic preference remain intact.
- [ ] Rotate the device and test the bubble/menu touch targets, clipping and saved position in both orientations.
- [ ] Enable TalkBack and verify the bubble description and settings controls are announced in a predictable focus order.

**Expected:** only the allowlisted app-owned actions can be selected. No gesture may run arbitrary shell commands, silently grant permissions, or change protected system settings. Record any OEM-specific gesture timing issue as FAIL with a short recording.

## Quick Favorites checks

- [ ] Confirm the four default entries are AI, Notes, Calculator and Apps.
- [ ] Change each slot independently in Island Interaction settings; reopen the bubble and confirm the updated shortcut order/labels.
- [ ] Tap each favorite and confirm it opens the intended app-owned screen and the expanded bubble panel closes.
- [ ] Restart the overlay service and confirm favorite configuration persists.
- [ ] Store an invalid favorite preference in a test build and confirm the slot falls back to a safe default.
- [ ] Turn on TalkBack and confirm every favorite exposes its full action label, not only its abbreviated visual label.

## Bubble category accordion checks

- [ ] Open the bubble panel and confirm all eight category headers are visible while their items are initially collapsed.
- [ ] Expand each category header and verify its action list becomes visible and the header announces the expanded state.
- [ ] Collapse each category and verify its items are hidden again.
- [ ] Use TalkBack to move through category headers; confirm each is focusable and announces its title and expanded/collapsed state.
- [ ] Select an action from each applicable category and confirm the bubble panel closes before the destination opens.
- [ ] Reopen the bubble panel after navigating back; confirm the accordion is in its documented default collapsed state.

## Launcher icon and gesture cancellation checks

- [ ] Confirm the icon is clipped to adaptive launcher masks (circle, squircle and rounded-square).
- [ ] Check the icon at home-screen, app drawer and Settings app-info sizes; ensure the waveform and status lens stay visible.
- [ ] Turn Haptic feedback off and long-press; confirm no vibration occurs.
- [ ] Turn Haptic feedback on and long-press; confirm the configured interaction feedback works.
- [ ] Begin dragging then trigger ACTION_CANCEL; confirm the bubble returns to its original position without a delayed tap action.

## Floating bubble auto-dim checks

- [ ] Confirm auto-dim is disabled on a fresh installation/default preferences.
- [ ] Enable auto-dim and leave the bubble untouched for eight seconds; confirm it dims but remains visible and touchable.
- [ ] Tap the dimmed bubble; confirm its normal opacity and configured pulse style are restored immediately.
- [ ] Move/drag the bubble; confirm the idle timer restarts after release.
- [ ] Open the category menu; leave it open beyond eight seconds and confirm the bubble/menu does not dim.
- [ ] Close the panel; confirm the eight-second idle timer starts again.
- [ ] Disable auto-dim while the bubble is dimmed; confirm its normal appearance is restored.
- [ ] With pulse style Off, repeat auto-dim and touch restoration; confirm the setting still works without an animator.


## Smart Toolbox checks

- [ ] Open Floating Island → category 09 Smart Toolbox; confirm the page opens and does not ask for new permissions.
- [ ] Convert 1.25 m to 125 cm, 1 kg to 1,000 g, 1 L to 1,000 mL, and 32 °F to 0 °C.
- [ ] Enter a decimal with comma and dot; verify conversion stays local and handles invalid input without crashing.
- [ ] Counter: test increment, decrement below zero, and reset.
- [ ] Stopwatch: start, pause, resume and reset; compare elapsed time with a physical clock.
- [ ] Countdown: test pause/resume/reset, zero duration validation and Pomodoro 25-minute preset.
- [ ] Assign Smart Toolbox to a gesture and each Quick Favorite slot; verify it launches only the app-owned toolbox.
- [ ] Rotate the device and test large font/TalkBack focus order.
- [ ] Background or force-stop the app during a countdown and record the observed behaviour; do not treat this in-app timer as a guaranteed alarm.


## File Manager checks (SAF scoped access)

- [ ] Launch File Manager from the dashboard and from Floating Bubble → My Floating Apps.
- [ ] Confirm the system folder picker appears and no broad storage permission is requested.
- [ ] Select a test folder and confirm its contents are listed.
- [ ] Open a subfolder, use Back, and confirm the navigation stack returns to the parent.
- [ ] Search for an existing file and test both alphabetical and newest-first sorting.
- [ ] Create a folder; confirm it appears and remains after leaving/reopening the activity.
- [ ] Open a supported file type and confirm Android launches a compatible viewer.
- [ ] Share a selected file and confirm the receiving app can read it.
- [ ] Rename an item and confirm the updated name is shown.
- [ ] Delete a test item only after the confirmation prompt; confirm it disappears.
- [ ] Reopen File Manager after force-stop/relaunch and confirm the persisted selected folder remains accessible.
- [ ] Revoke folder access from Android settings (or move/remove the test storage) and confirm the activity returns to folder selection without crashing.
- [ ] Repeat on Android 8/API 26 and a current Android release; record OEM/provider-specific failures.


## Browser library and download checks

- [ ] Open an HTTPS website and save it as a bookmark; close/reopen the browser and open it from Bookmarks.
- [ ] Visit several HTTPS pages; verify History shows recent unique URLs and revisiting a URL moves it to the latest position.
- [ ] Confirm the history list is capped at 100 entries and Clear History removes local records.
- [ ] Download a known safe HTTPS file from a site; confirm Android DownloadManager receives it and the completion notification is shown when notifications are allowed.
- [ ] Confirm download destination is app-specific external Downloads storage and no broad storage permission is requested.
- [ ] Confirm HTTP/HTTPS navigation and TLS error handling remain strict; certificate errors must not be bypassed.
- [ ] Confirm non-HTTP(S) active-content URLs are rejected.
- [ ] Test browser destruction/recreation, rotation, long downloads and WebView behaviour on Android 8/API 26 and a current Android version.


## Global navigation control service checks

- [ ] Open Global System Controls and read the disclosure before enabling the Accessibility Service.
- [ ] Confirm the service is disabled by default and the app opens Android Accessibility settings only after the user taps Enable.
- [ ] Enable the service manually and return; confirm the UI detects the enabled state.
- [ ] Tap Home, Back, Recent Apps, Notifications, Quick Settings and Power Dialog individually; confirm each action runs only after the direct tap.
- [ ] On API 28+, test Lock Screen; on API 26–27 confirm it is not offered.
- [ ] Confirm the service does not expose or read window text/content and no screen text is shown or stored by the module.
- [ ] Disable the service in Android settings; return and confirm controls become disabled and no false success is shown.
- [ ] Test with TalkBack enabled and across at least two OEM implementations.


## Smart Toolbox spirit-level checks

- [ ] Open Smart Toolbox → Spirit Level on a device with an accelerometer; confirm real readings arrive.
- [ ] Keep the device flat and check pitch/roll near zero; record expected sensor bias.
- [ ] Tilt the device on each axis and confirm the indicator moves in the corresponding direction.
- [ ] Set the current position as zero and confirm the displayed relative angles return near zero.
- [ ] Reset calibration and confirm the original gravity-based angle returns.
- [ ] Leave the activity and confirm sensor listeners stop; reopen and confirm readings resume.
- [ ] Test on a device without an accelerometer (or an emulator configured without one) and confirm the UI states the sensor is unavailable rather than displaying a simulated value.
- [ ] Repeat on Android 8/API 26 and record the model, sensor vendor and observed stability.


## Stopwatch lap checks

- [ ] Start stopwatch and record at least three laps; confirm each split reflects elapsed time since the prior lap.
- [ ] Pause stopwatch; confirm lap recording is disabled and elapsed time remains stable.
- [ ] Resume and record another lap; confirm split timing continues from the previous lap.
- [ ] Reset; confirm elapsed time and all lap records are cleared.
- [ ] Confirm elapsed timing uses monotonic elapsed-realtime and does not jump when device wall-clock time changes.


## Custom accent editor checks

- [ ] Open Island Interaction settings and adjust hue, saturation and brightness; confirm the preview color and HEX label update.
- [ ] Leave settings and confirm the floating island outline/accent changes without restarting the service.
- [ ] Close/reopen the app and confirm the custom accent persists in dashboard theme and island.
- [ ] Reset accent and confirm cyan default is restored.
- [ ] Test low brightness/high saturation combinations for text contrast and daylight readability.


## Encrypted Backup & Restore checks

- [ ] Create a backup using a passphrase of at least eight characters; confirm Android opens the document destination picker.
- [ ] Confirm the resulting `.diub` file is not readable as plain JSON/text.
- [ ] Change notes, gestures, custom accent and notification privacy settings, then restore the backup; confirm only the documented stores return to their backed-up values.
- [ ] Confirm island is stopped and disabled after restore, then manually restart it.
- [ ] Attempt restore with a wrong passphrase and a modified/truncated file; confirm it is rejected and current preferences remain unchanged.
- [ ] Confirm Premium purchases/entitlements, Android permission grants, and File Manager SAF folder grants are not restored or fabricated.
- [ ] Test export/import using at least two document providers and on Android 8/API 26.


## Persistent Reminder checks

- [ ] Schedule a 5-minute and a custom reminder; confirm both appear in the pending list.
- [ ] Close the app normally and confirm reminders remain scheduled.
- [ ] Cancel one reminder and confirm its notification does not appear at the expected time.
- [ ] Reboot the device before a future test reminder; confirm BootReceiver re-schedules it.
- [ ] Confirm the reminder notification appears when POST_NOTIFICATIONS is granted.
- [ ] Revoke notifications and confirm no crash; verify UI/documentation describes the notification permission requirement.
- [ ] Test during Doze and battery optimization; record actual delay and OEM model.
- [ ] Confirm expired reminders are removed during reboot rescheduling and notification dispatch.


- [ ] On Android 9/API 28+, tap Take Screenshot and verify Android's system screenshot action runs; confirm app code does not read the resulting image.
- [ ] On Android 8/API 26–27, verify the screenshot action is not shown.
