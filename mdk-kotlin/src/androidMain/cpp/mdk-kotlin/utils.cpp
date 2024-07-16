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
        case JNI_OK:
            break;
        default:
            LOG_INFO("Cannot acquire JNIEnv: [result=%d]", result);
    }
    return env;
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

    auto subtitleClass = env->FindClass("fr/dcs/mdk/native/NativeSubtitle");
    auto subtitleConstructor = env->GetMethodID(subtitleClass, "<init>", "(IJJLjava/util/Map;Lfr/dcs/mdk/native/NativeSubtitleCodec;)V");

    auto codecClass = env->FindClass("fr/dcs/mdk/native/NativeSubtitleCodec");
    auto codecConstructor = env->GetMethodID(codecClass, "<init>", "(Ljava/lang/String;I)V");

    for (const auto& item : vector) {
        auto codec = env->NewObject(
            /* clazz = */ codecClass,
            /* methodID = */ codecConstructor,
            /* codec = */ env->NewStringUTF(item.codec.codec),
            /* codecTag = */ static_cast<jlong>(item.codec.codec_tag)
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

    auto streamClass = env->FindClass("fr/dcs/mdk/native/NativeAudioStream");
    auto streamConstructor = env->GetMethodID(streamClass, "<init>", "(IJJJLjava/util/Map;Lfr/dcs/mdk/native/NativeAudioCodec;)V");

    auto codecClass = env->FindClass("fr/dcs/mdk/native/NativeAudioCodec");
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

    auto streamClass = env->FindClass("fr/dcs/mdk/native/NativeVideoStream");
    auto streamConstructor = env->GetMethodID(streamClass, "<init>", "(IJJJILjava/util/Map;Lfr/dcs/mdk/native/NativeVideoCodec;)V");

    auto codecClass = env->FindClass("fr/dcs/mdk/native/NativeVideoCodec");
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
    auto nativeMediaInfoClass = env->FindClass("fr/dcs/mdk/native/NativeMediaInfo");
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