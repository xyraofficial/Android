package com.xyra.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    
    public static final String PREFS_NAME = "xyra_settings";
    public static final String KEY_FONT_SIZE = "font_size";
    public static final String KEY_FONT_SIZE_VALUE = "font_size_value";
    
    private SharedPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_settings);
        
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        
        applyThemeColors();
        setupClickListeners();
        loadCurrentSettings();
    }
    
    private void applyThemeColors() {
        ThemeManager.ThemeColors colors = ThemeManager.getThemeColors(this);
        
        View rootView = findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setBackgroundColor(colors.background);
        }
    }
    
    private void loadCurrentSettings() {
        String fontSize = prefs.getString(KEY_FONT_SIZE, "Normal");
        TextView tvFontSize = (TextView) findViewById(R.id.tvCurrentFontSize);
        if (tvFontSize != null) {
            tvFontSize.setText(fontSize);
        }
    }
    
    private void setupClickListeners() {
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        
        findViewById(R.id.settingModel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showModelDialog();
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
    
    private void showFontSizeDialog() {
        final String[] sizes = {"Kecil", "Normal", "Besar", "Sangat Besar"};
        final int[] sizeValues = {13, 15, 17, 20};
        
        String currentSize = prefs.getString(KEY_FONT_SIZE, "Normal");
        int checkedItem = 1;
        for (int i = 0; i < sizes.length; i++) {
            if (sizes[i].equals(currentSize)) {
                checkedItem = i;
                break;
            }
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pilih Ukuran Font");
        
        builder.setSingleChoiceItems(sizes, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                prefs.edit()
                    .putString(KEY_FONT_SIZE, sizes[which])
                    .putInt(KEY_FONT_SIZE_VALUE, sizeValues[which])
                    .apply();
                    
                TextView tvSize = (TextView) findViewById(R.id.tvCurrentFontSize);
                tvSize.setText(sizes[which]);
                
                Toast.makeText(SettingsActivity.this, "Ukuran font: " + sizes[which] + "\nPerubahan akan diterapkan pada chat", Toast.LENGTH_LONG).show();
                dialog.dismiss();
            }
        });
        
        builder.setNegativeButton("Batal", null);
        builder.show();
    }
    
    public static int getFontSize(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(KEY_FONT_SIZE_VALUE, 15);
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
                    ChatHistory chatHistory = new ChatHistory(SettingsActivity.this);
                    chatHistory.clearAllData();
                    Toast.makeText(SettingsActivity.this, "Semua data dihapus", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Batal", null)
            .show();
    }
    
    private void showAboutDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_about);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        
        TextView btnOk = (TextView) dialog.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        
        dialog.show();
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
