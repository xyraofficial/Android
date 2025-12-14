package com.parentcontrol.child;

import android.content.Context;
import android.content.SharedPreferences;

public class Config {
    
    private static final String PREFS_NAME = "ParentControlPrefs";
    private static final String KEY_API_URL = "api_url";
    private static final String KEY_API_TOKEN = "api_token";
    private static final String KEY_DEVICE_ID = "device_id";
    
    private static final String DEFAULT_API_URL = "https://your-api-url.com";
    
    public static String getApiUrl(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_API_URL, DEFAULT_API_URL);
    }
    
    public static void setApiUrl(Context context, String url) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_API_URL, url).apply();
    }
    
    public static String getApiToken(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_API_TOKEN, "");
    }
    
    public static void setApiToken(Context context, String token) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_API_TOKEN, token).apply();
    }
    
    public static String getDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_DEVICE_ID, "");
    }
    
    public static void setDeviceId(Context context, String deviceId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
    }
}
