package com.example.medimanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.medimanager.models.Appointment;
import com.example.medimanager.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {
    private final DatabaseHelper dbHelper;
    private static final String TAG = "AppointmentDAO";

    public AppointmentDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // Create
    public long insertAppointment(Appointment appointment) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_PATIENT_ID, appointment.getPatientId());
        values.put(DatabaseHelper.KEY_DOCTOR_ID, appointment.getDoctorId());
        values.put(DatabaseHelper.KEY_APPOINTMENT_DATE, appointment.getAppointmentDate());
        values.put(DatabaseHelper.KEY_APPOINTMENT_TIME, appointment.getAppointmentTime());
        values.put(DatabaseHelper.KEY_REASON, appointment.getReason());
        values.put(DatabaseHelper.KEY_STATUS, appointment.getStatus());
        values.put(DatabaseHelper.KEY_NOTES, appointment.getNotes());

        try {
            return database.insert(DatabaseHelper.TABLE_APPOINTMENTS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting appointment", e);
            return -1;
        }
    }

    // Read - Get by ID
    public Appointment getAppointmentById(int id) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String query = "SELECT a.*, p." + DatabaseHelper.KEY_FIRST_NAME + " || ' ' || p." +
                DatabaseHelper.KEY_LAST_NAME + " as patient_name, " +
                "u." + DatabaseHelper.KEY_USER_FIRST_NAME + " || ' ' || u." +
                DatabaseHelper.KEY_USER_LAST_NAME + " as doctor_name FROM " +
                DatabaseHelper.TABLE_APPOINTMENTS + " a " +
                "LEFT JOIN " + DatabaseHelper.TABLE_PATIENTS + " p ON a." +
                DatabaseHelper.KEY_PATIENT_ID + " = p." + DatabaseHelper.KEY_ID +
                " LEFT JOIN " + DatabaseHelper.TABLE_USERS + " u ON a." +
                DatabaseHelper.KEY_DOCTOR_ID + " = u." + DatabaseHelper.KEY_ID +
                " WHERE a." + DatabaseHelper.KEY_ID + " = ?";

        Appointment appointment = null;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, new String[]{String.valueOf(id)});
            if (cursor.moveToFirst()) {
                appointment = cursorToAppointment(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading appointment by id", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return appointment;
    }

    // Read - Get all appointments
    public List<Appointment> getAllAppointments(int doctorId) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String query = "SELECT a.*, p." + DatabaseHelper.KEY_FIRST_NAME + " || ' ' || p." +
                DatabaseHelper.KEY_LAST_NAME + " as patient_name, " +
                "u." + DatabaseHelper.KEY_USER_FIRST_NAME + " || ' ' || u." +
                DatabaseHelper.KEY_USER_LAST_NAME + " as doctor_name FROM " +
                DatabaseHelper.TABLE_APPOINTMENTS + " a " +
                "LEFT JOIN " + DatabaseHelper.TABLE_PATIENTS + " p ON a." +
                DatabaseHelper.KEY_PATIENT_ID + " = p." + DatabaseHelper.KEY_ID +
                " LEFT JOIN " + DatabaseHelper.TABLE_USERS + " u ON a." +
                DatabaseHelper.KEY_DOCTOR_ID + " = u." + DatabaseHelper.KEY_ID +
                " WHERE a." + DatabaseHelper.KEY_DOCTOR_ID + " = ?" +
                " ORDER BY a." + DatabaseHelper.KEY_APPOINTMENT_DATE + " DESC, a." +
                DatabaseHelper.KEY_APPOINTMENT_TIME + " DESC";

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, new String[]{String.valueOf(doctorId)});

            if (cursor.moveToFirst()) {
                do {
                    appointments.add(cursorToAppointment(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading appointments", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return appointments;
    }

    // Read - Get appointments by patient
    public List<Appointment> getAppointmentsByPatient(int patientId) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String query = "SELECT a.*, p." + DatabaseHelper.KEY_FIRST_NAME + " || ' ' || p." +
                DatabaseHelper.KEY_LAST_NAME + " as patient_name, " +
                "u." + DatabaseHelper.KEY_USER_FIRST_NAME + " || ' ' || u." +
                DatabaseHelper.KEY_USER_LAST_NAME + " as doctor_name FROM " +
                DatabaseHelper.TABLE_APPOINTMENTS + " a " +
                "LEFT JOIN " + DatabaseHelper.TABLE_PATIENTS + " p ON a." +
                DatabaseHelper.KEY_PATIENT_ID + " = p." + DatabaseHelper.KEY_ID +
                " LEFT JOIN " + DatabaseHelper.TABLE_USERS + " u ON a." +
                DatabaseHelper.KEY_DOCTOR_ID + " = u." + DatabaseHelper.KEY_ID +
                " WHERE a." + DatabaseHelper.KEY_PATIENT_ID + " = ?" +
                " ORDER BY a." + DatabaseHelper.KEY_APPOINTMENT_DATE + " DESC";

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, new String[]{String.valueOf(patientId)});
            if (cursor.moveToFirst()) {
                do {
                    appointments.add(cursorToAppointment(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading appointments by patient", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return appointments;
    }

    // Read - Get today's appointments
    public List<Appointment> getTodayAppointments(int doctorId, String today) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String query = "SELECT a.*, p." + DatabaseHelper.KEY_FIRST_NAME + " || ' ' || p." +
                DatabaseHelper.KEY_LAST_NAME + " as patient_name, " +
                "u." + DatabaseHelper.KEY_USER_FIRST_NAME + " || ' ' || u." +
                DatabaseHelper.KEY_USER_LAST_NAME + " as doctor_name FROM " +
                DatabaseHelper.TABLE_APPOINTMENTS + " a " +
                "LEFT JOIN " + DatabaseHelper.TABLE_PATIENTS + " p ON a." +
                DatabaseHelper.KEY_PATIENT_ID + " = p." + DatabaseHelper.KEY_ID +
                " LEFT JOIN " + DatabaseHelper.TABLE_USERS + " u ON a." +
                DatabaseHelper.KEY_DOCTOR_ID + " = u." + DatabaseHelper.KEY_ID +
                " WHERE a." + DatabaseHelper.KEY_DOCTOR_ID + " = ? AND a." + DatabaseHelper.KEY_APPOINTMENT_DATE + " = ?" +
                " ORDER BY a." + DatabaseHelper.KEY_APPOINTMENT_TIME + " ASC";

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, new String[]{String.valueOf(doctorId), today});

            if (cursor.moveToFirst()) {
                do {
                    appointments.add(cursorToAppointment(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading today's appointments", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return appointments;
    }

    // Read - Get appointments by status
    public List<Appointment> getAppointmentsByStatus(int doctorId, String status) {
        List<Appointment> appointments = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String query = "SELECT a.*, p." + DatabaseHelper.KEY_FIRST_NAME + " || ' ' || p." +
                DatabaseHelper.KEY_LAST_NAME + " as patient_name, " +
                "u." + DatabaseHelper.KEY_USER_FIRST_NAME + " || ' ' || u." +
                DatabaseHelper.KEY_USER_LAST_NAME + " as doctor_name FROM " +
                DatabaseHelper.TABLE_APPOINTMENTS + " a " +
                "LEFT JOIN " + DatabaseHelper.TABLE_PATIENTS + " p ON a." +
                DatabaseHelper.KEY_PATIENT_ID + " = p." + DatabaseHelper.KEY_ID +
                " LEFT JOIN " + DatabaseHelper.TABLE_USERS + " u ON a." +
                DatabaseHelper.KEY_DOCTOR_ID + " = u." + DatabaseHelper.KEY_ID +
                " WHERE a." + DatabaseHelper.KEY_DOCTOR_ID + " = ? AND a." + DatabaseHelper.KEY_STATUS + " = ?" +
                " ORDER BY a." + DatabaseHelper.KEY_APPOINTMENT_DATE + " DESC";

        Cursor cursor = null;
        try {
            cursor = database.rawQuery(query, new String[]{String.valueOf(doctorId), status});

            if (cursor.moveToFirst()) {
                do {
                    appointments.add(cursorToAppointment(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading appointments by status", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return appointments;
    }

    // Update
    public int updateAppointment(Appointment appointment) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_PATIENT_ID, appointment.getPatientId());
        values.put(DatabaseHelper.KEY_DOCTOR_ID, appointment.getDoctorId());
        values.put(DatabaseHelper.KEY_APPOINTMENT_DATE, appointment.getAppointmentDate());
        values.put(DatabaseHelper.KEY_APPOINTMENT_TIME, appointment.getAppointmentTime());
        values.put(DatabaseHelper.KEY_REASON, appointment.getReason());
        values.put(DatabaseHelper.KEY_STATUS, appointment.getStatus());
        values.put(DatabaseHelper.KEY_NOTES, appointment.getNotes());

        try {
            return database.update(
                    DatabaseHelper.TABLE_APPOINTMENTS,
                    values,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(appointment.getId())}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error updating appointment", e);
            return 0;
        }
    }

    // Update status only
    public int updateAppointmentStatus(int id, String status) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_STATUS, status);

        try {
            return database.update(
                    DatabaseHelper.TABLE_APPOINTMENTS,
                    values,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error updating appointment status", e);
            return 0;
        }
    }

    // Delete
    public int deleteAppointment(int id) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            return database.delete(
                    DatabaseHelper.TABLE_APPOINTMENTS,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error deleting appointment", e);
            return 0;
        }
    }

    // Statistics
        public int getTodayAppointmentsCount(int doctorId, String today) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_APPOINTMENTS +
                    " WHERE " + DatabaseHelper.KEY_DOCTOR_ID + " = ? AND " +
                    DatabaseHelper.KEY_APPOINTMENT_DATE + " = ?",
                new String[]{String.valueOf(doctorId), today}
            );

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error counting today's appointments", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

        public int getUpcomingAppointmentsCount(int doctorId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_APPOINTMENTS +
                    " WHERE " + DatabaseHelper.KEY_DOCTOR_ID + " = ? AND (" +
                    DatabaseHelper.KEY_STATUS + " = '" + Constants.STATUS_SCHEDULED + "' OR " +
                    DatabaseHelper.KEY_STATUS + " = '" + Constants.STATUS_IN_PROGRESS + "')",
                new String[]{String.valueOf(doctorId)}
            );

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error counting upcoming appointments", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    // Helper method
    private Appointment cursorToAppointment(Cursor cursor) {
        Appointment appointment = new Appointment();

        appointment.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID)));
        appointment.setPatientId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PATIENT_ID)));
        appointment.setDoctorId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DOCTOR_ID)));
        appointment.setAppointmentDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_APPOINTMENT_DATE)));
        appointment.setAppointmentTime(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_APPOINTMENT_TIME)));
        appointment.setReason(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_REASON)));
        appointment.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_STATUS)));
        appointment.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_NOTES)));
        appointment.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT)));

        // Get patient name from JOIN
        int nameIndex = cursor.getColumnIndex("patient_name");
        if (nameIndex != -1) {
            appointment.setPatientName(cursor.getString(nameIndex));
        }

        // Get doctor name from JOIN
        int doctorNameIndex = cursor.getColumnIndex("doctor_name");
        if (doctorNameIndex != -1) {
            appointment.setDoctorName(cursor.getString(doctorNameIndex));
        }

        return appointment;
    }
}
