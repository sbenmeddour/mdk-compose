//
// Created by Samy Benmeddour on 15/07/2024.
//

#include "utils.hpp"
#include <jni.h>

Utils &Utils::getInstance() {
    static Utils instance;
    return instance;
}

void Utils::setJavaVm(JavaVM *vm) {
    this->javaVm = vm;
}

JavaVM *Utils::getJavaVm() const {
    return this->javaVm;
}

JNIEnv *Utils::getEnv() const {
    auto jvm = this->javaVm;
    if (jvm == nullptr) return nullptr;
    JNIEnv *env = nullptr;
    auto result = jvm->GetEnv( (void**) &env, JNI_VERSION_1_6);
    switch (result) {
        case JNI_EDETACHED:
            jvm->AttachCurrentThread(&env, nullptr);
            break;
        default:
            LOG_INFO("Cannot acquire JNIEnv: [result=%d]", result);
    }
    return env;
}

std::vector<std::string> javaStringArray_toVector(JNIEnv* env, jobjectArray javaArray) {
    jsize arraySize = env->GetArrayLength(javaArray);
    std::vector<std::string> result;
    result.reserve(arraySize);
    for (jsize index = 0; index < arraySize; index++) {
        auto javaString = (jstring) env->GetObjectArrayElement(javaArray, index);
        auto cString = env->GetStringUTFChars(javaString, nullptr);
        result.emplace_back(cString);
        env->ReleaseStringUTFChars(javaString, cString);
        env->DeleteLocalRef(javaString);
    }
    return result;
}
