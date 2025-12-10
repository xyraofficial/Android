package com.xyra.ai;

import android.os.Handler;
import android.os.Looper;

public class TypingAnimator {
    
    private String fullText;
    private int currentIndex;
    private Handler handler;
    private TypingCallback callback;
    private boolean isAnimating;
    private int typingSpeed;
    
    private static final int DEFAULT_SPEED = 0;
    private static final int FAST_SPEED = 0;
    private static final int SLOW_SPEED = 0;
    private static final int CHUNK_SIZE = 50;
    
    public interface TypingCallback {
        void onTextUpdated(String currentText);
        void onTypingComplete(String fullText);
    }
    
    public TypingAnimator() {
        this.handler = new Handler(Looper.getMainLooper());
        this.typingSpeed = DEFAULT_SPEED;
        this.isAnimating = false;
    }
    
    public void startTyping(String text, TypingCallback callback) {
        if (text == null || text.isEmpty()) {
            if (callback != null) {
                callback.onTypingComplete("");
            }
            return;
        }
        
        stopTyping();
        
        this.fullText = text;
        this.callback = callback;
        this.currentIndex = 0;
        this.isAnimating = true;
        
        typeNextCharacter();
    }
    
    private void typeNextCharacter() {
        if (!isAnimating || currentIndex >= fullText.length()) {
            if (callback != null && isAnimating) {
                callback.onTypingComplete(fullText);
            }
            isAnimating = false;
            return;
        }
        
        int charsToAdd = Math.min(CHUNK_SIZE, fullText.length() - currentIndex);
        currentIndex += charsToAdd;
        
        int speed = calculateSpeed();
        
        String currentText = fullText.substring(0, currentIndex);
        
        if (callback != null) {
            callback.onTextUpdated(currentText);
        }
        
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                typeNextCharacter();
            }
        }, speed);
    }
    
    private int calculateSpeed() {
        if (currentIndex >= fullText.length()) {
            return typingSpeed;
        }
        
        char currentChar = fullText.charAt(currentIndex - 1);
        
        if (currentChar == '\n') {
            return SLOW_SPEED;
        } else if (currentChar == '.' || currentChar == '!' || currentChar == '?') {
            return SLOW_SPEED;
        } else if (currentChar == ',' || currentChar == ';' || currentChar == ':') {
            return FAST_SPEED;
        } else if (currentChar == ' ') {
            return FAST_SPEED;
        }
        
        if (isInsideCodeBlock()) {
            return FAST_SPEED;
        }
        
        return typingSpeed;
    }
    
    private boolean isInsideCodeBlock() {
        String textSoFar = fullText.substring(0, currentIndex);
        int codeBlockCount = countOccurrences(textSoFar, "```");
        return codeBlockCount % 2 == 1;
    }
    
    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
    
    public void stopTyping() {
        isAnimating = false;
        handler.removeCallbacksAndMessages(null);
    }
    
    public void skipToEnd() {
        if (isAnimating && fullText != null && callback != null) {
            stopTyping();
            callback.onTextUpdated(fullText);
            callback.onTypingComplete(fullText);
        }
    }
    
    public void setTypingSpeed(int speed) {
        this.typingSpeed = speed;
    }
    
    public boolean isAnimating() {
        return isAnimating;
    }
}
