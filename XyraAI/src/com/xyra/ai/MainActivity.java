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
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.os.Environment;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

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
    private TextView tvStatus;
    private TextView tvTyping;
    private LinearLayout imagePreviewContainer;
    private ImageView ivPreview;
    private ImageButton btnRemoveImage;
    
    private ChatAdapter chatAdapter;
    private GroqApiService groqApiService;
    private ChatHistory chatHistory;
    private boolean isWaitingResponse = false;
    
    private String selectedImageBase64 = null;
    private Uri selectedImageUri = null;
    private boolean userScrolledUp = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupListView();
        setupClickListeners();
        initGroqService();
        initChatHistory();
        
        addWelcomeMessage();
    }
    
    private void initViews() {
        listView = (ListView) findViewById(R.id.listView);
        etMessage = (EditText) findViewById(R.id.etMessage);
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        btnMenu = (ImageButton) findViewById(R.id.btnMenu);
        btnImage = (ImageButton) findViewById(R.id.btnImage);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvTyping = (TextView) findViewById(R.id.tvTyping);
        
        imagePreviewContainer = (LinearLayout) findViewById(R.id.imagePreviewContainer);
        if (imagePreviewContainer != null) {
            ivPreview = (ImageView) findViewById(R.id.ivPreview);
            btnRemoveImage = (ImageButton) findViewById(R.id.btnRemoveImage);
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
    
    private void showMessageOptions(final int position) {
        final Message message = (Message) chatAdapter.getItem(position);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Options");
        
        String[] options = {"Copy Text", "Share"};
        
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
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
    }
    
    private void shareText(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(intent, "Share via"));
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
        
        if (btnRemoveImage != null) {
            btnRemoveImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearSelectedImage();
                }
            });
        }
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
                Bitmap scaledBitmap = scaleBitmap(bitmap, 512);
                
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                byte[] imageBytes = baos.toByteArray();
                selectedImageBase64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
                
                showImagePreview(scaledBitmap);
                
                Toast.makeText(this, "Gambar dipilih! Tulis pertanyaan tentang gambar ini.", Toast.LENGTH_SHORT).show();
                
            } catch (Exception e) {
                Toast.makeText(this, "Gagal memuat gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                selectedImageBase64 = null;
                selectedImageUri = null;
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
        if (imagePreviewContainer != null) {
            imagePreviewContainer.setVisibility(View.GONE);
        }
    }
    
    private void showMainMenu() {
        PopupMenu popup = new PopupMenu(this, btnMenu);
        popup.getMenu().add(0, 1, 0, "New Chat");
        popup.getMenu().add(0, 2, 1, "Clear History");
        popup.getMenu().add(0, 3, 2, "About");
        
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1:
                        startNewChat();
                        return true;
                    case 2:
                        confirmClearHistory();
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
        chatAdapter.clearMessages();
        chatHistory.clearHistory();
        clearSelectedImage();
        addWelcomeMessage();
        Toast.makeText(this, "New chat started", Toast.LENGTH_SHORT).show();
    }
    
    private void confirmClearHistory() {
        new AlertDialog.Builder(this)
            .setTitle("Clear History")
            .setMessage("Are you sure you want to clear all chat history?")
            .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startNewChat();
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void showAbout() {
        new AlertDialog.Builder(this)
            .setTitle("About XyraAI")
            .setMessage("XyraAI v1.1\n\nPowered by GROQ API with Llama 3.3 70B\n\nFeatures:\n- Multi-language support\n- Auto language detection\n- Typing animation\n- Image analysis\n- Code assistance\n- Beautiful thinking animation\n- And more!")
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void initGroqService() {
        groqApiService = new GroqApiService(API_KEY);
    }
    
    private void initChatHistory() {
        chatHistory = new ChatHistory(this);
    }
    
    private void addWelcomeMessage() {
        String welcome = getString(R.string.welcome_message);
        chatAdapter.addMessage(new Message(welcome, Message.TYPE_AI));
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
            displayMessage = "[Gambar] " + (TextUtils.isEmpty(messageText) ? "Analisis gambar ini" : messageText);
        }
        
        chatAdapter.addMessage(new Message(displayMessage, Message.TYPE_USER));
        forceScrollToBottom();
        
        etMessage.setText("");
        hideKeyboard();
        
        setWaitingState(true);
        
        chatAdapter.addMessage(new Message("thinking", Message.TYPE_AI));
        forceScrollToBottom();
        
        final String imageToSend = selectedImageBase64;
        final String textToSend = TextUtils.isEmpty(messageText) ? "Tolong analisis dan jelaskan gambar ini" : messageText;
        
        clearSelectedImage();
        
        if (imageToSend != null) {
            groqApiService.sendMessageWithImage(chatAdapter.getMessages(), textToSend, imageToSend, new GroqApiService.ChatCallback() {
                @Override
                public void onSuccess(final String response) {
                    chatAdapter.updateLastMessageWithTyping(response, new Runnable() {
                        @Override
                        public void run() {
                            setWaitingState(false);
                            saveChat();
                        }
                    });
                    scrollToBottom();
                }
                
                @Override
                public void onError(String error) {
                    chatAdapter.updateLastMessage(getString(R.string.error_api) + "\n" + error);
                    scrollToBottom();
                    setWaitingState(false);
                }
            });
        } else {
            groqApiService.sendMessage(chatAdapter.getMessages(), new GroqApiService.ChatCallback() {
                @Override
                public void onSuccess(final String response) {
                    chatAdapter.updateLastMessageWithTyping(response, new Runnable() {
                        @Override
                        public void run() {
                            setWaitingState(false);
                            saveChat();
                        }
                    });
                    scrollToBottom();
                }
                
                @Override
                public void onError(String error) {
                    chatAdapter.updateLastMessage(getString(R.string.error_api) + "\n" + error);
                    scrollToBottom();
                    setWaitingState(false);
                }
            });
        }
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
        } else {
            tvStatus.setVisibility(View.VISIBLE);
            tvTyping.setVisibility(View.GONE);
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
