package com.xyra.ai;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BookmarkManager {
    
    private static final String TAG = "BookmarkManager";
    private static final String PREFS_NAME = "xyra_bookmarks";
    private static final String KEY_BOOKMARKS = "bookmarks";
    
    private Context context;
    private SharedPreferences prefs;
    private List<BookmarkItem> bookmarks;
    
    public static class BookmarkItem {
        public String id;
        public String chatId;
        public String content;
        public int messageType;
        public long timestamp;
        public long bookmarkedAt;
        public String note;
        
        public BookmarkItem() {
            this.id = String.valueOf(System.currentTimeMillis());
            this.bookmarkedAt = System.currentTimeMillis();
        }
    }
    
    public BookmarkManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.bookmarks = new ArrayList<BookmarkItem>();
        loadBookmarks();
    }
    
    private void loadBookmarks() {
        try {
            String json = prefs.getString(KEY_BOOKMARKS, "[]");
            JSONArray array = new JSONArray(json);
            
            bookmarks.clear();
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                BookmarkItem item = new BookmarkItem();
                item.id = obj.optString("id", "");
                item.chatId = obj.optString("chatId", "");
                item.content = obj.optString("content", "");
                item.messageType = obj.optInt("messageType", 0);
                item.timestamp = obj.optLong("timestamp", 0);
                item.bookmarkedAt = obj.optLong("bookmarkedAt", 0);
                item.note = obj.optString("note", "");
                bookmarks.add(item);
            }
        } catch (Exception e) {
            bookmarks = new ArrayList<BookmarkItem>();
        }
    }
    
    private void saveBookmarks() {
        try {
            JSONArray array = new JSONArray();
            for (BookmarkItem item : bookmarks) {
                JSONObject obj = new JSONObject();
                obj.put("id", item.id);
                obj.put("chatId", item.chatId);
                obj.put("content", item.content);
                obj.put("messageType", item.messageType);
                obj.put("timestamp", item.timestamp);
                obj.put("bookmarkedAt", item.bookmarkedAt);
                obj.put("note", item.note);
                array.put(obj);
            }
            
            prefs.edit().putString(KEY_BOOKMARKS, array.toString()).apply();
        } catch (Exception e) {
        }
    }
    
    public void addBookmark(Message message, String chatId) {
        addBookmark(message, chatId, "");
    }
    
    public void addBookmark(Message message, String chatId, String note) {
        if (isBookmarked(message, chatId)) {
            return;
        }
        
        BookmarkItem item = new BookmarkItem();
        item.chatId = chatId;
        item.content = message.getContent();
        item.messageType = message.getType();
        item.timestamp = message.getTimestamp();
        item.note = note;
        
        bookmarks.add(0, item);
        saveBookmarks();
    }
    
    public void removeBookmark(String bookmarkId) {
        for (int i = 0; i < bookmarks.size(); i++) {
            if (bookmarks.get(i).id.equals(bookmarkId)) {
                bookmarks.remove(i);
                saveBookmarks();
                return;
            }
        }
    }
    
    public void removeBookmarkByMessage(Message message, String chatId) {
        for (int i = 0; i < bookmarks.size(); i++) {
            BookmarkItem item = bookmarks.get(i);
            if (item.chatId.equals(chatId) && 
                item.content.equals(message.getContent()) && 
                item.timestamp == message.getTimestamp()) {
                bookmarks.remove(i);
                saveBookmarks();
                return;
            }
        }
    }
    
    public boolean isBookmarked(Message message, String chatId) {
        for (BookmarkItem item : bookmarks) {
            if (item.chatId.equals(chatId) && 
                item.content.equals(message.getContent()) && 
                item.timestamp == message.getTimestamp()) {
                return true;
            }
        }
        return false;
    }
    
    public void toggleBookmark(Message message, String chatId) {
        if (isBookmarked(message, chatId)) {
            removeBookmarkByMessage(message, chatId);
        } else {
            addBookmark(message, chatId);
        }
    }
    
    public List<BookmarkItem> getAllBookmarks() {
        return new ArrayList<BookmarkItem>(bookmarks);
    }
    
    public List<BookmarkItem> getBookmarksByChatId(String chatId) {
        List<BookmarkItem> result = new ArrayList<BookmarkItem>();
        for (BookmarkItem item : bookmarks) {
            if (item.chatId.equals(chatId)) {
                result.add(item);
            }
        }
        return result;
    }
    
    public List<BookmarkItem> searchBookmarks(String query) {
        List<BookmarkItem> result = new ArrayList<BookmarkItem>();
        String lowerQuery = query.toLowerCase();
        
        for (BookmarkItem item : bookmarks) {
            if (item.content.toLowerCase().contains(lowerQuery) ||
                (item.note != null && item.note.toLowerCase().contains(lowerQuery))) {
                result.add(item);
            }
        }
        return result;
    }
    
    public void updateNote(String bookmarkId, String note) {
        for (BookmarkItem item : bookmarks) {
            if (item.id.equals(bookmarkId)) {
                item.note = note;
                saveBookmarks();
                return;
            }
        }
    }
    
    public int getBookmarkCount() {
        return bookmarks.size();
    }
    
    public void clearAllBookmarks() {
        bookmarks.clear();
        saveBookmarks();
    }
}
