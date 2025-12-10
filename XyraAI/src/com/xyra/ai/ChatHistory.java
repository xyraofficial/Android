package com.xyra.ai;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ChatHistory {
    
    private static final String PREFS_NAME = "xyra_chat_history";
    private static final String KEY_MESSAGES = "messages";
    
    private SharedPreferences prefs;
    
    public ChatHistory(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
                jsonArray.put(jsonMsg);
            }
            
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_MESSAGES, jsonArray.toString());
            editor.apply();
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
}
