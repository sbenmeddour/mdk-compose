#include <jni.h>
#include <mdk/Player.h>
#include <future>
#include "utils.hpp"
#include "prepare_exception.hpp"
#include <iostream>
#include <stdexcept>

using namespace mdk;


struct PlayerReference {
    ~PlayerReference() {
        delete this->player;
    }
    PlayerReference() {
        this->player = new Player();
    }
    mdk::Player* player;
};

extern "C" {

    JNIEXPORT jint JNICALL
    JNI_OnLoad(JavaVM* vm, void* reserved) {
        Utils::setJavaVm(vm);
        MDK_setGlobalOptionPtr("JavaVM", Utils::getJavaVm());
        MDK_setGlobalOptionPtr("jvm", Utils::getJavaVm());
        MDK_setLogLevel(MDK_LogLevel_All);
        SetGlobalOption("logLevel", "all");
        setLogHandler(
            [] (const LogLevel level, const char* value) {
                //if (level >= LogLevel::Warning) return;
                MDK_LOG("[level=%d, message=%s]", level, value);
            }
        );
        return JNI_VERSION_1_6;
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_base_MdkLibrary_setGlobalOptionChar(
            JNIEnv *env,
            jobject thiz,
            jstring name,
            jchar value
    ) {
        LOG_INFO(__PRETTY_FUNCTION__);
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
    Java_fr_dcs_mdk_jni_Mdk_newInstance(
        JNIEnv *env,
        jobject thiz
    ) {
        LOG_INFO("JNI call: newInstance");
        auto ref = new PlayerReference();
        return jlong(ref);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_deleteInstance(
        JNIEnv *env,
        jobject thiz,
        jlong player
    ) {
        LOG_INFO("JNI call: deleteInstance: [player=%lld]", player);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        delete reference;
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setMute(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jboolean value
    ) {
        LOG_INFO("JNI call: setMute: [value=%d]", value);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->setMute(value);
    }

    JNIEXPORT jboolean JNICALL
    Java_fr_dcs_mdk_jni_Mdk_isMuted(
        JNIEnv *env,
        jobject thiz,
        jlong player
    ) {
        LOG_INFO("JNI call: isMuted");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return false;
        return mdkPlayer->isMute();
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setVolume(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jfloat value
    ) {
        LOG_INFO("JNI call: setVolume: [value=%f]", value);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->setVolume(value);
    }

    JNIEXPORT jfloat JNICALL
    Java_fr_dcs_mdk_jni_Mdk_getVolume(
        JNIEnv *env,
        jobject thiz,
        jlong player
    ) {
        LOG_INFO("JNI call: getVolume");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return 0.0;
        return mdkPlayer->volume();
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setFrameRate(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jfloat value
    ) {
        LOG_INFO("JNI call: setFrameRate: [value=%f]", value);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->setFrameRate(value);
    }


    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setMedia(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jstring url,
        jint type
    ) {
        LOG_INFO("JNI call: setMedia: [player=%lld, type=%d]", player, type);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto cUrl = env->GetStringUTFChars(url, nullptr);
        LOG_INFO("JNI call: setMedia: [url=%s]", cUrl);
        mdkPlayer->setMedia(cUrl);
        env->ReleaseStringUTFChars(url, cUrl);
    }

    JNIEXPORT jstring JNICALL
    Java_fr_dcs_mdk_jni_Mdk_getUrl(
        JNIEnv *env,
        jobject thiz,
        jlong player
    ) {
        LOG_INFO("JNI call: getUrl");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return nullptr;
        auto url = mdkPlayer->url();
        if (url == nullptr) return nullptr;
        return env->NewStringUTF(url);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setPreloadImmediately(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jboolean value
    ) {
        LOG_INFO("JNI call: setPreloadImmediately: [player=%lld, value=%d]", player, value);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->setPreloadImmediately(value);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setNextMedia(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jstring value,
        jlong start_position,
        jint flags
    ) {
        LOG_INFO("JNI call: setNextMedia: [player=%lld, start_position=%lld, flags=%d]", player, start_position, flags);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto cUrl = env->GetStringUTFChars(value, nullptr);
        LOG_INFO("JNI call: setNextMedia: [url=%s]", cUrl);
        auto seekFlags = static_cast<mdk::SeekFlag>(flags);
        mdkPlayer->setNextMedia(cUrl, start_position, seekFlags);
        env->ReleaseStringUTFChars(value, cUrl);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setActiveTracks(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jint type,
        jintArray values
    ) {
        LOG_INFO("JNI call: setActiveTracks: [player=%lld, type=%d]", player, type);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto mdkType = static_cast<mdk::MediaType>(type);
        auto array = Utils::arrayToSet(env, values);
        mdkPlayer->setActiveTracks(mdkType, array);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setAudioBackends(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jobjectArray values
    ) {
        LOG_INFO("JNI call: setAudioBackends: [player=%lld]", player);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto vector = Utils::arrayToVector(env, values);
        mdkPlayer->setAudioBackends(vector);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setDecoders(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jint type,
        jobjectArray values
    ) {
        LOG_INFO("JNI call: setDecoders: [player=%lld, type=%d]", player, type);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto vector = Utils::arrayToVector(env, values);
        auto mdkType = static_cast<mdk::MediaType>(type);
        mdkPlayer->setDecoders(mdkType, vector);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setTimeout(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jlong value,
        jboolean abortOperation
    ) {
        LOG_INFO("JNI call: setTimeout: [player=%lld, value=%lld, abortOperation=%d]", player, value, abortOperation);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto promise = std::promise<int64_t>();
        auto future = promise.get_future();
        std::function<bool(int64_t ms)> callback = [abortOperation, &promise] (const int64_t elapsedTime) {
            promise.set_value(elapsedTime);
            return abortOperation;
        };
        mdkPlayer->setTimeout(value, callback);
        auto result = future.get();
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setState(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jint value
    ) {
        LOG_INFO("JNI call: setState: [player=%lld, value=%d]", player, value);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto mdkState = static_cast<mdk::State>(value);
        mdkPlayer->set(mdkState);
    }

    JNIEXPORT jint JNICALL
    Java_fr_dcs_mdk_jni_Mdk_getState(
        JNIEnv *env,
        jobject thiz,
        jlong player
    ) {
        LOG_INFO("JNI call: getState: [player=%lld]", player);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return static_cast<jint>(mdk::PlaybackState::NotRunning);
        return static_cast<jint>(mdkPlayer->state());
    }

    JNIEXPORT jint JNICALL
    Java_fr_dcs_mdk_jni_Mdk_getMediaStatus(
        JNIEnv *env,
        jobject thiz,
        jlong player
    ) {
        LOG_INFO("JNI call: getMediaStatus: [player=%lld]", player);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return static_cast<jint>(mdk::MediaStatus::Invalid);
        return static_cast<jint>(mdkPlayer->mediaStatus());
    }

    JNIEXPORT jlong JNICALL
    Java_fr_dcs_mdk_jni_Mdk_getPosition(
        JNIEnv *env,
        jobject thiz,
        jlong player
    ) {
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return 0;
        return mdkPlayer->position();
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_seek(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jlong position,
        jint flags
    ) {
        LOG_INFO("JNI call: seek: [player=%lld, position=%lld, flags=%d]", player, position, flags);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto seekFlags = static_cast<mdk::SeekFlag>(flags);
        mdkPlayer->seek(position, seekFlags, nullptr);
    }

    JNIEXPORT jlong JNICALL
    Java_fr_dcs_mdk_jni_Mdk_seekAwait(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jlong position,
        jint flags
    ) {
        LOG_INFO("JNI call: seekAwait: [player=%lld, position=%lld, flags=%d]", player, position, flags);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return -1;
        auto seekFlags = static_cast<mdk::SeekFlag>(flags);
        std::promise<int64_t> promise;
        auto future = promise.get_future();
        std::function<void(int64_t)> callback = [&promise] (const int64_t position) {
            promise.set_value(position);
        };
        mdkPlayer->seek(position, seekFlags, callback);
        return future.get();
    }

    JNIEXPORT jboolean JNICALL
    Java_fr_dcs_mdk_jni_Mdk_waitFor(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jint state,
        jlong timeout
    ) {
        LOG_INFO("JNI call: waitFor: [player=%lld, state=%d, timeout=%lld]", player, state, timeout);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return -1;
        auto mdkState = static_cast<mdk::State>(state);
        auto cTimeout = static_cast<long>(timeout);
        return mdkPlayer->waitFor(mdkState, cTimeout);
    }

    JNIEXPORT jstring JNICALL
    Java_fr_dcs_mdk_jni_Mdk_getProperty(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jstring key
    ) {
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return nullptr;
        auto cKey = env->GetStringUTFChars(key, nullptr);
        auto cProperty = mdkPlayer->property(cKey);
        LOG_INFO("JNI call: getProperty: [key=%s] -> %s", cKey, cProperty.data());
        env->ReleaseStringUTFChars(key, cKey);
        auto result = env->NewStringUTF(cProperty.data());
        return result;
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setProperty(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jstring key,
        jstring value
    ) {
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto cKey = env->GetStringUTFChars(key, nullptr);
        auto cValue = value == nullptr ? nullptr : env->GetStringUTFChars(value, nullptr);
        mdkPlayer->setProperty(cKey, cValue);
        env->ReleaseStringUTFChars(key, cKey);
        env->ReleaseStringUTFChars(value, cValue);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_showSurface(
        JNIEnv *env,
        jobject thiz,
        jlong player
    ) {
        LOG_INFO("JNI call: showSurface: [player=%lld]", player);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->showSurface();
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_resizeSurface(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jint width,
        jint height
    ) {
        LOG_INFO("JNI call: resizeSurface: [player=%lld, width=%d, height=%d]", player, width, height);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->resizeSurface(width, height);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setBufferRange(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jlong min,
        jlong max,
        jboolean drop
    ) {
        LOG_INFO("JNI call: setBufferRange: [player=%lld, min=%lld, max=%lld, drop=%d]", player, min, max, drop);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->setBufferRange(min, max, drop);
    }

    JNIEXPORT jboolean JNICALL
    Java_fr_dcs_mdk_jni_Mdk_switchBitrate(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jstring url,
        jlong delay
    ) {
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return false;
        auto cUrl = env->GetStringUTFChars(url, nullptr);
        auto promise = std::promise<bool>();
        auto future = promise.get_future();
        const std::function<void(bool)> callback = [&promise] (bool result) {
            promise.set_value(result);
        };
        mdkPlayer->switchBitrate(cUrl, delay, callback);
        env->ReleaseStringUTFChars(url, cUrl);
        return future.get();
    }

    JNIEXPORT jboolean JNICALL
    Java_fr_dcs_mdk_jni_Mdk_switchBitrateSingleConnection(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jstring url
    ) {
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return false;
        auto cUrl = env->GetStringUTFChars(url, nullptr);
        auto promise = std::promise<bool>();
        auto future = promise.get_future();
        const std::function<void(bool)> callback = [&promise] (bool result) {
            promise.set_value(result);
        };
        mdkPlayer->switchBitrateSingleConnection(cUrl, callback);
        env->ReleaseStringUTFChars(url, cUrl);
        return future.get();
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setLoop(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jint count
    ) {
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->setLoop(count);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_scale(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jfloat x,
        jfloat y,
        jobject opaque
    ) {
        auto mdk = reinterpret_cast<mdk::Player*>(player);
        if (mdk == nullptr) return;
        mdk->scale(x, y, opaque);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_rotate(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jint degree,
        jobject opaque
    ) {
        auto mdk = reinterpret_cast<mdk::Player*>(player);
        if (mdk == nullptr) return;
        mdk->rotate(degree, opaque);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setAspectRatio(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jfloat value,
        jobject opaque
    ) {
        auto mdk = reinterpret_cast<mdk::Player*>(player);
        if (mdk == nullptr) return;
        mdk->setAspectRatio(value, opaque);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setPlaybackRate(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jfloat value
    ) {
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->setPlaybackRate(value);
    }

    JNIEXPORT jfloat JNICALL
    Java_fr_dcs_mdk_jni_Mdk_getPlaybackRate(
        JNIEnv *env,
        jobject thiz,
        jlong player
    ) {
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return 0;
        return mdkPlayer->playbackRate();
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setVideoViewport(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jfloat x,
        jfloat y,
        jfloat width,
        jfloat height,
        jobject opaque
    ) {
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->setVideoViewport(x, y, width, height, opaque);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_setVideoSurfaceSize(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jint width,
        jint height,
        jobject opaque
    ) {
        LOG_INFO("JNI call: setVideoSurfaceSize: [player=%lld, width=%d, height=%d, opaque=%lld]", player, width, height, reinterpret_cast<jlong>(opaque));
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->setVideoSurfaceSize(width, height, opaque);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_createSurface(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jobject surface,
        jint type
    ) {
        LOG_INFO("JNI call: createSurface: [player=%lld, opaque=%lld, type=%d]", player, reinterpret_cast<jlong>(surface), type);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->createSurface(nullptr, mdk::Player::SurfaceType::Auto);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_updateNativeSurface(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jobject surface,
        jint width,
        jint height
    ) {
        LOG_INFO("JNI call: updateNativeSurface: [player=%lld, surface=%lld, width=%d, height=%d]", player, reinterpret_cast<jlong>(surface), width, height);
        LOG_INFO("zgeg");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->updateNativeSurface(surface, width, height, mdk::Player::SurfaceType::Auto);
    }

    JNIEXPORT jobject JNICALL
    Java_fr_dcs_mdk_jni_Mdk_prepare(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jlong position,
        jint flags,
        jboolean unload
    ) {
        LOG_INFO("JNI call: prepare: [player=%lld, position=%lld, flags=%d, unload=%d]", player, position, flags, unload);
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return nullptr;
        auto seekFlags = static_cast<mdk::SeekFlag>(flags);
        auto promise = std::promise<mdk::MediaInfo>();
        auto future = promise.get_future();
        mdkPlayer->prepare(
            /* startPosition = */ position,
            /* cb = */ [unload, mdkPlayer, &promise] (const int64_t position, bool* boost) {
                LOG_INFO("Prepare callback result: %lld", position);
                try {
                    if (position < 0) {
                        throw PrepareException(position);
                    } else {
                        auto mediaInfo = mdkPlayer->mediaInfo();
                        promise.set_value(mediaInfo);
                    }
                } catch (...) {
                    auto exception = std::current_exception();
                    promise.set_exception(exception);
                }
                return unload;
            },
            /* flags = */ static_cast<SeekFlag>(flags)
        );

        try {
            auto result = future.get();
            return Utils::jniMediaInfo(env, result);
        } catch (const PrepareException& exception) {
            auto exceptionClazz = env->FindClass("fr/dcs/mdk/jni/Mdk$PrepareException");
            auto constructor = env->GetMethodID(exceptionClazz, "<init>", "(J)V");
            auto javaException = (jthrowable) env->NewObject(exceptionClazz, constructor, exception.code);
            env->Throw(javaException);
            return nullptr;
        }
    }

    JNIEXPORT jobject JNICALL
    Java_fr_dcs_mdk_jni_Mdk_getMediaInfo(
        JNIEnv *env,
        jobject thiz,
        jlong player
    ) {
        LOG_INFO("JNI call: getMediaInfo");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return nullptr;
        auto mediaInfo = mdkPlayer->mediaInfo();
        return Utils::jniMediaInfo(env, mediaInfo);
    }

    JNIEXPORT jobject JNICALL
    Java_fr_dcs_mdk_jni_Mdk_onMediaStatusChanged(
        JNIEnv* env,
        jobject thiz,
        jlong player,
        jobject listener
    ) {
        LOG_INFO("JNI call: onMediaStatusChanged");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return nullptr;
        auto listenerGlobalRef = env->NewGlobalRef(listener);
        auto listenerAddress = reinterpret_cast<jlong>(listenerGlobalRef);

        mdk::CallbackToken* newCallbackToken = nullptr;
        auto callbackFunction = [listenerAddress] (const mdk::MediaStatus previous, const mdk::MediaStatus next) {
            auto javaListener = reinterpret_cast<jobject>(listenerAddress);
            Utils::onMediaStatus(javaListener, previous, next);
            return false;
        };
        mdkPlayer->onMediaStatus(callbackFunction, newCallbackToken);
        return Utils::listenerAndToken(
            /* listener = */ listenerAddress,
            /* token = */ reinterpret_cast<jlong>(newCallbackToken)
        );
    }


    JNIEXPORT jobject JNICALL
    Java_fr_dcs_mdk_jni_Mdk_onEvent(
        JNIEnv* env,
        jobject thiz,
        jlong player,
        jobject listener
    ) {
        LOG_INFO("JNI call: onEvent");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return nullptr;

        auto globalRef = env->NewGlobalRef(listener);
        auto listenerAddress = reinterpret_cast<jlong>(globalRef);

        mdk::CallbackToken* newCallbackToken = nullptr;
        auto callbackFunction = [listenerAddress] (const mdk::MediaEvent& event) {
            auto javaListener = reinterpret_cast<jobject>(listenerAddress);
            Utils::onEvent(javaListener, event);
            return false;
        };
        mdkPlayer->onEvent(callbackFunction, newCallbackToken);
        auto tokenAddress = reinterpret_cast<jlong>(newCallbackToken);
        return Utils::listenerAndToken(listenerAddress, tokenAddress);
    }

    JNIEXPORT jlong JNICALL
    Java_fr_dcs_mdk_jni_Mdk_onStateChanged(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jobject listener
    ) {
        LOG_INFO("JNI call: onStateChanged");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return 0;
        auto globalRef = env->NewGlobalRef(listener);
        auto listenerAddress = reinterpret_cast<jlong>(globalRef);

        std::function<void(State)> callback = [listenerAddress] (const mdk::State state) {
            auto listener = reinterpret_cast<jobject>(listenerAddress);
            Utils::onState(listener, state);
        };
        mdkPlayer->onStateChanged(callback);
        return listenerAddress;
    }

    JNIEXPORT jobject JNICALL
    Java_fr_dcs_mdk_jni_Mdk_onLoop(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jobject listener
    ) {
        LOG_INFO("JNI call: onLoop");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return nullptr;

        auto listenerGlobalRef = env->NewGlobalRef(listener);
        auto listenerAddress = reinterpret_cast<jlong>(listenerGlobalRef);

        mdk::CallbackToken* callbackToken = nullptr;
        auto callback = [listenerAddress] (const int64_t value) {
            auto javaListener = reinterpret_cast<jobject>(listenerAddress);
            Utils::onLoop(javaListener, value);
        };
        mdkPlayer->onLoop(callback, callbackToken);
        auto callbackAddress = reinterpret_cast<jlong>(callbackToken);
        return Utils::listenerAndToken(listenerAddress, callbackAddress);
    }

    JNIEXPORT jlong JNICALL
    Java_fr_dcs_mdk_jni_Mdk_onCurrentMediaChanged(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jobject listener
    ) {
        LOG_INFO("JNI call: onCurrentMediaChanged");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return 0;

        auto globalRef = env->NewGlobalRef(listener);
        auto globalRefPointer = reinterpret_cast<jlong>(globalRef);

        auto callback = [globalRefPointer] () {
            auto javaListener = reinterpret_cast<jobject>(globalRefPointer);
            Utils::onCurrentMediaChanged(javaListener);
        };
        mdkPlayer->currentMediaChanged(callback);
        return globalRefPointer;
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_unregisterOnLoopCallback(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jlong listener,
        jlong token
    ) {
        LOG_INFO("JNI call: unregisterOnLoopCallback");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto listenerGlobalRef = reinterpret_cast<jobject>(listener);
        auto callbackToken = reinterpret_cast<CallbackToken*>(token);
        mdkPlayer->onLoop(nullptr, callbackToken);
        env->DeleteGlobalRef(listenerGlobalRef);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_unregisterOnMediaStatusChangedCallback(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jlong listener,
        jlong token
    ) {
        LOG_INFO("JNI call: unregisterOnMediaStatusChangedCallback");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto listenerGlobalRef = reinterpret_cast<jobject>(listener);
        auto callbackToken = reinterpret_cast<CallbackToken*>(token);
        mdkPlayer->onMediaStatus(nullptr, callbackToken);
        env->DeleteGlobalRef(listenerGlobalRef);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_unregisterOnStateChangedCallback(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jlong listener
    ) {
        LOG_INFO("JNI call: unregisterOnStateChangedCallback");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto listenerGlobalRef = reinterpret_cast<jobject>(listener);
        mdkPlayer->onStateChanged(nullptr);
        env->DeleteGlobalRef(listenerGlobalRef);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_unregisterOnCurrentMediaChangedCallback(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jlong listener
    ) {
        LOG_INFO("JNI call: unregisterOnCurrentMediaChangedCallback");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto listenerGlobalRef = reinterpret_cast<jobject>(listener);
        mdkPlayer->currentMediaChanged(nullptr);
        env->DeleteGlobalRef(listenerGlobalRef);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_unregisterOnEventCallback(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jlong listener,
        jlong token
    ) {
        LOG_INFO("JNI call: unregisterOnEventCallback");
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        auto listenerGlobalRef = reinterpret_cast<jobject>(listener);
        auto callbackToken = reinterpret_cast<CallbackToken*>(token);
        mdkPlayer->onEvent(nullptr, callbackToken);
        env->DeleteGlobalRef(listenerGlobalRef);
    }

    JNIEXPORT void JNICALL
    Java_fr_dcs_mdk_jni_Mdk_renderVideo(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jobject opaque
    ) {
        auto reference = reinterpret_cast<PlayerReference*>(player);
        auto mdkPlayer = reference->player;
        if (mdkPlayer == nullptr) return;
        mdkPlayer->renderVideo();
    }
}


extern "C"
JNIEXPORT jlong JNICALL
Java_fr_dcs_mdk_jni_Mdk_setupVulkanRenderer(
    JNIEnv *env,
    jobject thiz,
    jlong player,
    jobject surface
) {
    LOG_INFO("setupVulkanRenderer: [player=%lld, surface=%lld]", player, (int64_t) surface);
    auto reference = reinterpret_cast<PlayerReference*>(player);
    auto mdkPlayer = reference->player;
    if (mdkPlayer == nullptr) return 0;
    LOG_INFO("setupVulkanRenderer.setProperty");
    //mdkPlayer->setProperty("video.decoder", "surface=0");
    auto globalRef = env->NewGlobalRef(surface);
    mdkPlayer->updateNativeSurface(surface, -1, -1);

    VulkanRenderAPI renderApi{};
    mdkPlayer->setRenderAPI(&renderApi, globalRef);
    return reinterpret_cast<jlong>(globalRef);
}

/*extern "C"
JNIEXPORT void JNICALL
Java_fr_dcs_mdk_jni_Mdk_updateVulkanView(
    JNIEnv *env,
    jobject thiz,
    jlong player,
    jobject surface,
    jint width,
    jint height
) {
    LOG_INFO("updateVulkanView: [surface=%lld, width=%d, height=%d]", (int64_t) surface, width, height);
    auto reference = reinterpret_cast<PlayerReference*>(player);
    auto mdkPlayer = reference->player;
    if (mdkPlayer == nullptr) return;
    //mdkPlayer->updateNativeSurface(surface, width, height);
    mdkPlayer->resizeSurface(width, height);
    //mdkPlayer->setVideoSurfaceSize(width, height, surface);
}*/


//fixme: not tested yet
extern "C"
JNIEXPORT void JNICALL
Java_fr_dcs_mdk_jni_Mdk_detachVulkanRenderer(
        JNIEnv *env,
        jobject thiz,
        jlong player,
        jlong globalRef
) {
    LOG_INFO("detachVulkanRenderer: [globalRef=%lld]", globalRef);
    auto reference = reinterpret_cast<PlayerReference*>(player);
    auto mdkPlayer = reference->player;
    if (mdkPlayer == nullptr) return;
    auto ref = reinterpret_cast<jobject>(globalRef);
    mdkPlayer->setRenderAPI(nullptr, ref);
    mdkPlayer->updateNativeSurface(ref, 0, 0);
    env->DeleteGlobalRef(ref);

}