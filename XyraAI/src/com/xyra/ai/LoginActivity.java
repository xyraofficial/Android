package com.xyra.ai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginActivity extends Activity {
    
    private static final String PREFS_NAME = "XyraAIProfile";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHOTO = "userPhoto";
    private static final String KEY_JOIN_DATE = "joinDate";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    
    private ImageView ivLogo;
    private ImageView ivLogoGlow;
    private TextView tvAppName;
    private TextView tvTagline;
    private LinearLayout btnGoogleSignIn;
    private ProgressBar progressBar;
    private LinearLayout loginContainer;
    private View gradientOverlay;
    
    private SharedPreferences prefs;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        if (isLoggedIn()) {
            navigateToMain();
            return;
        }
        
        setContentView(R.layout.activity_login);
        
        initViews();
        setupClickListeners();
        startEntranceAnimations();
    }
    
    private boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    private void initViews() {
        ivLogo = (ImageView) findViewById(R.id.ivLogo);
        ivLogoGlow = (ImageView) findViewById(R.id.ivLogoGlow);
        tvAppName = (TextView) findViewById(R.id.tvAppName);
        tvTagline = (TextView) findViewById(R.id.tvTagline);
        btnGoogleSignIn = (LinearLayout) findViewById(R.id.btnGoogleSignIn);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        loginContainer = (LinearLayout) findViewById(R.id.loginContainer);
        gradientOverlay = findViewById(R.id.gradientOverlay);
    }
    
    private void setupClickListeners() {
        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonPress(v);
                startGoogleSignIn();
            }
        });
    }
    
    private void startEntranceAnimations() {
        ivLogo.setAlpha(0f);
        ivLogo.setScaleX(0.3f);
        ivLogo.setScaleY(0.3f);
        
        if (ivLogoGlow != null) {
            ivLogoGlow.setAlpha(0f);
            ivLogoGlow.setScaleX(0.3f);
            ivLogoGlow.setScaleY(0.3f);
        }
        
        tvAppName.setAlpha(0f);
        tvAppName.setTranslationY(30f);
        
        tvTagline.setAlpha(0f);
        tvTagline.setTranslationY(20f);
        
        loginContainer.setAlpha(0f);
        loginContainer.setTranslationY(50f);
        
        ivLogo.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(800)
            .setInterpolator(new OvershootInterpolator(1.2f))
            .setStartDelay(200)
            .start();
        
        if (ivLogoGlow != null) {
            ivLogoGlow.animate()
                .alpha(0.6f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(400)
                .start();
            
            startGlowPulseAnimation();
        }
        
        tvAppName.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setStartDelay(500)
            .start();
        
        tvTagline.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setStartDelay(650)
            .start();
        
        loginContainer.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(700)
            .setInterpolator(new AccelerateDecelerateInterpolator())
            .setStartDelay(800)
            .start();
    }
    
    private void startGlowPulseAnimation() {
        if (ivLogoGlow == null) return;
        
        final Runnable pulseRunnable = new Runnable() {
            boolean expanding = true;
            
            @Override
            public void run() {
                if (isFinishing()) return;
                
                float targetScale = expanding ? 1.4f : 1.1f;
                float targetAlpha = expanding ? 0.3f : 0.6f;
                
                ivLogoGlow.animate()
                    .scaleX(targetScale)
                    .scaleY(targetScale)
                    .alpha(targetAlpha)
                    .setDuration(2000)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .withEndAction(this)
                    .start();
                
                expanding = !expanding;
            }
        };
        
        handler.postDelayed(pulseRunnable, 1500);
    }
    
    private void animateButtonPress(final View v) {
        ScaleAnimation scaleDown = new ScaleAnimation(
            1f, 0.95f, 1f, 0.95f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f);
        scaleDown.setDuration(100);
        
        ScaleAnimation scaleUp = new ScaleAnimation(
            0.95f, 1f, 0.95f, 1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f);
        scaleUp.setDuration(100);
        scaleUp.setStartOffset(100);
        
        AnimationSet animSet = new AnimationSet(true);
        animSet.addAnimation(scaleDown);
        animSet.addAnimation(scaleUp);
        v.startAnimation(animSet);
    }
    
    private void startGoogleSignIn() {
        btnGoogleSignIn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        
        btnGoogleSignIn.animate()
            .alpha(0.7f)
            .setDuration(200)
            .start();
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                performLogin();
            }
        }, 1500);
    }
    
    private void performLogin() {
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID")).format(new Date());
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, "xyra_user_" + System.currentTimeMillis());
        editor.putString(KEY_USER_NAME, "XyraAI User");
        editor.putString(KEY_USER_EMAIL, "user@gmail.com");
        editor.putString(KEY_JOIN_DATE, currentDate);
        editor.putString(KEY_USER_PHOTO, "");
        editor.apply();
        
        animateLoginSuccess();
    }
    
    private void animateLoginSuccess() {
        progressBar.setVisibility(View.GONE);
        
        ivLogo.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(300)
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    ivLogo.animate()
                        .scaleX(0f)
                        .scaleY(0f)
                        .alpha(0f)
                        .setDuration(400)
                        .start();
                }
            })
            .start();
        
        if (ivLogoGlow != null) {
            ivLogoGlow.animate()
                .scaleX(2f)
                .scaleY(2f)
                .alpha(0f)
                .setDuration(500)
                .start();
        }
        
        tvAppName.animate()
            .alpha(0f)
            .translationY(-30f)
            .setDuration(300)
            .setStartDelay(100)
            .start();
        
        tvTagline.animate()
            .alpha(0f)
            .translationY(-20f)
            .setDuration(300)
            .setStartDelay(150)
            .start();
        
        loginContainer.animate()
            .alpha(0f)
            .translationY(30f)
            .setDuration(300)
            .setStartDelay(100)
            .start();
        
        if (gradientOverlay != null) {
            gradientOverlay.animate()
                .alpha(0f)
                .setDuration(400)
                .start();
        }
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoginActivity.this, "Selamat datang di XyraAI!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            }
        }, 600);
    }
    
    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
