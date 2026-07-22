package com.example.jobfinderapp.database;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {
    private static final String PREF_NAME = "UserSessionPref";
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    private static final String KEY_USER_ID = "UserId";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    public UserSession(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Lưu phiên đăng nhập
    public void createSession(int userId) {
        editor.putBoolean(IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    // Kiểm tra đã đăng nhập chưa
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGGED_IN, false);
    }

    // Đăng xuất
    public void logout() {
        editor.clear();
        editor.apply();
    }
}