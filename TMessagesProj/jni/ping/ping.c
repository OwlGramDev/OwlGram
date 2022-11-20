#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>

#include <netinet/in.h>
#include <sys/socket.h>

#include <jni.h>
#include <c_utils.h>

#include "ping.h"

struct sockaddr_in* getSockAddrIn(JNIEnv *env, jstring address) {
    static struct sockaddr_in socketAddress;
    memset(&socketAddress, 0x00, sizeof(socketAddress));

    socketAddress.sin_port = htons(PING_PORT);
    socketAddress.sin_family = AF_INET;

    const char *ip = (*env)->GetStringUTFChars(env, address, 0);

    LOGF(D, 255, "Pinging %s", ip);

    inet_aton(ip, &socketAddress.sin_addr);
    (*env)->ReleaseStringUTFChars(env, address, ip);

    return &socketAddress;
}

extern JNIEXPORT jint JNICALL Java_it_owlgram_android_helpers_StandardHTTPRequest_ping(
    JNIEnv *env,
    __attribute__((unused)) jclass clazz,
    jstring address
) {
    MEASURE(t0, t1, CLOCK_MONOTONIC,
    {
        int socketfd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

        struct sockaddr_in *socketAddress = getSockAddrIn(env, address);

        int res = connect(socketfd, socketAddress, sizeof(*socketAddress));
        if (handleConnectError(env, res) == EXIT_FAILURE) {
            return NULL; // won't be processed
        }

        res = send(socketfd, PING_REQUEST, strlen(PING_REQUEST), MSG_NOSIGNAL);
        if (handleSendError(env, res) == EXIT_FAILURE) {
            return NULL; // won't be processed
        }

        shutdown(socketfd, SHUT_RDWR);
    });

    int delta = (int) ((t1.tv_sec - t0.tv_sec) * 1000L + ((t1.tv_nsec - t0.tv_nsec) / 1000000L));

    LOGF(D, 255, "t0: %ul t1: %ul delta: %d", t0.tv_nsec, t1.tv_nsec, delta);

    return (jint) delta;
}
