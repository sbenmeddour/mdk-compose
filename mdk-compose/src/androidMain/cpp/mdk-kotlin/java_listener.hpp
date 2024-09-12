#ifndef MDK_COMPOSE_JAVA_LISTENER_HPP
#define MDK_COMPOSE_JAVA_LISTENER_HPP

#include <jni.h>

class JavaListener {

public:
    JavaListener(jclass clazz, jmethodID methodID) {
        this->clazz = clazz;
        this-> methodID = methodID;
    }
    jclass clazz;
    jmethodID methodID;
};


#endif //MDK_COMPOSE_JAVA_LISTENER_HPP
