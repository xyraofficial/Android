package com.xyra.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    
    private static final String PREFS_NAME = "xyra_settings";
    private static final String KEY_API_KEY = "api_key";
    private static final String KEY_THEME = "theme";
    private static final String KEY_FONT_SIZE = "font_size";
    
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        setupClickListeners();
    }
    
    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        
        findViewById(R.id.settingApiKey).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showApiKeyDialog();
            }
        });
        
        findViewById(R.id.settingModel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModelDialog();
            }
        });
        
        findViewById(R.id.settingTheme).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showThemeDialog();
            }
        });
        
        findViewById(R.id.settingFontSize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFontSizeDialog();
            }
        });
        
        findViewById(R.id.settingExport).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportChats();
            }
        });
        
        findViewById(R.id.settingClearData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showClearDataDialog();
            }
        });
        
        findViewById(R.id.settingAbout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });
        
        findViewById(R.id.settingExit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitDialog();
            }
        });
    }
    
    private void showApiKeyDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("API Key");
        
        final EditText input = new EditText(this);
        input.setHint("Masukkan GROQ API Key");
        input.setText(prefs.getString(KEY_API_KEY, ""));
        input.setPadding(48, 32, 48, 32);
        builder.setView(input);
        
        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String apiKey = input.getText().toString().trim();
                prefs.edit().putString(KEY_API_KEY, apiKey).apply();
                Toast.makeText(SettingsActivity.this, "API Key disimpan", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Batal", null);
        builder.show();
    }
    
    private void showModelDialog() {
        final String[] models = {"Llama 3.3 70B", "Llama 4 Scout (Vision)", "Mixtral 8x7B"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Model AI");
        
        builder.setItems(models, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextView tvModel = (TextView) findViewById(R.id.tvCurrentModel);
                tvModel.setText(models[which]);
                Toast.makeText(SettingsActivity.this, "Model: " + models[which], Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.show();
    }
    
    private void showThemeDialog() {
        final String[] themes = {"Dark Mode", "Light Mode", "Auto"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Tema");
        
        builder.setItems(themes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                prefs.edit().putString(KEY_THEME, themes[which]).apply();
                TextView tvTheme = (TextView) findViewById(R.id.tvCurrentTheme);
                tvTheme.setText(themes[which]);
                Toast.makeText(SettingsActivity.this, "Tema: " + themes[which], Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.show();
    }
    
    private void showFontSizeDialog() {
        final String[] sizes = {"Kecil", "Normal", "Besar", "Sangat Besar"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Ukuran Font");
        
        builder.setItems(sizes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                prefs.edit().putString(KEY_FONT_SIZE, sizes[which]).apply();
                TextView tvSize = (TextView) findViewById(R.id.tvCurrentFontSize);
                tvSize.setText(sizes[which]);
                Toast.makeText(SettingsActivity.this, "Ukuran: " + sizes[which], Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.show();
    }
    
    private void exportChats() {
        Toast.makeText(this, "Fitur export akan segera hadir", Toast.LENGTH_SHORT).show();
    }
    
    private void showClearDataDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Hapus Semua Data")
            .setMessage("Apakah Anda yakin ingin menghapus semua chat dan pengaturan? Tindakan ini tidak dapat dibatalkan.")
            .setPositiveButton("Hapus", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    prefs.edit().clear().apply();
                    getSharedPreferences("xyra_chat_history", MODE_PRIVATE).edit().clear().apply();
                    Toast.makeText(SettingsActivity.this, "Semua data dihapus", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }
    
    private void showAboutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Tentang XyraAI")
            .setMessage("XyraAI v1.2\n\n" +
                "AI Chat Assistant powered by GROQ API\n\n" +
                "Fitur:\n" +
                "- Chat dengan AI Llama 3.3 70B\n" +
                "- Analisis gambar dengan AI Vision\n" +
                "- Riwayat chat tersimpan\n" +
                "- UI modern dan responsif\n" +
                "- Multi-bahasa otomatis\n\n" +
                "Dibuat untuk AIDE developers")
            .setPositiveButton("OK", null)
            .show();
    }
    
    private void showExitDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Keluar Aplikasi")
            .setMessage("Apakah Anda yakin ingin keluar dari XyraAI?")
            .setPositiveButton("Keluar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishAffinity();
                    System.exit(0);
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
}
