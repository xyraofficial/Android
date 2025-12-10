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
    private static final String VISION_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";
    
    private static final String SYSTEM_PROMPT = 
        "You are Xyra, a highly intelligent, helpful, and friendly AI assistant similar to ChatGPT. " +
        "You have the following capabilities and guidelines:\n\n" +
        
        "LANGUAGE DETECTION & RESPONSE:\n" +
        "- ALWAYS detect the language the user is writing in\n" +
        "- ALWAYS respond in the SAME language as the user's message\n" +
        "- If user writes in Indonesian, respond in Indonesian\n" +
        "- If user writes in English, respond in English\n" +
        "- If user writes in Japanese, respond in Japanese\n" +
        "- And so on for any language\n\n" +
        
        "CAPABILITIES:\n" +
        "- Answer questions on any topic with accurate, helpful information\n" +
        "- Help with coding, debugging, and explaining code in any programming language\n" +
        "- Assist with writing, editing, and improving text\n" +
        "- Translate between languages when asked\n" +
        "- Explain complex concepts in simple terms\n" +
        "- Help with math, science, history, and general knowledge\n" +
        "- Provide creative ideas and brainstorming\n" +
        "- Summarize long texts or articles\n" +
        "- Help with learning and education\n\n" +
        
        "RESPONSE STYLE:\n" +
        "- Be conversational, friendly, and engaging\n" +
        "- Use clear formatting with bullet points and numbered lists when helpful\n" +
        "- For code, wrap it in markdown code blocks with language specification\n" +
        "- Keep responses concise but complete\n" +
        "- If you don't know something, admit it honestly\n" +
        "- Ask clarifying questions when the request is ambiguous\n" +
        "- Be encouraging and supportive\n\n" +
        
        "FORMATTING:\n" +
        "- Use **bold** for emphasis\n" +
        "- Use bullet points for lists\n" +
        "- Use numbered lists for steps\n" +
        "- Use code blocks for code snippets\n" +
        "- Break long responses into readable paragraphs";
    
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
                    final String response = makeApiCall(conversationHistory, null, null);
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
    
    public void sendMessageWithImage(final List<Message> conversationHistory, final String text, final String imageBase64, final ChatCallback callback) {
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String response = makeApiCall(conversationHistory, text, imageBase64);
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
    
    private String makeApiCall(List<Message> conversationHistory, String imageText, String imageBase64) throws Exception {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        try {
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setDoOutput(true);
            connection.setConnectTimeout(30000);
            connection.setReadTimeout(60000);
            
            JSONObject requestBody;
            if (imageBase64 != null && !imageBase64.isEmpty()) {
                requestBody = buildRequestBodyWithImage(conversationHistory, imageText, imageBase64);
            } else {
                requestBody = buildRequestBody(conversationHistory);
            }
            
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
        body.put("max_tokens", 4096);
        body.put("top_p", 0.9);
        
        JSONArray messages = new JSONArray();
        
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT);
        messages.put(systemMessage);
        
        for (int i = 0; i < conversationHistory.size(); i++) {
            Message msg = conversationHistory.get(i);
            if (isThinkingMessage(msg.getContent())) {
                continue;
            }
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
    
    private JSONObject buildRequestBodyWithImage(List<Message> conversationHistory, String text, String imageBase64) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", VISION_MODEL);
        body.put("temperature", 0.7);
        body.put("max_tokens", 4096);
        body.put("top_p", 0.9);
        
        JSONArray messages = new JSONArray();
        
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT + "\n\nYou can also analyze images. When given an image, describe what you see in detail and answer any questions about it.");
        messages.put(systemMessage);
        
        for (int i = 0; i < conversationHistory.size() - 2; i++) {
            Message msg = conversationHistory.get(i);
            if (isThinkingMessage(msg.getContent())) {
                continue;
            }
            JSONObject messageObj = new JSONObject();
            if (msg.getType() == Message.TYPE_USER) {
                messageObj.put("role", "user");
            } else {
                messageObj.put("role", "assistant");
            }
            messageObj.put("content", msg.getContent());
            messages.put(messageObj);
        }
        
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        
        JSONArray contentArray = new JSONArray();
        
        JSONObject textContent = new JSONObject();
        textContent.put("type", "text");
        textContent.put("text", text);
        contentArray.put(textContent);
        
        JSONObject imageContent = new JSONObject();
        imageContent.put("type", "image_url");
        JSONObject imageUrl = new JSONObject();
        imageUrl.put("url", "data:image/jpeg;base64," + imageBase64);
        imageContent.put("image_url", imageUrl);
        contentArray.put(imageContent);
        
        userMessage.put("content", contentArray);
        messages.put(userMessage);
        
        body.put("messages", messages);
        
        return body;
    }
    
    private boolean isThinkingMessage(String content) {
        return content.equals("Thinking...") || 
               content.equals("Sedang berpikir...") ||
               content.equals("thinking") ||
               content.startsWith("[Gambar]");
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
