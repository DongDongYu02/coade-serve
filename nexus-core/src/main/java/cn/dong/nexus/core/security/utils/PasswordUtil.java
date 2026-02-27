package cn.dong.nexus.core.security.utils;

import cn.hutool.crypto.digest.BCrypt;

public class PasswordUtil {

    public static String encode(String rawPassword) {
        String salt = BCrypt.gensalt(12);
        return BCrypt.hashpw(rawPassword, salt);
    }

    public static boolean matches(String rawPassword, String storedHash) {
        return storedHash != null && BCrypt.checkpw(rawPassword, storedHash);
    }

}
