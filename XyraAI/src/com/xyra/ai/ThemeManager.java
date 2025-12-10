package com.xyra.ai;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class ThemeManager {
    
    private static final String PREFS_NAME = "xyra_settings";
    private static final String KEY_THEME = "theme";
    
    public static final String THEME_SYSTEM = "System Default";
    public static final String THEME_DARK = "Dark";
    public static final String THEME_LIGHT = "Light";
    
    public static class ThemeColors {
        public int background;
        public int backgroundSecondary;
        public int surface;
        public int textPrimary;
        public int textSecondary;
        public int userBubble;
        public int aiBubble;
        public int inputBackground;
        public int divider;
        public int drawerBackground;
        public int headerBackground;
        public int colorPrimary;
        public int colorAccent;
        public int statusBar;
        public boolean isDark;
    }
    
    public static String getCurrentTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_THEME, THEME_SYSTEM);
    }
    
    public static boolean isDarkMode(Context context) {
        String theme = getCurrentTheme(context);
        
        if (THEME_DARK.equals(theme)) {
            return true;
        } else if (THEME_LIGHT.equals(theme)) {
            return false;
        } else {
            int nightModeFlags = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            return nightModeFlags == Configuration.UI_MODE_NIGHT_YES;
        }
    }
    
    public static ThemeColors getThemeColors(Context context) {
        ThemeColors colors = new ThemeColors();
        boolean isDark = isDarkMode(context);
        colors.isDark = isDark;
        
        if (isDark) {
            colors.background = Color.parseColor("#212121");
            colors.backgroundSecondary = Color.parseColor("#2F2F2F");
            colors.surface = Color.parseColor("#343541");
            colors.textPrimary = Color.parseColor("#ECECF1");
            colors.textSecondary = Color.parseColor("#8E8EA0");
            colors.userBubble = Color.parseColor("#343541");
            colors.aiBubble = Color.parseColor("#444654");
            colors.inputBackground = Color.parseColor("#40414F");
            colors.divider = Color.parseColor("#4D4D4F");
            colors.drawerBackground = Color.parseColor("#202123");
            colors.headerBackground = Color.parseColor("#343541");
            colors.colorPrimary = Color.parseColor("#10A37F");
            colors.colorAccent = Color.parseColor("#10A37F");
            colors.statusBar = Color.parseColor("#343541");
        } else {
            colors.background = Color.parseColor("#FFFFFF");
            colors.backgroundSecondary = Color.parseColor("#F7F7F8");
            colors.surface = Color.parseColor("#ECECEC");
            colors.textPrimary = Color.parseColor("#202123");
            colors.textSecondary = Color.parseColor("#6E6E80");
            colors.userBubble = Color.parseColor("#10A37F");
            colors.aiBubble = Color.parseColor("#F7F7F8");
            colors.inputBackground = Color.parseColor("#FFFFFF");
            colors.divider = Color.parseColor("#E5E5E5");
            colors.drawerBackground = Color.parseColor("#F7F7F8");
            colors.headerBackground = Color.parseColor("#FFFFFF");
            colors.colorPrimary = Color.parseColor("#10A37F");
            colors.colorAccent = Color.parseColor("#10A37F");
            colors.statusBar = Color.parseColor("#FFFFFF");
        }
        
        return colors;
    }
    
    public static void applyTheme(Activity activity) {
        ThemeColors colors = getThemeColors(activity);
        
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(colors.statusBar);
        window.setNavigationBarColor(colors.background);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            if (!colors.isDark) {
                decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | 
                    View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                );
            } else {
                decorView.setSystemUiVisibility(0);
            }
        }
    }
}
