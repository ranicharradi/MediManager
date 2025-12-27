package com.example.medimanager.utils;

import android.content.Context;
import android.content.DialogInterface;

import androidx.appcompat.app.AlertDialog;

import com.example.medimanager.R;
import com.example.medimanager.models.Appointment;

public final class AppointmentApprovalHelper {

    public interface ApprovalActions {
        void onApprove(Appointment appointment);
        void onModify(Appointment appointment);
        void onReject(Appointment appointment);
    }

    private AppointmentApprovalHelper() {
        throw new AssertionError("No instances.");
    }

    public static void showApprovalDialog(Context context, Appointment appointment, ApprovalActions actions) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.pending_appointment_request)
                .setMessage(context.getString(R.string.appointment_request_details,
                        appointment.getPatientName(),
                        appointment.getAppointmentDate(),
                        appointment.getAppointmentTime(),
                        appointment.getReason()))
                .setPositiveButton(R.string.approve_appointment, (dialog, which) -> {
                    actions.onApprove(appointment);
                })
                .setNeutralButton(R.string.modify_and_approve, (dialog, which) -> {
                    actions.onModify(appointment);
                })
                .setNegativeButton(R.string.reject_appointment, (dialog, which) -> {
                    actions.onReject(appointment);
                })
                .show();
    }
}
