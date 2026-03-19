#include <jni.h>
#include <string>
#include <thread>
#include <chrono>
#include <android/log.h>

static int counter = 0;

extern "C" JNIEXPORT jint JNICALL
Java_com_ab_couterexamplewithndk_data_repository_CounterRepositoryImpl_nativeIncrementCount(
        JNIEnv* env,
        jobject thisz) {
    // increment counter
    counter++;
    // Delay a bit
    std::this_thread::sleep_for(std::chrono::milliseconds(3000));
    // reply back with updated count
    return counter;
}

extern "C" JNIEXPORT void JNICALL
Java_com_ab_couterexamplewithndk_data_repository_CounterRepositoryImpl_nativeResetCount(
        JNIEnv* env,
jobject thisz) {
counter = 0;
}