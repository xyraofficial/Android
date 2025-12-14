package com.xyra.admin;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ActivityLogsActivity extends Activity {
    private ImageButton btnBack, btnRefresh;
    private ListView listLogs;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private AdminConfig config;
    private AdminApiService apiService;
    private ArrayList<JSONObject> logList;
    private LogAdapter logAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logs);
        
        config = AdminConfig.getInstance(this);
        apiService = new AdminApiService(config.getApiBaseUrl(), config.getAccessToken());
        logList = new ArrayList<>();
        
        initViews();
        setupClickListeners();
        loadLogs();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);
        listLogs = findViewById(R.id.listLogs);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        logAdapter = new LogAdapter();
        listLogs.setAdapter(logAdapter);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadLogs();
            }
        });
    }
    
    private void loadLogs() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        
        apiService.getActivityLogs(1, 100, new AdminApiService.ApiArrayCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                progressBar.setVisibility(View.GONE);
                logList.clear();
                
                try {
                    for (int i = 0; i < response.length(); i++) {
                        logList.add(response.getJSONObject(i));
                    }
                } catch (Exception e) {
                    Toast.makeText(ActivityLogsActivity.this, "Failed to parse logs", Toast.LENGTH_SHORT).show();
                }
                
                logAdapter.notifyDataSetChanged();
                tvEmpty.setVisibility(logList.isEmpty() ? View.VISIBLE : View.GONE);
            }
            
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ActivityLogsActivity.this, error, Toast.LENGTH_SHORT).show();
                tvEmpty.setVisibility(logList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }
    
    private class LogAdapter extends BaseAdapter {
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        
        @Override
        public int getCount() {
            return logList.size();
        }
        
        @Override
        public Object getItem(int position) {
            return logList.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ActivityLogsActivity.this)
                    .inflate(R.layout.item_log, parent, false);
            }
            
            JSONObject log = logList.get(position);
            
            TextView tvAction = convertView.findViewById(R.id.tvAction);
            TextView tvDetails = convertView.findViewById(R.id.tvDetails);
            TextView tvTimestamp = convertView.findViewById(R.id.tvTimestamp);
            
            String action = log.optString("action", "Unknown");
            String details = log.optString("details", "");
            String user = log.optString("admin_email", "");
            long timestamp = log.optLong("timestamp", 0);
            
            tvAction.setText(action);
            tvDetails.setText(user + (details.isEmpty() ? "" : " - " + details));
            
            if (timestamp > 0) {
                tvTimestamp.setText(dateFormat.format(new Date(timestamp)));
            } else {
                tvTimestamp.setText("");
            }
            
            return convertView;
        }
    }
}
