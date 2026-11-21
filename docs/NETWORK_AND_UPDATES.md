# Network policy and in-app updates

## Deliberate network boundaries

The APK declares `INTERNET` because two explicitly user-facing features need network access:

1. **YouTube Browser** — an in-app WebView whose top-level navigation is restricted to HTTPS YouTube hosts. Subresources are restricted to the YouTube player and a documented allowlist of Google static/API and video CDN domains. JavaScript is enabled only because YouTube's player requires it; file/content access, mixed content and arbitrary top-level browsing are disabled.
2. **GitHub Update Center** — reads only the latest release of `adebojel/dynamic-island-ultra`, downloads the APK and checksum from that release, and verifies SHA-256 before opening Android's package installer.

The app does not request `ACCESS_NETWORK_STATE`, does not install a remote AI SDK, and does not expose a general-purpose HTTP client to assistant modules.

## AI and local feature boundary

All code under `ai/` must remain local-only. CI rejects network APIs, URLs, client SDKs and socket access from that package. The current built-in assistant is a deterministic on-device decision/command engine with arithmetic and system-intent routing; it is not a generative LLM. It can run without internet and without importing a model.

A genuine generative local LLM requires model weights plus a compatible runtime. Google LiteRT-LM's Android guidance describes model files in the hundreds of MB to several GB, and keeps the model as a file on device rather than inside APK assets. Embedding a model that large would conflict with the present 400 MiB APK ceiling and broad Android 8+ hardware/RAM coverage. Do not label this deterministic assistant as a general-purpose LLM.

## Update compatibility and user consent

Android accepts an in-place update only when the application ID is unchanged, the signing certificate matches (or has a valid proof-of-rotation), and the new version code is not lower than the installed one. The system package installer remains in control and may require the user to confirm the installation; this app cannot silently bypass that consent.

Every successful main-branch workflow run assigns a monotonically increasing GitHub Actions run number as `versionCode` and publishes a version tag of the form `v0.0.N`. The CI release APK must be signed using a stable private keystore held in GitHub Actions secrets, never committed to this public repository:

- `RELEASE_KEYSTORE_BASE64`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Without these secrets CI can build a debug artifact for testing, but it must not publish it as an update-compatible release. A one-time signing-key migration from an older debug-signed build may require reinstalling once; future releases signed with the stable key can update in place and preserve app data.
