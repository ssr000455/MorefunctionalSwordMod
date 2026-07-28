#include "mfswordmod_native_config.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <stdarg.h>
#include <sys/stat.h>
#include <unistd.h>
#include <pthread.h>
#include <errno.h>

#define LOGI(...) printf("[mfswordmod] " __VA_ARGS__)
#define LOGE(...) printf("[mfswordmod] " __VA_ARGS__)
#define LOGW(...) printf("[mfswordmod] " __VA_ARGS__)

void log_write(const char* format, ...);

static FILE* g_log_file = NULL;
static FILE* g_alert_file = NULL;
static bool g_log_initialized = false;
static pthread_mutex_t g_log_lock = PTHREAD_MUTEX_INITIALIZER;
static int g_log_level = 0;

#define LOG_LEVEL_INFO  0
#define LOG_LEVEL_WARN  1
#define LOG_LEVEL_ERROR 2
#define LOG_LEVEL_DEBUG 3

static void mkdir_recursive_log(const char* path) {
    if (path == NULL || path[0] == 0) return;
    char tmp[512];
    strncpy(tmp, path, sizeof(tmp) - 1);
    tmp[sizeof(tmp) - 1] = 0;
    for (char* p = tmp + 1; *p; p++) {
        if (*p == '/') {
            *p = 0;
            mkdir(tmp, 0755);
            *p = '/';
        }
    }
    mkdir(tmp, 0755);
}

static void get_time_str(char* buffer, int size) {
    time_t now = time(NULL);
    struct tm* tm_info = localtime(&now);
    strftime(buffer, size, "%Y-%m-%d %H:%M:%S", tm_info);
}

void init_log() {
    if (g_log_initialized) return;

    pthread_mutex_lock(&g_log_lock);

    if (!get_config_bool("enable_logging")) {
        LOGI("日志功能未启用 (enable_logging=0)");
        pthread_mutex_unlock(&g_log_lock);
        return;
    }

    const char* log_path = get_log_path();
    const char* dir = get_config_dir();
    mkdir_recursive_log(dir);

    g_log_file = fopen(log_path, "a");
    if (g_log_file == NULL) {
        printf("[mfswordmod] 无法打开日志文件: %s (errno=%d)\n", log_path, errno);
        pthread_mutex_unlock(&g_log_lock);
        return;
    }

    const char* alert_path = get_alert_path();
    g_alert_file = fopen(alert_path, "a");
    if (g_alert_file == NULL) {
        printf("[mfswordmod] 无法打开告警文件: %s (errno=%d)\n", alert_path, errno);
    }

    g_log_initialized = true;
    pthread_mutex_unlock(&g_log_lock);

    log_write("=== 日志系统初始化 ===");
    log_write("日志文件: %s", log_path);
    if (g_alert_file != NULL) {
        log_write("告警文件: %s", alert_path);
    }
    LOGI("日志系统已初始化: %s", log_path);
}

void log_write(const char* format, ...) {
    if (!g_log_initialized) {
        init_log();
        if (!g_log_initialized) return;
    }

    pthread_mutex_lock(&g_log_lock);

    if (g_log_file == NULL) {
        pthread_mutex_unlock(&g_log_lock);
        return;
    }

    char time_str[32];
    get_time_str(time_str, sizeof(time_str));

    fprintf(g_log_file, "[%s] ", time_str);

    va_list args;
    va_start(args, format);
    vfprintf(g_log_file, format, args);
    va_end(args);

    fprintf(g_log_file, "\n");
    fflush(g_log_file);

    pthread_mutex_unlock(&g_log_lock);
}

void log_write_level(int level, const char* tag, const char* format, ...) {
    if (!g_log_initialized) {
        init_log();
        if (!g_log_initialized) return;
    }

    if (level < g_log_level) return;

    pthread_mutex_lock(&g_log_lock);

    if (g_log_file == NULL) {
        pthread_mutex_unlock(&g_log_lock);
        return;
    }

    char time_str[32];
    get_time_str(time_str, sizeof(time_str));

    const char* level_str = "";
    switch (level) {
        case LOG_LEVEL_INFO:  level_str = "INFO"; break;
        case LOG_LEVEL_WARN:  level_str = "WARN"; break;
        case LOG_LEVEL_ERROR: level_str = "ERROR"; break;
        case LOG_LEVEL_DEBUG: level_str = "DEBUG"; break;
        default: level_str = "INFO"; break;
    }

    fprintf(g_log_file, "[%s] [%s] [%s] ", time_str, level_str, tag);

    va_list args;
    va_start(args, format);
    vfprintf(g_log_file, format, args);
    va_end(args);

    fprintf(g_log_file, "\n");
    fflush(g_log_file);

    pthread_mutex_unlock(&g_log_lock);
}

void alert_write(const char* format, ...) {
    if (!g_log_initialized) {
        init_log();
        if (!g_log_initialized) return;
    }

    pthread_mutex_lock(&g_log_lock);

    if (g_log_file != NULL) {
        char time_str[32];
        get_time_str(time_str, sizeof(time_str));
        fprintf(g_log_file, "[%s] [ALERT] ", time_str);
        va_list args;
        va_start(args, format);
        vfprintf(g_log_file, format, args);
        va_end(args);
        fprintf(g_log_file, "\n");
        fflush(g_log_file);
    }

    if (g_alert_file != NULL) {
        char time_str[32];
        get_time_str(time_str, sizeof(time_str));
        fprintf(g_alert_file, "[%s] ", time_str);
        va_list args;
        va_start(args, format);
        vfprintf(g_alert_file, format, args);
        va_end(args);
        fprintf(g_alert_file, "\n");
        fflush(g_alert_file);
    } else {
        const char* alert_path = get_alert_path();
        g_alert_file = fopen(alert_path, "a");
        if (g_alert_file != NULL) {
            char time_str[32];
            get_time_str(time_str, sizeof(time_str));
            fprintf(g_alert_file, "[%s] ", time_str);
            va_list args;
            va_start(args, format);
            vfprintf(g_alert_file, format, args);
            va_end(args);
            fprintf(g_alert_file, "\n");
            fflush(g_alert_file);
        }
    }

    pthread_mutex_unlock(&g_log_lock);
}

void log_protect_event(const char* event, const char* detail) {
    log_write_level(LOG_LEVEL_INFO, "PROTECT", "%s: %s", event, detail);
    if (strstr(event, "拦截") != NULL || strstr(event, "防") != NULL) {
        alert_write("[PROTECT] %s: %s", event, detail);
    }
}

void log_detect_event(const char* event, const char* detail) {
    log_write_level(LOG_LEVEL_WARN, "DETECT", "%s: %s", event, detail);
    alert_write("[DETECT] %s: %s", event, detail);
}

void log_error_event(const char* event, const char* detail) {
    log_write_level(LOG_LEVEL_ERROR, "ERROR", "%s: %s", event, detail);
    alert_write("[ERROR] %s: %s", event, detail);
}

void log_debug_event(const char* event, const char* detail) {
    log_write_level(LOG_LEVEL_DEBUG, "DEBUG", "%s: %s", event, detail);
}

void log_hook_install(const char* hook_name, long addr) {
    log_write_level(LOG_LEVEL_INFO, "HOOK", "安装 %s (addr=0x%lx)", hook_name, addr);
}

void log_config_loaded() {
    log_write("=== 配置加载完成 ===");
    log_write("enable_protect=%d", get_config_bool("enable_protect"));
    log_write("enable_death_keep=%d", get_config_bool("enable_death_keep"));
    log_write("enable_clear_protect=%d", get_config_bool("enable_clear_protect"));
    log_write("enable_drop_protect=%d", get_config_bool("enable_drop_protect"));
    log_write("enable_item_delete_protect=%d", get_config_bool("enable_item_delete_protect"));
    log_write("enable_item_replace_protect=%d", get_config_bool("enable_item_replace_protect"));
    log_write("enable_quick_move_protect=%d", get_config_bool("enable_quick_move_protect"));
    log_write("enable_kill_protect=%d", get_config_bool("enable_kill_protect"));
    log_write("enable_kick_protect=%d", get_config_bool("enable_kick_protect"));
    log_write("enable_cheat_scan=%d", get_config_bool("enable_cheat_scan"));
    log_write("enable_anticheat_scan=%d", get_config_bool("enable_anticheat_scan"));
    log_write("enable_debugger_detect=%d", get_config_bool("enable_debugger_detect"));
    log_write("enable_module_hide=%d", get_config_bool("enable_module_hide"));
    log_write("enable_logging=%d", get_config_bool("enable_logging"));
    log_write("enable_alert=%d", get_config_bool("enable_alert"));
}

void close_log() {
    pthread_mutex_lock(&g_log_lock);

    if (g_log_file != NULL) {
        log_write("=== 日志系统关闭 ===");
        fclose(g_log_file);
        g_log_file = NULL;
    }

    if (g_alert_file != NULL) {
        fclose(g_alert_file);
        g_alert_file = NULL;
    }

    g_log_initialized = false;
    pthread_mutex_unlock(&g_log_lock);

    LOGI("日志系统已关闭");
}

void rotate_log() {
    if (!g_log_initialized) return;

    pthread_mutex_lock(&g_log_lock);

    if (g_log_file != NULL) {
        fseek(g_log_file, 0, SEEK_END);
        long size = ftell(g_log_file);
        fseek(g_log_file, 0, SEEK_SET);

        if (size > 5 * 1024 * 1024) {
            fclose(g_log_file);
            g_log_file = NULL;

            const char* log_path = get_log_path();
            char old_path[512];
            snprintf(old_path, sizeof(old_path), "%s.old", log_path);

            rename(log_path, old_path);
            g_log_file = fopen(log_path, "a");
            if (g_log_file != NULL) {
                log_write("=== 日志轮转完成 ===");
            }
        }
    }

    pthread_mutex_unlock(&g_log_lock);
}

void flush_log() {
    if (!g_log_initialized) return;

    pthread_mutex_lock(&g_log_lock);

    if (g_log_file != NULL) {
        fflush(g_log_file);
    }
    if (g_alert_file != NULL) {
        fflush(g_alert_file);
    }

    pthread_mutex_unlock(&g_log_lock);
}

bool is_log_enabled() {
    return g_log_initialized && g_log_file != NULL;
}

bool is_alert_enabled() {
    return g_log_initialized && g_alert_file != NULL;
}

__attribute__((destructor))
static void log_auto_cleanup() {
    if (g_log_initialized) {
        log_write("=== 程序退出 ===");
        close_log();
    }
}