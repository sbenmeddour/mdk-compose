#include "jni.h"
#include <vector>
#include <string>
#include <android/log.h>

#define LOG_TAG "mdk-jni"
#define LOG_INFO(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOG_ERROR(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#ifndef MDK_COMPOSE_UTILS_HPP
#define MDK_COMPOSE_UTILS_HPP

class Utils {
public:
    static Utils& getInstance();
    void setJavaVm(JavaVM* vm);
    JavaVM* getJavaVm() const;
    JNIEnv* getEnv() const;
private:
    Utils() : javaVm(nullptr) {}
    ~Utils() = default;
    Utils(const Utils&) = delete;
    Utils& operator=(const Utils&) = delete;
    JavaVM* javaVm;
};

std::vector<std::string> javaStringArray_toVector(JNIEnv* env, jobjectArray javaArray);

#endif //MDK_COMPOSE_UTILS_HPP
