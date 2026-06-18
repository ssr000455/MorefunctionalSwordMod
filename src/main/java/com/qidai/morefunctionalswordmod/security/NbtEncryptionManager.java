package com.qidai.morefunctionalswordmod.security;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 七彩神剑NBT加密管理器
 * 使用 AES-GCM 加密，密钥基于玩家 UUID + 服务器固定盐值派生
 */
public class NbtEncryptionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("mfswordmod-encrypt");
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 10000;
    private static final String SECRET_SALT = "MoreFunctionalSwordMod_Salt_2026"; // 服务端固定盐，可更改但必须一致

    // 缓存派生密钥，避免每次加解密都执行 PBKDF2(10000次迭代)
    private static final ConcurrentHashMap<String, SecretKey> KEY_CACHE = new ConcurrentHashMap<>();

    private static SecretKey generateKey(String playerUuid) throws Exception {
        // 从缓存读取
        SecretKey cached = KEY_CACHE.get(playerUuid);
        if (cached != null) return cached;

        String combined = playerUuid + SECRET_SALT;
        PBEKeySpec spec = new PBEKeySpec(combined.toCharArray(), combined.getBytes(StandardCharsets.UTF_8),
                ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        KEY_CACHE.put(playerUuid, key);
        return key;
    }

    /**
     * 加密 NBT 数据
     * @param player 玩家（用于获取UUID）
     * @param data 原始 NBT
     * @return Base64 编码的加密字符串，失败返回 null
     */
    public static String encrypt(ServerPlayerEntity player, NbtCompound data) {
        // 使用 encryptNbt 的字节序列化方案（更可靠）
        return encryptNbt(player, data);
    }

    /**
     * 解密 NBT 数据
     * @param player 玩家
     * @param encryptedBase64 Base64 加密串
     * @return 解密后的 NbtCompound，失败返回 null
     */
    public static NbtCompound decrypt(ServerPlayerEntity player, String encryptedBase64) {
        // 使用 encryptNbt/decryptNbt 的字节序列化方案（更可靠）
        return decryptNbt(player, encryptedBase64);
    }

    // 更好的实现：直接序列化 NBT 到字节数组
    public static byte[] serializeNbt(NbtCompound nbt) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            net.minecraft.nbt.NbtIo.writeCompressed(nbt, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    public static NbtCompound deserializeNbt(byte[] data) {
        try {
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data);
            return net.minecraft.nbt.NbtIo.readCompressed(bais);
        } catch (Exception e) {
            return null;
        }
    }

    // 重新实现加密（使用字节序列化）
    public static String encryptNbt(ServerPlayerEntity player, NbtCompound nbt) {
        try {
            byte[] data = serializeNbt(nbt);
            if (data == null) return null;
            SecretKey key = generateKey(player.getUuid().toString());
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] encrypted = cipher.doFinal(data);
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            LOGGER.error("Encrypt NBT failed", e);
            return null;
        }
    }

    public static NbtCompound decryptNbt(ServerPlayerEntity player, String encryptedBase64) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedBase64);
            if (combined.length < IV_LENGTH) return null;
            byte[] iv = new byte[IV_LENGTH];
            byte[] encrypted = new byte[combined.length - IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH);
            System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);
            SecretKey key = generateKey(player.getUuid().toString());
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] decrypted = cipher.doFinal(encrypted);
            return deserializeNbt(decrypted);
        } catch (Exception e) {
            LOGGER.error("Decrypt NBT failed", e);
            return null;
        }
    }
}
