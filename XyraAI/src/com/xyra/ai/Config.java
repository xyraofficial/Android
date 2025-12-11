package com.xyra.ai;

public class Config {
    public static final String CONFIG_URL = "https://raw.githubusercontent.com/xyraofficial/Android/refs/heads/main/configurasi/config.json";
    
    public static String GROQ_API_KEY = ""; // Will be loaded from remote config

    public static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";

    public static final String GROQ_MODEL = "llama-3.3-70b-versatile";

    public static final double TEMPERATURE = 0.7;
    public static final int MAX_TOKENS = 2048;
    public static final int CONNECT_TIMEOUT = 30000;
    public static final int READ_TIMEOUT = 60000;
}