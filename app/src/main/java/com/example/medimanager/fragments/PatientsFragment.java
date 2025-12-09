package com.example.medimanager.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.example.medimanager.activities.AddPatientActivity;
import com.example.medimanager.activities.PatientDetailsActivity;
import com.example.medimanager.adapters.PatientAdapter;
import com.example.medimanager.database.PatientDAO;
import com.example.medimanager.databinding.FragmentPatientsBinding;
import com.example.medimanager.models.Patient;
import com.example.medimanager.utils.Constants;
import com.example.medimanager.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class PatientsFragment extends Fragment {

    private FragmentPatientsBinding binding;

    // Data
    private PatientDAO patientDAO;
    private PatientAdapter patientAdapter;
    private List<Patient> patientList;
    private List<Patient> filteredList;

    private int doctorId = -1;
    private SessionManager sessionManager;

    private final ActivityResultLauncher<Intent> patientFormLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK) {
                    loadPatients();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPatientsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize DAO
        patientDAO = new PatientDAO(requireContext());

        // Load current doctor id
        sessionManager = new SessionManager(requireContext());
        doctorId = (int) sessionManager.getUserId();

        // Initialize UI
        setupRecyclerView();
        setupSearchView();

        // Load data
        loadPatients();
    }

    private void setupRecyclerView() {
        patientList = new ArrayList<>();
        filteredList = new ArrayList<>();

        patientAdapter = new PatientAdapter(requireContext(), filteredList);
        binding.rvPatients.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvPatients.setAdapter(patientAdapter);

        // Set click listeners
        patientAdapter.setOnItemClickListener(new PatientAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Patient patient) {
                // Open patient details
                Intent intent = new Intent(requireContext(), PatientDetailsActivity.class);
                intent.putExtra(Constants.EXTRA_PATIENT_ID, patient.getId());
                startActivity(intent);
            }

            @Override
            public void onEditClick(Patient patient) {
                // Open edit patient
                Intent intent = new Intent(requireContext(), AddPatientActivity.class);
                intent.putExtra(Constants.EXTRA_PATIENT_ID, patient.getId());
                intent.putExtra(Constants.EXTRA_IS_EDIT_MODE, true);
                patientFormLauncher.launch(intent);
            }

            @Override
            public void onDeleteClick(Patient patient) {
                showDeleteConfirmationDialog(patient);
            }
        });
    }

    private void setupSearchView() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterPatients(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void loadPatients() {
        if (doctorId == -1) {
            patientList.clear();
            filterPatients("");
            return;
        }

        patientList = patientDAO.getAllPatients(doctorId);
        filterPatients(binding.etSearch.getText().toString().trim());
    }

    private void filterPatients(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            // Show all patients
            filteredList.addAll(patientList);
        } else {
            // Filter by name
            String lowerCaseQuery = query.toLowerCase();
            for (Patient patient : patientList) {
                String fullName = patient.getFullName().toLowerCase();
                if (fullName.contains(lowerCaseQuery)) {
                    filteredList.add(patient);
                }
            }
        }

        patientAdapter.notifyDataSetChanged();
        updateUI();
    }

    private void updateUI() {
        // Update total count
        binding.tvTotalPatients.setText(filteredList.size() + " Total");

        // Show/hide empty state
        if (filteredList.isEmpty()) {
            binding.rvPatients.setVisibility(View.GONE);
            binding.tvEmptyState.setVisibility(View.VISIBLE);

            String searchQuery = binding.etSearch.getText().toString().trim();
            if (searchQuery.isEmpty()) {
                binding.tvEmptyState.setText(R.string.no_patients_found);
            } else {
                binding.tvEmptyState.setText("No patients found for \"" + searchQuery + "\"");
            }
        } else {
            binding.rvPatients.setVisibility(View.VISIBLE);
            binding.tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void showDeleteConfirmationDialog(final Patient patient) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_confirmation)
                .setMessage(R.string.delete_patient_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> deletePatient(patient))
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deletePatient(Patient patient) {
        int result = patientDAO.deletePatient(patient.getId());

        if (result > 0) {
            Toast.makeText(requireContext(), R.string.patient_deleted, Toast.LENGTH_SHORT).show();

            // Remove from lists
            patientList.remove(patient);
            filteredList.remove(patient);

            patientAdapter.notifyDataSetChanged();
            updateUI();
        } else {
            Toast.makeText(requireContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data when returning to this fragment
        loadPatients();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
