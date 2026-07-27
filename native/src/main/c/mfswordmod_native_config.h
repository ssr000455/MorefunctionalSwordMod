#ifndef MFSWORDMOD_NATIVE_CONFIG_H
#define MFSWORDMOD_NATIVE_CONFIG_H

#include <stdbool.h>

typedef struct {
    bool enable_tcp_protect;
    bool enable_auto_block;
    bool enable_no_knockback;
    bool enable_no_fall_damage;
    bool enable_infinite_combo;
    bool enable_anti_anticheat;
    bool enable_kick_protect;
    bool enable_inventory_lock;
    bool enable_extended_attack_range;
    bool enable_ignore_armor;
    bool enable_always_critical;
    bool enable_remove_fog;
    bool enable_item_glint;
    bool enable_bypass_chat_filter;
    bool enable_ladder_speed;
    bool enable_swim_speed;
    bool enable_no_clip;
    bool enable_multi_jump;
    bool enable_bedrock_break;
    bool enable_rainbow_immunity;
    bool enable_rainbow_kill;
    int multi_jump_count;
    int attack_range_value;
    float ladder_speed_multiplier;
    float swim_speed_multiplier;
    float armor_penetration_rate;
    float knockback_reserve_rate;
    char config_path[256];
} ModConfig;

extern ModConfig g_config;

void load_config();
void save_config();
void reset_config();
int get_config_int(const char* key);
float get_config_float(const char* key);
int get_config_bool(const char* key);
void set_config_int(const char* key, int value);
void set_config_float(const char* key, float value);
void set_config_bool(const char* key, int value);

#endif