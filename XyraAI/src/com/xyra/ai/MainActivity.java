package com.xyra.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.os.Environment;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    
    private static final String API_KEY = "YOUR_GROQ_API_KEY_HERE";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAMERA_REQUEST = 2;
    private static final int FILE_REQUEST = 3;
    private static final int PERMISSION_REQUEST = 100;
    
    private ListView listView;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnMenu;
    private ImageButton btnImage;
    private ImageButton btnDrawer;
    private TextView tvStatus;
    private TextView tvTyping;
    private LinearLayout imagePreviewContainer;
    private ImageView ivPreview;
    private ImageButton btnRemoveImage;
    
    private LinearLayout drawerLayout;
    private View drawerOverlay;
    private EditText etSearch;
    private ListView chatHistoryList;
    private LinearLayout btnNewChat;
    private LinearLayout btnSettings;
    
    private ChatAdapter chatAdapter;
    private GroqApiService groqApiService;
    private ChatHistory chatHistory;
    private ChatHistoryAdapter chatHistoryAdapter;
    private boolean isWaitingResponse = false;
    private boolean isDrawerOpen = false;
    
    private String selectedImageBase64 = null;
    private Uri selectedImageUri = null;
    private Bitmap selectedImageBitmap = null;
    private boolean userScrolledUp = false;
    
    private FrameLayout fullscreenImageContainer;
    private ImageView ivFullscreenImage;
    private ImageButton btnCloseFullscreen;
    
    private GestureDetector gestureDetector;
    
    private FrameLayout avatarSection;
    private ImageView ivAvatar;
    private View avatarGlow;
    private AvatarAnimator avatarAnimator;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_main);
        
        initViews();
        applyThemeColors();
        setupListView();
        setupDrawer();
        setupClickListeners();
        setupSwipeGesture();
        initGroqService();
        initChatHistory();
        
        addWelcomeMessage();
    }
    
    private void applyThemeColors() {
        ThemeManager.ThemeColors colors = ThemeManager.getThemeColors(this);
        
        View mainContent = findViewById(R.id.mainContent);
        if (mainContent != null) {
            mainContent.setBackgroundColor(colors.background);
        }
        
        if (drawerLayout != null) {
            drawerLayout.setBackgroundColor(colors.drawerBackground);
        }
        
        View header = findViewById(R.id.header);
        if (header != null) {
            header.setBackgroundColor(colors.headerBackground);
        }
        
        View inputArea = findViewById(R.id.inputArea);
        if (inputArea != null) {
            inputArea.setBackgroundColor(colors.surface);
        }
        
        if (etMessage != null) {
            etMessage.setBackgroundColor(colors.inputBackground);
            etMessage.setTextColor(colors.textPrimary);
            etMessage.setHintTextColor(colors.textSecondary);
        }
        
        if (tvStatus != null) {
            tvStatus.setTextColor(colors.textSecondary);
        }
        
        if (tvTyping != null) {
            tvTyping.setTextColor(colors.textSecondary);
        }
        
        if (etSearch != null) {
            etSearch.setBackgroundColor(colors.inputBackground);
            etSearch.setTextColor(colors.textPrimary);
            etSearch.setHintTextColor(colors.textSecondary);
        }
        
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        if (tvTitle != null) {
            tvTitle.setTextColor(colors.textPrimary);
        }
        
        if (listView != null) {
            listView.setBackgroundColor(colors.background);
        }
        
        if (chatAdapter != null) {
            chatAdapter.setThemeColors(colors);
        }
    }
    
    private void initViews() {
        listView = (ListView) findViewById(R.id.listView);
        etMessage = (EditText) findViewById(R.id.etMessage);
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnMenu = (ImageButton) findViewById(R.id.btnMenu);
        btnImage = (ImageButton) findViewById(R.id.btnImage);
        btnDrawer = (ImageButton) findViewById(R.id.btnDrawer);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvTyping = (TextView) findViewById(R.id.tvTyping);
        
        imagePreviewContainer = (LinearLayout) findViewById(R.id.imagePreviewContainer);
        if (imagePreviewContainer != null) {
            ivPreview = (ImageView) findViewById(R.id.ivPreview);
            btnRemoveImage = (ImageButton) findViewById(R.id.btnRemoveImage);
        }
        
        drawerLayout = (LinearLayout) findViewById(R.id.drawerLayout);
        drawerOverlay = findViewById(R.id.drawerOverlay);
        etSearch = (EditText) findViewById(R.id.etSearch);
        chatHistoryList = (ListView) findViewById(R.id.chatHistoryList);
        btnNewChat = (LinearLayout) findViewById(R.id.btnNewChat);
        btnSettings = (LinearLayout) findViewById(R.id.btnSettings);
        
        fullscreenImageContainer = (FrameLayout) findViewById(R.id.fullscreenImageContainer);
        ivFullscreenImage = (ImageView) findViewById(R.id.ivFullscreenImage);
        btnCloseFullscreen = (ImageButton) findViewById(R.id.btnCloseFullscreen);
        
        avatarSection = (FrameLayout) findViewById(R.id.avatarSection);
        ivAvatar = (ImageView) findViewById(R.id.ivAvatar);
        avatarGlow = findViewById(R.id.avatarGlow);
        
        if (ivAvatar != null) {
            avatarAnimator = new AvatarAnimator(ivAvatar);
        }
    }
    
    private void setupListView() {
        chatAdapter = new ChatAdapter(this);
        listView.setAdapter(chatAdapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showMessageOptions(position);
                return true;
            }
        });
        
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (chatAdapter.isTyping()) {
                    chatAdapter.skipTypingAnimation();
                }
            }
        });
        
        listView.setOnScrollListener(new android.widget.AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(android.widget.AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    userScrolledUp = true;
                }
            }
            
            @Override
            public void onScroll(android.widget.AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
                    userScrolledUp = false;
                }
            }
        });
    }
    
    private void setupDrawer() {
        chatHistoryAdapter = new ChatHistoryAdapter(this);
        chatHistoryList.setAdapter(chatHistoryAdapter);
        
        chatHistoryAdapter.setOnChatItemClickListener(new ChatHistoryAdapter.OnChatItemClickListener() {
            @Override
            public void onChatClick(ChatHistory.ChatItem item) {
                loadChatFromHistory(item);
            }
            
            @Override
            public void onChatDelete(ChatHistory.ChatItem item) {
                confirmDeleteChat(item);
            }
        });
        
        drawerOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer();
            }
        });
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadChatFromHistory(ChatHistory.ChatItem item) {
        if (isWaitingResponse) {
            Toast.makeText(this, "Tunggu respons selesai", Toast.LENGTH_SHORT).show();
            return;
        }
        
        saveChat();
        
        chatHistory.loadChat(item.id);
        List<Message> messages = chatHistory.loadMessagesForChat(item.id);
        chatAdapter.setMessages(messages, item.id);
        closeDrawer();
        
        if (messages.isEmpty()) {
            addWelcomeMessage();
        }
    }
    
    private void confirmDeleteChat(final ChatHistory.ChatItem item) {
        new AlertDialog.Builder(this)
            .setTitle("Hapus Chat")
            .setMessage("Hapus chat \"" + item.preview + "\"?")
            .setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    boolean isCurrentChat = item.id.equals(chatAdapter.getCurrentChatId());
                    chatHistory.deleteChat(item.id);
                    refreshChatHistoryList();
                    
                    if (isCurrentChat) {
                        chatAdapter.clearMessages();
                        chatHistory.startNewChat();
                        chatAdapter.setCurrentChatId(chatHistory.getCurrentChatId());
                        clearSelectedImage();
                        addWelcomeMessage();
                        closeDrawer();
                        Toast.makeText(MainActivity.this, "Chat dihapus, chat baru dimulai", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Chat dihapus", Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }
    
    private void filterChats(String query) {
        if (query.isEmpty()) {
            chatHistoryAdapter.setItems(chatHistory.getChatList());
        } else {
            chatHistoryAdapter.setItems(chatHistory.searchChats(query));
        }
    }
    
    private void refreshChatHistoryList() {
        chatHistoryAdapter.setItems(chatHistory.getChatList());
    }
    
    private void setupSwipeGesture() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;
            
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) return false;
                
                float diffX = e2.getX() - e1.getX();
                float diffY = e2.getY() - e1.getY();
                
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            if (!isDrawerOpen) {
                                openDrawer();
                            }
                        } else {
                            if (isDrawerOpen) {
                                closeDrawer();
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
        });
        
        View mainContent = findViewById(R.id.mainContent);
        if (mainContent != null) {
            mainContent.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(ev);
        }
        return super.dispatchTouchEvent(ev);
    }
    
    private void openDrawer() {
        isDrawerOpen = true;
        refreshChatHistoryList();
        
        drawerOverlay.setVisibility(View.VISIBLE);
        drawerOverlay.animate().alpha(1f).setDuration(200);
        
        drawerLayout.animate()
            .translationX(0)
            .setDuration(250)
            .start();
    }
    
    private void closeDrawer() {
        isDrawerOpen = false;
        chatHistoryAdapter.setDeleteMode(false);
        
        drawerOverlay.animate().alpha(0f).setDuration(200).withEndAction(new Runnable() {
            @Override
            public void run() {
                drawerOverlay.setVisibility(View.GONE);
            }
        });
        
        drawerLayout.animate()
            .translationX(-280 * getResources().getDisplayMetrics().density)
            .setDuration(250)
            .start();
    }
    
    private void showMessageOptions(final int position) {
        final Message message = (Message) chatAdapter.getItem(position);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opsi");
        
        String[] options = {"Salin Teks", "Bagikan"};
        
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        copyToClipboard(message.getContent());
                        break;
                    case 1:
                        shareText(message.getContent());
                        break;
                }
            }
        });
        
        builder.show();
    }
    
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("XyraAI", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "Disalin ke clipboard", Toast.LENGTH_SHORT).show();
    }
    
    private void shareText(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Bagikan via"));
    }
    
    private void setupClickListeners() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMainMenu();
            }
        });
        
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });
        
        btnDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDrawerOpen) {
                    closeDrawer();
                } else {
                    openDrawer();
                }
            }
        });
        
        if (btnRemoveImage != null) {
            btnRemoveImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearSelectedImage();
                }
            });
        }
        
        if (btnCloseFullscreen != null) {
            btnCloseFullscreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeFullscreenImage();
                }
            });
        }
        
        if (fullscreenImageContainer != null) {
            fullscreenImageContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeFullscreenImage();
                }
            });
        }
        
        btnNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNewChat();
                closeDrawer();
            }
        });
        
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDrawer();
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }
    
    private void openImagePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Sumber Gambar");
        
        String[] options = {"Kamera", "Galeri Foto", "File"};
        
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        openCamera();
                        break;
                    case 1:
                        openGallery();
                        break;
                    case 2:
                        openFilePicker();
                        break;
                }
            }
        });
        
        builder.show();
    }
    
    private void openCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST);
                return;
            }
        }
        
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, CAMERA_REQUEST);
        } else {
            Toast.makeText(this, "Kamera tidak tersedia", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    
    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Pilih File Gambar"), FILE_REQUEST);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode != RESULT_OK) {
            return;
        }
        
        Bitmap bitmap = null;
        
        if (requestCode == CAMERA_REQUEST && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null) {
                bitmap = (Bitmap) extras.get("data");
            }
        } else if ((requestCode == PICK_IMAGE_REQUEST || requestCode == FILE_REQUEST) && data != null) {
            selectedImageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (Exception e) {
                Toast.makeText(this, "Gagal memuat gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        
        if (bitmap != null) {
            try {
                Bitmap scaledBitmap = scaleBitmap(bitmap, 1024);
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 95, baos);
                byte[] imageBytes = baos.toByteArray();
                selectedImageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                selectedImageBitmap = scaledBitmap;
                
                showImagePreview(scaledBitmap);
                
                Toast.makeText(this, "Gambar dipilih! Tulis pertanyaan tentang gambar ini.", Toast.LENGTH_SHORT).show();
                
            } catch (Exception e) {
                Toast.makeText(this, "Gagal memuat gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                selectedImageBase64 = null;
                selectedImageUri = null;
                selectedImageBitmap = null;
            }
        }
    }
    
    private Bitmap scaleBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        if (width <= maxSize && height <= maxSize) {
            return bitmap;
        }
        
        float scale = Math.min((float) maxSize / width, (float) maxSize / height);
        int newWidth = Math.round(width * scale);
        int newHeight = Math.round(height * scale);
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }
    
    private void showImagePreview(Bitmap bitmap) {
        if (imagePreviewContainer != null && ivPreview != null) {
            ivPreview.setImageBitmap(bitmap);
            imagePreviewContainer.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Gambar siap dikirim", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void clearSelectedImage() {
        selectedImageBase64 = null;
        selectedImageUri = null;
        selectedImageBitmap = null;
        if (imagePreviewContainer != null) {
            imagePreviewContainer.setVisibility(View.GONE);
        }
    }
    
    public void showFullscreenImage(Bitmap bitmap) {
        if (fullscreenImageContainer != null && ivFullscreenImage != null && bitmap != null) {
            ivFullscreenImage.setImageBitmap(bitmap);
            fullscreenImageContainer.setVisibility(View.VISIBLE);
            fullscreenImageContainer.animate().alpha(1f).setDuration(200).start();
        }
    }
    
    private void closeFullscreenImage() {
        if (fullscreenImageContainer != null) {
            fullscreenImageContainer.animate().alpha(0f).setDuration(200).withEndAction(new Runnable() {
                @Override
                public void run() {
                    fullscreenImageContainer.setVisibility(View.GONE);
                }
            }).start();
        }
    }
    
    private void showMainMenu() {
        PopupMenu popup = new PopupMenu(this, btnMenu);
        popup.getMenu().add(0, 1, 0, "Chat Baru");
        popup.getMenu().add(0, 2, 1, "Pengaturan");
        popup.getMenu().add(0, 3, 2, "Tentang");
        
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1:
                        startNewChat();
                        return true;
                    case 2:
                        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        return true;
                    case 3:
                        showAbout();
                        return true;
                }
                return false;
            }
        });
        
        popup.show();
    }
    
    private void startNewChat() {
        if (isWaitingResponse) {
            Toast.makeText(this, "Tunggu respons selesai", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (chatAdapter.getCount() > 0) {
            saveChat();
        }
        
        chatAdapter.clearMessages();
        chatHistory.startNewChat();
        chatAdapter.setCurrentChatId(chatHistory.getCurrentChatId());
        clearSelectedImage();
        showAvatarSection(true);
        addWelcomeMessage();
        Toast.makeText(this, "Chat baru dimulai", Toast.LENGTH_SHORT).show();
    }
    
    private void confirmClearHistory() {
        new AlertDialog.Builder(this)
            .setTitle("Hapus Riwayat")
            .setMessage("Apakah Anda yakin ingin menghapus semua riwayat chat?")
            .setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startNewChat();
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }
    
    private void showAbout() {
        new AlertDialog.Builder(this)
            .setTitle("Tentang XyraAI")
            .setMessage("XyraAI v1.2\n\n" +
                "Powered by GROQ API dengan Llama 3.3 70B\n\n" +
                "Fitur:\n" +
                "- Multi-bahasa otomatis\n" +
                "- Animasi mengetik\n" +
                "- Analisis gambar dengan preview\n" +
                "- Bantuan kode\n" +
                "- Riwayat chat tersimpan\n" +
                "- UI modern dan responsif")
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void initGroqService() {
        groqApiService = new GroqApiService(API_KEY);
    }
    
    private void initChatHistory() {
        chatHistory = new ChatHistory(this);
        chatAdapter.setCurrentChatId(chatHistory.getCurrentChatId());
        
        List<Message> savedMessages = chatHistory.loadMessages();
        if (!savedMessages.isEmpty()) {
            chatAdapter.setMessages(savedMessages, chatHistory.getCurrentChatId());
        }
    }
    
    private void addWelcomeMessage() {
        if (chatAdapter.getCount() == 0) {
            showAvatarSection(true);
            String welcome = getString(R.string.welcome_message);
            chatAdapter.addMessage(new Message(welcome, Message.TYPE_AI));
            if (avatarAnimator != null) {
                avatarAnimator.startWaveAnimation();
            }
        }
    }
    
    private void showAvatarSection(boolean show) {
        if (avatarSection != null) {
            avatarSection.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    
    private void sendMessage() {
        if (isWaitingResponse) {
            return;
        }
        
        String messageText = etMessage.getText().toString().trim();
        
        if (TextUtils.isEmpty(messageText) && selectedImageBase64 == null) {
            return;
        }
        
        String displayMessage = messageText;
        if (selectedImageBase64 != null) {
            displayMessage = TextUtils.isEmpty(messageText) ? "Analisis gambar ini" : messageText;
        }
        
        Message userMessage = new Message(displayMessage, Message.TYPE_USER);
        
        if (selectedImageBitmap != null) {
            userMessage.setImageBitmap(selectedImageBitmap);
            userMessage.setImageBase64(selectedImageBase64);
        }
        
        chatAdapter.addMessage(userMessage);
        forceScrollToBottom();
        
        etMessage.setText("");
        hideKeyboard();
        
        setWaitingState(true);
        
        chatAdapter.addMessage(new Message("thinking", Message.TYPE_AI));
        forceScrollToBottom();
        
        final String imageToSend = selectedImageBase64;
        final String textToSend = TextUtils.isEmpty(messageText) ? "Analisis gambar ini secara detail. Identifikasi masalah utama jika ada error atau bug, dan berikan solusi langsung." : messageText;
        final String requestChatId = chatAdapter.getCurrentChatId();
        final List<Message> requestMessages = new ArrayList<Message>(chatAdapter.getMessages());
        
        clearSelectedImage();
        
        if (imageToSend != null) {
            groqApiService.sendMessageWithImage(requestMessages, textToSend, imageToSend, new GroqApiService.ChatCallback() {
                @Override
                public void onSuccess(final String response) {
                    handleApiResponse(response, requestChatId, requestMessages);
                }
                
                @Override
                public void onError(String error) {
                    handleApiError(error, requestChatId);
                }
            });
        } else {
            groqApiService.sendMessage(requestMessages, new GroqApiService.ChatCallback() {
                @Override
                public void onSuccess(final String response) {
                    handleApiResponse(response, requestChatId, requestMessages);
                }
                
                @Override
                public void onError(String error) {
                    handleApiError(error, requestChatId);
                }
            });
        }
    }
    
    private void handleApiResponse(final String response, final String requestChatId, final List<Message> requestMessages) {
        boolean isCurrentChat = requestChatId.equals(chatAdapter.getCurrentChatId());
        
        if (isCurrentChat) {
            chatAdapter.updateLastMessageWithTyping(response, requestChatId, new Runnable() {
                @Override
                public void run() {
                    setWaitingState(false);
                    saveChat();
                    refreshChatHistoryList();
                }
            });
            scrollToBottom();
        } else {
            if (requestMessages.size() > 0) {
                Message lastMsg = requestMessages.get(requestMessages.size() - 1);
                lastMsg.setContent(response);
            }
            chatHistory.saveMessagesToChat(requestChatId, requestMessages);
            refreshChatHistoryList();
            setWaitingState(false);
        }
    }
    
    private void handleApiError(String error, String requestChatId) {
        boolean isCurrentChat = requestChatId.equals(chatAdapter.getCurrentChatId());
        
        if (isCurrentChat) {
            chatAdapter.updateLastMessage(getString(R.string.error_api) + "\n" + error, requestChatId);
            scrollToBottom();
        }
        setWaitingState(false);
    }
    
    private void saveChat() {
        chatHistory.saveMessages(chatAdapter.getMessages());
    }
    
    private void setWaitingState(boolean waiting) {
        isWaitingResponse = waiting;
        btnSend.setEnabled(!waiting);
        btnSend.setAlpha(waiting ? 0.5f : 1.0f);
        btnImage.setEnabled(!waiting);
        btnImage.setAlpha(waiting ? 0.5f : 1.0f);
        
        if (waiting) {
            tvStatus.setVisibility(View.GONE);
            tvTyping.setVisibility(View.VISIBLE);
            showAvatarSection(true);
            if (avatarAnimator != null) {
                avatarAnimator.startThinkingAnimation();
            }
        } else {
            tvStatus.setVisibility(View.VISIBLE);
            tvTyping.setVisibility(View.GONE);
            if (avatarAnimator != null) {
                avatarAnimator.stopAnimation();
            }
            if (chatAdapter.getCount() > 1) {
                showAvatarSection(false);
            }
        }
    }
    
    private void scrollToBottom() {
        if (userScrolledUp) {
            return;
        }
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(chatAdapter.getCount() - 1);
            }
        });
    }
    
    private void forceScrollToBottom() {
        userScrolledUp = false;
        listView.post(new Runnable() {
            @Override
            public void run() {
                listView.setSelection(chatAdapter.getCount() - 1);
            }
        });
    }
    
    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        ThemeManager.applyTheme(this);
        applyThemeColors();
        if (chatAdapter != null) {
            chatAdapter.setThemeColors(ThemeManager.getThemeColors(this));
        }
    }
    
    @Override
    public void onBackPressed() {
        if (fullscreenImageContainer != null && fullscreenImageContainer.getVisibility() == View.VISIBLE) {
            closeFullscreenImage();
        } else if (isDrawerOpen) {
            closeDrawer();
        } else {
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveChat();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (groqApiService != null) {
            groqApiService.shutdown();
        }
    }
}
