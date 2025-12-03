package com.example.medimanager.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medimanager.R;
import com.example.medimanager.database.UserDAO;
import com.example.medimanager.databinding.ActivityEditProfileBinding;
import com.example.medimanager.models.User;
import com.example.medimanager.utils.Constants;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private SharedPreferences sharedPreferences;
    private UserDAO userDAO;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        userDAO = new UserDAO(this);

        setupToolbar();
        loadUserData();
        setupListeners();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadUserData() {
        String email = sharedPreferences.getString(Constants.PREF_USER_EMAIL, "");
        currentUser = userDAO.getUserByEmail(email);

        if (currentUser != null) {
            binding.etFirstName.setText(currentUser.getFirstName());
            binding.etLastName.setText(currentUser.getLastName());
            binding.etEmail.setText(currentUser.getEmail());
            
            // Strip +216 prefix for phone display
            String phone = currentUser.getPhone();
            if (phone != null && phone.startsWith("+216 ")) {
                phone = phone.substring(5);
            }
            binding.etPhone.setText(phone);
        }
    }

    private void setupListeners() {
        binding.btnSave.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
                return;
            }

            String firstName = binding.etFirstName.getText().toString().trim();
            String lastName = binding.etLastName.getText().toString().trim();
            String phone = binding.etPhone.getText().toString().trim();

            if (firstName.isEmpty() || lastName.isEmpty()) {
                Toast.makeText(this, R.string.required_field, Toast.LENGTH_SHORT).show();
                return;
            }

            // Update user object
            currentUser.setFirstName(firstName);
            currentUser.setLastName(lastName);
            
            // Prepend +216 prefix to phone
            String fullPhone = phone.isEmpty() ? "" : "+216 " + phone;
            currentUser.setPhone(fullPhone);

            // Save to database
            int result = userDAO.updateUser(currentUser);

            if (result > 0) {
                // Update stored name in SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(Constants.PREF_USER_NAME, currentUser.getFullName());
                editor.apply();

                Toast.makeText(this, R.string.profile_updated, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
