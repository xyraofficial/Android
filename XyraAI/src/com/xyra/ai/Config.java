package com.xyra.ai;

public class Config {
    public static final String GROQ_API_KEY = "YOUR_GROQ_API_KEY_HERE";
    
    public static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    
    public static final String GROQ_MODEL = "llama-3.3-70b-versatile";
    
    public static final double TEMPERATURE = 0.7;
    public static final int MAX_TOKENS = 2048;
    public static final int CONNECT_TIMEOUT = 30000;
    public static final int READ_TIMEOUT = 60000;
}
