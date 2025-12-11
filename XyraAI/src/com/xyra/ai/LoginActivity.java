package com.xyra.ai;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends Activity {
    
    private static final String TAG = "LoginActivity";
    
    private static final String PREFS_NAME = "XyraAIProfile";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_PHOTO = "userPhoto";
    private static final String KEY_JOIN_DATE = "joinDate";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    
    // Firebase REST API Key - get from Firebase Console > Project Settings > Web API Key
    private static final String FIREBASE_API_KEY = "YOUR_FIREBASE_API_KEY_HERE";
    private static final String FIREBASE_AUTH_URL = "https://identitytoolkit.googleapis.com/v1/accounts:";
    
    private ImageView ivLogo;
    private ImageView ivLogoGlow;
    private TextView tvAppName;
    private TextView tvTagline;
    private EditText etEmail;
    private EditText etPassword;
    private EditText etDisplayName;
    private Button btnLogin;
    private Button btnRegister;
    private TextView tvSwitchMode;
    private ProgressBar progressBar;
    private LinearLayout loginContainer;
    private LinearLayout formContainer;
    private View gradientOverlay;
    
    private SharedPreferences prefs;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    
    private boolean isRegisterMode = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        // Check if user is already logged in
        if (prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            navigateToMain();
            return;
        }
        
        setContentView(R.layout.activity_login);
        
        initViews();
        setupClickListeners();
        startEntranceAnimations();
    }
    
    private void initViews() {
        ivLogo = (ImageView) findViewById(R.id.ivLogo);
        ivLogoGlow = (ImageView) findViewById(R.id.ivLogoGlow);
        tvAppName = (TextView) findViewById(R.id.tvAppName);
        tvTagline = (TextView) findViewById(R.id.tvTagline);
        loginContainer = (LinearLayout) findViewById(R.id.loginContainer);
        gradientOverlay = findViewById(R.id.gradientOverlay);
        
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etDisplayName = (EditText) findViewById(R.id.etDisplayName);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        tvSwitchMode = (TextView) findViewById(R.id.tvSwitchMode);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        formContainer = (LinearLayout) findViewById(R.id.formContainer);
        
        // Initially hide register fields
        if (etDisplayName != null) {
            etDisplayName.setVisibility(View.GONE);
        }
        if (btnRegister != null) {
            btnRegister.setVisibility(View.GONE);
        }
    }
    
    private void setupClickListeners() {
        if (btnLogin != null) {
            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateButtonPress(v);
                    if (isRegisterMode) {
                        performRegister();
                    } else {
                        performLogin();
                    }
                }
            });
        }
        
        if (tvSwitchMode != null) {
            tvSwitchMode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleMode();
                }
            });
        }
    }
    
    private void toggleMode() {
        isRegisterMode = !isRegisterMode;
        
        if (isRegisterMode) {
            if (etDisplayName != null) etDisplayName.setVisibility(View.VISIBLE);
            if (btnLogin != null) btnLogin.setText("Daftar");
            if (tvSwitchMode != null) tvSwitchMode.setText("Sudah punya akun? Masuk");
        } else {
            if (etDisplayName != null) etDisplayName.setVisibility(View.GONE);
            if (btnLogin != null) btnLogin.setText("Masuk");
            if (tvSwitchMode != null) tvSwitchMode.setText("Belum punya akun? Daftar");
        }
    }
    
    private void performLogin() {
        final String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        final String password = etPassword != null ? etPassword.getText().toString().trim() : "";
        
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        executor.execute(new Runnable() {
            @Override
            public void run() {
                firebaseSignIn(email, password);
            }
        });
    }
    
    private void performRegister() {
        final String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        final String password = etPassword != null ? etPassword.getText().toString().trim() : "";
        final String displayName = etDisplayName != null ? etDisplayName.getText().toString().trim() : "";
        
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (password.length() < 6) {
            Toast.makeText(this, "Password minimal 6 karakter", Toast.LENGTH_SHORT).show();
            return;
        }
        
        showLoading(true);
        
        executor.execute(new Runnable() {
            @Override
            public void run() {
                firebaseSignUp(email, password, displayName);
            }
        });
    }
    
    private void firebaseSignIn(String email, String password) {
        try {
            URL url = new URL(FIREBASE_AUTH_URL + "signInWithPassword?key=" + FIREBASE_API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("returnSecureToken", true);
            
            OutputStream os = conn.getOutputStream();
            os.write(requestBody.toString().getBytes("UTF-8"));
            os.close();
            
            int responseCode = conn.getResponseCode();
            
            BufferedReader reader;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            final JSONObject jsonResponse = new JSONObject(response.toString());
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                final String userId = jsonResponse.optString("localId", "");
                final String userEmail = jsonResponse.optString("email", email);
                final String displayName = jsonResponse.optString("displayName", "XyraAI User");
                
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onLoginSuccess(userId, userEmail, displayName);
                    }
                });
            } else {
                final String errorMessage = parseFirebaseError(jsonResponse);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
            
            conn.disconnect();
            
        } catch (final Exception e) {
            Log.e(TAG, "Login error", e);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Koneksi error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    
    private void firebaseSignUp(String email, String password, final String displayName) {
        try {
            URL url = new URL(FIREBASE_AUTH_URL + "signUp?key=" + FIREBASE_API_KEY);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("returnSecureToken", true);
            
            OutputStream os = conn.getOutputStream();
            os.write(requestBody.toString().getBytes("UTF-8"));
            os.close();
            
            int responseCode = conn.getResponseCode();
            
            BufferedReader reader;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            final JSONObject jsonResponse = new JSONObject(response.toString());
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                final String userId = jsonResponse.optString("localId", "");
                final String userEmail = jsonResponse.optString("email", email);
                final String name = TextUtils.isEmpty(displayName) ? "XyraAI User" : displayName;
                
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onLoginSuccess(userId, userEmail, name);
                    }
                });
            } else {
                final String errorMessage = parseFirebaseError(jsonResponse);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        showLoading(false);
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
            }
            
            conn.disconnect();
            
        } catch (final Exception e) {
            Log.e(TAG, "Register error", e);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, "Koneksi error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
    }
    
    private String parseFirebaseError(JSONObject response) {
        try {
            JSONObject error = response.optJSONObject("error");
            if (error != null) {
                String message = error.optString("message", "Terjadi kesalahan");
                
                switch (message) {
                    case "EMAIL_NOT_FOUND":
                        return "Email tidak ditemukan";
                    case "INVALID_PASSWORD":
                        return "Password salah";
                    case "USER_DISABLED":
                        return "Akun dinonaktifkan";
                    case "EMAIL_EXISTS":
                        return "Email sudah terdaftar";
                    case "WEAK_PASSWORD":
                        return "Password terlalu lemah (min 6 karakter)";
                    case "INVALID_EMAIL":
                        return "Format email tidak valid";
                    case "TOO_MANY_ATTEMPTS_TRY_LATER":
                        return "Terlalu banyak percobaan. Coba lagi nanti";
                    default:
                        if (message.contains("INVALID_LOGIN_CREDENTIALS")) {
                            return "Email atau password salah";
                        }
                        return "Error: " + message;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing firebase error", e);
        }
        return "Terjadi kesalahan";
    }
    
    private void onLoginSuccess(String userId, String email, String displayName) {
        String currentDate = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID")).format(new Date());
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, displayName);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_JOIN_DATE, currentDate);
        editor.putString(KEY_USER_PHOTO, "");
        editor.apply();
        
        animateLoginSuccess();
    }
    
    private void startEntranceAnimations() {
        if (ivLogo != null) {
            ivLogo.setAlpha(0f);
            ivLogo.setScaleX(0.3f);
            ivLogo.setScaleY(0.3f);
        }
        
        if (ivLogoGlow != null) {
            ivLogoGlow.setAlpha(0f);
            ivLogoGlow.setScaleX(0.3f);
            ivLogoGlow.setScaleY(0.3f);
        }
        
        if (tvAppName != null) {
            tvAppName.setAlpha(0f);
            tvAppName.setTranslationY(30f);
        }
        
        if (tvTagline != null) {
            tvTagline.setAlpha(0f);
            tvTagline.setTranslationY(20f);
        }
        
        if (loginContainer != null) {
            loginContainer.setAlpha(0f);
            loginContainer.setTranslationY(50f);
        }
        
        if (ivLogo != null) {
            ivLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator(1.2f))
                .setStartDelay(200)
                .start();
        }
        
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
        
        if (tvAppName != null) {
            tvAppName.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(500)
                .start();
        }
        
        if (tvTagline != null) {
            tvTagline.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(650)
                .start();
        }
        
        if (loginContainer != null) {
            loginContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(700)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setStartDelay(800)
                .start();
        }
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
    
    private void showLoading(boolean show) {
        if (show) {
            if (btnLogin != null) btnLogin.setEnabled(false);
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            if (btnLogin != null) btnLogin.animate().alpha(0.7f).setDuration(200).start();
        } else {
            if (btnLogin != null) btnLogin.setEnabled(true);
            if (progressBar != null) progressBar.setVisibility(View.GONE);
            if (btnLogin != null) btnLogin.animate().alpha(1f).setDuration(200).start();
        }
    }
    
    private void animateLoginSuccess() {
        if (progressBar != null) progressBar.setVisibility(View.GONE);
        
        if (ivLogo != null) {
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
        }
        
        if (ivLogoGlow != null) {
            ivLogoGlow.animate()
                .scaleX(2f)
                .scaleY(2f)
                .alpha(0f)
                .setDuration(500)
                .start();
        }
        
        if (tvAppName != null) {
            tvAppName.animate()
                .alpha(0f)
                .translationY(-30f)
                .setDuration(300)
                .setStartDelay(100)
                .start();
        }
        
        if (tvTagline != null) {
            tvTagline.animate()
                .alpha(0f)
                .translationY(-20f)
                .setDuration(300)
                .setStartDelay(150)
                .start();
        }
        
        if (loginContainer != null) {
            loginContainer.animate()
                .alpha(0f)
                .translationY(30f)
                .setDuration(300)
                .setStartDelay(100)
                .start();
        }
        
        if (gradientOverlay != null) {
            gradientOverlay.animate()
                .alpha(0f)
                .setDuration(400)
                .start();
        }
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                String userName = prefs.getString(KEY_USER_NAME, "");
                String welcomeMsg = "Selamat datang";
                if (!TextUtils.isEmpty(userName)) {
                    welcomeMsg += ", " + userName + "!";
                } else {
                    welcomeMsg += " di XyraAI!";
                }
                Toast.makeText(LoginActivity.this, welcomeMsg, Toast.LENGTH_SHORT).show();
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
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
