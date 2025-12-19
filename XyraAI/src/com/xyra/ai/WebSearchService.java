package com.xyra.ai;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSearchService {
    
    private static final String TAG = "WebSearchService";
    
    private static final String DUCKDUCKGO_HTML_URL = "https://html.duckduckgo.com/html/?q=";
    private static final String GOOGLE_SEARCH_LITE = "https://www.google.com/search?q=";
    
    private Context context;
    private ExecutorService executor;
    private Handler handler;
    
    public interface SearchCallback {
        void onSuccess(List<SearchResult> results);
        void onError(String error);
    }
    
    public static class SearchResult {
        public String title;
        public String url;
        public String snippet;
        public String source;
        
        public SearchResult() {}
        
        public SearchResult(String title, String url, String snippet) {
            this.title = title;
            this.url = url;
            this.snippet = snippet;
        }
        
        @Override
        public String toString() {
            return title + " - " + snippet;
        }
    }
    
    public WebSearchService(Context context) {
        this.context = context;
        this.executor = Executors.newSingleThreadExecutor();
        this.handler = new Handler(Looper.getMainLooper());
    }
    
    public void search(final String query, final SearchCallback callback) {
        search(query, 5, callback);
    }
    
    public void search(final String query, final int maxResults, final SearchCallback callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<SearchResult> results = performDuckDuckGoSearch(query, maxResults);
                    
                    if (results.isEmpty()) {
                        results = performSimpleSearch(query, maxResults);
                    }
                    
                    final List<SearchResult> finalResults = results;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (finalResults.isEmpty()) {
                                callback.onError("Tidak ada hasil ditemukan");
                            } else {
                                callback.onSuccess(finalResults);
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
    
    private List<SearchResult> performDuckDuckGoSearch(String query, int maxResults) {
        List<SearchResult> results = new ArrayList<SearchResult>();
        
        try {
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            URL url = new URL(DUCKDUCKGO_HTML_URL + encodedQuery);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36");
            conn.setRequestProperty("Accept", "text/html");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            
            int responseCode = conn.getResponseCode();
            
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                results = parseDuckDuckGoResults(response.toString(), maxResults);
            }
            
            conn.disconnect();
            
        } catch (Exception e) {
            Log.e(TAG, "DuckDuckGo search failed: " + e.getMessage());
        }
        
        return results;
    }
    
    private List<SearchResult> parseDuckDuckGoResults(String html, int maxResults) {
        List<SearchResult> results = new ArrayList<SearchResult>();
        
        Pattern resultPattern = Pattern.compile(
            "<a[^>]*class=\"result__a\"[^>]*href=\"([^\"]+)\"[^>]*>([^<]+)</a>" +
            "[\\s\\S]*?<a[^>]*class=\"result__snippet\"[^>]*>([^<]+)</a>",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher matcher = resultPattern.matcher(html);
        
        while (matcher.find() && results.size() < maxResults) {
            SearchResult result = new SearchResult();
            result.url = decodeUrl(matcher.group(1));
            result.title = cleanHtml(matcher.group(2));
            result.snippet = cleanHtml(matcher.group(3));
            result.source = "DuckDuckGo";
            
            if (isValidResult(result)) {
                results.add(result);
            }
        }
        
        if (results.isEmpty()) {
            Pattern simplePattern = Pattern.compile(
                "<a[^>]*href=\"([^\"]+)\"[^>]*>([^<]{10,})</a>",
                Pattern.CASE_INSENSITIVE
            );
            
            Matcher simpleMatcher = simplePattern.matcher(html);
            
            while (simpleMatcher.find() && results.size() < maxResults) {
                String url = simpleMatcher.group(1);
                String title = simpleMatcher.group(2);
                
                if (url.startsWith("http") && !url.contains("duckduckgo.com")) {
                    SearchResult result = new SearchResult();
                    result.url = url;
                    result.title = cleanHtml(title);
                    result.snippet = "";
                    result.source = "Web";
                    results.add(result);
                }
            }
        }
        
        return results;
    }
    
    private List<SearchResult> performSimpleSearch(String query, int maxResults) {
        List<SearchResult> results = new ArrayList<SearchResult>();
        
        SearchResult result = new SearchResult();
        result.title = "Pencarian: " + query;
        result.url = "https://www.google.com/search?q=" + query.replace(" ", "+");
        result.snippet = "Klik untuk mencari di Google";
        result.source = "Google";
        results.add(result);
        
        result = new SearchResult();
        result.title = "Wikipedia: " + query;
        result.url = "https://id.wikipedia.org/wiki/" + query.replace(" ", "_");
        result.snippet = "Cari di Wikipedia Indonesia";
        result.source = "Wikipedia";
        results.add(result);
        
        return results;
    }
    
    private String decodeUrl(String url) {
        if (url == null) return "";
        
        if (url.contains("uddg=")) {
            int start = url.indexOf("uddg=") + 5;
            int end = url.indexOf("&", start);
            if (end == -1) end = url.length();
            
            try {
                return java.net.URLDecoder.decode(url.substring(start, end), "UTF-8");
            } catch (Exception e) {
                return url;
            }
        }
        
        return url;
    }
    
    private String cleanHtml(String text) {
        if (text == null) return "";
        
        text = text.replaceAll("<[^>]+>", "");
        text = text.replaceAll("&nbsp;", " ");
        text = text.replaceAll("&amp;", "&");
        text = text.replaceAll("&lt;", "<");
        text = text.replaceAll("&gt;", ">");
        text = text.replaceAll("&quot;", "\"");
        text = text.replaceAll("&#39;", "'");
        text = text.replaceAll("\\s+", " ");
        
        return text.trim();
    }
    
    private boolean isValidResult(SearchResult result) {
        if (result.title == null || result.title.isEmpty()) return false;
        if (result.url == null || !result.url.startsWith("http")) return false;
        
        return true;
    }
    
    public String formatResultsForAI(List<SearchResult> results, String query) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[WEB SEARCH RESULTS]\n");
        sb.append("Query: ").append(query).append("\n\n");
        
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            sb.append((i + 1)).append(". **").append(result.title).append("**\n");
            sb.append("   URL: ").append(result.url).append("\n");
            if (result.snippet != null && !result.snippet.isEmpty()) {
                sb.append("   ").append(result.snippet).append("\n");
            }
            sb.append("\n");
        }
        
        sb.append("[END SEARCH RESULTS]\n\n");
        sb.append("Berdasarkan hasil pencarian di atas, ");
        
        return sb.toString();
    }
    
    public boolean containsSearchRequest(String message) {
        if (message == null) return false;
        
        String lower = message.toLowerCase();
        
        String[] searchKeywords = {
            "cari di internet",
            "cari di web",
            "search internet",
            "search web",
            "google",
            "browse",
            "cari online",
            "cari info terbaru",
            "berita terbaru",
            "latest news",
            "apa kabar terbaru",
            "cari tahu",
            "tolong cari",
            "bantu cari",
            "carilah",
            "searching"
        };
        
        for (String keyword : searchKeywords) {
            if (lower.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    public String extractSearchQuery(String message) {
        if (message == null) return message;
        
        String[] prefixes = {
            "cari di internet tentang ",
            "cari di web tentang ",
            "cari info tentang ",
            "tolong cari ",
            "bantu cari ",
            "search for ",
            "google ",
            "cari ",
            "search "
        };
        
        String lower = message.toLowerCase();
        for (String prefix : prefixes) {
            if (lower.startsWith(prefix)) {
                return message.substring(prefix.length()).trim();
            }
        }
        
        return message;
    }
    
    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
