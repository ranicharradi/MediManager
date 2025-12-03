package com.example.medimanager.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.medimanager.R;
import com.example.medimanager.activities.MainActivity;

public class NotificationHelper {

    /**
     * Check if notifications are enabled in app settings
     */
    public static boolean areNotificationsEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, true);
    }

    public static final String CHANNEL_ID_APPOINTMENTS = "appointments_channel";
    public static final String CHANNEL_NAME_APPOINTMENTS = "Appointments";

    private static final int NOTIFICATION_ID_BASE = 2000;

    /**
     * Create notification channels (required for Android 8.0+)
     * Call this in Application onCreate or MainActivity
     */
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_APPOINTMENTS,
                    CHANNEL_NAME_APPOINTMENTS,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for appointment updates");
            
            // Enable lights
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            
            // Enable vibration
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 200, 500});
            
            // Set sound
            Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);
            
            // Show on lock screen
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Show notification when a doctor adds a new appointment for a patient
     */
    public static void notifyPatientNewAppointment(Context context, String doctorName, String date, String time, String reason) {
        String title = "📅 " + context.getString(R.string.new_appointment_notification);
        String message = String.format("Dr. %s scheduled an appointment for you on %s at %s\nReason: %s",
                doctorName, date, time, reason);

        showNotification(context, title, message, NOTIFICATION_ID_BASE + 1);
    }

    /**
     * Show notification when a patient's appointment request is approved
     */
    public static void notifyPatientAppointmentApproved(Context context, String date, String time) {
        String title = "✅ " + context.getString(R.string.appointment_approved);
        String message = String.format("Your appointment request for %s at %s has been approved!", date, time);

        showNotification(context, title, message, NOTIFICATION_ID_BASE + 2);
    }

    /**
     * Show notification when a patient's appointment request is rejected
     */
    public static void notifyPatientAppointmentRejected(Context context, String date, String time) {
        String title = "❌ " + context.getString(R.string.appointment_rejected);
        String message = String.format("Your appointment request for %s at %s has been rejected.", date, time);

        showNotification(context, title, message, NOTIFICATION_ID_BASE + 3);
    }

    /**
     * Show notification when a patient requests an appointment (for doctor)
     */
    public static void notifyDoctorNewRequest(Context context, String patientName, String date, String time, String reason) {
        String title = "🔔 " + context.getString(R.string.appointment_request_notification);
        String message = String.format("%s has requested an appointment on %s at %s\nReason: %s",
                patientName, date, time, reason);

        showNotification(context, title, message, NOTIFICATION_ID_BASE + 4);
    }

    /**
     * Show notification when appointment is modified
     */
    public static void notifyPatientAppointmentModified(Context context, String oldDate, String newDate, String newTime) {
        String title = "🔄 Appointment Modified";
        String message = String.format("Your appointment has been rescheduled to %s at %s", newDate, newTime);

        showNotification(context, title, message, NOTIFICATION_ID_BASE + 5);
    }

    private static void showNotification(Context context, String title, String message, int notificationId) {
        // Check if notifications are enabled in app settings
        if (!areNotificationsEnabled(context)) {
            return;
        }

        // Create intent to open MainActivity when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Get default notification sound
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_APPOINTMENTS)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message)
                        .setSummaryText("MediManager"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setLights(Color.BLUE, 1000, 500)
                .setColor(context.getResources().getColor(R.color.primary, null))
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, can't show notification
                return;
            }
        }

        notificationManager.notify(notificationId, builder.build());
    }
}
