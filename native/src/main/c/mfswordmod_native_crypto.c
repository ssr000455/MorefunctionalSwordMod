#include "mfswordmod_native_config.h"
#include <jni.h>
#include <string.h>
#include <stdbool.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>

#define LOGI(...) printf("[mfswordmod] " __VA_ARGS__)
#define LOGE(...) printf("[mfswordmod] " __VA_ARGS__)
#define LOGW(...) printf("[mfswordmod] " __VA_ARGS__)

typedef struct {
    uint32_t round_keys[60];
} AES256_CTX;

static const uint8_t SBOX[256] = {
    0x63,0x7c,0x77,0x7b,0xf2,0x6b,0x6f,0xc5,0x30,0x01,0x67,0x2b,0xfe,0xd7,0xab,0x76,
    0xca,0x82,0xc9,0x7d,0xfa,0x59,0x47,0xf0,0xad,0xd4,0xa2,0xaf,0x9c,0xa4,0x72,0xc0,
    0xb7,0xfd,0x93,0x26,0x36,0x3f,0xf7,0xcc,0x34,0xa5,0xe5,0xf1,0x71,0xd8,0x31,0x15,
    0x04,0xc7,0x23,0xc3,0x18,0x96,0x05,0x9a,0x07,0x12,0x80,0xe2,0xeb,0x27,0xb2,0x75,
    0x09,0x83,0x2c,0x1a,0x1b,0x6e,0x5a,0xa0,0x52,0x3b,0xd6,0xb3,0x29,0xe3,0x2f,0x84,
    0x53,0xd1,0x00,0xed,0x20,0xfc,0xb1,0x5b,0x6a,0xcb,0xbe,0x39,0x4a,0x4c,0x58,0xcf,
    0xd0,0xef,0xaa,0xfb,0x43,0x4d,0x33,0x85,0x45,0xf9,0x02,0x7f,0x50,0x3c,0x9f,0xa8,
    0x51,0xa3,0x40,0x8f,0x92,0x9d,0x38,0xf5,0xbc,0xb6,0xda,0x21,0x10,0xff,0xf3,0xd2,
    0xcd,0x0c,0x13,0xec,0x5f,0x97,0x44,0x17,0xc4,0xa7,0x7e,0x3d,0x64,0x5d,0x19,0x73,
    0x60,0x81,0x4f,0xdc,0x22,0x2a,0x90,0x88,0x46,0xee,0xb8,0x14,0xde,0x5e,0x0b,0xdb,
    0xe0,0x32,0x3a,0x0a,0x49,0x06,0x24,0x5c,0xc2,0xd3,0xac,0x62,0x91,0x95,0xe4,0x79,
    0xe7,0xc8,0x37,0x6d,0x8d,0xd5,0x4e,0xa9,0x6c,0x56,0xf4,0xea,0x65,0x7a,0xae,0x08,
    0xba,0x78,0x25,0x2e,0x1c,0xa6,0xb4,0xc6,0xe8,0xdd,0x74,0x1f,0x4b,0xbd,0x8b,0x8a,
    0x70,0x3e,0xb5,0x66,0x48,0x03,0xf6,0x0e,0x61,0x35,0x57,0xb9,0x86,0xc1,0x1d,0x9e,
    0xe1,0xf8,0x98,0x11,0x69,0xd9,0x8e,0x94,0x9b,0x1e,0x87,0xe9,0xce,0x55,0x28,0xdf,
    0x8c,0xa1,0x89,0x0d,0xbf,0xe6,0x42,0x68,0x41,0x99,0x2d,0x0f,0xb0,0x54,0xbb,0x16
};

static const uint8_t INV_SBOX[256] = {
    0x52,0x09,0x6a,0xd5,0x30,0x36,0xa5,0x38,0xbf,0x40,0xa3,0x9e,0x81,0xf3,0xd7,0xfb,
    0x7c,0xe3,0x39,0x82,0x9b,0x2f,0xff,0x87,0x34,0x8e,0x43,0x44,0xc4,0xde,0xe9,0xcb,
    0x54,0x7b,0x94,0x32,0xa6,0xc2,0x23,0x3d,0xee,0x4c,0x95,0x0b,0x42,0xfa,0xc3,0x4e,
    0x08,0x2e,0xa1,0x66,0x28,0xd9,0x24,0xb2,0x76,0x5b,0xa2,0x49,0x6d,0x8b,0xd1,0x25,
    0x72,0xf8,0xf6,0x64,0x86,0x68,0x98,0x16,0xd4,0xa4,0x5c,0xcc,0x5d,0x65,0xb6,0x92,
    0x6c,0x70,0x48,0x50,0xfd,0xed,0xb9,0xda,0x5e,0x15,0x46,0x57,0xa7,0x8d,0x9d,0x84,
    0x90,0xd8,0xab,0x00,0x8c,0xbc,0xd3,0x0a,0xf7,0xe4,0x58,0x05,0xb8,0xb3,0x45,0x06,
    0xd0,0x2c,0x1e,0x8f,0xca,0x3f,0x0f,0x02,0xc1,0xaf,0xbd,0x03,0x01,0x13,0x8a,0x6b,
    0x3a,0x91,0x11,0x41,0x4f,0x67,0xdc,0xea,0x97,0xf2,0xcf,0xce,0xf0,0xb4,0xe6,0x73,
    0x96,0xac,0x74,0x22,0xe7,0xad,0x35,0x85,0xe2,0xf9,0x37,0xe8,0x1c,0x75,0xdf,0x6e,
    0x47,0xf1,0x1a,0x71,0x1d,0x29,0xc5,0x89,0x6f,0xb7,0x62,0x0e,0xaa,0x18,0xbe,0x1b,
    0xfc,0x56,0x3e,0x4b,0xc6,0xd2,0x79,0x20,0x9a,0xdb,0xc0,0xfe,0x78,0xcd,0x5a,0xf4,
    0x1f,0xdd,0xa8,0x33,0x88,0x07,0xc7,0x31,0xb1,0x12,0x10,0x59,0x27,0x80,0xec,0x5f,
    0x60,0x51,0x7f,0xa9,0x19,0xb5,0x4a,0x0d,0x2d,0xe5,0x7a,0x9f,0x93,0xc9,0x9c,0xef,
    0xa0,0xe0,0x3b,0x4d,0xae,0x2a,0xf5,0xb0,0xc8,0xeb,0xbb,0x3c,0x83,0x53,0x99,0x61,
    0x17,0x2b,0x04,0x7e,0xba,0x77,0xd6,0x26,0xe1,0x69,0x14,0x63,0x55,0x21,0x0c,0x7d
};

static const uint32_t RCON[10] = {
    0x01000000, 0x02000000, 0x04000000, 0x08000000, 0x10000000,
    0x20000000, 0x40000000, 0x80000000, 0x1b000000, 0x36000000
};

static uint32_t sub_word(uint32_t word) {
    uint32_t result = 0;
    result |= SBOX[(word >> 24) & 0xFF] << 24;
    result |= SBOX[(word >> 16) & 0xFF] << 16;
    result |= SBOX[(word >> 8) & 0xFF] << 8;
    result |= SBOX[word & 0xFF];
    return result;
}

static uint32_t rot_word(uint32_t word) {
    return (word << 8) | (word >> 24);
}

static void aes256_key_expansion(const uint8_t* key, AES256_CTX* ctx) {
    uint32_t* round_keys = ctx->round_keys;

    for (int i = 0; i < 8; i++) {
        round_keys[i] = (key[i*4] << 24) | (key[i*4+1] << 16) | (key[i*4+2] << 8) | key[i*4+3];
    }

    for (int i = 8; i < 60; i++) {
        uint32_t temp = round_keys[i-1];
        if (i % 8 == 0) {
            temp = sub_word(rot_word(temp)) ^ RCON[(i/8)-1];
        } else if (i % 8 == 4) {
            temp = sub_word(temp);
        }
        round_keys[i] = round_keys[i-8] ^ temp;
    }
}

static void add_round_key(uint8_t* state, const uint32_t* round_key, int round) {
    for (int i = 0; i < 4; i++) {
        state[i*4] ^= (round_key[round * 4 + i] >> 24) & 0xFF;
        state[i*4+1] ^= (round_key[round * 4 + i] >> 16) & 0xFF;
        state[i*4+2] ^= (round_key[round * 4 + i] >> 8) & 0xFF;
        state[i*4+3] ^= round_key[round * 4 + i] & 0xFF;
    }
}

static void sub_bytes(uint8_t* state) {
    for (int i = 0; i < 16; i++) {
        state[i] = SBOX[state[i]];
    }
}

static void inv_sub_bytes(uint8_t* state) {
    for (int i = 0; i < 16; i++) {
        state[i] = INV_SBOX[state[i]];
    }
}

static void shift_rows(uint8_t* state) {
    uint8_t temp[16];
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            temp[i*4+j] = state[i*4+((j+i) % 4)];
        }
    }
    memcpy(state, temp, 16);
}

static void inv_shift_rows(uint8_t* state) {
    uint8_t temp[16];
    for (int i = 0; i < 4; i++) {
        for (int j = 0; j < 4; j++) {
            temp[i*4+j] = state[i*4+((j-i+4) % 4)];
        }
    }
    memcpy(state, temp, 16);
}

static uint8_t gf_mul(uint8_t a, uint8_t b) {
    uint8_t result = 0;
    for (int i = 0; i < 8; i++) {
        if (b & 1) result ^= a;
        uint8_t high = a & 0x80;
        a <<= 1;
        if (high) a ^= 0x1B;
        b >>= 1;
    }
    return result;
}

static void mix_columns(uint8_t* state) {
    uint8_t temp[16];
    for (int i = 0; i < 4; i++) {
        temp[i*4] = gf_mul(state[i*4], 2) ^ gf_mul(state[i*4+1], 3) ^ state[i*4+2] ^ state[i*4+3];
        temp[i*4+1] = state[i*4] ^ gf_mul(state[i*4+1], 2) ^ gf_mul(state[i*4+2], 3) ^ state[i*4+3];
        temp[i*4+2] = state[i*4] ^ state[i*4+1] ^ gf_mul(state[i*4+2], 2) ^ gf_mul(state[i*4+3], 3);
        temp[i*4+3] = gf_mul(state[i*4], 3) ^ state[i*4+1] ^ state[i*4+2] ^ gf_mul(state[i*4+3], 2);
    }
    memcpy(state, temp, 16);
}

static void inv_mix_columns(uint8_t* state) {
    uint8_t temp[16];
    for (int i = 0; i < 4; i++) {
        temp[i*4] = gf_mul(state[i*4], 0x0E) ^ gf_mul(state[i*4+1], 0x0B) ^ gf_mul(state[i*4+2], 0x0D) ^ gf_mul(state[i*4+3], 0x09);
        temp[i*4+1] = gf_mul(state[i*4], 0x09) ^ gf_mul(state[i*4+1], 0x0E) ^ gf_mul(state[i*4+2], 0x0B) ^ gf_mul(state[i*4+3], 0x0D);
        temp[i*4+2] = gf_mul(state[i*4], 0x0D) ^ gf_mul(state[i*4+1], 0x09) ^ gf_mul(state[i*4+2], 0x0E) ^ gf_mul(state[i*4+3], 0x0B);
        temp[i*4+3] = gf_mul(state[i*4], 0x0B) ^ gf_mul(state[i*4+1], 0x0D) ^ gf_mul(state[i*4+2], 0x09) ^ gf_mul(state[i*4+3], 0x0E);
    }
    memcpy(state, temp, 16);
}

static void aes256_encrypt_block(const uint8_t* input, uint8_t* output, const AES256_CTX* ctx) {
    uint8_t state[16];
    memcpy(state, input, 16);

    add_round_key(state, ctx->round_keys, 0);

    for (int round = 1; round < 14; round++) {
        sub_bytes(state);
        shift_rows(state);
        mix_columns(state);
        add_round_key(state, ctx->round_keys, round);
    }

    sub_bytes(state);
    shift_rows(state);
    add_round_key(state, ctx->round_keys, 14);

    memcpy(output, state, 16);
}

static void aes256_decrypt_block(const uint8_t* input, uint8_t* output, const AES256_CTX* ctx) {
    uint8_t state[16];
    memcpy(state, input, 16);

    add_round_key(state, ctx->round_keys, 14);

    for (int round = 13; round > 0; round--) {
        inv_shift_rows(state);
        inv_sub_bytes(state);
        add_round_key(state, ctx->round_keys, round);
        inv_mix_columns(state);
    }

    inv_shift_rows(state);
    inv_sub_bytes(state);
    add_round_key(state, ctx->round_keys, 0);

    memcpy(output, state, 16);
}

static uint8_t g_encryption_key[32] = {0};
static uint8_t g_encryption_iv[16] = {0};
static bool g_key_initialized = false;
static bool g_crypto_enabled = true;

static void get_random_bytes(uint8_t* buf, int len) {
    FILE* fp = fopen("/dev/urandom", "rb");
    if (fp != NULL) {
        size_t read = fread(buf, 1, len, fp);
        fclose(fp);
        if (read == len) return;
    }

    srand((unsigned int)(time(NULL) ^ (getpid() << 16) ^ (clock() << 8)));
    for (int i = 0; i < len; i++) {
        buf[i] = (uint8_t)(rand() % 256);
    }

    long long ts = time(NULL) * 1000000LL + clock();
    for (int i = 0; i < len; i++) {
        buf[i] ^= (uint8_t)(ts >> (i * 8));
    }
}

void generate_dynamic_key(void) {
    if (g_key_initialized && get_config_bool("enable_dynamic_key")) {
        return;
    }

    if (!get_config_bool("enable_protect")) {
        g_crypto_enabled = false;
        LOGI("加密未启用 (enable_protect=0)");
        return;
    }

    g_crypto_enabled = true;
    get_random_bytes(g_encryption_key, 32);
    get_random_bytes(g_encryption_iv, 16);

    time_t now = time(NULL);
    for (int i = 0; i < 8; i++) {
        g_encryption_key[i] ^= (uint8_t)((now >> (i * 8)) & 0xFF);
        g_encryption_iv[i] ^= (uint8_t)((now >> ((i + 8) * 8)) & 0xFF);
    }

    g_key_initialized = true;
    LOGI("动态密钥已生成 (AES-256)");
}

bool is_crypto_initialized(void) {
    return g_key_initialized && g_crypto_enabled;
}

int encrypt_data(const uint8_t* input, int input_len, uint8_t* output, int output_max) {
    if (!g_crypto_enabled || !g_key_initialized) {
        return -1;
    }

    AES256_CTX ctx;
    aes256_key_expansion(g_encryption_key, &ctx);

    int pad_len = 16 - (input_len % 16);
    if (pad_len == 0) pad_len = 16;

    int total_len = input_len + pad_len;
    if (total_len > output_max) return -2;

    uint8_t padded[512];
    if (total_len > 512) return -2;
    memcpy(padded, input, input_len);
    for (int i = 0; i < pad_len; i++) {
        padded[input_len + i] = (uint8_t)pad_len;
    }

    uint8_t prev[16];
    memcpy(prev, g_encryption_iv, 16);

    uint8_t block[16];
    for (int i = 0; i < total_len; i += 16) {
        for (int j = 0; j < 16; j++) {
            block[j] = padded[i + j] ^ prev[j];
        }
        aes256_encrypt_block(block, output + i, &ctx);
        memcpy(prev, output + i, 16);
    }

    return total_len;
}

int decrypt_data(const uint8_t* input, int input_len, uint8_t* output, int output_max) {
    if (!g_crypto_enabled || !g_key_initialized) {
        return -1;
    }

    if (input_len % 16 != 0 || input_len == 0) {
        return -3;
    }

    AES256_CTX ctx;
    aes256_key_expansion(g_encryption_key, &ctx);

    uint8_t prev[16];
    memcpy(prev, g_encryption_iv, 16);

    uint8_t block[16];
    uint8_t decrypted[512];
    if (input_len > 512) return -2;

    for (int i = 0; i < input_len; i += 16) {
        aes256_decrypt_block(input + i, block, &ctx);
        for (int j = 0; j < 16; j++) {
            decrypted[i + j] = block[j] ^ prev[j];
        }
        memcpy(prev, input + i, 16);
    }

    int pad_len = decrypted[input_len - 1];
    if (pad_len < 1 || pad_len > 16) {
        return -4;
    }

    for (int i = input_len - pad_len; i < input_len; i++) {
        if (decrypted[i] != pad_len) {
            return -4;
        }
    }

    int result_len = input_len - pad_len;
    if (result_len > output_max) return -2;

    memcpy(output, decrypted, result_len);
    return result_len;
}

static bool detect_mem_dump(void) {
    if (!get_config_bool("enable_protect")) {
        return false;
    }

    char line[512];
    FILE* fp = fopen("/proc/self/maps", "r");
    if (fp == NULL) return false;

    bool found = false;
    while (fgets(line, sizeof(line), fp)) {
        if (strstr(line, "mem") != NULL ||
            strstr(line, "dump") != NULL ||
            strstr(line, "gdb") != NULL) {
            found = true;
            break;
        }
    }
    fclose(fp);

    fp = fopen("/proc/self/status", "r");
    if (fp != NULL) {
        while (fgets(line, sizeof(line), fp)) {
            if (strncmp(line, "TracerPid:", 10) == 0) {
                int pid = atoi(line + 10);
                if (pid > 0) {
                    fclose(fp);
                    return true;
                }
                break;
            }
        }
        fclose(fp);
    }

    return found;
}

void clear_sensitive_data_on_dump(void) {
    if (!get_config_bool("enable_protect")) {
        return;
    }

    if (detect_mem_dump()) {
        LOGW("检测到内存dump/调试器，清空加密密钥");
        memset(g_encryption_key, 0, sizeof(g_encryption_key));
        memset(g_encryption_iv, 0, sizeof(g_encryption_iv));
        g_key_initialized = false;
    }
}

void periodic_mem_check(void) {
    static int counter = 0;
    counter++;
    if (counter % 10 == 0) {
        clear_sensitive_data_on_dump();
    }
}

int encrypt_nbt_data(const uint8_t* data, int data_len, uint8_t** out) {
    if (!g_crypto_enabled || !g_key_initialized) {
        return -1;
    }

    int max_len = data_len + 32;
    uint8_t* buffer = (uint8_t*)malloc(max_len);
    if (buffer == NULL) return -1;

    int result = encrypt_data(data, data_len, buffer, max_len);
    if (result < 0) {
        free(buffer);
        return -1;
    }

    *out = buffer;
    return result;
}

int decrypt_nbt_data(const uint8_t* data, int data_len, uint8_t** out) {
    if (!g_crypto_enabled || !g_key_initialized) {
        return -1;
    }

    uint8_t* buffer = (uint8_t*)malloc(data_len);
    if (buffer == NULL) return -1;

    int result = decrypt_data(data, data_len, buffer, data_len);
    if (result < 0) {
        free(buffer);
        return -1;
    }

    *out = buffer;
    return result;
}

bool is_key_valid(void) {
    if (!g_key_initialized) return false;
    for (int i = 0; i < 32; i++) {
        if (g_encryption_key[i] != 0) return true;
    }
    return false;
}

void regenerate_key(void) {
    memset(g_encryption_key, 0, sizeof(g_encryption_key));
    memset(g_encryption_iv, 0, sizeof(g_encryption_iv));
    g_key_initialized = false;
    generate_dynamic_key();
    LOGI("密钥已重新生成");
}

void init_crypto(void) {
    if (get_config_bool("enable_protect")) {
        generate_dynamic_key();
        LOGI("加密模块初始化完成 (AES-256-CBC)");
    } else {
        g_crypto_enabled = false;
        LOGI("加密模块未启用 (enable_protect=0)");
    }
}