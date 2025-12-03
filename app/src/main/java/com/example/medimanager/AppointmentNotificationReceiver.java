package com.example.medimanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.medimanager.activities.MainActivity;
import com.example.medimanager.utils.Constants;

public class AppointmentNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "appointment_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if notifications are enabled
        android.content.SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        boolean areNotificationsEnabled = prefs.getBoolean(Constants.PREF_NOTIFICATIONS_ENABLED, true);

        if (!areNotificationsEnabled) {
            return;
        }

        int appointmentId = intent.getIntExtra("appointment_id", -1);
        String patientName = intent.getStringExtra("patient_name");
        String appointmentTime = intent.getStringExtra("appointment_time");

        if (appointmentId != -1) {
            createNotificationChannel(context);
            showNotification(context, appointmentId, patientName, appointmentTime);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Appointment Reminders";
            String description = "Channel for appointment reminder notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH; // High importance for heads-up
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
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

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void showNotification(Context context, int appointmentId, String patientName, String appointmentTime) {
        // Intent to open the app when notification is clicked
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appointmentId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Get default notification sound
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        
        String contentText = "You have an appointment with " + patientName + " at " + appointmentTime;

        // Build notification with enhanced visibility
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("📅 Appointment Reminder")
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(contentText)
                        .setBigContentTitle("📅 Upcoming Appointment")
                        .setSummaryText("MediManager"))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(soundUri)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setLights(Color.BLUE, 1000, 500)
                .setColor(context.getResources().getColor(R.color.primary, null))
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(appointmentId, builder.build());
    }
}
