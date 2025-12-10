package com.xyra.ai;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageRenderer {
    
    private Context context;
    private LayoutInflater inflater;
    
    public MessageRenderer(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }
    
    public void renderMessage(String content, LinearLayout container) {
        container.removeAllViews();
        
        List<MessagePart> parts = parseMessage(content);
        
        for (MessagePart part : parts) {
            if (part.isCode) {
                addCodeBlock(container, part.language, part.content);
            } else {
                addTextBlock(container, part.content);
            }
        }
    }
    
    private List<MessagePart> parseMessage(String content) {
        List<MessagePart> parts = new ArrayList<MessagePart>();
        
        Pattern codePattern = Pattern.compile("```(\\w*)\\n([\\s\\S]*?)```");
        Matcher matcher = codePattern.matcher(content);
        
        int lastEnd = 0;
        
        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                String textBefore = content.substring(lastEnd, matcher.start()).trim();
                if (!textBefore.isEmpty()) {
                    parts.add(new MessagePart(textBefore, false, null));
                }
            }
            
            String language = matcher.group(1);
            String code = matcher.group(2);
            
            if (language == null || language.isEmpty()) {
                language = "text";
            }
            
            parts.add(new MessagePart(code, true, language));
            lastEnd = matcher.end();
        }
        
        if (lastEnd < content.length()) {
            String remaining = content.substring(lastEnd).trim();
            if (!remaining.isEmpty()) {
                parts.add(new MessagePart(remaining, false, null));
            }
        }
        
        if (parts.isEmpty()) {
            parts.add(new MessagePart(content, false, null));
        }
        
        return parts;
    }
    
    private void addCodeBlock(LinearLayout container, String language, final String code) {
        View codeView = inflater.inflate(R.layout.item_code_block, container, false);
        
        TextView tvLanguage = (TextView) codeView.findViewById(R.id.tvLanguage);
        TextView tvCode = (TextView) codeView.findViewById(R.id.tvCode);
        View btnCopy = codeView.findViewById(R.id.btnCopy);
        
        String displayLang = getDisplayLanguage(language);
        tvLanguage.setText(displayLang);
        
        SpannableStringBuilder highlightedCode = SyntaxHighlighter.highlight(code.trim(), language);
        tvCode.setText(highlightedCode);
        
        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                copyToClipboard(code.trim());
            }
        });
        
        container.addView(codeView);
    }
    
    private void addTextBlock(LinearLayout container, String text) {
        TextView textView = new TextView(context);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 4, 0, 4);
        textView.setLayoutParams(params);
        
        textView.setTextColor(0xFFE5E7EB);
        textView.setTextSize(15);
        textView.setLineSpacing(4, 1);
        
        SpannableStringBuilder formatted = formatText(text);
        textView.setText(formatted);
        
        container.addView(textView);
    }
    
    private SpannableStringBuilder formatText(String text) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        
        String[] lines = text.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            
            if (line.startsWith("### ")) {
                SpannableStringBuilder header = new SpannableStringBuilder(line.substring(4));
                header.setSpan(new StyleSpan(Typeface.BOLD), 0, header.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                header.setSpan(new RelativeSizeSpan(1.1f), 0, header.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(header);
            } else if (line.startsWith("## ")) {
                SpannableStringBuilder header = new SpannableStringBuilder(line.substring(3));
                header.setSpan(new StyleSpan(Typeface.BOLD), 0, header.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                header.setSpan(new RelativeSizeSpan(1.2f), 0, header.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(header);
            } else if (line.startsWith("# ")) {
                SpannableStringBuilder header = new SpannableStringBuilder(line.substring(2));
                header.setSpan(new StyleSpan(Typeface.BOLD), 0, header.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                header.setSpan(new RelativeSizeSpan(1.3f), 0, header.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                builder.append(header);
            } else if (line.matches("^\\s*[-*+]\\s+.*")) {
                String bulletContent = line.replaceFirst("^\\s*[-*+]\\s+", "");
                SpannableStringBuilder formatted = formatInlineStyles(bulletContent);
                builder.append("  \u2022 ");
                builder.append(formatted);
            } else if (line.matches("^\\s*\\d+\\.\\s+.*")) {
                String numMatch = line.replaceFirst("^\\s*(\\d+)\\.\\s+.*", "$1");
                String listContent = line.replaceFirst("^\\s*\\d+\\.\\s+", "");
                SpannableStringBuilder formatted = formatInlineStyles(listContent);
                builder.append("  " + numMatch + ". ");
                builder.append(formatted);
            } else {
                SpannableStringBuilder formatted = formatInlineStyles(line);
                builder.append(formatted);
            }
            
            if (i < lines.length - 1) {
                builder.append("\n");
            }
        }
        
        return builder;
    }
    
    private SpannableStringBuilder formatInlineStyles(String text) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        
        Pattern boldPattern = Pattern.compile("\\*\\*([^*]+)\\*\\*");
        Matcher boldMatcher = boldPattern.matcher(text);
        int offset = 0;
        
        while (boldMatcher.find()) {
            int start = boldMatcher.start() - offset;
            int end = boldMatcher.end() - offset;
            String boldText = boldMatcher.group(1);
            
            builder.replace(start, end, boldText);
            builder.setSpan(new StyleSpan(Typeface.BOLD), 
                start, start + boldText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            
            offset += 4;
        }
        
        text = builder.toString();
        Pattern italicPattern = Pattern.compile("(?<!\\*)\\*([^*]+)\\*(?!\\*)");
        Matcher italicMatcher = italicPattern.matcher(text);
        offset = 0;
        
        while (italicMatcher.find()) {
            int start = italicMatcher.start() - offset;
            int end = italicMatcher.end() - offset;
            String italicText = italicMatcher.group(1);
            
            builder.replace(start, end, italicText);
            builder.setSpan(new StyleSpan(Typeface.ITALIC), 
                start, start + italicText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            
            offset += 2;
        }
        
        text = builder.toString();
        Pattern codePattern = Pattern.compile("`([^`]+)`");
        Matcher codeMatcher = codePattern.matcher(text);
        offset = 0;
        
        while (codeMatcher.find()) {
            int start = codeMatcher.start() - offset;
            int end = codeMatcher.end() - offset;
            String codeText = codeMatcher.group(1);
            
            builder.replace(start, end, codeText);
            builder.setSpan(new ForegroundColorSpan(0xFF10B981), 
                start, start + codeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            builder.setSpan(new android.text.style.BackgroundColorSpan(0xFF1E293B), 
                start, start + codeText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            
            offset += 2;
        }
        
        return builder;
    }
    
    private String getDisplayLanguage(String lang) {
        if (lang == null) return "Text";
        
        switch (lang.toLowerCase()) {
            case "python":
            case "py":
                return "Python";
            case "java":
                return "Java";
            case "javascript":
            case "js":
                return "JavaScript";
            case "typescript":
            case "ts":
                return "TypeScript";
            case "html":
                return "HTML";
            case "css":
                return "CSS";
            case "json":
                return "JSON";
            case "xml":
                return "XML";
            case "kotlin":
            case "kt":
                return "Kotlin";
            case "c":
                return "C";
            case "cpp":
            case "c++":
                return "C++";
            case "csharp":
            case "cs":
                return "C#";
            case "bash":
            case "sh":
            case "shell":
                return "Bash";
            case "sql":
                return "SQL";
            case "php":
                return "PHP";
            case "ruby":
            case "rb":
                return "Ruby";
            case "go":
            case "golang":
                return "Go";
            case "rust":
            case "rs":
                return "Rust";
            case "swift":
                return "Swift";
            case "dart":
                return "Dart";
            case "yaml":
            case "yml":
                return "YAML";
            case "markdown":
            case "md":
                return "Markdown";
            default:
                return lang.substring(0, 1).toUpperCase() + lang.substring(1);
        }
    }
    
    private void copyToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Code", text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(context, "Kode disalin!", Toast.LENGTH_SHORT).show();
    }
    
    private static class MessagePart {
        String content;
        boolean isCode;
        String language;
        
        MessagePart(String content, boolean isCode, String language) {
            this.content = content;
            this.isCode = isCode;
            this.language = language;
        }
    }
}
