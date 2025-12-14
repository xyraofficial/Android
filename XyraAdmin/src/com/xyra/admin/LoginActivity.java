package com.xyra.admin;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

public class LoginActivity extends Activity {
    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvError;
    private AdminConfig config;
    private AdminApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        config = AdminConfig.getInstance(this);
        apiService = new AdminApiService(config.getApiBaseUrl(), null);
        
        if (config.isLoggedIn()) {
            navigateToDashboard();
            return;
        }
        
        initViews();
        setupClickListeners();
    }
    
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvError = findViewById(R.id.tvError);
    }
    
    private void setupClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });
    }
    
    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        if (email.isEmpty()) {
            showError("Please enter admin email");
            return;
        }
        
        if (password.isEmpty()) {
            showError("Please enter password");
            return;
        }
        
        setLoading(true);
        hideError();
        
        apiService.login(email, password, new AdminApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                setLoading(false);
                try {
                    String accessToken = response.getString("access_token");
                    String refreshToken = response.getString("refresh_token");
                    long expiresIn = response.optLong("expires_in", 3600) * 1000;
                    long expiryTime = System.currentTimeMillis() + expiresIn;
                    
                    config.saveTokens(accessToken, refreshToken, expiryTime);
                    config.setAdminEmail(email);
                    apiService.setAccessToken(accessToken);
                    
                    navigateToDashboard();
                } catch (Exception e) {
                    showError("Invalid response from server");
                }
            }
            
            @Override
            public void onError(String error) {
                setLoading(false);
                showError(error);
            }
        });
    }
    
    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
    }
    
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
    
    private void hideError() {
        tvError.setVisibility(View.GONE);
    }
    
    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
