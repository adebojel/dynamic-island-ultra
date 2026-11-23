# Third-party license notices

## Wicara model

- Source: https://huggingface.co/num1notsvn/wicara-56m-chat-GGUF
- Artifact: wicara-56m-chat-q8_0.gguf
- Upstream model card declares Apache License 2.0.
- Build revision and SHA-256 are recorded in generated release-info.json.

## llama.cpp

- Source: https://github.com/ggml-org/llama.cpp
- Pinned revision: 2d8a32502909ca065c41c4cf5f2fb1c427c047fc
- License: MIT License.
- Copyright: ggml authors, 2023–2026.
- Upstream license text: https://github.com/ggml-org/llama.cpp/blob/2d8a32502909ca065c41c4cf5f2fb1c427c047fc/LICENSE

The upstream notices must be preserved with distributions. The workflow fetches llama.cpp source to the build workspace and does not vendor its source files into this repository. The model weights are downloaded at build time and are not committed to Git.
