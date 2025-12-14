package com.parentcontrol.child;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                Manifest.permission.READ_EXTERNAL_STORAGE
            };
        }
    }
    
    private TextView statusText;
    private Button startServiceBtn;
    private Button stopServiceBtn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        statusText = findViewById(R.id.statusText);
        startServiceBtn = findViewById(R.id.startServiceBtn);
        stopServiceBtn = findViewById(R.id.stopServiceBtn);
        
        startServiceBtn.setOnClickListener(v -> showConsentDialog());
        stopServiceBtn.setOnClickListener(v -> stopDataService());
        
        updateStatus();
    }
    
    private void showConsentDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Parent Control Consent")
            .setMessage(
                "This app will collect and transmit the following data:\n\n" +
                "• Location data\n" +
                "• Contact list\n" +
                "• SMS messages\n" +
                "• Gallery metadata\n\n" +
                "This data will be sent securely to the parent monitoring server.\n\n" +
                "By proceeding, you confirm that:\n" +
                "1. You are the device owner or have consent\n" +
                "2. You understand this is for parental monitoring\n" +
                "3. You agree to the data collection\n\n" +
                "Do you consent to proceed?"
            )
            .setPositiveButton("I Consent", (dialog, which) -> checkAndRequestPermissions())
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show();
    }
    
    private void checkAndRequestPermissions() {
        String[] permissions = getRequiredPermissions();
        boolean allGranted = true;
        
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }
        
        if (allGranted) {
            startDataService();
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                startDataService();
            } else {
                Toast.makeText(this, "Permissions required for monitoring", 
                    Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void startDataService() {
        Intent intent = new Intent(this, DataSyncService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        Toast.makeText(this, "Monitoring service started", Toast.LENGTH_SHORT).show();
        updateStatus();
    }
    
    private void stopDataService() {
        Intent intent = new Intent(this, DataSyncService.class);
        stopService(intent);
        Toast.makeText(this, "Monitoring service stopped", Toast.LENGTH_SHORT).show();
        updateStatus();
    }
    
    private void updateStatus() {
        if (DataSyncService.isRunning) {
            statusText.setText("Status: Running");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            statusText.setText("Status: Stopped");
            statusText.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }
}
