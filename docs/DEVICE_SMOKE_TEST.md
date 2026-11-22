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
