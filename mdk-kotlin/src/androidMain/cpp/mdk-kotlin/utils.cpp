#include "utils.hpp"
#include <jni.h>
#include <pthread.h>
#include <iostream>
#include <cstring>
#include "sys/prctl.h"

void Utils::setJavaVm(JavaVM *vm) {
    Utils& instance = Utils::instance();
    instance.vm = vm;
    auto env = Utils::getEnv();

    auto onStateChangedClazz= env->FindClass("fr/dcs/mdk/jni/Mdk$OnStateChanged");
    auto onMediaStatusClazz= env->FindClass("fr/dcs/mdk/jni/Mdk$OnMediaStatus");
    auto onEventClazz= env->FindClass("fr/dcs/mdk/jni/Mdk$OnEvent");
    auto onLoopClazz= env->FindClass("fr/dcs/mdk/jni/Mdk$OnLoop");
    auto onCurrentMediaChangedClazz= env->FindClass("fr/dcs/mdk/jni/Mdk$OnCurrentMediaChanged");

    instance._onStateChangedHelper = new JavaListener(
        /* clazz = */ onStateChangedClazz,
        /* methodID = */ env->GetMethodID(onStateChangedClazz, "onState", "(I)V")
    );
    instance._onEventHelper = new JavaListener(
        /* clazz = */ onEventClazz,
        /* methodID = */ env->GetMethodID(onEventClazz, "onEvent", "(JLjava/lang/String;Ljava/lang/String;)V")
    );
    instance._onLoopHelper = new JavaListener(
        /* clazz = */ onLoopClazz,
        /* methodID = */ env->GetMethodID(onLoopClazz, "onLoop", "(J)V")
    );
    instance._onStatusHelper = new JavaListener(
        /* clazz = */ onMediaStatusClazz,
        /* methodID = */ env->GetMethodID(onMediaStatusClazz, "onStatus", "(II)V")
    );
    instance._onCurrentMediaHelper = new JavaListener(
        /* clazz = */ onCurrentMediaChangedClazz,
        /* methodID = */ env->GetMethodID(onCurrentMediaChangedClazz, "onCurrentMediaChanged", "()V")
    );
}

JavaVM *Utils::getJavaVm() {
    return Utils::instance().vm;
}

JNIEnv *Utils::getEnv() {
    Utils& utils = Utils::instance();
    auto jvm = utils.vm;
    if (jvm == nullptr) return nullptr;
    JNIEnv *env = nullptr;
    auto result = jvm->GetEnv( (void**) &env, JNI_VERSION_1_6);
    switch (result) {
        case JNI_EDETACHED:
            jvm->AttachCurrentThread(&env, nullptr);
            break;
        case JNI_OK:
            break;
        default:
            LOG_INFO("Cannot acquire JNIEnv: [result=%d]", result);
    }
    return env;
}

jobject Utils::jniMediaInfo(JNIEnv *env, mdk::MediaInfo &mediaInfo) {
    return buildJavaMediaInfo(env, mediaInfo);
}

void Utils::onState(jobject listener, const mdk::State state) {
    auto listenerHelper = Utils::onStateChangedHelper();
    auto env = Utils::getEnv();
    if (env == nullptr) return;
    env->CallVoidMethod(listener, listenerHelper->methodID, (jint) state);
}

void Utils::onMediaStatus(jobject listener, mdk::MediaStatus previous, mdk::MediaStatus next) {
    auto listenerHelper = Utils::onStatusHelper();
    auto env = Utils::getEnv();
    if (env == nullptr) return;
    env->CallVoidMethod(listener, listenerHelper->methodID, (jint) previous, (jint) next);
}

void Utils::onCurrentMediaChanged(jobject listener) {
    auto listenerHelper = Utils::onCurrentMediaHelper();
    auto env = Utils::getEnv();
    if (env == nullptr) return;
    env->CallVoidMethod(listener, listenerHelper->methodID);
}

void Utils::onLoop(jobject listener, jlong value) {
    auto listenerHelper = Utils::onLoopHelper();
    auto env = Utils::getEnv();
    if (env == nullptr) return;
    env->CallVoidMethod(listener, listenerHelper->methodID, value);
}

void Utils::onEvent(jobject listener, mdk::MediaEvent event) {
    auto env = Utils::getEnv();
    if (env == nullptr) return;
    auto category = env->NewStringUTF(event.category.data());
    auto detail = env->NewStringUTF(event.detail.data());
    auto listenerHelper = Utils::onEventHelper();
    if (env == nullptr) return;
    env->CallVoidMethod(listener, listenerHelper->methodID, (jlong) event.error, category, detail);
    env->DeleteLocalRef(category);
    env->DeleteLocalRef(detail);
}

std::set<int> Utils::arrayToSet(JNIEnv *env, jintArray array) {
    std::set<int> result;
    auto length = env->GetArrayLength(array);
    auto elements = env->GetIntArrayElements(array, nullptr);
    for (jsize i = 0; i < length; i++) {
        auto element = elements[i];
        result.insert(element);
    }
    return result;
}

std::vector<std::string> Utils::arrayToVector(JNIEnv *env, jobjectArray array) {
    std::vector<std::string> result;
    auto length = env->GetArrayLength(array);
    result.reserve(length);
    for (jsize i = 0; i < length; i++) {
        auto element = (jstring) env->GetObjectArrayElement(array, i);
        const char* cString = env->GetStringUTFChars(element, nullptr);
        result.emplace_back(cString);
        env->ReleaseStringUTFChars(element, cString);
        env->DeleteLocalRef(element);
    }
    return result;
}

JavaListener* Utils::onStateChangedHelper() {
    return Utils::instance()._onStateChangedHelper;
}

jobject Utils::listenerAndToken(jlong listener, jlong token) {
    auto env = Utils::getEnv();
    auto clazz = env->FindClass("fr/dcs/mdk/jni/Mdk$ListenerAndToken");
    auto constructor = env->GetMethodID(clazz, "<init>", "(JJ)V");
    return env->NewObject(clazz, constructor, listener, token);
}

JavaListener *Utils::onEventHelper() {
    return Utils::instance()._onEventHelper;
}

JavaListener *Utils::onLoopHelper() {
    return Utils::instance()._onLoopHelper;
}

JavaListener *Utils::onStatusHelper() {
    return Utils::instance()._onStatusHelper;
}

JavaListener *Utils::onCurrentMediaHelper() {
    return Utils::instance()._onCurrentMediaHelper;
}


jobject java_hashmap(
    JNIEnv *env,
    const std::unordered_map<std::string, std::string> &cppMap
) {
    auto hashMapClass = env->FindClass("java/util/HashMap");
    auto hashMapConstructor = env->GetMethodID(hashMapClass, "<init>", "()V");
    auto hashMap = env->NewObject(hashMapClass, hashMapConstructor);
    auto put = env->GetMethodID(hashMapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    for (const auto &pair : cppMap) {
        jstring key = env->NewStringUTF(pair.first.c_str());
        jstring value = env->NewStringUTF(pair.second.c_str());
        env->CallObjectMethod(hashMap, put, key, value);
        env->DeleteLocalRef(key);
        env->DeleteLocalRef(value);
    }
    return hashMap;
}

jobject mapSubtitles(
    JNIEnv* env,
    const std::vector<mdk::SubtitleStreamInfo>& vector
) {
    auto arrayListClass = env->FindClass("java/util/ArrayList");
    auto arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    auto arrayListAdd = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    auto arrayList = env->NewObject(arrayListClass, arrayListConstructor);

    auto subtitleClass = env->FindClass("fr/dcs/mdk/player/models/Subtitle");
    auto subtitleConstructor = env->GetMethodID(subtitleClass, "<init>", "(IJJLjava/util/Map;Lfr/dcs/mdk/player/models/SubtitleCodec;)V");

    auto codecClass = env->FindClass("fr/dcs/mdk/player/models/SubtitleCodec");
    auto codecConstructor = env->GetMethodID(codecClass, "<init>", "(Ljava/lang/String;I)V");

    for (const auto& item : vector) {
        auto codec = env->NewObject(
            /* clazz = */ codecClass,
            /* methodID = */ codecConstructor,
            /* codec = */ env->NewStringUTF(item.codec.codec),
            /* codecTag = */ static_cast<jint>(item.codec.codec_tag)
        );
        auto javaItem = env->NewObject(
            /* clazz =  */ subtitleClass,
            /* methodID =  */ subtitleConstructor,
            /* index = */ item.index,
            /* startTime = */ item.start_time,
            /* duration = */ item.duration,
            /* metaData = */ java_hashmap(env, item.metadata),
            /* codec = */ codec
        );

        env->CallBooleanMethod(arrayList, arrayListAdd, javaItem);
        env->DeleteLocalRef(javaItem);
        env->DeleteLocalRef(codec);
    }
    return arrayList;
}

jobject mapAudio(
    JNIEnv* env,
    const std::vector<mdk::AudioStreamInfo>& vector
) {
    auto arrayListClass = env->FindClass("java/util/ArrayList");
    auto arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    auto arrayListAdd = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    auto arrayList = env->NewObject(arrayListClass, arrayListConstructor);

    auto streamClass = env->FindClass("fr/dcs/mdk/player/models/AudioStream");
    auto streamConstructor = env->GetMethodID(streamClass, "<init>", "(IJJJLjava/util/Map;Lfr/dcs/mdk/player/models/AudioCodec;)V");

    auto codecClass = env->FindClass("fr/dcs/mdk/player/models/AudioCodec");
    auto codecConstructor = env->GetMethodID(codecClass, "<init>", "(Ljava/lang/String;IJIIFZZZIIIII)V");

    for (const auto& item : vector) {
        jobject javaCodec = env->NewObject(
            /* clazz = */ codecClass,
            /* methodID = */ codecConstructor,
            /* codec = */ env->NewStringUTF(item.codec.codec),
            /* codecTag = */ static_cast<jint>(item.codec.codec_tag),
            /* bitRate = */ item.codec.bit_rate,
            /* profile = */ item.codec.profile,
            /* level = */ item.codec.level,
            /* frameRate = */ item.codec.frame_rate,
            /* isFloat = */ item.codec.is_float,
            /* isUnsigned = */ item.codec.is_unsigned,
            /* isPlanar = */ item.codec.is_planar,
            /* rawSampleSize = */ item.codec.raw_sample_size,
            /* channels = */ item.codec.channels,
            /* sampleRate = */ item.codec.sample_rate,
            /* blockAlign = */ item.codec.block_align,
            /* frameSize = */ item.codec.frame_size
        );
        jobject javaStream = env->NewObject(
            /* clazz = */ streamClass,
            /* methodID = */ streamConstructor,
            /* index = */ item.index,
            /* startTime = */ item.start_time,
            /* duration = */ item.duration,
            /* frames = */ item.frames,
            /* metaData = */ java_hashmap(env, item.metadata),
            /* codec = */ javaCodec
        );

        env->CallBooleanMethod(arrayList, arrayListAdd, javaStream);
        env->DeleteLocalRef(javaStream);
        env->DeleteLocalRef(javaCodec);
    }
    return arrayList;
}

jobject mapVideo(
    JNIEnv* env,
    const std::vector<mdk::VideoStreamInfo>& vector
) {
    auto arrayListClass = env->FindClass("java/util/ArrayList");
    auto arrayListConstructor = env->GetMethodID(arrayListClass, "<init>", "()V");
    auto arrayListAdd = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
    auto arrayList = env->NewObject(arrayListClass, arrayListConstructor);

    auto streamClass = env->FindClass("fr/dcs/mdk/player/models/VideoStream");
    auto streamConstructor = env->GetMethodID(streamClass, "<init>", "(IJJJILjava/util/Map;Lfr/dcs/mdk/player/models/VideoCodec;)V");

    auto codecClass = env->FindClass("fr/dcs/mdk/player/models/VideoCodec");
    auto codecConstructor = env->GetMethodID(codecClass, "<init>", "(Ljava/lang/String;IJIIFILjava/lang/String;IIIF)V");

    for (const auto& item : vector) {
        jobject javaCodec = env->NewObject(
            /* clazz = */ codecClass,
            /* methodID = */ codecConstructor,
            /* codec = */ env->NewStringUTF(item.codec.codec),
            /* codecTag = */ static_cast<jint>(item.codec.codec_tag),
            /* bitRate = */ item.codec.bit_rate,
            /* profile = */ item.codec.profile,
            /* level = */ item.codec.level,
            /* frameRate = */ item.codec.frame_rate,
            /* format = */ item.codec.format,
            /* formatName = */ env->NewStringUTF(item.codec.format_name),
            /* width = */ item.codec.width,
            /* height = */ item.codec.height,
            /* bFrames = */ item.codec.b_frames,
            /* par = */ item.codec.par
        );
        jobject javaStream = env->NewObject(
            /* clazz = */ streamClass,
            /* methodID = */ streamConstructor,
            /*index = index*/ item.index  ,
            /*startTime = startTime*/ item.start_time,
            /*duration = duration*/ item.duration,
            /*frames = frames*/ item.frames,
            /*rotation = rotation*/ item.rotation,
            /*metaData = metaData*/ java_hashmap(env, item.metadata),
            /*codec = codec*/ javaCodec
        );

        env->CallBooleanMethod(arrayList, arrayListAdd, javaStream);
        env->DeleteLocalRef(javaStream);
        env->DeleteLocalRef(javaCodec);
    }
    return arrayList;
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

jobject buildJavaMediaInfo(JNIEnv* env, const mdk::MediaInfo& mediaInfo) {
    auto nativeMediaInfoClass = env->FindClass("fr/dcs/mdk/player/models/MediaInfo");
    auto qsd = env->FindClass("java/util/List");
    auto nativeMediaInfoConstructor = env->GetMethodID(
        /* clazz = */ nativeMediaInfoClass,
        /* name = */ "<init>",
        /* sign = */ "(JJJJLjava/lang/String;ILjava/util/List;Ljava/util/List;Ljava/util/List;)V"
    );
    jobject video = mapVideo(env, mediaInfo.video);
    jobject audio = mapAudio(env, mediaInfo.audio);
    jobject subtitles = mapSubtitles(env, mediaInfo.subtitle);

    return env->NewObject(
        /* class = */ nativeMediaInfoClass,
        /* methodID */ nativeMediaInfoConstructor,
        /* startTime = */ mediaInfo.start_time,
        /* duration = */ mediaInfo.duration,
        /* bitRate = */ mediaInfo.bit_rate,
        /* size = */ mediaInfo.size,
        /* format = */ env->NewStringUTF(mediaInfo.format),
        /* streams = */ mediaInfo.streams,
        /* audio = */ audio,
        /* video = */ video,
        /* subtitles = */ subtitles
    );
    return nullptr;
}