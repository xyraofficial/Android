package com.xyra.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfileActivity extends Activity {
    
    private static final String PREFS_NAME = "XyraAIProfile";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHOTO = "userPhoto";
    private static final String KEY_JOIN_DATE = "joinDate";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    
    private ImageButton btnBack;
    private ImageView ivProfileAvatar;
    private ImageView ivVerifiedBadge;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private LinearLayout accountInfoSection;
    private LinearLayout btnSignOut;
    private TextView tvUserId;
    private TextView tvUserEmailInfo;
    private TextView tvJoinDate;
    private TextView tvTotalChats;
    
    private SharedPreferences prefs;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        if (!isLoggedIn()) {
            redirectToLogin();
            return;
        }
        
        setContentView(R.layout.activity_profile);
        
        initViews();
        setupClickListeners();
        loadUserData();
        animateEntrance();
    }
    
    private boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void initViews() {
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        ivProfileAvatar = (ImageView) findViewById(R.id.ivProfileAvatar);
        ivVerifiedBadge = (ImageView) findViewById(R.id.ivVerifiedBadge);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvUserEmail = (TextView) findViewById(R.id.tvUserEmail);
        accountInfoSection = (LinearLayout) findViewById(R.id.accountInfoSection);
        btnSignOut = (LinearLayout) findViewById(R.id.btnSignOut);
        tvUserId = (TextView) findViewById(R.id.tvUserId);
        tvUserEmailInfo = (TextView) findViewById(R.id.tvUserEmailInfo);
        tvJoinDate = (TextView) findViewById(R.id.tvJoinDate);
        tvTotalChats = (TextView) findViewById(R.id.tvTotalChats);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateExit();
            }
        });
        
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonPress(v);
                confirmSignOut();
            }
        });
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
    
    private void animateEntrance() {
        View profileCard = findViewById(R.id.profileCardContainer);
        if (profileCard != null) {
            profileCard.setAlpha(0f);
            profileCard.setTranslationY(50f);
            profileCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(100)
                .start();
        }
        
        if (accountInfoSection.getVisibility() == View.VISIBLE) {
            accountInfoSection.setAlpha(0f);
            accountInfoSection.setTranslationY(30f);
            accountInfoSection.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(200)
                .start();
        }
    }
    
    private void animateExit() {
        View profileCard = findViewById(R.id.profileCardContainer);
        if (profileCard != null) {
            profileCard.animate()
                .alpha(0f)
                .translationY(-30f)
                .setDuration(200)
                .start();
        }
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        }, 200);
    }
    
    private void loadUserData() {
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        
        if (isLoggedIn) {
            showLoggedInState();
        }
        
        loadChatCount();
    }
    
    private void showLoggedInState() {
        String userName = prefs.getString(KEY_USER_NAME, "User");
        String userEmail = prefs.getString(KEY_USER_EMAIL, "");
        String userId = prefs.getString(KEY_USER_ID, "");
        String joinDate = prefs.getString(KEY_JOIN_DATE, "");
        String photoUrl = prefs.getString(KEY_USER_PHOTO, "");
        
        tvUserName.setText(userName);
        tvUserEmail.setText(userEmail);
        tvUserId.setText(userId.length() > 20 ? userId.substring(0, 20) + "..." : userId);
        tvUserEmailInfo.setText(userEmail);
        tvJoinDate.setText(joinDate);
        
        accountInfoSection.setVisibility(View.VISIBLE);
        btnSignOut.setVisibility(View.VISIBLE);
        ivVerifiedBadge.setVisibility(View.VISIBLE);
        
        if (!photoUrl.isEmpty()) {
            loadProfileImage(photoUrl);
        }
    }
    
    private void loadChatCount() {
        SharedPreferences chatPrefs = getSharedPreferences("XyraAIChatHistory", Context.MODE_PRIVATE);
        int chatCount = chatPrefs.getInt("chatCount", 0);
        tvTotalChats.setText(String.valueOf(chatCount));
    }
    
    private void loadProfileImage(final String photoUrl) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(photoUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    final Bitmap bitmap = BitmapFactory.decodeStream(input);
                    
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (bitmap != null) {
                                ivProfileAvatar.setImageBitmap(bitmap);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    private void confirmSignOut() {
        new AlertDialog.Builder(this)
            .setTitle("Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari akun?")
            .setPositiveButton("Keluar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    signOut();
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }
    
    private void signOut() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        Toast.makeText(this, "Berhasil keluar", Toast.LENGTH_SHORT).show();
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            }
        }, 500);
    }
    
    @Override
    public void onBackPressed() {
        animateExit();
    }
}
