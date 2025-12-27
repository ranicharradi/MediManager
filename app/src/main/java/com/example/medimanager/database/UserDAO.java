package com.example.medimanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.medimanager.models.User;
import com.example.medimanager.utils.PasswordUtils;

public class UserDAO {

    private final DatabaseHelper dbHelper;
    private static final String TAG = "UserDAO";

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
        values.put(DatabaseHelper.KEY_USER_PASSWORD, PasswordUtils.hashPassword(user.getPassword()));
        values.put(DatabaseHelper.KEY_USER_ROLE, user.getRole());
        values.put(DatabaseHelper.KEY_USER_PHONE, user.getPhone());

        try {
            return db.insert(DatabaseHelper.TABLE_USERS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "Error registering user", e);
            return -1;
        }
    }

    /**
     * Authenticate user by email and password
     */
    public User authenticateUser(String email, String password, String role) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.KEY_USER_EMAIL + " = ? AND " +
                DatabaseHelper.KEY_USER_ROLE + " = ?";
        String[] selectionArgs = {email, role};

        User user = null;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                String storedPassword = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.KEY_USER_PASSWORD));
                if (PasswordUtils.verifyPassword(password, storedPassword)) {
                    user = cursorToUser(cursor);
                    if (!PasswordUtils.isHashed(storedPassword)) {
                        updateUserPassword(user.getId(), PasswordUtils.hashPassword(password));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error authenticating user", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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

        Cursor cursor = null;
        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    new String[]{DatabaseHelper.KEY_ID},
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            return cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking email registration", e);
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Get user by ID
     */
    public User getUserById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.KEY_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        User user = null;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user by id", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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

        User user = null;
        Cursor cursor = null;
        try {
            cursor = db.query(
                    DatabaseHelper.TABLE_USERS,
                    null,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                user = cursorToUser(cursor);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user by email", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
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

        try {
            return db.update(DatabaseHelper.TABLE_USERS, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error updating user", e);
            return 0;
        }
    }

    private void updateUserPassword(long userId, String hashedPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.KEY_USER_PASSWORD, hashedPassword);

        String whereClause = DatabaseHelper.KEY_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId)};
        try {
            db.update(DatabaseHelper.TABLE_USERS, values, whereClause, whereArgs);
        } catch (Exception e) {
            Log.e(TAG, "Error updating password", e);
        }
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
