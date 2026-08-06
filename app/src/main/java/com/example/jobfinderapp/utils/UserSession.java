package com.example.jobfinderapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.jobfinderapp.models.User;

/** Centralized storage for the local login session. SQLite remains the source of truth. */
public final class UserSession {

    private static final String PREF_NAME = "UserSession";
    private static final String KEY_LOGIN_STATUS = "LOGIN_STATUS";
    private static final String KEY_USER_ID = "USER_ID";
    private static final String KEY_USER_EMAIL = "USER_EMAIL";
    private static final String KEY_USER_NAME = "USER_NAME";
    private static final String KEY_USER_ROLE = "USER_ROLE";
    private static final String KEY_USER_STATUS = "USER_STATUS";

    private final SharedPreferences preferences;

    public UserSession(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void save(User user) {
        preferences.edit()
                .putInt(KEY_USER_ID, user.getId())
                .putString(KEY_USER_EMAIL, user.getEmail())
                .putString(KEY_USER_NAME, user.getFullname())
                .putString(KEY_USER_ROLE, user.getRole())
                .putString(KEY_USER_STATUS, user.getStatus())
                .putBoolean(KEY_LOGIN_STATUS, true)
                .apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_LOGIN_STATUS, false) && getUserId() > 0;
    }

    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
