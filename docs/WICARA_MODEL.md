# Wicara offline model

## Artifact provenance

- Upstream: https://huggingface.co/num1notsvn/wicara-56m-chat-GGUF
- File: wicara-56m-chat-q8_0.gguf, GGUF Q8_0.
- Upstream model card declares Apache-2.0. The GGUF page's parameter-count display differs from the original model card; this project avoids marketing an unverified exact parameter count.
- Runtime: llama.cpp pinned to 2d8a32502909ca065c41c4cf5f2fb1c427c047fc (v1.0.0 source line), MIT license.
- CI resolves a Hugging Face immutable revision for each build, then downloads from that exact revision and verifies upstream LFS SHA-256 and byte size.
- Model revision, size and SHA-256 are placed into the app asset metadata and release-info.json. Weights are not committed to Git.

## Packaging and integrity

CI rejects a missing or malformed model revision, an upstream license mismatch, missing LFS SHA-256, downloaded size/hash mismatch, invalid GGUF magic, or model larger than 100,000,000 bytes. The GGUF is packaged uncompressed as an app asset. On first activation, Wicara copies it into app-private noBackupFilesDir, rechecks size/header/SHA-256, and llama.cpp memory maps that private copy. There is no file picker, manual download, API key, user account, or cloud AI fallback.

CI checks APK/AAB size plus a second model-size allowance against the 400 MiB delivery budget to reserve room for the app-private copy.

## Runtime and lifecycle

- ARM64 (arm64-v8a) builds llama.cpp via Android NDK/CMake and executes inference on CPU.
- ARMv7 (armeabi-v7a) includes an explicit unsupported-runtime stub; other application features remain packaged and available.
- Context is capped at 512 tokens, generation at 96 tokens and native CPU threads at two.
- Prompt format uses Wicara role/end markers: '<|system|>', '<|user|>', '<|assistant|>' and '<|end|>'.
- Deterministic arithmetic, Android system actions and diagnostics are routed before the LLM.
- A Kotlin mutex and native mutex serialize model lifecycle and inference. Native cancellation uses llama.cpp's CPU decode-abort callback. Leaving the assistant screen requests unload.
- Wicara has no network client. Browser, ads and updater are separate app features.

## Limitations

This is a very small experimental language model, not a reliable reasoning engine. It may return incorrect facts, calculations, instructions or context-sensitive output. Use deterministic calculator/system functions for actions and verify important information independently. Do not rely on it for medical, legal, financial, emergency or accessibility-critical decisions.

## Physical validation

CI verifies source/build integration, packaging, model metadata, size/hash and unit tests. CI cannot measure real-device tokens/second, RAM/PSS, temperature, thermal throttling, battery drain or OEM stability. Use docs/MODEL_BENCHMARK_PROTOCOL.md and record raw evidence on physical devices.
