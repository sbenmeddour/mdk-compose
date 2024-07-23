#ifndef MDK_COMPOSE_UTILS_HPP
#define MDK_COMPOSE_UTILS_HPP


#include "jni.h"
#include <vector>
#include <string>
#include <android/log.h>
#include <mdk/Player.h>
#include "java_listener.hpp"

#define LOG_TAG "mdk-jni"
#define MDK_LOG_TAG "mdk-log"
#define LOG_INFO(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define MDK_LOG(...) __android_log_print(ANDROID_LOG_INFO, MDK_LOG_TAG, __VA_ARGS__)
#define LOG_ERROR(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

class Utils {
public:

    Utils(const Utils&) = delete;
    Utils& operator=(const Utils&) = delete;

    static void setJavaVm(JavaVM* vm);
    static JavaVM* getJavaVm();
    static JNIEnv* getEnv();

    static jobject jniMediaInfo(JNIEnv* env, mdk::MediaInfo& mediaInfo);
    static void onState(jobject listener, mdk::State state);
    static void onMediaStatus(jobject listener, mdk::MediaStatus previous, mdk::MediaStatus next);
    static void onEvent(jobject listener, mdk::MediaEvent event);
    static void onLoop(jobject listener, jlong value);
    static void onCurrentMediaChanged(jobject listener);
    static std::vector<std::string> arrayToVector(JNIEnv* env, jobjectArray array);
    static std::set<int> arrayToSet(JNIEnv* env, jintArray array);

    static JavaListener* onStateChangedHelper();
    static JavaListener* onEventHelper();
    static JavaListener* onLoopHelper();
    static JavaListener* onStatusHelper();
    static JavaListener* onCurrentMediaHelper();

    static jobject listenerAndToken(jlong listener, jlong token);


private:
    JavaVM* vm;
    JavaListener* _onStateChangedHelper;
    JavaListener* _onEventHelper;
    JavaListener* _onLoopHelper;
    JavaListener* _onStatusHelper;
    JavaListener* _onCurrentMediaHelper;
    static Utils& instance() {
        static Utils singleton;
        return singleton;
    }
    Utils() {
        this->vm = nullptr;
        this->_onStateChangedHelper = nullptr;
        this->_onEventHelper = nullptr;
        this->_onLoopHelper = nullptr;
        this->_onStatusHelper = nullptr;
        this->_onCurrentMediaHelper = nullptr;
    }

};

jobject buildJavaMediaInfo(JNIEnv* env, const mdk::MediaInfo& mediaInfo);

#endif //MDK_COMPOSE_UTILS_HPP
