package com.example.medimanager.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.medimanager.R;
import com.example.medimanager.activities.AddAppointmentActivity;
import com.example.medimanager.activities.PatientDetailsActivity;
import com.example.medimanager.adapters.AppointmentAdapter;
import com.example.medimanager.database.AppointmentDAO;
import com.example.medimanager.database.PatientDAO;
import com.example.medimanager.databinding.FragmentAppointmentsBinding;
import com.example.medimanager.models.Appointment;
import com.example.medimanager.models.Patient;
import com.example.medimanager.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsFragment extends Fragment {

    private FragmentAppointmentsBinding binding;

    // Data
    private AppointmentDAO appointmentDAO;
    private PatientDAO patientDAO;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentList;
    private List<Appointment> filteredList;
    private String currentFilter = "all";
    private boolean isDoctor = true;
    private int doctorId = -1;
    private int patientId = -1;

    private final ActivityResultLauncher<Intent> addAppointmentLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK) {
                    loadAppointments();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAppointmentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize DAO
        appointmentDAO = new AppointmentDAO(requireContext());
        patientDAO = new PatientDAO(requireContext());

        // Load current user info
        SharedPreferences prefs = requireContext().getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        isDoctor = prefs.getBoolean(Constants.PREF_IS_DOCTOR, true);
        doctorId = prefs.getInt(Constants.PREF_USER_ID, -1);
        if (!isDoctor) {
            String email = prefs.getString(Constants.PREF_USER_EMAIL, "");
            Patient patient = patientDAO.getPatientByEmail(email);
            if (patient != null) {
                patientId = patient.getId();
            }
        }

        // Initialize UI
        setupRecyclerView();
        setupFilterChips();
        setupClickListeners();

        // Load data
        loadAppointments();
    }

    private void setupRecyclerView() {
        appointmentList = new ArrayList<>();
        filteredList = new ArrayList<>();

        appointmentAdapter = new AppointmentAdapter(requireContext(), filteredList);
        binding.rvAppointments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvAppointments.setAdapter(appointmentAdapter);

        // Set click listeners
        appointmentAdapter.setOnItemClickListener(new AppointmentAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Appointment appointment) {
                // Open patient details
                Intent intent = new Intent(requireContext(), PatientDetailsActivity.class);
                intent.putExtra(Constants.EXTRA_PATIENT_ID, appointment.getPatientId());
                startActivity(intent);
            }

            @Override
            public void onStatusClick(Appointment appointment) {
                if (!isDoctor) {
                    Toast.makeText(requireContext(), "Only doctors can update status", Toast.LENGTH_SHORT).show();
                    return;
                }
                updateAppointmentStatus(appointment);
            }
        });
    }

    private void setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                binding.chipAll.setChecked(true);
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                currentFilter = "all";
            } else if (checkedId == R.id.chipScheduled) {
                currentFilter = Constants.STATUS_SCHEDULED;
            } else if (checkedId == R.id.chipInProgress) {
                currentFilter = Constants.STATUS_IN_PROGRESS;
            } else if (checkedId == R.id.chipCompleted) {
                currentFilter = Constants.STATUS_COMPLETED;
            }

            filterAppointments();
        });
    }

    private void setupClickListeners() {
        // Note: The FAB is in the main activity, so we'll handle the click there
    }

    private void loadAppointments() {
        if (isDoctor) {
            if (doctorId == -1) {
                appointmentList = new ArrayList<>();
            } else {
                appointmentList = appointmentDAO.getAllAppointments(doctorId);
            }
        } else {
            if (patientId == -1) {
                appointmentList = new ArrayList<>();
            } else {
                appointmentList = appointmentDAO.getAppointmentsByPatient(patientId);
            }
        }
        filterAppointments();
    }

    private void filterAppointments() {
        filteredList.clear();

        if (currentFilter.equals("all")) {
            filteredList.addAll(appointmentList);
        } else {
            for (Appointment appointment : appointmentList) {
                if (currentFilter.equals(appointment.getStatus())) {
                    filteredList.add(appointment);
                }
            }
        }

        appointmentAdapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        if (filteredList.isEmpty()) {
            binding.rvAppointments.setVisibility(View.GONE);
            binding.tvEmptyState.setVisibility(View.VISIBLE);
            binding.tvEmptyState.setText("No appointments found");
        } else {
            binding.rvAppointments.setVisibility(View.VISIBLE);
            binding.tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void updateAppointmentStatus(Appointment appointment) {
        if (!isDoctor) {
            return;
        }

        String newStatus;
        if (appointment.isScheduled()) {
            newStatus = Constants.STATUS_IN_PROGRESS;
        } else if (appointment.isInProgress()) {
            newStatus = Constants.STATUS_COMPLETED;
        } else {
            newStatus = Constants.STATUS_SCHEDULED;
        }

        int result = appointmentDAO.updateAppointmentStatus(appointment.getId(), newStatus);

        if (result > 0) {
            appointment.setStatus(newStatus);
            appointmentAdapter.notifyDataSetChanged();
            Toast.makeText(requireContext(), "Status updated", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAppointments();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
