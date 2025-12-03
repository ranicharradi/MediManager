package com.example.medimanager.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper instance;

    // Database Info
    private static final String DATABASE_NAME = "medimanager.db";
    private static final int DATABASE_VERSION = 4;

    // Table Names
    public static final String TABLE_PATIENTS = "patients";
    public static final String TABLE_CONSULTATIONS = "consultations";
    public static final String TABLE_APPOINTMENTS = "appointments";
    public static final String TABLE_USERS = "users";

    // Common Column Names
    public static final String KEY_ID = "id";
    public static final String KEY_CREATED_AT = "created_at";

    // Patients Table Columns
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_DATE_OF_BIRTH = "date_of_birth";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_BLOOD_GROUP = "blood_group";
    public static final String KEY_ALLERGIES = "allergies";
    public static final String KEY_LAST_VISIT = "last_visit";

    // Consultations Table Columns
    public static final String KEY_PATIENT_ID = "patient_id";
    public static final String KEY_DOCTOR_ID = "doctor_id";
    public static final String KEY_CONSULTATION_DATE = "consultation_date";
    public static final String KEY_DIAGNOSIS = "diagnosis";
    public static final String KEY_TREATMENT = "treatment";
    public static final String KEY_PRESCRIPTION = "prescription";
    public static final String KEY_NOTES = "notes";

    // Appointments Table Columns
    public static final String KEY_APPOINTMENT_DATE = "appointment_date";
    public static final String KEY_APPOINTMENT_TIME = "appointment_time";
    public static final String KEY_REASON = "reason";
    public static final String KEY_STATUS = "status";

    // Users Table Columns
    public static final String KEY_USER_FIRST_NAME = "first_name";
    public static final String KEY_USER_LAST_NAME = "last_name";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_PASSWORD = "password";
    public static final String KEY_USER_ROLE = "role";
    public static final String KEY_USER_PHONE = "phone";

    // Create Tables SQL
    private static final String CREATE_TABLE_PATIENTS =
            "CREATE TABLE " + TABLE_PATIENTS + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_DOCTOR_ID + " INTEGER NOT NULL, " +
                    KEY_FIRST_NAME + " TEXT NOT NULL, " +
                    KEY_LAST_NAME + " TEXT NOT NULL, " +
                    KEY_DATE_OF_BIRTH + " TEXT, " +
                    KEY_GENDER + " TEXT, " +
                    KEY_PHONE + " TEXT, " +
                    KEY_EMAIL + " TEXT, " +
                    KEY_ADDRESS + " TEXT, " +
                    KEY_BLOOD_GROUP + " TEXT, " +
                    KEY_ALLERGIES + " TEXT, " +
                    KEY_LAST_VISIT + " TEXT, " +
                    KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(" + KEY_DOCTOR_ID + ") REFERENCES " +
                    TABLE_USERS + "(" + KEY_ID + ") ON DELETE CASCADE" +
                    ")";

    private static final String CREATE_TABLE_CONSULTATIONS =
            "CREATE TABLE " + TABLE_CONSULTATIONS + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_PATIENT_ID + " INTEGER NOT NULL, " +
                    KEY_CONSULTATION_DATE + " TEXT NOT NULL, " +
                    KEY_DIAGNOSIS + " TEXT, " +
                    KEY_TREATMENT + " TEXT, " +
                    KEY_PRESCRIPTION + " TEXT, " +
                    KEY_NOTES + " TEXT, " +
                    KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(" + KEY_PATIENT_ID + ") REFERENCES " +
                    TABLE_PATIENTS + "(" + KEY_ID + ") ON DELETE CASCADE" +
                    ")";

    private static final String CREATE_TABLE_APPOINTMENTS =
            "CREATE TABLE " + TABLE_APPOINTMENTS + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_PATIENT_ID + " INTEGER NOT NULL, " +
                    KEY_DOCTOR_ID + " INTEGER NOT NULL, " +
                    KEY_APPOINTMENT_DATE + " TEXT NOT NULL, " +
                    KEY_APPOINTMENT_TIME + " TEXT NOT NULL, " +
                    KEY_REASON + " TEXT, " +
                    KEY_STATUS + " TEXT DEFAULT 'scheduled', " +
                    KEY_NOTES + " TEXT, " +
                    KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY(" + KEY_PATIENT_ID + ") REFERENCES " +
                    TABLE_PATIENTS + "(" + KEY_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY(" + KEY_DOCTOR_ID + ") REFERENCES " +
                    TABLE_USERS + "(" + KEY_ID + ") ON DELETE CASCADE" +
                    ")";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY_USER_FIRST_NAME + " TEXT NOT NULL, " +
                    KEY_USER_LAST_NAME + " TEXT NOT NULL, " +
                    KEY_USER_EMAIL + " TEXT UNIQUE NOT NULL, " +
                    KEY_USER_PASSWORD + " TEXT NOT NULL, " +
                    KEY_USER_ROLE + " TEXT NOT NULL, " +
                    KEY_USER_PHONE + " TEXT, " +
                    KEY_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON;");

        // Create tables
        db.execSQL(CREATE_TABLE_PATIENTS);
        db.execSQL(CREATE_TABLE_CONSULTATIONS);
        db.execSQL(CREATE_TABLE_APPOINTMENTS);
        db.execSQL(CREATE_TABLE_USERS);

        // Insert sample data for testing
        insertSampleUsers(db);
        insertSampleData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_APPOINTMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONSULTATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys=ON;");
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Insert sample patients (Tunisian names and phone numbers)
        // All sample patients belong to the sample doctor (id=1)
        db.execSQL("INSERT INTO " + TABLE_PATIENTS + " (" +
                KEY_DOCTOR_ID + ", " + KEY_FIRST_NAME + ", " + KEY_LAST_NAME + ", " + KEY_DATE_OF_BIRTH + ", " +
                KEY_GENDER + ", " + KEY_PHONE + ", " + KEY_EMAIL + ", " +
                KEY_BLOOD_GROUP + ", " + KEY_LAST_VISIT + ") VALUES " +
                "(1, 'Fatma', 'Trabelsi', '1990-05-15', 'Female', '+216 20 123 456', 'fatma.trabelsi@email.tn', 'A+', '2025-11-10')");

        db.execSQL("INSERT INTO " + TABLE_PATIENTS + " (" +
                KEY_DOCTOR_ID + ", " + KEY_FIRST_NAME + ", " + KEY_LAST_NAME + ", " + KEY_DATE_OF_BIRTH + ", " +
                KEY_GENDER + ", " + KEY_PHONE + ", " + KEY_EMAIL + ", " +
                KEY_BLOOD_GROUP + ", " + KEY_LAST_VISIT + ") VALUES " +
                "(1, 'Mohamed', 'Ben Ali', '1978-08-22', 'Male', '+216 55 987 654', 'mohamed.benali@email.tn', 'O+', '2025-11-12')");

        db.execSQL("INSERT INTO " + TABLE_PATIENTS + " (" +
                KEY_DOCTOR_ID + ", " + KEY_FIRST_NAME + ", " + KEY_LAST_NAME + ", " + KEY_DATE_OF_BIRTH + ", " +
                KEY_GENDER + ", " + KEY_PHONE + ", " + KEY_EMAIL + ", " +
                KEY_BLOOD_GROUP + ", " + KEY_LAST_VISIT + ") VALUES " +
                "(1, 'Amira', 'Jaziri', '1997-03-08', 'Female', '+216 98 765 432', 'amira.jaziri@email.tn', 'B+', '2025-11-08')");

        db.execSQL("INSERT INTO " + TABLE_PATIENTS + " (" +
                KEY_DOCTOR_ID + ", " + KEY_FIRST_NAME + ", " + KEY_LAST_NAME + ", " + KEY_DATE_OF_BIRTH + ", " +
                KEY_GENDER + ", " + KEY_PHONE + ", " + KEY_EMAIL + ", " +
                KEY_BLOOD_GROUP + ", " + KEY_LAST_VISIT + ") VALUES " +
                "(1, 'Ahmed', 'Gharbi', '1973-11-30', 'Male', '+216 22 333 444', 'ahmed.gharbi@email.tn', 'AB+', '2025-11-09')");

        // Insert sample appointments (all belong to doctor id=1)
        db.execSQL("INSERT INTO " + TABLE_APPOINTMENTS + " (" +
                KEY_PATIENT_ID + ", " + KEY_DOCTOR_ID + ", " + KEY_APPOINTMENT_DATE + ", " + KEY_APPOINTMENT_TIME + ", " +
                KEY_REASON + ", " + KEY_STATUS + ") VALUES " +
                "(1, 1, '2025-11-12', '09:00 AM', 'Consultation Générale', 'completed')");

        db.execSQL("INSERT INTO " + TABLE_APPOINTMENTS + " (" +
                KEY_PATIENT_ID + ", " + KEY_DOCTOR_ID + ", " + KEY_APPOINTMENT_DATE + ", " + KEY_APPOINTMENT_TIME + ", " +
                KEY_REASON + ", " + KEY_STATUS + ") VALUES " +
                "(2, 1, '2025-11-12', '10:30 AM', 'Suivi', 'in_progress')");

        db.execSQL("INSERT INTO " + TABLE_APPOINTMENTS + " (" +
                KEY_PATIENT_ID + ", " + KEY_DOCTOR_ID + ", " + KEY_APPOINTMENT_DATE + ", " + KEY_APPOINTMENT_TIME + ", " +
                KEY_REASON + ", " + KEY_STATUS + ") VALUES " +
                "(3, 1, '2025-11-12', '02:00 PM', 'Urgence', 'scheduled')");

        db.execSQL("INSERT INTO " + TABLE_APPOINTMENTS + " (" +
                KEY_PATIENT_ID + ", " + KEY_DOCTOR_ID + ", " + KEY_APPOINTMENT_DATE + ", " + KEY_APPOINTMENT_TIME + ", " +
                KEY_REASON + ", " + KEY_STATUS + ") VALUES " +
                "(4, 1, '2025-11-12', '03:30 PM', 'Vaccination', 'scheduled')");
    }

    private void insertSampleUsers(SQLiteDatabase db) {
        // Insert sample doctor account
        // Email: doctor@medimanager.tn | Password: doctor123
        db.execSQL("INSERT INTO " + TABLE_USERS + " (" +
                KEY_USER_FIRST_NAME + ", " + KEY_USER_LAST_NAME + ", " +
                KEY_USER_EMAIL + ", " + KEY_USER_PASSWORD + ", " +
                KEY_USER_ROLE + ", " + KEY_USER_PHONE + ") VALUES " +
                "('Amine', 'Ben Amor', 'doctor@medimanager.tn', 'doctor123', 'doctor', '+216 71 123 456')");

        // Insert sample patient account
        // Email: patient@medimanager.tn | Password: patient123
        db.execSQL("INSERT INTO " + TABLE_USERS + " (" +
                KEY_USER_FIRST_NAME + ", " + KEY_USER_LAST_NAME + ", " +
                KEY_USER_EMAIL + ", " + KEY_USER_PASSWORD + ", " +
                KEY_USER_ROLE + ", " + KEY_USER_PHONE + ") VALUES " +
                "('Sarra', 'Mejri', 'patient@medimanager.tn', 'patient123', 'patient', '+216 98 111 222')");
    }
}
