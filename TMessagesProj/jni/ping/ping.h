#pragma once

#define LOG_TAG "owlgram/native/ping"

#define PING_REQUEST "HEAD / HTTP/1.1\r\nConnection: close\r\n\r\n"
#define PING_PORT 80

#define SOCKET_CONNECT_OK 0
#define SOCKET_SEND_FAIL -1
#define SOCKET_EXCEPTION_CLASSNAME "java/net/SocketException"

#define SOCKET_MSG_EADDRNOTAVAIL "The specified address is not available from the local machine"
#define SOCKET_MSG_ECONNREFUSED "The target address was not listening for connections or refused the connection request"
#define SOCKET_MSG_ETIMEDOUT "The attempt to connect timed out before a connection was made"
#define SOCKET_MSG_ECONNRESET "A connection was forcibly closed by a peer"
#define SOCKET_MSG_ENETDOWN "The local network interface used to reach the destination is down"
#define SOCKET_MSG_ENETUNREACH "No route to the network is present."

#define SOCKET_FMT_EGENERIC "A generic error has occurred. Errno: %d"

#define LOGF(level, size, fmt, ...) \
    { \
        const char buffer[size]; \
        sprintf(buffer, fmt, __VA_ARGS__); \
        LOG##level(buffer); \
    } NULL

jint throwSocketException(JNIEnv *env, char *msg);
unsigned char handleConnectError(JNIEnv *env, int res);
unsigned char handleSendError(JNIEnv *env, int res);
