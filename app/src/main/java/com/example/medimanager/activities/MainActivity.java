package com.example.medimanager.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.medimanager.R;
import com.example.medimanager.databinding.ActivityMainBinding;
import com.example.medimanager.fragments.AppointmentsFragment;
import com.example.medimanager.fragments.HomeFragment;
import com.example.medimanager.fragments.PatientHomeFragment;
import com.example.medimanager.fragments.PatientsFragment;
import com.example.medimanager.fragments.ProfileFragment;
import com.example.medimanager.utils.Constants;
import com.example.medimanager.utils.NotificationHelper;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean isDoctor = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        // Check user role
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        isDoctor = prefs.getBoolean(Constants.PREF_IS_DOCTOR, true);

        // Create notification channels
        NotificationHelper.createNotificationChannels(this);

        // Setup navigation based on role
        setupNavigation();

        // Set the initial fragment
        if (savedInstanceState == null) {
            if (isDoctor) {
                loadFragment(new HomeFragment());
            } else {
                loadFragment(new PatientHomeFragment());
            }
            binding.fab.setVisibility(View.GONE);
        }
    }

    private void setupNavigation() {
        if (isDoctor) {
            // Doctor sees full navigation
            binding.bottomNavigation.getMenu().clear();
            binding.bottomNavigation.inflateMenu(R.menu.bottom_navigation);
            setupDoctorNavigation();
        } else {
            // Patient sees limited navigation
            binding.bottomNavigation.getMenu().clear();
            binding.bottomNavigation.inflateMenu(R.menu.bottom_navigation_patient);
            setupPatientNavigation();
        }
    }

    private void setupDoctorNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                binding.fab.setVisibility(View.GONE);
            } else if (itemId == R.id.nav_appointments) {
                selectedFragment = new AppointmentsFragment();
                binding.fab.setVisibility(View.VISIBLE);
                binding.fab.setOnClickListener(v -> {
                    startActivity(new Intent(this, AddAppointmentActivity.class));
                });
            } else if (itemId == R.id.nav_patients) {
                selectedFragment = new PatientsFragment();
                binding.fab.setVisibility(View.VISIBLE);
                binding.fab.setOnClickListener(v -> {
                    startActivity(new Intent(this, AddPatientActivity.class));
                });
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                binding.fab.setVisibility(View.GONE);
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    private void setupPatientNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = new PatientHomeFragment();
                binding.fab.setVisibility(View.GONE);
            } else if (itemId == R.id.nav_appointments) {
                selectedFragment = new AppointmentsFragment();
                binding.fab.setVisibility(View.GONE); // Patients can't add appointments
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                binding.fab.setVisibility(View.GONE);
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });
    }

    public void navigateToAppointments() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_appointments);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        MenuItem themeIcon = menu.findItem(R.id.action_theme);
        if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
            themeIcon.setIcon(R.drawable.ic_light_mode);
        } else {
            themeIcon.setIcon(R.drawable.ic_dark_mode);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_theme) {
            int nightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
