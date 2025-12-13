package com.xyra.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookmarksActivity extends Activity {
    
    private ListView listBookmarks;
    private ImageButton btnBack;
    private LinearLayout emptyState;
    
    private BookmarkManager bookmarkManager;
    private BookmarkAdapter adapter;
    private SimpleDateFormat dateFormat;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_bookmarks);
        
        bookmarkManager = new BookmarkManager(this);
        dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
        
        initViews();
        setupListeners();
        loadBookmarks();
    }
    
    private void initViews() {
        listBookmarks = (ListView) findViewById(R.id.bookmarkList);
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        emptyState = (LinearLayout) findViewById(R.id.emptyState);
    }
    
    private void setupListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                }
            });
        }
        
        listBookmarks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BookmarkManager.BookmarkItem item = adapter.getItem(position);
                showBookmarkDetail(item);
            }
        });
        
        listBookmarks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                BookmarkManager.BookmarkItem item = adapter.getItem(position);
                showDeleteDialog(item);
                return true;
            }
        });
    }
    
    private void loadBookmarks() {
        List<BookmarkManager.BookmarkItem> bookmarks = bookmarkManager.getAllBookmarks();
        
        if (bookmarks.isEmpty()) {
            listBookmarks.setVisibility(View.GONE);
            if (emptyState != null) {
                emptyState.setVisibility(View.VISIBLE);
            }
        } else {
            listBookmarks.setVisibility(View.VISIBLE);
            if (emptyState != null) {
                emptyState.setVisibility(View.GONE);
            }
            adapter = new BookmarkAdapter(bookmarks);
            listBookmarks.setAdapter(adapter);
        }
    }
    
    private void showBookmarkDetail(BookmarkManager.BookmarkItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bookmark");
        
        String message = item.content;
        if (item.note != null && !item.note.isEmpty()) {
            message += "\n\n--- Catatan ---\n" + item.note;
        }
        message += "\n\n--- Ditandai ---\n" + dateFormat.format(new Date(item.bookmarkedAt));
        
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.setNeutralButton("Salin", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
                    getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("Bookmark", item.content);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(BookmarksActivity.this, "Disalin ke clipboard", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
    
    private void showDeleteDialog(final BookmarkManager.BookmarkItem item) {
        new AlertDialog.Builder(this)
            .setTitle("Hapus Bookmark")
            .setMessage("Hapus bookmark ini?")
            .setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    bookmarkManager.removeBookmark(item.id);
                    loadBookmarks();
                    Toast.makeText(BookmarksActivity.this, "Bookmark dihapus", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }
    
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    private class BookmarkAdapter extends BaseAdapter {
        private List<BookmarkManager.BookmarkItem> bookmarks;
        
        public BookmarkAdapter(List<BookmarkManager.BookmarkItem> bookmarks) {
            this.bookmarks = bookmarks;
        }
        
        @Override
        public int getCount() {
            return bookmarks.size();
        }
        
        @Override
        public BookmarkManager.BookmarkItem getItem(int position) {
            return bookmarks.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(BookmarksActivity.this)
                    .inflate(R.layout.item_bookmark, parent, false);
            }
            
            BookmarkManager.BookmarkItem item = getItem(position);
            
            TextView tvContent = (TextView) convertView.findViewById(R.id.tvContent);
            TextView tvTime = (TextView) convertView.findViewById(R.id.tvTime);
            TextView tvNote = (TextView) convertView.findViewById(R.id.tvNote);
            ImageButton btnDelete = (ImageButton) convertView.findViewById(R.id.btnDelete);
            
            String preview = item.content;
            if (preview.length() > 100) {
                preview = preview.substring(0, 100) + "...";
            }
            tvContent.setText(preview);
            if (tvTime != null) {
                tvTime.setText(dateFormat.format(new Date(item.bookmarkedAt)));
            }
            
            if (tvNote != null) {
                if (item.note != null && !item.note.isEmpty()) {
                    tvNote.setVisibility(View.VISIBLE);
                    tvNote.setText(item.note);
                } else {
                    tvNote.setVisibility(View.GONE);
                }
            }
            
            if (btnDelete != null) {
                final BookmarkManager.BookmarkItem finalItem = item;
                btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDeleteDialog(finalItem);
                    }
                });
            }
            
            return convertView;
        }
    }
}
