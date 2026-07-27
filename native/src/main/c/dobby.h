// ================================================================
// 文件名: dobby.h
// 用途: Dobby Hook 框架 - 轻量级 inline hook 库
// 支持: ARM64 / ARM32 / x86_64
// 原始项目: https://github.com/jmpews/Dobby
// ================================================================

#ifndef DOBBY_H
#define DOBBY_H

#include <stddef.h>
#include <stdint.h>
#include <stdbool.h>

#ifdef __cplusplus
extern "C" {
#endif

// ================================================================
// 版本信息
// ================================================================

#define DOBBY_VERSION_MAJOR 1
#define DOBBY_VERSION_MINOR 0
#define DOBBY_VERSION_PATCH 0

// ================================================================
// 核心 API
// ================================================================

/**
 * 安装 inline hook
 * 
 * @param target        要 hook 的函数地址 (必须可执行)
 * @param replace       替换函数地址 (你的 hook 函数)
 * @param origin        输出参数: 原始函数 trampoline 地址 (可传 NULL)
 * @return              0 成功, 其他为错误码
 * 
 * 使用示例:
 *   int (*orig_func)(int);
 *   DobbyHook((void*)target_func, (void*)my_hook, (void**)&orig_func);
 *   int result = orig_func(123); // 调用原函数
 */
int DobbyHook(void* target, void* replace, void** origin);

/**
 * 卸载 hook (恢复原始指令)
 * 
 * @param target        被 hook 的函数地址
 * @return              0 成功, 其他为错误码
 * 
 * 注意: 并非所有平台都支持动态恢复，部分情况需要重启进程
 */
int DobbyUnhook(void* target);

/**
 * 禁用 hook (临时关闭)
 * 
 * @param target        被 hook 的函数地址
 * @return              0 成功, 其他为错误码
 */
int DobbyDisableHook(void* target);

/**
 * 启用 hook (重新开启)
 * 
 * @param target        被 hook 的函数地址
 * @return              0 成功, 其他为错误码
 */
int DobbyEnableHook(void* target);

/**
 * 复制函数到可执行内存 (用于生成 trampoline)
 * 
 * @param address       要复制的函数地址
 * @param size          要复制的字节数
 * @return              新内存地址，失败返回 NULL
 */
void* DobbyCopyCode(void* address, size_t size);

/**
 * 分配可执行内存 (用于动态生成的代码)
 * 
 * @param size          要分配的大小
 * @return              内存地址，失败返回 NULL
 */
void* DobbyAllocExecutableMemory(size_t size);

/**
 * 释放可执行内存
 * 
 * @param address       要释放的内存地址
 * @param size          内存大小
 */
void DobbyFreeExecutableMemory(void* address, size_t size);

/**
 * 重置 hook (恢复原始指令并重新安装)
 * 
 * @param target        被 hook 的函数地址
 * @return              0 成功, 其他为错误码
 */
int DobbyResetHook(void* target);

/**
 * 检查某个地址是否被 hook
 * 
 * @param target        要检查的函数地址
 * @return              true 已 hook, false 未 hook
 */
bool DobbyIsHooked(void* target);

/**
 * 获取 hook 信息
 * 
 * @param target        被 hook 的函数地址
 * @param replace       输出: 当前 hook 函数地址 (可传 NULL)
 * @param origin        输出: 原始函数 trampoline 地址 (可传 NULL)
 * @return              0 成功, 其他为错误码
 */
int DobbyGetHookInfo(void* target, void** replace, void** origin);

/**
 * 设置日志回调 (用于调试)
 * 
 * @param callback      日志回调函数 (传 NULL 关闭日志)
 */
typedef void (*DobbyLogCallback)(const char* message);
void DobbySetLogCallback(DobbyLogCallback callback);

// ================================================================
// 错误码定义
// ================================================================

#define DOBBY_SUCCESS                   0
#define DOBBY_ERROR                     -1
#define DOBBY_ERROR_MEMORY_ALLOC        -2
#define DOBBY_ERROR_INVALID_TARGET      -3
#define DOBBY_ERROR_INVALID_REPLACE     -4
#define DOBBY_ERROR_INSTRUCTION_TOO_LONG -5
#define DOBBY_ERROR_UNSUPPORTED_ARCH    -6
#define DOBBY_ERROR_PERMISSION          -7
#define DOBBY_ERROR_NOT_HOOKED          -8
#define DOBBY_ERROR_ALREADY_HOOKED      -9
#define DOBBY_ERROR_INVALID_ORIGIN      -10

// ================================================================
// 调试开关 (编译时控制)
// ================================================================

#ifndef DOBBY_DEBUG
#define DOBBY_DEBUG 0
#endif

#if DOBBY_DEBUG
#include <stdio.h>
#define DOBBY_LOG(fmt, ...) printf("[Dobby] " fmt "\n", ##__VA_ARGS__)
#define DOBBY_DEBUG_PRINT(fmt, ...) printf("[Dobby DEBUG] " fmt "\n", ##__VA_ARGS__)
#else
#define DOBBY_LOG(fmt, ...)
#define DOBBY_DEBUG_PRINT(fmt, ...)
#endif

// ================================================================
// 内部结构体 (用于追踪 hook 状态)
// ================================================================

typedef struct DobbyHookEntry {
    void* target;               // 原始函数地址
    void* replace;              // Hook 函数地址
    void* origin;               // Trampoline 地址
    uint8_t* original_bytes;    // 原始指令备份
    size_t original_size;       // 原始指令大小
    bool enabled;               // 是否启用
    struct DobbyHookEntry* next; // 链表指针
} DobbyHookEntry;

/**
 * 获取 hook 链表头 (用于调试和遍历)
 */
DobbyHookEntry* DobbyGetHookList(void);

#ifdef __cplusplus
}
#endif

#endif // DOBBY_H