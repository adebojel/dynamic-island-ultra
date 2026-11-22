# Network access, browser, AdMob readiness and in-app updates

## Application network permission

The Android manifest declares:

- `android.permission.INTERNET`
- `android.permission.ACCESS_NETWORK_STATE`

Android documents both as normal install-time permissions. They do not cause a runtime permission dialog. They make network connectivity available to application modules. Cleartext HTTP remains disabled; HTTPS is preferred.

The built-in AI package is still isolated from network calls by design and CI checks. Local commands, arithmetic, diagnostics, notes and calculator work without internet. Other explicitly networked modules can be added when needed, with their data use documented.

## Default browser: DuckDuckGo

The built-in browser opens DuckDuckGo as its home page and search engine. The address field accepts search queries or HTTPS website URLs. It includes Back, Forward, Reload, Home and a Desktop/Mobile mode switch.

Desktop mode changes the WebView User-Agent and viewport layout; mobile mode restores the WebView's platform User-Agent. User-Agent switching is a site-compatibility mechanism, not a full desktop operating system. Some websites may still provide mobile-only experiences or require sign-in/cookies.

Web content executes in Android WebView. File access and content-provider access are disabled, mixed content is blocked, Safe Browsing is enabled when supported, and TLS certificate errors are cancelled. Cleartext HTTP is disabled by the app network security config.

## AdMob and Premium implementation

This project integrates Google Mobile Ads Next-Gen SDK `1.7.0`, UMP SDK `4.0.0`, and Google Play Billing Library `9.1.0`. The manifest contains the AdMob App ID. Release builds use the publisher's four ad units; debug builds use Google's official test units. Google specifically requires test ads during development and warns that live ad testing can lead to account suspension.

| Format | Placement / trigger | In-app rule |
|---|---|---|
| Banner | Bottom slot, Bonus & Premium screen only | User can hide it; refresh is disabled in AdMob unit settings |
| Native Advanced | One card labelled “IKLAN”, Bonus & Premium only | User can hide it |
| Rewarded Ads | Explicit tap by user | Reward only on the SDK's earned-reward callback |
| Rewarded Interstitial | Explicit tap plus disclosure dialog with Continue/Skip | At most one app impression per rolling 24 hours |

The app caps all rewarded-format video impressions together at two per rolling 24 hours. No ads are requested from the floating island, offline assistant, microphone, notifications, browser, permission centre, or Android system controls. The video duration depends on the creative served by AdMob and cannot be guaranteed to be under 30 seconds.

UMP consent information is refreshed at every app launch, applicable forms are presented, and ad requests are gated by `canRequestAds()`. The privacy-options entry point is shown when required. The publisher must still publish user messages in AdMob → Privacy & messaging and complete AdMob app review/eligibility. Having an AdMob app and ad units does not prove that production fill is approved or available.

### Premium product

The app queries Play subscription product ID `diu_premium`; price and available offers are fetched live from Google Play. The app does not invent prices or fake purchase success. Create the product and active base plans in Play Console before purchasable plans appear. Configure prepaid base plans to prevent automatic renewal when this is the business choice. The intended affordability target starts at Rp2,000, while actual localized prices and terms are controlled by Play Console.

Standard prepaid subscription periods include 1 week, 4 weeks, 1, 2, 3, 4, 6, or 8 months, and 1 year. Exact 2-week, 3-week, and 2-year options are not standard base plan periods for Indonesia; they need a separately designed one-time pass and secure entitlement validation. These durations are not advertised as available in this build. Current cosmetic Premium features are RGB themes, Island Pulse FX, Island Size, and Compact Panel. Earned ad rewards give a 24-hour local unlock and are not account-backed.

The paid subscription is queried and acknowledged on-device using Google Play Billing. A trusted backend with Google Play Developer API purchase-token verification should be added before relying on account-synced or transferable entitlements.

Official references:
- GMA Next-Gen SDK: https://developers.google.com/admob/android/next-gen/quick-start
- UMP privacy and consent: https://developers.google.com/admob/android/next-gen/privacy
- Banner: https://developers.google.com/ad-manager/mobile-ads-sdk/android/next-gen/banner
- Native Advanced: https://developers.google.com/ad-manager/mobile-ads-sdk/android/next-gen/native/advanced
- Rewarded: https://developers.google.com/ad-manager/mobile-ads-sdk/android/next-gen/rewarded
- Rewarded Interstitial: https://developers.google.com/ad-manager/mobile-ads-sdk/android/next-gen/rewarded-interstitial
- Google Play Billing: https://developer.android.com/google/play/billing/integrate
- AdMob test ads: https://support.google.com/admob/answer/3342099

## AI and local feature boundary

The in-APK assistant is a deterministic on-device decision engine with command routing, calculations and read-only diagnostics. It has no HTTP client, endpoint, API key, telemetry or remote inference. CI rejects network URLs and network clients from the `ai/` source package.

This built-in decision engine is not a general-purpose generative LLM. A generative offline LLM requires model weights and an inference runtime. Those files may be hundreds of MB to several GB, so they must be evaluated against the 400 MiB package budget, runtime RAM, device CPU/GPU and Android 8+ support before claiming generative AI.

## GitHub updates and signing

The updater reads the latest release of `adebojel/dynamic-island-ultra`, reads `release-info.json`, downloads the APK plus SHA-256 asset, checks the digest and opens Android's package installer.

For a non-destructive in-place update Android requires:
- unchanged application ID;
- same signing certificate (or valid proof-of-rotation);
- a version code greater than the installed version;
- user approval in Android's installer UI when requested.

CI uses the monotonically increasing GitHub Actions run number as `versionCode` and version name `0.0.N`. Signed releases are published only when a stable private keystore is configured in GitHub Actions secrets:

- `RELEASE_KEYSTORE_BASE64`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Without those secrets CI builds a debug preview but does not publish it as an update-compatible release. The signing key must be backed up securely and must never be committed to the repository. Moving from the earlier debug-signed preview to the first stable-signed release may require a one-time reinstall; future releases using the same key can update in place and preserve app data.

The app cannot silently install APKs or bypass Android's package installer/user-consent controls.


## Google Play Console upload bundle

The release workflow produces two separately signed files:
- `app-release.apk`: install directly on Android for physical-device smoke tests and GitHub-based updates.
- `app-release.aab`: upload to Google Play Console. The AAB uses the same stable keystore certificate as the APK, and CI verifies the JAR signature and fingerprint before publication.

For a new Google Play app, use the AAB and enroll in Play App Signing. Keep the stable JKS as the upload key and retain its encrypted backups. Google Play will manage the app-signing key used to distribute device-specific APKs. Since this package has already been distributed outside Play, review the Android developer verification/package ownership process and be prepared to prove ownership of the same certificate if Play Console requests it.

The Premium subscription product ID in the app is `diu_premium`. Play Console base plans must be created and activated before the app can display any purchase offers. See `docs/PLAY_CONSOLE_CHECKLIST.md` for the staged checklist.

## Public policy website

The website source and documents are stored in `site/`. **One-time activation is still required** in repository Settings → Pages → Build and deployment → Source → **GitHub Actions**. The first Pages workflow attempt failed because no Pages site had been enabled/configured for this repository. After enabling it, start the “Deploy Dynamic Island Ultra website” workflow from the Actions tab.

After a successful deployment, the website is intended to be available at:
- Privacy: https://adebojel.github.io/dynamic-island-ultra/privacy.html
- Terms: https://adebojel.github.io/dynamic-island-ultra/terms.html
- Ads: https://adebojel.github.io/dynamic-island-ultra/ads.html
- Premium: https://adebojel.github.io/dynamic-island-ultra/premium.html
- Support: https://adebojel.github.io/dynamic-island-ultra/support.html

The public website URL must not be submitted to AdMob or Play Console until the Pages deployment run succeeds and each page is opened and checked. Review the site whenever app permissions, SDKs, data processing, subscriptions or ad placements change.

### AdMob app-ads.txt root-host requirement

AdMob derives the hostname from the developer website in the store listing and checks `https://<hostname>/app-ads.txt` (then HTTP fallback). The current project Pages address uses hostname `adebojel.github.io` with project path `/dynamic-island-ultra/`; publishing `app-ads.txt` only inside this repository's `site/` directory would leave it at the project subpath and would not satisfy the crawler's root-host lookup. A root-host site/repository, custom domain, or Firebase Hosting root is needed. Copy the exact seller declaration from AdMob → Settings rather than guessing it, then verify the public root URL returns the plain-text file before using the developer website in Play Console. app-ads.txt is recommended by Google but is not itself proof of app approval or guaranteed ad fill.

Official guidance: https://support.google.com/admob/answer/9363762 and https://developers.google.com/admob/android/app-ads.
