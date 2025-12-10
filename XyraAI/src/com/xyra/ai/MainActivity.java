package com.xyra.ai;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {
    
    private static final String API_KEY = "YOUR_GROQ_API_KEY_HERE";
    
    private ListView listView;
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
        setupListView();
        setupClickListeners();
        initGroqService();
        
        addWelcomeMessage();
    }
    
    private void initViews() {
        listView = (ListView) findViewById(R.id.listView);
        etMessage = (EditText) findViewById(R.id.etMessage);
        btnSend = (ImageButton) findViewById(R.id.btnSend);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
    }
    
    private void setupListView() {
        chatAdapter = new ChatAdapter(this);
        listView.setAdapter(chatAdapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);
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
    protected void onDestroy() {
        super.onDestroy();
        if (groqApiService != null) {
            groqApiService.shutdown();
        }
    }
}
