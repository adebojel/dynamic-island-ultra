#include <jni.h>
#include <android/log.h>
#include <llama.h>
#include <algorithm>
#include <atomic>
#include <cstdint>
#include <ctime>
#include <mutex>
#include <string>
#include <vector>

namespace {
constexpr const char * TAG = "WicaraJNI";
std::mutex g_mutex;
std::atomic<bool> g_cancelled{false};
llama_model * g_model = nullptr;
bool g_backend_initialized = false;
bool abort_decode(void *) { return g_cancelled.load(std::memory_order_relaxed); }

jstring to_java_string(JNIEnv * env, const std::string & value) {
    std::vector<jchar> utf16;
    utf16.reserve(value.size());
    size_t i = 0;
    while (i < value.size()) {
        const auto first = static_cast<unsigned char>(value[i]);
        uint32_t cp = 0;
        size_t extra = 0;
        if (first < 0x80) cp = first;
        else if ((first & 0xE0) == 0xC0) { cp = first & 0x1F; extra = 1; }
        else if ((first & 0xF0) == 0xE0) { cp = first & 0x0F; extra = 2; }
        else if ((first & 0xF8) == 0xF0) { cp = first & 0x07; extra = 3; }
        else { utf16.push_back('?'); ++i; continue; }
        if (i + extra >= value.size()) { utf16.push_back('?'); break; }
        bool valid = true;
        for (size_t j = 1; j <= extra; ++j) {
            const auto next = static_cast<unsigned char>(value[i + j]);
            if ((next & 0xC0) != 0x80) { valid = false; break; }
            cp = (cp << 6) | (next & 0x3F);
        }
        if (!valid || cp > 0x10FFFF || (cp >= 0xD800 && cp <= 0xDFFF)) {
            utf16.push_back('?'); ++i; continue;
        }
        if (cp <= 0xFFFF) utf16.push_back(static_cast<jchar>(cp));
        else {
            cp -= 0x10000;
            utf16.push_back(static_cast<jchar>(0xD800 + (cp >> 10)));
            utf16.push_back(static_cast<jchar>(0xDC00 + (cp & 0x3FF)));
        }
        i += extra + 1;
    }
    return env->NewString(utf16.data(), static_cast<jsize>(utf16.size()));
}
std::string from_java_string(JNIEnv * env, jstring value) {
    if (value == nullptr) return {};
    const char * chars = env->GetStringUTFChars(value, nullptr);
    if (chars == nullptr) return {};
    std::string result(chars);
    env->ReleaseStringUTFChars(value, chars);
    return result;
}
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_adebojel_dynamicislandultra_ai_WicaraNative_nativeLoadModel(
        JNIEnv * env, jclass, jstring model_path, jint context_tokens, jint threads) {
    const std::string path = from_java_string(env, model_path);
    if (path.empty() || context_tokens <= 0 || threads <= 0) return JNI_FALSE;
    std::lock_guard<std::mutex> lock(g_mutex);
    if (g_model != nullptr) return JNI_TRUE;
    g_cancelled.store(false, std::memory_order_relaxed);
    if (!g_backend_initialized) { llama_backend_init(); g_backend_initialized = true; }
    llama_model_params params = llama_model_default_params();
    params.n_gpu_layers = 0;
    params.load_mode = LLAMA_LOAD_MODE_MMAP;
    g_model = llama_model_load_from_file(path.c_str(), params);
    if (g_model == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Could not load bundled GGUF model");
        return JNI_FALSE;
    }
    if (llama_model_n_ctx_train(g_model) <= 0) {
        llama_model_free(g_model);
        g_model = nullptr;
        return JNI_FALSE;
    }
    __android_log_print(ANDROID_LOG_INFO, TAG, "Wicara loaded using CPU mmap");
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_adebojel_dynamicislandultra_ai_WicaraNative_nativeGenerate(
        JNIEnv * env, jclass, jstring prompt_value, jint max_tokens) {
    const std::string prompt = from_java_string(env, prompt_value);
    if (prompt.empty()) return to_java_string(env, "ERROR:Prompt kosong.");
    if (prompt.size() > 12000) return to_java_string(env, "ERROR:Prompt terlalu panjang.");
    std::lock_guard<std::mutex> lock(g_mutex);
    if (g_model == nullptr) return to_java_string(env, "ERROR:Model Wicara belum aktif.");
    g_cancelled.store(false, std::memory_order_relaxed);

    llama_context_params cp = llama_context_default_params();
    const int32_t trained = llama_model_n_ctx_train(g_model);
    const uint32_t n_ctx = static_cast<uint32_t>(std::min<int32_t>(512, std::max<int32_t>(128, trained)));
    cp.n_ctx = n_ctx;
    cp.n_batch = n_ctx;
    cp.n_ubatch = std::min<uint32_t>(128, n_ctx);
    cp.n_threads = 2;
    cp.n_threads_batch = 2;
    cp.abort_callback = abort_decode;
    cp.abort_callback_data = nullptr;
    llama_context * ctx = llama_init_from_model(g_model, cp);
    if (ctx == nullptr) return to_java_string(env, "ERROR:Konteks inferensi tidak dapat dibuat.");

    const llama_vocab * vocab = llama_model_get_vocab(g_model);
    int32_t n_tokens = llama_tokenize(vocab, prompt.c_str(),
            static_cast<int32_t>(prompt.size()), nullptr, 0, true, true);
    if (n_tokens >= 0) {
        llama_free(ctx);
        return to_java_string(env, "ERROR:Ukuran token prompt tidak tersedia.");
    }
    n_tokens = -n_tokens;
    std::vector<llama_token> tokens(static_cast<size_t>(n_tokens) + 8);
    const int32_t actual = llama_tokenize(vocab, prompt.c_str(),
            static_cast<int32_t>(prompt.size()), tokens.data(),
            static_cast<int32_t>(tokens.size()), true, true);
    if (actual <= 0) {
        llama_free(ctx);
        return to_java_string(env, "ERROR:Prompt tidak dapat ditokenisasi.");
    }
    tokens.resize(static_cast<size_t>(actual));
    const int max_out = std::clamp<int>(max_tokens, 1, 96);
    const size_t prompt_limit = n_ctx > static_cast<uint32_t>(max_out + 8)
            ? n_ctx - static_cast<uint32_t>(max_out + 8) : n_ctx / 2;
    if (tokens.size() > prompt_limit)
        tokens.erase(tokens.begin(), tokens.end() - static_cast<std::ptrdiff_t>(prompt_limit));

    llama_batch batch = llama_batch_init(static_cast<int32_t>(tokens.size()), 0, 1);
    for (size_t i = 0; i < tokens.size(); ++i) {
        batch.token[i] = tokens[i];
        batch.pos[i] = static_cast<llama_pos>(i);
        batch.n_seq_id[i] = 1;
        batch.seq_id[i][0] = 0;
        batch.logits[i] = (i + 1 == tokens.size()) ? 1 : 0;
    }

    std::string output;
    llama_sampler * sampler = nullptr;
    if (llama_decode(ctx, batch) != 0) {
        output = g_cancelled.load() ? "Proses Wicara dibatalkan." : "ERROR:Prefill model gagal.";
    } else {
        sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
        if (sampler != nullptr) {
            // Match the upstream Wicara Modelfile sampling configuration.
            llama_sampler_chain_add(sampler, llama_sampler_init_penalties(
                    llama_vocab_n_tokens(vocab), 64, 1.15f, 0.0f, 0.0f));
            llama_sampler_chain_add(sampler, llama_sampler_init_top_k(40));
            llama_sampler_chain_add(sampler, llama_sampler_init_top_p(0.9f, 1));
            llama_sampler_chain_add(sampler, llama_sampler_init_temp(0.7f));
            llama_sampler_chain_add(sampler, llama_sampler_init_dist(static_cast<uint32_t>(std::time(nullptr))));
            int position = static_cast<int>(tokens.size());
            for (int step = 0; step < max_out && !g_cancelled.load(); ++step) {
                const llama_token token = llama_sampler_sample(sampler, ctx, -1);
                if (llama_vocab_is_eog(vocab, token)) break;
                llama_sampler_accept(sampler, token);
                char piece[512];
                int32_t bytes = llama_token_to_piece(vocab, token, piece, sizeof(piece), 0, true);
                if (bytes < 0) {
                    std::vector<char> expanded(static_cast<size_t>(-bytes) + 1);
                    bytes = llama_token_to_piece(vocab, token, expanded.data(),
                            static_cast<int32_t>(expanded.size()), 0, true);
                    if (bytes > 0) output.append(expanded.data(), static_cast<size_t>(bytes));
                } else if (bytes > 0) output.append(piece, static_cast<size_t>(bytes));
                const size_t end = output.find("<|end|>");
                if (end != std::string::npos) { output.resize(end); break; }
                if (step + 1 >= max_out || g_cancelled.load()) break;
                batch.token[0] = token;
                batch.pos[0] = position++;
                batch.n_seq_id[0] = 1;
                batch.seq_id[0][0] = 0;
                batch.logits[0] = 1;
                batch.n_tokens = 1;
                if (llama_decode(ctx, batch) != 0) break;
            }
        } else output = "ERROR:Sampler lokal tidak dapat dibuat.";
    }
    if (sampler != nullptr) llama_sampler_free(sampler);
    llama_batch_free(batch);
    llama_free(ctx);
    if (g_cancelled.load()) output = "Proses Wicara dibatalkan.";
    if (output.empty()) output = "Wicara belum menghasilkan jawaban. Coba pertanyaan lebih singkat.";
    return to_java_string(env, output);
}

extern "C" JNIEXPORT void JNICALL
Java_com_adebojel_dynamicislandultra_ai_WicaraNative_nativeCancel(JNIEnv *, jclass) {
    g_cancelled.store(true, std::memory_order_relaxed);
}

extern "C" JNIEXPORT void JNICALL
Java_com_adebojel_dynamicislandultra_ai_WicaraNative_nativeUnload(JNIEnv *, jclass) {
    g_cancelled.store(true, std::memory_order_relaxed);
    std::lock_guard<std::mutex> lock(g_mutex);
    if (g_model != nullptr) {
        llama_model_free(g_model);
        g_model = nullptr;
    }
    __android_log_print(ANDROID_LOG_INFO, TAG, "Wicara model unloaded");
}
