package com.xyra.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONObject;

public class DashboardActivity extends Activity {
    private TextView tvTotalUsers, tvActiveUsers, tvBannedUsers, tvApiRequests;
    private ImageButton btnRefresh, btnLogout;
    private LinearLayout btnUserManagement, btnAIConfig, btnSupabaseSettings, btnActivityLogs;
    private AdminConfig config;
    private AdminApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        config = AdminConfig.getInstance(this);
        apiService = new AdminApiService(config.getApiBaseUrl(), config.getAccessToken());
        
        initViews();
        setupClickListeners();
        loadDashboardStats();
    }
    
    private void initViews() {
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvActiveUsers = findViewById(R.id.tvActiveUsers);
        tvBannedUsers = findViewById(R.id.tvBannedUsers);
        tvApiRequests = findViewById(R.id.tvApiRequests);
        btnRefresh = findViewById(R.id.btnRefresh);
        btnLogout = findViewById(R.id.btnLogout);
        btnUserManagement = findViewById(R.id.btnUserManagement);
        btnAIConfig = findViewById(R.id.btnAIConfig);
        btnSupabaseSettings = findViewById(R.id.btnSupabaseSettings);
        btnActivityLogs = findViewById(R.id.btnActivityLogs);
    }
    
    private void setupClickListeners() {
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDashboardStats();
            }
        });
        
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutConfirmation();
            }
        });
        
        btnUserManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, UserManagementActivity.class));
            }
        });
        
        btnAIConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, AIConfigActivity.class));
            }
        });
        
        btnSupabaseSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, SupabaseSettingsActivity.class));
            }
        });
        
        btnActivityLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DashboardActivity.this, ActivityLogsActivity.class));
            }
        });
    }
    
    private void loadDashboardStats() {
        apiService.getDashboardStats(new AdminApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    int totalUsers = response.optInt("total_users", 0);
                    int activeUsers = response.optInt("active_users", 0);
                    int bannedUsers = response.optInt("banned_users", 0);
                    int apiRequests = response.optInt("api_requests", 0);
                    
                    tvTotalUsers.setText(String.valueOf(totalUsers));
                    tvActiveUsers.setText(String.valueOf(activeUsers));
                    tvBannedUsers.setText(String.valueOf(bannedUsers));
                    tvApiRequests.setText(formatNumber(apiRequests));
                } catch (Exception e) {
                    Toast.makeText(DashboardActivity.this, "Failed to parse stats", Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(DashboardActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private String formatNumber(int num) {
        if (num >= 1000000) {
            return String.format("%.1fM", num / 1000000.0);
        } else if (num >= 1000) {
            return String.format("%.1fK", num / 1000.0);
        }
        return String.valueOf(num);
    }
    
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    logout();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void logout() {
        config.clearSession();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }
}
