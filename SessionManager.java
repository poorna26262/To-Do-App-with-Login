package com.example.todologin;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS = "todo_session";
    private static final String KEY_USER_ID = "user_id";
    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void createSession(long userId) {
        preferences.edit().putLong(KEY_USER_ID, userId).apply();
    }

    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1L);
    }

    public boolean isLoggedIn() {
        return getUserId() > 0;
    }

    public void clearSession() {
        preferences.edit().clear().apply();
    }
}
