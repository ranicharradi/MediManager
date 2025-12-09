package com.example.medimanager.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.medimanager.R;
import com.example.medimanager.activities.EditProfileActivity;
import com.example.medimanager.activities.LoginActivity;
import com.example.medimanager.activities.NotificationSettingsActivity;
import com.example.medimanager.database.UserDAO;
import com.example.medimanager.databinding.FragmentProfileBinding;
import com.example.medimanager.models.User;
import com.example.medimanager.utils.Constants;
import com.example.medimanager.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private UserDAO userDAO;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = new SessionManager(requireContext());
        userDAO = new UserDAO(requireContext());

        loadUserData();

        binding.editProfileButton.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), EditProfileActivity.class));
        });

        binding.notificationsButton.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), NotificationSettingsActivity.class));
        });

        binding.logoutButton.setOnClickListener(v -> {
            logout();
        });
    }

    private void loadUserData() {
        String email = sessionManager.getUserEmail();
        boolean isDoctor = sessionManager.isDoctor();

        // Get user from database
        User user = userDAO.getUserByEmail(email);

        if (user != null) {
            String displayName = user.getFullName();
            if (isDoctor) {
                displayName = "Dr. " + displayName;
            }
            binding.doctorName.setText(displayName);
            binding.doctorEmail.setText(user.getEmail());

            // Update profile image based on role
            if (isDoctor) {
                binding.profileImage.setImageResource(R.drawable.ic_doctor);
            } else {
                binding.profileImage.setImageResource(R.drawable.ic_patient);
            }
        } else {
            // Fallback to stored name
            String name = sessionManager.getUserName();
            binding.doctorName.setText(name);
            binding.doctorEmail.setText(email);
        }

        // Show role badge
        binding.tvRole.setText(isDoctor ? R.string.doctor : R.string.patient);
        binding.tvRole.setVisibility(View.VISIBLE);
    }

    private void logout() {
        // Reset session flags for login alerts
        HomeFragment.resetSessionFlag();
        PatientHomeFragment.resetSessionFlag();
        
        // Clear login state
        sessionManager.clearSession();

        // Navigate to login
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
