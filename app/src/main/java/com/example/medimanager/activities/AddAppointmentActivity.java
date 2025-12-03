package com.example.medimanager.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.medimanager.AppointmentNotificationReceiver;
import com.example.medimanager.R;
import com.example.medimanager.database.AppointmentDAO;
import com.example.medimanager.database.PatientDAO;
import com.example.medimanager.database.UserDAO;
import com.example.medimanager.databinding.ActivityAddAppointmentBinding;
import com.example.medimanager.models.Appointment;
import com.example.medimanager.models.Patient;
import com.example.medimanager.models.User;
import com.example.medimanager.utils.Constants;
import com.example.medimanager.utils.DateUtils;
import com.example.medimanager.utils.NotificationHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddAppointmentActivity extends AppCompatActivity {

    private ActivityAddAppointmentBinding binding;

    // Data
    private AppointmentDAO appointmentDAO;
    private PatientDAO patientDAO;
    private Appointment currentAppointment;
    private List<Patient> patientList;
    private int selectedPatientId = -1;
    private int appointmentId = -1;
    private boolean isEditMode = false;
    private Calendar selectedDate;
    private int selectedHour = 9;
    private int selectedMinute = 0;
    private int doctorId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddAppointmentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get extras from intent
        selectedPatientId = getIntent().getIntExtra(Constants.EXTRA_PATIENT_ID, -1);
        appointmentId = getIntent().getIntExtra(Constants.EXTRA_APPOINTMENT_ID, -1);
        isEditMode = getIntent().getBooleanExtra(Constants.EXTRA_IS_EDIT_MODE, false);

        // Initialize DAOs
        appointmentDAO = new AppointmentDAO(this);
        patientDAO = new PatientDAO(this);

        // Load doctor id
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, MODE_PRIVATE);
        boolean isDoctor = prefs.getBoolean(Constants.PREF_IS_DOCTOR, true);
        doctorId = (int) prefs.getLong(Constants.PREF_USER_ID, -1);
        if (!isDoctor || doctorId == -1) {
            Toast.makeText(this, "Only doctors can manage appointments", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize UI
        setupSpinners();
        setupClickListeners();

        // Load appointment data if editing
        if (isEditMode && appointmentId != -1) {
            loadAppointmentData();
        } else {
            // Set default values
            binding.etAppointmentDate.setText(DateUtils.getCurrentDate());
            binding.etAppointmentTime.setText("09:00 AM");
            binding.spinnerStatus.setText(Constants.STATUS_SCHEDULED, false);
        }
    }

    private void setupSpinners() {
        // Load patients
        loadPatients();

        // Status Spinner (In Progress removed - status cycles from Scheduled to Completed)
        String[] statuses = {
                "Scheduled",
                "Completed",
                "Cancelled"
        };
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statuses
        );
        binding.spinnerStatus.setAdapter(statusAdapter);
    }

    private void loadPatients() {
        patientList = patientDAO.getAllPatients(doctorId);

        List<String> patientNames = new ArrayList<>();
        for (Patient patient : patientList) {
            patientNames.add(patient.getFullName());
        }

        ArrayAdapter<String> patientAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                patientNames
        );
        binding.spinnerPatient.setAdapter(patientAdapter);

        // If patient ID was passed, select it
        if (selectedPatientId != -1) {
            for (int i = 0; i < patientList.size(); i++) {
                if (patientList.get(i).getId() == selectedPatientId) {
                    binding.spinnerPatient.setText(patientList.get(i).getFullName(), false);
                    break;
                }
            }
        }
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveAppointment();
            }
        });

        binding.etAppointmentDate.setOnClickListener(v -> showDatePicker());
        binding.etAppointmentTime.setOnClickListener(v -> showTimePicker());

        binding.spinnerPatient.setOnItemClickListener((parent, view, position, id) -> {
            selectedPatientId = patientList.get(position).getId();
        });
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

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
                    binding.etAppointmentDate.setText(dateString);
                },
                year, month, day
        );

        // Set min date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;

                    // Format time as 12-hour with AM/PM
                    String amPm = hourOfDay >= 12 ? "PM" : "AM";
                    int displayHour = hourOfDay % 12;
                    if (displayHour == 0) displayHour = 12;

                    String timeString = String.format(Locale.getDefault(),
                            "%02d:%02d %s", displayHour, minute, amPm);
                    binding.etAppointmentTime.setText(timeString);
                },
                selectedHour, selectedMinute, false
        );

        timePickerDialog.show();
    }

    private void loadAppointmentData() {
        currentAppointment = appointmentDAO.getAppointmentById(appointmentId);

        if (currentAppointment != null) {
            // Set patient
            selectedPatientId = currentAppointment.getPatientId();
            loadPatients();

            // Set other fields
            binding.etAppointmentDate.setText(currentAppointment.getAppointmentDate());
            binding.etAppointmentTime.setText(currentAppointment.getAppointmentTime());
            binding.etReason.setText(currentAppointment.getReason());
            binding.etNotes.setText(currentAppointment.getNotes());

            // Set status
            String status = currentAppointment.getStatusDisplayName();
            binding.spinnerStatus.setText(status, false);
        }
    }

    private boolean validateInputs() {
        String selectedPatient = binding.spinnerPatient.getText().toString().trim();
        String appointmentDate = binding.etAppointmentDate.getText().toString().trim();
        String appointmentTime = binding.etAppointmentTime.getText().toString().trim();
        String reason = binding.etReason.getText().toString().trim();

        // Validate patient selection
        if (selectedPatient.isEmpty() || selectedPatientId == -1) {
            binding.spinnerPatient.setError("Please select a patient");
            binding.spinnerPatient.requestFocus();
            return false;
        }

        // Validate date
        if (appointmentDate.isEmpty()) {
            binding.etAppointmentDate.setError(getString(R.string.required_field));
            binding.etAppointmentDate.requestFocus();
            return false;
        }

        // Validate time
        if (appointmentTime.isEmpty()) {
            binding.etAppointmentTime.setError(getString(R.string.required_field));
            binding.etAppointmentTime.requestFocus();
            return false;
        }

        // Validate reason
        if (reason.isEmpty()) {
            binding.etReason.setError(getString(R.string.required_field));
            binding.etReason.requestFocus();
            return false;
        }

        return true;
    }

    private void saveAppointment() {
        // Create or update appointment object
        if (currentAppointment == null) {
            currentAppointment = new Appointment();
        }

        currentAppointment.setPatientId(selectedPatientId);
        currentAppointment.setDoctorId(doctorId);
        currentAppointment.setAppointmentDate(binding.etAppointmentDate.getText().toString().trim());
        currentAppointment.setAppointmentTime(binding.etAppointmentTime.getText().toString().trim());
        currentAppointment.setReason(binding.etReason.getText().toString().trim());
        currentAppointment.setNotes(binding.etNotes.getText().toString().trim());

        // Convert status display name to database value
        String statusDisplay = binding.spinnerStatus.getText().toString();
        String status = Constants.STATUS_SCHEDULED;
        if (statusDisplay.equals("Completed")) {
            status = Constants.STATUS_COMPLETED;
        } else if (statusDisplay.equals("Cancelled")) {
            status = Constants.STATUS_CANCELLED;
        }
        currentAppointment.setStatus(status);

        if (isEditMode && appointmentId != -1) {
            // Update existing appointment
            currentAppointment.setId(appointmentId);
            if (currentAppointment.getDoctorId() == 0) {
                currentAppointment.setDoctorId(doctorId);
            }
            int result = appointmentDAO.updateAppointment(currentAppointment);

            if (result > 0) {
                Toast.makeText(this, "Appointment updated successfully", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        } else {
            // Insert new appointment
            long id = appointmentDAO.insertAppointment(currentAppointment);

            if (id > 0) {
                Toast.makeText(this, R.string.appointment_added, Toast.LENGTH_SHORT).show();

                // Schedule notification (1 hour before)
                scheduleNotification(currentAppointment);

                // Notify patient about the new appointment
                UserDAO userDAO = new UserDAO(this);
                User doctor = userDAO.getUserById(doctorId);
                String doctorName = doctor != null ? doctor.getLastName() : "Your doctor";
                NotificationHelper.notifyPatientNewAppointment(
                        this,
                        doctorName,
                        currentAppointment.getAppointmentDate(),
                        currentAppointment.getAppointmentTime(),
                        currentAppointment.getReason()
                );

                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void scheduleNotification(Appointment appointment) {
        try {
            // Parse date and time
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            String dateTimeString = appointment.getAppointmentDate() + " " + appointment.getAppointmentTime();
            Date appointmentDateTime = sdf.parse(dateTimeString);

            if (appointmentDateTime != null) {
                // Schedule notification 1 hour before
                long notificationTime = appointmentDateTime.getTime() - (60 * 60 * 1000);

                if (notificationTime > System.currentTimeMillis()) {
                    // Create notification intent
                    Intent intent = new Intent(this, AppointmentNotificationReceiver.class);
                    intent.putExtra("appointment_id", appointment.getId());
                    intent.putExtra("patient_name", appointment.getPatientName());
                    intent.putExtra("appointment_time", appointment.getAppointmentTime());

                    PendingIntent pendingIntent = PendingIntent.getBroadcast(
                            this,
                            (int) appointment.getId(),
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
                    );

                    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null) {
                        alarmManager.setExactAndAllowWhileIdle(
                                AlarmManager.RTC_WAKEUP,
                                notificationTime,
                                pendingIntent
                        );

                        Toast.makeText(this, "Reminder set for 1 hour before appointment", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
