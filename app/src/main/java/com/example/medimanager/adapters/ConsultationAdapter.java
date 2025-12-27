package com.example.medimanager.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.medimanager.R;
import com.example.medimanager.databinding.ItemConsultationBinding;
import com.example.medimanager.models.Consultation;
import com.example.medimanager.utils.DateUtils;

import java.util.List;

public class ConsultationAdapter extends RecyclerView.Adapter<ConsultationAdapter.ConsultationViewHolder> {

    private final Context context;
    private List<Consultation> consultationList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Consultation consultation);
        void onEditClick(Consultation consultation);
        void onDeleteClick(Consultation consultation);
    }

    public ConsultationAdapter(Context context, List<Consultation> consultationList) {
        this.context = context;
        this.consultationList = consultationList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ConsultationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemConsultationBinding binding = ItemConsultationBinding.inflate(LayoutInflater.from(context), parent, false);
        return new ConsultationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConsultationViewHolder holder, int position) {
        Consultation consultation = consultationList.get(position);
        holder.bind(consultation);
    }

    @Override
    public int getItemCount() {
        return consultationList != null ? consultationList.size() : 0;
    }

    public void updateList(List<Consultation> newList) {
        this.consultationList = newList;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        consultationList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, consultationList.size());
    }

    public class ConsultationViewHolder extends RecyclerView.ViewHolder {
        private final ItemConsultationBinding binding;

        public ConsultationViewHolder(ItemConsultationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final Consultation consultation) {
            // Format and set consultation date
            String formattedDate = DateUtils.formatDate(consultation.getConsultationDate());
            binding.tvDate.setText(formattedDate);

            // Set diagnosis
            String diagnosis = consultation.getDiagnosis();
            if (diagnosis != null && !diagnosis.isEmpty()) {
                binding.tvDiagnosis.setText(diagnosis);
            } else {
                binding.tvDiagnosis.setText(context.getString(R.string.no_diagnosis));
            }

            // Set treatment
            String treatment = consultation.getTreatment();
            if (treatment != null && !treatment.isEmpty()) {
                binding.tvTreatment.setText(treatment);
            } else {
                binding.tvTreatment.setText(context.getString(R.string.no_treatment_specified));
            }

            // Set time ago
            String timeAgo = DateUtils.getTimeAgo(context, consultation.getConsultationDate());
            binding.tvTimeAgo.setText(timeAgo);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(consultation);
                }
            });

            binding.btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(consultation);
                }
            });

            binding.btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(consultation);
                }
            });
        }
    }
}
