# Google Play Console & Monetization Release Checklist

This checklist records the app's current release configuration and the remaining Play Console work. It is not proof that a Play Console review or AdMob approval has passed.

## Application identity

- App name: Dynamic Island Ultra
- Android package: `com.adebojel.dynamicislandultra`
- Repository: `adebojel/dynamic-island-ultra`
- Release branch: `main`
- Minimum Android: API 26 / Android 8.0
- Current production ad SDK configuration: Google Mobile Ads Next-Gen 1.7.0 and UMP 4.0.0
- Billing dependency: Google Play Billing Library 9.1.0
- Premium subscription product queried by the app: `diu_premium`

## Latest verified CI release

- Latest signed release: [v0.0.4](https://github.com/adebojel/dynamic-island-ultra/releases/tag/v0.0.4), published 22 November 2026.
- Android version code: `117`.
- CI: [run #117 — completed successfully](https://github.com/adebojel/dynamic-island-ultra/actions/runs/44990083563).
- Release assets: signed `app-release.apk`, signed `app-release.aab`, both SHA-256 files, and `release-info.json`.
- APK and AAB signatures verified successfully; their certificate SHA-256 values match: `7b2c2fe9ccc24320133e78402c7171d7a835e0d7b425f9119f8d8f1d871a8a0e`.
- This CI verification is not a substitute for physical-device installation, Play Console review, Play Billing testing, AdMob production eligibility, or a successful public website deployment.

## Correct upload file

Each signed GitHub release should attach:

1. `app-release.apk` from the `github` flavor for direct Android device installation and GitHub updater tests.
2. `app-release.aab` from the `play` flavor for upload to Google Play Console.
3. SHA-256 checksum files for each.
4. `release-info.json` with package/version metadata.

**Upload only the AAB built from `bundlePlayRelease` to Play Console, not the APK**, for a new Play app. New Play apps use Play App Signing. The stable JKS already used to sign the APK is the upload key candidate. The CI must verify that the Play AAB and GitHub APK share the same certificate SHA-256.

**Do not upload the old v0.0.3 AAB.** It predates the product-flavor split and includes `REQUEST_INSTALL_PACKAGES` used by the GitHub self-updater. The current v0.0.4 AAB was built from the `play` flavor in the successfully verified [CI run #117](https://github.com/adebojel/dynamic-island-ultra/actions/runs/44990083563); CI checked the merged Play APK manifest and confirmed the installer permission is absent. Use the v0.0.4 `app-release.aab` for the Play Console upload step below.

Because this package has already been distributed outside Google Play, check the Android developer verification/package registration flow during app creation. If Play Console asks to prove package ownership, use the original stable signing keystore and its matching certificate. The SHA-256 fingerprint verified by CI is `7b2c2fe9ccc24320133e78402c7171d7a835e0d7b425f9119f8d8f1d871a8a0e`. Never replace the JKS or upload the private file to the repository.

## Play Console setup

- [ ] Create the app in Play Console with exact package `com.adebojel.dynamicislandultra`.
- [ ] Add a public developer contact email to the Play listing. This is still needed; do not invent one in the policy pages.
- [ ] Enroll in Play App Signing. The stable JKS is already the release/upload key used by GitHub CI. **Choose the app-signing-key option carefully:** the APK currently installed from GitHub is signed by this stable certificate. If the goal is an in-place update from that APK to the Play Store build, select the option to use/provide the same existing app-signing key when Play Console offers it before the first rollout. If Play App Signing uses a different Google-generated app-signing key, Play-distributed APKs will have a different signer and cannot update the GitHub-signed installation in place; a one-time uninstall/reinstall may be required. The upload key and app-signing key have different roles—do not assume that setting the upload key alone preserves the final APK certificate.
- [ ] Upload [v0.0.4 `app-release.aab`](https://github.com/adebojel/dynamic-island-ultra/releases/download/v0.0.4/app-release.aab) from the Play flavor.
- [ ] Verify app signing, package name, version code, target SDK, and generated device APKs.
- [ ] Verify the uploaded AAB came from the `play` flavor and the merged Play manifest does not contain `android.permission.REQUEST_INSTALL_PACKAGES`.
- [ ] Confirm the Play flavor's update screen opens the Google Play listing and never downloads or installs GitHub APKs.
- [ ] Complete the store listing, screenshots, app icon, feature graphic, content rating, target audience, and app access declarations.
- [ ] Complete the Data safety form using the actual SDK disclosures and app behaviour. Do not guess that the app collects no data: AdMob/UMP and WebView interactions introduce third-party data processing.
- [ ] Open the deployed Privacy, Terms, Ads Disclosure, Premium, and Support pages in a normal browser/device and confirm content/navigation; deployment succeeded, but reachability could not be independently confirmed from this environment.
- [ ] Provide the live Privacy Policy URL in Play Console only after the browser check.
- [ ] Complete app review and test track requirements before public rollout.

## Foreground service and overlay declarations

The Play flavor targets API 36 and declares `FloatingIslandService` with `android:foregroundServiceType="specialUse"`, `FOREGROUND_SERVICE_SPECIAL_USE`, and the service-level `PROPERTY_SPECIAL_USE_FGS_SUBTYPE`. The service is started from the user's **Start Island** action, displays an ongoing notification while enabled, and can be stopped from the app's **Stop** control. The `specialUse` case is subject to Play review; this checklist is preparation, not approval.

- [ ] In Play Console → App content, complete the Foreground Service declaration and select the matching `specialUse` use case.
- [ ] Describe the feature accurately: a user-enabled floating island overlay and assistive shortcut panel remain available over other apps while the user keeps the feature enabled.
- [ ] Explain user impact: if the service is deferred or interrupted, the floating island and its shortcut panel disappear until the user starts the feature again; the app's other tools remain available.
- [ ] Explain user control: the feature is started from the dashboard, an ongoing notification identifies the running service and has a Stop action, and both that notification action and the dashboard Stop control end it.
- [ ] Demonstration video should show the notification Stop action as well as the dashboard Stop control, so reviewers can see the persistent service is user-stoppable from either surface.
- [ ] Provide a demonstration video showing the user tapping Start Island, granting the Android overlay permission if needed, using the floating island/shortcut panel, observing the foreground notification, then tapping Stop and confirming the overlay disappears.
- [ ] Review the manifest subtype wording and Play Console description so both describe only the actual overlay/shortcut service.
- [ ] Complete the separate special-access permission disclosure for `SYSTEM_ALERT_WINDOW` if Play Console requests it; explain that the permission is required only to draw the user-enabled floating island and that the feature remains off if permission is declined.

Google requires Android 14+ apps to declare foreground-service types in the manifest and Play Console. Each type needs a description, impact if deferred/interrupted, and a video demonstrating the user steps. `specialUse` may be used only for a valid use case not covered by another standard type and remains subject to review. Official guidance: https://developer.android.com/develop/background-work/services/fgs/service-types and https://support.google.com/googleplay/android-developer/answer/13392821.

## Google AdMob

The app includes the publisher App ID and four publisher production unit IDs in release builds. Debug builds use Google's official test units. Google requires test ads while developing; do not click or repeatedly test live production ads.

- [ ] Confirm the four ad units in AdMob match the IDs integrated into the code.
- [ ] Publish all required UMP consent messages in AdMob → Privacy & messaging.
- [ ] Add the published privacy policy URL to the relevant AdMob app settings.
- [ ] Complete the app readiness/review steps required by AdMob.
- [ ] Verify ad serving with production release without clicking your own ads.
- [ ] Review invalid-traffic alerts and keep video frequency caps appropriate.
- [ ] Keep ad placement disclosures and Data safety responses aligned with the deployed app.

Production IDs being compiled into a signed release does not guarantee ad fill or revenue. Serving depends on account/app eligibility, consent, region, inventory and AdMob policy review.

**app-ads.txt is still a release blocker for AdMob app verification.** Google requires new AdMob apps to verify app-ads.txt (requirement effective January 2025). Copy the personalized seller line from AdMob and publish it at the root of the developer website hostname, then verify its status in AdMob. The current project Pages URL is a subpath (`https://adebojel.github.io/dynamic-island-ultra/`), while the crawler checks the hostname root (`https://adebojel.github.io/app-ads.txt`). A file placed only inside this repository's `site/` directory would resolve under the project subpath and is not sufficient. Before listing this website as the developer website, provide a root-domain hosting solution (for example, a custom domain correctly mapped to Pages or Firebase Hosting) and verify the resulting `/app-ads.txt` URL returns successfully.

## Premium product in Play Console

The app queries a subscription product with ID `diu_premium`. Offers are read from Google Play, so the app displays no fabricated or locally simulated purchasable price.

Recommended prepaid base-plan IDs and intended durations:

| Base plan ID | Duration |
|---|---|
| `weekly` | 1 week |
| `four-weeks` | 4 weeks |
| `monthly` | 1 month |
| `two-months` | 2 months |
| `three-months` | 3 months |
| `six-months` | 6 months |
| `yearly` | 1 year |

- [ ] Create subscription product `diu_premium`.
- [ ] Add base plans above and set them to **Prepaid** if the intention is no automatic renewal.
- [ ] Set Indonesia regional prices in Play Console. Rp2,000 is the intended minimum price target, but final availability and any minimum-price restrictions are controlled by Play Console.
- [ ] Test purchase and restore behaviour in an internal testing track.
- [ ] Verify expiry, extension, pending transactions, refunds and cancellations.

The standard prepaid durations do not include exactly 2 weeks, 3 weeks, or 2 years. Google documents 1 week, 4 weeks, 1/2/3/4/6/8 months, and 1 year. The installment type can cover 3–24 months but is currently offered only in Brazil, France, Italy, and Spain, not Indonesia. Do not advertise those durations as purchasable until a secure one-time pass/entitlement flow is built and validated. The current app only queries the standard subscription product.

## AdMob app-ads.txt

- [ ] Choose a developer website hostname that can serve `/app-ads.txt` at its root.
- [ ] Copy the exact personalized seller line from AdMob → Settings; do not infer the publisher declaration from an ad-unit ID.
- [ ] Publish the plain-text file at `https://<hostname>/app-ads.txt` and confirm it returns HTTP 200 and is crawlable.
- [ ] Enter the same developer website URL in the Play Store listing and check the app-ads.txt status in AdMob after the crawler has had time to process it.

**Current limitation:** `https://adebojel.github.io/dynamic-island-ultra/` is a project Pages subpath. AdMob derives the hostname and checks `https://adebojel.github.io/app-ads.txt`; adding a file only under this repository's `site/` directory is not enough. Set up a root-host site/repository, custom domain, or Firebase Hosting before treating this item as complete. Google recommends app-ads.txt to identify authorized sellers; do not claim it is verified until AdMob reports the status. This is not a Google Play Console AAB upload gate. However, Google's current AdMob setup guidance says new AdMob apps configured from January 2025 must be verified with app-ads.txt before full ad serving; treat it as an AdMob monetization-verification gate.

## Firebase Hosting root-domain deployment

- [ ] Create a Firebase project and enable Hosting.
- [ ] Add GitHub Actions variable `FIREBASE_PROJECT_ID`.
- [ ] Add GitHub Actions secret `FIREBASE_SERVICE_ACCOUNT` containing the Firebase Hosting deployment service-account JSON.
- [ ] Run **Deploy official website and app-ads.txt to Firebase Hosting**.
- [ ] Verify `https://<FIREBASE_PROJECT_ID>.web.app/` and `https://<FIREBASE_PROJECT_ID>.web.app/app-ads.txt` return the intended content.
- [ ] Confirm the seller declaration matches AdMob's personalized snippet character-for-character.
- [ ] Set the Firebase hostname as the developer website in the Play Store listing and complete AdMob app verification.

## Website activation

- [x] GitHub Pages configured with Source = **GitHub Actions**.
- [x] Deployment succeeded in [workflow run #6](https://github.com/adebojel/dynamic-island-ultra/actions/runs/44989672233) on 22 November 2026.
- [ ] Open and check the live Privacy, Terms, Ads Disclosure, Premium, and Support URLs from a normal browser/device. The deployment workflow confirms deployment, but this environment could not independently resolve the `github.io` hostname.
- [ ] Use the privacy URL in Play Console and AdMob only after confirming the page opens and the policy content is accurate.

Intended deployed URLs:
- Home: https://adebojel.github.io/dynamic-island-ultra/
- Privacy: https://adebojel.github.io/dynamic-island-ultra/privacy.html
- Terms: https://adebojel.github.io/dynamic-island-ultra/terms.html
- Ads disclosure: https://adebojel.github.io/dynamic-island-ultra/ads.html
- Premium: https://adebojel.github.io/dynamic-island-ultra/premium.html
- Support: https://adebojel.github.io/dynamic-island-ultra/support.html

