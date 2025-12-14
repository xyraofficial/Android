package com.xyra.admin;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;
import org.json.JSONObject;

public class SupabaseSettingsActivity extends Activity {
    private ImageButton btnBack;
    private EditText etSupabaseUrl, etAnonKey, etServiceKey;
    private Switch switchAuth, switchStorage, switchRealtime;
    private Button btnSave;
    private ProgressBar progressBar;
    private AdminConfig config;
    private AdminApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supabase_settings);
        
        config = AdminConfig.getInstance(this);
        apiService = new AdminApiService(config.getApiBaseUrl(), config.getAccessToken());
        
        initViews();
        setupClickListeners();
        loadSettings();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etSupabaseUrl = findViewById(R.id.etSupabaseUrl);
        etAnonKey = findViewById(R.id.etAnonKey);
        etServiceKey = findViewById(R.id.etServiceKey);
        switchAuth = findViewById(R.id.switchAuth);
        switchStorage = findViewById(R.id.switchStorage);
        switchRealtime = findViewById(R.id.switchRealtime);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }
    
    private void loadSettings() {
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.getSupabaseSettings(new AdminApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                
                String url = response.optString("supabase_url", "");
                String anonKey = response.optString("anon_key", "");
                String serviceKey = response.optString("service_key", "");
                boolean auth = response.optBoolean("auth_enabled", true);
                boolean storage = response.optBoolean("storage_enabled", true);
                boolean realtime = response.optBoolean("realtime_enabled", false);
                
                etSupabaseUrl.setText(url);
                etAnonKey.setHint(maskKey(anonKey));
                etServiceKey.setHint(maskKey(serviceKey));
                switchAuth.setChecked(auth);
                switchStorage.setChecked(storage);
                switchRealtime.setChecked(realtime);
            }
            
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SupabaseSettingsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private String maskKey(String key) {
        if (key == null || key.isEmpty()) {
            return "Enter key";
        }
        if (key.length() > 8) {
            return key.substring(0, 4) + "..." + key.substring(key.length() - 4);
        }
        return "****";
    }
    
    private void saveSettings() {
        String url = etSupabaseUrl.getText().toString().trim();
        String anonKey = etAnonKey.getText().toString().trim();
        String serviceKey = etServiceKey.getText().toString().trim();
        
        if (url.isEmpty()) {
            Toast.makeText(this, "Supabase URL is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        JSONObject settings = new JSONObject();
        try {
            settings.put("supabase_url", url);
            if (!anonKey.isEmpty()) {
                settings.put("anon_key", anonKey);
            }
            if (!serviceKey.isEmpty()) {
                settings.put("service_key", serviceKey);
            }
            settings.put("auth_enabled", switchAuth.isChecked());
            settings.put("storage_enabled", switchStorage.isChecked());
            settings.put("realtime_enabled", switchRealtime.isChecked());
        } catch (Exception e) {
            Toast.makeText(this, "Failed to create settings", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        
        apiService.updateSupabaseSettings(settings, new AdminApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(SupabaseSettingsActivity.this, "Settings saved", Toast.LENGTH_SHORT).show();
                etAnonKey.setText("");
                etServiceKey.setText("");
                loadSettings();
            }
            
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(SupabaseSettingsActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
