package com.example.medimanager.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medimanager.R;
import com.example.medimanager.adapters.AppointmentAdapter;
import com.example.medimanager.adapters.ConsultationAdapter;
import com.example.medimanager.database.AppointmentDAO;
import com.example.medimanager.database.ConsultationDAO;
import com.example.medimanager.database.PatientDAO;
import com.example.medimanager.databinding.FragmentPatientHomeBinding;
import com.example.medimanager.models.Appointment;
import com.example.medimanager.models.Consultation;
import com.example.medimanager.models.Patient;
import com.example.medimanager.utils.AppointmentStatusUtils;
import com.example.medimanager.utils.Constants;
import com.example.medimanager.utils.DateTimePickerHelper;
import com.example.medimanager.utils.NotificationHelper;
import com.example.medimanager.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientHomeFragment extends Fragment {

    private static boolean hasShownAppointmentAlert = false; // Session flag - reset on app restart
    
    private FragmentPatientHomeBinding binding;

    private AppointmentDAO appointmentDAO;
    private ConsultationDAO consultationDAO;
    private PatientDAO patientDAO;

    private AppointmentAdapter appointmentAdapter;
    private ConsultationAdapter consultationAdapter;
    private List<Appointment> upcomingAppointments;
    private List<Consultation> recentConsultations;

    private int patientId = -1;
    private int doctorId = -1;
    private String patientName = "";
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPatientHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize DAOs
        appointmentDAO = new AppointmentDAO(requireContext());
        consultationDAO = new ConsultationDAO(requireContext());
        patientDAO = new PatientDAO(requireContext());
        sessionManager = new SessionManager(requireContext());

        // Get logged-in user info
        loadUserInfo();

        // Initialize UI
        setupRecyclerViews();
        setupClickListeners();

        // Load data
        updateDate();
        loadPatientData();
        
        // Check for appointment status updates (show alert on login)
        checkAppointmentUpdates();
    }

    private void loadUserInfo() {
        int userId = sessionManager.getUserId();
        patientName = sessionManager.getUserName();
        if (patientName == null || patientName.isEmpty()) {
            patientName = getString(R.string.default_patient_name);
        }

        // Find the patient record linked to this user account via user_id
        if (userId != -1) {
            Patient patient = patientDAO.getPatientByUserId(userId);
            if (patient != null) {
                patientId = patient.getId();
                doctorId = patient.getDoctorId(); // Get the assigned doctor
            }
        }

        binding.tvWelcome.setText(getString(R.string.welcome_user, patientName));
    }

    private void setupRecyclerViews() {
        // Upcoming Appointments
        upcomingAppointments = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(requireContext(), upcomingAppointments);
        appointmentAdapter.setReadOnly(true); // Patients cannot edit appointments
        appointmentAdapter.setShowDoctorName(true); // Show doctor name instead of patient name
        binding.rvUpcomingAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUpcomingAppointments.setAdapter(appointmentAdapter);
        binding.rvUpcomingAppointments.setNestedScrollingEnabled(false);

        appointmentAdapter.setOnItemClickListener(new AppointmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Appointment appointment) {
                // Show appointment details in a toast (read-only)
                String info = getString(R.string.appointment_info,
                    appointment.getReason(),
                    appointment.getAppointmentDate(),
                    appointment.getAppointmentTime(),
                    AppointmentStatusUtils.getStatusLabel(requireContext(), appointment.getStatus()));
                Toast.makeText(requireContext(), info, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onStatusClick(Appointment appointment) {
                // Patients cannot change appointment status
                Toast.makeText(requireContext(), getString(R.string.contact_doctor_change_status), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditClick(Appointment appointment) {
                // Patients cannot edit appointments
            }

            @Override
            public void onDeleteClick(Appointment appointment) {
                // Patients cannot delete appointments
            }
        });

        // Recent Consultations
        recentConsultations = new ArrayList<>();
        consultationAdapter = new ConsultationAdapter(requireContext(), recentConsultations);
        binding.rvRecentConsultations.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentConsultations.setAdapter(consultationAdapter);
        binding.rvRecentConsultations.setNestedScrollingEnabled(false);

        consultationAdapter.setOnItemClickListener(new ConsultationAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Consultation consultation) {
                Toast.makeText(requireContext(), getString(R.string.toast_consultation_diagnosis, consultation.getDiagnosis()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditClick(Consultation consultation) {
                // Patients cannot edit consultations
            }

            @Override
            public void onDeleteClick(Consultation consultation) {
                // Patients cannot delete consultations
            }
        });
    }

    private void setupClickListeners() {
        binding.btnBookAppointment.setOnClickListener(v -> {
            if (patientId == -1) {
                Toast.makeText(requireContext(), getString(R.string.error_no_patient_record), Toast.LENGTH_SHORT).show();
                return;
            }
            if (doctorId == -1) {
                Toast.makeText(requireContext(), getString(R.string.error_no_doctor_assigned), Toast.LENGTH_SHORT).show();
                return;
            }
            showRequestAppointmentDialog();
        });
    }

    private void showRequestAppointmentDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_request_appointment, null);
        
        EditText etPreferredDate = dialogView.findViewById(R.id.etPreferredDate);
        EditText etPreferredTime = dialogView.findViewById(R.id.etPreferredTime);
        EditText etReason = dialogView.findViewById(R.id.etReason);
        EditText etNotes = dialogView.findViewById(R.id.etNotes);

        // Set default date to tomorrow
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
        etPreferredDate.setText(dateFormat.format(calendar.getTime()));

        // Set default time
        etPreferredTime.setText(getString(R.string.default_time_9am));

        // Date picker
        etPreferredDate.setOnClickListener(v -> DateTimePickerHelper.showDatePicker(
                requireContext(),
                calendar,
                true,
                false,
                (formattedDate, selectedCalendar) -> {
                    calendar.setTime(selectedCalendar.getTime());
                    etPreferredDate.setText(formattedDate);
                }
        ));

        // Time picker
        etPreferredTime.setOnClickListener(v -> DateTimePickerHelper.showTimePicker(
                requireContext(),
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                (hourOfDay, minute, formattedTime) -> etPreferredTime.setText(formattedTime)
        ));

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton(R.string.request_appointment, (dialog, which) -> {
                    String date = etPreferredDate.getText().toString().trim();
                    String time = etPreferredTime.getText().toString().trim();
                    String reason = etReason.getText().toString().trim();
                    String notes = etNotes.getText().toString().trim();

                    if (date.isEmpty() || time.isEmpty() || reason.isEmpty()) {
                        Toast.makeText(requireContext(), getString(R.string.error_fill_required_fields), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    submitAppointmentRequest(date, time, reason, notes);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void submitAppointmentRequest(String date, String time, String reason, String notes) {
        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorId(doctorId);
        appointment.setAppointmentDate(date);
        appointment.setAppointmentTime(time);
        appointment.setReason(reason);
        appointment.setNotes(notes);
        appointment.setStatus(Constants.STATUS_PENDING); // Pending approval

        long id = appointmentDAO.insertAppointment(appointment);
        if (id > 0) {
            Toast.makeText(requireContext(), R.string.appointment_request_sent, Toast.LENGTH_SHORT).show();
            // Notify doctor about new request
            NotificationHelper.notifyDoctorNewRequest(requireContext(), patientName, date, time, reason);
            loadPatientData(); // Refresh the list
        } else {
            Toast.makeText(requireContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPatientData() {
        if (patientId == -1) {
            // No linked patient record found
            binding.tvMyAppointments.setText(String.valueOf(0));
            binding.tvMyConsultations.setText(String.valueOf(0));
            binding.tvNoAppointments.setVisibility(View.VISIBLE);
            binding.tvNoConsultations.setVisibility(View.VISIBLE);
            return;
        }

        // Load appointments for this patient
        List<Appointment> appointments = appointmentDAO.getAppointmentsByPatient(patientId);
        upcomingAppointments.clear();

        // Filter to only show upcoming (scheduled or pending) appointments
        for (Appointment apt : appointments) {
            if (apt.isScheduled() || apt.isPending()) {
                upcomingAppointments.add(apt);
            }
        }
        appointmentAdapter.notifyDataSetChanged();

        binding.tvMyAppointments.setText(String.valueOf(appointments.size()));

        if (upcomingAppointments.isEmpty()) {
            binding.tvNoAppointments.setVisibility(View.VISIBLE);
            binding.rvUpcomingAppointments.setVisibility(View.GONE);
        } else {
            binding.tvNoAppointments.setVisibility(View.GONE);
            binding.rvUpcomingAppointments.setVisibility(View.VISIBLE);
        }

        // Load consultations for this patient
        List<Consultation> consultations = consultationDAO.getConsultationsByPatient(patientId);
        recentConsultations.clear();

        // Show last 5 consultations
        int limit = Math.min(consultations.size(), 5);
        for (int i = 0; i < limit; i++) {
            recentConsultations.add(consultations.get(i));
        }
        consultationAdapter.notifyDataSetChanged();

        binding.tvMyConsultations.setText(String.valueOf(consultations.size()));

        if (recentConsultations.isEmpty()) {
            binding.tvNoConsultations.setVisibility(View.VISIBLE);
            binding.rvRecentConsultations.setVisibility(View.GONE);
        } else {
            binding.tvNoConsultations.setVisibility(View.GONE);
            binding.rvRecentConsultations.setVisibility(View.VISIBLE);
        }
    }

    private void updateDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        binding.tvDate.setText(currentDate);
    }

    /**
     * Check for appointment status updates (approved/scheduled requests).
     * Shows an alert if the patient has upcoming scheduled appointments.
     */
    private void checkAppointmentUpdates() {
        // Only show once per session
        if (hasShownAppointmentAlert) return;
        
        // Respect the notification toggle setting
        if (!NotificationHelper.areNotificationsEnabled(requireContext())) {
            return;
        }
        
        if (patientId == -1) return;

        List<Appointment> appointments = appointmentDAO.getAppointmentsByPatient(patientId);
        
        int scheduledCount = 0;
        int pendingCount = 0;
        StringBuilder scheduledDetails = new StringBuilder();
        
        for (Appointment apt : appointments) {
            if (apt.isScheduled()) {
                scheduledCount++;
                if (scheduledCount <= 3) { // Show up to 3 appointments in detail
                    String detailLine = getString(R.string.appointment_summary_line,
                            apt.getAppointmentDate(),
                            apt.getAppointmentTime(),
                            apt.getReason());
                    scheduledDetails.append(detailLine).append("\n");
                }
            } else if (apt.isPending()) {
                pendingCount++;
            }
        }

        // Build message based on what we found
        StringBuilder message = new StringBuilder();
        
        if (scheduledCount > 0) {
            message.append(getString(R.string.pending_summary_scheduled,
                    scheduledCount,
                    scheduledDetails.toString()));
            if (scheduledCount > 3) {
                message.append(getString(R.string.pending_summary_more, scheduledCount - 3)).append("\n");
            }
        }
        
        if (pendingCount > 0) {
            if (message.length() > 0) {
                message.append("\n");
            }
            message.append(getString(R.string.pending_summary_pending, pendingCount));
        }

        // Only show dialog if there are appointments to mention
        if (scheduledCount > 0 || pendingCount > 0) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.appointments_summary_title)
                    .setMessage(message.toString())
                    .setPositiveButton(R.string.ok, null)
                    .show();
            
            hasShownAppointmentAlert = true; // Mark as shown for this session
        }
    }
    
    /**
     * Reset the session flag (call this on logout)
     */
    public static void resetSessionFlag() {
        hasShownAppointmentAlert = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPatientData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
