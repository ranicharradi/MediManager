package com.example.medimanager.utils;

import android.content.Context;

import com.example.medimanager.R;

public final class AppointmentStatusUtils {

    private AppointmentStatusUtils() {
        throw new AssertionError("No instances.");
    }

    public static String getStatusLabel(Context context, String status) {
        if (status == null) {
            return context.getString(R.string.scheduled);
        }
        if (Constants.STATUS_PENDING.equalsIgnoreCase(status)) {
            return context.getString(R.string.pending);
        }
        if (Constants.STATUS_SCHEDULED.equalsIgnoreCase(status)) {
            return context.getString(R.string.scheduled);
        }
        if (Constants.STATUS_IN_PROGRESS.equalsIgnoreCase(status)) {
            return context.getString(R.string.in_progress);
        }
        if (Constants.STATUS_COMPLETED.equalsIgnoreCase(status)) {
            return context.getString(R.string.completed);
        }
        if (Constants.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            return context.getString(R.string.cancelled);
        }
        return context.getString(R.string.scheduled);
    }
}
