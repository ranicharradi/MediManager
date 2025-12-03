package com.example.medimanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.medimanager.models.User;

public class UserDAO {

    private final DatabaseHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Register a new user
     */
    public long registerUser(User user) {
        // Check if email already exists
        if (isEmailRegistered(user.getEmail())) {
            return -1;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_USER_FIRST_NAME, user.getFirstName());
        values.put(DatabaseHelper.KEY_USER_LAST_NAME, user.getLastName());
        values.put(DatabaseHelper.KEY_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.KEY_USER_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.KEY_USER_ROLE, user.getRole());
        values.put(DatabaseHelper.KEY_USER_PHONE, user.getPhone());

        long id = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        return id;
    }

    /**
     * Authenticate user by email and password
     */
    public User authenticateUser(String email, String password, String role) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.KEY_USER_EMAIL + " = ? AND " +
                DatabaseHelper.KEY_USER_PASSWORD + " = ? AND " +
                DatabaseHelper.KEY_USER_ROLE + " = ?";
        String[] selectionArgs = {email, password, role};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        return user;
    }

    /**
     * Check if email is already registered
     */
    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.KEY_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.KEY_ID},
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) {
            cursor.close();
        }

        return exists;
    }

    /**
     * Get user by ID
     */
    public User getUserById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.KEY_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        return user;
    }

    /**
     * Get user by email
     */
    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.KEY_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        return user;
    }

    /**
     * Update user information
     */
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_USER_FIRST_NAME, user.getFirstName());
        values.put(DatabaseHelper.KEY_USER_LAST_NAME, user.getLastName());
        values.put(DatabaseHelper.KEY_USER_PHONE, user.getPhone());

        String whereClause = DatabaseHelper.KEY_ID + " = ?";
        String[] whereArgs = {String.valueOf(user.getId())};

        return db.update(DatabaseHelper.TABLE_USERS, values, whereClause, whereArgs);
    }

    /**
     * Convert cursor to User object
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_ID)));
        user.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_FIRST_NAME)));
        user.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_LAST_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_PASSWORD)));
        user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_ROLE)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_PHONE)));
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_CREATED_AT)));
        return user;
    }
}
