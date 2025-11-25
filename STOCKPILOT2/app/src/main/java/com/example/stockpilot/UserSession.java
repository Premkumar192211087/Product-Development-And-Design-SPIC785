package com.example.stockpilot;
import android.content.Context;
import android.content.SharedPreferences;

public class UserSession {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_STORE_ID = "storeId";
    private static final String KEY_STORE_NAME = "storeName";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLE = "role";
    private static final String KEY_TOKEN = "token";

    private static UserSession instance;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public UserSession(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static synchronized UserSession getInstance(Context context) {
        if (instance == null) {
            instance = new UserSession(context.getApplicationContext());
        }
        return instance;
    }

    // Save user login data - Updated to include userId and token
    public void createUserSession(String userId, String storeId, String storeName, String username, String role, String token) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_STORE_ID, storeId);
        editor.putString(KEY_STORE_NAME, storeName);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    // Overloaded method for backward compatibility
    public void createUserSession(String userId, String storeId, String storeName, String username, String role) {
        createUserSession(userId, storeId, storeName, username, role, ""); // Empty token
    }

    // Overloaded method for backward compatibility
    public void createUserSession(String storeId, String storeName, String username, String role) {
        createUserSession("1", storeId, storeName, username, role, ""); // Default user ID, empty token
    }

    // Check if user is logged in
    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // Get user details
    public String getUserId() {
        return preferences.getString(KEY_USER_ID, "1");
    }

    public String getStoreId() {
        return preferences.getString(KEY_STORE_ID, "");
    }

    public String getStoreName() {
        return preferences.getString(KEY_STORE_NAME, "");
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, "");
    }

    public String getRole() {
        return preferences.getString(KEY_ROLE, "");
    }
    
    public String getToken() {
        return preferences.getString(KEY_TOKEN, "");
    }

    public void setUserData(String userId, String username, String role, String storeId, String storeName) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_STORE_ID, storeId);
        editor.putString(KEY_STORE_NAME, storeName);
        editor.apply();
    }

    // Clear user session (logout)
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
