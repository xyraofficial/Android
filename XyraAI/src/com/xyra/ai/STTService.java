package com.xyra.ai;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Locale;

public class STTService {
    
    private static final String TAG = "STTService";
    
    private Context context;
    private SpeechRecognizer speechRecognizer;
    private Intent recognizerIntent;
    private boolean isListening = false;
    private STTCallback callback;
    private String currentLanguage = "id-ID";
    
    public interface STTCallback {
        void onReadyForSpeech();
        void onBeginningOfSpeech();
        void onEndOfSpeech();
        void onResults(String result);
        void onPartialResults(String partialResult);
        void onError(String error);
        void onRmsChanged(float rmsDb);
    }
    
    public STTService(Context context) {
        this.context = context;
        initSpeechRecognizer();
    }
    
    private void initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            setupRecognizerIntent();
            setupRecognitionListener();
        }
    }
    
    private void setupRecognizerIntent() {
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLanguage);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 5000);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500);
    }
    
    private void setupRecognitionListener() {
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                isListening = true;
                if (callback != null) {
                    callback.onReadyForSpeech();
                }
            }
            
            @Override
            public void onBeginningOfSpeech() {
                if (callback != null) {
                    callback.onBeginningOfSpeech();
                }
            }
            
            @Override
            public void onRmsChanged(float rmsdB) {
                if (callback != null) {
                    callback.onRmsChanged(rmsdB);
                }
            }
            
            @Override
            public void onBufferReceived(byte[] buffer) {
            }
            
            @Override
            public void onEndOfSpeech() {
                isListening = false;
                if (callback != null) {
                    callback.onEndOfSpeech();
                }
            }
            
            @Override
            public void onError(int error) {
                isListening = false;
                String errorMessage = getErrorMessage(error);
                if (callback != null) {
                    callback.onError(errorMessage);
                }
            }
            
            @Override
            public void onResults(Bundle results) {
                isListening = false;
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String result = matches.get(0);
                    if (callback != null) {
                        callback.onResults(result);
                    }
                }
            }
            
            @Override
            public void onPartialResults(Bundle partialResults) {
                ArrayList<String> matches = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String partialResult = matches.get(0);
                    if (callback != null) {
                        callback.onPartialResults(partialResult);
                    }
                }
            }
            
            @Override
            public void onEvent(int eventType, Bundle params) {
            }
        });
    }
    
    private String getErrorMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No speech detected";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                return "Unknown error";
        }
    }
    
    public void setCallback(STTCallback callback) {
        this.callback = callback;
    }
    
    public void startListening() {
        if (speechRecognizer == null) {
            if (callback != null) {
                callback.onError("Speech recognition not available");
            }
            return;
        }
        
        if (isListening) {
            stopListening();
        }
        
        setupRecognizerIntent();
        speechRecognizer.startListening(recognizerIntent);
    }
    
    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            isListening = false;
        }
    }
    
    public void cancelListening() {
        if (speechRecognizer != null) {
            speechRecognizer.cancel();
            isListening = false;
        }
    }
    
    public boolean isListening() {
        return isListening;
    }
    
    public void setLanguage(String languageCode) {
        switch (languageCode.toLowerCase()) {
            case "id":
            case "indonesian":
                currentLanguage = "id-ID";
                break;
            case "en":
            case "english":
                currentLanguage = "en-US";
                break;
            case "ja":
            case "japanese":
                currentLanguage = "ja-JP";
                break;
            case "zh":
            case "chinese":
                currentLanguage = "zh-CN";
                break;
            case "ko":
            case "korean":
                currentLanguage = "ko-KR";
                break;
            case "de":
            case "german":
                currentLanguage = "de-DE";
                break;
            case "fr":
            case "french":
                currentLanguage = "fr-FR";
                break;
            case "es":
            case "spanish":
                currentLanguage = "es-ES";
                break;
            case "pt":
            case "portuguese":
                currentLanguage = "pt-BR";
                break;
            case "ar":
            case "arabic":
                currentLanguage = "ar-SA";
                break;
            case "hi":
            case "hindi":
                currentLanguage = "hi-IN";
                break;
            default:
                currentLanguage = languageCode;
        }
        setupRecognizerIntent();
    }
    
    public String getCurrentLanguage() {
        return currentLanguage;
    }
    
    public static boolean isAvailable(Context context) {
        return SpeechRecognizer.isRecognitionAvailable(context);
    }
    
    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
        isListening = false;
    }
    
    public void shutdown() {
        destroy();
    }
}
