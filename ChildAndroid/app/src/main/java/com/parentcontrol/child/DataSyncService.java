package com.parentcontrol.child;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataSyncService extends Service {
    
    private static final String TAG = "DataSyncService";
    private static final String CHANNEL_ID = "parent_control_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long SYNC_INTERVAL = 15 * 60 * 1000; // 15 minutes
    
    public static boolean isRunning = false;
    
    private Handler handler;
    private ExecutorService executor;
    private FusedLocationProviderClient locationClient;
    private String apiUrl;
    private String apiToken;
    
    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();
        locationClient = LocationServices.getFusedLocationProviderClient(this);
        
        apiUrl = Config.getApiUrl(this);
        apiToken = Config.getApiToken(this);
        
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Parent Control Active")
            .setContentText("Monitoring service is running")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
        
        startForeground(NOTIFICATION_ID, notification);
        
        startSyncLoop();
        
        return START_STICKY;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Parent Control Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background monitoring service");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private void startSyncLoop() {
        Runnable syncTask = new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    performSync();
                    handler.postDelayed(this, SYNC_INTERVAL);
                }
            }
        };
        
        handler.post(syncTask);
    }
    
    private void performSync() {
        executor.execute(() -> {
            try {
                syncLocation();
                syncContacts();
                syncSms();
                syncGallery();
                Log.d(TAG, "Sync completed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Sync failed", e);
            }
        });
    }
    
    private void syncLocation() {
        try {
            locationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    executor.execute(() -> {
                        try {
                            JSONObject data = new JSONObject();
                            data.put("latitude", location.getLatitude());
                            data.put("longitude", location.getLongitude());
                            data.put("accuracy", location.getAccuracy());
                            
                            sendToApi("/api/data/location", data);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to sync location", e);
                        }
                    });
                }
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied", e);
        }
    }
    
    private void syncContacts() {
        try {
            JSONArray contacts = new JSONArray();
            ContentResolver resolver = getContentResolver();
            
            Cursor cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                },
                null, null, null
            );
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject contact = new JSONObject();
                    contact.put("name", cursor.getString(0));
                    contact.put("phone", cursor.getString(1));
                    contacts.put(contact);
                }
                cursor.close();
            }
            
            JSONObject data = new JSONObject();
            data.put("contacts", contacts);
            sendToApi("/api/data/contacts", data);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to sync contacts", e);
        }
    }
    
    private void syncSms() {
        try {
            JSONArray smsList = new JSONArray();
            ContentResolver resolver = getContentResolver();
            
            Cursor cursor = resolver.query(
                Telephony.Sms.CONTENT_URI,
                new String[]{
                    Telephony.Sms.ADDRESS,
                    Telephony.Sms.BODY,
                    Telephony.Sms.DATE,
                    Telephony.Sms.TYPE
                },
                null, null, 
                Telephony.Sms.DATE + " DESC LIMIT 100"
            );
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject sms = new JSONObject();
                    sms.put("address", cursor.getString(0));
                    sms.put("body", cursor.getString(1));
                    sms.put("date", cursor.getLong(2));
                    sms.put("type", cursor.getInt(3) == Telephony.Sms.MESSAGE_TYPE_SENT ? "sent" : "received");
                    smsList.put(sms);
                }
                cursor.close();
            }
            
            JSONObject data = new JSONObject();
            data.put("sms", smsList);
            sendToApi("/api/data/sms", data);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to sync SMS", e);
        }
    }
    
    private void syncGallery() {
        try {
            JSONArray gallery = new JSONArray();
            ContentResolver resolver = getContentResolver();
            
            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.MIME_TYPE
            };
            
            Cursor cursor = resolver.query(uri, projection, null, null,
                MediaStore.Images.Media.DATE_TAKEN + " DESC LIMIT 50");
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject item = new JSONObject();
                    item.put("filename", cursor.getString(0));
                    item.put("size", cursor.getLong(1));
                    item.put("date_taken", cursor.getLong(2));
                    item.put("type", cursor.getString(3));
                    gallery.put(item);
                }
                cursor.close();
            }
            
            JSONObject data = new JSONObject();
            data.put("gallery", gallery);
            sendToApi("/api/data/gallery", data);
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to sync gallery", e);
        }
    }
    
    private void sendToApi(String endpoint, JSONObject data) {
        try {
            URL url = new URL(apiUrl + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("X-API-Token", apiToken);
            conn.setDoOutput(true);
            
            byte[] input = data.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            Log.d(TAG, "API response for " + endpoint + ": " + responseCode);
            
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "API request failed: " + endpoint, e);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        handler.removeCallbacksAndMessages(null);
        executor.shutdown();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
