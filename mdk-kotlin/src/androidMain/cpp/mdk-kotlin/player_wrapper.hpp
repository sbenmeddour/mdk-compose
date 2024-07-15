#ifndef MDK_COMPOSE_PLAYER_WRAPPER_HPP
#define MDK_COMPOSE_PLAYER_WRAPPER_HPP

#include <mdk/Player.h>
#include <vector>
#include <memory>
#include <mutex>
#include <jni.h>

using namespace mdk;

class PlayerWrapper {

public:
    PlayerWrapper(JNIEnv* env, jobject listener);
    ~PlayerWrapper();

    Player* getMdkPlayer();

    void addListener(JNIEnv* env, jobject javaListener);
    void removeListener(JNIEnv* env, jobject javaListener);

    void notifyStateChanged();
    void notifyMediaStatus();
    void notifyMediaEvent();

private:
    std::unique_ptr<Player> mdkPlayer;
    std::mutex mutex;
    mdk::CallbackToken* onEventToken;
    mdk::CallbackToken* onMediaStatusToken;

    jobject listener;
    jmethodID onStateChangedMethod;
    jmethodID onMediaStatusMethod;
    jmethodID onMediaEventMethod;
};


#endif //MDK_COMPOSE_PLAYER_WRAPPER_HPP
