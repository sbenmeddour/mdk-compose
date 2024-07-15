#include "player_wrapper.hpp"
#include "utils.hpp"
#include "thread_queue.hpp"

PlayerWrapper::PlayerWrapper(JNIEnv* env, jobject listener) {
    this->mdkPlayer = std::make_unique<Player>();
    this->onMediaStatusToken = nullptr;
    this->onEventToken = nullptr;
    this->listener = env->NewGlobalRef(listener);
    auto clazz = env->FindClass("fr/dcs/mdk/jni/JniListener");
    this->onStateChangedMethod = env->GetMethodID(clazz, "onStateChanged" ,"(I)V");
    this->onMediaStatusMethod = env->GetMethodID(clazz, "onMediaStatus" ,"(II)V");
    this->onMediaEventMethod = env->GetMethodID(clazz, "onMediaEvent" ,"(JLjava/lang/String;Ljava/lang/String;)V");

    this->mdkPlayer->onEvent(
        /* cb = */ [this] (const mdk::MediaEvent &event) {
            LOG_INFO("onEvent callback: [error=%lld, category=%s, detail=%s]", event.error, event.category.c_str(), event.detail.c_str());
            auto env = Utils::getInstance().getEnv();
            auto category = env->NewStringUTF(event.category.c_str());
            auto detail = env->NewStringUTF(event.detail.c_str());
            env->CallVoidMethod(
                /* obj = */ this->listener,
                /* methodID = */this->onMediaEventMethod,
                /* args = */ event.error, category, detail
            );
            env->DeleteLocalRef(category);
            env->DeleteLocalRef(detail);
            return false;
        },
        /* token = */ this->onEventToken
    );
    this->mdkPlayer->onMediaStatus(
        /* cb = */ [this] (const mdk::MediaStatus &previousValue, const mdk::MediaStatus &newValue) {
            LOG_INFO("onMediaStatus callback: [previousValue=%d, newValue=%d]", previousValue, newValue);
            auto env = Utils::getInstance().getEnv();
            env->CallVoidMethod(
                /* obj = */ this->listener,
                /* methodID = */this->onMediaStatusMethod,
                /* args = */ previousValue, newValue
            );
            return false;
        },
        /* token = */ this->onMediaStatusToken
    );
    this->mdkPlayer->onStateChanged( [this] (const mdk::State &state) {
        LOG_INFO("onStateChanged callback: [state=%d]", (int8_t) state);
        auto env = Utils::getInstance().getEnv();
        env->CallVoidMethod(
            /* obj = */ this->listener,
            /* methodID = */this->onStateChangedMethod,
            /* args = */ state
        );
    });

}


PlayerWrapper::~PlayerWrapper() {
    this->mdkPlayer->onEvent(nullptr, nullptr);
    this->mdkPlayer->onMediaStatus(nullptr, nullptr);
    this->mdkPlayer->onStateChanged(nullptr);
    this->onEventToken = nullptr;
    this->onMediaStatusToken = nullptr;
    auto env = Utils::getInstance().getEnv();
    if (env != nullptr) {
        env->DeleteGlobalRef(this->listener);
        //env->DeleteGlobalRef(this->listenerClass);
    }
}


Player* PlayerWrapper::getMdkPlayer() {
    return this->mdkPlayer.get();
}


void PlayerWrapper::addListener(JNIEnv* env, jobject javaListener) {

}

void PlayerWrapper::removeListener(JNIEnv* env, jobject javaListener) {

}


void PlayerWrapper::notifyStateChanged() {

}

void PlayerWrapper::notifyMediaStatus() {

}

void PlayerWrapper::notifyMediaEvent() {

}
