package com.xyra.ai;

import android.graphics.Bitmap;

public class Message {
    
    public static final int TYPE_USER = 0;
    public static final int TYPE_AI = 1;
    
    private String content;
    private int type;
    private long timestamp;
    private String imageBase64;
    private Bitmap imageBitmap;
    private String chatId;
    
    public Message(String content, int type) {
        this.content = content;
        this.type = type;
        this.timestamp = System.currentTimeMillis();
        this.imageBase64 = null;
        this.imageBitmap = null;
        this.chatId = null;
    }
    
    public Message(String content, int type, long timestamp) {
        this.content = content;
        this.type = type;
        this.timestamp = timestamp;
        this.imageBase64 = null;
        this.imageBitmap = null;
        this.chatId = null;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getImageBase64() {
        return imageBase64;
    }
    
    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }
    
    public Bitmap getImageBitmap() {
        return imageBitmap;
    }
    
    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }
    
    public boolean hasImage() {
        return imageBitmap != null || (imageBase64 != null && !imageBase64.isEmpty());
    }
    
    public String getChatId() {
        return chatId;
    }
    
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
