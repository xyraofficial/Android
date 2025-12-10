package com.xyra.ai;

import com.xyra.ai.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
    
    private UserManager userManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_login);
        
        userManager = new UserManager(this);
        
        applyThemeColors();
        setupClickListeners();
    }
    
    private void applyThemeColors() {
        ThemeManager.ThemeColors colors = ThemeManager.getThemeColors(this);
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setBackgroundColor(colors.background);
        }
    }
    
    private void setupClickListeners() {
        findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipLogin();
            }
        });
        
        findViewById(R.id.btnLoginGoogle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginWithGoogle();
            }
        });
        
        findViewById(R.id.btnLoginEmail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEmailLoginDialog();
            }
        });
        
        findViewById(R.id.btnSkip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                skipLogin();
            }
        });
    }
    
    private void loginWithGoogle() {
        showGoogleSimulationDialog();
    }
    
    private void showGoogleSimulationDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_email_login);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        TextView title = (TextView) dialog.findViewById(R.id.etEmail);
        if (title != null) {
            ((View)title.getParent().getParent()).setVisibility(View.VISIBLE);
        }
        
        final EditText etEmail = (EditText) dialog.findViewById(R.id.etEmail);
        final EditText etName = (EditText) dialog.findViewById(R.id.etName);
        TextView btnSubmit = (TextView) dialog.findViewById(R.id.btnSubmitEmail);
        TextView btnCancel = (TextView) dialog.findViewById(R.id.btnCancelEmail);
        
        etEmail.setHint("email@gmail.com");
        etName.setHint("Nama lengkap Anda");
        
        btnSubmit.setText("Masuk dengan Google");
        
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String name = etName.getText().toString().trim();
                
                if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Masukkan email Google Anda", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (name.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Masukkan nama Anda", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (!email.contains("@")) {
                    Toast.makeText(LoginActivity.this, "Format email tidak valid", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                userManager.login(name, email, UserManager.LOGIN_TYPE_GOOGLE);
                Toast.makeText(LoginActivity.this, "Berhasil masuk dengan Google!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                goToMain();
            }
        });
        
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        
        dialog.show();
    }
    
    private void showEmailLoginDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_email_login);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        final EditText etEmail = (EditText) dialog.findViewById(R.id.etEmail);
        final EditText etName = (EditText) dialog.findViewById(R.id.etName);
        TextView btnSubmit = (TextView) dialog.findViewById(R.id.btnSubmitEmail);
        TextView btnCancel = (TextView) dialog.findViewById(R.id.btnCancelEmail);
        
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String name = etName.getText().toString().trim();
                
                if (email.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Masukkan email Anda", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (name.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Masukkan nama Anda", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (!email.contains("@")) {
                    Toast.makeText(LoginActivity.this, "Format email tidak valid", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                userManager.login(name, email, UserManager.LOGIN_TYPE_EMAIL);
                Toast.makeText(LoginActivity.this, "Berhasil masuk!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                goToMain();
            }
        });
        
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        
        dialog.show();
    }
    
    private void skipLogin() {
        userManager.setSkipLogin(true);
        goToMain();
    }
    
    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
    
    @Override
    public void onBackPressed() {
        skipLogin();
    }
}
