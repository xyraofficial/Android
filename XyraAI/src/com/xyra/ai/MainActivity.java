package com.xyra.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    
    private static final String API_KEY = "YOUR_GROQ_API_KEY_HERE";
    
    private ListView listView;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnMenu;
    private TextView tvStatus;
    private TextView tvTyping;
    
    private ChatAdapter chatAdapter;
    private GroqApiService groqApiService;
    private ChatHistory chatHistory;
    private boolean isWaitingResponse = false;
    
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
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvTyping = (TextView) findViewById(R.id.tvTyping);
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
        android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, text);
        startActivity(android.content.Intent.createChooser(intent, "Share via"));
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
            .setMessage("XyraAI v1.0\n\nPowered by GROQ API with Llama 3.3 70B\n\nFeatures:\n- Multi-language support\n- Auto language detection\n- ChatGPT-like responses\n- Code assistance\n- And more!")
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
        
        if (TextUtils.isEmpty(messageText)) {
            return;
        }
        
        chatAdapter.addMessage(new Message(messageText, Message.TYPE_USER));
        scrollToBottom();
        
        etMessage.setText("");
        hideKeyboard();
        
        setWaitingState(true);
        
        chatAdapter.addMessage(new Message(getString(R.string.thinking), Message.TYPE_AI));
        scrollToBottom();
        
        groqApiService.sendMessage(chatAdapter.getMessages(), new GroqApiService.ChatCallback() {
            @Override
            public void onSuccess(String response) {
                chatAdapter.updateLastMessage(response);
                scrollToBottom();
                setWaitingState(false);
                saveChat();
            }
            
            @Override
            public void onError(String error) {
                chatAdapter.updateLastMessage(getString(R.string.error_api) + "\n" + error);
                scrollToBottom();
                setWaitingState(false);
            }
        });
    }
    
    private void saveChat() {
        chatHistory.saveMessages(chatAdapter.getMessages());
    }
    
    private void setWaitingState(boolean waiting) {
        isWaitingResponse = waiting;
        btnSend.setEnabled(!waiting);
        btnSend.setAlpha(waiting ? 0.5f : 1.0f);
        
        if (waiting) {
            tvStatus.setVisibility(View.GONE);
            tvTyping.setVisibility(View.VISIBLE);
        } else {
            tvStatus.setVisibility(View.VISIBLE);
            tvTyping.setVisibility(View.GONE);
        }
    }
    
    private void scrollToBottom() {
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
