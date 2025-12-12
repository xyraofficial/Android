package com.xyra.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiService {
    
    private static final String PREFS_NAME = "XyraAIAuth";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    
    private static String BASE_URL = "https://android-r2sh.vercel.app";
    
    private Context context;
    private SharedPreferences prefs;
    private ExecutorService executor;
    private Handler mainHandler;
    
    public interface AuthCallback {
        void onSuccess(String userId, String username, String email, String token);
        void onError(String error);
    }
    
    public interface SyncCallback {
        void onSuccess();
        void onError(String error);
    }
    
    public interface ChatsCallback {
        void onSuccess(JSONArray chats);
        void onError(String error);
    }
    
    public interface MessagesCallback {
        void onSuccess(JSONArray messages);
        void onError(String error);
    }
    
    public ApiService(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public static void setBaseUrl(String url) {
        BASE_URL = url;
    }
    
    public boolean isLoggedIn() {
        return prefs.getString(KEY_TOKEN, null) != null;
    }
    
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }
    
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }
    
    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }
    
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }
    
    private void saveAuth(String userId, String username, String email, String token) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .putString(KEY_EMAIL, email)
            .putString(KEY_TOKEN, token)
            .apply();
        
        SharedPreferences profilePrefs = context.getSharedPreferences("XyraAIProfile", Context.MODE_PRIVATE);
        profilePrefs.edit().putString("userId", userId).apply();
    }
    
    public void clearAuth() {
        prefs.edit().clear().apply();
    }
    
    public void register(String username, String email, String password, AuthCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("username", username);
                body.put("email", email);
                body.put("password", password);
                
                JSONObject response = postRequest("/api/auth/register", body, null);
                
                if (response.getBoolean("success")) {
                    JSONObject data = response.getJSONObject("data");
                    String userId = data.getString("user_id");
                    String uname = data.getString("username");
                    String mail = data.getString("email");
                    String token = data.getString("token");
                    
                    saveAuth(userId, uname, mail, token);
                    
                    mainHandler.post(() -> callback.onSuccess(userId, uname, mail, token));
                } else {
                    String error = response.optString("error", "Registration failed");
                    mainHandler.post(() -> callback.onError(error));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    public void login(String email, String password, AuthCallback callback) {
        executor.execute(() -> {
            try {
                JSONObject body = new JSONObject();
                body.put("email", email);
                body.put("password", password);
                
                JSONObject response = postRequest("/api/auth/login", body, null);
                
                if (response.getBoolean("success")) {
                    JSONObject data = response.getJSONObject("data");
                    String userId = data.getString("user_id");
                    String username = data.getString("username");
                    String mail = data.getString("email");
                    String token = data.getString("token");
                    
                    saveAuth(userId, username, mail, token);
                    
                    mainHandler.post(() -> callback.onSuccess(userId, username, mail, token));
                } else {
                    String error = response.optString("error", "Login failed");
                    mainHandler.post(() -> callback.onError(error));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    public void logout(SyncCallback callback) {
        executor.execute(() -> {
            try {
                String token = getToken();
                if (token != null) {
                    postRequest("/api/auth/logout", new JSONObject(), token);
                }
                clearAuth();
                mainHandler.post(() -> callback.onSuccess());
            } catch (Exception e) {
                clearAuth();
                mainHandler.post(() -> callback.onSuccess());
            }
        });
    }
    
    public void getChats(ChatsCallback callback) {
        executor.execute(() -> {
            try {
                String token = getToken();
                if (token == null) {
                    mainHandler.post(() -> callback.onError("Not logged in"));
                    return;
                }
                
                JSONObject response = getRequest("/api/chats", token);
                
                if (response.getBoolean("success")) {
                    JSONArray chats = response.getJSONArray("data");
                    mainHandler.post(() -> callback.onSuccess(chats));
                } else {
                    String error = response.optString("error", "Failed to get chats");
                    mainHandler.post(() -> callback.onError(error));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    public void getMessages(String chatId, MessagesCallback callback) {
        executor.execute(() -> {
            try {
                String token = getToken();
                if (token == null) {
                    mainHandler.post(() -> callback.onError("Not logged in"));
                    return;
                }
                
                JSONObject response = getRequest("/api/chats/" + chatId + "/messages", token);
                
                if (response.getBoolean("success")) {
                    JSONArray messages = response.getJSONArray("data");
                    mainHandler.post(() -> callback.onSuccess(messages));
                } else {
                    String error = response.optString("error", "Failed to get messages");
                    mainHandler.post(() -> callback.onError(error));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    public void syncMessages(String chatId, List<Message> messages, SyncCallback callback) {
        executor.execute(() -> {
            try {
                String token = getToken();
                if (token == null) {
                    mainHandler.post(() -> callback.onError("Not logged in"));
                    return;
                }
                
                JSONArray msgArray = new JSONArray();
                for (Message msg : messages) {
                    JSONObject msgObj = new JSONObject();
                    msgObj.put("content", msg.getContent());
                    msgObj.put("type", msg.getType());
                    msgObj.put("timestamp", msg.getTimestamp());
                    if (msg.getImageBase64() != null) {
                        msgObj.put("image_base64", msg.getImageBase64());
                    }
                    msgArray.put(msgObj);
                }
                
                JSONObject body = new JSONObject();
                body.put("messages", msgArray);
                
                JSONObject response = postRequest("/api/chats/" + chatId + "/sync", body, token);
                
                if (response.getBoolean("success")) {
                    mainHandler.post(() -> callback.onSuccess());
                } else {
                    String error = response.optString("error", "Sync failed");
                    mainHandler.post(() -> callback.onError(error));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    public void createChat(String title, SyncCallback callback) {
        executor.execute(() -> {
            try {
                String token = getToken();
                if (token == null) {
                    mainHandler.post(() -> callback.onError("Not logged in"));
                    return;
                }
                
                JSONObject body = new JSONObject();
                body.put("title", title);
                
                JSONObject response = postRequest("/api/chats", body, token);
                
                if (response.getBoolean("success")) {
                    mainHandler.post(() -> callback.onSuccess());
                } else {
                    String error = response.optString("error", "Failed to create chat");
                    mainHandler.post(() -> callback.onError(error));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    public void deleteChat(String chatId, SyncCallback callback) {
        executor.execute(() -> {
            try {
                String token = getToken();
                if (token == null) {
                    mainHandler.post(() -> callback.onError("Not logged in"));
                    return;
                }
                
                JSONObject response = deleteRequest("/api/chats/" + chatId, token);
                
                if (response.getBoolean("success")) {
                    mainHandler.post(() -> callback.onSuccess());
                } else {
                    String error = response.optString("error", "Failed to delete chat");
                    mainHandler.post(() -> callback.onError(error));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }
    
    private JSONObject postRequest(String endpoint, JSONObject body, String token) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        
        OutputStream os = conn.getOutputStream();
        os.write(body.toString().getBytes("UTF-8"));
        os.close();
        
        int responseCode = conn.getResponseCode();
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
        
        return new JSONObject(response.toString());
    }
    
    private JSONObject getRequest(String endpoint, String token) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        
        int responseCode = conn.getResponseCode();
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
        
        return new JSONObject(response.toString());
    }
    
    private JSONObject deleteRequest(String endpoint, String token) throws Exception {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        if (token != null) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        
        int responseCode = conn.getResponseCode();
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
        
        return new JSONObject(response.toString());
    }
}
