package com.example.medimanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Thin wrapper around SharedPreferences for login session data.
 */
public class SessionManager {

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        this.preferences = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveLoginSession(long userId, String email, String name, boolean isDoctor) {
        preferences.edit()
                .putBoolean(Constants.PREF_IS_LOGGED_IN, true)
                .putBoolean(Constants.PREF_IS_DOCTOR, isDoctor)
                .putLong(Constants.PREF_USER_ID, userId)
                .putString(Constants.PREF_USER_EMAIL, email)
                .putString(Constants.PREF_USER_NAME, name)
                .apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(Constants.PREF_IS_LOGGED_IN, false);
    }

    public boolean isDoctor() {
        return preferences.getBoolean(Constants.PREF_IS_DOCTOR, false);
    }

    public int getUserId() {
        return (int) preferences.getLong(Constants.PREF_USER_ID, -1);
    }

    public String getUserEmail() {
        return preferences.getString(Constants.PREF_USER_EMAIL, "");
    }

    public String getUserName() {
        return preferences.getString(Constants.PREF_USER_NAME, "");
    }

    public void clearSession() {
        preferences.edit()
                .remove(Constants.PREF_IS_LOGGED_IN)
                .remove(Constants.PREF_IS_DOCTOR)
                .remove(Constants.PREF_USER_ID)
                .remove(Constants.PREF_USER_EMAIL)
                .remove(Constants.PREF_USER_NAME)
                .apply();
    }
}
