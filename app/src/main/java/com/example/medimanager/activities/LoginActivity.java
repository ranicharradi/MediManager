package com.example.medimanager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medimanager.R;
import com.example.medimanager.database.UserDAO;
import com.example.medimanager.databinding.ActivityLoginBinding;
import com.example.medimanager.models.User;
import com.example.medimanager.utils.Constants;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private UserDAO userDAO;
    private boolean isDoctorSelected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false)) {
            navigateToMain();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userDAO = new UserDAO(this);

        setupRoleSelection();
        setupClickListeners();
    }

    private void setupRoleSelection() {
        // Doctor card is selected by default
        updateRoleSelection(true);

        binding.cardDoctor.setOnClickListener(v -> updateRoleSelection(true));
        binding.cardPatient.setOnClickListener(v -> updateRoleSelection(false));
    }

    private void updateRoleSelection(boolean selectDoctor) {
        isDoctorSelected = selectDoctor;

        if (selectDoctor) {
            binding.cardDoctor.setStrokeColor(getColor(R.color.primary));
            binding.cardPatient.setStrokeColor(getColor(R.color.transparent));
        } else {
            binding.cardDoctor.setStrokeColor(getColor(R.color.transparent));
            binding.cardPatient.setStrokeColor(getColor(R.color.accent));
        }
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        binding.tvForgotPassword.setOnClickListener(v -> {
            Toast.makeText(this, R.string.feature_coming_soon, Toast.LENGTH_SHORT).show();
        });

        binding.tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
        });
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Clear previous errors
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);

        // Validate input
        if (TextUtils.isEmpty(email)) {
            binding.tilEmail.setError(getString(R.string.enter_email));
            binding.etEmail.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError(getString(R.string.invalid_email));
            binding.etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.required_field));
            binding.etPassword.requestFocus();
            return;
        }

        // For demo purposes, accept any valid email/password combination
        // In production, this would validate against a backend or local database
        performLogin(email, password);
    }

    private void performLogin(String email, String password) {
        String role = isDoctorSelected ? "doctor" : "patient";
        User user = userDAO.authenticateUser(email, password, role);

        if (user == null) {
            Toast.makeText(this, R.string.invalid_credentials, Toast.LENGTH_SHORT).show();
            return;
        }

        // Save login state
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, true);
        editor.putBoolean(Constants.PREF_IS_DOCTOR, user.isDoctor());
        editor.putString(Constants.PREF_USER_EMAIL, email);
        editor.putLong(Constants.PREF_USER_ID, user.getId());
        editor.putString(Constants.PREF_USER_NAME, user.getFullName());
        editor.apply();

        Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
        navigateToMain();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
