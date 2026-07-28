#ifndef MFSWORDMOD_NATIVE_CONFIG_H
#define MFSWORDMOD_NATIVE_CONFIG_H

#include <stdbool.h>

typedef struct {
    int enable_protect;
    int enable_death_keep;
    int enable_clear_protect;
    int enable_drop_protect;
    int enable_item_delete_protect;
    int enable_item_replace_protect;
    int enable_quick_move_protect;
    int enable_kill_protect;
    int enable_kick_protect;
    int enable_auto_block;
    int enable_no_knockback;
    int enable_no_fall_damage;
    int enable_infinite_combo;
    int enable_extended_attack_range;
    int enable_ignore_armor;
    int enable_always_critical;
    int attack_range_value;
    float armor_penetration_rate;
    float knockback_reserve_rate;
    int enable_ladder_speed;
    int enable_swim_speed;
    int enable_no_clip;
    int enable_multi_jump;
    float ladder_speed_multiplier;
    float swim_speed_multiplier;
    int multi_jump_count;
    int enable_remove_fog;
    int enable_item_glint;
    int enable_anti_anticheat;
    int enable_bypass_chat_filter;
    int enable_inventory_lock;
    int enable_bedrock_break;
    int enable_rainbow_immunity;
    int enable_rainbow_kill;
} ModConfig;

extern ModConfig g_config;

void set_game_dir(const char* dir);
const char* get_game_dir();
const char* get_config_dir();
const char* get_config_path();
const char* get_log_path();
const char* get_alert_path();
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