#include <jni.h>
extern "C" JNIEXPORT jboolean JNICALL
Java_com_adebojel_dynamicislandultra_ai_WicaraNative_nativeLoadModel(JNIEnv *, jclass, jstring, jint, jint) {
    return JNI_FALSE;
}
extern "C" JNIEXPORT jstring JNICALL
Java_com_adebojel_dynamicislandultra_ai_WicaraNative_nativeGenerate(JNIEnv * env, jclass, jstring, jint) {
    return env->NewStringUTF("ERROR:Runtime Wicara generatif hanya tersedia pada perangkat ARM64.");
}
extern "C" JNIEXPORT void JNICALL
Java_com_adebojel_dynamicislandultra_ai_WicaraNative_nativeCancel(JNIEnv *, jclass) {}
extern "C" JNIEXPORT void JNICALL
Java_com_adebojel_dynamicislandultra_ai_WicaraNative_nativeUnload(JNIEnv *, jclass) {}
