package com.xyra.ai;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends Activity {
    
    private static final String API_KEY = "YOUR_GROQ_API_KEY_HERE";
    
    private RecyclerView recyclerView;
    private EditText etMessage;
    private ImageButton btnSend;
    private TextView tvStatus;
    
    private ChatAdapter chatAdapter;
    private GroqApiService groqApiService;
    private boolean isWaitingResponse = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        initGroqService();
        
        addWelcomeMessage();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvStatus = findViewById(R.id.tvStatus);
    }
    
    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(chatAdapter);
    }
    
    private void setupClickListeners() {
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }
    
    private void initGroqService() {
        groqApiService = new GroqApiService(API_KEY);
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
            }
            
            @Override
            public void onError(String error) {
                chatAdapter.updateLastMessage(getString(R.string.error_api) + "\n" + error);
                scrollToBottom();
                setWaitingState(false);
            }
        });
    }
    
    private void setWaitingState(boolean waiting) {
        isWaitingResponse = waiting;
        btnSend.setEnabled(!waiting);
        btnSend.setAlpha(waiting ? 0.5f : 1.0f);
        tvStatus.setText(waiting ? getString(R.string.thinking) : "Online");
    }
    
    private void scrollToBottom() {
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (chatAdapter.getItemCount() > 0) {
                    recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                }
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
    protected void onDestroy() {
        super.onDestroy();
        if (groqApiService != null) {
            groqApiService.shutdown();
        }
    }
}
