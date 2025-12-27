package com.example.medimanager.utils;

public final class PhoneUtils {

    private static final String PHONE_PREFIX = "+216 ";

    private PhoneUtils() {
        throw new AssertionError("No instances.");
    }

    public static String formatForStorage(String rawInput) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return "";
        }
        String trimmed = rawInput.trim();
        if (trimmed.startsWith(PHONE_PREFIX)) {
            return trimmed;
        }
        return PHONE_PREFIX + trimmed;
    }

    public static String stripPrefixForDisplay(String storedPhone) {
        if (storedPhone == null) {
            return "";
        }
        if (storedPhone.startsWith(PHONE_PREFIX)) {
            return storedPhone.substring(PHONE_PREFIX.length());
        }
        return storedPhone;
    }
}
