package com.udmurt.learn.data;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Пароли никогда не хранятся в открытом виде — только SHA-256 хэш. */
public final class Passwords {

    private Passwords() {
    }

    public static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                builder.append(Character.forDigit((b >> 4) & 0xF, 16));
                builder.append(Character.forDigit(b & 0xF, 16));
            }
            return builder.toString();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось захешировать пароль", e);
        }
    }
}
