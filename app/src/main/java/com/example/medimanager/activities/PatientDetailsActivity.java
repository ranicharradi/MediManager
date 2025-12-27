package com.example.medimanager.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medimanager.R;
import com.example.medimanager.adapters.AppointmentAdapter;
import com.example.medimanager.adapters.ConsultationAdapter;
import com.example.medimanager.database.AppointmentDAO;
import com.example.medimanager.database.ConsultationDAO;
import com.example.medimanager.database.PatientDAO;
import com.example.medimanager.databinding.ActivityPatientDetailsBinding;
import com.example.medimanager.models.Appointment;
import com.example.medimanager.models.Consultation;
import com.example.medimanager.models.Patient;
import com.example.medimanager.utils.AppointmentApprovalHelper;
import com.example.medimanager.utils.AppointmentStatusUtils;
import com.example.medimanager.utils.Constants;
import com.example.medimanager.utils.NotificationHelper;

import java.util.ArrayList;
import java.util.List;

public class PatientDetailsActivity extends AppCompatActivity {

    private ActivityPatientDetailsBinding binding;

    // Data
    private Patient patient;
    private PatientDAO patientDAO;
    private ConsultationDAO consultationDAO;
    private AppointmentDAO appointmentDAO;

    // Adapters
    private ConsultationAdapter consultationAdapter;
    private AppointmentAdapter appointmentAdapter;
    private List<Consultation> consultations;
    private List<Appointment> appointments;

    private int patientId;

    private final ActivityResultLauncher<Intent> formLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadPatientData();
                    loadConsultations();
                    loadAppointments();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatientDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get patient ID from intent
        patientId = getIntent().getIntExtra(Constants.EXTRA_PATIENT_ID, -1);
        if (patientId == -1) {
            Toast.makeText(this, R.string.patient_not_found, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize DAOs
        patientDAO = new PatientDAO(this);
        consultationDAO = new ConsultationDAO(this);
        appointmentDAO = new AppointmentDAO(this);

        // Initialize UI
        setupClickListeners();
        setupRecyclerViews();

        // Load data
        loadPatientData();
        loadConsultations();
        loadAppointments();
    }

    private void setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener(v -> finish());

        // Edit button
        binding.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(PatientDetailsActivity.this, AddPatientActivity.class);
            intent.putExtra(Constants.EXTRA_PATIENT_ID, patientId);
            intent.putExtra(Constants.EXTRA_IS_EDIT_MODE, true);
            formLauncher.launch(intent);
        });

        // Delete button
        binding.btnDelete.setOnClickListener(v -> showDeleteConfirmationDialog());

        // Book appointment
        binding.btnBookAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(PatientDetailsActivity.this, AddAppointmentActivity.class);
            intent.putExtra(Constants.EXTRA_PATIENT_ID, patientId);
            formLauncher.launch(intent);
        });

        // Add consultation
        binding.btnAddConsultation.setOnClickListener(v -> {
            Intent intent = new Intent(PatientDetailsActivity.this, AddConsultationActivity.class);
            intent.putExtra(Constants.EXTRA_PATIENT_ID, patientId);
            formLauncher.launch(intent);
        });
    }

    private void setupRecyclerViews() {
        // Consultations RecyclerView
        consultations = new ArrayList<>();
        consultationAdapter = new ConsultationAdapter(this, consultations);
        binding.rvConsultations.setLayoutManager(new LinearLayoutManager(this));
        binding.rvConsultations.setAdapter(consultationAdapter);
        binding.rvConsultations.setNestedScrollingEnabled(false);

        consultationAdapter.setOnItemClickListener(new ConsultationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Consultation consultation) {
                // Show consultation details in a dialog
                StringBuilder details = new StringBuilder();
                details.append(getString(R.string.consultation_date_label)).append(" ").append(consultation.getConsultationDate()).append("\n");
                details.append(getString(R.string.diagnosis_label)).append(" ").append(consultation.getDiagnosis()).append("\n");
                details.append(getString(R.string.treatment_label)).append(" ").append(consultation.getTreatment()).append("\n");
                details.append(getString(R.string.prescription_label)).append(" ").append(consultation.getPrescription()).append("\n");
                details.append(getString(R.string.notes_label)).append(" ").append(consultation.getNotes());

                new AlertDialog.Builder(PatientDetailsActivity.this)
                        .setTitle(R.string.consultation_details_title)
                        .setMessage(details.toString())
                        .setPositiveButton(R.string.ok, null)
                        .show();
            }

            @Override
            public void onEditClick(Consultation consultation) {
                // Edit consultation
                Intent intent = new Intent(PatientDetailsActivity.this, AddConsultationActivity.class);
                intent.putExtra(Constants.EXTRA_CONSULTATION_ID, consultation.getId());
                intent.putExtra(Constants.EXTRA_PATIENT_ID, patientId);
                intent.putExtra(Constants.EXTRA_IS_EDIT_MODE, true);
                formLauncher.launch(intent);
            }

            @Override
            public void onDeleteClick(Consultation consultation) {
                showDeleteConsultationDialog(consultation);
            }
        });

        // Appointments RecyclerView
        appointments = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(this, appointments);
        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAppointments.setAdapter(appointmentAdapter);
        binding.rvAppointments.setNestedScrollingEnabled(false);

        appointmentAdapter.setOnItemClickListener(new AppointmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Appointment appointment) {
                // If pending, show approval dialog
                if (appointment.isPending()) {
                    showApprovalDialog(appointment);
                } else {
                    // Show appointment info
                    String info = getString(R.string.appointment_info,
                            appointment.getReason(),
                            appointment.getAppointmentDate(),
                            appointment.getAppointmentTime(),
                            AppointmentStatusUtils.getStatusLabel(PatientDetailsActivity.this, appointment.getStatus()));
                    Toast.makeText(PatientDetailsActivity.this, info, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onStatusClick(Appointment appointment) {
                // If pending, show approval dialog instead of cycling status
                if (appointment.isPending()) {
                    showApprovalDialog(appointment);
                } else {
                    // Toggle status
                    String newStatus = appointment.isScheduled() ? Constants.STATUS_COMPLETED : Constants.STATUS_SCHEDULED;
                    appointmentDAO.updateAppointmentStatus(appointment.getId(), newStatus);
                    appointment.setStatus(newStatus);
                    appointmentAdapter.notifyDataSetChanged();
                    Toast.makeText(PatientDetailsActivity.this, R.string.status_updated, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onEditClick(Appointment appointment) {
                Intent intent = new Intent(PatientDetailsActivity.this, AddAppointmentActivity.class);
                intent.putExtra(Constants.EXTRA_APPOINTMENT_ID, appointment.getId());
                intent.putExtra(Constants.EXTRA_IS_EDIT_MODE, true);
                formLauncher.launch(intent);
            }

            @Override
            public void onDeleteClick(Appointment appointment) {
                showDeleteAppointmentDialog(appointment);
            }
        });
    }

    private void loadPatientData() {
        patient = patientDAO.getPatientById(patientId);

        if (patient != null) {
            // Set patient info
            binding.tvPatientName.setText(patient.getFullName());
            binding.tvAge.setText(getString(R.string.age_years_full, patient.getAge()));
            binding.tvGender.setText(patient.getGender() != null ? patient.getGender() : getString(R.string.info_not_available));
            String bloodGroup = patient.getBloodGroup();
            binding.tvBloodGroup.setText(getString(R.string.blood_prefix,
                    bloodGroup != null ? bloodGroup : getString(R.string.info_not_available)));
            binding.tvPhone.setText(patient.getPhone() != null ? patient.getPhone() : getString(R.string.no_phone));
            binding.tvEmail.setText(patient.getEmail() != null ? patient.getEmail() : getString(R.string.no_email));
            String lastVisit = patient.getLastVisit();
            binding.tvLastVisit.setText(getString(R.string.last_visit_prefix,
                    lastVisit != null ? lastVisit : getString(R.string.never)));
        } else {
            Toast.makeText(this, R.string.patient_not_found, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadConsultations() {
        List<Consultation> loadedConsultations = consultationDAO.getConsultationsByPatient(patientId);
        consultations.clear();
        consultations.addAll(loadedConsultations);
        consultationAdapter.notifyDataSetChanged();
    }

    private void loadAppointments() {
        List<Appointment> loadedAppointments = appointmentDAO.getAppointmentsByPatient(patientId);
        appointments.clear();
        appointments.addAll(loadedAppointments);
        appointmentAdapter.notifyDataSetChanged();
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirmation)
                .setMessage(R.string.delete_patient_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> deletePatient())
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deletePatient() {
        int result = patientDAO.deletePatient(patientId);

        if (result > 0) {
            Toast.makeText(this, R.string.patient_deleted, Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConsultationDialog(final Consultation consultation) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_confirmation)
                .setMessage(R.string.delete_consultation_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteConsultation(consultation))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showDeleteAppointmentDialog(final Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_appointment)
                .setMessage(R.string.delete_appointment_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteAppointment(appointment))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void deleteAppointment(Appointment appointment) {
        int result = appointmentDAO.deleteAppointment(appointment.getId());
        if (result > 0) {
            loadAppointments();
            Toast.makeText(this, R.string.appointment_deleted, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteConsultation(Consultation consultation) {
        int result = consultationDAO.deleteConsultation(consultation.getId());
        if (result > 0) {
            Toast.makeText(this, R.string.consultation_deleted, Toast.LENGTH_SHORT).show();
            loadConsultations();
        } else {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    private void showApprovalDialog(Appointment appointment) {
        AppointmentApprovalHelper.showApprovalDialog(this, appointment,
                new AppointmentApprovalHelper.ApprovalActions() {
                    @Override
                    public void onApprove(Appointment appt) {
                        approveAppointment(appt);
                    }

                    @Override
                    public void onModify(Appointment appt) {
                        Intent intent = new Intent(PatientDetailsActivity.this, AddAppointmentActivity.class);
                        intent.putExtra(Constants.EXTRA_APPOINTMENT_ID, appt.getId());
                        intent.putExtra(Constants.EXTRA_IS_EDIT_MODE, true);
                        formLauncher.launch(intent);
                    }

                    @Override
                    public void onReject(Appointment appt) {
                        showRejectConfirmationDialog(appt);
                    }
                });
    }

    private void approveAppointment(Appointment appointment) {
        int result = appointmentDAO.updateAppointmentStatus(appointment.getId(), Constants.STATUS_SCHEDULED);
        if (result > 0) {
            Toast.makeText(this, R.string.appointment_approved, Toast.LENGTH_SHORT).show();
            // Notify patient about approval
            NotificationHelper.notifyPatientAppointmentApproved(this,
                    appointment.getAppointmentDate(), appointment.getAppointmentTime());
            loadAppointments();
        } else {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    private void showRejectConfirmationDialog(Appointment appointment) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.reject_appointment)
                .setMessage(R.string.reject_appointment_message)
                .setPositiveButton(R.string.reject, (dialog, which) -> {
                    rejectAppointment(appointment);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void rejectAppointment(Appointment appointment) {
        int result = appointmentDAO.deleteAppointment(appointment.getId());
        if (result > 0) {
            Toast.makeText(this, R.string.appointment_rejected, Toast.LENGTH_SHORT).show();
            // Notify patient about rejection
            NotificationHelper.notifyPatientAppointmentRejected(this,
                    appointment.getAppointmentDate(), appointment.getAppointmentTime());
            loadAppointments();
        } else {
            Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }
}
