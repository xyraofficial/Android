package com.xyra.ai;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserManager {
    
    private static final String PREFS_NAME = "xyra_user";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_LOGIN_TYPE = "login_type";
    private static final String KEY_JOIN_DATE = "join_date";
    private static final String KEY_SKIP_LOGIN = "skip_login";
    
    public static final String LOGIN_TYPE_GOOGLE = "Google";
    public static final String LOGIN_TYPE_EMAIL = "Email";
    
    private SharedPreferences prefs;
    private Context context;
    
    public UserManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public boolean hasSkippedLogin() {
        return prefs.getBoolean(KEY_SKIP_LOGIN, false);
    }
    
    public void setSkipLogin(boolean skip) {
        prefs.edit().putBoolean(KEY_SKIP_LOGIN, skip).apply();
    }
    
    public void login(String name, String email, String loginType) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String joinDate = sdf.format(new Date());
        
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_LOGIN_TYPE, loginType)
            .putString(KEY_JOIN_DATE, joinDate)
            .putBoolean(KEY_SKIP_LOGIN, false)
            .apply();
    }
    
    public void logout() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean(KEY_SKIP_LOGIN, false)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_LOGIN_TYPE)
            .apply();
    }
    
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "Pengguna");
    }
    
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }
    
    public String getLoginType() {
        return prefs.getString(KEY_LOGIN_TYPE, "");
    }
    
    public String getJoinDate() {
        return prefs.getString(KEY_JOIN_DATE, "Hari ini");
    }
    
    public String getUserInitial() {
        String name = getUserName();
        if (name != null && name.length() > 0) {
            return name.substring(0, 1).toUpperCase();
        }
        return "U";
    }
    
    public boolean shouldShowLogin() {
        return !isLoggedIn() && !hasSkippedLogin();
    }
}
