package com.example.medimanager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medimanager.R;
import com.example.medimanager.database.UserDAO;
import com.example.medimanager.databinding.ActivityRegisterBinding;
import com.example.medimanager.models.User;
import com.example.medimanager.utils.Constants;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private UserDAO userDAO;
    private boolean isDoctorSelected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userDAO = new UserDAO(this);

        setupRoleSelection();
        setupClickListeners();
    }

    private void setupRoleSelection() {
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
        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnRegister.setOnClickListener(v -> attemptRegistration());

        binding.tvLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegistration() {
        // Clear previous errors
        binding.tilFirstName.setError(null);
        binding.tilLastName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPhone.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);

        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        // Validate first name
        if (TextUtils.isEmpty(firstName)) {
            binding.tilFirstName.setError(getString(R.string.enter_first_name));
            binding.etFirstName.requestFocus();
            return;
        }

        // Validate last name
        if (TextUtils.isEmpty(lastName)) {
            binding.tilLastName.setError(getString(R.string.enter_last_name));
            binding.etLastName.requestFocus();
            return;
        }

        // Validate email
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

        // Validate password
        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError(getString(R.string.required_field));
            binding.etPassword.requestFocus();
            return;
        }

        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            binding.tilPassword.setError(getString(R.string.password_too_short));
            binding.etPassword.requestFocus();
            return;
        }

        // Validate confirm password
        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.passwords_dont_match));
            binding.etConfirmPassword.requestFocus();
            return;
        }

        // Create user
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(password);
        user.setRole(isDoctorSelected ? "doctor" : "patient");

        // Register user
        long userId = userDAO.registerUser(user);

        if (userId == -1) {
            binding.tilEmail.setError(getString(R.string.email_already_registered));
            binding.etEmail.requestFocus();
            return;
        }

        // Registration successful - auto login
        Toast.makeText(this, R.string.registration_success, Toast.LENGTH_SHORT).show();

        // Save login state
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.PREF_IS_LOGGED_IN, true);
        editor.putBoolean(Constants.PREF_IS_DOCTOR, isDoctorSelected);
        editor.putString(Constants.PREF_USER_EMAIL, email);
        editor.putLong(Constants.PREF_USER_ID, userId);
        editor.apply();

        // Navigate to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
