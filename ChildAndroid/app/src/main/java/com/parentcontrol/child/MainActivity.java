package com.parentcontrol.child;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private TextView statusText;
    private Button startServiceBtn;
    private Button stopServiceBtn;
    
    private String[] getRequiredPermissions() {
        if (Build.VERSION.SDK_INT >= 33) {
            return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.READ_SMS,
                "android.permission.READ_MEDIA_IMAGES",
                "android.permission.POST_NOTIFICATIONS"
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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        statusText = (TextView) findViewById(R.id.statusText);
        startServiceBtn = (Button) findViewById(R.id.startServiceBtn);
        stopServiceBtn = (Button) findViewById(R.id.stopServiceBtn);
        
        startServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConsentDialog();
            }
        });
        
        stopServiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDataService();
            }
        });
        
        updateStatus();
    }
    
    private void showConsentDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Parent Control Consent")
            .setMessage(
                "This app will collect and transmit the following data:\n\n" +
                "- Location data\n" +
                "- Contact list\n" +
                "- SMS messages\n" +
                "- Gallery metadata\n\n" +
                "This data will be sent securely to the parent monitoring server.\n\n" +
                "By proceeding, you confirm that:\n" +
                "1. You are the device owner or have consent\n" +
                "2. You understand this is for parental monitoring\n" +
                "3. You agree to the data collection\n\n" +
                "Do you consent to proceed?"
            )
            .setPositiveButton("I Consent", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    checkAndRequestPermissions();
                }
            })
            .setNegativeButton("Cancel", null)
            .setCancelable(false)
            .show();
    }
    
    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] permissions = getRequiredPermissions();
            boolean allGranted = true;
            
            for (int i = 0; i < permissions.length; i++) {
                if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                startDataService();
            } else {
                requestPermissions(permissions, PERMISSION_REQUEST_CODE);
            }
        } else {
            startDataService();
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            
            if (allGranted) {
                startDataService();
            } else {
                Toast.makeText(this, "Permissions required for monitoring", Toast.LENGTH_LONG).show();
            }
        }
    }
    
    private void startDataService() {
        Intent intent = new Intent(this, DataSyncService.class);
        if (Build.VERSION.SDK_INT >= 26) {
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
            statusText.setTextColor(0xFF00AA00);
        } else {
            statusText.setText("Status: Stopped");
            statusText.setTextColor(0xFFAA0000);
        }
    }
}
