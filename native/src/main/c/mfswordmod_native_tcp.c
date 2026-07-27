#include "mfswordmod_native_config.h"
#include <dlfcn.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>
#include <arpa/inet.h>
#include <netinet/tcp.h>
#include <pthread.h>

static int (*original_send)(int sockfd, const void *buf, size_t len, int flags) = NULL;
static int (*original_recv)(int sockfd, void *buf, size_t len, int flags) = NULL;
static int (*original_connect)(int sockfd, const struct sockaddr *addr, socklen_t addrlen) = NULL;
static int (*original_close)(int fd) = NULL;
static int g_tcp_hooked = 0;
static int g_tcp_protect_enabled = 0;
static int g_tcp_protect_socket = -1;
static pthread_t g_tcp_keepalive_thread = 0;

static int hooked_send(int sockfd, const void *buf, size_t len, int flags) {
    if (g_tcp_protect_enabled && sockfd == g_tcp_protect_socket) {
        const char* data = (const char*)buf;
        if (strstr(data, "disconnect") != NULL ||
            strstr(data, "kick") != NULL ||
            strstr(data, "remove") != NULL) {
            return len;
        }
        if (strstr(data, "close") != NULL) {
            return len;
        }
    }
    return original_send(sockfd, buf, len, flags);
}

static int hooked_recv(int sockfd, void *buf, size_t len, int flags) {
    int ret = original_recv(sockfd, buf, len, flags);
    if (g_tcp_protect_enabled && ret > 0 && sockfd == g_tcp_protect_socket) {
        char* data = (char*)buf;
        if (strstr(data, "disconnect") != NULL ||
            strstr(data, "kick") != NULL ||
            strstr(data, "remove") != NULL) {
            memset(buf, 0, ret);
            return 0;
        }
    }
    return ret;
}

static int hooked_connect(int sockfd, const struct sockaddr *addr, socklen_t addrlen) {
    int ret = original_connect(sockfd, addr, addrlen);
    if (ret == 0 && g_tcp_protect_enabled) {
        g_tcp_protect_socket = sockfd;
        int keepalive = 1;
        int keepidle = 5;
        int keepintvl = 1;
        int keepcnt = 3;
        setsockopt(sockfd, SOL_SOCKET, SO_KEEPALIVE, &keepalive, sizeof(keepalive));
        setsockopt(sockfd, IPPROTO_TCP, TCP_KEEPIDLE, &keepidle, sizeof(keepidle));
        setsockopt(sockfd, IPPROTO_TCP, TCP_KEEPINTVL, &keepintvl, sizeof(keepintvl));
        setsockopt(sockfd, IPPROTO_TCP, TCP_KEEPCNT, &keepcnt, sizeof(keepcnt));
        LOGI("TCP保护已启用，socket=%d", sockfd);
    }
    return ret;
}

static int hooked_close(int fd) {
    if (g_tcp_protect_enabled && fd == g_tcp_protect_socket) {
        LOGI("TCP保护拦截close调用");
        return 0;
    }
    return original_close(fd);
}

static void* tcp_keepalive_thread(void* arg) {
    LOGI("TCP保活线程启动");
    while (g_tcp_protect_enabled) {
        if (g_tcp_protect_socket > 0) {
            char keepalive_packet[1] = {0x00};
            original_send(g_tcp_protect_socket, keepalive_packet, 1, MSG_NOSIGNAL);
        }
        sleep(5);
    }
    return NULL;
}

void install_tcp_protect() {
    if (g_tcp_hooked) return;
    void* libc = dlopen("libc.so", RTLD_LAZY);
    if (libc == NULL) {
        libc = dlopen("libc.so.6", RTLD_LAZY);
    }
    if (libc == NULL) {
        LOGE("无法加载libc.so");
        return;
    }
    void* send_ptr = dlsym(libc, "send");
    void* recv_ptr = dlsym(libc, "recv");
    void* connect_ptr = dlsym(libc, "connect");
    void* close_ptr = dlsym(libc, "close");
    if (send_ptr == NULL || recv_ptr == NULL || connect_ptr == NULL || close_ptr == NULL) {
        LOGE("无法获取系统调用地址");
        return;
    }
    DobbyHook(send_ptr, (void*)hooked_send, (void**)&original_send);
    DobbyHook(recv_ptr, (void*)hooked_recv, (void**)&original_recv);
    DobbyHook(connect_ptr, (void*)hooked_connect, (void**)&original_connect);
    DobbyHook(close_ptr, (void*)hooked_close, (void**)&original_close);
    g_tcp_hooked = 1;
    LOGI("TCP Hook安装完成");
}

void enable_tcp_protect() {
    if (!g_tcp_hooked) install_tcp_protect();
    g_tcp_protect_enabled = 1;
    if (g_tcp_keepalive_thread == 0) {
        pthread_create(&g_tcp_keepalive_thread, NULL, tcp_keepalive_thread, NULL);
    }
    LOGI("TCP保护已启用");
}

void disable_tcp_protect() {
    g_tcp_protect_enabled = 0;
    g_tcp_protect_socket = -1;
    LOGI("TCP保护已禁用");
}

int is_tcp_protect_enabled() {
    return g_tcp_protect_enabled;
}