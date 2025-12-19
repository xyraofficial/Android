package com.xyra.ai;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyntaxHighlighter {
    
    private static final int COLOR_KEYWORD = 0xFFC586C0;
    private static final int COLOR_STRING = 0xFFCE9178;
    private static final int COLOR_NUMBER = 0xFFB5CEA8;
    private static final int COLOR_COMMENT = 0xFF6A9955;
    private static final int COLOR_FUNCTION = 0xFFDCDCAA;
    private static final int COLOR_CLASS = 0xFF4EC9B0;
    private static final int COLOR_VARIABLE = 0xFF9CDCFE;
    private static final int COLOR_OPERATOR = 0xFFD4D4D4;
    private static final int COLOR_BUILTIN = 0xFF4FC1FF;
    
    public static SpannableStringBuilder highlight(String code, String language) {
        SpannableStringBuilder builder = new SpannableStringBuilder(code);
        
        if (language == null) {
            language = "text";
        }
        
        language = language.toLowerCase();
        
        switch (language) {
            case "python":
            case "py":
                highlightPython(builder);
                break;
            case "java":
                highlightJava(builder);
                break;
            case "javascript":
            case "js":
                highlightJavaScript(builder);
                break;
            case "html":
                highlightHTML(builder);
                break;
            case "css":
                highlightCSS(builder);
                break;
            case "json":
                highlightJSON(builder);
                break;
            case "xml":
                highlightXML(builder);
                break;
            case "kotlin":
            case "kt":
                highlightKotlin(builder);
                break;
            case "c":
            case "cpp":
            case "c++":
                highlightC(builder);
                break;
            case "bash":
            case "sh":
            case "shell":
                highlightBash(builder);
                break;
            default:
                highlightGeneric(builder);
                break;
        }
        
        return builder;
    }
    
    private static void highlightPython(SpannableStringBuilder builder) {
        String[] keywords = {"import", "from", "def", "class", "return", "if", "elif", "else", 
            "for", "while", "in", "not", "and", "or", "True", "False", "None", "try", "except", 
            "finally", "with", "as", "lambda", "yield", "pass", "break", "continue", "raise",
            "global", "nonlocal", "assert", "del", "is"};
        
        String[] builtins = {"print", "len", "range", "str", "int", "float", "list", "dict", 
            "set", "tuple", "open", "input", "type", "isinstance", "hasattr", "getattr", 
            "setattr", "enumerate", "zip", "map", "filter", "sorted", "reversed", "sum", 
            "min", "max", "abs", "round", "format", "super", "self"};
        
        applyKeywords(builder, keywords, COLOR_KEYWORD);
        applyKeywords(builder, builtins, COLOR_BUILTIN);
        applyStrings(builder);
        applyNumbers(builder);
        applyComments(builder, "#");
    }
    
    private static void highlightJava(SpannableStringBuilder builder) {
        String[] keywords = {"public", "private", "protected", "static", "final", "class", 
            "interface", "extends", "implements", "new", "return", "if", "else", "for", 
            "while", "do", "switch", "case", "break", "continue", "try", "catch", "finally", 
            "throw", "throws", "import", "package", "void", "int", "boolean", "String", 
            "double", "float", "long", "short", "byte", "char", "null", "true", "false",
            "this", "super", "abstract", "synchronized", "volatile", "transient", "native"};
        
        applyKeywords(builder, keywords, COLOR_KEYWORD);
        applyStrings(builder);
        applyNumbers(builder);
        applyComments(builder, "//");
    }
    
    private static void highlightJavaScript(SpannableStringBuilder builder) {
        String[] keywords = {"const", "let", "var", "function", "return", "if", "else", 
            "for", "while", "do", "switch", "case", "break", "continue", "try", "catch", 
            "finally", "throw", "new", "class", "extends", "import", "export", "from", 
            "default", "async", "await", "true", "false", "null", "undefined", "this",
            "typeof", "instanceof", "in", "of", "delete", "void", "yield", "static"};
        
        String[] builtins = {"console", "log", "document", "window", "fetch", "Promise",
            "Array", "Object", "String", "Number", "Boolean", "JSON", "Math", "Date",
            "parseInt", "parseFloat", "setTimeout", "setInterval", "require", "module"};
        
        applyKeywords(builder, keywords, COLOR_KEYWORD);
        applyKeywords(builder, builtins, COLOR_BUILTIN);
        applyStrings(builder);
        applyNumbers(builder);
        applyComments(builder, "//");
    }
    
    private static void highlightHTML(SpannableStringBuilder builder) {
        Pattern tagPattern = Pattern.compile("</?[a-zA-Z][^>]*>");
        Matcher matcher = tagPattern.matcher(builder.toString());
        
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(COLOR_KEYWORD), 
                matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        applyStrings(builder);
    }
    
    private static void highlightCSS(SpannableStringBuilder builder) {
        Pattern propPattern = Pattern.compile("[a-z-]+(?=\\s*:)");
        Matcher matcher = propPattern.matcher(builder.toString());
        
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(COLOR_VARIABLE), 
                matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        applyStrings(builder);
        applyNumbers(builder);
    }
    
    private static void highlightJSON(SpannableStringBuilder builder) {
        applyStrings(builder);
        applyNumbers(builder);
        
        String[] keywords = {"true", "false", "null"};
        applyKeywords(builder, keywords, COLOR_KEYWORD);
    }
    
    private static void highlightXML(SpannableStringBuilder builder) {
        highlightHTML(builder);
    }
    
    private static void highlightKotlin(SpannableStringBuilder builder) {
        String[] keywords = {"fun", "val", "var", "class", "object", "interface", "if", "else",
            "when", "for", "while", "do", "return", "break", "continue", "throw", "try", 
            "catch", "finally", "import", "package", "is", "as", "in", "out", "true", "false",
            "null", "this", "super", "override", "open", "abstract", "final", "companion",
            "data", "sealed", "inline", "suspend", "lateinit", "by", "lazy"};
        
        applyKeywords(builder, keywords, COLOR_KEYWORD);
        applyStrings(builder);
        applyNumbers(builder);
        applyComments(builder, "//");
    }
    
    private static void highlightC(SpannableStringBuilder builder) {
        String[] keywords = {"int", "char", "float", "double", "void", "long", "short",
            "unsigned", "signed", "const", "static", "extern", "register", "volatile",
            "if", "else", "for", "while", "do", "switch", "case", "break", "continue",
            "return", "goto", "struct", "union", "enum", "typedef", "sizeof", "include",
            "define", "ifdef", "ifndef", "endif", "NULL", "true", "false"};
        
        applyKeywords(builder, keywords, COLOR_KEYWORD);
        applyStrings(builder);
        applyNumbers(builder);
        applyComments(builder, "//");
    }
    
    private static void highlightBash(SpannableStringBuilder builder) {
        String[] keywords = {"if", "then", "else", "elif", "fi", "case", "esac", "for", 
            "while", "until", "do", "done", "in", "function", "return", "exit", "export",
            "local", "readonly", "declare", "unset", "shift", "source", "echo", "printf",
            "cd", "pwd", "ls", "mkdir", "rm", "cp", "mv", "cat", "grep", "sed", "awk"};
        
        applyKeywords(builder, keywords, COLOR_KEYWORD);
        applyStrings(builder);
        applyNumbers(builder);
        applyComments(builder, "#");
    }
    
    private static void highlightGeneric(SpannableStringBuilder builder) {
        applyStrings(builder);
        applyNumbers(builder);
    }
    
    private static void applyKeywords(SpannableStringBuilder builder, String[] keywords, int color) {
        String text = builder.toString();
        
        for (String keyword : keywords) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b");
            Matcher matcher = pattern.matcher(text);
            
            while (matcher.find()) {
                builder.setSpan(new ForegroundColorSpan(color), 
                    matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
    
    private static void applyStrings(SpannableStringBuilder builder) {
        String text = builder.toString();
        
        Pattern pattern = Pattern.compile("([\"'])(?:(?!\\1)[^\\\\]|\\\\.)*\\1");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(COLOR_STRING), 
                matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private static void applyNumbers(SpannableStringBuilder builder) {
        String text = builder.toString();
        
        Pattern pattern = Pattern.compile("\\b\\d+\\.?\\d*\\b");
        Matcher matcher = pattern.matcher(text);
        
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(COLOR_NUMBER), 
                matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private static void applyComments(SpannableStringBuilder builder, String commentStart) {
        String text = builder.toString();
        String[] lines = text.split("\n");
        int pos = 0;
        
        for (String line : lines) {
            int commentIndex = line.indexOf(commentStart);
            if (commentIndex >= 0) {
                int start = pos + commentIndex;
                int end = pos + line.length();
                if (end <= builder.length()) {
                    builder.setSpan(new ForegroundColorSpan(COLOR_COMMENT), 
                        start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
            pos += line.length() + 1;
        }
    }
}
