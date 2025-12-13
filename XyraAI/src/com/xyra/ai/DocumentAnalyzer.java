package com.xyra.ai;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocumentAnalyzer {
    
    private static final String TAG = "DocumentAnalyzer";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    private Context context;
    
    public interface AnalysisCallback {
        void onSuccess(DocumentInfo info);
        void onError(String error);
    }
    
    public static class DocumentInfo {
        public String fileName;
        public String mimeType;
        public long fileSize;
        public String content;
        public String base64Data;
        public boolean isPDF;
        public boolean isImage;
        public boolean isText;
        public int pageCount;
        public String summary;
        
        public DocumentInfo() {
            this.pageCount = 1;
        }
    }
    
    public DocumentAnalyzer(Context context) {
        this.context = context;
    }
    
    public void analyzeDocument(Uri uri, AnalysisCallback callback) {
        try {
            DocumentInfo info = new DocumentInfo();
            
            ContentResolver resolver = context.getContentResolver();
            info.mimeType = resolver.getType(uri);
            
            Cursor cursor = resolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                
                if (nameIndex >= 0) {
                    info.fileName = cursor.getString(nameIndex);
                }
                if (sizeIndex >= 0) {
                    info.fileSize = cursor.getLong(sizeIndex);
                }
                cursor.close();
            }
            
            if (info.fileName == null) {
                info.fileName = "document";
            }
            
            if (info.fileSize > MAX_FILE_SIZE) {
                callback.onError("File terlalu besar. Maksimal 10MB.");
                return;
            }
            
            info.isPDF = info.mimeType != null && info.mimeType.equals("application/pdf");
            info.isImage = info.mimeType != null && info.mimeType.startsWith("image/");
            info.isText = info.mimeType != null && (
                info.mimeType.startsWith("text/") || 
                info.mimeType.equals("application/json") ||
                info.mimeType.equals("application/xml")
            );
            
            InputStream inputStream = resolver.openInputStream(uri);
            if (inputStream == null) {
                callback.onError("Tidak dapat membaca file");
                return;
            }
            
            if (info.isText) {
                info.content = readTextContent(inputStream);
            } else {
                info.base64Data = readBase64Content(inputStream);
                
                if (info.isPDF) {
                    info.content = extractPDFText(info.base64Data);
                    info.pageCount = estimatePDFPages(info.base64Data);
                }
            }
            
            inputStream.close();
            callback.onSuccess(info);
            
        } catch (Exception e) {
            callback.onError("Error: " + e.getMessage());
        }
    }
    
    private String readTextContent(InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        int lineCount = 0;
        int maxLines = 1000;
        
        while ((line = reader.readLine()) != null && lineCount < maxLines) {
            sb.append(line).append("\n");
            lineCount++;
        }
        
        if (lineCount >= maxLines) {
            sb.append("\n[... File dipotong karena terlalu panjang ...]");
        }
        
        reader.close();
        return sb.toString();
    }
    
    private String readBase64Content(InputStream inputStream) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        
        return Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP);
    }
    
    private String extractPDFText(String base64Data) {
        try {
            byte[] pdfBytes = Base64.decode(base64Data, Base64.DEFAULT);
            String pdfContent = new String(pdfBytes, "ISO-8859-1");
            
            StringBuilder extractedText = new StringBuilder();
            
            Pattern streamPattern = Pattern.compile("stream\\s*([\\s\\S]*?)\\s*endstream");
            Matcher matcher = streamPattern.matcher(pdfContent);
            
            while (matcher.find()) {
                String streamContent = matcher.group(1);
                String text = extractTextFromStream(streamContent);
                if (text != null && !text.trim().isEmpty()) {
                    extractedText.append(text).append(" ");
                }
            }
            
            if (extractedText.length() == 0) {
                Pattern textPattern = Pattern.compile("\\(([^)]+)\\)");
                Matcher textMatcher = textPattern.matcher(pdfContent);
                
                while (textMatcher.find()) {
                    String text = textMatcher.group(1);
                    if (isPrintableText(text)) {
                        extractedText.append(text).append(" ");
                    }
                }
            }
            
            String result = extractedText.toString().trim();
            if (result.isEmpty()) {
                return "[PDF berisi gambar atau teks terenkripsi yang tidak dapat diekstrak secara langsung]";
            }
            
            return cleanExtractedText(result);
            
        } catch (Exception e) {
            return "[Tidak dapat mengekstrak teks dari PDF]";
        }
    }
    
    private String extractTextFromStream(String streamContent) {
        StringBuilder text = new StringBuilder();
        
        Pattern tjPattern = Pattern.compile("\\[([^\\]]+)\\]\\s*TJ|\\(([^)]+)\\)\\s*Tj");
        Matcher matcher = tjPattern.matcher(streamContent);
        
        while (matcher.find()) {
            String content = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (content != null) {
                Pattern textInArray = Pattern.compile("\\(([^)]+)\\)");
                Matcher textMatcher = textInArray.matcher(content);
                
                while (textMatcher.find()) {
                    String extractedText = textMatcher.group(1);
                    if (isPrintableText(extractedText)) {
                        text.append(extractedText);
                    }
                }
            }
        }
        
        return text.toString();
    }
    
    private boolean isPrintableText(String text) {
        if (text == null || text.isEmpty()) return false;
        
        int printableCount = 0;
        for (char c : text.toCharArray()) {
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || 
                ".,!?;:'-\"()".indexOf(c) >= 0) {
                printableCount++;
            }
        }
        
        return printableCount > text.length() * 0.5;
    }
    
    private String cleanExtractedText(String text) {
        text = text.replaceAll("\\s+", " ");
        text = text.replaceAll("[\\x00-\\x1F\\x7F-\\x9F]", "");
        text = text.trim();
        
        if (text.length() > 10000) {
            text = text.substring(0, 10000) + "\n[... Teks dipotong ...]";
        }
        
        return text;
    }
    
    private int estimatePDFPages(String base64Data) {
        try {
            byte[] pdfBytes = Base64.decode(base64Data, Base64.DEFAULT);
            String pdfContent = new String(pdfBytes, "ISO-8859-1");
            
            Pattern pagePattern = Pattern.compile("/Type\\s*/Page[^s]");
            Matcher matcher = pagePattern.matcher(pdfContent);
            
            int count = 0;
            while (matcher.find()) {
                count++;
            }
            
            return Math.max(1, count);
            
        } catch (Exception e) {
            return 1;
        }
    }
    
    public String formatDocumentForAI(DocumentInfo info) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("[DOCUMENT ANALYSIS]\n");
        prompt.append("Nama File: ").append(info.fileName).append("\n");
        prompt.append("Tipe: ").append(info.mimeType).append("\n");
        prompt.append("Ukuran: ").append(formatFileSize(info.fileSize)).append("\n");
        
        if (info.isPDF) {
            prompt.append("Halaman: ~").append(info.pageCount).append("\n");
        }
        
        prompt.append("\n--- KONTEN DOKUMEN ---\n");
        
        if (info.content != null && !info.content.isEmpty()) {
            prompt.append(info.content);
        } else if (info.isImage) {
            prompt.append("[Dokumen adalah gambar]");
        } else {
            prompt.append("[Konten tidak dapat diekstrak]");
        }
        
        prompt.append("\n--- END DOCUMENT ---\n");
        
        return prompt.toString();
    }
    
    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.1f KB", size / 1024.0);
        } else {
            return String.format("%.1f MB", size / (1024.0 * 1024.0));
        }
    }
    
    public boolean isSupportedFormat(String mimeType) {
        if (mimeType == null) return false;
        
        return mimeType.equals("application/pdf") ||
               mimeType.startsWith("text/") ||
               mimeType.equals("application/json") ||
               mimeType.equals("application/xml") ||
               mimeType.startsWith("image/") ||
               mimeType.equals("application/msword") ||
               mimeType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }
    
    public static String[] getSupportedMimeTypes() {
        return new String[] {
            "application/pdf",
            "text/plain",
            "text/html",
            "text/csv",
            "text/markdown",
            "application/json",
            "application/xml",
            "image/*"
        };
    }
}
