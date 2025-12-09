package com.example.medimanager.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medimanager.R;
import com.example.medimanager.database.ConsultationDAO;
import com.example.medimanager.database.PatientDAO;
import com.example.medimanager.databinding.ActivityAddConsultationBinding;
import com.example.medimanager.models.Consultation;
import com.example.medimanager.models.Patient;
import com.example.medimanager.utils.Constants;
import com.example.medimanager.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddConsultationActivity extends AppCompatActivity {

    private ActivityAddConsultationBinding binding;

    // Data
    private ConsultationDAO consultationDAO;
    private PatientDAO patientDAO;
    private Consultation currentConsultation;
    private int patientId;
    private int consultationId = -1;
    private boolean isEditMode = false;
    private Calendar selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddConsultationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get extras from intent
        patientId = getIntent().getIntExtra(Constants.EXTRA_PATIENT_ID, -1);
        consultationId = getIntent().getIntExtra(Constants.EXTRA_CONSULTATION_ID, -1);
        isEditMode = getIntent().getBooleanExtra(Constants.EXTRA_IS_EDIT_MODE, false);

        if (patientId == -1) {
            Toast.makeText(this, "Error: Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize DAOs
        consultationDAO = new ConsultationDAO(this);
        patientDAO = new PatientDAO(this);

        // Initialize UI
        setupClickListeners();
        loadPatientName();

        // Load consultation data if editing
        if (isEditMode && consultationId != -1) {
            loadConsultationData();
        } else {
            // Set current date by default
            binding.etConsultationDate.setText(DateUtils.getCurrentDate());
        }
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveConsultation();
            }
        });

        binding.etConsultationDate.setOnClickListener(v -> showDatePicker());
    }

    private void loadPatientName() {
        Patient patient = patientDAO.getPatientById(patientId);
        if (patient != null) {
            binding.tvPatientName.setText("Patient: " + patient.getFullName());
        } else {
            Toast.makeText(this, R.string.patient_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadConsultationData() {
        currentConsultation = consultationDAO.getConsultationById(consultationId);

        if (currentConsultation != null) {
            binding.etConsultationDate.setText(currentConsultation.getConsultationDate());
            binding.etDiagnosis.setText(currentConsultation.getDiagnosis());
            binding.etTreatment.setText(currentConsultation.getTreatment());
            binding.etPrescription.setText(currentConsultation.getPrescription());
            binding.etNotes.setText(currentConsultation.getNotes());
        }
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        // If editing and date exists, use it
        if (isEditMode && currentConsultation != null && currentConsultation.getConsultationDate() != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
                calendar.setTime(sdf.parse(currentConsultation.getConsultationDate()));
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

                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
                    String dateString = sdf.format(selectedDate.getTime());
                    binding.etConsultationDate.setText(dateString);
                },
                year, month, day
        );

        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private boolean validateInputs() {
        String consultationDate = binding.etConsultationDate.getText().toString().trim();
        String diagnosis = binding.etDiagnosis.getText().toString().trim();

        // Validate consultation date
        if (consultationDate.isEmpty()) {
            binding.etConsultationDate.setError(getString(R.string.required_field));
            binding.etConsultationDate.requestFocus();
            return false;
        }

        // Validate diagnosis
        if (diagnosis.isEmpty()) {
            binding.etDiagnosis.setError(getString(R.string.required_field));
            binding.etDiagnosis.requestFocus();
            return false;
        }

        return true;
    }

    private void saveConsultation() {
        // Create or update consultation object
        if (currentConsultation == null) {
            currentConsultation = new Consultation();
        }

        currentConsultation.setPatientId(patientId);
        currentConsultation.setConsultationDate(binding.etConsultationDate.getText().toString().trim());
        currentConsultation.setDiagnosis(binding.etDiagnosis.getText().toString().trim());
        currentConsultation.setTreatment(binding.etTreatment.getText().toString().trim());
        currentConsultation.setPrescription(binding.etPrescription.getText().toString().trim());
        currentConsultation.setNotes(binding.etNotes.getText().toString().trim());

        if (isEditMode && consultationId != -1) {
            // Update existing consultation
            currentConsultation.setId(consultationId);
            int result = consultationDAO.updateConsultation(currentConsultation);

            if (result > 0) {
                Toast.makeText(this, R.string.consultation_updated, Toast.LENGTH_SHORT).show();

                // Update patient's last visit date
                updatePatientLastVisit(currentConsultation.getConsultationDate());

                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Insert new consultation
            long id = consultationDAO.insertConsultation(currentConsultation);

            if (id > 0) {
                Toast.makeText(this, R.string.consultation_added, Toast.LENGTH_SHORT).show();

                // Update patient's last visit date
                updatePatientLastVisit(currentConsultation.getConsultationDate());

                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updatePatientLastVisit(String visitDate) {
        patientDAO.updateLastVisit(patientId, visitDate);
    }
}
