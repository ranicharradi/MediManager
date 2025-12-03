package com.example.medimanager.activities;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medimanager.databinding.ActivityNotificationSettingsBinding;
import com.example.medimanager.utils.Constants;

public class NotificationSettingsActivity extends AppCompatActivity {

    private ActivityNotificationSettingsBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNotificationSettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);

        setupToolbar();
        loadSettings();
        setupListeners();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadSettings() {
        boolean notificationsEnabled = sharedPreferences.getBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, true);
        binding.switchNotifications.setChecked(notificationsEnabled);
    }

    private void setupListeners() {
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, isChecked);
            editor.apply();
        });
    }
}
