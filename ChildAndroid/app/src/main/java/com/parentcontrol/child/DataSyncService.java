package com.parentcontrol.child;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataSyncService extends Service {
    
    private static final String TAG = "DataSyncService";
    private static final String CHANNEL_ID = "parent_control_channel";
    private static final int NOTIFICATION_ID = 1001;
    private static final long SYNC_INTERVAL = 15 * 60 * 1000;
    
    public static boolean isRunning = false;
    
    private Handler handler;
    private ExecutorService executor;
    private LocationManager locationManager;
    private String apiUrl;
    private String apiToken;
    private Location lastLocation;
    
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            lastLocation = location;
        }
        
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        
        @Override
        public void onProviderEnabled(String provider) {}
        
        @Override
        public void onProviderDisabled(String provider) {}
    };
    
    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        
        apiUrl = Config.getApiUrl(this);
        apiToken = Config.getApiToken(this);
        
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isRunning = true;
        
        Notification notification;
        if (Build.VERSION.SDK_INT >= 26) {
            notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Parent Control Active")
                .setContentText("Monitoring service is running")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build();
        } else {
            notification = new Notification.Builder(this)
                .setContentTitle("Parent Control Active")
                .setContentText("Monitoring service is running")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .build();
        }
        
        startForeground(NOTIFICATION_ID, notification);
        
        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                60000,
                10,
                locationListener
            );
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied", e);
        }
        
        startSyncLoop();
        
        return START_STICKY;
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Parent Control Service",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Background monitoring service");
            
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private void startSyncLoop() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    if (apiToken == null || apiToken.isEmpty()) {
                        registerDevice();
                    } else {
                        performSync();
                    }
                    handler.postDelayed(this, SYNC_INTERVAL);
                }
            }
        });
    }
    
    private void registerDevice() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(apiUrl + "/api/auth/register");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);
                    
                    JSONObject data = new JSONObject();
                    data.put("device_name", Build.MODEL);
                    data.put("model", Build.MODEL);
                    data.put("android_version", Build.VERSION.RELEASE);
                    
                    byte[] input = data.toString().getBytes("UTF-8");
                    OutputStream os = conn.getOutputStream();
                    os.write(input, 0, input.length);
                    os.close();
                    
                    int responseCode = conn.getResponseCode();
                    if (responseCode == 201) {
                        java.io.BufferedReader reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        
                        JSONObject result = new JSONObject(response.toString());
                        String token = result.getString("api_token");
                        String deviceId = result.getString("device_id");
                        
                        Config.setApiToken(DataSyncService.this, token);
                        Config.setDeviceId(DataSyncService.this, deviceId);
                        apiToken = token;
                        
                        Log.d(TAG, "Device registered successfully. Token: " + token.substring(0, 8) + "...");
                    } else {
                        Log.e(TAG, "Registration failed with code: " + responseCode);
                    }
                    
                    conn.disconnect();
                } catch (Exception e) {
                    Log.e(TAG, "Registration failed", e);
                }
            }
        });
    }
    
    private void performSync() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    syncLocation();
                    syncContacts();
                    syncSms();
                    syncGallery();
                    Log.d(TAG, "Sync completed successfully");
                } catch (Exception e) {
                    Log.e(TAG, "Sync failed", e);
                }
            }
        });
    }
    
    private void syncLocation() {
        try {
            if (lastLocation != null) {
                JSONObject data = new JSONObject();
                data.put("latitude", lastLocation.getLatitude());
                data.put("longitude", lastLocation.getLongitude());
                data.put("accuracy", lastLocation.getAccuracy());
                
                sendToApi("/api/data/location", data);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to sync location", e);
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
                Uri.parse("content://sms"),
                new String[]{"address", "body", "date", "type"},
                null, null, 
                "date DESC LIMIT 100"
            );
            
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    JSONObject sms = new JSONObject();
                    sms.put("address", cursor.getString(0));
                    sms.put("body", cursor.getString(1));
                    sms.put("date", cursor.getLong(2));
                    sms.put("type", cursor.getInt(3) == 2 ? "sent" : "received");
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
            
            byte[] input = data.toString().getBytes("UTF-8");
            OutputStream os = conn.getOutputStream();
            os.write(input, 0, input.length);
            os.close();
            
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
        try {
            locationManager.removeUpdates(locationListener);
        } catch (Exception e) {
            Log.e(TAG, "Error removing location updates", e);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
