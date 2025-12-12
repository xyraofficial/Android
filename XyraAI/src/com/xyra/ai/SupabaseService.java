package com.xyra.ai;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SupabaseService {
    
    private static final String TAG = "SupabaseService";
    
    private static final String SUPABASE_URL = "https://figcqxynrcnimagpswqn.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZpZ2NxeHlucmNuaW1hZ3Bzd3FuIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU1Mzk3NDYsImV4cCI6MjA4MTExNTc0Nn0.uGVsFxsbLrSjtrli80UDrg96y1JRQyuMc7chQMVDG1A";
    
    private static final String AUTH_URL = SUPABASE_URL + "/auth/v1";
    private static final String REST_URL = SUPABASE_URL + "/rest/v1";
    
    private static final String REDIRECT_URL = "https://android-rho-five.vercel.app/auth/callback";
    
    private static final String PREFS_NAME = "XyraAIProfile";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_JOIN_DATE = "joinDate";
    
    private Context context;
    private SharedPreferences prefs;
    private ExecutorService executor;
    private Handler handler;
    
    public interface AuthCallback {
        void onSuccess(String userId, String email, String displayName);
        void onError(String errorMessage);
    }
    
    public interface ChatSyncCallback {
        void onSuccess();
        void onError(String errorMessage);
    }
    
    public interface ChatLoadCallback {
        void onSuccess(List<ChatHistory.ChatItem> chats);
        void onError(String errorMessage);
    }
    
    public interface MessagesLoadCallback {
        void onSuccess(List<Message> messages);
        void onError(String errorMessage);
    }
    
    public SupabaseService(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.executor = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    public void signUp(final String email, final String password, final String displayName, final AuthCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(AUTH_URL + "/signup");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("email", email);
                    requestBody.put("password", password);
                    
                    JSONObject data = new JSONObject();
                    data.put("display_name", displayName);
                    requestBody.put("data", data);
                    
                    OutputStream os = conn.getOutputStream();
                    os.write(requestBody.toString().getBytes("UTF-8"));
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
                    conn.disconnect();
                    
                    final JSONObject jsonResponse = new JSONObject(response.toString());
                    
                    if (responseCode >= 200 && responseCode < 300) {
                        JSONObject user = jsonResponse.optJSONObject("user");
                        if (user != null) {
                            final String userId = user.optString("id", "");
                            final String userEmail = user.optString("email", email);
                            
                            String accessToken = jsonResponse.optString("access_token", "");
                            String refreshToken = jsonResponse.optString("refresh_token", "");
                            
                            saveTokens(accessToken, refreshToken, userId, userEmail, displayName);
                            
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess(userId, userEmail, displayName);
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onSuccess("", email, displayName);
                                }
                            });
                        }
                    } else {
                        final String errorMessage = parseSupabaseError(jsonResponse);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(errorMessage);
                            }
                        });
                    }
                    
                } catch (final Exception e) {
                    Log.e(TAG, "SignUp error", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Koneksi error: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }
    
    public void signIn(final String email, final String password, final AuthCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(AUTH_URL + "/token?grant_type=password");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    
                    JSONObject requestBody = new JSONObject();
                    requestBody.put("email", email);
                    requestBody.put("password", password);
                    
                    OutputStream os = conn.getOutputStream();
                    os.write(requestBody.toString().getBytes("UTF-8"));
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
                    conn.disconnect();
                    
                    final JSONObject jsonResponse = new JSONObject(response.toString());
                    
                    if (responseCode >= 200 && responseCode < 300) {
                        JSONObject user = jsonResponse.optJSONObject("user");
                        String accessToken = jsonResponse.optString("access_token", "");
                        String refreshToken = jsonResponse.optString("refresh_token", "");
                        
                        String userId = "";
                        String userEmail = email;
                        String displayName = "XyraAI User";
                        
                        if (user != null) {
                            userId = user.optString("id", "");
                            userEmail = user.optString("email", email);
                            
                            JSONObject userMetadata = user.optJSONObject("user_metadata");
                            if (userMetadata != null) {
                                displayName = userMetadata.optString("display_name", "XyraAI User");
                            }
                        }
                        
                        final String finalUserId = userId;
                        final String finalEmail = userEmail;
                        final String finalDisplayName = displayName;
                        
                        saveTokens(accessToken, refreshToken, userId, userEmail, displayName);
                        
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(finalUserId, finalEmail, finalDisplayName);
                            }
                        });
                    } else {
                        final String errorMessage = parseSupabaseError(jsonResponse);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError(errorMessage);
                            }
                        });
                    }
                    
                } catch (final Exception e) {
                    Log.e(TAG, "SignIn error", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Koneksi error: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }
    
    public String getGoogleOAuthUrl() {
        try {
            return AUTH_URL + "/authorize?provider=google&redirect_to=" + 
                   java.net.URLEncoder.encode(REDIRECT_URL, "UTF-8");
        } catch (Exception e) {
            return AUTH_URL + "/authorize?provider=google&redirect_to=" + REDIRECT_URL;
        }
    }
    
    public void handleOAuthCallback(final String accessToken, final String refreshToken, final AuthCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(AUTH_URL + "/user");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    
                    int responseCode = conn.getResponseCode();
                    
                    if (responseCode >= 200 && responseCode < 300) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        conn.disconnect();
                        
                        JSONObject user = new JSONObject(response.toString());
                        final String userId = user.optString("id", "");
                        final String userEmail = user.optString("email", "");
                        
                        JSONObject userMetadata = user.optJSONObject("user_metadata");
                        String displayName = "XyraAI User";
                        String photoUrl = "";
                        
                        if (userMetadata != null) {
                            displayName = userMetadata.optString("full_name", 
                                         userMetadata.optString("name", "XyraAI User"));
                            photoUrl = userMetadata.optString("avatar_url", "");
                        }
                        
                        final String finalDisplayName = displayName;
                        final String finalPhotoUrl = photoUrl;
                        
                        saveTokens(accessToken, refreshToken, userId, userEmail, displayName);
                        
                        SharedPreferences.Editor editor = prefs.edit();
                        if (!photoUrl.isEmpty()) {
                            editor.putString("userPhoto", photoUrl);
                        }
                        editor.apply();
                        
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(userId, userEmail, finalDisplayName);
                            }
                        });
                    } else {
                        conn.disconnect();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError("Failed to get user info");
                            }
                        });
                    }
                    
                } catch (final Exception e) {
                    Log.e(TAG, "OAuth callback error", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("OAuth error: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }
    
    public void signOut() {
        String accessToken = prefs.getString(KEY_ACCESS_TOKEN, "");
        
        if (!accessToken.isEmpty()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        URL url = new URL(AUTH_URL + "/logout");
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
                        conn.setRequestProperty("Authorization", "Bearer " + prefs.getString(KEY_ACCESS_TOKEN, ""));
                        conn.setConnectTimeout(10000);
                        conn.setReadTimeout(10000);
                        conn.getResponseCode();
                        conn.disconnect();
                    } catch (Exception e) {
                        Log.e(TAG, "SignOut error", e);
                    }
                }
            });
        }
        
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
    
    public void syncChatToCloud(final String chatId, final String preview, final long timestamp, final int messageCount, final List<Message> messages, final ChatSyncCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String accessToken = prefs.getString(KEY_ACCESS_TOKEN, "");
                    String userId = prefs.getString(KEY_USER_ID, "");
                    
                    if (accessToken.isEmpty() || userId.isEmpty()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) callback.onError("Not authenticated");
                            }
                        });
                        return;
                    }
                    
                    JSONObject chatData = new JSONObject();
                    chatData.put("id", chatId);
                    chatData.put("user_id", userId);
                    chatData.put("preview", preview);
                    chatData.put("updated_at", timestamp);
                    chatData.put("message_count", messageCount);
                    
                    JSONArray messagesArray = new JSONArray();
                    for (Message msg : messages) {
                        JSONObject msgObj = new JSONObject();
                        msgObj.put("content", msg.getContent());
                        msgObj.put("type", msg.getType());
                        msgObj.put("timestamp", msg.getTimestamp());
                        if (msg.getImageBase64() != null) {
                            msgObj.put("imageBase64", msg.getImageBase64());
                        }
                        messagesArray.put(msgObj);
                    }
                    chatData.put("messages", messagesArray.toString());
                    
                    URL url = new URL(REST_URL + "/chats?on_conflict=id");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                    conn.setRequestProperty("Prefer", "resolution=merge-duplicates");
                    conn.setDoOutput(true);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    
                    OutputStream os = conn.getOutputStream();
                    os.write(chatData.toString().getBytes("UTF-8"));
                    os.close();
                    
                    int responseCode = conn.getResponseCode();
                    conn.disconnect();
                    
                    if (responseCode >= 200 && responseCode < 300) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) callback.onSuccess();
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) callback.onError("Sync failed: " + responseCode);
                            }
                        });
                    }
                    
                } catch (final Exception e) {
                    Log.e(TAG, "Sync error", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) callback.onError("Sync error: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }
    
    public void loadChatsFromCloud(final ChatLoadCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String accessToken = prefs.getString(KEY_ACCESS_TOKEN, "");
                    String userId = prefs.getString(KEY_USER_ID, "");
                    
                    if (accessToken.isEmpty() || userId.isEmpty()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError("Not authenticated");
                            }
                        });
                        return;
                    }
                    
                    URL url = new URL(REST_URL + "/chats?user_id=eq." + userId + "&order=updated_at.desc");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    
                    int responseCode = conn.getResponseCode();
                    
                    if (responseCode >= 200 && responseCode < 300) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        conn.disconnect();
                        
                        JSONArray chatsArray = new JSONArray(response.toString());
                        final List<ChatHistory.ChatItem> chats = new ArrayList<ChatHistory.ChatItem>();
                        
                        for (int i = 0; i < chatsArray.length(); i++) {
                            JSONObject chatObj = chatsArray.getJSONObject(i);
                            ChatHistory.ChatItem item = new ChatHistory.ChatItem();
                            item.id = chatObj.getString("id");
                            item.preview = chatObj.optString("preview", "Chat");
                            item.timestamp = chatObj.optLong("updated_at", System.currentTimeMillis());
                            item.messageCount = chatObj.optInt("message_count", 0);
                            chats.add(item);
                        }
                        
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(chats);
                            }
                        });
                    } else {
                        conn.disconnect();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError("Load failed: " + responseCode);
                            }
                        });
                    }
                    
                } catch (final Exception e) {
                    Log.e(TAG, "Load chats error", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Load error: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }
    
    public void loadMessagesFromCloud(final String chatId, final MessagesLoadCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String accessToken = prefs.getString(KEY_ACCESS_TOKEN, "");
                    String userId = prefs.getString(KEY_USER_ID, "");
                    
                    if (accessToken.isEmpty() || userId.isEmpty()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError("Not authenticated");
                            }
                        });
                        return;
                    }
                    
                    URL url = new URL(REST_URL + "/chats?id=eq." + chatId + "&user_id=eq." + userId + "&select=messages");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    
                    int responseCode = conn.getResponseCode();
                    
                    if (responseCode >= 200 && responseCode < 300) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();
                        conn.disconnect();
                        
                        JSONArray resultArray = new JSONArray(response.toString());
                        final List<Message> messages = new ArrayList<Message>();
                        
                        if (resultArray.length() > 0) {
                            JSONObject chatObj = resultArray.getJSONObject(0);
                            String messagesJson = chatObj.optString("messages", "[]");
                            JSONArray messagesArray = new JSONArray(messagesJson);
                            
                            for (int i = 0; i < messagesArray.length(); i++) {
                                JSONObject msgObj = messagesArray.getJSONObject(i);
                                Message msg = new Message(
                                    msgObj.getString("content"),
                                    msgObj.getInt("type")
                                );
                                msg.setTimestamp(msgObj.getLong("timestamp"));
                                if (msgObj.has("imageBase64")) {
                                    msg.setImageBase64(msgObj.getString("imageBase64"));
                                }
                                msg.setChatId(chatId);
                                messages.add(msg);
                            }
                        }
                        
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onSuccess(messages);
                            }
                        });
                    } else {
                        conn.disconnect();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                callback.onError("Load failed: " + responseCode);
                            }
                        });
                    }
                    
                } catch (final Exception e) {
                    Log.e(TAG, "Load messages error", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Load error: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }
    
    public void deleteChatFromCloud(final String chatId, final ChatSyncCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String accessToken = prefs.getString(KEY_ACCESS_TOKEN, "");
                    String userId = prefs.getString(KEY_USER_ID, "");
                    
                    if (accessToken.isEmpty() || userId.isEmpty()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) callback.onError("Not authenticated");
                            }
                        });
                        return;
                    }
                    
                    URL url = new URL(REST_URL + "/chats?id=eq." + chatId + "&user_id=eq." + userId);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("DELETE");
                    conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
                    conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(15000);
                    
                    int responseCode = conn.getResponseCode();
                    conn.disconnect();
                    
                    if (responseCode >= 200 && responseCode < 300) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) callback.onSuccess();
                            }
                        });
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callback != null) callback.onError("Delete failed: " + responseCode);
                            }
                        });
                    }
                    
                } catch (final Exception e) {
                    Log.e(TAG, "Delete chat error", e);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) callback.onError("Delete error: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }
    
    private void saveTokens(String accessToken, String refreshToken, String userId, String email, String displayName) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, displayName);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy", new java.util.Locale("id", "ID"));
        String joinDate = sdf.format(new java.util.Date());
        editor.putString(KEY_JOIN_DATE, joinDate);
        
        editor.apply();
    }
    
    private String parseSupabaseError(JSONObject response) {
        try {
            String message = response.optString("msg", "");
            if (message.isEmpty()) {
                message = response.optString("message", "");
            }
            if (message.isEmpty()) {
                message = response.optString("error_description", "");
            }
            if (message.isEmpty()) {
                message = response.optString("error", "Terjadi kesalahan");
            }
            
            if (message.contains("Invalid login credentials")) {
                return "Email atau password salah";
            } else if (message.contains("User already registered")) {
                return "Email sudah terdaftar";
            } else if (message.contains("Password should be at least")) {
                return "Password minimal 6 karakter";
            } else if (message.contains("Invalid email")) {
                return "Format email tidak valid";
            } else if (message.contains("Email not confirmed")) {
                return "Email belum dikonfirmasi. Silakan cek email Anda.";
            }
            
            return message;
        } catch (Exception e) {
            return "Terjadi kesalahan";
        }
    }
    
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }
    
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, "");
    }
    
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
