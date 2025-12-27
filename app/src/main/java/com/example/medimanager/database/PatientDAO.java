package com.example.medimanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.medimanager.models.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    private final DatabaseHelper dbHelper;
    private static final String TAG = "PatientDAO";

    public PatientDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // Create
    public long insertPatient(Patient patient) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_DOCTOR_ID, patient.getDoctorId());
        if (patient.getUserId() != null) {
            values.put(DatabaseHelper.KEY_USER_ID, patient.getUserId());
        }
        values.put(DatabaseHelper.KEY_FIRST_NAME, patient.getFirstName());
        values.put(DatabaseHelper.KEY_LAST_NAME, patient.getLastName());
        values.put(DatabaseHelper.KEY_DATE_OF_BIRTH, patient.getDateOfBirth());
        values.put(DatabaseHelper.KEY_GENDER, patient.getGender());
        values.put(DatabaseHelper.KEY_PHONE, patient.getPhone());
        values.put(DatabaseHelper.KEY_EMAIL, patient.getEmail());
        values.put(DatabaseHelper.KEY_ADDRESS, patient.getAddress());
        values.put(DatabaseHelper.KEY_BLOOD_GROUP, patient.getBloodGroup());
        values.put(DatabaseHelper.KEY_ALLERGIES, patient.getAllergies());
        values.put(DatabaseHelper.KEY_LAST_VISIT, patient.getLastVisit());

        try {
            return database.insert(DatabaseHelper.TABLE_PATIENTS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error inserting patient", e);
            return -1;
        }
    }

    // Read - Get by ID
    public Patient getPatientById(int id) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Patient patient = null;
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_PATIENTS,
                    null,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(id)},
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                patient = cursorToPatient(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading patient by id", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patient;
    }

    // Read - Get all patients
    public List<Patient> getAllPatients(int doctorId) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(
                DatabaseHelper.TABLE_PATIENTS,
                null,
                DatabaseHelper.KEY_DOCTOR_ID + " = ?",
                new String[]{String.valueOf(doctorId)},
                null,
                null,
                DatabaseHelper.KEY_FIRST_NAME + " ASC"
            );

            if (cursor.moveToFirst()) {
                do {
                    patients.add(cursorToPatient(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading patients", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    // Read - Get recent patients
    public List<Patient> getRecentPatients(int doctorId, int limit) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.KEY_DOCTOR_ID + " = ?";
        String[] selectionArgs = new String[]{String.valueOf(doctorId)};

        Cursor cursor = null;
        try {
            cursor = database.query(
                DatabaseHelper.TABLE_PATIENTS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.KEY_CREATED_AT + " DESC",
                String.valueOf(limit)
            );

            if (cursor.moveToFirst()) {
                do {
                    patients.add(cursorToPatient(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading recent patients", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    // Read - Search patients
        public List<Patient> searchPatients(int doctorId, String query) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.KEY_DOCTOR_ID + " = ? AND (" +
            DatabaseHelper.KEY_FIRST_NAME + " LIKE ? OR " +
            DatabaseHelper.KEY_LAST_NAME + " LIKE ?)";
        String[] selectionArgs = new String[]{
            String.valueOf(doctorId),
            "%" + query + "%",
            "%" + query + "%"
        };

        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_PATIENTS,
                    null, selection, selectionArgs, null, null,
                    DatabaseHelper.KEY_FIRST_NAME + " ASC"
            );

            if (cursor.moveToFirst()) {
                do {
                    patients.add(cursorToPatient(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error searching patients", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    // Read - Get patient by email
    public Patient getPatientByEmail(String email) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Patient patient = null;
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_PATIENTS,
                    null,
                    DatabaseHelper.KEY_EMAIL + " = ?",
                    new String[]{email},
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                patient = cursorToPatient(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading patient by email", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patient;
    }

    // Read - Get patient by user_id (for linking patient user accounts)
    public Patient getPatientByUserId(int userId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Patient patient = null;
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_PATIENTS,
                    null,
                    DatabaseHelper.KEY_USER_ID + " = ?",
                    new String[]{String.valueOf(userId)},
                    null, null, null
            );

            if (cursor.moveToFirst()) {
                patient = cursorToPatient(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading patient by user id", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patient;
    }

    // Update
    public int updatePatient(Patient patient) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_DOCTOR_ID, patient.getDoctorId());
        if (patient.getUserId() != null) {
            values.put(DatabaseHelper.KEY_USER_ID, patient.getUserId());
        } else {
            values.putNull(DatabaseHelper.KEY_USER_ID);
        }
        values.put(DatabaseHelper.KEY_FIRST_NAME, patient.getFirstName());
        values.put(DatabaseHelper.KEY_LAST_NAME, patient.getLastName());
        values.put(DatabaseHelper.KEY_DATE_OF_BIRTH, patient.getDateOfBirth());
        values.put(DatabaseHelper.KEY_GENDER, patient.getGender());
        values.put(DatabaseHelper.KEY_PHONE, patient.getPhone());
        values.put(DatabaseHelper.KEY_EMAIL, patient.getEmail());
        values.put(DatabaseHelper.KEY_ADDRESS, patient.getAddress());
        values.put(DatabaseHelper.KEY_BLOOD_GROUP, patient.getBloodGroup());
        values.put(DatabaseHelper.KEY_ALLERGIES, patient.getAllergies());
        values.put(DatabaseHelper.KEY_LAST_VISIT, patient.getLastVisit());

        try {
            return database.update(
                    DatabaseHelper.TABLE_PATIENTS,
                    values,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(patient.getId())}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error updating patient", e);
            return 0;
        }
    }

    // Update last visit
    public int updateLastVisit(int patientId, String lastVisit) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_LAST_VISIT, lastVisit);

        try {
            return database.update(
                    DatabaseHelper.TABLE_PATIENTS,
                    values,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(patientId)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error updating last visit", e);
            return 0;
        }
    }

    // Delete
    public int deletePatient(int id) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        try {
            return database.delete(
                    DatabaseHelper.TABLE_PATIENTS,
                    DatabaseHelper.KEY_ID + " = ?",
                    new String[]{String.valueOf(id)}
            );
        } catch (Exception e) {
            Log.e(TAG, "Error deleting patient", e);
            return 0;
        }
    }

    // Statistics
    public int getTotalPatientsCount(int doctorId) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        int count = 0;
        Cursor cursor = null;
        try {
            cursor = database.rawQuery(
                    "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PATIENTS +
                            " WHERE " + DatabaseHelper.KEY_DOCTOR_ID + " = ?",
                    new String[]{String.valueOf(doctorId)}
            );

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error counting patients", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    public List<Patient> getPatientsByDoctor(int doctorId) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = database.query(
                    DatabaseHelper.TABLE_PATIENTS,
                    null,
                    DatabaseHelper.KEY_DOCTOR_ID + " = ?",
                    new String[]{String.valueOf(doctorId)},
                    null,
                    null,
                    DatabaseHelper.KEY_FIRST_NAME + " ASC"
            );

            if (cursor.moveToFirst()) {
                do {
                    patients.add(cursorToPatient(cursor));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading patients by doctor", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    // Helper method to convert cursor to Patient object
    private Patient cursorToPatient(Cursor cursor) {
        Patient patient = new Patient();

        patient.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID)));
        patient.setDoctorId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DOCTOR_ID)));
        int userIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_ID);
        if (!cursor.isNull(userIdIndex)) {
            patient.setUserId(cursor.getInt(userIdIndex));
        }
        patient.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_FIRST_NAME)));
        patient.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_LAST_NAME)));
        patient.setDateOfBirth(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_DATE_OF_BIRTH)));
        patient.setGender(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_GENDER)));
        patient.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_PHONE)));
        patient.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_EMAIL)));
        patient.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ADDRESS)));
        patient.setBloodGroup(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_BLOOD_GROUP)));
        patient.setAllergies(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ALLERGIES)));
        patient.setLastVisit(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_LAST_VISIT)));
        patient.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT)));

        return patient;
    }
}
