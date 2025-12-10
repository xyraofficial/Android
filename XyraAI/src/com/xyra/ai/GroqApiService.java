package com.xyra.ai;

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

public class GroqApiService {
    
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    
    private String apiKey;
    private Handler mainHandler;
    private Thread workerThread;
    
    public interface ChatCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    public GroqApiService(String apiKey) {
        this.apiKey = apiKey;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void sendMessage(final List<Message> conversationHistory, final ChatCallback callback) {
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String response = makeApiCall(conversationHistory);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(response);
                        }
                    });
                } catch (final Exception e) {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(e.getMessage());
                        }
                    });
                }
            }
        });
        workerThread.start();
    }
    
    private String makeApiCall(List<Message> conversationHistory) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            
            JSONObject requestBody = buildRequestBody(conversationHistory);
            
            OutputStream os = connection.getOutputStream();
            byte[] input = requestBody.toString().getBytes("UTF-8");
            os.write(input, 0, input.length);
            os.close();
            
            int responseCode = connection.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                return parseResponse(response.toString());
            } else {
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), "UTF-8"));
                StringBuilder error = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    error.append(line);
                }
                reader.close();
                
                throw new Exception("API Error: " + responseCode + " - " + error.toString());
            }
        } finally {
            connection.disconnect();
        }
    }
    
    private JSONObject buildRequestBody(List<Message> conversationHistory) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", MODEL);
        body.put("temperature", 0.7);
        body.put("max_tokens", 2048);
        
        JSONArray messages = new JSONArray();
        
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are Xyra, a helpful, friendly, and intelligent AI assistant. " +
            "You provide clear, accurate, and helpful responses. " +
            "You are knowledgeable about many topics and always aim to be helpful. " +
            "Keep your responses concise but informative. " +
            "If you don't know something, admit it honestly.");
        messages.put(systemMessage);
        
        for (int i = 0; i < conversationHistory.size(); i++) {
            Message msg = conversationHistory.get(i);
            JSONObject messageObj = new JSONObject();
            if (msg.getType() == Message.TYPE_USER) {
                messageObj.put("role", "user");
            } else {
                messageObj.put("role", "assistant");
            }
            messageObj.put("content", msg.getContent());
            messages.put(messageObj);
        }
        
        body.put("messages", messages);
        
        return body;
    }
    
    private String parseResponse(String jsonResponse) throws Exception {
        JSONObject response = new JSONObject(jsonResponse);
        JSONArray choices = response.getJSONArray("choices");
        
        if (choices.length() > 0) {
            JSONObject choice = choices.getJSONObject(0);
            JSONObject message = choice.getJSONObject("message");
            return message.getString("content");
        }
        
        throw new Exception("No response content");
    }
    
    public void shutdown() {
        if (workerThread != null && workerThread.isAlive()) {
            workerThread.interrupt();
        }
    }
}
