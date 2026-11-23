package com.example.todologin;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "personal_todo.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "email TEXT NOT NULL UNIQUE," +
                "password_hash TEXT NOT NULL," +
                "salt TEXT NOT NULL)");
        db.execSQL("CREATE TABLE tasks (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "notes TEXT," +
                "completed INTEGER NOT NULL DEFAULT 0," +
                "created_at INTEGER NOT NULL," +
                "FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE INDEX idx_tasks_user_id ON tasks(user_id)");
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Add explicit migration steps here when the schema changes.
    }

    public long registerUser(String name, String email, String password)
            throws GeneralSecurityException {
        String salt = PasswordHasher.newSalt();
        String hash = PasswordHasher.hash(password, salt);
        ContentValues values = new ContentValues();
        values.put("name", name.trim());
        values.put("email", email.trim().toLowerCase(Locale.ROOT));
        values.put("password_hash", hash);
        values.put("salt", salt);
        return getWritableDatabase().insertOrThrow("users", null, values);
    }

    public long authenticate(String email, String password) throws GeneralSecurityException {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query("users",
                new String[]{"id", "password_hash", "salt"},
                "email = ?",
                new String[]{email.trim().toLowerCase(Locale.ROOT)},
                null, null, null)) {
            if (!cursor.moveToFirst()) return -1L;
            String savedHash = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"));
            String salt = cursor.getString(cursor.getColumnIndexOrThrow("salt"));
            return PasswordHasher.verify(password, salt, savedHash)
                    ? cursor.getLong(cursor.getColumnIndexOrThrow("id")) : -1L;
        }
    }

    public String getUserName(long userId) {
        try (Cursor cursor = getReadableDatabase().query("users",
                new String[]{"name"}, "id = ?", new String[]{String.valueOf(userId)},
                null, null, null)) {
            return cursor.moveToFirst() ? cursor.getString(0) : "User";
        }
    }

    public long addTask(long userId, String title, String notes) {
        ContentValues values = new ContentValues();
        values.put("user_id", userId);
        values.put("title", title.trim());
        values.put("notes", notes == null ? "" : notes.trim());
        values.put("completed", 0);
        values.put("created_at", System.currentTimeMillis());
        return getWritableDatabase().insert("tasks", null, values);
    }

    public List<TaskItem> getTasks(long userId) {
        List<TaskItem> tasks = new ArrayList<>();
        try (Cursor cursor = getReadableDatabase().query("tasks",
                new String[]{"id", "title", "notes", "completed"},
                "user_id = ?", new String[]{String.valueOf(userId)},
                null, null, "completed ASC, created_at DESC")) {
            while (cursor.moveToNext()) {
                tasks.add(new TaskItem(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getInt(3) == 1));
            }
        }
        return tasks;
    }

    public boolean setTaskCompleted(long taskId, long userId, boolean completed) {
        ContentValues values = new ContentValues();
        values.put("completed", completed ? 1 : 0);
        return getWritableDatabase().update("tasks", values,
                "id = ? AND user_id = ?",
                new String[]{String.valueOf(taskId), String.valueOf(userId)}) > 0;
    }

    public boolean deleteTask(long taskId, long userId) {
        return getWritableDatabase().delete("tasks",
                "id = ? AND user_id = ?",
                new String[]{String.valueOf(taskId), String.valueOf(userId)}) > 0;
    }
}
