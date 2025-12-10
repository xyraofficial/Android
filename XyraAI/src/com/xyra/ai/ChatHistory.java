package com.xyra.ai;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatHistory {
    
    private static final String PREFS_NAME = "xyra_chat_history";
    private static final String KEY_MESSAGES = "messages";
    private static final String KEY_CHATS = "chats";
    private static final String KEY_CURRENT_CHAT = "current_chat";
    
    private SharedPreferences prefs;
    private Context context;
    
    public ChatHistory(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public void saveMessages(List<Message> messages) {
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
            editor.putString(KEY_MESSAGES, jsonArray.toString());
            editor.apply();
            
            saveChatPreview(messages);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void saveChatPreview(List<Message> messages) {
        if (messages.isEmpty()) return;
        
        try {
            String currentChatId = getCurrentChatId();
            JSONArray chats = getChatsArray();
            
            String preview = "";
            for (int i = messages.size() - 1; i >= 0; i--) {
                Message msg = messages.get(i);
                if (msg.getType() == Message.TYPE_USER) {
                    preview = msg.getContent();
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
                if (chat.getString("id").equals(currentChatId)) {
                    chat.put("preview", preview);
                    chat.put("timestamp", System.currentTimeMillis());
                    chat.put("messageCount", messages.size());
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                JSONObject newChat = new JSONObject();
                newChat.put("id", currentChatId);
                newChat.put("preview", preview);
                newChat.put("timestamp", System.currentTimeMillis());
                newChat.put("messageCount", messages.size());
                chats.put(newChat);
            }
            
            prefs.edit().putString(KEY_CHATS, chats.toString()).apply();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public List<Message> loadMessages() {
        List<Message> messages = new ArrayList<Message>();
        
        try {
            String json = prefs.getString(KEY_MESSAGES, "[]");
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
                messages.add(msg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return messages;
    }
    
    public void clearHistory() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_MESSAGES);
        editor.apply();
    }
    
    public String getCurrentChatId() {
        String chatId = prefs.getString(KEY_CURRENT_CHAT, null);
        if (chatId == null) {
            chatId = UUID.randomUUID().toString();
            prefs.edit().putString(KEY_CURRENT_CHAT, chatId).apply();
        }
        return chatId;
    }
    
    public void startNewChat() {
        String newChatId = UUID.randomUUID().toString();
        prefs.edit()
            .putString(KEY_CURRENT_CHAT, newChatId)
            .remove(KEY_MESSAGES)
            .apply();
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
            
            prefs.edit().putString(KEY_CHATS, newChats.toString()).apply();
            
            String currentChatId = getCurrentChatId();
            if (currentChatId.equals(chatId)) {
                startNewChat();
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
    
    public static class ChatItem {
        public String id;
        public String preview;
        public long timestamp;
        public int messageCount;
    }
}
