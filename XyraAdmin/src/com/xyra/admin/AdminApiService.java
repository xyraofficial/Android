package com.xyra.admin;

import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AdminApiService {
    private String baseUrl;
    private String accessToken;
    private ExecutorService executor;
    private Handler mainHandler;
    
    public interface ApiCallback {
        void onSuccess(JSONObject response);
        void onError(String error);
    }
    
    public interface ApiArrayCallback {
        void onSuccess(JSONArray response);
        void onError(String error);
    }
    
    public AdminApiService(String baseUrl, String accessToken) {
        this.baseUrl = baseUrl;
        this.accessToken = accessToken;
        this.executor = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void setAccessToken(String token) {
        this.accessToken = token;
    }
    
    public void login(String email, String password, ApiCallback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("email", email);
            body.put("password", password);
        } catch (Exception e) {
            callback.onError("Failed to create request");
            return;
        }
        post("/api/admin/login", body, false, callback);
    }
    
    public void refreshToken(String refreshToken, ApiCallback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("refresh_token", refreshToken);
        } catch (Exception e) {
            callback.onError("Failed to create request");
            return;
        }
        post("/api/admin/refresh", body, false, callback);
    }
    
    public void getDashboardStats(ApiCallback callback) {
        get("/api/admin/dashboard/stats", callback);
    }
    
    public void getUsers(int page, int limit, String search, ApiArrayCallback callback) {
        String query = "?page=" + page + "&limit=" + limit;
        if (search != null && !search.isEmpty()) {
            query += "&search=" + search;
        }
        getArray("/api/admin/users" + query, callback);
    }
    
    public void banUser(String userId, boolean ban, ApiCallback callback) {
        JSONObject body = new JSONObject();
        try {
            body.put("user_id", userId);
            body.put("banned", ban);
        } catch (Exception e) {
            callback.onError("Failed to create request");
            return;
        }
        post("/api/admin/users/ban", body, true, callback);
    }
    
    public void deleteUser(String userId, ApiCallback callback) {
        delete("/api/admin/users/" + userId, callback);
    }
    
    public void getAIConfig(ApiCallback callback) {
        get("/api/admin/config/ai", callback);
    }
    
    public void updateAIConfig(JSONObject config, ApiCallback callback) {
        post("/api/admin/config/ai", config, true, callback);
    }
    
    public void getSupabaseSettings(ApiCallback callback) {
        get("/api/admin/config/supabase", callback);
    }
    
    public void updateSupabaseSettings(JSONObject settings, ApiCallback callback) {
        post("/api/admin/config/supabase", settings, true, callback);
    }
    
    public void getActivityLogs(int page, int limit, ApiArrayCallback callback) {
        getArray("/api/admin/logs?page=" + page + "&limit=" + limit, callback);
    }
    
    private void get(String endpoint, ApiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(baseUrl + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                if (accessToken != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                }
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                
                int responseCode = conn.getResponseCode();
                String response = readResponse(conn, responseCode);
                
                mainHandler.post(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        try {
                            callback.onSuccess(new JSONObject(response));
                        } catch (Exception e) {
                            callback.onError("Invalid response format");
                        }
                    } else {
                        callback.onError(parseError(response, responseCode));
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }
    
    private void getArray(String endpoint, ApiArrayCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(baseUrl + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                if (accessToken != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                }
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                
                int responseCode = conn.getResponseCode();
                String response = readResponse(conn, responseCode);
                
                mainHandler.post(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            if (obj.has("data")) {
                                callback.onSuccess(obj.getJSONArray("data"));
                            } else {
                                callback.onSuccess(new JSONArray(response));
                            }
                        } catch (Exception e) {
                            try {
                                callback.onSuccess(new JSONArray(response));
                            } catch (Exception e2) {
                                callback.onError("Invalid response format");
                            }
                        }
                    } else {
                        callback.onError(parseError(response, responseCode));
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }
    
    private void post(String endpoint, JSONObject body, boolean requireAuth, ApiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(baseUrl + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                if (requireAuth && accessToken != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                }
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                
                OutputStream os = conn.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.close();
                
                int responseCode = conn.getResponseCode();
                String response = readResponse(conn, responseCode);
                
                mainHandler.post(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        try {
                            callback.onSuccess(new JSONObject(response));
                        } catch (Exception e) {
                            JSONObject empty = new JSONObject();
                            callback.onSuccess(empty);
                        }
                    } else {
                        callback.onError(parseError(response, responseCode));
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }
    
    private void delete(String endpoint, ApiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(baseUrl + endpoint);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setRequestProperty("Content-Type", "application/json");
                if (accessToken != null) {
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                }
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                
                int responseCode = conn.getResponseCode();
                String response = readResponse(conn, responseCode);
                
                mainHandler.post(() -> {
                    if (responseCode >= 200 && responseCode < 300) {
                        try {
                            callback.onSuccess(new JSONObject(response));
                        } catch (Exception e) {
                            JSONObject empty = new JSONObject();
                            callback.onSuccess(empty);
                        }
                    } else {
                        callback.onError(parseError(response, responseCode));
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }
    
    private String readResponse(HttpURLConnection conn, int responseCode) throws Exception {
        BufferedReader reader;
        if (responseCode >= 200 && responseCode < 300) {
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }
    
    private String parseError(String response, int code) {
        try {
            JSONObject obj = new JSONObject(response);
            if (obj.has("error")) {
                return obj.getString("error");
            } else if (obj.has("message")) {
                return obj.getString("message");
            }
        } catch (Exception ignored) {}
        return "Error " + code;
    }
}
