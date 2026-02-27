package cn.dong.nexus.core.security.utils;

import cn.hutool.extra.spring.SpringUtil;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
public class AesDecryptUtil {

    public static String decrypt(String ivB64, String dataB64, String keyB64) throws Exception {
        byte[] key = Base64.getDecoder().decode(keyB64);
        byte[] iv = Base64.getDecoder().decode(ivB64);
        byte[] ct = Base64.getDecoder().decode(dataB64);

        if (!(key.length == 16 || key.length == 24 || key.length == 32)) {
            throw new IllegalArgumentException("Invalid AES key length: " + key.length);
        }
        if (iv.length != 16) {
            throw new IllegalArgumentException("Invalid IV length: " + iv.length);
        }

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding"); // PKCS5 == PKCS7(对AES块大小来说)
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));

        byte[] pt = cipher.doFinal(ct);
        return new String(pt, StandardCharsets.UTF_8);
    }

    public static String decrypt(String encrypt) {
        try {
            String aesKey = SpringUtil.getProperty("nexus.aes-key");
            String[] encryption = encrypt.split(":");
            String data = encryption[0];
            String iv = encryption[1];
            return decrypt(iv, data, aesKey);
        } catch (Exception e) {
            log.error("用户密码解密失败：{}", e.getMessage());
            return null;
        }
    }
}
