package com.example.medimanager.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medimanager.R;
import com.example.medimanager.databinding.ItemPatientBinding;
import com.example.medimanager.models.Patient;

import java.util.List;
import java.util.Random;

public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.PatientViewHolder> {

    private final Context context;
    private List<Patient> patientList;
    private OnItemClickListener listener;

    // Interface for click listeners
    public interface OnItemClickListener {
        void onItemClick(Patient patient);
        void onEditClick(Patient patient);
        void onDeleteClick(Patient patient);
    }

    public PatientAdapter(Context context, List<Patient> patientList) {
        this.context = context;
        this.patientList = patientList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPatientBinding binding = ItemPatientBinding.inflate(LayoutInflater.from(context), parent, false);
        return new PatientViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        Patient patient = patientList.get(position);
        holder.bind(patient);
    }

    @Override
    public int getItemCount() {
        return patientList != null ? patientList.size() : 0;
    }

    public void updateList(List<Patient> newList) {
        this.patientList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        patientList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, patientList.size());
    }

    private int getRandomAvatarColor() {
        int[] colors = {
                Color.parseColor("#64B5F6"), // Blue
                Color.parseColor("#81C784"), // Green
                Color.parseColor("#FFB74D"), // Orange
                Color.parseColor("#BA68C8"), // Purple
                Color.parseColor("#F06292")  // Pink
        };

        Random random = new Random();
        return colors[random.nextInt(colors.length)];
    }

    public class PatientViewHolder extends RecyclerView.ViewHolder {
        private final ItemPatientBinding binding;

        public PatientViewHolder(ItemPatientBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Patient patient) {
            // Set patient name
            binding.tvPatientName.setText(patient.getFullName());

            // Set avatar initials
            binding.tvAvatar.setText(patient.getInitials());

            // Set random avatar color
            binding.tvAvatar.setBackgroundColor(getRandomAvatarColor());

            // Set age
            binding.tvAge.setText(context.getString(R.string.age_years_short, patient.getAge()));

            // Set gender
            binding.tvGender.setText(patient.getGender() != null ? patient.getGender() : "");

            // Set blood group
            String bloodGroup = patient.getBloodGroup();
            if (bloodGroup != null && !bloodGroup.isEmpty()) {
                binding.tvBloodGroup.setText(context.getString(com.example.medimanager.R.string.blood_prefix, bloodGroup));
            } else {
                binding.tvBloodGroup.setText(context.getString(com.example.medimanager.R.string.blood_unknown));
            }

            // Set last visit
            String lastVisit = patient.getLastVisit();
            if (lastVisit != null && !lastVisit.isEmpty()) {
                binding.tvLastVisit.setText(context.getString(com.example.medimanager.R.string.last_visit_prefix, lastVisit));
            } else {
                binding.tvLastVisit.setText(context.getString(com.example.medimanager.R.string.last_visit_unknown));
            }

            // Set click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(patient);
                }
            });

            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(patient);
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(patient);
                }
            });
        }
    }
}
