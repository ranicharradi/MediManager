package com.example.medimanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.medimanager.models.Patient;

import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
    private final DatabaseHelper dbHelper;

    public PatientDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    // Create
    public long insertPatient(Patient patient) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
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

        return database.insert(DatabaseHelper.TABLE_PATIENTS, null, values);
    }

    // Read - Get by ID
    public Patient getPatientById(int id) {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_PATIENTS,
                null,
                DatabaseHelper.KEY_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Patient patient = null;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                patient = cursorToPatient(cursor);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patient;
    }

    // Read - Get all patients
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_PATIENTS,
                null, null, null, null, null,
                DatabaseHelper.KEY_FIRST_NAME + " ASC"
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    patients.add(cursorToPatient(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    // Read - Get recent patients
    public List<Patient> getRecentPatients(int limit) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.query(
                DatabaseHelper.TABLE_PATIENTS,
                null, null, null, null, null,
                DatabaseHelper.KEY_CREATED_AT + " DESC",
                String.valueOf(limit)
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    patients.add(cursorToPatient(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    // Read - Search patients
    public List<Patient> searchPatients(String query) {
        List<Patient> patients = new ArrayList<>();
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        String selection = DatabaseHelper.KEY_FIRST_NAME + " LIKE ? OR " +
                DatabaseHelper.KEY_LAST_NAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + query + "%", "%" + query + "%"};

        Cursor cursor = database.query(
                DatabaseHelper.TABLE_PATIENTS,
                null, selection, selectionArgs, null, null,
                DatabaseHelper.KEY_FIRST_NAME + " ASC"
        );

        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    patients.add(cursorToPatient(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return patients;
    }

    // Update
    public int updatePatient(Patient patient) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
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

        return database.update(
                DatabaseHelper.TABLE_PATIENTS,
                values,
                DatabaseHelper.KEY_ID + " = ?",
                new String[]{String.valueOf(patient.getId())}
        );
    }

    // Update last visit
    public int updateLastVisit(int patientId, String lastVisit) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_LAST_VISIT, lastVisit);

        return database.update(
                DatabaseHelper.TABLE_PATIENTS,
                values,
                DatabaseHelper.KEY_ID + " = ?",
                new String[]{String.valueOf(patientId)}
        );
    }

    // Delete
    public int deletePatient(int id) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        return database.delete(
                DatabaseHelper.TABLE_PATIENTS,
                DatabaseHelper.KEY_ID + " = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // Statistics
    public int getTotalPatientsCount() {
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        Cursor cursor = database.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PATIENTS,
                null
        );

        int count = 0;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return count;
    }

    // Helper method to convert cursor to Patient object
    private Patient cursorToPatient(Cursor cursor) {
        Patient patient = new Patient();

        patient.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID)));
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
