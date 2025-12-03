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
import androidx.appcompat.app.AlertDialog;
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
import com.example.medimanager.utils.NotificationHelper;

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
        doctorId = (int) prefs.getLong(Constants.PREF_USER_ID, -1);
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
                if (isDoctor) {
                    // If pending, show approval dialog
                    if (appointment.isPending()) {
                        showApprovalDialog(appointment);
                    } else {
                        // Doctors can open patient details
                        Intent intent = new Intent(requireContext(), PatientDetailsActivity.class);
                        intent.putExtra(Constants.EXTRA_PATIENT_ID, appointment.getPatientId());
                        startActivity(intent);
                    }
                } else {
                    // Patients can only view appointment info
                    String statusText = appointment.isPending() ? "Pending doctor approval" : appointment.getStatusDisplayName();
                    String info = "Appointment: " + appointment.getReason() + "\n" +
                            "Date: " + appointment.getAppointmentDate() + "\n" +
                            "Time: " + appointment.getAppointmentTime() + "\n" +
                            "Status: " + statusText;
                    Toast.makeText(requireContext(), info, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onStatusClick(Appointment appointment) {
                if (!isDoctor) {
                    Toast.makeText(requireContext(), "Only doctors can update status", Toast.LENGTH_SHORT).show();
                    return;
                }
                // If pending, show approval dialog instead of cycling status
                if (appointment.isPending()) {
                    showApprovalDialog(appointment);
                } else {
                    updateAppointmentStatus(appointment);
                }
            }

            @Override
            public void onEditClick(Appointment appointment) {
                if (!isDoctor) {
                    return;
                }
                // Open AddAppointmentActivity in edit mode
                Intent intent = new Intent(requireContext(), AddAppointmentActivity.class);
                intent.putExtra(Constants.EXTRA_APPOINTMENT_ID, appointment.getId());
                intent.putExtra(Constants.EXTRA_IS_EDIT_MODE, true);
                addAppointmentLauncher.launch(intent);
            }

            @Override
            public void onDeleteClick(Appointment appointment) {
                if (!isDoctor) {
                    return;
                }
                showDeleteConfirmationDialog(appointment);
            }
        });

        // Set read-only mode for patients (hides status click indicator)
        appointmentAdapter.setReadOnly(!isDoctor);
        // Show doctor name for patients, patient name for doctors
        appointmentAdapter.setShowDoctorName(!isDoctor);
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
            } else if (checkedId == R.id.chipPending) {
                currentFilter = Constants.STATUS_PENDING;
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

        // Cycle status: Scheduled -> Completed -> Scheduled
        String newStatus;
        if (appointment.isScheduled()) {
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

    private void showDeleteConfirmationDialog(Appointment appointment) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_appointment)
                .setMessage(R.string.delete_appointment_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    deleteAppointment(appointment);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showApprovalDialog(Appointment appointment) {
        String[] options = {
                getString(R.string.approve_appointment),
                getString(R.string.modify_and_approve),
                getString(R.string.reject_appointment)
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.pending_appointment_request)
                .setMessage(getString(R.string.appointment_request_details,
                        appointment.getPatientName(),
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime(),
                        appointment.getReason()))
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Approve
                            approveAppointment(appointment);
                            break;
                        case 1: // Modify and approve
                            Intent intent = new Intent(requireContext(), AddAppointmentActivity.class);
                            intent.putExtra(Constants.EXTRA_APPOINTMENT_ID, appointment.getId());
                            intent.putExtra(Constants.EXTRA_IS_EDIT_MODE, true);
                            addAppointmentLauncher.launch(intent);
                            break;
                        case 2: // Reject
                            showRejectConfirmationDialog(appointment);
                            break;
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void approveAppointment(Appointment appointment) {
        int result = appointmentDAO.updateAppointmentStatus(appointment.getId(), Constants.STATUS_SCHEDULED);
        if (result > 0) {
            Toast.makeText(requireContext(), R.string.appointment_approved, Toast.LENGTH_SHORT).show();
            // Notify patient about approval
            NotificationHelper.notifyPatientAppointmentApproved(requireContext(), 
                    appointment.getAppointmentDate(), appointment.getAppointmentTime());
            loadAppointments();
        } else {
            Toast.makeText(requireContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    private void showRejectConfirmationDialog(Appointment appointment) {
        new AlertDialog.Builder(requireContext())
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
            Toast.makeText(requireContext(), R.string.appointment_rejected, Toast.LENGTH_SHORT).show();
            // Notify patient about rejection
            NotificationHelper.notifyPatientAppointmentRejected(requireContext(),
                    appointment.getAppointmentDate(), appointment.getAppointmentTime());
            loadAppointments();
        } else {
            Toast.makeText(requireContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAppointment(Appointment appointment) {
        int result = appointmentDAO.deleteAppointment(appointment.getId());
        if (result > 0) {
            loadAppointments();
            Toast.makeText(requireContext(), "Appointment deleted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
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
