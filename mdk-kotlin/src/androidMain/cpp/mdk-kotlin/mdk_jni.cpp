#include "player_wrapper.hpp"
#include "utils.hpp"
#include <jni.h>
#include <future>
#include "thread_queue.hpp"

using namespace mdk;



extern "C" {

    JNIEXPORT jint JNICALL
    JNI_OnLoad(JavaVM* vm, void* reserved) {
        Utils::getInstance().setJavaVm(vm);
        MDK_setGlobalOptionPtr("JavaVM", vm);
        return JNI_VERSION_1_6;
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_base_MdkLibrary_setGlobalOptionChar(
        JNIEnv *env,
        jobject thiz,
        jstring name,
        jchar value
    ) {
        auto key = env->GetStringUTFChars(name, nullptr);
        mdk::SetGlobalOption(key, value);
        env->ReleaseStringUTFChars(name, key);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_base_MdkLibrary_setGlobalOptionString(
        JNIEnv *env,
        jobject thiz,
        jstring name,
        jstring value
    ) {
        auto key = env->GetStringUTFChars(name, nullptr);
        auto cString = value == nullptr ? nullptr : env->GetStringUTFChars(value, nullptr);
        mdk::SetGlobalOption(key, cString);
        env->ReleaseStringUTFChars(name, key);
        if (cString != nullptr) env->ReleaseStringUTFChars(value, cString);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_base_MdkLibrary_setGlobalOptionInt(
        JNIEnv *env,
        jobject thiz,
        jstring name,
        jint value
    ) {
        auto key = env->GetStringUTFChars(name, nullptr);
        mdk::SetGlobalOption(key, value);
        env->ReleaseStringUTFChars(name, key);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_base_MdkLibrary_setGlobalOptionFloat(
        JNIEnv *env,
        jobject thiz,
        jstring name,
        jfloat value
    ) {
        auto key = env->GetStringUTFChars(name, nullptr);
        mdk::SetGlobalOption(key, value);
        env->ReleaseStringUTFChars(name, key);
    }

    JNIEXPORT jint JNICALL
    Java_fr_dcs_mdk_base_MdkLibrary_getGlobalOptionInt(
            JNIEnv *env,
            jobject thiz,
            jstring name
    ) {
        auto key = env->GetStringUTFChars(name, nullptr);
        int value;
        auto success = mdk::GetGlobalOption(key, &value);
        env->ReleaseStringUTFChars(name, key);
        if (!success) return 0;
        return value;
    }
    
    JNIEXPORT jstring JNICALL
    Java_fr_dcs_mdk_base_MdkLibrary_getGlobalOptionString(
            JNIEnv *env,
            jobject thiz,
            jstring name
    ) {
        auto key = env->GetStringUTFChars(name, nullptr);
        const char* value = nullptr;
        auto success = mdk::GetGlobalOption(key, &value);
        env->ReleaseStringUTFChars(name, key);
        if (!success) return nullptr;
        if (value == nullptr) return nullptr;
        auto javaResult = env->NewStringUTF(value);
        return javaResult;
    }

    JNIEXPORT jlong JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_createWrapper(
        JNIEnv* env,
        jobject thiz,
        jobject listener
    ) {
        LOG_INFO("Create wrapper called");
        auto wrapper = new PlayerWrapper(env, listener);
        return reinterpret_cast<jlong>(wrapper);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_setAudioBackends(
        JNIEnv* env,
        jobject thiz,
        jlong handle,
        jobjectArray values
    ) {
        LOG_INFO("setAudioBackends");
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting setAudioBackends: playerWrapper is null");
            return;
        }
        auto names = javaStringArray_toVector(env, values);
        for (const auto& name : names) {
            LOG_INFO("setAudioBackends found: %s", name.c_str());
        }
        playerWrapper->getMdkPlayer()->setAudioBackends(names);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_setDecoders(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jint mediaType,
        jobjectArray values
    ) {
        LOG_INFO("setDecoders: [type=%d]", mediaType);
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting setDecoders: playerWrapper is null");
            return;
        }
        auto names = javaStringArray_toVector(env, values);
        for (const auto& name : names) {
            LOG_INFO("setDecoders found: %s", name.c_str());
        }
        playerWrapper->getMdkPlayer()->setDecoders((mdk::MediaType) mediaType, names);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_setState(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jint value
    ) {
        LOG_INFO("setState: [value=%d]", value);
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting setState: playerWrapper is null");
            return;
        }
        playerWrapper->getMdkPlayer()->set( (mdk::State) value);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_setMedia(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jstring url
    ) {
        LOG_INFO("setMedia");
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting setMedia: playerWrapper is null");
            return;
        }
        auto cString = env->GetStringUTFChars(url, nullptr);
        LOG_INFO("setMedia: [url=%s]", cString);
        playerWrapper->getMdkPlayer()->setMedia(cString);
        env->ReleaseStringUTFChars(url, cString);
    }

    JNIEXPORT jobject JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_prepare(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jlong start_position,
        jint flags,
        jboolean unloadImmediately
    ) {
        LOG_INFO("prepare: [start_position=%lld, flags=%d, unloadImmediately=%d]", start_position, flags, unloadImmediately);
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting prepare: playerWrapper is null");
            return nullptr;
        }
        std::promise<mdk::MediaInfo> promise;
        std::future<mdk::MediaInfo> future = promise.get_future();
        playerWrapper->getMdkPlayer()->prepare(
            /* startPosition = */start_position,
            /* cb = */ [playerWrapper, unloadImmediately, &promise] (int64_t position, bool* boost) {
                LOG_INFO("prepare callback: [position=%lld]", position);
                auto mediaInfo = playerWrapper->getMdkPlayer()->mediaInfo();
                promise.set_value(mediaInfo);
                return !unloadImmediately;
            },
            /* flags = */(mdk::SeekFlag) flags
        );
        auto result = future.get();
        return buildJavaMediaInfo(env, result);
    }

    JNIEXPORT jobject JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_getMediaInfo(
        JNIEnv *env,
        jobject thiz,
        jlong handle
    ) {
        LOG_INFO("getMediaInfo");
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting getMediaInfo: playerWrapper is null");
            return nullptr;
        }
        auto result = playerWrapper->getMdkPlayer()->mediaInfo();
        return buildJavaMediaInfo(env, result);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_setFrameRate(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jfloat value
    ) {
        LOG_INFO("setFrameRate: [value=%f]", value);
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting setFrameRate: playerWrapper is null");
            return;
        }
        playerWrapper->getMdkPlayer()->setFrameRate(value);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_setTrack(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jint type,
        jint index
    ) {
        LOG_INFO("setTrack: [type=%d, index=%d]", type, index);
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting setTrack: playerWrapper is null");
            return;
        }
        auto tracks = std::set<int>();
        if (index >= 0) tracks.insert(index);
        playerWrapper->getMdkPlayer()->setActiveTracks( (mdk::MediaType) type, tracks);
    }

    JNIEXPORT jfloat JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_setPlaybackRate(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jfloat rate
    ) {
        LOG_INFO("setPlaybackRate: [rate=%f]", rate);
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting setPlaybackRate: playerWrapper is null");
            return -1;
        }
        playerWrapper->getMdkPlayer()->setPlaybackRate(rate);
        return playerWrapper->getMdkPlayer()->playbackRate();
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_setBufferRange(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jlong min,
        jlong max,
        jboolean drop
    ) {
        LOG_INFO("setBufferRange: [min=%lld, max=%lld, drop=%d]", min, max, drop);
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting setBufferRange: playerWrapper is null");
            return;
        }
        playerWrapper->getMdkPlayer()->setBufferRange(min, max, drop);
    }

    JNIEXPORT jfloat JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_setVolume(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jfloat value
    ) {
        LOG_INFO("setVolume: [value=%f]", value);
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting setVolume: playerWrapper is null");
            return -1;
        }
        playerWrapper->getMdkPlayer()->setVolume(value);
        return playerWrapper->getMdkPlayer()->volume();
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_seek(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jint flags,
        jlong position
    ) {
        LOG_INFO("seek: [flags=%d, position=%lld]", flags, position);
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting seek: playerWrapper is null");
            return;
        }
        playerWrapper->getMdkPlayer()->seek(position, (mdk::SeekFlag) flags, nullptr);
    }

    JNIEXPORT jlong JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_seekAsync(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jint flags,
        jlong position
    ) {
        LOG_INFO("seekAsync: [flags=%d, position=%lld]", flags, position);
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting seekAsync: playerWrapper is null");
            return -1;
        }
        std::promise<int64_t> promise;
        std::future<int64_t> future = promise.get_future();
        playerWrapper->getMdkPlayer()->seek(
            /* pos = */ position,
            /* flags = */ (mdk::SeekFlag) flags,
            /* cb = */ [&promise] (int64_t result) {
                LOG_INFO("seekAsync, callback: [result=%lld]", result);
                promise.set_value(result);
            }
        );
        return future.get();
    }

    JNIEXPORT jboolean JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_setIsMuted(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jboolean is_muted
    ) {
        LOG_INFO("setIsMuted: [value=%hhu]", is_muted);
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting setIsMuted: playerWrapper is null");
            return false;
        }
        playerWrapper->getMdkPlayer()->setMute(is_muted);
        return playerWrapper->getMdkPlayer()->isMute();
    }

    JNIEXPORT jlong JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_setSurface(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jobject surface,
        jint width,
        jint height
    ) {
        LOG_INFO("setSurface: [surface=%lld, width=%d, height=%d]", reinterpret_cast<jlong>(surface), width, height);
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting setSurface: playerWrapper is null");
            return 0L;
        }
        if (surface == nullptr) {
            return 0;
        }
        playerWrapper->getMdkPlayer()->updateNativeSurface(
            /* surface = */ surface,
            /* width = */ width,
            /* height = */ height,
            /* type = */ mdk::Player::SurfaceType::Auto
        );
        return reinterpret_cast<jlong>(surface);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_resizeSurface(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jint width,
        jint height
    ) {
        LOG_INFO("resizeSurface: [width=%d, height=%d]", width, height);
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting resizeSurface: playerWrapper is null");
            return;
        }
        playerWrapper->getMdkPlayer()->resizeSurface(width, height);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_release(
        JNIEnv *env,
        jobject thiz,
        jlong handle
    ) {
        LOG_INFO("release");
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            LOG_ERROR("Aborting release: playerWrapper is null");
            return;
        }
        delete playerWrapper;
    }

    JNIEXPORT jlong JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_getPosition(
        JNIEnv *env,
        jobject thiz,
        jlong handle
    ) {
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            return 0;
        }
        return playerWrapper->getMdkPlayer()->position();
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_setProperty(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jstring key,
        jstring value
    ) {
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            return;
        }
        auto cKey = env->GetStringUTFChars(key, nullptr);
        auto cValue = env->GetStringUTFChars(value, nullptr);
        playerWrapper->getMdkPlayer()->setProperty(cKey, cValue);
        env->ReleaseStringUTFChars(key, cKey);
        env->ReleaseStringUTFChars(value, cValue);
    }

    JNIEXPORT jstring JNICALL
    Java_fr_dcs_mdk_jni_JNIPlayer_getProperty(
        JNIEnv *env,
        jobject thiz,
        jlong handle,
        jstring key
    ) {
        auto playerWrapper = reinterpret_cast<PlayerWrapper*>(handle);
        if (playerWrapper == nullptr) {
            return nullptr;
        }
        auto cKey = env->GetStringUTFChars(key, nullptr);
        auto result = playerWrapper->getMdkPlayer()->property(cKey);
        env->ReleaseStringUTFChars(key, cKey);
        auto javaResult = env->NewStringUTF(result.c_str());
        return javaResult;
    }

}
