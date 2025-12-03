package com.example.medimanager.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medimanager.R;
import com.example.medimanager.databinding.ItemAppointmentBinding;
import com.example.medimanager.models.Appointment;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder> {

    private final Context context;
    private List<Appointment> appointmentList;
    private OnItemClickListener listener;
    private boolean readOnly = false;

    // Interface for click listeners
    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
        void onStatusClick(Appointment appointment);
    }

    public AppointmentAdapter(Context context, List<Appointment> appointmentList) {
        this.context = context;
        this.appointmentList = appointmentList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AppointmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemAppointmentBinding binding = ItemAppointmentBinding.inflate(LayoutInflater.from(context), parent, false);
        return new AppointmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppointmentViewHolder holder, int position) {
        Appointment appointment = appointmentList.get(position);
        holder.bind(appointment);
    }

    @Override
    public int getItemCount() {
        return appointmentList != null ? appointmentList.size() : 0;
    }

    public void updateList(List<Appointment> newList) {
        this.appointmentList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (appointmentList != null && position >= 0 && position < appointmentList.size()) {
            appointmentList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, appointmentList.size());
        }
    }

    public class AppointmentViewHolder extends RecyclerView.ViewHolder {
        private final ItemAppointmentBinding binding;

        public AppointmentViewHolder(ItemAppointmentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Appointment appointment) {
            // Set patient name
            if (appointment.getPatientName() != null && !appointment.getPatientName().isEmpty()) {
                binding.tvPatientName.setText(appointment.getPatientName());
            } else {
                binding.tvPatientName.setText("Unknown Patient");
            }

            // Set appointment time
            if (appointment.getAppointmentTime() != null && !appointment.getAppointmentTime().isEmpty()) {
                binding.tvTime.setText(appointment.getAppointmentTime());
            } else {
                binding.tvTime.setText("--:--");
            }

            // Set reason
            if (appointment.getReason() != null && !appointment.getReason().isEmpty()) {
                binding.tvReason.setText(appointment.getReason());
            } else {
                binding.tvReason.setText("No reason specified");
            }

            // Set status with color
            String status = appointment.getStatusDisplayName();
            binding.tvStatus.setText(status);

            // Set status background color and text color based on status
            if (appointment.isCompleted()) {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_completed);
                binding.tvStatus.setTextColor(Color.WHITE);
            } else if (appointment.isInProgress()) {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_in_progress);
                binding.tvStatus.setTextColor(Color.WHITE);
            } else if (appointment.isCancelled()) {
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_scheduled);
                binding.tvStatus.setTextColor(Color.WHITE);
            } else {
                // Scheduled (default)
                binding.tvStatus.setBackgroundResource(R.drawable.bg_status_scheduled);
                binding.tvStatus.setTextColor(Color.WHITE);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(appointment);
                }
            });

            // Status click only enabled for doctors (not read-only)
            if (!readOnly) {
                binding.tvStatus.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onStatusClick(appointment);
                    }
                });
            } else {
                binding.tvStatus.setOnClickListener(null);
                binding.tvStatus.setClickable(false);
            }

            // Optional: Make the card clickable with ripple effect
            binding.getRoot().setClickable(true);
            binding.getRoot().setFocusable(true);
        }
    }
}
