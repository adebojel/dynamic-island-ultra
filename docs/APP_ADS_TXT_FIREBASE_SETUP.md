# Firebase Hosting and app-ads.txt Setup (Android-only)

## Purpose

The legal website source is in `site/`. GitHub Pages can mirror the pages, but this repository's GitHub Pages URL is a project subpath. AdMob crawlers request `https://<hostname>/app-ads.txt` at the hostname root. Firebase Hosting is prepared to serve the same website and seller file from a root-level `*.web.app` hostname.

Google's documentation:
- https://developers.google.com/admob/android/app-ads
- https://support.google.com/admob/answer/9820295
- https://firebase.google.com/docs/hosting/github-integration

## Repository files prepared

- `site/app-ads.txt`: Google seller declaration derived from the publisher number in the supplied AdMob App ID.
- `firebase.json`: Hosting configuration pointing to generated `firebase-public/`.
- `.github/workflows/firebase-hosting.yml`: validates the seller file, prepares a root-domain deployment copy, generates hostname-specific sitemap/robots files, and deploys to the live channel.

Seller line currently prepared:
```text
google.com, pub-9305273212801233, DIRECT, f08c47fec0942fa0
```

**AdMob Console is authoritative.** Before production, open AdMob → Apps → app-ads.txt → How to set up, copy its personalized snippet, and compare it character-for-character to `site/app-ads.txt`. Do not add reseller lines unless another ad network is actually used.

## Android phone setup

1. Open https://console.firebase.google.com/ and sign in with the Google account that will own the project.
2. Create a Firebase project. A suggested ID is `dynamic-island-ultra`; Firebase may require a different globally unique ID. The project ID determines the public URL `https://PROJECT_ID.web.app`.
3. In the Firebase project, open Build → Hosting and enable Firebase Hosting.
4. Create a dedicated Google Cloud service account for GitHub Actions. Grant Firebase Hosting Admin (`roles/firebasehosting.admin`). If the Firebase CLI reports an API access requirement, also grant Service Usage API Keys Viewer (`roles/serviceusage.apiKeysViewer`). Do not use your normal Google account password.
5. Create a JSON key for the service account and keep the downloaded file private on your phone. Never add it to repository files or send it in chat.
6. In GitHub repository Settings → Secrets and variables → Actions:
   - Add a **Variable** named `FIREBASE_PROJECT_ID` containing the exact project ID.
   - Add a **Secret** named `FIREBASE_SERVICE_ACCOUNT` containing the entire JSON key content.
7. Open Actions → **Deploy official website and app-ads.txt to Firebase Hosting** → Run workflow → branch `main`.
8. Verify `https://PROJECT_ID.web.app/` loads the website.
9. Verify `https://PROJECT_ID.web.app/app-ads.txt` returns HTTP 200 and the exact plain-text seller line.
10. Set `https://PROJECT_ID.web.app` as the developer website in Google Play Console's store-listing contact details. Then inspect app-ads.txt status in AdMob after the crawler has processed it.

## Secret handling

Never put the service-account JSON into the repository, README, workflow YAML, issue, or chat. Store it only in the encrypted GitHub Actions Secret `FIREBASE_SERVICE_ACCOUNT`. If exposed, revoke the key in Google Cloud IAM and create a replacement.

## GitHub Pages mirror

The GitHub Pages workflow remains separate. It still requires repository Settings → Pages → Build and deployment → Source → GitHub Actions. Firebase Hosting is the root-domain host for app-ads.txt; GitHub Pages can serve the public policy-site mirror.
