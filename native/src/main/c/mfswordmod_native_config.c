#include "mfswordmod_native_config.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <unistd.h>
#include <sys/stat.h>
#include <dirent.h>
#include <errno.h>

ModConfig g_config = {0};

static char g_config_dir[512] = {0};
static char g_config_path[512] = {0};
static char g_log_path[512] = {0};
static char g_alert_path[512] = {0};

static const char* DEFAULT_CONFIG =
    "# Minecraft Sword Mod Native Config\n"
    "# 0 = disable, 1 = enable\n"
    "\n"
    "# ============================================================\n"
    "# 防护总开关\n"
    "# ============================================================\n"
    "enable_protect=1\n"
    "\n"
    "# ============================================================\n"
    "# 物品防护\n"
    "# ============================================================\n"
    "enable_death_keep=1\n"
    "enable_clear_protect=1\n"
    "enable_drop_protect=1\n"
    "enable_item_delete_protect=1\n"
    "enable_item_replace_protect=1\n"
    "enable_quick_move_protect=1\n"
    "\n"
    "# ============================================================\n"
    "# 实体防护\n"
    "# ============================================================\n"
    "enable_kill_protect=1\n"
    "enable_kick_protect=1\n"
    "\n"
    "# ============================================================\n"
    "# 战斗功能\n"
    "# ============================================================\n"
    "enable_auto_block=0\n"
    "enable_no_knockback=0\n"
    "enable_no_fall_damage=0\n"
    "enable_infinite_combo=0\n"
    "enable_extended_attack_range=0\n"
    "enable_ignore_armor=0\n"
    "enable_always_critical=0\n"
    "attack_range_value=6\n"
    "armor_penetration_rate=0.7\n"
    "knockback_reserve_rate=0.1\n"
    "\n"
    "# ============================================================\n"
    "# 移动功能\n"
    "# ============================================================\n"
    "enable_ladder_speed=0\n"
    "enable_swim_speed=0\n"
    "enable_no_clip=0\n"
    "enable_multi_jump=0\n"
    "ladder_speed_multiplier=3.0\n"
    "swim_speed_multiplier=2.5\n"
    "multi_jump_count=5\n"
    "\n"
    "# ============================================================\n"
    "# 渲染功能\n"
    "# ============================================================\n"
    "enable_remove_fog=0\n"
    "enable_item_glint=0\n"
    "\n"
    "# ============================================================\n"
    "# 网络功能\n"
    "# ============================================================\n"
    "enable_anti_anticheat=0\n"
    "enable_bypass_chat_filter=0\n"
    "\n"
    "# ============================================================\n"
    "# 物品功能\n"
    "# ============================================================\n"
    "enable_inventory_lock=0\n"
    "\n"
    "# ============================================================\n"
    "# 七彩神剑专属\n"
    "# ============================================================\n"
    "enable_bedrock_break=0\n"
    "enable_rainbow_immunity=0\n"
    "enable_rainbow_kill=0\n";

static char g_game_dir[512] = {0};

void set_game_dir(const char* dir) {
    if (dir == NULL) return;
    strncpy(g_game_dir, dir, sizeof(g_game_dir) - 1);
    g_game_dir[sizeof(g_game_dir) - 1] = 0;
}

const char* get_game_dir() {
    if (g_game_dir[0] == 0) {
        const char* home = getenv("HOME");
        if (home == NULL) home = ".";
        snprintf(g_game_dir, sizeof(g_game_dir), "%s/.minecraft", home);
    }
    return g_game_dir;
}

static const char* get_config_dir() {
    if (g_config_dir[0] != 0) return g_config_dir;
    const char* gameDir = get_game_dir();
    const char* versionDir = getenv("MINECRAFT_VERSION");
    if (versionDir != NULL && versionDir[0] != 0) {
        snprintf(g_config_dir, sizeof(g_config_dir), "%s/versions/%s/config", gameDir, versionDir);
    } else {
        snprintf(g_config_dir, sizeof(g_config_dir), "%s/config", gameDir);
    }
    return g_config_dir;
}

static const char* get_config_path() {
    if (g_config_path[0] != 0) return g_config_path;
    const char* dir = get_config_dir();
    snprintf(g_config_path, sizeof(g_config_path), "%s/mfswordmod.properties", dir);
    return g_config_path;
}

const char* get_log_path() {
    if (g_log_path[0] != 0) return g_log_path;
    const char* dir = get_config_dir();
    snprintf(g_log_path, sizeof(g_log_path), "%s/mfswordmod.log", dir);
    return g_log_path;
}

const char* get_alert_path() {
    if (g_alert_path[0] != 0) return g_alert_path;
    const char* dir = get_config_dir();
    snprintf(g_alert_path, sizeof(g_alert_path), "%s/mfswordmod_alerts.log", dir);
    return g_alert_path;
}

static void mkdir_recursive(const char* path) {
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

static void create_default_config() {
    const char* dir = get_config_dir();
    mkdir_recursive(dir);
    const char* path = get_config_path();
    FILE* fp = fopen(path, "w");
    if (fp == NULL) {
        return;
    }
    fputs(DEFAULT_CONFIG, fp);
    fclose(fp);
}

static void parse_line(char* line) {
    char* p = line;
    while (*p && isspace(*p)) p++;
    if (*p == '#' || *p == '\n' || *p == 0) return;
    char* eq = strchr(p, '=');
    if (eq == NULL) return;
    *eq = 0;
    char* key = p;
    char* value = eq + 1;
    while (isspace(*key)) key++;
    char* end = key + strlen(key) - 1;
    while (end > key && isspace(*end)) { *end = 0; end--; }
    while (isspace(*value)) value++;
    end = value + strlen(value) - 1;
    while (end > value && isspace(*end)) { *end = 0; end--; }

    if (strcmp(key, "enable_protect") == 0) g_config.enable_protect = atoi(value);
    else if (strcmp(key, "enable_death_keep") == 0) g_config.enable_death_keep = atoi(value);
    else if (strcmp(key, "enable_clear_protect") == 0) g_config.enable_clear_protect = atoi(value);
    else if (strcmp(key, "enable_drop_protect") == 0) g_config.enable_drop_protect = atoi(value);
    else if (strcmp(key, "enable_item_delete_protect") == 0) g_config.enable_item_delete_protect = atoi(value);
    else if (strcmp(key, "enable_item_replace_protect") == 0) g_config.enable_item_replace_protect = atoi(value);
    else if (strcmp(key, "enable_quick_move_protect") == 0) g_config.enable_quick_move_protect = atoi(value);
    else if (strcmp(key, "enable_kill_protect") == 0) g_config.enable_kill_protect = atoi(value);
    else if (strcmp(key, "enable_kick_protect") == 0) g_config.enable_kick_protect = atoi(value);
    else if (strcmp(key, "enable_auto_block") == 0) g_config.enable_auto_block = atoi(value);
    else if (strcmp(key, "enable_no_knockback") == 0) g_config.enable_no_knockback = atoi(value);
    else if (strcmp(key, "enable_no_fall_damage") == 0) g_config.enable_no_fall_damage = atoi(value);
    else if (strcmp(key, "enable_infinite_combo") == 0) g_config.enable_infinite_combo = atoi(value);
    else if (strcmp(key, "enable_extended_attack_range") == 0) g_config.enable_extended_attack_range = atoi(value);
    else if (strcmp(key, "enable_ignore_armor") == 0) g_config.enable_ignore_armor = atoi(value);
    else if (strcmp(key, "enable_always_critical") == 0) g_config.enable_always_critical = atoi(value);
    else if (strcmp(key, "attack_range_value") == 0) g_config.attack_range_value = atoi(value);
    else if (strcmp(key, "armor_penetration_rate") == 0) g_config.armor_penetration_rate = atof(value);
    else if (strcmp(key, "knockback_reserve_rate") == 0) g_config.knockback_reserve_rate = atof(value);
    else if (strcmp(key, "enable_ladder_speed") == 0) g_config.enable_ladder_speed = atoi(value);
    else if (strcmp(key, "enable_swim_speed") == 0) g_config.enable_swim_speed = atoi(value);
    else if (strcmp(key, "enable_no_clip") == 0) g_config.enable_no_clip = atoi(value);
    else if (strcmp(key, "enable_multi_jump") == 0) g_config.enable_multi_jump = atoi(value);
    else if (strcmp(key, "ladder_speed_multiplier") == 0) g_config.ladder_speed_multiplier = atof(value);
    else if (strcmp(key, "swim_speed_multiplier") == 0) g_config.swim_speed_multiplier = atof(value);
    else if (strcmp(key, "multi_jump_count") == 0) g_config.multi_jump_count = atoi(value);
    else if (strcmp(key, "enable_remove_fog") == 0) g_config.enable_remove_fog = atoi(value);
    else if (strcmp(key, "enable_item_glint") == 0) g_config.enable_item_glint = atoi(value);
    else if (strcmp(key, "enable_anti_anticheat") == 0) g_config.enable_anti_anticheat = atoi(value);
    else if (strcmp(key, "enable_bypass_chat_filter") == 0) g_config.enable_bypass_chat_filter = atoi(value);
    else if (strcmp(key, "enable_inventory_lock") == 0) g_config.enable_inventory_lock = atoi(value);
    else if (strcmp(key, "enable_bedrock_break") == 0) g_config.enable_bedrock_break = atoi(value);
    else if (strcmp(key, "enable_rainbow_immunity") == 0) g_config.enable_rainbow_immunity = atoi(value);
    else if (strcmp(key, "enable_rainbow_kill") == 0) g_config.enable_rainbow_kill = atoi(value);
}

void load_config() {
    memset(&g_config, 0, sizeof(ModConfig));
    g_config.multi_jump_count = 5;
    g_config.attack_range_value = 6;
    g_config.ladder_speed_multiplier = 3.0f;
    g_config.swim_speed_multiplier = 2.5f;
    g_config.armor_penetration_rate = 0.7f;
    g_config.knockback_reserve_rate = 0.1f;
    g_config.enable_protect = 1;
    g_config.enable_death_keep = 1;
    g_config.enable_clear_protect = 1;
    g_config.enable_drop_protect = 1;
    g_config.enable_item_delete_protect = 1;
    g_config.enable_item_replace_protect = 1;
    g_config.enable_quick_move_protect = 1;
    g_config.enable_kill_protect = 1;
    g_config.enable_kick_protect = 1;

    const char* path = get_config_path();
    FILE* fp = fopen(path, "r");
    if (fp == NULL) {
        create_default_config();
        fp = fopen(path, "r");
        if (fp == NULL) return;
    }
    char line[512];
    while (fgets(line, sizeof(line), fp)) {
        parse_line(line);
    }
    fclose(fp);
}

void save_config() {
    const char* dir = get_config_dir();
    mkdir_recursive(dir);
    const char* path = get_config_path();
    FILE* fp = fopen(path, "w");
    if (fp == NULL) return;

    fprintf(fp, "# Minecraft Sword Mod Native Config\n");
    fprintf(fp, "# 配置文件位置: %s\n", path);
    fprintf(fp, "# 0 = 关闭, 1 = 开启\n\n");

    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "# 防护总开关\n");
    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "enable_protect=%d\n\n", g_config.enable_protect);

    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "# 物品防护\n");
    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "enable_death_keep=%d\n", g_config.enable_death_keep);
    fprintf(fp, "enable_clear_protect=%d\n", g_config.enable_clear_protect);
    fprintf(fp, "enable_drop_protect=%d\n", g_config.enable_drop_protect);
    fprintf(fp, "enable_item_delete_protect=%d\n", g_config.enable_item_delete_protect);
    fprintf(fp, "enable_item_replace_protect=%d\n", g_config.enable_item_replace_protect);
    fprintf(fp, "enable_quick_move_protect=%d\n\n", g_config.enable_quick_move_protect);

    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "# 实体防护\n");
    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "enable_kill_protect=%d\n", g_config.enable_kill_protect);
    fprintf(fp, "enable_kick_protect=%d\n\n", g_config.enable_kick_protect);

    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "# 战斗功能\n");
    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "enable_auto_block=%d\n", g_config.enable_auto_block);
    fprintf(fp, "enable_no_knockback=%d\n", g_config.enable_no_knockback);
    fprintf(fp, "enable_no_fall_damage=%d\n", g_config.enable_no_fall_damage);
    fprintf(fp, "enable_infinite_combo=%d\n", g_config.enable_infinite_combo);
    fprintf(fp, "enable_extended_attack_range=%d\n", g_config.enable_extended_attack_range);
    fprintf(fp, "enable_ignore_armor=%d\n", g_config.enable_ignore_armor);
    fprintf(fp, "enable_always_critical=%d\n", g_config.enable_always_critical);
    fprintf(fp, "attack_range_value=%d\n", g_config.attack_range_value);
    fprintf(fp, "armor_penetration_rate=%.2f\n", g_config.armor_penetration_rate);
    fprintf(fp, "knockback_reserve_rate=%.2f\n\n", g_config.knockback_reserve_rate);

    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "# 移动功能\n");
    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "enable_ladder_speed=%d\n", g_config.enable_ladder_speed);
    fprintf(fp, "enable_swim_speed=%d\n", g_config.enable_swim_speed);
    fprintf(fp, "enable_no_clip=%d\n", g_config.enable_no_clip);
    fprintf(fp, "enable_multi_jump=%d\n", g_config.enable_multi_jump);
    fprintf(fp, "ladder_speed_multiplier=%.2f\n", g_config.ladder_speed_multiplier);
    fprintf(fp, "swim_speed_multiplier=%.2f\n", g_config.swim_speed_multiplier);
    fprintf(fp, "multi_jump_count=%d\n\n", g_config.multi_jump_count);

    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "# 渲染功能\n");
    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "enable_remove_fog=%d\n", g_config.enable_remove_fog);
    fprintf(fp, "enable_item_glint=%d\n\n", g_config.enable_item_glint);

    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "# 网络功能\n");
    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "enable_anti_anticheat=%d\n", g_config.enable_anti_anticheat);
    fprintf(fp, "enable_bypass_chat_filter=%d\n\n", g_config.enable_bypass_chat_filter);

    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "# 物品功能\n");
    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "enable_inventory_lock=%d\n\n", g_config.enable_inventory_lock);

    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "# 七彩神剑专属\n");
    fprintf(fp, "# ============================================================\n");
    fprintf(fp, "enable_bedrock_break=%d\n", g_config.enable_bedrock_break);
    fprintf(fp, "enable_rainbow_immunity=%d\n", g_config.enable_rainbow_immunity);
    fprintf(fp, "enable_rainbow_kill=%d\n", g_config.enable_rainbow_kill);

    fclose(fp);
}

void reset_config() {
    memset(&g_config, 0, sizeof(ModConfig));
    g_config.multi_jump_count = 5;
    g_config.attack_range_value = 6;
    g_config.ladder_speed_multiplier = 3.0f;
    g_config.swim_speed_multiplier = 2.5f;
    g_config.armor_penetration_rate = 0.7f;
    g_config.knockback_reserve_rate = 0.1f;
    g_config.enable_protect = 1;
    g_config.enable_death_keep = 1;
    g_config.enable_clear_protect = 1;
    g_config.enable_drop_protect = 1;
    g_config.enable_item_delete_protect = 1;
    g_config.enable_item_replace_protect = 1;
    g_config.enable_quick_move_protect = 1;
    g_config.enable_kill_protect = 1;
    g_config.enable_kick_protect = 1;
    save_config();
}

int get_config_int(const char* key) {
    if (strcmp(key, "multi_jump_count") == 0) return g_config.multi_jump_count;
    if (strcmp(key, "attack_range_value") == 0) return g_config.attack_range_value;
    return 0;
}

float get_config_float(const char* key) {
    if (strcmp(key, "ladder_speed_multiplier") == 0) return g_config.ladder_speed_multiplier;
    if (strcmp(key, "swim_speed_multiplier") == 0) return g_config.swim_speed_multiplier;
    if (strcmp(key, "armor_penetration_rate") == 0) return g_config.armor_penetration_rate;
    if (strcmp(key, "knockback_reserve_rate") == 0) return g_config.knockback_reserve_rate;
    return 0.0f;
}

int get_config_bool(const char* key) {
    if (strcmp(key, "enable_protect") == 0) return g_config.enable_protect;
    if (strcmp(key, "enable_death_keep") == 0) return g_config.enable_death_keep;
    if (strcmp(key, "enable_clear_protect") == 0) return g_config.enable_clear_protect;
    if (strcmp(key, "enable_drop_protect") == 0) return g_config.enable_drop_protect;
    if (strcmp(key, "enable_item_delete_protect") == 0) return g_config.enable_item_delete_protect;
    if (strcmp(key, "enable_item_replace_protect") == 0) return g_config.enable_item_replace_protect;
    if (strcmp(key, "enable_quick_move_protect") == 0) return g_config.enable_quick_move_protect;
    if (strcmp(key, "enable_kill_protect") == 0) return g_config.enable_kill_protect;
    if (strcmp(key, "enable_kick_protect") == 0) return g_config.enable_kick_protect;
    if (strcmp(key, "enable_auto_block") == 0) return g_config.enable_auto_block;
    if (strcmp(key, "enable_no_knockback") == 0) return g_config.enable_no_knockback;
    if (strcmp(key, "enable_no_fall_damage") == 0) return g_config.enable_no_fall_damage;
    if (strcmp(key, "enable_infinite_combo") == 0) return g_config.enable_infinite_combo;
    if (strcmp(key, "enable_extended_attack_range") == 0) return g_config.enable_extended_attack_range;
    if (strcmp(key, "enable_ignore_armor") == 0) return g_config.enable_ignore_armor;
    if (strcmp(key, "enable_always_critical") == 0) return g_config.enable_always_critical;
    if (strcmp(key, "enable_ladder_speed") == 0) return g_config.enable_ladder_speed;
    if (strcmp(key, "enable_swim_speed") == 0) return g_config.enable_swim_speed;
    if (strcmp(key, "enable_no_clip") == 0) return g_config.enable_no_clip;
    if (strcmp(key, "enable_multi_jump") == 0) return g_config.enable_multi_jump;
    if (strcmp(key, "enable_remove_fog") == 0) return g_config.enable_remove_fog;
    if (strcmp(key, "enable_item_glint") == 0) return g_config.enable_item_glint;
    if (strcmp(key, "enable_anti_anticheat") == 0) return g_config.enable_anti_anticheat;
    if (strcmp(key, "enable_bypass_chat_filter") == 0) return g_config.enable_bypass_chat_filter;
    if (strcmp(key, "enable_inventory_lock") == 0) return g_config.enable_inventory_lock;
    if (strcmp(key, "enable_bedrock_break") == 0) return g_config.enable_bedrock_break;
    if (strcmp(key, "enable_rainbow_immunity") == 0) return g_config.enable_rainbow_immunity;
    if (strcmp(key, "enable_rainbow_kill") == 0) return g_config.enable_rainbow_kill;
    return 0;
}

void set_config_int(const char* key, int value) {
    if (strcmp(key, "multi_jump_count") == 0) g_config.multi_jump_count = value;
    if (strcmp(key, "attack_range_value") == 0) g_config.attack_range_value = value;
    save_config();
}

void set_config_float(const char* key, float value) {
    if (strcmp(key, "ladder_speed_multiplier") == 0) g_config.ladder_speed_multiplier = value;
    if (strcmp(key, "swim_speed_multiplier") == 0) g_config.swim_speed_multiplier = value;
    if (strcmp(key, "armor_penetration_rate") == 0) g_config.armor_penetration_rate = value;
    if (strcmp(key, "knockback_reserve_rate") == 0) g_config.knockback_reserve_rate = value;
    save_config();
}

void set_config_bool(const char* key, int value) {
    if (strcmp(key, "enable_protect") == 0) g_config.enable_protect = value;
    else if (strcmp(key, "enable_death_keep") == 0) g_config.enable_death_keep = value;
    else if (strcmp(key, "enable_clear_protect") == 0) g_config.enable_clear_protect = value;
    else if (strcmp(key, "enable_drop_protect") == 0) g_config.enable_drop_protect = value;
    else if (strcmp(key, "enable_item_delete_protect") == 0) g_config.enable_item_delete_protect = value;
    else if (strcmp(key, "enable_item_replace_protect") == 0) g_config.enable_item_replace_protect = value;
    else if (strcmp(key, "enable_quick_move_protect") == 0) g_config.enable_quick_move_protect = value;
    else if (strcmp(key, "enable_kill_protect") == 0) g_config.enable_kill_protect = value;
    else if (strcmp(key, "enable_kick_protect") == 0) g_config.enable_kick_protect = value;
    else if (strcmp(key, "enable_auto_block") == 0) g_config.enable_auto_block = value;
    else if (strcmp(key, "enable_no_knockback") == 0) g_config.enable_no_knockback = value;
    else if (strcmp(key, "enable_no_fall_damage") == 0) g_config.enable_no_fall_damage = value;
    else if (strcmp(key, "enable_infinite_combo") == 0) g_config.enable_infinite_combo = value;
    else if (strcmp(key, "enable_extended_attack_range") == 0) g_config.enable_extended_attack_range = value;
    else if (strcmp(key, "enable_ignore_armor") == 0) g_config.enable_ignore_armor = value;
    else if (strcmp(key, "enable_always_critical") == 0) g_config.enable_always_critical = value;
    else if (strcmp(key, "enable_ladder_speed") == 0) g_config.enable_ladder_speed = value;
    else if (strcmp(key, "enable_swim_speed") == 0) g_config.enable_swim_speed = value;
    else if (strcmp(key, "enable_no_clip") == 0) g_config.enable_no_clip = value;
    else if (strcmp(key, "enable_multi_jump") == 0) g_config.enable_multi_jump = value;
    else if (strcmp(key, "enable_remove_fog") == 0) g_config.enable_remove_fog = value;
    else if (strcmp(key, "enable_item_glint") == 0) g_config.enable_item_glint = value;
    else if (strcmp(key, "enable_anti_anticheat") == 0) g_config.enable_anti_anticheat = value;
    else if (strcmp(key, "enable_bypass_chat_filter") == 0) g_config.enable_bypass_chat_filter = value;
    else if (strcmp(key, "enable_inventory_lock") == 0) g_config.enable_inventory_lock = value;
    else if (strcmp(key, "enable_bedrock_break") == 0) g_config.enable_bedrock_break = value;
    else if (strcmp(key, "enable_rainbow_immunity") == 0) g_config.enable_rainbow_immunity = value;
    else if (strcmp(key, "enable_rainbow_kill") == 0) g_config.enable_rainbow_kill = value;
    save_config();
}