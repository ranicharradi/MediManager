package com.example.medimanager.utils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

import com.example.medimanager.database.DatabaseHelper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utility class to export database contents to a readable text file.
 * Useful for debugging and tracking data changes.
 */
public class DatabaseExporter {

    private static final String TAG = "DatabaseExporter";
    private static final String EXPORT_FOLDER = "MediManager_DB_Export";

    /**
     * Export the entire database structure and data to a text file.
     * File is saved to the app's external files directory.
     * Runs on a background thread to avoid blocking the UI.
     *
     * @param context The application context
     * @param trigger Description of what triggered the export (e.g., "New Patient Added")
     */
    public static void exportDatabase(Context context, String trigger) {
        new Thread(() -> {
            Log.d(TAG, "Starting database export triggered by: " + trigger);
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            StringBuilder output = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String timestamp = sdf.format(new Date());

            // Header
            output.append("=".repeat(60)).append("\n");
            output.append("MediManager Database Export\n");
            output.append("=".repeat(60)).append("\n");
            output.append("Timestamp: ").append(timestamp).append("\n");
            output.append("Trigger: ").append(trigger).append("\n");
            output.append("Database Version: 5\n");
            output.append("=".repeat(60)).append("\n\n");

            // Export each table
            output.append(exportTable(db, DatabaseHelper.TABLE_USERS, "USERS"));
            output.append(exportTable(db, DatabaseHelper.TABLE_PATIENTS, "PATIENTS"));
            output.append(exportTable(db, DatabaseHelper.TABLE_APPOINTMENTS, "APPOINTMENTS"));
            output.append(exportTable(db, DatabaseHelper.TABLE_CONSULTATIONS, "CONSULTATIONS"));

            // Save to file
            saveToFile(context, output.toString(), timestamp);
        }).start();
    }

    private static String exportTable(SQLiteDatabase db, String tableName, String displayName) {
        StringBuilder sb = new StringBuilder();
        sb.append("-".repeat(60)).append("\n");
        sb.append("TABLE: ").append(displayName).append("\n");
        sb.append("-".repeat(60)).append("\n");

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName, null);

            if (cursor == null || cursor.getCount() == 0) {
                sb.append("(No records)\n\n");
                return sb.toString();
            }

            // Get column names
            String[] columns = cursor.getColumnNames();
            sb.append("Columns: ").append(String.join(", ", columns)).append("\n");
            sb.append("Total Records: ").append(cursor.getCount()).append("\n\n");

            // Export each row
            int rowNum = 1;
            while (cursor.moveToNext()) {
                sb.append("Record #").append(rowNum++).append(":\n");
                for (String col : columns) {
                    int idx = cursor.getColumnIndex(col);
                    String value = cursor.isNull(idx) ? "NULL" : cursor.getString(idx);
                    // Mask password for security
                    if (col.equals("password")) {
                        value = "********";
                    }
                    sb.append("  ").append(col).append(": ").append(value).append("\n");
                }
                sb.append("\n");
            }

        } catch (Exception e) {
            sb.append("Error reading table: ").append(e.getMessage()).append("\n");
            Log.e(TAG, "Error exporting table " + tableName, e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return sb.toString();
    }

    private static void saveToFile(Context context, String content, String timestamp) {
        try {
            // Get app's external files directory (no special permissions needed)
            File exportDir = new File(context.getExternalFilesDir(null), EXPORT_FOLDER);
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }

            // Delete all old export files first
            File[] oldFiles = exportDir.listFiles();
            if (oldFiles != null) {
                for (File oldFile : oldFiles) {
                    if (oldFile.isFile()) {
                        oldFile.delete();
                    }
                }
            }

            // Save only the latest export
            File latestFile = new File(exportDir, "latest_export.txt");
            FileWriter latestWriter = new FileWriter(latestFile);
            latestWriter.write(content);
            latestWriter.close();

            Log.i(TAG, "Database exported to: " + latestFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "Error saving export file", e);
        }
    }

    /**
     * Get the path where exports are saved.
     */
    public static String getExportPath(Context context) {
        File exportDir = new File(context.getExternalFilesDir(null), EXPORT_FOLDER);
        return exportDir.getAbsolutePath();
    }
}
