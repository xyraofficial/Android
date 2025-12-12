package com.xyra.ai;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class OAuthActivity extends Activity {
    
    private WebView webView;
    private ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.applyTheme(this);
        
        setContentView(R.layout.activity_oauth);
        
        webView = (WebView) findViewById(R.id.webView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        
        setupWebView();
        
        String oauthUrl = getIntent().getStringExtra("oauth_url");
        if (oauthUrl != null && !oauthUrl.isEmpty()) {
            webView.loadUrl(oauthUrl);
        } else {
            finish();
        }
    }
    
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36");
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return handleUrl(url);
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }
        });
        
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progressBar != null) {
                    progressBar.setProgress(newProgress);
                }
            }
        });
    }
    
    private boolean handleUrl(String url) {
        if (url.contains("android-rho-five.vercel.app") || url.contains("access_token=")) {
            Uri uri = Uri.parse(url.replace("#", "?"));
            
            String accessToken = uri.getQueryParameter("access_token");
            String refreshToken = uri.getQueryParameter("refresh_token");
            
            if (accessToken == null && url.contains("#")) {
                String fragment = url.substring(url.indexOf("#") + 1);
                String[] params = fragment.split("&");
                for (String param : params) {
                    if (param.startsWith("access_token=")) {
                        accessToken = param.substring("access_token=".length());
                    } else if (param.startsWith("refresh_token=")) {
                        refreshToken = param.substring("refresh_token=".length());
                    }
                }
            }
            
            if (accessToken != null && !accessToken.isEmpty()) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("access_token", accessToken);
                resultIntent.putExtra("refresh_token", refreshToken != null ? refreshToken : "");
                setResult(RESULT_OK, resultIntent);
                finish();
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            setResult(RESULT_CANCELED);
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
