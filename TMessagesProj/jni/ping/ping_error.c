#include <errno.h>
#include <stdio.h>
#include <stdlib.h>

#include <jni.h>
#include <c_utils.h>

#include "ping.h"

jint throwSocketException(JNIEnv *env, char *msg) {
    jclass exceptionClass = (*env)->FindClass(env, SOCKET_EXCEPTION_CLASSNAME);

    return (*env)->ThrowNew(env, exceptionClass, msg);
}

jint throwGenericError(JNIEnv *env) {
    const char buffer[] = { SOCKET_FMT_EGENERIC "\00\00\00\00\00" };
    sprintf(&buffer, &buffer, errno);

    return throwSocketException(env, buffer);
}

unsigned char handleConnectError(JNIEnv *env, int res) {
    LOGF(D, 255, "connect errno: %d", res);

    if(res != SOCKET_CONNECT_OK) {
        switch(errno) {
        case EADDRNOTAVAIL:
            throwSocketException(env, SOCKET_MSG_EADDRNOTAVAIL);
            break;
        case ECONNREFUSED:
            throwSocketException(env, SOCKET_MSG_ECONNREFUSED);
            break;
        case ETIMEDOUT:
            throwSocketException(env, SOCKET_MSG_ETIMEDOUT);
            break;

        default:
            throwGenericError(env);
            break;
        }

        return EXIT_FAILURE;
    }
    else {
        return EXIT_SUCCESS;
    }
}

unsigned char handleSendError(JNIEnv *env, int res) {
    LOGF(D, 255, "send errno: %d", res);

    if(res == SOCKET_SEND_FAIL) {
        switch(errno) {
        case ECONNRESET:
            throwSocketException(env, SOCKET_MSG_ECONNRESET);
            break;
        case ENETDOWN:
            throwSocketException(env, SOCKET_MSG_ENETDOWN);
            break;
        case ENETUNREACH:
            throwSocketException(env, SOCKET_MSG_ENETUNREACH);
            break;

        default:
            throwGenericError(env);
            break;
        }

        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}
