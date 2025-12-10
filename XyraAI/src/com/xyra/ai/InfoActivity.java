package com.xyra.ai;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class InfoActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        setContentView(R.layout.activity_info);
        
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
        findViewById(R.id.btnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
        
        findViewById(R.id.itemHelpCenter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHelpCenterDialog();
            }
        });
        
        findViewById(R.id.itemTerms).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTermsDialog();
            }
        });
        
        findViewById(R.id.itemPrivacy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPrivacyDialog();
            }
        });
        
        findViewById(R.id.itemLicense).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLicenseDialog();
            }
        });
        
        findViewById(R.id.itemAppInfo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAppInfoDialog();
            }
        });
    }
    
    private void showHelpCenterDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Pusat Bantuan")
            .setMessage("Selamat datang di Pusat Bantuan XyraAI!\n\n" +
                "Cara menggunakan XyraAI:\n\n" +
                "1. Ketik pertanyaan Anda di kolom chat\n" +
                "2. Tekan tombol kirim untuk mengirim pesan\n" +
                "3. Tunggu respons dari AI\n" +
                "4. Gunakan menu untuk mengakses fitur lainnya\n\n" +
                "Tips:\n" +
                "• Gunakan bahasa yang jelas dan spesifik\n" +
                "• Anda bisa mengirim gambar untuk analisis\n" +
                "• Riwayat chat tersimpan otomatis\n\n" +
                "Jika mengalami masalah, silakan hubungi developer melalui menu Pengaturan.")
            .setPositiveButton("Mengerti", null)
            .show();
    }
    
    private void showTermsDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Ketentuan Penggunaan")
            .setMessage("KETENTUAN PENGGUNAAN XYRAAI\n\n" +
                "Terakhir diperbarui: Desember 2024\n\n" +
                "1. PENERIMAAN KETENTUAN\n" +
                "Dengan menggunakan aplikasi XyraAI, Anda setuju untuk terikat dengan ketentuan penggunaan ini.\n\n" +
                "2. LAYANAN\n" +
                "XyraAI menyediakan layanan asisten AI berbasis chat yang didukung oleh teknologi GROQ API.\n\n" +
                "3. PENGGUNAAN YANG DIIZINKAN\n" +
                "• Menggunakan untuk keperluan pribadi dan edukasi\n" +
                "• Mengajukan pertanyaan dan mendapatkan bantuan AI\n" +
                "• Analisis gambar dan konten\n\n" +
                "4. LARANGAN\n" +
                "• Penggunaan untuk tujuan ilegal\n" +
                "• Menyalahgunakan layanan\n" +
                "• Mengirim konten berbahaya\n\n" +
                "5. BATASAN TANGGUNG JAWAB\n" +
                "Layanan disediakan \"sebagaimana adanya\" tanpa jaminan apapun.")
            .setPositiveButton("Tutup", null)
            .show();
    }
    
    private void showPrivacyDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Kebijakan Privasi")
            .setMessage("KEBIJAKAN PRIVASI XYRAAI\n\n" +
                "Terakhir diperbarui: Desember 2024\n\n" +
                "1. DATA YANG DIKUMPULKAN\n" +
                "• Riwayat percakapan (disimpan lokal di perangkat)\n" +
                "• Preferensi pengaturan aplikasi\n\n" +
                "2. PENGGUNAAN DATA\n" +
                "• Menyediakan layanan chat AI\n" +
                "• Menyimpan riwayat untuk kenyamanan pengguna\n" +
                "• Menerapkan preferensi pengguna\n\n" +
                "3. PENYIMPANAN DATA\n" +
                "Data chat disimpan secara lokal di perangkat Anda. Pesan dikirim ke server GROQ untuk diproses dan tidak disimpan secara permanen di server.\n\n" +
                "4. KEAMANAN\n" +
                "Kami berkomitmen untuk melindungi privasi Anda dengan menggunakan enkripsi dan praktik keamanan standar industri.\n\n" +
                "5. HAK PENGGUNA\n" +
                "Anda dapat menghapus semua data kapan saja melalui menu Pengaturan.")
            .setPositiveButton("Tutup", null)
            .show();
    }
    
    private void showLicenseDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Lisensi")
            .setMessage("LISENSI PERANGKAT LUNAK XYRAAI\n\n" +
                "Hak Cipta (c) 2024 XyraAI Team\n\n" +
                "LISENSI OPEN SOURCE\n\n" +
                "Aplikasi ini menggunakan komponen open source berikut:\n\n" +
                "• GROQ API - Untuk pemrosesan AI\n" +
                "  Lisensi: Proprietary API Service\n\n" +
                "• Llama 3.3 Model - Model AI\n" +
                "  Lisensi: Meta AI Community License\n\n" +
                "• Android SDK\n" +
                "  Lisensi: Apache License 2.0\n\n" +
                "KETENTUAN:\n" +
                "Aplikasi ini disediakan \"sebagaimana adanya\" untuk tujuan edukasi dan penggunaan pribadi.\n\n" +
                "Dibuat untuk komunitas AIDE developers.")
            .setPositiveButton("Tutup", null)
            .show();
    }
    
    private void showAppInfoDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Tentang XyraAI")
            .setMessage("XyraAI untuk Android\n\n" +
                "Versi: 1.2025.322 (23)\n" +
                "Build: Release\n\n" +
                "AI Chat Assistant yang didukung oleh:\n" +
                "• GROQ API\n" +
                "• Llama 3.3 70B Model\n" +
                "• Llama 4 Scout (Vision)\n\n" +
                "Fitur:\n" +
                "• Chat AI multi-bahasa\n" +
                "• Analisis gambar\n" +
                "• Syntax highlighting untuk kode\n" +
                "• Riwayat chat\n" +
                "• Tema modern neumorphic\n\n" +
                "Dibuat dengan cinta untuk AIDE developers")
            .setPositiveButton("Tutup", null)
            .show();
    }
    
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}
