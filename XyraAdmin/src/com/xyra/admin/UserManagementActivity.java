package com.xyra.admin;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

public class UserManagementActivity extends Activity {
    private ImageButton btnBack, btnRefresh;
    private EditText etSearch;
    private ListView listUsers;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private AdminConfig config;
    private AdminApiService apiService;
    private UserAdapter userAdapter;
    private ArrayList<JSONObject> userList;
    private String currentSearch = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);
        
        config = AdminConfig.getInstance(this);
        apiService = new AdminApiService(config.getApiBaseUrl(), config.getAccessToken());
        userList = new ArrayList<>();
        
        initViews();
        setupClickListeners();
        loadUsers();
    }
    
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnRefresh = findViewById(R.id.btnRefresh);
        etSearch = findViewById(R.id.etSearch);
        listUsers = findViewById(R.id.listUsers);
        progressBar = findViewById(R.id.progressBar);
        tvEmpty = findViewById(R.id.tvEmpty);
        
        userAdapter = new UserAdapter(this, userList, new UserAdapter.UserActionListener() {
            @Override
            public void onBanUser(JSONObject user, boolean ban) {
                handleBanUser(user, ban);
            }
            
            @Override
            public void onDeleteUser(JSONObject user) {
                handleDeleteUser(user);
            }
        });
        listUsers.setAdapter(userAdapter);
    }
    
    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadUsers();
            }
        });
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
                loadUsers();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        
        apiService.getUsers(1, 50, currentSearch, new AdminApiService.ApiArrayCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                progressBar.setVisibility(View.GONE);
                userList.clear();
                
                try {
                    for (int i = 0; i < response.length(); i++) {
                        userList.add(response.getJSONObject(i));
                    }
                } catch (Exception e) {
                    Toast.makeText(UserManagementActivity.this, "Failed to parse users", Toast.LENGTH_SHORT).show();
                }
                
                userAdapter.notifyDataSetChanged();
                tvEmpty.setVisibility(userList.isEmpty() ? View.VISIBLE : View.GONE);
            }
            
            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserManagementActivity.this, error, Toast.LENGTH_SHORT).show();
                tvEmpty.setVisibility(userList.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }
    
    private void handleBanUser(final JSONObject user, final boolean ban) {
        String action = ban ? "ban" : "unban";
        String email = user.optString("email", "this user");
        
        new AlertDialog.Builder(this)
            .setTitle(ban ? "Ban User" : "Unban User")
            .setMessage("Are you sure you want to " + action + " " + email + "?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    banUser(user, ban);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void banUser(JSONObject user, boolean ban) {
        String userId = user.optString("id", "");
        
        apiService.banUser(userId, ban, new AdminApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(UserManagementActivity.this, 
                    ban ? "User banned" : "User unbanned", Toast.LENGTH_SHORT).show();
                loadUsers();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(UserManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void handleDeleteUser(final JSONObject user) {
        String email = user.optString("email", "this user");
        
        new AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to permanently delete " + email + "? This action cannot be undone.")
            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    deleteUser(user);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    private void deleteUser(JSONObject user) {
        String userId = user.optString("id", "");
        
        apiService.deleteUser(userId, new AdminApiService.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(UserManagementActivity.this, "User deleted", Toast.LENGTH_SHORT).show();
                loadUsers();
            }
            
            @Override
            public void onError(String error) {
                Toast.makeText(UserManagementActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
