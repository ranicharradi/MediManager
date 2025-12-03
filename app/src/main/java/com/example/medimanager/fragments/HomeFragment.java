package com.example.medimanager.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medimanager.R;
import com.example.medimanager.activities.AddAppointmentActivity;
import com.example.medimanager.activities.AddPatientActivity;
import com.example.medimanager.activities.MainActivity;
import com.example.medimanager.activities.PatientDetailsActivity;
import com.example.medimanager.adapters.AppointmentAdapter;
import com.example.medimanager.adapters.PatientAdapter;
import com.example.medimanager.database.AppointmentDAO;
import com.example.medimanager.database.ConsultationDAO;
import com.example.medimanager.database.PatientDAO;
import com.example.medimanager.databinding.FragmentHomeBinding;
import com.example.medimanager.models.Appointment;
import com.example.medimanager.models.Patient;
import com.example.medimanager.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    // Database
    private PatientDAO patientDAO;
    private AppointmentDAO appointmentDAO;
    private ConsultationDAO consultationDAO;

    // Adapters
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> todayAppointments;
    private PatientAdapter recentPatientsAdapter;
    private List<Patient> recentPatients;
    private int doctorId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize DAOs
        patientDAO = new PatientDAO(requireContext());
        appointmentDAO = new AppointmentDAO(requireContext());
        consultationDAO = new ConsultationDAO(requireContext());
        // Load current doctor id
        SharedPreferences prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        doctorId = prefs.getInt(Constants.PREF_USER_ID, -1);

        // Initialize UI
        setupRecyclerView();
        setupClickListeners();

        // Load data
        loadStatistics();
        loadTodayAppointments();
        loadRecentPatients();
        updateDate();
    }

    private void setupRecyclerView() {
        // Today's Appointments
        todayAppointments = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(requireContext(), todayAppointments);

        binding.rvTodayAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTodayAppointments.setAdapter(appointmentAdapter);
        binding.rvTodayAppointments.setNestedScrollingEnabled(false);

        // Set item click listener
        appointmentAdapter.setOnItemClickListener(new AppointmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Appointment appointment) {
                // Navigate to appointment details or patient details
                Intent intent = new Intent(requireContext(), PatientDetailsActivity.class);
                intent.putExtra("PATIENT_ID", appointment.getPatientId());
                startActivity(intent);
            }

            @Override
            public void onStatusClick(Appointment appointment) {
                // Update appointment status
                updateAppointmentStatus(appointment);
            }
        });

        // Recent Patients
        recentPatients = new ArrayList<>();
        recentPatientsAdapter = new PatientAdapter(requireContext(), recentPatients);

        binding.rvRecentPatients.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecentPatients.setAdapter(recentPatientsAdapter);
        binding.rvRecentPatients.setNestedScrollingEnabled(false);

        recentPatientsAdapter.setOnItemClickListener(new PatientAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Patient patient) {
                Intent intent = new Intent(requireContext(), PatientDetailsActivity.class);
                intent.putExtra("PATIENT_ID", patient.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Patient patient) {
                Intent intent = new Intent(requireContext(), AddPatientActivity.class);
                intent.putExtra("PATIENT_ID", patient.getId());
                intent.putExtra("IS_EDIT_MODE", true);
                startActivity(intent);
            }

            @Override
            public void onDeleteClick(Patient patient) {
                // For dashboard, maybe just show a toast or navigate to details
                Toast.makeText(requireContext(), "Go to Patients tab to delete", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        // View All button
        binding.tvViewAll.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToAppointments();
            }
        });

        // New Appointment button
        binding.btnNewAppointment.setOnClickListener(v -> navigateToAddAppointment());

        // Add Patient button
        binding.btnAddPatient.setOnClickListener(v -> navigateToAddPatient());
    }

    private void loadStatistics() {
        // Get today's date
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Load statistics
        // Load statistics (guard if doctorId missing)
        int totalPatients = doctorId == -1 ? 0 : patientDAO.getTotalPatientsCount(doctorId);
        int monthlyConsultations = consultationDAO.getMonthlyConsultationsCount();
        int upcomingAppointments = doctorId == -1 ? 0 : appointmentDAO.getUpcomingAppointmentsCount(doctorId);
        int todayAppointmentsCount = doctorId == -1 ? 0 : appointmentDAO.getTodayAppointmentsCount(doctorId, today);

        // Update UI
        binding.tvTotalPatients.setText(String.valueOf(totalPatients));
        binding.tvMonthlyConsultations.setText(String.valueOf(monthlyConsultations));
        binding.tvUpcomingAppointments.setText(String.valueOf(upcomingAppointments));
        binding.tvTodayAppointmentsCount.setText(String.valueOf(todayAppointmentsCount));
    }

    private void loadTodayAppointments() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (doctorId == -1) {
            todayAppointments.clear();
            appointmentAdapter.notifyDataSetChanged();
            return;
        }

        List<Appointment> appointments = appointmentDAO.getTodayAppointments(doctorId, today);

        todayAppointments.clear();
        todayAppointments.addAll(appointments);
        appointmentAdapter.notifyDataSetChanged();
    }

    private void loadRecentPatients() {
        if (doctorId == -1) {
            recentPatients.clear();
            recentPatientsAdapter.notifyDataSetChanged();
            return;
        }

        List<Patient> patients = patientDAO.getRecentPatients(doctorId, 5); // Get last 5 patients
        recentPatients.clear();
        recentPatients.addAll(patients);
        recentPatientsAdapter.notifyDataSetChanged();
    }

    private void updateAppointmentStatus(Appointment appointment) {
        String newStatus;
        if (appointment.isScheduled()) {
            newStatus = "in_progress";
        } else if (appointment.isInProgress()) {
            newStatus = "completed";
        } else {
            newStatus = "scheduled";
        }

        int result = appointmentDAO.updateAppointmentStatus(appointment.getId(), newStatus);

        if (result > 0) {
            appointment.setStatus(newStatus);
            appointmentAdapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "Status updated", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        binding.tvDate.setText(currentDate);
    }

    private void navigateToAddPatient() {
        Intent intent = new Intent(requireContext(), AddPatientActivity.class);
        startActivity(intent);
    }

    private void navigateToAddAppointment() {
        Intent intent = new Intent(requireContext(), AddAppointmentActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        loadStatistics();
        loadTodayAppointments();
        loadRecentPatients();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
