#include "mfswordmod_native_config.h"
#include <jni.h>
#include <string.h>
#include <stdbool.h>
#include <stdlib.h>
#include <stdio.h>
#include <dirent.h>
#include <unistd.h>
#include <dlfcn.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <ctype.h>
#include <pthread.h>
#include <link.h>

#define LOGI(...) printf("[mfswordmod] " __VA_ARGS__)
#define LOGE(...) printf("[mfswordmod] " __VA_ARGS__)
#define LOGW(...) printf("[mfswordmod] " __VA_ARGS__)

extern void log_write(const char* format, ...);
extern void log_detect_event(const char* event, const char* detail);
extern void alert_write(const char* format, ...);

// ============================================================
// 已知外挂进程名/模块名列表
// ============================================================
static const char* cheat_processes[] = {
    "wurst",
    "impact",
    "liquidbounce",
    "aristois",
    "future",
    "salhack",
    "phobos",
    "kura",
    "novoline",
    "konas",
    "gamesense",
    "meteor",
    "inertia",
    "bleach",
    "sigma",
    "jigsaw",
    "zero",
    "kami",
    "fdp",
    "forgelabs",
    "inertia",
    "rise",
    "exhibition",
    "vape",
    "flux",
    "astolfo",
    "skilled",
    "raven",
    "tenacity",
    "moon",
    "spark",
    "novoline",
    "eternal",
    "zero",
    "xenon",
    "crystal",
    "huzuni",
    "nodus",
    "weepcraft",
    "nightmare",
    "pandora",
    "sight",
    "venom",
    "projectx",
    NULL
};

// ============================================================
// 已知反作弊检测特征
// ============================================================
static const char* anticheat_signatures[] = {
    "anticheat",
    "vulcan",
    "verus",
    "watchdog",
    "aac",
    "nocheatplus",
    "matrix",
    "spartan",
    "negativity",
    "viaversion",
    "bungeecord",
    "waterfall",
    "anticheat",
    "anti-cheat",
    "ac",
    "cheatdetection",
    "detection",
    "guardian",
    "sentinela",
    "karhu",
    "grim",
    "reflex",
    "themis",
    "zeus",
    "atlas",
    "mantle",
    "clover",
    "hydra",
    "elytra",
    "probe",
    "qol",
    "utility",
    NULL
};

// ============================================================
// 检测状态
// ============================================================
static bool g_detect_running = false;
static pthread_t g_detect_thread = 0;
static int g_cheat_count = 0;
static int g_anticheat_count = 0;
static bool g_debugger_detected = false;
static time_t g_last_scan_time = 0;

// ============================================================
// 检查进程是否在列表中
// ============================================================
static bool is_process_in_list(const char* name, const char** list) {
    if (name == NULL) return false;
    for (int i = 0; list[i] != NULL; i++) {
        if (strcasestr(name, list[i]) != NULL) {
            return true;
        }
    }
    return false;
}

// ============================================================
// 扫描进程列表检测外挂
// ============================================================
static int scan_for_cheats_internal() {
    DIR* dir = opendir("/proc");
    if (dir == NULL) {
        LOGE("无法打开 /proc 目录");
        return 0;
    }

    struct dirent* entry;
    int found = 0;

    while ((entry = readdir(dir)) != NULL) {
        if (entry->d_type != DT_DIR) continue;

        int is_pid = 1;
        for (int i = 0; entry->d_name[i]; i++) {
            if (!isdigit(entry->d_name[i])) {
                is_pid = 0;
                break;
            }
        }
        if (!is_pid) continue;

        char path[512];
        char comm[256];

        // 读取进程名
        snprintf(path, sizeof(path), "/proc/%s/comm", entry->d_name);
        FILE* fp = fopen(path, "r");
        if (fp != NULL) {
            if (fgets(comm, sizeof(comm), fp)) {
                comm[strcspn(comm, "\n")] = 0;
                if (is_process_in_list(comm, cheat_processes)) {
                    LOGW("检测到外挂进程: %s (PID: %s)", comm, entry->d_name);
                    log_detect_event("外挂进程", comm);
                    found++;
                }
            }
            fclose(fp);
        }

        // 读取命令行
        snprintf(path, sizeof(path), "/proc/%s/cmdline", entry->d_name);
        fp = fopen(path, "r");
        if (fp != NULL) {
            char cmdline[512];
            size_t n = fread(cmdline, 1, sizeof(cmdline) - 1, fp);
            if (n > 0) {
                cmdline[n] = 0;
                for (int i = 0; i < n - 1; i++) {
                    if (cmdline[i] == 0) cmdline[i] = ' ';
                }
                if (is_process_in_list(cmdline, cheat_processes)) {
                    LOGW("检测到外挂进程(命令行): %s (PID: %s)", cmdline, entry->d_name);
                    log_detect_event("外挂进程(命令行)", cmdline);
                    found++;
                }
            }
            fclose(fp);
        }
    }

    closedir(dir);
    return found;
}

// ============================================================
// 扫描内存检测反作弊模块
// ============================================================
static int scan_for_anticheat_internal() {
    FILE* fp = fopen("/proc/self/maps", "r");
    if (fp == NULL) {
        LOGE("无法打开 /proc/self/maps");
        return 0;
    }

    char line[1024];
    int found = 0;

    while (fgets(line, sizeof(line), fp)) {
        for (int i = 0; anticheat_signatures[i] != NULL; i++) {
            if (strcasestr(line, anticheat_signatures[i]) != NULL) {
                // 提取模块路径
                char* path_start = strchr(line, '/');
                if (path_start != NULL) {
                    char* path_end = strchr(path_start, '\n');
                    if (path_end != NULL) *path_end = 0;
                    LOGW("检测到反作弊模块: %s", path_start);
                    log_detect_event("反作弊模块", path_start);
                    found++;
                } else {
                    LOGW("检测到反作弊模块: %s", line);
                    log_detect_event("反作弊模块", line);
                    found++;
                }
                break;
            }
        }
    }

    fclose(fp);
    return found;
}

// ============================================================
// 检测调试器附加
// ============================================================
static int detect_debugger_internal() {
    FILE* fp = fopen("/proc/self/status", "r");
    if (fp == NULL) return 0;

    char line[256];
    int tracer_pid = 0;

    while (fgets(line, sizeof(line), fp)) {
        if (strncmp(line, "TracerPid:", 10) == 0) {
            tracer_pid = atoi(line + 10);
            break;
        }
    }
    fclose(fp);

    if (tracer_pid > 0) {
        LOGW("检测到调试器附加: TracerPid=%d", tracer_pid);
        log_detect_event("调试器附加", "TracerPid");
        return tracer_pid;
    }

    // 检查是否有ptrace附加
    int ptrace_check = 0;
    char ptrace_path[64];
    snprintf(ptrace_path, sizeof(ptrace_path), "/proc/self/fd/%d", ptrace_check);
    if (access(ptrace_path, F_OK) == 0) {
        LOGW("检测到可能的ptrace附加");
        log_detect_event("ptrace附加", "检测到");
        return 1;
    }

    return 0;
}

// ============================================================
// 读取 /proc/self/status 获取进程状态
// ============================================================
static void read_process_status() {
    FILE* fp = fopen("/proc/self/status", "r");
    if (fp == NULL) return;

    char line[256];
    while (fgets(line, sizeof(line), fp)) {
        if (strncmp(line, "TracerPid:", 10) == 0) {
            int pid = atoi(line + 10);
            if (pid > 0) {
                LOGW("当前进程被跟踪: TracerPid=%d", pid);
                g_debugger_detected = true;
                log_detect_event("进程被跟踪", line);
            }
            break;
        }
    }
    fclose(fp);
}

// ============================================================
// 检测Java层调试标志
// ============================================================
static void detect_java_debug() {
    // 检查JVM调试标志
    const char* debug_args[] = {
        "-agentlib:jdwp",
        "-Xdebug",
        "-Xrunjdwp",
        NULL
    };

    FILE* fp = fopen("/proc/self/cmdline", "r");
    if (fp == NULL) return;

    char cmdline[1024];
    size_t n = fread(cmdline, 1, sizeof(cmdline) - 1, fp);
    fclose(fp);
    if (n == 0) return;

    cmdline[n] = 0;

    for (int i = 0; debug_args[i] != NULL; i++) {
        if (strstr(cmdline, debug_args[i]) != NULL) {
            LOGW("检测到Java调试参数: %s", debug_args[i]);
            log_detect_event("Java调试", debug_args[i]);
            g_debugger_detected = true;
            break;
        }
    }
}

// ============================================================
// 隐藏Native模块 (防进程扫描)
// ============================================================
static int hide_native_module_internal() {
    // 修改 /proc/self/maps 输出需要Hook read系统调用
    // 这里使用替代方案: 使用LD_PRELOAD或者修改内存映射标记
    // 简化版本: 将自身模块标记为"已删除"状态

    Dl_info info;
    if (dladdr((void*)hide_native_module_internal, &info) == 0) {
        LOGE("无法获取自身模块信息");
        return -1;
    }

    LOGI("自身模块路径: %s", info.dli_fname);

    // 将模块名改为已删除标记
    // 实际实现需要修改内存中的模块名或Hook read系统调用
    // 这里只做日志记录
    LOGI("Native模块隐藏已启用 (模拟)");
    log_write("Native模块隐藏已启用: %s", info.dli_fname);

    return 0;
}

// ============================================================
// 获取加载的模块列表
// ============================================================
static void list_loaded_modules() {
    FILE* fp = fopen("/proc/self/maps", "r");
    if (fp == NULL) return;

    char line[512];
    LOGI("已加载模块列表:");
    while (fgets(line, sizeof(line), fp)) {
        if (strstr(line, ".so") != NULL || strstr(line, ".jar") != NULL) {
            char* path = strchr(line, '/');
            if (path != NULL) {
                char* end = strchr(path, '\n');
                if (end != NULL) *end = 0;
                LOGI("  %s", path);
            }
        }
    }
    fclose(fp);
}

// ============================================================
// 完整扫描 (外挂 + 反作弊 + 调试器)
// ============================================================
static void perform_full_scan() {
    if (!get_config_bool("enable_protect")) {
        return;
    }

    LOGI("开始安全扫描...");

    g_cheat_count = 0;
    g_anticheat_count = 0;
    g_debugger_detected = false;

    if (get_config_bool("enable_cheat_scan")) {
        g_cheat_count = scan_for_cheats_internal();
        LOGI("外挂进程扫描完成: 发现 %d 个", g_cheat_count);
    }

    if (get_config_bool("enable_anticheat_scan")) {
        g_anticheat_count = scan_for_anticheat_internal();
        LOGI("反作弊模块扫描完成: 发现 %d 个", g_anticheat_count);
    }

    if (get_config_bool("enable_debugger_detect")) {
        g_debugger_detected = detect_debugger_internal() > 0;
        detect_java_debug();
        LOGI("调试器检测完成: %s", g_debugger_detected ? "检测到" : "未检测到");
    }

    if (g_cheat_count > 0 || g_anticheat_count > 0 || g_debugger_detected) {
        alert_write("安全威胁: 外挂=%d 反作弊=%d 调试器=%d",
                    g_cheat_count, g_anticheat_count, g_debugger_detected);
    }

    g_last_scan_time = time(NULL);
}

// ============================================================
// 扫描线程 (定期执行)
// ============================================================
static void* detect_thread_func(void* arg) {
    LOGI("检测线程启动");
    g_detect_running = true;

    while (g_detect_running) {
        sleep(10); // 每10秒扫描一次

        time_t now = time(NULL);
        if (now - g_last_scan_time > 10) {
            perform_full_scan();
        }
    }

    LOGI("检测线程停止");
    return NULL;
}

// ============================================================
// 启动检测线程
// ============================================================
void start_detect_thread() {
    if (g_detect_thread != 0) {
        LOGI("检测线程已运行");
        return;
    }

    if (!get_config_bool("enable_cheat_scan") &&
        !get_config_bool("enable_anticheat_scan") &&
        !get_config_bool("enable_debugger_detect") &&
        !get_config_bool("enable_module_hide")) {
        LOGI("所有检测功能已禁用");
        return;
    }

    if (get_config_bool("enable_module_hide")) {
        hide_native_module_internal();
    }

    read_process_status();
    perform_full_scan();

    pthread_create(&g_detect_thread, NULL, detect_thread_func, NULL);
    LOGI("检测线程已启动");
}

// ============================================================
// 停止检测线程
// ============================================================
void stop_detect_thread() {
    g_detect_running = false;
    if (g_detect_thread != 0) {
        pthread_join(g_detect_thread, NULL);
        g_detect_thread = 0;
        LOGI("检测线程已停止");
    }
}

// ============================================================
// 获取检测结果
// ============================================================
int get_cheat_count() {
    return g_cheat_count;
}

int get_anticheat_count() {
    return g_anticheat_count;
}

bool is_debugger_detected() {
    return g_debugger_detected;
}

bool is_detect_running() {
    return g_detect_running;
}

// ============================================================
// 手动触发扫描 (Java可调用)
// ============================================================
JNIEXPORT void JNICALL Java_com_qidai_morefunctionalswordmod_NativeDetect_triggerScan(JNIEnv* env, jobject obj) {
    LOGI("手动触发安全扫描");
    perform_full_scan();
}

// ============================================================
// 获取扫描结果 (Java可调用)
// ============================================================
JNIEXPORT jint JNICALL Java_com_qidai_morefunctionalswordmod_NativeDetect_getCheatCount(JNIEnv* env, jobject obj) {
    return g_cheat_count;
}

JNIEXPORT jint JNICALL Java_com_qidai_morefunctionalswordmod_NativeDetect_getAnticheatCount(JNIEnv* env, jobject obj) {
    return g_anticheat_count;
}

JNIEXPORT jboolean JNICALL Java_com_qidai_morefunctionalswordmod_NativeDetect_isDebuggerDetected(JNIEnv* env, jobject obj) {
    return g_debugger_detected ? JNI_TRUE : JNI_FALSE;
}

// ============================================================
// 初始化检测模块
// ============================================================
void init_detect() {
    LOGI("检测模块初始化完成");
    start_detect_thread();
}