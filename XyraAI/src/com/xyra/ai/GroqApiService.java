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
        "- For code, wrap it in markdown code blocks with language specification like ```python\n" +
        "- Keep responses concise but complete\n" +
        "- If you don't know something, admit it honestly\n" +
        "- Ask clarifying questions when the request is ambiguous\n" +
        "- Be encouraging and supportive\n\n" +
        
        "FORMATTING RULES (VERY IMPORTANT):\n" +
        "- NEVER use === or --- or *** as separators or decorations\n" +
        "- NEVER use horizontal lines or ASCII art decorations\n" +
        "- Use **bold** for emphasis and important terms\n" +
        "- Use bullet points (•) for unordered lists\n" +
        "- Use numbered lists (1. 2. 3.) for sequential steps\n" +
        "- Use code blocks with language for code: ```python, ```java, ```bash, etc.\n" +
        "- Use inline `code` for commands, file names, or short code\n" +
        "- Use headers (## or ###) sparingly for major sections only\n" +
        "- Break long responses into readable paragraphs with blank lines\n" +
        "- For instructions, use clear numbered steps like ChatGPT:\n" +
        "  1. First step here\n" +
        "  2. Second step here\n" +
        "- Keep formatting clean and minimal like ChatGPT responses";
    
    private static final String IMAGE_ANALYSIS_PROMPT = 
        "IMAGE ANALYSIS SPECIALIST:\n\n" +
        "CRITICAL RULES - FOLLOW EXACTLY:\n" +
        "1. LANGSUNG ke inti masalah - jangan bertele-tele\n" +
        "2. Identifikasi ERROR/BUG dengan SPESIFIK:\n" +
        "   - Baca SETIAP teks di gambar dengan teliti\n" +
        "   - Jika ada error message, COPY persis kata-katanya\n" +
        "   - Jika ada typo, tunjukkan: 'Kamu tulis X, seharusnya Y'\n" +
        "3. BERIKAN SOLUSI LANGSUNG:\n" +
        "   - Untuk error: berikan fix code yang bisa di-copy paste\n" +
        "   - Untuk bug: jelaskan step by step cara perbaiki\n" +
        "4. FORMAT RESPONS:\n" +
        "   - Baris 1: Masalah utama dalam 1 kalimat\n" +
        "   - Baris 2-5: Solusi spesifik\n" +
        "   - Code block jika perlu\n" +
        "5. JANGAN:\n" +
        "   - Jangan bilang 'sepertinya' atau 'mungkin'\n" +
        "   - Jangan beri saran generic\n" +
        "   - Jangan terlalu panjang di awal\n\n" +
        "CONTOH RESPONS YANG BENAR:\n" +
        "Error: 'pkhj not found'\n" +
        "Masalah: Typo pada command. Kamu tulis 'pkhj' tapi seharusnya 'pkg'.\n" +
        "Fix: Jalankan ulang dengan command:\n" +
        "```\npkg install python\n```";
    
    private String apiKey;
    private Handler mainHandler;
    private Thread workerThread;
    private PersonaManager personaManager;
    
    public interface ChatCallback {
        void onSuccess(String response);
        void onError(String error);
    }
    
    public GroqApiService(String apiKey) {
        this.apiKey = apiKey;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public void setPersonaManager(PersonaManager pm) {
        this.personaManager = pm;
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
        
        String fullSystemPrompt = SYSTEM_PROMPT;
        if (personaManager != null) {
            String personaPrompt = personaManager.getActiveSystemPrompt();
            if (personaPrompt != null && !personaPrompt.isEmpty()) {
                fullSystemPrompt = SYSTEM_PROMPT + "\n\n" + "PERSONA INSTRUCTIONS:\n" + personaPrompt;
            }
        }
        
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", fullSystemPrompt);
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
            String content = msg.getContent();
            if (msg.hasImage()) {
                content = content.replace("[Gambar] ", "");
            }
            messageObj.put("content", content);
            messages.put(messageObj);
        }
        
        body.put("messages", messages);
        
        return body;
    }
    
    private JSONObject buildRequestBodyWithImage(List<Message> conversationHistory, String text, String imageBase64) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", VISION_MODEL);
        body.put("temperature", 0.5);
        body.put("max_tokens", 4096);
        body.put("top_p", 0.9);
        
        JSONArray messages = new JSONArray();
        
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", SYSTEM_PROMPT + "\n\n" + IMAGE_ANALYSIS_PROMPT);
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
        textContent.put("text", text + "\n\nAnalisis gambar ini dengan teliti. Identifikasi masalah utama dan berikan solusi spesifik.");
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
               content.equals("thinking");
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
