package com.xyra.ai;

import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownParser {
    
    private static final int CODE_BG_COLOR = 0xFF1E293B;
    private static final int CODE_TEXT_COLOR = 0xFF10B981;
    private static final int BOLD_COLOR = 0xFFFFFFFF;
    
    public static CharSequence parse(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        
        applyCodeBlocks(builder);
        applyInlineCode(builder);
        applyBold(builder);
        applyItalic(builder);
        
        return builder;
    }
    
    private static void applyCodeBlocks(SpannableStringBuilder builder) {
        Pattern pattern = Pattern.compile("```[a-zA-Z]*\\n([\\s\\S]*?)```");
        String text = builder.toString();
        Matcher matcher = pattern.matcher(text);
        
        int offset = 0;
        while (matcher.find()) {
            int start = matcher.start() - offset;
            int end = matcher.end() - offset;
            String code = matcher.group(1);
            
            if (code != null) {
                builder.replace(start, end, code);
                builder.setSpan(new BackgroundColorSpan(CODE_BG_COLOR), 
                    start, start + code.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new ForegroundColorSpan(CODE_TEXT_COLOR), 
                    start, start + code.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new TypefaceSpan("monospace"), 
                    start, start + code.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                
                offset += (end - start) - code.length();
            }
        }
    }
    
    private static void applyInlineCode(SpannableStringBuilder builder) {
        Pattern pattern = Pattern.compile("`([^`]+)`");
        String text = builder.toString();
        Matcher matcher = pattern.matcher(text);
        
        int offset = 0;
        while (matcher.find()) {
            int start = matcher.start() - offset;
            int end = matcher.end() - offset;
            String code = matcher.group(1);
            
            if (code != null) {
                builder.replace(start, end, code);
                builder.setSpan(new BackgroundColorSpan(CODE_BG_COLOR), 
                    start, start + code.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new ForegroundColorSpan(CODE_TEXT_COLOR), 
                    start, start + code.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.setSpan(new TypefaceSpan("monospace"), 
                    start, start + code.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                
                offset += 2;
            }
        }
    }
    
    private static void applyBold(SpannableStringBuilder builder) {
        Pattern pattern = Pattern.compile("\\*\\*([^*]+)\\*\\*");
        String text = builder.toString();
        Matcher matcher = pattern.matcher(text);
        
        int offset = 0;
        while (matcher.find()) {
            int start = matcher.start() - offset;
            int end = matcher.end() - offset;
            String boldText = matcher.group(1);
            
            if (boldText != null) {
                builder.replace(start, end, boldText);
                builder.setSpan(new StyleSpan(Typeface.BOLD), 
                    start, start + boldText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                
                offset += 4;
            }
        }
    }
    
    private static void applyItalic(SpannableStringBuilder builder) {
        Pattern pattern = Pattern.compile("\\*([^*]+)\\*");
        String text = builder.toString();
        Matcher matcher = pattern.matcher(text);
        
        int offset = 0;
        while (matcher.find()) {
            int start = matcher.start() - offset;
            int end = matcher.end() - offset;
            String italicText = matcher.group(1);
            
            if (italicText != null) {
                builder.replace(start, end, italicText);
                builder.setSpan(new StyleSpan(Typeface.ITALIC), 
                    start, start + italicText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                
                offset += 2;
            }
        }
    }
}
