package com.xyra.ai;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatHistory {
    
    private static final String PREFS_NAME_PREFIX = "xyra_chat_history_";
    private static final String KEY_CHATS = "chats";
    private static final String KEY_CURRENT_CHAT = "current_chat";
    private static final String KEY_MESSAGES_PREFIX = "messages_";
    private static final String USER_PREFS_NAME = "XyraAIProfile";
    private static final String KEY_USER_ID = "userId";
    
    private SharedPreferences prefs;
    private Context context;
    private String currentUserId;
    private SupabaseService supabaseService;
    
    public ChatHistory(Context context) {
        this.context = context;
        this.currentUserId = getUserId(context);
        String prefsName = PREFS_NAME_PREFIX + currentUserId;
        this.prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        this.supabaseService = new SupabaseService(context);
    }
    
    private String getUserId(Context context) {
        SharedPreferences userPrefs = context.getSharedPreferences(USER_PREFS_NAME, Context.MODE_PRIVATE);
        String oderId = userPrefs.getString(KEY_USER_ID, "default_user");
        return oderId;
    }
    
    public void refreshForUser() {
        String newUserId = getUserId(context);
        if (!newUserId.equals(currentUserId)) {
            currentUserId = newUserId;
            String prefsName = PREFS_NAME_PREFIX + currentUserId;
            prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE);
        }
    }
    
    public void saveMessages(List<Message> messages) {
        String currentChatId = getCurrentChatId();
        saveMessagesToChat(currentChatId, messages);
    }
    
    public void saveMessagesToChat(String chatId, List<Message> messages) {
        try {
            JSONArray jsonArray = new JSONArray();
            
            for (int i = 0; i < messages.size(); i++) {
                Message msg = messages.get(i);
                JSONObject jsonMsg = new JSONObject();
                jsonMsg.put("content", msg.getContent());
                jsonMsg.put("type", msg.getType());
                jsonMsg.put("timestamp", msg.getTimestamp());
                if (msg.getImageBase64() != null) {
                    jsonMsg.put("imageBase64", msg.getImageBase64());
                }
                jsonArray.put(jsonMsg);
            }
            
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_MESSAGES_PREFIX + chatId, jsonArray.toString());
            editor.apply();
            
            String preview = saveChatPreview(chatId, messages);
            
            syncToCloud(chatId, preview, messages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void syncToCloud(String chatId, String preview, List<Message> messages) {
        if (supabaseService.isLoggedIn()) {
            supabaseService.syncChatToCloud(chatId, preview, System.currentTimeMillis(), messages.size(), messages, null);
        }
    }
    
    private String saveChatPreview(String chatId, List<Message> messages) {
        if (messages.isEmpty()) return "";
        
        String preview = "";
        try {
            JSONArray chats = getChatsArray();
            
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message msg = messages.get(i);
                if (msg.getType() == Message.TYPE_USER) {
                    preview = msg.getContent();
                    if (preview.startsWith("[Gambar] ")) {
                        preview = preview.substring(9);
                    }
                    if (preview.length() > 50) {
                        preview = preview.substring(0, 47) + "...";
                    }
                    break;
                }
            }
            
            if (preview.isEmpty() && messages.size() > 0) {
                preview = "Chat baru";
            }
            
            boolean found = false;
            for (int i = 0; i < chats.length(); i++) {
                JSONObject chat = chats.getJSONObject(i);
                if (chat.getString("id").equals(chatId)) {
                    chat.put("preview", preview);
                    chat.put("timestamp", System.currentTimeMillis());
                    chat.put("messageCount", messages.size());
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                JSONObject newChat = new JSONObject();
                newChat.put("id", chatId);
                newChat.put("preview", preview);
                newChat.put("timestamp", System.currentTimeMillis());
                newChat.put("messageCount", messages.size());
                chats.put(newChat);
            }
            
            prefs.edit().putString(KEY_CHATS, chats.toString()).apply();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return preview;
    }
    
    public List<Message> loadMessages() {
        String currentChatId = getCurrentChatId();
        return loadMessagesForChat(currentChatId);
    }
    
    public List<Message> loadMessagesForChat(String chatId) {
        List<Message> messages = new ArrayList<Message>();
        
        try {
            String json = prefs.getString(KEY_MESSAGES_PREFIX + chatId, "[]");
            JSONArray jsonArray = new JSONArray(json);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonMsg = jsonArray.getJSONObject(i);
                Message msg = new Message(
                    jsonMsg.getString("content"),
                    jsonMsg.getInt("type")
                );
                msg.setTimestamp(jsonMsg.getLong("timestamp"));
                if (jsonMsg.has("imageBase64")) {
                    msg.setImageBase64(jsonMsg.getString("imageBase64"));
                }
                msg.setChatId(chatId);
                messages.add(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return messages;
    }
    
    public void clearHistory() {
        String currentChatId = getCurrentChatId();
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_MESSAGES_PREFIX + currentChatId);
        editor.apply();
    }
    
    public String getCurrentChatId() {
        String chatId = prefs.getString(KEY_CURRENT_CHAT, null);
        if (chatId == null) {
            List<ChatItem> existingChats = getChatList();
            if (!existingChats.isEmpty()) {
                chatId = existingChats.get(0).id;
                prefs.edit().putString(KEY_CURRENT_CHAT, chatId).apply();
            } else {
                chatId = UUID.randomUUID().toString();
                prefs.edit().putString(KEY_CURRENT_CHAT, chatId).apply();
            }
        }
        return chatId;
    }
    
    public void initializeFromCloud(final InitCallback callback) {
        if (!supabaseService.isLoggedIn()) {
            String localId = getCurrentChatIdInternal();
            if (callback != null) callback.onComplete(false, localId);
            return;
        }
        
        supabaseService.loadChatsFromCloud(new SupabaseService.ChatLoadCallback() {
            @Override
            public void onSuccess(List<ChatItem> cloudChats) {
                String resolvedChatId = null;
                
                if (cloudChats != null && !cloudChats.isEmpty()) {
                    for (ChatItem cloudChat : cloudChats) {
                        if (!chatExists(cloudChat.id)) {
                            createChatWithId(cloudChat.id, cloudChat.preview, cloudChat.preview);
                        }
                    }
                    
                    String currentId = prefs.getString(KEY_CURRENT_CHAT, null);
                    if (currentId == null) {
                        long mostRecentTime = 0;
                        String mostRecentId = null;
                        for (ChatItem chat : cloudChats) {
                            if (chat.timestamp > mostRecentTime) {
                                mostRecentTime = chat.timestamp;
                                mostRecentId = chat.id;
                            }
                        }
                        if (mostRecentId != null) {
                            prefs.edit().putString(KEY_CURRENT_CHAT, mostRecentId).apply();
                            resolvedChatId = mostRecentId;
                        }
                    } else {
                        resolvedChatId = currentId;
                    }
                    
                    final String chatIdToLoad = resolvedChatId;
                    if (chatIdToLoad != null) {
                        loadMessagesFromCloudForChatSync(chatIdToLoad);
                    }
                } else {
                    resolvedChatId = getCurrentChatIdInternal();
                }
                
                if (callback != null) callback.onComplete(true, resolvedChatId);
            }
            
            @Override
            public void onError(String errorMessage) {
                String localId = getCurrentChatIdInternal();
                if (callback != null) callback.onComplete(false, localId);
            }
        });
    }
    
    private String getCurrentChatIdInternal() {
        String chatId = prefs.getString(KEY_CURRENT_CHAT, null);
        if (chatId == null) {
            List<ChatItem> existingChats = getChatList();
            if (!existingChats.isEmpty()) {
                chatId = existingChats.get(0).id;
                prefs.edit().putString(KEY_CURRENT_CHAT, chatId).apply();
            } else {
                chatId = UUID.randomUUID().toString();
                prefs.edit().putString(KEY_CURRENT_CHAT, chatId).apply();
            }
        }
        return chatId;
    }
    
    private void loadMessagesFromCloudForChatSync(final String chatId) {
        supabaseService.loadMessagesFromCloud(chatId, new SupabaseService.MessagesLoadCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (messages != null && !messages.isEmpty()) {
                    try {
                        JSONArray jsonArray = new JSONArray();
                        for (Message msg : messages) {
                            JSONObject jsonMsg = new JSONObject();
                            jsonMsg.put("content", msg.getContent());
                            jsonMsg.put("type", msg.getType());
                            jsonMsg.put("timestamp", msg.getTimestamp());
                            if (msg.getImageBase64() != null) {
                                jsonMsg.put("imageBase64", msg.getImageBase64());
                            }
                            jsonArray.put(jsonMsg);
                        }
                        prefs.edit().putString(KEY_MESSAGES_PREFIX + chatId, jsonArray.toString()).apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onError(String errorMessage) {
            }
        });
    }
    
    public interface InitCallback {
        void onComplete(boolean success, String currentChatId);
    }
    
    public interface MessagesCallback {
        void onComplete(List<Message> messages);
    }
    
    public void loadMessagesFromCloudAndDisplay(final String chatId, final MessagesCallback callback) {
        if (!supabaseService.isLoggedIn()) {
            if (callback != null) callback.onComplete(new ArrayList<Message>());
            return;
        }
        
        supabaseService.loadMessagesFromCloud(chatId, new SupabaseService.MessagesLoadCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (messages != null && !messages.isEmpty()) {
                    try {
                        JSONArray jsonArray = new JSONArray();
                        for (Message msg : messages) {
                            JSONObject jsonMsg = new JSONObject();
                            jsonMsg.put("content", msg.getContent());
                            jsonMsg.put("type", msg.getType());
                            jsonMsg.put("timestamp", msg.getTimestamp());
                            if (msg.getImageBase64() != null) {
                                jsonMsg.put("imageBase64", msg.getImageBase64());
                            }
                            jsonArray.put(jsonMsg);
                        }
                        prefs.edit().putString(KEY_MESSAGES_PREFIX + chatId, jsonArray.toString()).apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (callback != null) callback.onComplete(messages != null ? messages : new ArrayList<Message>());
            }
            
            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onComplete(new ArrayList<Message>());
            }
        });
    }
    
    public void startNewChat() {
        String newChatId = UUID.randomUUID().toString();
        
        try {
            JSONArray chats = getChatsArray();
            JSONObject newChat = new JSONObject();
            newChat.put("id", newChatId);
            newChat.put("preview", "Chat baru");
            newChat.put("timestamp", System.currentTimeMillis());
            newChat.put("messageCount", 0);
            chats.put(newChat);
            
            prefs.edit()
                .putString(KEY_CURRENT_CHAT, newChatId)
                .putString(KEY_MESSAGES_PREFIX + newChatId, "[]")
                .putString(KEY_CHATS, chats.toString())
                .apply();
        } catch (Exception e) {
            prefs.edit()
                .putString(KEY_CURRENT_CHAT, newChatId)
                .putString(KEY_MESSAGES_PREFIX + newChatId, "[]")
                .apply();
        }
    }
    
    public List<ChatItem> getChatList() {
        List<ChatItem> chatList = new ArrayList<ChatItem>();
        
        try {
            JSONArray chats = getChatsArray();
            
            for (int i = chats.length() - 1; i >= 0; i--) {
                JSONObject chat = chats.getJSONObject(i);
                ChatItem item = new ChatItem();
                item.id = chat.getString("id");
                item.preview = chat.getString("preview");
                item.timestamp = chat.getLong("timestamp");
                item.messageCount = chat.optInt("messageCount", 0);
                chatList.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return chatList;
    }
    
    public void syncFromCloud(final SyncCallback callback) {
        if (!supabaseService.isLoggedIn()) {
            if (callback != null) callback.onComplete(false);
            return;
        }
        
        supabaseService.loadChatsFromCloud(new SupabaseService.ChatLoadCallback() {
            @Override
            public void onSuccess(List<ChatItem> cloudChats) {
                for (ChatItem cloudChat : cloudChats) {
                    if (!chatExists(cloudChat.id)) {
                        createChatWithId(cloudChat.id, cloudChat.preview, cloudChat.preview);
                        
                        loadMessagesFromCloudForChat(cloudChat.id);
                    }
                }
                if (callback != null) callback.onComplete(true);
            }
            
            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onComplete(false);
            }
        });
    }
    
    private void loadMessagesFromCloudForChat(final String chatId) {
        supabaseService.loadMessagesFromCloud(chatId, new SupabaseService.MessagesLoadCallback() {
            @Override
            public void onSuccess(List<Message> messages) {
                if (!messages.isEmpty()) {
                    try {
                        JSONArray jsonArray = new JSONArray();
                        for (Message msg : messages) {
                            JSONObject jsonMsg = new JSONObject();
                            jsonMsg.put("content", msg.getContent());
                            jsonMsg.put("type", msg.getType());
                            jsonMsg.put("timestamp", msg.getTimestamp());
                            if (msg.getImageBase64() != null) {
                                jsonMsg.put("imageBase64", msg.getImageBase64());
                            }
                            jsonArray.put(jsonMsg);
                        }
                        prefs.edit().putString(KEY_MESSAGES_PREFIX + chatId, jsonArray.toString()).apply();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            
            @Override
            public void onError(String errorMessage) {
            }
        });
    }
    
    public void deleteChat(String chatId) {
        try {
            JSONArray chats = getChatsArray();
            JSONArray newChats = new JSONArray();
            
            for (int i = 0; i < chats.length(); i++) {
                JSONObject chat = chats.getJSONObject(i);
                if (!chat.getString("id").equals(chatId)) {
                    newChats.put(chat);
                }
            }
            
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_CHATS, newChats.toString());
            editor.remove(KEY_MESSAGES_PREFIX + chatId);
            editor.apply();
            
            if (supabaseService.isLoggedIn()) {
                supabaseService.deleteChatFromCloud(chatId, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void loadChat(String chatId) {
        prefs.edit().putString(KEY_CURRENT_CHAT, chatId).apply();
    }
    
    private JSONArray getChatsArray() {
        try {
            String json = prefs.getString(KEY_CHATS, "[]");
            return new JSONArray(json);
        } catch (Exception e) {
            return new JSONArray();
        }
    }
    
    public List<ChatItem> searchChats(String query) {
        List<ChatItem> results = new ArrayList<ChatItem>();
        List<ChatItem> allChats = getChatList();
        
        String lowerQuery = query.toLowerCase();
        
        for (ChatItem chat : allChats) {
            if (chat.preview.toLowerCase().contains(lowerQuery)) {
                results.add(chat);
            }
        }
        
        return results;
    }
    
    public void clearAllData() {
        prefs.edit().clear().apply();
    }
    
    public boolean chatExists(String chatId) {
        try {
            JSONArray chats = getChatsArray();
            for (int i = 0; i < chats.length(); i++) {
                JSONObject chat = chats.getJSONObject(i);
                if (chat.getString("id").equals(chatId)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public void createChatWithId(String chatId, String title, String preview) {
        try {
            JSONArray chats = getChatsArray();
            JSONObject newChat = new JSONObject();
            newChat.put("id", chatId);
            newChat.put("preview", preview.isEmpty() ? title : preview);
            newChat.put("timestamp", System.currentTimeMillis());
            newChat.put("messageCount", 0);
            chats.put(newChat);
            
            prefs.edit()
                .putString(KEY_MESSAGES_PREFIX + chatId, "[]")
                .putString(KEY_CHATS, chats.toString())
                .apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveMessagesForChat(String chatId, List<Message> messages) {
        saveMessagesToChat(chatId, messages);
    }
    
    public interface SyncCallback {
        void onComplete(boolean success);
    }
    
    public static class ChatItem {
        public String id;
        public String preview;
        public long timestamp;
        public int messageCount;
    }
}
