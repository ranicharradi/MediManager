package com.example.medimanager.utils;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtils {

    private static final String PREFIX = "pbkdf2";
    private static final String DELIMITER = "$";
    private static final int ITERATIONS = 12000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BYTES = 32;

    private PasswordUtils() {
        throw new AssertionError("No instances.");
    }

    public static String hashPassword(String password) {
        if (password == null) {
            return null;
        }

        byte[] salt = new byte[SALT_BYTES];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(password, salt, ITERATIONS, KEY_BYTES);

        String saltEncoded = Base64.encodeToString(salt, Base64.NO_WRAP);
        String hashEncoded = Base64.encodeToString(hash, Base64.NO_WRAP);

        return PREFIX + DELIMITER + ITERATIONS + DELIMITER + saltEncoded + DELIMITER + hashEncoded;
    }

    public static boolean verifyPassword(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }

        if (!isHashed(stored)) {
            return stored.equals(password);
        }

        String[] parts = stored.split("\\$");
        if (parts.length != 4) {
            return false;
        }

        int iterations;
        try {
            iterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }

        byte[] salt = Base64.decode(parts[2], Base64.NO_WRAP);
        byte[] expected = Base64.decode(parts[3], Base64.NO_WRAP);
        try {
            byte[] actual = pbkdf2(password, salt, iterations, expected.length);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalStateException e) {
            return false;
        }
    }

    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith(PREFIX + DELIMITER);
    }

    private static byte[] pbkdf2(String password, byte[] salt, int iterations, int keyBytes) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, keyBytes * 8);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (Exception firstFailure) {
            try {
                return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).getEncoded();
            } catch (Exception fallbackFailure) {
                throw new IllegalStateException("PBKDF2 unavailable", fallbackFailure);
            }
        } finally {
            spec.clearPassword();
        }
    }
}
