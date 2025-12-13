package com.xyra.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

public class CodeExecutor {
    
    private static final String TAG = "CodeExecutor";
    
    private static final String PISTON_API = "https://emkc.org/api/v2/piston/execute";
    
    private Context context;
    private ExecutorService executor;
    private Handler handler;
    private WebView jsWebView;
    
    public interface ExecutionCallback {
        void onSuccess(ExecutionResult result);
        void onError(String error);
    }
    
    public static class ExecutionResult {
        public String language;
        public String output;
        public String error;
        public long executionTime;
        public boolean success;
        
        public ExecutionResult() {
            this.success = false;
        }
        
        public String getFormattedOutput() {
            StringBuilder sb = new StringBuilder();
            sb.append("**Hasil Eksekusi (").append(language).append(")**\n\n");
            
            if (success && output != null && !output.isEmpty()) {
                sb.append("```\n").append(output).append("\n```");
            } else if (error != null && !error.isEmpty()) {
                sb.append("**Error:**\n```\n").append(error).append("\n```");
            } else {
                sb.append("_Tidak ada output_");
            }
            
            sb.append("\n\n_Waktu eksekusi: ").append(executionTime).append("ms_");
            
            return sb.toString();
        }
    }
    
    public CodeExecutor(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    public void initJsWebView() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                jsWebView = new WebView(context);
                jsWebView.getSettings().setJavaScriptEnabled(true);
            }
        });
    }
    
    public void executeCode(final String code, final String language, final ExecutionCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                
                try {
                    ExecutionResult result;
                    
                    if (language.equalsIgnoreCase("javascript") || language.equalsIgnoreCase("js")) {
                        result = executeJavaScriptLocal(code);
                    } else {
                        result = executePistonAPI(code, language);
                    }
                    
                    result.executionTime = System.currentTimeMillis() - startTime;
                    
                    final ExecutionResult finalResult = result;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (finalResult.success) {
                                callback.onSuccess(finalResult);
                            } else {
                                callback.onError(finalResult.error != null ? finalResult.error : "Eksekusi gagal");
                            }
                        }
                    });
                    
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError("Error: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }
    
    private ExecutionResult executePistonAPI(String code, String language) {
        ExecutionResult result = new ExecutionResult();
        result.language = language;
        
        try {
            String pistonLang = mapLanguageToPiston(language);
            String version = getLanguageVersion(language);
            
            URL url = new URL(PISTON_API);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            
            JSONObject requestBody = new JSONObject();
            requestBody.put("language", pistonLang);
            requestBody.put("version", version);
            
            JSONObject file = new JSONObject();
            file.put("content", code);
            requestBody.put("files", new org.json.JSONArray().put(file));
            
            OutputStream os = conn.getOutputStream();
            os.write(requestBody.toString().getBytes("UTF-8"));
            os.close();
            
            int responseCode = conn.getResponseCode();
            
            BufferedReader reader;
            if (responseCode >= 200 && responseCode < 300) {
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            conn.disconnect();
            
            JSONObject jsonResponse = new JSONObject(response.toString());
            
            if (jsonResponse.has("run")) {
                JSONObject run = jsonResponse.getJSONObject("run");
                result.output = run.optString("stdout", "");
                result.error = run.optString("stderr", "");
                
                if (result.error.isEmpty() || !result.output.isEmpty()) {
                    result.success = true;
                }
            } else if (jsonResponse.has("message")) {
                result.error = jsonResponse.getString("message");
            }
            
        } catch (Exception e) {
            result.error = "Eksekusi gagal: " + e.getMessage();
        }
        
        return result;
    }
    
    private ExecutionResult executeJavaScriptLocal(String code) {
        final ExecutionResult result = new ExecutionResult();
        result.language = "JavaScript";
        
        try {
            String wrappedCode = 
                "(function() { " +
                "  var output = []; " +
                "  var console = { " +
                "    log: function() { output.push(Array.prototype.slice.call(arguments).join(' ')); }, " +
                "    error: function() { output.push('Error: ' + Array.prototype.slice.call(arguments).join(' ')); }, " +
                "    warn: function() { output.push('Warning: ' + Array.prototype.slice.call(arguments).join(' ')); } " +
                "  }; " +
                "  try { " +
                code + 
                "  } catch(e) { output.push('Error: ' + e.message); } " +
                "  return output.join('\\n'); " +
                "})()";
            
            result.output = evaluateSimpleJS(code);
            result.success = true;
            
        } catch (Exception e) {
            result.error = e.getMessage();
        }
        
        return result;
    }
    
    private String evaluateSimpleJS(String code) {
        StringBuilder output = new StringBuilder();
        
        Pattern printPattern = Pattern.compile("console\\.log\\(['\"]([^'\"]+)['\"]\\)");
        Matcher matcher = printPattern.matcher(code);
        
        while (matcher.find()) {
            output.append(matcher.group(1)).append("\n");
        }
        
        Pattern mathPattern = Pattern.compile("(\\d+)\\s*([+\\-*/])\\s*(\\d+)");
        Matcher mathMatcher = mathPattern.matcher(code);
        
        while (mathMatcher.find()) {
            try {
                int a = Integer.parseInt(mathMatcher.group(1));
                int b = Integer.parseInt(mathMatcher.group(3));
                String op = mathMatcher.group(2);
                int result = 0;
                
                switch (op) {
                    case "+": result = a + b; break;
                    case "-": result = a - b; break;
                    case "*": result = a * b; break;
                    case "/": result = b != 0 ? a / b : 0; break;
                }
                
                output.append(a).append(" ").append(op).append(" ").append(b)
                      .append(" = ").append(result).append("\n");
            } catch (Exception e) {
            }
        }
        
        if (output.length() == 0) {
            output.append("[Kode JavaScript diterima - untuk eksekusi penuh, gunakan browser atau Node.js]");
        }
        
        return output.toString().trim();
    }
    
    private String mapLanguageToPiston(String language) {
        String lower = language.toLowerCase();
        
        switch (lower) {
            case "python":
            case "py":
                return "python";
            case "javascript":
            case "js":
                return "javascript";
            case "java":
                return "java";
            case "c":
                return "c";
            case "cpp":
            case "c++":
                return "cpp";
            case "ruby":
            case "rb":
                return "ruby";
            case "go":
            case "golang":
                return "go";
            case "rust":
            case "rs":
                return "rust";
            case "php":
                return "php";
            case "swift":
                return "swift";
            case "kotlin":
            case "kt":
                return "kotlin";
            case "typescript":
            case "ts":
                return "typescript";
            case "bash":
            case "sh":
            case "shell":
                return "bash";
            case "lua":
                return "lua";
            case "perl":
                return "perl";
            case "r":
                return "r";
            case "scala":
                return "scala";
            case "csharp":
            case "c#":
                return "csharp";
            default:
                return "python";
        }
    }
    
    private String getLanguageVersion(String language) {
        String lower = language.toLowerCase();
        
        switch (lower) {
            case "python":
            case "py":
                return "3.10.0";
            case "javascript":
            case "js":
                return "18.15.0";
            case "java":
                return "15.0.2";
            case "c":
                return "10.2.0";
            case "cpp":
            case "c++":
                return "10.2.0";
            case "go":
            case "golang":
                return "1.16.2";
            case "rust":
            case "rs":
                return "1.68.2";
            case "typescript":
            case "ts":
                return "5.0.3";
            default:
                return "*";
        }
    }
    
    public String extractCodeFromMessage(String message) {
        Pattern codePattern = Pattern.compile("```(?:\\w+)?\\n([\\s\\S]*?)```");
        Matcher matcher = codePattern.matcher(message);
        
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }
    
    public String extractLanguageFromMessage(String message) {
        Pattern langPattern = Pattern.compile("```(\\w+)\\n");
        Matcher matcher = langPattern.matcher(message);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return "python";
    }
    
    public boolean containsExecutableCode(String message) {
        return message != null && message.contains("```") && message.contains("\n");
    }
    
    public static String[] getSupportedLanguages() {
        return new String[] {
            "Python", "JavaScript", "Java", "C", "C++", "Go", 
            "Rust", "Ruby", "PHP", "TypeScript", "Kotlin", 
            "Swift", "Bash", "Lua", "Perl", "R", "Scala", "C#"
        };
    }
    
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        
        if (jsWebView != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    jsWebView.destroy();
                }
            });
        }
    }
}
