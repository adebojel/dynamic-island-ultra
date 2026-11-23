# Phase 2 QA checklist

## CI-verifiable gates

- [ ] Hugging Face model revision is a 40-character immutable commit.
- [ ] Upstream model card declares Apache-2.0 and the Hugging Face Hub HEAD metadata exposes a valid X-Linked-Etag/ETag file SHA-256.
- [ ] Downloaded GGUF is no larger than 100,000,000 bytes, has valid GGUF magic and matches exact upstream size/hash.
- [ ] Model asset and metadata are present in APK/AAB; release-info.json parses as valid JSON.
- [ ] APK/AAB plus a second model-size allowance stays below 400 MiB.
- [ ] llama.cpp is pinned and compiled for ARM64.
- [ ] ARMv7 includes explicit unsupported-runtime behaviour and remains non-AI installable.
- [ ] Unit tests cover command routing, prompt markers, input limits, model size, GGUF magic and SHA-256.
- [ ] Release signature and APK/AAB SHA-256 checks are successful.

## Physical / external gates

- [ ] Android 8/API 26 physical install and non-AI regression.
- [ ] ARM64 inference, cancellation, screen exit, unload and reload.
- [ ] Offline inference with radios disabled.
- [ ] ARMv7 install and clear unsupported-AI message.
- [ ] Samsung, Xiaomi/Redmi, Motorola, OPPO, vivo and realme lifecycle tests.
- [ ] TalkBack, focus order, text scaling, touch target and contrast.
- [ ] 10-minute and 30-minute RAM/PSS, CPU, battery, temperature and throttling.
- [ ] Signed APK in-place update without uninstall.

## Evidence baseline

Before Wicara integration, workflow #166 succeeded for unit tests, signed APK/AAB build, signatures, checksums and release publication. That run did not contain a GGUF/JNI runtime and is not evidence of generative AI. Populate this page with the new workflow run, release SHA-256 and physical test records only after those checks exist. Never mark an untested OEM or physical metric as passed.


## ARM64 Android emulator integration/UI tests

- [ ] Instrumentation APK builds with AndroidJUnitRunner and Compose UI test support.
- [ ] API 30 ARM64 emulator boots on the Apple Silicon GitHub-hosted macOS runner.
- [ ] With Wi-Fi and mobile data disabled, the packaged model is provisioned and its SHA-256/GGUF checks pass.
- [ ] llama.cpp loads the actual packaged model, generates a non-empty local response and unloads cleanly.
- [ ] Model reload after unload succeeds.
- [ ] Compose assistant screen displays the Aktifkan AI and Batalkan controls.
- [ ] Emulator workflow is green for the exact current main commit.

**Boundary:** an ARM64 emulator result does not claim physical OEM, Android 8/API 26, sustained thermal, battery or screen-reader validation. Those remain separately tracked.
