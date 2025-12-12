package com.xyra.ai;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeEditorEngine {
    
    public static class Theme {
        public int background;
        public int lineNumberBg;
        public int lineNumber;
        public int text;
        public int keyword;
        public int string;
        public int comment;
        public int number;
        public int function;
        public int type;
        public int variable;
        public int operator;
        public int bracket;
        public int tag;
        public int attribute;
        
        public static Theme MONOKAI = new Theme() {{
            background = Color.parseColor("#272822");
            lineNumberBg = Color.parseColor("#1e1f1c");
            lineNumber = Color.parseColor("#90908a");
            text = Color.parseColor("#f8f8f2");
            keyword = Color.parseColor("#f92672");
            string = Color.parseColor("#e6db74");
            comment = Color.parseColor("#75715e");
            number = Color.parseColor("#ae81ff");
            function = Color.parseColor("#a6e22e");
            type = Color.parseColor("#66d9ef");
            variable = Color.parseColor("#f8f8f2");
            operator = Color.parseColor("#f92672");
            bracket = Color.parseColor("#f8f8f2");
            tag = Color.parseColor("#f92672");
            attribute = Color.parseColor("#a6e22e");
        }};
        
        public static Theme DRACULA = new Theme() {{
            background = Color.parseColor("#282a36");
            lineNumberBg = Color.parseColor("#21222c");
            lineNumber = Color.parseColor("#6272a4");
            text = Color.parseColor("#f8f8f2");
            keyword = Color.parseColor("#ff79c6");
            string = Color.parseColor("#f1fa8c");
            comment = Color.parseColor("#6272a4");
            number = Color.parseColor("#bd93f9");
            function = Color.parseColor("#50fa7b");
            type = Color.parseColor("#8be9fd");
            variable = Color.parseColor("#f8f8f2");
            operator = Color.parseColor("#ff79c6");
            bracket = Color.parseColor("#f8f8f2");
            tag = Color.parseColor("#ff79c6");
            attribute = Color.parseColor("#50fa7b");
        }};
        
        public static Theme ONE_DARK = new Theme() {{
            background = Color.parseColor("#282c34");
            lineNumberBg = Color.parseColor("#21252b");
            lineNumber = Color.parseColor("#4b5263");
            text = Color.parseColor("#abb2bf");
            keyword = Color.parseColor("#c678dd");
            string = Color.parseColor("#98c379");
            comment = Color.parseColor("#5c6370");
            number = Color.parseColor("#d19a66");
            function = Color.parseColor("#61afef");
            type = Color.parseColor("#e5c07b");
            variable = Color.parseColor("#e06c75");
            operator = Color.parseColor("#56b6c2");
            bracket = Color.parseColor("#abb2bf");
            tag = Color.parseColor("#e06c75");
            attribute = Color.parseColor("#d19a66");
        }};
        
        public static Theme LIGHT = new Theme() {{
            background = Color.parseColor("#ffffff");
            lineNumberBg = Color.parseColor("#f5f5f5");
            lineNumber = Color.parseColor("#999999");
            text = Color.parseColor("#333333");
            keyword = Color.parseColor("#0000ff");
            string = Color.parseColor("#a31515");
            comment = Color.parseColor("#008000");
            number = Color.parseColor("#098658");
            function = Color.parseColor("#795e26");
            type = Color.parseColor("#267f99");
            variable = Color.parseColor("#001080");
            operator = Color.parseColor("#000000");
            bracket = Color.parseColor("#333333");
            tag = Color.parseColor("#800000");
            attribute = Color.parseColor("#ff0000");
        }};
    }
    
    private Theme currentTheme = Theme.MONOKAI;
    private String currentLanguage = "text";
    
    public void setTheme(Theme theme) {
        this.currentTheme = theme;
    }
    
    public Theme getTheme() {
        return currentTheme;
    }
    
    public void setLanguage(String language) {
        this.currentLanguage = language;
    }
    
    public SpannableStringBuilder highlight(String code) {
        SpannableStringBuilder builder = new SpannableStringBuilder(code);
        
        switch (currentLanguage) {
            case "python":
                highlightPython(builder);
                break;
            case "javascript":
            case "typescript":
                highlightJavaScript(builder);
                break;
            case "java":
            case "kotlin":
                highlightJava(builder);
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
            case "dart":
                highlightDart(builder);
                break;
            case "sql":
                highlightSQL(builder);
                break;
            case "php":
                highlightPHP(builder);
                break;
            default:
                highlightGeneric(builder);
        }
        
        return builder;
    }
    
    private void highlightPython(SpannableStringBuilder builder) {
        String[] keywords = {"def", "class", "if", "elif", "else", "for", "while", "try", "except", 
            "finally", "with", "as", "import", "from", "return", "yield", "lambda", "and", "or", 
            "not", "in", "is", "None", "True", "False", "pass", "break", "continue", "raise", 
            "global", "nonlocal", "assert", "del", "async", "await"};
        
        String[] builtins = {"print", "len", "range", "str", "int", "float", "list", "dict", 
            "set", "tuple", "bool", "type", "input", "open", "file", "map", "filter", "zip",
            "enumerate", "sorted", "reversed", "sum", "min", "max", "abs", "round"};
        
        applyKeywords(builder, keywords, currentTheme.keyword);
        applyKeywords(builder, builtins, currentTheme.function);
        applyStrings(builder);
        applyComments(builder, "#");
        applyNumbers(builder);
    }
    
    private void highlightJavaScript(SpannableStringBuilder builder) {
        String[] keywords = {"function", "const", "let", "var", "if", "else", "for", "while", 
            "do", "switch", "case", "break", "continue", "return", "try", "catch", "finally",
            "throw", "new", "delete", "typeof", "instanceof", "this", "class", "extends", 
            "static", "get", "set", "import", "export", "from", "default", "async", "await",
            "true", "false", "null", "undefined", "of", "in"};
        
        String[] builtins = {"console", "document", "window", "Array", "Object", "String", 
            "Number", "Boolean", "Date", "Math", "JSON", "Promise", "fetch", "setTimeout",
            "setInterval", "parseInt", "parseFloat", "isNaN", "isFinite"};
        
        applyKeywords(builder, keywords, currentTheme.keyword);
        applyKeywords(builder, builtins, currentTheme.type);
        applyStrings(builder);
        applyComments(builder, "//");
        applyMultilineComments(builder);
        applyNumbers(builder);
    }
    
    private void highlightJava(SpannableStringBuilder builder) {
        String[] keywords = {"public", "private", "protected", "class", "interface", "extends", 
            "implements", "static", "final", "abstract", "synchronized", "volatile", "transient",
            "native", "strictfp", "if", "else", "for", "while", "do", "switch", "case", "default",
            "break", "continue", "return", "try", "catch", "finally", "throw", "throws", "new",
            "this", "super", "instanceof", "import", "package", "void", "boolean", "byte", "char",
            "short", "int", "long", "float", "double", "true", "false", "null", "enum", "assert"};
        
        String[] types = {"String", "Integer", "Long", "Double", "Float", "Boolean", "Character",
            "Object", "Class", "System", "Math", "List", "ArrayList", "Map", "HashMap", "Set",
            "HashSet", "Collection", "Iterator", "Exception", "Thread", "Runnable"};
        
        applyKeywords(builder, keywords, currentTheme.keyword);
        applyKeywords(builder, types, currentTheme.type);
        applyStrings(builder);
        applyComments(builder, "//");
        applyMultilineComments(builder);
        applyNumbers(builder);
        applyAnnotations(builder);
    }
    
    private void highlightDart(SpannableStringBuilder builder) {
        String[] keywords = {"abstract", "as", "assert", "async", "await", "break", "case", 
            "catch", "class", "const", "continue", "covariant", "default", "deferred", "do",
            "dynamic", "else", "enum", "export", "extends", "extension", "external", "factory",
            "false", "final", "finally", "for", "Function", "get", "hide", "if", "implements",
            "import", "in", "interface", "is", "late", "library", "mixin", "new", "null", "on",
            "operator", "part", "required", "rethrow", "return", "set", "show", "static", "super",
            "switch", "sync", "this", "throw", "true", "try", "typedef", "var", "void", "while",
            "with", "yield"};
        
        String[] types = {"int", "double", "String", "bool", "List", "Map", "Set", "Future",
            "Stream", "Widget", "BuildContext", "State", "StatelessWidget", "StatefulWidget"};
        
        applyKeywords(builder, keywords, currentTheme.keyword);
        applyKeywords(builder, types, currentTheme.type);
        applyStrings(builder);
        applyComments(builder, "//");
        applyMultilineComments(builder);
        applyNumbers(builder);
        applyAnnotations(builder);
    }
    
    private void highlightHTML(SpannableStringBuilder builder) {
        Pattern tagPattern = Pattern.compile("</?\\w+[^>]*>");
        Matcher matcher = tagPattern.matcher(builder);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(currentTheme.tag), 
                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        Pattern attrPattern = Pattern.compile("\\w+(?=\\s*=)");
        matcher = attrPattern.matcher(builder);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(currentTheme.attribute), 
                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        applyStrings(builder);
        applyHTMLComments(builder);
    }
    
    private void highlightCSS(SpannableStringBuilder builder) {
        Pattern selectorPattern = Pattern.compile("[.#]?[\\w-]+(?=\\s*\\{)");
        Matcher matcher = selectorPattern.matcher(builder);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(currentTheme.tag), 
                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        Pattern propertyPattern = Pattern.compile("[\\w-]+(?=\\s*:)");
        matcher = propertyPattern.matcher(builder);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(currentTheme.attribute), 
                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        applyStrings(builder);
        applyMultilineComments(builder);
        applyNumbers(builder);
    }
    
    private void highlightJSON(SpannableStringBuilder builder) {
        Pattern keyPattern = Pattern.compile("\"[^\"]+\"(?=\\s*:)");
        Matcher matcher = keyPattern.matcher(builder);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(currentTheme.attribute), 
                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        
        applyStrings(builder);
        applyNumbers(builder);
        
        String[] keywords = {"true", "false", "null"};
        applyKeywords(builder, keywords, currentTheme.keyword);
    }
    
    private void highlightSQL(SpannableStringBuilder builder) {
        String[] keywords = {"SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE", 
            "SET", "DELETE", "CREATE", "TABLE", "DROP", "ALTER", "ADD", "INDEX", "JOIN", 
            "LEFT", "RIGHT", "INNER", "OUTER", "ON", "AND", "OR", "NOT", "IN", "LIKE", 
            "BETWEEN", "IS", "NULL", "ORDER", "BY", "GROUP", "HAVING", "LIMIT", "OFFSET",
            "AS", "DISTINCT", "COUNT", "SUM", "AVG", "MIN", "MAX", "UNION", "ALL"};
        
        for (String keyword : keywords) {
            applyKeywordCaseInsensitive(builder, keyword, currentTheme.keyword);
        }
        
        applyStrings(builder);
        applyComments(builder, "--");
        applyMultilineComments(builder);
        applyNumbers(builder);
    }
    
    private void highlightPHP(SpannableStringBuilder builder) {
        String[] keywords = {"function", "class", "public", "private", "protected", "static",
            "if", "else", "elseif", "for", "foreach", "while", "do", "switch", "case", "default",
            "break", "continue", "return", "try", "catch", "finally", "throw", "new", "extends",
            "implements", "interface", "abstract", "final", "const", "use", "namespace", "require",
            "include", "require_once", "include_once", "echo", "print", "true", "false", "null",
            "array", "as", "global", "isset", "unset", "empty"};
        
        applyKeywords(builder, keywords, currentTheme.keyword);
        applyPHPVariables(builder);
        applyStrings(builder);
        applyComments(builder, "//");
        applyComments(builder, "#");
        applyMultilineComments(builder);
        applyNumbers(builder);
    }
    
    private void highlightGeneric(SpannableStringBuilder builder) {
        applyStrings(builder);
        applyNumbers(builder);
    }
    
    private void applyKeywords(SpannableStringBuilder builder, String[] keywords, int color) {
        String text = builder.toString();
        for (String keyword : keywords) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b");
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                builder.setSpan(new ForegroundColorSpan(color), 
                    matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }
    
    private void applyKeywordCaseInsensitive(SpannableStringBuilder builder, String keyword, int color) {
        String text = builder.toString();
        Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(color), 
                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private void applyStrings(SpannableStringBuilder builder) {
        String text = builder.toString();
        Pattern pattern = Pattern.compile("\"[^\"\\\\]*(\\\\.[^\"\\\\]*)*\"|'[^'\\\\]*(\\\\.[^'\\\\]*)*'|`[^`]*`");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(currentTheme.string), 
                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private void applyComments(SpannableStringBuilder builder, String commentStart) {
        String text = builder.toString();
        Pattern pattern = Pattern.compile(Pattern.quote(commentStart) + ".*$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(currentTheme.comment), 
                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private void applyMultilineComments(SpannableStringBuilder builder) {
        String text = builder.toString();
        Pattern pattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(currentTheme.comment), 
                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private void applyHTMLComments(SpannableStringBuilder builder) {
        String text = builder.toString();
        Pattern pattern = Pattern.compile("<!--.*?-->", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(currentTheme.comment), 
                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private void applyNumbers(SpannableStringBuilder builder) {
        String text = builder.toString();
        Pattern pattern = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(currentTheme.number), 
                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private void applyAnnotations(SpannableStringBuilder builder) {
        String text = builder.toString();
        Pattern pattern = Pattern.compile("@\\w+");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(currentTheme.type), 
                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
    
    private void applyPHPVariables(SpannableStringBuilder builder) {
        String text = builder.toString();
        Pattern pattern = Pattern.compile("\\$\\w+");
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            builder.setSpan(new ForegroundColorSpan(currentTheme.variable), 
                matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
