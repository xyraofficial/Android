package com.xyra.ai;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigLoader {
    private static final String TAG = "ConfigLoader";
    private static boolean isLoaded = false;
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler handler = new Handler(Looper.getMainLooper());

    public interface ConfigCallback {
        void onConfigLoaded(boolean success);
    }

    public static boolean isConfigLoaded() {
        return isLoaded;
    }

    public static void loadConfig(final ConfigCallback callback) {
        if (isLoaded) {
            if (callback != null) {
                callback.onConfigLoaded(true);
            }
            return;
        }

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(Config.CONFIG_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);

                    int responseCode = conn.getResponseCode();
                    
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

                        JSONObject json = new JSONObject(response.toString());
                        
                        if (json.has("groq_api")) {
                            Config.GROQ_API_KEY = json.getString("groq_api");
                            Log.d(TAG, "GROQ API key loaded successfully");
                        }
                        
                        isLoaded = true;
                        
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) {
                                    callback.onConfigLoaded(true);
                                }
                            }
                        });
                    } else {
                        Log.e(TAG, "Failed to load config: HTTP " + responseCode);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) {
                                    callback.onConfigLoaded(false);
                                }
                            }
                        });
                    }
                    
                    conn.disconnect();
                    
                } catch (final Exception e) {
                    Log.e(TAG, "Error loading config", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onConfigLoaded(false);
                            }
                        }
                    });
                }
            }
        });
    }
}
