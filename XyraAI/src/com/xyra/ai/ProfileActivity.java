package com.xyra.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.json.JSONObject;

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
    private LinearLayout loginSection;
    private LinearLayout btnGoogleSignIn;
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
        setContentView(R.layout.activity_profile);
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        initViews();
        setupClickListeners();
        loadUserData();
        animateEntrance();
    }
    
    private void initViews() {
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        ivProfileAvatar = (ImageView) findViewById(R.id.ivProfileAvatar);
        ivVerifiedBadge = (ImageView) findViewById(R.id.ivVerifiedBadge);
        tvUserName = (TextView) findViewById(R.id.tvUserName);
        tvUserEmail = (TextView) findViewById(R.id.tvUserEmail);
        loginSection = (LinearLayout) findViewById(R.id.loginSection);
        btnGoogleSignIn = (LinearLayout) findViewById(R.id.btnGoogleSignIn);
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
        
        btnGoogleSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateButtonPress(v);
                startGoogleSignIn();
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
        
        if (loginSection.getVisibility() == View.VISIBLE) {
            loginSection.setAlpha(0f);
            loginSection.setTranslationY(30f);
            loginSection.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(200)
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
        } else {
            showLoggedOutState();
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
        
        loginSection.setVisibility(View.GONE);
        accountInfoSection.setVisibility(View.VISIBLE);
        btnSignOut.setVisibility(View.VISIBLE);
        ivVerifiedBadge.setVisibility(View.VISIBLE);
        
        if (!photoUrl.isEmpty()) {
            loadProfileImage(photoUrl);
        }
    }
    
    private void showLoggedOutState() {
        tvUserName.setText("Guest User");
        tvUserEmail.setText("Belum login");
        
        loginSection.setVisibility(View.VISIBLE);
        accountInfoSection.setVisibility(View.GONE);
        btnSignOut.setVisibility(View.GONE);
        ivVerifiedBadge.setVisibility(View.GONE);
        
        ivProfileAvatar.setImageResource(R.drawable.ic_user_placeholder);
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
    
    private void startGoogleSignIn() {
        showInfoDialog(
            "Login dengan Google",
            "Untuk menggunakan Firebase Authentication dengan Google Sign-In di AIDE:\n\n" +
            "1. Buka Firebase Console\n" +
            "2. Buat project baru\n" +
            "3. Aktifkan Google Sign-In\n" +
            "4. Download google-services.json\n" +
            "5. Tambahkan konfigurasi ke project\n\n" +
            "Fitur ini memerlukan setup Firebase terlebih dahulu.\n\n" +
            "Untuk demo, klik OK untuk simulasi login."
        );
    }
    
    private void showInfoDialog(String title, String message) {
        new AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    simulateLogin();
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }
    
    private void simulateLogin() {
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID")).format(new Date());
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, "xyra_user_" + System.currentTimeMillis());
        editor.putString(KEY_USER_NAME, "XyraAI User");
        editor.putString(KEY_USER_EMAIL, "user@example.com");
        editor.putString(KEY_JOIN_DATE, currentDate);
        editor.putString(KEY_USER_PHOTO, "");
        editor.apply();
        
        Toast.makeText(this, "Login berhasil!", Toast.LENGTH_SHORT).show();
        
        animateLoginSuccess();
    }
    
    private void animateLoginSuccess() {
        loginSection.animate()
            .alpha(0f)
            .translationY(-30f)
            .setDuration(300)
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    loginSection.setVisibility(View.GONE);
                    showLoggedInState();
                    
                    accountInfoSection.setAlpha(0f);
                    accountInfoSection.setTranslationY(30f);
                    accountInfoSection.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .start();
                    
                    btnSignOut.setAlpha(0f);
                    btnSignOut.animate()
                        .alpha(1f)
                        .setDuration(400)
                        .setStartDelay(200)
                        .start();
                }
            })
            .start();
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
        
        animateSignOut();
    }
    
    private void animateSignOut() {
        accountInfoSection.animate()
            .alpha(0f)
            .translationY(-30f)
            .setDuration(300)
            .start();
        
        btnSignOut.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction(new Runnable() {
                @Override
                public void run() {
                    btnSignOut.setVisibility(View.GONE);
                    accountInfoSection.setVisibility(View.GONE);
                    showLoggedOutState();
                    
                    loginSection.setAlpha(0f);
                    loginSection.setTranslationY(30f);
                    loginSection.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(400)
                        .start();
                }
            })
            .start();
    }
    
    @Override
    public void onBackPressed() {
        animateExit();
    }
}
