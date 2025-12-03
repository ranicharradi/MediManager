package com.example.medimanager.fragments;

import android.content.Context;
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

import com.example.medimanager.adapters.AppointmentAdapter;
import com.example.medimanager.adapters.ConsultationAdapter;
import com.example.medimanager.database.AppointmentDAO;
import com.example.medimanager.database.ConsultationDAO;
import com.example.medimanager.database.PatientDAO;
import com.example.medimanager.databinding.FragmentPatientHomeBinding;
import com.example.medimanager.models.Appointment;
import com.example.medimanager.models.Consultation;
import com.example.medimanager.models.Patient;
import com.example.medimanager.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PatientHomeFragment extends Fragment {

    private FragmentPatientHomeBinding binding;

    private AppointmentDAO appointmentDAO;
    private ConsultationDAO consultationDAO;
    private PatientDAO patientDAO;

    private AppointmentAdapter appointmentAdapter;
    private ConsultationAdapter consultationAdapter;
    private List<Appointment> upcomingAppointments;
    private List<Consultation> recentConsultations;

    private long patientId = -1;
    private String patientName = "";

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

        // Get logged-in user info
        loadUserInfo();

        // Initialize UI
        setupRecyclerViews();
        setupClickListeners();

        // Load data
        updateDate();
        loadPatientData();
    }

    private void loadUserInfo() {
        SharedPreferences prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        String userEmail = prefs.getString(Constants.PREF_USER_EMAIL, "");
        patientName = prefs.getString(Constants.PREF_USER_NAME, "Patient");

        // Find the patient record linked to this user email
        Patient patient = patientDAO.getPatientByEmail(userEmail);
        if (patient != null) {
            patientId = patient.getId();
        }

        binding.tvWelcome.setText("Welcome, " + patientName);
    }

    private void setupRecyclerViews() {
        // Upcoming Appointments
        upcomingAppointments = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(requireContext(), upcomingAppointments);
        binding.rvUpcomingAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvUpcomingAppointments.setAdapter(appointmentAdapter);
        binding.rvUpcomingAppointments.setNestedScrollingEnabled(false);

        appointmentAdapter.setOnItemClickListener(new AppointmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Appointment appointment) {
                Toast.makeText(requireContext(), "Appointment: " + appointment.getReason(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusClick(Appointment appointment) {
                // Patients cannot change appointment status
                Toast.makeText(requireContext(), "Contact your doctor to change status", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(requireContext(), "Consultation: " + consultation.getDiagnosis(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), "Please contact the clinic to book an appointment", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadPatientData() {
        if (patientId == -1) {
            // No linked patient record found
            binding.tvMyAppointments.setText("0");
            binding.tvMyConsultations.setText("0");
            binding.tvNoAppointments.setVisibility(View.VISIBLE);
            binding.tvNoConsultations.setVisibility(View.VISIBLE);
            return;
        }

        // Load appointments for this patient
        List<Appointment> appointments = appointmentDAO.getAppointmentsByPatient((int) patientId);
        upcomingAppointments.clear();

        // Filter to only show upcoming (scheduled) appointments
        for (Appointment apt : appointments) {
            if (apt.isScheduled() || apt.isInProgress()) {
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
        List<Consultation> consultations = consultationDAO.getConsultationsByPatient((int) patientId);
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
