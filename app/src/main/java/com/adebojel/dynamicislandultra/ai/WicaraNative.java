package com.adebojel.dynamicislandultra.ai;

import android.os.Build;

/** JNI boundary for the CPU-only bundled llama.cpp runtime. */
final class WicaraNative {
    private static final boolean LIBRARY_LOADED;

    static {
        boolean loaded;
        try {
            System.loadLibrary("wicara_jni");
            loaded = true;
        } catch (UnsatisfiedLinkError | SecurityException error) {
            loaded = false;
        }
        LIBRARY_LOADED = loaded;
    }

    private WicaraNative() {}

    static boolean isSupported() {
        if (!LIBRARY_LOADED) return false;
        for (String abi : Build.SUPPORTED_ABIS) {
            if ("arm64-v8a".equals(abi)) return true;
            if ("armeabi-v7a".equals(abi)) return false;
        }
        return false;
    }

    static boolean isNativeLoaded() { return LIBRARY_LOADED; }

    static native boolean nativeLoadModel(String modelPath, int contextTokens, int threads);
    static native String nativeGenerate(String prompt, int maxTokens);
    static native void nativeCancel();
    static native void nativeUnload();
}
