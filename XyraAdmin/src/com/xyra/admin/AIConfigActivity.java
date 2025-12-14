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

public class AIConfigActivity extends Activity {
    private ImageButton btnBack;
    private EditText etApiKey, etModel, etMaxTokens, etRateLimit;
    private Switch switchStreaming, switchVoice;
    private Button btnSave;
    private ProgressBar progressBar;
    private AdminConfig config;
    private AdminApiService apiService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_config);
        
        config = AdminConfig.getInstance(this);
        apiService = new AdminApiService(config.getApiBaseUrl(), config.getAccessToken());
        
        initViews();
        setupClickListeners();
        loadConfig();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etApiKey = findViewById(R.id.etApiKey);
        etModel = findViewById(R.id.etModel);
        etMaxTokens = findViewById(R.id.etMaxTokens);
        etRateLimit = findViewById(R.id.etRateLimit);
        switchStreaming = findViewById(R.id.switchStreaming);
        switchVoice = findViewById(R.id.switchVoice);
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
                saveConfig();
            }
        });
    }
    
    private void loadConfig() {
        progressBar.setVisibility(View.VISIBLE);
        
        apiService.getAIConfig(new AdminApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                
                String apiKey = response.optString("api_key", "");
                String model = response.optString("model", "llama-3.3-70b-versatile");
                int maxTokens = response.optInt("max_tokens", 8192);
                int rateLimit = response.optInt("rate_limit", 60);
                boolean streaming = response.optBoolean("streaming", true);
                boolean voice = response.optBoolean("voice_enabled", true);
                
                if (!apiKey.isEmpty() && apiKey.length() > 8) {
                    apiKey = apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
                }
                
                etApiKey.setHint(apiKey.isEmpty() ? "Enter API Key" : apiKey);
                etModel.setText(model);
                etMaxTokens.setText(String.valueOf(maxTokens));
                etRateLimit.setText(String.valueOf(rateLimit));
                switchStreaming.setChecked(streaming);
                switchVoice.setChecked(voice);
            }
            
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AIConfigActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void saveConfig() {
        String apiKey = etApiKey.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String maxTokensStr = etMaxTokens.getText().toString().trim();
        String rateLimitStr = etRateLimit.getText().toString().trim();
        
        if (model.isEmpty()) {
            Toast.makeText(this, "Model is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int maxTokens = 8192;
        int rateLimit = 60;
        
        try {
            if (!maxTokensStr.isEmpty()) {
                maxTokens = Integer.parseInt(maxTokensStr);
            }
            if (!rateLimitStr.isEmpty()) {
                rateLimit = Integer.parseInt(rateLimitStr);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }
        
        JSONObject configData = new JSONObject();
        try {
            if (!apiKey.isEmpty()) {
                configData.put("api_key", apiKey);
            }
            configData.put("model", model);
            configData.put("max_tokens", maxTokens);
            configData.put("rate_limit", rateLimit);
            configData.put("streaming", switchStreaming.isChecked());
            configData.put("voice_enabled", switchVoice.isChecked());
        } catch (Exception e) {
            Toast.makeText(this, "Failed to create config", Toast.LENGTH_SHORT).show();
            return;
        }
        
        progressBar.setVisibility(View.VISIBLE);
        btnSave.setEnabled(false);
        
        apiService.updateAIConfig(configData, new AdminApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(AIConfigActivity.this, "Configuration saved", Toast.LENGTH_SHORT).show();
                etApiKey.setText("");
                loadConfig();
            }
            
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
                Toast.makeText(AIConfigActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
