package com.xyra.ai;

import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;

public class TTSService implements TextToSpeech.OnInitListener {
    
    private static final String TAG = "TTSService";
    
    private Context context;
    private TextToSpeech tts;
    private boolean isInitialized = false;
    private Locale currentLocale = Locale.getDefault();
    private float speechRate = 1.0f;
    private float pitch = 1.0f;
    private TTSCallback callback;
    
    public interface TTSCallback {
        void onStart();
        void onDone();
        void onError(String error);
    }
    
    public TTSService(Context context) {
        this.context = context;
        this.tts = new TextToSpeech(context, this);
    }
    
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(currentLocale);
            
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(Locale.US);
            }
            
            isInitialized = true;
            
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    if (callback != null) {
                        callback.onStart();
                    }
                }
                
                @Override
                public void onDone(String utteranceId) {
                    if (callback != null) {
                        callback.onDone();
                    }
                }
                
                @Override
                public void onError(String utteranceId) {
                    if (callback != null) {
                        callback.onError("TTS Error occurred");
                    }
                }
            });
        } else {
            isInitialized = false;
        }
    }
    
    public void setCallback(TTSCallback callback) {
        this.callback = callback;
    }
    
    public void speak(String text) {
        if (!isInitialized || tts == null) {
            if (callback != null) {
                callback.onError("TTS not initialized");
            }
            return;
        }
        
        String cleanText = cleanTextForSpeech(text);
        
        tts.setSpeechRate(speechRate);
        tts.setPitch(pitch);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "messageId");
        } else {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "messageId");
            tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params);
        }
    }
    
    public void speakQueued(String text) {
        if (!isInitialized || tts == null) {
            return;
        }
        
        String cleanText = cleanTextForSpeech(text);
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(cleanText, TextToSpeech.QUEUE_ADD, null, "messageId_" + System.currentTimeMillis());
        } else {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "messageId_" + System.currentTimeMillis());
            tts.speak(cleanText, TextToSpeech.QUEUE_ADD, params);
        }
    }
    
    private String cleanTextForSpeech(String text) {
        if (text == null) return "";
        
        String cleaned = text.replaceAll("```[a-zA-Z]*\\n[\\s\\S]*?```", " code block ");
        cleaned = cleaned.replaceAll("`[^`]+`", " code ");
        cleaned = cleaned.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");
        cleaned = cleaned.replaceAll("\\*([^*]+)\\*", "$1");
        cleaned = cleaned.replaceAll("#+\\s*", "");
        cleaned = cleaned.replaceAll("\\[([^\\]]+)\\]\\([^)]+\\)", "$1");
        cleaned = cleaned.replaceAll("[-•]\\s+", "");
        cleaned = cleaned.replaceAll("\\d+\\.\\s+", "");
        cleaned = cleaned.replaceAll("\\s+", " ");
        cleaned = cleaned.trim();
        
        return cleaned;
    }
    
    public void stop() {
        if (tts != null) {
            tts.stop();
        }
    }
    
    public boolean isSpeaking() {
        return tts != null && tts.isSpeaking();
    }
    
    public void setLanguage(String languageCode) {
        if (!isInitialized || tts == null) return;
        
        Locale locale;
        switch (languageCode.toLowerCase()) {
            case "id":
            case "indonesian":
                locale = new Locale("id", "ID");
                break;
            case "en":
            case "english":
                locale = Locale.US;
                break;
            case "ja":
            case "japanese":
                locale = Locale.JAPANESE;
                break;
            case "zh":
            case "chinese":
                locale = Locale.CHINESE;
                break;
            case "ko":
            case "korean":
                locale = Locale.KOREAN;
                break;
            case "de":
            case "german":
                locale = Locale.GERMAN;
                break;
            case "fr":
            case "french":
                locale = Locale.FRENCH;
                break;
            case "es":
            case "spanish":
                locale = new Locale("es", "ES");
                break;
            case "pt":
            case "portuguese":
                locale = new Locale("pt", "BR");
                break;
            case "ar":
            case "arabic":
                locale = new Locale("ar");
                break;
            case "hi":
            case "hindi":
                locale = new Locale("hi", "IN");
                break;
            default:
                locale = Locale.getDefault();
        }
        
        int result = tts.setLanguage(locale);
        if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
            currentLocale = locale;
        }
    }
    
    public void autoDetectLanguage(String text) {
        if (text == null || text.isEmpty()) return;
        
        String sample = text.length() > 100 ? text.substring(0, 100) : text;
        
        if (sample.matches(".*[\\u4e00-\\u9fa5]+.*")) {
            setLanguage("zh");
        } else if (sample.matches(".*[\\u3040-\\u30ff]+.*")) {
            setLanguage("ja");
        } else if (sample.matches(".*[\\uac00-\\ud7a3]+.*")) {
            setLanguage("ko");
        } else if (sample.matches(".*[\\u0600-\\u06FF]+.*")) {
            setLanguage("ar");
        } else if (sample.matches(".*[\\u0900-\\u097F]+.*")) {
            setLanguage("hi");
        } else if (sample.matches("(?i).*(aku|saya|kamu|adalah|dan|yang|untuk|dengan|tidak|ini|itu).*")) {
            setLanguage("id");
        } else {
            setLanguage("en");
        }
    }
    
    public void setSpeechRate(float rate) {
        this.speechRate = Math.max(0.5f, Math.min(2.0f, rate));
    }
    
    public void setPitch(float pitch) {
        this.pitch = Math.max(0.5f, Math.min(2.0f, pitch));
    }
    
    public float getSpeechRate() {
        return speechRate;
    }
    
    public float getPitch() {
        return pitch;
    }
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    public Locale getCurrentLocale() {
        return currentLocale;
    }
    
    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        isInitialized = false;
    }
}
