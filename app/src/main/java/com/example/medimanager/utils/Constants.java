package com.example.medimanager.utils;

public class Constants {

    // Intent Extras
    public static final String EXTRA_PATIENT_ID = "PATIENT_ID";
    public static final String EXTRA_CONSULTATION_ID = "CONSULTATION_ID";
    public static final String EXTRA_APPOINTMENT_ID = "APPOINTMENT_ID";
    public static final String EXTRA_IS_EDIT_MODE = "IS_EDIT_MODE";

    // Appointment Status
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_SCHEDULED = "scheduled";
    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    // Date Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DISPLAY = "MMM dd, yyyy";
    public static final String TIME_FORMAT = "hh:mm a";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    // Preferences
    public static final String PREFS_NAME = "MediManagerPrefs";
    public static final String PREF_NOTIFICATIONS_ENABLED = "notifications_enabled";
    public static final String PREF_FIRST_LAUNCH = "first_launch";
    public static final String PREF_DARK_MODE = "dark_mode";
    public static final String PREF_IS_LOGGED_IN = "is_logged_in";
    public static final String PREF_IS_DOCTOR = "is_doctor";
    public static final String PREF_USER_EMAIL = "user_email";
    public static final String PREF_USER_ID = "user_id";
    public static final String PREF_USER_NAME = "user_name";

    // Validation
    public static final int MIN_PHONE_LENGTH = 10;
    public static final int MIN_PASSWORD_LENGTH = 6;

    // Database
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "medimanager.db";

    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}
