package com.xyra.ai;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShareService {
    
    private static final String TAG = "ShareService";
    
    private Context context;
    
    public ShareService(Context context) {
        this.context = context;
    }
    
    public void shareText(String text) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        
        Intent chooser = Intent.createChooser(shareIntent, "Bagikan via");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }
    
    public void shareChat(List<Message> messages, String chatTitle) {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        
        sb.append("=== ").append(chatTitle).append(" ===\n\n");
        
        for (Message message : messages) {
            String time = dateFormat.format(new Date(message.getTimestamp()));
            String sender = message.getType() == Message.TYPE_USER ? "Saya" : "XyraAI";
            
            sb.append("[").append(time).append("] ").append(sender).append(":\n");
            sb.append(message.getContent()).append("\n\n");
        }
        
        sb.append("---\n");
        sb.append("Dibagikan dari XyraAI");
        
        shareText(sb.toString());
    }
    
    public void shareSingleMessage(Message message) {
        String sender = message.getType() == Message.TYPE_USER ? "Saya" : "XyraAI";
        String text = sender + ": " + message.getContent() + "\n\n- Dari XyraAI";
        shareText(text);
    }
    
    public void shareToWhatsApp(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setPackage("com.whatsapp");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            try {
                intent.setPackage("com.whatsapp.w4b");
                context.startActivity(intent);
            } catch (Exception e2) {
                Toast.makeText(context, "WhatsApp tidak terinstall", Toast.LENGTH_SHORT).show();
                shareText(text);
            }
        }
    }
    
    public void shareToTelegram(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.setPackage("org.telegram.messenger");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Telegram tidak terinstall", Toast.LENGTH_SHORT).show();
            shareText(text);
        }
    }
    
    public void shareViaEmail(String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        
        try {
            Intent chooser = Intent.createChooser(intent, "Kirim Email");
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(chooser);
        } catch (Exception e) {
            Toast.makeText(context, "Tidak ada aplikasi email", Toast.LENGTH_SHORT).show();
        }
    }
    
    public void copyToClipboard(String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) 
            context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("XyraAI", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Disalin ke clipboard", Toast.LENGTH_SHORT).show();
    }
    
    public void exportChatToFile(List<Message> messages, String chatTitle, ExportCallback callback) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            SimpleDateFormat fileFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
            
            StringBuilder sb = new StringBuilder();
            sb.append("# ").append(chatTitle).append("\n\n");
            sb.append("Diekspor pada: ").append(dateFormat.format(new Date())).append("\n\n");
            sb.append("---\n\n");
            
            for (Message message : messages) {
                String time = dateFormat.format(new Date(message.getTimestamp()));
                String sender = message.getType() == Message.TYPE_USER ? "**Saya**" : "**XyraAI**";
                
                sb.append("### ").append(sender).append(" (").append(time).append(")\n\n");
                sb.append(message.getContent()).append("\n\n");
                sb.append("---\n\n");
            }
            
            String fileName = "XyraAI_Chat_" + fileFormat.format(new Date()) + ".md";
            File exportDir = new File(context.getExternalFilesDir(null), "exports");
            if (!exportDir.exists()) {
                exportDir.mkdirs();
            }
            
            File exportFile = new File(exportDir, fileName);
            FileWriter writer = new FileWriter(exportFile);
            writer.write(sb.toString());
            writer.close();
            
            if (callback != null) {
                callback.onSuccess(exportFile.getAbsolutePath());
            }
            
        } catch (Exception e) {
            if (callback != null) {
                callback.onError(e.getMessage());
            }
        }
    }
    
    public interface ExportCallback {
        void onSuccess(String filePath);
        void onError(String error);
    }
    
    public boolean isAppInstalled(String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }
}
