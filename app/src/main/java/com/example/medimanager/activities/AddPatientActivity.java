package com.example.medimanager.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medimanager.R;
import com.example.medimanager.database.PatientDAO;
import com.example.medimanager.database.UserDAO;
import com.example.medimanager.databinding.ActivityAddPatientBinding;
import com.example.medimanager.models.Patient;
import com.example.medimanager.models.User;
import com.example.medimanager.utils.Constants;
import com.example.medimanager.utils.DatabaseExporter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddPatientActivity extends AppCompatActivity {

    private ActivityAddPatientBinding binding;

    // Data
    private PatientDAO patientDAO;
    private UserDAO userDAO;
    private Patient currentPatient;
    private boolean isEditMode = false;
    private Calendar selectedDate;
    private int doctorId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPatientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize DAOs
        patientDAO = new PatientDAO(this);
        userDAO = new UserDAO(this);
        
        // Load doctor id
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        doctorId = (int) prefs.getLong(Constants.PREF_USER_ID, -1);
        if (doctorId == -1) {
            Toast.makeText(this, "Unable to determine doctor account", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if editing existing patient
        checkEditMode();

        // Initialize UI
        setupSpinners();
        setupClickListeners();

        // Load patient data if editing
        if (isEditMode && currentPatient != null) {
            loadPatientData();
        }
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("PATIENT_ID")) {
            isEditMode = true;
            int patientId = getIntent().getIntExtra("PATIENT_ID", -1);
            currentPatient = patientDAO.getPatientById(patientId);
        }
    }

    private void setupSpinners() {
        // Gender Spinner
        String[] genders = getResources().getStringArray(R.array.genders);
        ArrayAdapter<String> genderAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                genders
        );
        binding.spinnerGender.setAdapter(genderAdapter);

        // Blood Group Spinner
        String[] bloodGroups = getResources().getStringArray(R.array.blood_groups);
        ArrayAdapter<String> bloodGroupAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                bloodGroups
        );
        binding.spinnerBloodGroup.setAdapter(bloodGroupAdapter);
    }

    private void setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Date picker
        binding.etDateOfBirth.setOnClickListener(v -> showDatePicker());

        // Cancel button
        binding.btnCancel.setOnClickListener(v -> finish());

        // Save button
        binding.btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                savePatient();
            }
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        // If editing and date exists, use it
        if (isEditMode && currentPatient != null && currentPatient.getDateOfBirth() != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                calendar.setTime(sdf.parse(currentPatient.getDateOfBirth()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    selectedDate = Calendar.getInstance();
                    selectedDate.set(year1, month1, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    String dateString = sdf.format(selectedDate.getTime());
                    binding.etDateOfBirth.setText(dateString);
                },
                year, month, day
        );

        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private boolean validateInputs() {
        String firstName = binding.etFirstName.getText().toString().trim();
        String lastName = binding.etLastName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();

        // Validate first name
        if (firstName.isEmpty()) {
            binding.etFirstName.setError(getString(R.string.required_field));
            binding.etFirstName.requestFocus();
            return false;
        }

        // Validate last name
        if (lastName.isEmpty()) {
            binding.etLastName.setError(getString(R.string.required_field));
            binding.etLastName.requestFocus();
            return false;
        }

        // Validate phone (optional but check format if provided)
        // Tunisian phone numbers are 8 digits (without +216 prefix)
        if (!phone.isEmpty() && phone.length() < 8) {
            binding.etPhone.setError(getString(R.string.invalid_phone));
            binding.etPhone.requestFocus();
            return false;
        }

        // Validate email (optional but check format if provided)
        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError(getString(R.string.invalid_email));
            binding.etEmail.requestFocus();
            return false;
        }

        return true;
    }

    private void savePatient() {
        // Get current date for lastVisit if new patient
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String currentDate = sdf.format(Calendar.getInstance().getTime());

        // Create or update patient object
        if (currentPatient == null) {
            currentPatient = new Patient();
        }

        currentPatient.setDoctorId(doctorId);
        currentPatient.setFirstName(binding.etFirstName.getText().toString().trim());
        currentPatient.setLastName(binding.etLastName.getText().toString().trim());
        currentPatient.setDateOfBirth(binding.etDateOfBirth.getText().toString().trim());
        currentPatient.setGender(binding.spinnerGender.getText().toString());
        
        // Prepend +216 prefix to phone number
        String phoneInput = binding.etPhone.getText().toString().trim();
        String fullPhone = phoneInput.isEmpty() ? "" : "+216 " + phoneInput;
        currentPatient.setPhone(fullPhone);
        
        currentPatient.setEmail(binding.etEmail.getText().toString().trim());
        currentPatient.setAddress(binding.etAddress.getText().toString().trim());
        currentPatient.setBloodGroup(binding.spinnerBloodGroup.getText().toString());
        currentPatient.setAllergies(binding.etAllergies.getText().toString().trim());

        // Auto-link: Check if a patient user account exists with this email
        String patientEmail = currentPatient.getEmail();
        if (patientEmail != null && !patientEmail.isEmpty()) {
            User existingUser = userDAO.getUserByEmail(patientEmail);
            if (existingUser != null && "patient".equals(existingUser.getRole())) {
                currentPatient.setUserId((int) existingUser.getId());
            }
        }

        if (isEditMode) {
            // Update existing patient
            int result = patientDAO.updatePatient(currentPatient);
            if (result > 0) {
                Toast.makeText(this, R.string.patient_updated, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Insert new patient
            currentPatient.setLastVisit(currentDate);
            long id = patientDAO.insertPatient(currentPatient);

            if (id > 0) {
                // Export database after adding new patient
                DatabaseExporter.exportDatabase(this, "New Patient Added: " + currentPatient.getFullName());
                
                Toast.makeText(this, R.string.patient_added, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadPatientData() {
        if (currentPatient == null) return;

        binding.etFirstName.setText(currentPatient.getFirstName());
        binding.etLastName.setText(currentPatient.getLastName());
        binding.etDateOfBirth.setText(currentPatient.getDateOfBirth());
        binding.spinnerGender.setText(currentPatient.getGender(), false);
        
        // Strip +216 prefix for display (it's shown as prefix in the input field)
        String phone = currentPatient.getPhone();
        if (phone != null && phone.startsWith("+216 ")) {
            phone = phone.substring(5); // Remove "+216 "
        }
        binding.etPhone.setText(phone);
        
        binding.etEmail.setText(currentPatient.getEmail());
        binding.etAddress.setText(currentPatient.getAddress());
        binding.spinnerBloodGroup.setText(currentPatient.getBloodGroup(), false);
        binding.etAllergies.setText(currentPatient.getAllergies());
    }
}
