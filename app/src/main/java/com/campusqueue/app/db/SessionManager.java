package com.campusqueue.app.db;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "CampusQueueSession";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_REG_NO = "user_reg_no";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_MUST_CHANGE_PASSWORD = "must_change_password";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createSession(long userId, String email, String name, String role, String regNo, String token, boolean mustChangePassword) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putLong(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_USER_REG_NO, regNo);
        editor.putString(KEY_AUTH_TOKEN, token != null ? token : "mock-jwt-token-" + userId);
        editor.putBoolean(KEY_MUST_CHANGE_PASSWORD, mustChangePassword);
        editor.apply();
    }

    public void createSession(long userId, String email, String name, String role, String regNo, String token) {
        createSession(userId, email, name, role, regNo, token, false);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public long getUserId() {
        return pref.getLong(KEY_USER_ID, -1);
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "User");
    }

    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, "Student");
    }

    public String getUserRegNo() {
        return pref.getString(KEY_USER_REG_NO, "");
    }

    public String getAuthToken() {
        return pref.getString(KEY_AUTH_TOKEN, "");
    }

    public boolean mustChangePassword() {
        return pref.getBoolean(KEY_MUST_CHANGE_PASSWORD, false);
    }

    public void setMustChangePassword(boolean mustChange) {
        editor.putBoolean(KEY_MUST_CHANGE_PASSWORD, mustChange);
        editor.apply();
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
