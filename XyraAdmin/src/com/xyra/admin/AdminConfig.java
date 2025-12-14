package com.xyra.admin;

import android.content.Context;
import android.content.SharedPreferences;

public class AdminConfig {
    private static final String PREFS_NAME = "XyraAdminPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_ADMIN_EMAIL = "admin_email";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";
    private static final String KEY_API_BASE_URL = "api_base_url";
    
    private static final String DEFAULT_API_URL = "https://xyra-admin-api.vercel.app";
    
    private SharedPreferences prefs;
    private static AdminConfig instance;
    
    private AdminConfig(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public static synchronized AdminConfig getInstance(Context context) {
        if (instance == null) {
            instance = new AdminConfig(context);
        }
        return instance;
    }
    
    public void saveTokens(String accessToken, String refreshToken, long expiryTime) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_TOKEN_EXPIRY, expiryTime)
            .apply();
    }
    
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }
    
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }
    
    public long getTokenExpiry() {
        return prefs.getLong(KEY_TOKEN_EXPIRY, 0);
    }
    
    public boolean isTokenExpired() {
        long expiry = getTokenExpiry();
        return expiry == 0 || System.currentTimeMillis() > expiry;
    }
    
    public void setAdminEmail(String email) {
        prefs.edit().putString(KEY_ADMIN_EMAIL, email).apply();
    }
    
    public String getAdminEmail() {
        return prefs.getString(KEY_ADMIN_EMAIL, null);
    }
    
    public void setApiBaseUrl(String url) {
        prefs.edit().putString(KEY_API_BASE_URL, url).apply();
    }
    
    public String getApiBaseUrl() {
        return prefs.getString(KEY_API_BASE_URL, DEFAULT_API_URL);
    }
    
    public void clearSession() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .remove(KEY_ADMIN_EMAIL)
            .apply();
    }
    
    public boolean isLoggedIn() {
        return getAccessToken() != null && !isTokenExpired();
    }
}
